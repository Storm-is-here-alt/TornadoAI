package com.storm.tornadoai

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Plain ViewModel (not AndroidViewModel) so we don't need an Application.
 * If you truly need Application later, we can switch to AndroidViewModel and a factory.
 */
class ChatViewModel : ViewModel() {

    private val repo = ChatRepository() // ← no arguments

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    fun send(text: String) {
        if (text.isBlank()) return
        // add user's message
        val afterUser = _messages.value + ChatMessage(Role.USER, text)
        // get bot reply
        val botReply = repo.replyTo(text)
        // publish both
        _messages.value = afterUser + botReply
    }
}