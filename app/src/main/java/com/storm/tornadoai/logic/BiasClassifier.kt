package com.storm.tornadoai.logic

/**
 * Minimal no-deps bias classifier so the app compiles.
 * Expand later with real logic.
 */
class BiasClassifier {

    enum class Bias { NEUTRAL, LEFT, RIGHT, UNKNOWN }

    data class Result(
        val bias: Bias = Bias.UNKNOWN,
        val confidence: Float = 0f
    )

    /** Instance method, in case code does `BiasClassifier().classify(text)` */
    fun classify(text: String): Result {
        if (text.isBlank()) return Result(Bias.UNKNOWN, 0f)
        // trivial heuristic placeholder
        val t = text.lowercase()
        return when {
            listOf("democrat", "progressive", "wef").any { it in t } -> Result(Bias.LEFT, 0.55f)
            listOf("maga", "patriot", "conservative").any { it in t } -> Result(Bias.RIGHT, 0.55f)
            else -> Result(Bias.NEUTRAL, 0.5f)
        }
    }

    companion object {
        /** Static-style call, in case code does `BiasClassifier.classify(text)` */
        @JvmStatic
        fun classify(text: String): Result = BiasClassifier().classify(text)
    }
}