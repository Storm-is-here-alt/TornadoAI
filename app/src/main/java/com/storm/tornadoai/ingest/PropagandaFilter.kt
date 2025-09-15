package com.storm.tornadoai.ingest

/**
 * Minimal, compile-safe placeholders for propaganda analysis.
 * You can improve the heuristics later — these are simple keyword rules.
 */

data class PropagandaReport(
    val direction: Direction,
    val targetsOfficials: Boolean,
    val flags: Set<String>
)

enum class Direction { PRO, ANTI, NEUTRAL }

/** Marker string that other parts of the app may look for. */
const val CONSTITUTIONAL_OVERRIDE = "CONSTITUTIONAL_OVERRIDE"

object PropagandaFilter {

    /**
     * Main entry point used by the app.
     * Call this to analyze a piece of text.
     */
    fun analyze(text: String): PropagandaReport {
        val dir = classifyDirection(text)
        val officials = targetsOfficials(text)
        val flags = mutableSetOf<String>()

        // Extremely rough heuristic for “override constitutional rights”
        val t = text.lowercase()
        if (officials &&
            (t.contains("ban") || t.contains("suspend") || t.contains("strip rights") || t.contains("override constitution"))
        ) {
            flags.add(CONSTITUTIONAL_OVERRIDE)
        }

        return PropagandaReport(direction = dir, targetsOfficials = officials, flags = flags)
    }
}

/** Simple keyword-based direction classifier. */
fun classifyDirection(text: String): Direction {
    val t = text.lowercase()
    val proHits = listOf("support", "back", "endorse", "praise", "good", "great", "love")
        .count { it in t }
    val antiHits = listOf("oppose", "against", "criticize", "attack", "bad", "terrible", "hate")
        .count { it in t }

    return when {
        proHits > antiHits -> Direction.PRO
        antiHits > proHits -> Direction.ANTI
        else -> Direction.NEUTRAL
    }
}

/**
 * Very loose matcher for whether the text targets public officials/institutions.
 * Tweak/expand as you like.
 */
fun targetsOfficials(text: String): Boolean {
    val t = text.lowercase()
    val keywords = listOf(
        "president", "governor", "mayor", "senator", "congress", "representative",
        "judge", "supreme court", "police", "sheriff", "attorney general",
        "minister", "parliament", "prime minister", "official", "agency", "commission"
    )
    return keywords.any { it in t }
}

/** Compatibility alias in case other code calls this older name. */
fun analyzePropaganda(text: String): PropagandaReport = PropagandaFilter.analyze(text)