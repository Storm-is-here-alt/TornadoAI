package com.storm.tornadoai.logic

/**
 * Simple wrapper logic that uses BiasClassifier.
 * Keeps the 'when' exhaustive and avoids missing symbol errors.
 */
object Logic {

    fun biasTagFor(text: String): String {
        return when (BiasClassifier.classify(text)) {
            Bias.POSITIVE -> "bias:positive"
            Bias.NEGATIVE -> "bias:negative"
            Bias.NEUTRAL  -> "bias:neutral"
        }
    }
}