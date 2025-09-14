package com.storm.tornadoai

import android.content.Context
import com.storm.tornadoai.model.ChatMessage
import com.storm.tornadoai.model.Role

class ChatRepository(context: Context) {
    // add your services here when ready

    suspend fun answer(query: String): List<ChatMessage> {
        // Stub: return echo; no duplicate Role/ChatMessage here
        return listOf(
            ChatMessage(Role.User, query),
            ChatMessage(Role.Bot, "Answering \"$query\" (stub).")
        )
    }
}