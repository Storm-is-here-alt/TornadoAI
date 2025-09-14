package com.storm.tornadoai

enum class Role { User, Bot }

data class SourceCard(
    val title: String,
    val url: String,
    val snippet: String,
    val colorIndex: Int
) {
    companion object {
        val PALETTE = listOf(
            0xFFE3F2FD.toInt(), // blue tint
            0xFFE8F5E9.toInt(), // green tint
            0xFFFFF3E0.toInt(), // orange tint
            0xFFF3E5F5.toInt(), // purple tint
            0xFFFFEBEE.toInt()  // red tint
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