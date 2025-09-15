package com.storm.tornadoai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.storm.tornadoai.model.ChatMessage
import com.storm.tornadoai.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel that owns the chat timeline.
 * Uses the centralized ChatMessage model (model/Models.kt).
 */
class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    fun sendUser(text: String) {
        append(ChatMessage(role = Role.USER, text = text))
        // placeholder echo; replace with real pipeline/coroutine call
        respond("Received: $text")
    }

    private fun respond(text: String) {
        viewModelScope.launch {
            append(ChatMessage(role = Role.ASSISTANT, text = text))
        }
    }

    private fun append(msg: ChatMessage) {
        _messages.value = _messages.value + msg
    }

    fun clear() {
        _messages.value = emptyList()
    }
}