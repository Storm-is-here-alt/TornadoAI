package com.storm.tornadoai

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class BiasFilter { ALL, MAINSTREAM, INDEPENDENT, STATE, LEFT, RIGHT }

class SharedPrefsViewModel(app: Application) : AndroidViewModel(app) {
    private val prefs = app.getSharedPreferences("tornadoai", Context.MODE_PRIVATE)
    private val _bias = MutableStateFlow(load())
    val bias: StateFlow<BiasFilter> = _bias

    fun setBias(b: BiasFilter) {
        _bias.value = b
        prefs.edit().putString("bias", b.name).apply()
    }

    private fun load(): BiasFilter {
        val s = prefs.getString("bias", BiasFilter.ALL.name) ?: BiasFilter.ALL.name
        return runCatching { BiasFilter.valueOf(s) }.getOrElse { BiasFilter.ALL }
    }
}