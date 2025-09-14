package com.storm.tornadoai

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UiState(
    val messages: List<ChatMessage> = emptyList(),
    val loading: Boolean = false
)

class ChatViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ChatRepository(app)

    private val _ui = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _ui

    fun onUserMessage(text: String) {
        val user = ChatMessage.user(text)
        _ui.value = _ui.value.copy(messages = _ui.value.messages + user, loading = true)

        viewModelScope.launch(Dispatchers.IO) {
            val result = repo.answer(text)
            val bot = ChatMessage.bot(result.answer, result.sources)
            val list = _ui.value.messages + bot
            _ui.value = UiState(messages = list, loading = false)
        }
    }

    fun generateTweetsFromLastAnswer() {
        val last = _ui.value.messages.lastOrNull { it.role == Role.Bot } ?: return
        val tweets = TweetGenerator.splitIntoTweets(last.content)
        val tweetCards = tweets.map { tw ->
            ChatMessage.bot("🧵 $tw", emptyList(), isTweetDraft = true)
        }
        _ui.value = _ui.value.copy(messages = _ui.value.messages + tweetCards)
    }
}