package com.storm.tornadoai

import android.content.Context

class ChatRepository(context: Context) {

    private val search: WebSearchService = if (BuildConfig.BING_KEY.isNotEmpty())
        BingSearchService(BuildConfig.BING_KEY)
    else
        RssSearchService(context) // fallback: first 10 items from your feeds

    private val fetcher = HtmlFetcher()
    private val corpus = CorpusReader(context)

    suspend fun answer(query: String): AnswerBundle {
        // 1) query corpus.db (optional)
        val corpusSnips = corpus.search(query).take(5)

        // 2) web search (up to 10)
        val results = search.search(query, 10)

        // 3) fetch & extract text
        val pages = results.map { it to fetcher.fetchText(it.url) }

        // 4) connect the dots (very simple heuristic summarizer)
        val summary = Summarizer.connect(query, corpusSnips, pages)

        // 5) build colored, padded sources list
        val sources = results.mapIndexed { i, r ->
            SourceCard(
                title = r.title,
                url = r.url,
                snippet = r.snippet.ifEmpty { pages.firstOrNull { it.first.url == r.url }?.second?.take(200) ?: "" },
                colorIndex = i % SourceCard.PALETTE.size
            )
        }

        return AnswerBundle(summary, sources)
    }
}

data class AnswerBundle(val answer: String, val sources: List<SourceCard>)