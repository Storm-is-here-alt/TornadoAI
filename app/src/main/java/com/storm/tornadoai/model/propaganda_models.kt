package com.storm.tornadoai.model

data class TechniqueHit(
    val name: String,
    val count: Int,
    val examples: List<String> = emptyList()
)

data class PropagandaReport(
    val score: Int,                    // 0–100 rough intensity
    val techniques: List<TechniqueHit>,
    val isOpinion: Boolean,            // opinion-style language detected
    val evidenceLinks: List<String>,   // URLs detected
    val hasCitations: Boolean,         // true if looks like citations/links
    val notes: List<String> = emptyList()
)