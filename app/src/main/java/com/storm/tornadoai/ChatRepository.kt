package com.storm.tornadoai

import android.content.Context

import com.storm.tornadoai.logic.BiasClassifier

class ChatRepository(context: Context) {

    private val corpus = CorpusReader(context)
    private val ddg = DuckDuckGoSearchService()
    private val rss = RssSearchService(context)
    private val crawler = DomainCrawler()
    private val fetcher = HtmlFetcher()

    suspend fun answer(query: String): AnswerBundle {
        // 1) Pull from local corpus
        val corpusSnips = corpus.search(query).take(5)

        // 2) Web search (pure Kotlin scraping)
        val ddgHits = ddg.search(query, 10)
        val rssHits = rss.search(query, 10)

        // 3) Crawl top domains for more on-topic pages
        val seeds = (ddgHits + rssHits).mapNotNull { it.urlDomain() }.distinct().take(3)
        val crawled = crawler.crawlSeeds(seeds, query, maxPages = 12)

        // 4) Merge + fetch page text
        val all = (ddgHits + rssHits + crawled).distinctBy { it.url }.take(15)
        val pages: List<Pair<SearchResult, String>> = all.map { it to fetcher.fetchText(it.url) }

        // 5) Stitch a research-style answer
        val summary = Summarizer.connect(query, corpusSnips, pages)

        // 6) Source cards with bias tags & palette index
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