package com.storm.tornadoai

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URI
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.ln

data class SearchResult(val title: String, val url: String, val snippet: String)

fun SearchResult.urlDomain(): String? = try {
    URI(this.url).host?.lowercase(Locale.US)?.removePrefix("www.")
} catch (_: Throwable) { null }

/** DUCKDUCKGO HTML scraping (no API, pure Kotlin). */
class DuckDuckGoSearchService {
    private val client = OkHttpClient()

    suspend fun search(query: String, limit: Int): List<SearchResult> {
        val q = URLEncoder.encode(query, "UTF-8")
        val url = "https://duckduckgo.com/html/?q=$q&kl=us-en"
        val html = httpGet(url)
        if (html.isBlank()) return emptyList()
        val doc = Jsoup.parse(html)

        // Results live in .result__a (title link) with snippet nearby
        val out = mutableListOf<SearchResult>()
        val links = doc.select(".result__a")
        for (a in links) {
            val title = a.text()
            val href = a.absUrl("href")
            if (href.isBlank()) continue
            val container = a.closest(".result")
            val snip = container?.selectFirst(".result__snippet")?.text().orEmpty()
            out += SearchResult(title, href, snip)
            if (out.size >= limit) break
        }
        return out
    }

    private fun httpGet(url: String): String {
        return try {
            val req = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
            client.newCall(req).execute().use { r -> r.body?.string().orEmpty() }
        } catch (_: Throwable) { "" }
    }
}

/** RSS-based search: read feeds, filter items matching query. */
class RssSearchService(private val context: Context) {
    private val client = OkHttpClient()

    suspend fun search(query: String, limit: Int): List<SearchResult> {
        val feeds = runCatching {
            context.assets.open("news_sources.txt").bufferedReader().readLines()
        }.getOrElse { emptyList() }

        val q = query.lowercase(Locale.US)
        val out = mutableListOf<SearchResult>()
        for (feed in feeds) {
            val xml = httpGet(feed.trim())
            if (xml.isBlank()) continue
            val doc = Jsoup.parse(xml, "", org.jsoup.parser.Parser.xmlParser())
            val chTitle = doc.selectFirst("channel>title")?.text().orEmpty()
            for (item in doc.select("item")) {
                val title = item.selectFirst("title")?.text().orEmpty()
                val link = item.selectFirst("link")?.text().orEmpty()
                val desc = item.selectFirst("description")?.text().orEmpty()
                val hay = (title + " " + desc).lowercase(Locale.US)
                if (scoreMatch(hay, q) > 0.0) {
                    out += SearchResult("$chTitle: $title", link, desc)
                    if (out.size >= limit) return out
                }
            }
        }
        return out.take(limit)
    }

    private fun httpGet(url: String): String {
        return try {
            val req = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
            client.newCall(req).execute().use { r -> r.body?.string().orEmpty() }
        } catch (_: Throwable) { "" }
    }
}

/** Tiny domain crawler */
class DomainCrawler {
    private val client = OkHttpClient()

    suspend fun crawlSeeds(seeds: List<String>, query: String, maxPages: Int): List<SearchResult> {
        val q = query.lowercase(Locale.US)
        val results = mutableListOf<Pair<Double, SearchResult>>()
        val visited = mutableSetOf<String>()

        fun addPage(url: String, doc: Document) {
            val text = doc.body()?.text().orEmpty()
            val s = scoreMatch(text.lowercase(Locale.US), q)
            if (s > 0.0) {
                val title = doc.title().ifBlank { url }
                val snippet = makeSnippet(text, q)
                results += s to SearchResult(title, url, snippet)
            }
        }

        for (seed in seeds) {
            if (results.size >= maxPages) break
            val homeUrl = "https://$seed/"
            val homeDoc = fetchDoc(homeUrl) ?: continue
            addPage(homeUrl, homeDoc)
            visited += homeUrl

            val links = homeDoc.select("a[href]").mapNotNull { it.absUrl("href") }
                .filter { link -> link.contains(seed) }
                .distinct().take(25)

            for (link in links) {
                if (results.size >= maxPages) break
                if (!visited.add(link)) continue
                val d = fetchDoc(link) ?: continue
                addPage(link, d)
            }
        }

        return results.sortedByDescending { it.first }.map { it.second }.distinctBy { it.url }.take(maxPages)
    }

