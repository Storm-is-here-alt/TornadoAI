package com.storm.tornadoai

import androidx.lifecycle.ViewModel
import com.storm.tornadoai.ingest.PropagandaFilter
import com.storm.tornadoai.model.BiasFilter
import com.storm.tornadoai.model.ChatMessage
import com.storm.tornadoai.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentBias: BiasFilter = BiasFilter.None
)

class ChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    fun onUserMessage(text: String) {
        val userMsg = ChatMessage(Role.User, text)
        _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + userMsg)

        // --- Stub answer (replace with repository later)
        val rawBot = ChatMessage(Role.Bot, "Processing: \"$text\"")

        // Storm Protocol filter pass
        val filtered = PropagandaFilter.process(rawBot, requireEvidence = false)
        val annotated = rawBot.copy(
            bias = filtered.bias,
            sources = rawBot.sources + filtered.report.evidenceLinks.map { link ->
                com.storm.tornadoai.model.SourceLink("source", link)
            }
        )

        _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + annotated)
    }

    fun generateTweetsFromLastAnswer() {
        val last = _uiState.value.messages.lastOrNull { it.role == Role.Bot } ?: return
        val draft = last.copy(isTweetDraft = true)
        _uiState.value = _uiState.value.copy(messages = _uiState.value.messages + draft)
    }

    fun setBias(bias: BiasFilter) {
        _uiState.value = _uiState.value.copy(currentBias = bias)
    }
}