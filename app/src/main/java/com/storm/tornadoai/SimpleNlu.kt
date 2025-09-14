package com.storm.tornadoai

data class Utterance(val text: String)
data class NluResult(val intent: Responder.Intent, val sentiment: Sentiment)

object SimpleNlu {
    private val hello = listOf("hi","hello","hey","yo")
    private val askHelp = listOf("help","how do i","what do i do","assist")
    private val deny = listOf("no","nah","nope","don’t","dont")

    private val pos = setOf("great","good","awesome","love","thanks","thank you")
    private val neg = setOf("bad","hate","terrible","angry","upset","wtf","sucks")

    fun analyze(utt: Utterance): NluResult {
        val t = utt.text.lowercase()
        val intent = when {
            hello.any { t.contains(it) } -> Responder.Intent.GREETING
            askHelp.any { t.contains(it) } -> Responder.Intent.HELP
            deny.any { t.contains(it) } -> Responder.Intent.DENY
            else -> Responder.Intent.OTHER
        }
        val sent = when {
            pos.any { t.contains(it) } -> Sentiment.POSITIVE
            neg.any { t.contains(it) } -> Sentiment.NEGATIVE
            else -> Sentiment.NEUTRAL
        }
        return NluResult(intent = intent, sentiment = sent)
    }
}