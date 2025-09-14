package com.storm.tornadoai

import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class SearchResult(val title: String, val url: String)

object WebSearch {
    private val http = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    fun duckDuckGo(query: String, max: Int = 5): List<SearchResult> {
        val enc = URLEncoder.encode(query, Charsets.UTF_8)
        val url = "https://duckduckgo.com/html/?q=$enc"
        val req = Request.Builder().url(url).header("User-Agent", ua()).build()
        http.newCall(req).execute().use { resp ->
            val body = resp.body?.string().orEmpty()
            val doc: Document = Jsoup.parse(body)
            return doc.select("a.result__a").take(max).map {
                SearchResult(title = it.text(), url = it.attr("href"))
            }
        }
    }

    fun fetch(url: String): String {
        val req = Request.Builder().url(url).header("User-Agent", ua()).build()
        http.newCall(req).execute().use { resp ->
            return resp.body?.string().orEmpty()
        }
    }

    private fun ua() = "Mozilla/5.0 (Android) TornadoAI/1.0"
}