package com.storm.tornadoai.conversation

/**
 * Simple, compile-safe responder that adds a human-friendly tone.
 * No Android dependencies; pure Kotlin so it builds on CI.
 */
object Responder {

    // ✅ Correct, consistent names (these caused the compile failure before)
    private const val empathyPrefix = "I hear you — "
    private const val empathyPrefixHere = "Here’s what I can do: "
    private const val empathyPrefixNot = "I can’t do that exactly, but "

    fun reply(userText: String): String {
        val text = userText.trim()
        if (text.isEmpty()) return "${empathyPrefix}could you share a bit more so I can help?"

        return when (detectIntent(text)) {
            Intent.GREETING -> "${empathyPrefix}${friendlyGreeting()}"
            Intent.HELP     -> "${empathyPrefixHere}${suggestHelp(text)}"
            Intent.DENY     -> "${empathyPrefixNot}${offerAlternative(text)}"
            Intent.OTHER    -> "${empathyPrefix}${reflect(text)}"
        }
    }

    // --- very small “intent” detector (kept dumb on purpose) ---
    private fun detectIntent(t: String): Intent {
        val s = t.lowercase()
        return when {
            listOf("hi", "hey", "hello", "yo").any { s.startsWith(it) } -> Intent.GREETING
            listOf("help", "how do i", "how to", "what do i").any { it in s } -> Intent.HELP
            listOf("can't", "cannot", "won't", "dont want", "don't want").any { it in s } -> Intent.DENY
            else -> Intent.OTHER
        }
    }

    private enum class Intent { GREETING, HELP, DENY, OTHER }

    // --- tiny response helpers ---
    private fun friendlyGreeting() =
        "thanks for reaching out. What would you like to do first?"

    private fun suggestHelp(text: String) =
        "here are a couple options I can walk you through based on what you said: “$text”."

    private fun offerAlternative(text: String) =
        "we can still make progress. Given “$text”, here’s a nearby option that should work."

    private fun reflect(text: String) =
        "you said “$text”. Want me to break that into steps or just do the first step?"
}