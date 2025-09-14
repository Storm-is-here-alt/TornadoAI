package com.storm.tornadoai.logic

object Logic {

    fun empathyPrefix(bias: BiasClassifier.Bias): String = when (bias) {
        BiasClassifier.Bias.POSITIVE -> "💡 Noted—and I’m glad to hear it."
        BiasClassifier.Bias.NEGATIVE -> "💡 I hear your frustration."
        BiasClassifier.Bias.NEUTRAL  -> "💡 Got it."
    }

    /** Builds a simple human-like response. */
    fun humanReply(userText: String): String {
        val bias = BiasClassifier.classify(userText)   // <-- use .classify(), not BiasClassifier(...)
        val prefix = empathyPrefix(bias)
        return "$prefix ${userText.trim()}"
    }
}