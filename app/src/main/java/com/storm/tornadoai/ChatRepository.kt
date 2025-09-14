package com.storm.tornadoai

import android.content.Context

class ChatRepository(context: Context) {

    private val corpus = CorpusReader(context)
    private val ddg = DuckDuckGoSearchService()     // generic web search (HTML scrape)
    private val rss = RssSearchService(context)     // news feeds (optional)
    private val crawler = DomainCrawler()           // tiny domain crawl
    private val fetcher = HtmlFetcher()

    suspend fun answer(query: String): AnswerBundle {
        // 1) corpus snippets
        val corpusSnips = corpus.search(query).take(5)

        // 2) web search first (DDG HTML results for *anything*)
        val ddgHits = ddg.search(query, 10)

        // 3) news RSS hits (optional)
        val rssHits = rss.search(query, 10)

        // 4) crawl a few domains from both sets
        val seeds = (ddgHits + rssHits).mapNotNull { it.urlDomain() }.distinct().take(3)
        val crawled = crawler.crawlSeeds(seeds, query, maxPages = 12)

        // Merge and de-dup
        val all = (ddgHits + rssHits + crawled).distinctBy { it.url }.take(15)

        // 5) Fetch bodies and summarize
        val pages = all.map { it to fetcher.fetchText(it.url) }
        val summary = Summarizer.connect(query, corpusSnips, pages)

        // 6) Build source cards with bias tagging + neutral colors
        val sources = all.mapIndexed { i, r ->
            val bias = BiasClassifier.classify(r.url)
            SourceCard(
                title = r.title,
                url = r.url,
                snippet = r.snippet.take(320),
                colorIndex = i % SourceCard.PALETTE.size,
                bias = bias
            )
        }

        return AnswerBundle(summary, sources)
    }
}

data class AnswerBundle(val answer: String, val sources: List<SourceCard>)