    private fun fetchDoc(url: String): Document? {
        return try {
            val req = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
            client.newCall(req).execute().use { r ->
                val html = r.body?.string().orEmpty()
                if (html.isBlank()) null else Jsoup.parse(html, url)
            }
        } catch (_: Throwable) { null }
    }
}

/** HTML extractor */
class HtmlFetcher {
    private val client = OkHttpClient()
    fun fetchText(url: String): String {
        return try {
            val req = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
            client.newCall(req).execute().use { resp ->
                val html = resp.body?.string().orEmpty()
                val doc = Jsoup.parse(html, url)
                doc.select("script,style,nav,header,footer,form,aside").remove()
                doc.select("p,li,h2,h3").joinToString("\n") { it.text() }
            }
        } catch (_: Throwable) { "" }
    }
}

/** Heuristic scorer (BM25-ish lite) */
fun scoreMatch(text: String, query: String): Double {
    if (query.isBlank() || text.isBlank()) return 0.0
    val terms = query.split(Regex("\\s+")).filter { it.isNotBlank() }
    val len = text.length.coerceAtLeast(1)
    var score = 0.0
    for (t in terms) {
        val c = countOccurrences(text, t.lowercase(Locale.US))
        if (c > 0) score += (1 + ln(c + 1.0)) / ln(len.toDouble() + 10)
    }
    return score
}

fun countOccurrences(hay: String, needle: String): Int {
    var i = 0; var c = 0
    while (true) {
        val j = hay.indexOf(needle, i, ignoreCase = true)
        if (j < 0) break
        c++; i = j + needle.length
    }
    return c
}

fun makeSnippet(text: String, query: String, maxLen: Int = 260): String {
    val term = query.split(Regex("\\s+")).firstOrNull()?.lowercase(Locale.US) ?: ""
    val idx = text.lowercase(Locale.US).indexOf(term)
    return if (idx >= 0) {
        val start = (idx - 120).coerceAtLeast(0)
        val end = (idx + 140).coerceAtMost(text.length)
        text.substring(start, end).trim()
    } else text.take(maxLen)
}

/** “Connect the dots” summary */
object Summarizer {
    fun connect(query: String, corpus: List<String>, pages: List<Pair<SearchResult, String>>): String {
        val bullets = mutableListOf<String>()
        if (corpus.isNotEmpty()) {
            bullets += "From your corpus:"
            corpus.forEach { bullets += "• $it" }
        }
        val keyLines = pages.asSequence()
            .flatMap { (_, text) -> text.lines().asSequence() }
            .map { it.trim() }
            .filter { it.length in 60..240 && scoreMatch(it.lowercase(Locale.US), query.lowercase(Locale.US)) > 0.0 }
            .distinct()
            .take(10)
            .toList()
        if (keyLines.isNotEmpty()) {
            bullets += "From the web:"
            keyLines.forEach { bullets += "• $it" }
        }
        if (bullets.isEmpty()) return "I couldn’t correlate much for “$query”. Try a more specific query."
        return "Here’s a stitched view of **$query**:\n\n" + bullets.joinToString("\n")
    }
}

/** Corpus reader (optional corpus.db placed in app files directory). */
class CorpusReader(private val context: Context) {
    fun search(query: String): List<String> {
        val dbFile = context.getFileStreamPath("corpus.db")
        if (dbFile.exists()) {
            try {
                val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                    dbFile.path, null, android.database.sqlite.SQLiteDatabase.OPEN_READONLY
                )
                val hits = mutableListOf<String>()
                val tables = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
                while (tables.moveToNext()) {
                    val t = tables.getString(0)
                    val c = db.rawQuery("PRAGMA table_info($t)", null)
                    val textCols = mutableListOf<String>()
                    while (c.moveToNext()) {
                        val name = c.getString(1)
                        val type = (c.getString(2) ?: "").lowercase(Locale.US)
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
            } catch (_: Throwable) { /* ignore */ }
        }
        // fallback asset (optional)
        return runCatching {
            context.assets.open("corpus_stub.txt").bufferedReader().readLines()
                .filter { it.contains(query, ignoreCase = true) }
                .take(5)
        }.getOrElse { emptyList() }
    }
}