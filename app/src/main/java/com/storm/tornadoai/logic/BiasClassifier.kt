package com.storm.tornadoai.logic

/**
 * Minimal, dependency-free bias scorer so ChatRepository can compile.
 * Swap out later for something smarter.
 */
class BiasClassifier {

    data class BiasResult(
        val label: Label,
        val leftScore: Double,
        val rightScore: Double,
        val neutrality: Double
    )

    enum class Label { LEFT, RIGHT, NEUTRAL, UNKNOWN }

    fun classify(text: String?): BiasResult {
        if (text.isNullOrBlank()) {
            return BiasResult(Label.UNKNOWN, 0.0, 0.0, 1.0)
        }
        val t = text.lowercase()

        val leftHints = listOf(
            "universal healthcare","climate crisis","wealth tax",
            "social justice","gun control","labor union","equity"
        )
        val rightHints = listOf(
            "border security","2a","second amendment","tax cuts",
            "school choice","america first","pro life","lower taxes"
        )

        val l = leftHints.count { it in t }
        val r = rightHints.count { it in t }
        val total = (l + r).coerceAtLeast(1)
        val leftScore = l.toDouble() / total
        val rightScore = r.toDouble() / total
        val neutrality = if (l == 0 && r == 0) 1.0 else 0.0

        val label = when {
            l == 0 && r == 0 -> Label.NEUTRAL
            leftScore  > rightScore + 0.15 -> Label.LEFT
            rightScore > leftScore  + 0.15 -> Label.RIGHT
            else -> Label.NEUTRAL
        }
        return BiasResult(label, leftScore, rightScore, neutrality)
    }
}