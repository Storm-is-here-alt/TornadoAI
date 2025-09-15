package com.storm.tornadoai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Simple chat models kept here to avoid missing-type issues
enum class Role { USER, ASSISTANT }

data class ChatMessage(
    val role: Role,
    val content: String
)

data class UiState(
    val messages: List<ChatMessage> = emptyList()
)

class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun onUserMessage(text: String) {
        if (text.isBlank()) return

        // Add the user message
        val withUser = _uiState.value.messages + ChatMessage(Role.USER, text)
        _uiState.value = _uiState.value.copy(messages = withUser)

        // Produce a basic reply (no external processors)
        viewModelScope.launch {
            val reply = respond(text)
            val withBot = _uiState.value.messages + ChatMessage(Role.ASSISTANT, reply)
            _uiState.value = _uiState.value.copy(messages = withBot)
        }
    }

    /** Trivial bot response for now so the app compiles/runs */
    private fun respond(input: String): String {
        return "You said: $input"
    }

    /** Stub to keep callers happy; wire up later if you like */
    fun generateTweetsFromLastAnswer() {
        // no-op for now
    }
}