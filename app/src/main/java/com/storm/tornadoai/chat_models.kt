package com.storm.tornadoai

enum class Role { User, Bot }

data class SourceCard(
    val title: String,
    val url: String,
    val snippet: String,
    val colorIndex: Int,
    val bias: BiasFilter
) {
    companion object {
        // Neutral, professional tints
        val PALETTE = listOf(
            0xFFE8EAED.toInt(), // light gray
            0xFFDDEAF6.toInt(), // steel blue tint
            0xFFE7F0EA.toInt(), // teal tint
            0xFFEFE7F0.toInt(), // muted purple gray
            0xFFF0EAE2.toInt()  // warm gray
        )
    }
}

data class ChatMessage(
    val role: Role,
    val content: String,
    val sources: List<SourceCard> = emptyList(),
    val isTweetDraft: Boolean = false
) {
    companion object {
        fun user(text: String) = ChatMessage(Role.User, text)
        fun bot(text: String, sources: List<SourceCard> = emptyList(), isTweetDraft: Boolean = false) =
            ChatMessage(Role.Bot, text, sources, isTweetDraft)
    }
}