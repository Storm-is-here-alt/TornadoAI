package com.storm.tornadoai.model

/**
 * Single source of truth for chat domain models.
 * Do not redeclare ChatMessage anywhere else.
 */
enum class Role { USER, ASSISTANT, SYSTEM }

data class ChatMessage(
    val role: Role,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isUser: Boolean get() = role == Role.USER
}