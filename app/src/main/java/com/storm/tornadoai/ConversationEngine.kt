package com.storm.tornadoai.conversation

import kotlin.random.Random

enum class Sentiment { POSITIVE, NEGATIVE, NEUTRAL, MIXED }

data class NLUResult(
    val intent: String,
    val sentiment: Sentiment,
    val entities: Map<String, String> = emptyMap()
)

object Responder {

    private val genericFallback = listOf(
        "Can you tell me more?",
        "I'm listening.",
        "What makes you feel that way?",
        "Go on, I'm here."
    )

    fun reply(nlu: NLUResult): String {
        val name = nlu.entities["name"]?.let { " $it" } ?: ""

        // --- Human-like empathy layer ---
        val empathyPrefix = when (nlu.sentiment) {
            Sentiment.NEGATIVE -> "I hear your frustration$name. "
            Sentiment.MIXED    -> "Got it$name—mixed feelings are normal. "
            Sentiment.POSITIVE -> ""
            Sentiment.NEUTRAL  -> ""
        }

        return when (nlu.intent.lowercase()) {
            "greeting" -> "$empathyPrefixHello$name! How’s it going?"
            "goodbye"  -> "$empathyPrefixTake care$name. Talk soon."
            "thanks"   -> "$empathyPrefixYou’re welcome$name!"
            "help"     -> "$empathyPrefixI’m here to help. What do you need?"
            else       -> empathyPrefix + genericFallback.random()
        }
    }
}