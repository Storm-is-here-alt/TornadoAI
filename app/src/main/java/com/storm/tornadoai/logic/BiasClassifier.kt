package com.storm.tornadoai.logic

object BiasClassifier {
    /**
     * Extremely simple placeholder classifier.
     * Replace with your real logic later.
     */
    fun classify(text: String): Bias {
        val t = text.lowercase()
        return when {
            t.contains("great") || t.contains("love") || t.contains("awesome") -> Bias.POSITIVE
            t.contains("bad") || t.contains("hate") || t.contains("terrible") -> Bias.NEGATIVE
            else -> Bias.NEUTRAL
        }
    }
}