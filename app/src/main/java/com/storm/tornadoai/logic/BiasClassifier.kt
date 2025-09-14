package com.storm.tornadoai.logic

/** Minimal placeholder to keep the app compiling. */
object BiasClassifier {
    enum class Bias { NEUTRAL, POSITIVE, NEGATIVE }

    fun classify(text: String): Bias {
        val t = text.lowercase()
        return when {
            listOf("love", "great", "awesome", "thanks").any { it in t } -> Bias.POSITIVE
            listOf("hate", "stupid", "awful", "angry").any { it in t } -> Bias.NEGATIVE
            else -> Bias.NEUTRAL
        }
    }
}