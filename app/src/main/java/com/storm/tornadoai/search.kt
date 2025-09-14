package com.storm.tornadoai

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URLEncoder

data class SearchResult(val title: String, val url: String, val snippet: String)

interface WebSearchService {
    suspend fun search(query: String, limit: Int = 10): List<SearchResult>
}

/** Optional Bing Web Search (set BING_KEY in local.properties) */
class BingSearchService(private val key: String) : WebSearchService {
    private val client = OkHttpClient()
    override suspend fun search(query: String, limit: Int): List<SearchResult> {
        val q = URLEncoder.encode(query, "UTF-8")
        val req = Request.Builder()
            .url("https://api.bing.microsoft.com/v7.0/search?q=$q&count=$limit&textDecorations=false&textFormat=Raw")
            .addHeader("Ocp-Apim-Subscription-Key", key)
            .build()
        client.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return emptyList()
            val body = resp.body?.string().orEmpty()
            // Very light parsing to avoid full JSON models
            // Extract "name","url","snippet" fields using Jsoup-less scan:
            val results = mutableListOf<SearchResult>()
            // naive parse (sufficient for quick wiring)
            val regex = Regex("\\{\\s*\"name\":\"(.*?)\".*?\"url\":\"(.*?)\".*?\"snippet\":\"(.*?)\".*?\\}", RegexOption.DOT_MATCHES_ALL)
            regex.findAll(body).take(limit).forEach { m ->
                results += SearchResult(
                    title = m.groupValues[1].replace("\\u003c", "<").replace("\\u003e", ">"),
                    url = m.groupValues[2],
                    snippet = m.groupValues[3]
                )
            }
            return results
        }
    }
}

/** RSS fallback: uses app/src/main/assets/news_sources.txt */
class RssSearchService(private val context: Context) : WebSearchService {
    private val client = OkHttpClient()
    override suspend fun search(query: String, limit: Int): List<SearchResult> {
        val feeds = context.assets.open("news_sources.txt").bufferedReader().readLines()
            .filter { it.isNotBlank() }.take(10)
        val items = mutableListOf<SearchResult>()
        for (f in feeds) {
            val req = Request.Builder().url(f.trim()).build()
            client.newCall(req).execute().use { resp ->
                val xml = resp.body?.string().orEmpty()
                val doc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser())
                val channelTitle = doc.selectFirst("channel>title")?.text().orEmpty()
                doc.select("item").forEach { item ->
                    val title = item.selectFirst("title")?.text().orEmpty()
                    val link = item.selectFirst("link")?.text().orEmpty()
                    val desc = item.selectFirst("description")?.text().orEmpty()
                    if ((title + desc).contains(query, ignoreCase = true)) {
                        items += SearchResult("$channelTitle: $title", link, desc)
                    }
                }
            }
            if (items.size >= limit) break
        }
        return items.take(limit)
    }
}

/** Fetch raw HTML and extract readable text */
class HtmlFetcher {
    private val client = OkHttpClient()
    fun fetchText(url: String): String {
        return try {
            val req = Request.Builder().url(url).build()
            client.newCall(req).execute().use { resp ->
                val html = resp.body?.string().orEmpty()
                val doc = Jsoup.parse(html)
                // strip scripts/nav and get paragraphs
                doc.select("script,style,nav,header,footer").remove()
                doc.select("p,li").joinToString("\n") { it.text() }
            }
        } catch (_: Throwable) { "" }
    }
}

/** Dumb-but-useful “connect the dots” summarizer */
object Summarizer {
    fun connect(query: String, corpus: List<String>, pages: List<Pair<SearchResult, String>>): String {
        val bullets = mutableListOf<String>()
        if (corpus.isNotEmpty()) bullets += "From your corpus:"
        corpus.forEach { bullets += "• $it" }

        val keyLines = pages.asSequence()
            .flatMap { (_, text) -> text.lines().asSequence() }
            .map { it.trim() }
            .filter { it.length in 60..240 && it.contains(Regex("\\b(${query.split(" ").take(3).joinToString("|")})\\b", RegexOption.IGNORE_CASE)) }
            .distinct()
            .take(8)
            .toList()

        if (keyLines.isNotEmpty()) {
            bullets += "From the web:"
            keyLines.forEach { bullets += "• $it" }
        }

        if (bullets.isEmpty()) return "I didn’t find much. Try rephrasing or be more specific."
        return "Here’s what I found tying **$query** together:\n\n" + bullets.joinToString("\n")
    }
}

/** Split long text into tweet-sized chunks */
object TweetGenerator {
    fun splitIntoTweets(text: String, maxLen: Int = 270): List<String> {
        val words = text.replace("\n", " ").split(" ")
        val out = mutableListOf<String>()
        var cur = StringBuilder()
        for (w in words) {
            if (cur.length + 1 + w.length > maxLen) {
                out += cur.toString().trim()
                cur = StringBuilder()
            }
            cur.append(' ').append(w)
        }
        if (cur.isNotBlank()) out += cur.toString().trim()
        return out.take(20)
    }
}

/** Simple reader for an optional prepackaged or downloaded corpus.db
 *  Strategy: open any readable DB at /data/data/<pkg>/files/corpus.db or assets/corpus_stub.txt.
 */
class CorpusReader(private val context: Context) {
    fun search(query: String): List<String> {
        // 1) try app-internal corpus.db (if user manually placed it via adb or file manager)
        val dbFile = context.getFileStreamPath("corpus.db")
        if (dbFile.exists()) {
            try {
                val db = android.database.sqlite.SQLiteDatabase.openDatabase(dbFile.path, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY)
                // naive search across text-like columns for first table we find
                val tables = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
                val hits = mutableListOf<String>()
                while (tables.moveToNext()) {
                    val t = tables.getString(0)
                    val c = db.rawQuery("PRAGMA table_info($t)", null)
                    val textCols = mutableListOf<String>()
                    while (c.moveToNext()) {
                        val name = c.getString(1)
                        val type = (c.getString(2) ?: "").lowercase()
                        if (type.contains("text") || type.contains("char")) textCols += name
                    }
                    c.close()
                    if (textCols.isNotEmpty()) {
                        val where = textCols.joinToString(" OR ") { "$it LIKE ?" }
                        val args = arrayOf("%$query%")
                        val q = db.rawQuery("SELECT ${textCols.first()} FROM $t WHERE $where LIMIT 5", args)
                        while (q.moveToNext()) hits += q.getString(0)
                        q.close()
                        if (hits.size >= 5) { tables.close(); db.close(); return hits.take(5) }
                    }
                }
                tables.close(); db.close()
                return hits.take(5)
            } catch (_: Throwable) { /* ignore and fallback */ }
        }
        // 2) fallback to a small asset file if present
        return runCatching {
            context.assets.open("corpus_stub.txt").bufferedReader().readLines()
                .filter { it.contains(query, ignoreCase = true) }
                .take(5)
        }.getOrElse { emptyList() }
    }
}