package com.storm.tornadoai

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.storm.tornadoai.databinding.ActivityChatBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private val vm: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        adapter = ChatAdapter { tweetText ->
            copyToClipboard(tweetText)
            Snackbar.make(binding.root, "Tweet copied", Snackbar.LENGTH_SHORT).show()
        }
        binding.recycler.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        binding.recycler.adapter = adapter
        binding.recycler.itemAnimator = null

        binding.sendBtn.setOnClickListener { send() }
        binding.input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) { send(); true } else false
        }
        binding.tweetsBtn.setOnClickListener { vm.generateTweetsFromLastAnswer() }

        lifecycleScope.launch {
            vm.uiState.collectLatest { state ->
                adapter.submitList(state.messages)
                binding.recycler.scrollToPosition(state.messages.lastIndex.coerceAtLeast(0))
                binding.progress.visibility = if (state.loading) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
    }

    private fun send() {
        val text = binding.input.text?.toString()?.trim().orEmpty()
        if (text.isNotEmpty()) {
            binding.input.setText("")
            vm.onUserMessage(text)
        }
    }

    private fun copyToClipboard(text: String) {
        val cm = getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.setPrimaryClip(android.content.ClipData.newPlainText("tweet", text))
    }
}