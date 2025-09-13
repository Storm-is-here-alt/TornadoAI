package com.storm.tornadoai

/**
 * Minimal NLU types + a very simple keyword NLU so the project compiles cleanly.
 * This file defines:
 *  - Utterance     : container for input text
 *  - NluResult     : output with intent + sentiment
 *  - SimpleNlu     : analyzer returning a Responder.Intent and Sentiment
 *
 * It relies on:
 *  - Responder.Intent   (defined in Responder.kt)
 *  - Sentiment          (defined in Models.kt)
 */

data class Utterance(val text: String)

data class NluResult(
    val intent: Responder.Intent,
    val sentiment: Sentiment
)

object SimpleNlu {

    private val hello = listOf("hi", "hello", "hey", "yo", "greetings")
    private val askHelp = listOf("help", "how do i", "what do i do", "can you help", "assist")
    private val deny = listOf("no", "nah", "nope", "don’t", "dont", "deny")

    private val positiveWords = setOf("great", "good", "awesome", "love", "thanks", "thank you")
    private val negativeWords = setOf("bad", "hate", "terrible", "angry", "upset", "wtf", "sucks")

    fun analyze(utt: Utterance): NluResult {
        val t = utt.text.lowercase()

        val intent = when {
            hello.any { t.contains(it) } -> Responder.Intent.GREETING
            askHelp.any { t.contains(it) } -> Responder.Intent.HELP
            deny.any { t.contains(it) } -> Responder.Intent.DENY
            else -> Responder.Intent.OTHER
        }

        val sentiment = when {
            positiveWords.any { t.contains(it) } -> Sentiment.POSITIVE
            negativeWords.any { t.contains(it) } -> Sentiment.NEGATIVE
            else -> Sentiment.NEUTRAL
        }

        return NluResult(intent = intent, sentiment = sentiment)
    }
}