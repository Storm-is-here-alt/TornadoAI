package com.storm.tornadoai.model

data class SourceLink(
    val title: String,
    val url: String
)

enum class Role { User, Bot }

enum class BiasFilter { None, Left, Right, Establishment, AntiEstablishment, Unknown }

data class ChatMessage(
    val role: Role,
    val content: String,
    val isTweetDraft: Boolean = false,
    val sources: List<SourceLink> = emptyList(),
    val bias: BiasFilter = BiasFilter.None
)