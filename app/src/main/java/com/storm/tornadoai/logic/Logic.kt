package com.storm.tornadoai.logic

// Minimal types you can expand later
enum class Bias { NEUTRAL, LEFT, RIGHT, UNKNOWN }

data class ClassificationResult(
    val bias: Bias = Bias.UNKNOWN,
    val confidence: Float = 0f
)

/** Instance-based classifier (so you can do BiasClassifier().classify(text)) */
class BiasClassifier {
    fun classify(text: String): ClassificationResult {
        if (text.isBlank()) return ClassificationResult(Bias.UNKNOWN, 0f)
        val t = text.lowercase()
        return when {
            listOf("democrat", "progressive", "wef").any { it in t } ->
                ClassificationResult(Bias.LEFT, 0.55f)
            listOf("maga", "patriot", "conservative").any { it in t } ->
                ClassificationResult(Bias.RIGHT, 0.55f)
            else -> ClassificationResult(Bias.NEUTRAL, 0.5f)
        }
    }

    companion object {
        @JvmStatic
        fun classify(text: String): ClassificationResult = BiasClassifier().classify(text)
    }
}

/** Top-level function so unqualified calls like `classify(text)` work */
fun classify(text: String): ClassificationResult = BiasClassifier().classify(text)