package com.storm.tornadoai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {
    private val vm: ChatViewModel by viewModels()
    private lateinit var adapter: ChatAdapter
    private lateinit var list: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View? =
        inflater.inflate(R.layout.fragment_chat, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ChatAdapter()
        list = view.findViewById(R.id.chat_list)
        list.adapter = adapter

        val input = view.findViewById<TextInputEditText>(R.id.chat_input)
        view.findViewById<MaterialButton>(R.id.chat_send).setOnClickListener {
            val text = input.text?.toString()?.trim().orEmpty()
            if (text.isNotEmpty()) {
                vm.onUserMessage(text)
                input.setText("")
            }
        }
        view.findViewById<MaterialButton>(R.id.chat_tweet).setOnClickListener {
            vm.generateTweetsFromLastAnswer()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                vm.uiState.collect { state ->
                    adapter.submitList(state.messages) {
                        list.scrollToPosition(adapter.itemCount - 1)
                    }
                }
            }
        }
    }
}