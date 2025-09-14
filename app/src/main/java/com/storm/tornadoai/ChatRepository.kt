package com.storm.tornadoai

import android.content.Context

class ChatRepository(context: Context) {

    // No external search API: we combine RSS results and a tiny crawler
    private val rss = RssSearchService(context)
    private val crawler = DomainCrawler()
    private val fetcher = HtmlFetcher()
    private val corpus = CorpusReader(context)

    suspend fun answer(query: String): AnswerBundle {
        // 1) corpus snippets
        val corpusSnips = corpus.search(query).take(5)

        // 2) RSS “search”
        val rssHits = rss.search(query, 10)

        // 3) Lightweight crawl from RSS domains (broadens coverage)
        val seeds = rssHits.mapNotNull { it.urlDomain() }.distinct().take(3)
        val crawled = crawler.crawlSeeds(seeds, query, maxPages = 12)

        // Merge and de-dup by url
        val results = (rssHits + crawled).distinctBy { it.url }.take(10)

        // 4) Fetch + extract text
        val pages = results.map { it to fetcher.fetchText(it.url) }

        // 5) Summarize
        val summary = Summarizer.connect(query, corpusSnips, pages)

        // 6) Build source cards with color coding
        val sources = results.mapIndexed { i, r ->
            SourceCard(
                title = r.title,
                url = r.url,
                snippet = r.snippet.take(240),
                colorIndex = i % SourceCard.PALETTE.size
            )
        }

        return AnswerBundle(summary, sources)
    }
}

data class AnswerBundle(val answer: String, val sources: List<SourceCard>)