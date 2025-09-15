package com.storm.tornadoai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatUiState(val messages: List<ChatMessage> = emptyList())
enum class Role { USER, ASSISTANT }
data class ChatMessage(val role: Role, val content: String)

class ChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    fun onUserMessage(text: String) {
        val withUser = _uiState.value.messages + ChatMessage(Role.USER, text)
        _uiState.value = ChatUiState(withUser)

        // Fake assistant reply
        viewModelScope.launch {
            delay(350)
            val reply = ChatMessage(Role.ASSISTANT, "You said: $text")
            _uiState.value = ChatUiState(_uiState.value.messages + reply)
        }
    }

    fun generateTweetsFromLastAnswer() {
        // no-op for now; you can hook your tweet generator here later
    }
}