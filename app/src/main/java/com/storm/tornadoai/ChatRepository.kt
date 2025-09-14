package com.storm.tornadoai

import android.content.Context

// Minimal data model used by the repo & VM
enum class Role { USER, ASSISTANT, SYSTEM }
data class ChatMessage(val role: Role, val content: String)

/**
 * Keep constructor optional. If callers pass a Context later, it still works.
 * Current ViewModel will use the no-arg form.
 */
class ChatRepository(private val context: Context? = null) {

    /**
     * Very simple placeholder “AI” reply so the app compiles and runs.
     * Swap this with your real pipeline when ready.
     */
    fun replyTo(userText: String): ChatMessage {
        val response = "You said: $userText"
        return ChatMessage(Role.ASSISTANT, response)
    }
}