package package com.storm.tornadoai

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

    private var currentBias: BiasFilter = BiasFilter.ALL

    fun setBias(b: BiasFilter) {
        currentBias = b
        val last = _ui.value.messages.lastOrNull { it.role == Role.Bot } ?: return
        val filtered = last.copy(sources = last.sources.filter { allowBias(it.bias) })
        val updated = _ui.value.messages.toMutableList()
        val idx = updated.lastIndexOf(last)
        if (idx >= 0) {
            updated[idx] = filtered
            _ui.value = _ui.value.copy(messages = updated)
        }
    }

    private fun allowBias(b: BiasFilter): Boolean {
        if (currentBias == BiasFilter.ALL) return true
        return currentBias == b
    }

    fun onUserMessage(text: String) {
        val user = ChatMessage.user(text)
        _ui.value = _ui.value.copy(messages = _ui.value.messages + user, loading = true)

        viewModelScope.launch(Dispatchers.IO) {
            val result = repo.answer(text)
            val filteredSources = result.sources.filter { allowBias(it.bias) }
            val bot = ChatMessage.bot(result.answer, filteredSources)
            val list = _ui.value.messages + bot
            _ui.value = UiState(messages = list, loading = false)
        }
    }

    fun generateTweetsFromLastAnswer() {
        val last = _ui.value.messages.lastOrNull { it.role == Role.Bot } ?: return
        val tweets: List<String> = TweetGenerator.splitIntoTweets(last.content)
        val tweetCards: List<ChatMessage> = tweets.map { tw: String ->
            ChatMessage.bot("🧵 $tw", emptyList(), isTweetDraft = true)
        }
        _ui.value = _ui.value.copy(messages = _ui.value.messages.plus(tweetCards))
    }
}

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

    private var currentBias: BiasFilter = BiasFilter.ALL

    fun setBias(b: BiasFilter) {
        currentBias = b
        // Re-render last bot message with filter applied
        val last = _ui.value.messages.lastOrNull { it.role == Role.Bot } ?: return
        val filtered = last.copy(sources = last.sources.filter { allowBias(it.bias) })
        val updated = _ui.value.messages.toMutableList()
        updated[updated.lastIndexOf(last)] = filtered
        _ui.value = _ui.value.copy(messages = updated)
    }

    private fun allowBias(b: BiasFilter): Boolean {
        if (currentBias == BiasFilter.ALL) return true
        return currentBias == b
    }

    fun onUserMessage(text: String) {
        val user = ChatMessage.user(text)
        _ui.value = _ui.value.copy(messages = _ui.value.messages + user, loading = true)

        viewModelScope.launch(Dispatchers.IO) {
            val result = repo.answer(text)
            val filteredSources = result.sources.filter { allowBias(it.bias) }
            val bot = ChatMessage.bot(result.answer, filteredSources)
            val list = _ui.value.messages + bot
            _ui.value = UiState(messages = list, loading = false)
        }
    }

    fun generateTweetsFromLastAnswer() {
        val last = _ui.value.messages.lastOrNull { it.role == Role.Bot } ?: return
        val tweets = TweetGenerator.splitIntoTweets(last.content)
        val tweetCards = tweets.map { tw -> ChatMessage.bot("🧵 $tw", isTweetDraft = true) }
        _ui.value = _ui.value.copy(messages = _ui.value.messages + tweetCards)
    }
}