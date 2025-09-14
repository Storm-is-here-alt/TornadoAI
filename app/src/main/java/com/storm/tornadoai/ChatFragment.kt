package com.storm.tornadoai

import android.os.Bundle
import android.text.util.Linkify
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.storm.tornadoai.databinding.FragmentChatBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val vm: ChatViewModel by viewModels()
    private val shared: SharedPrefsViewModel by activityViewModels()
    private lateinit var adapter: ChatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        adapter = ChatAdapter { tweet ->
            copyToClipboard(tweet)
            Snackbar.make(binding.root, "Tweet copied", Snackbar.LENGTH_SHORT).show()
        }
        binding.recycler.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        binding.recycler.adapter = adapter
        binding.recycler.itemAnimator = null

        binding.sendBtn.setOnClickListener { send() }
        binding.tweetsBtn.setOnClickListener { vm.generateTweetsFromLastAnswer() }

        viewLifecycleOwner.lifecycleScope.launch {
            vm.uiState.collectLatest { state ->
                adapter.submitList(state.messages)
                binding.recycler.scrollToPosition(state.messages.lastIndex.coerceAtLeast(0))
                binding.progress.isVisible = state.loading
            }
        }

        // React to bias filter changes
        viewLifecycleOwner.lifecycleScope.launch {
            shared.bias.collectLatest { bias ->
                vm.setBias(bias)
            }
        }
    }

    private fun send() {
        val text = binding.input.text?.toString()?.trim().orEmpty()
        if (text.isBlank()) return
        binding.input.setText("")

        if (text.startsWith("/tweets")) {
            vm.generateTweetsFromLastAnswer()
        } else {
            vm.onUserMessage(text)
        }
    }

    private fun copyToClipboard(text: String) {
        val cm = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.setPrimaryClip(android.content.ClipData.newPlainText("tweet", text))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.bias_all -> shared.setBias(BiasFilter.ALL)
            R.id.bias_mainstream -> shared.setBias(BiasFilter.MAINSTREAM)
            R.id.bias_independent -> shared.setBias(BiasFilter.INDEPENDENT)
            R.id.bias_state -> shared.setBias(BiasFilter.STATE)
            R.id.bias_left -> shared.setBias(BiasFilter.LEFT)
            R.id.bias_right -> shared.setBias(BiasFilter.RIGHT)
        }
        return true
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}