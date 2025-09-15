package com.storm.tornadoai

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.storm.tornadoai.model.ChatMessage
import com.storm.tornadoai.model.Role

/**
 * RecyclerView adapter for chat messages.
 * Depends on model.ChatMessage ONLY. Do not redeclare data classes here.
 */
class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(old: ChatMessage, new: ChatMessage): Boolean =
                old.timestamp == new.timestamp && old.role == new.role && old.text == new.text

            override fun areContentsTheSame(old: ChatMessage, new: ChatMessage): Boolean =
                old == new
        }
    }

    class VH(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val prefix = when (item.role) {
            Role.USER -> "You: "
            Role.ASSISTANT -> "AI: "
            Role.SYSTEM -> "Sys: "
        }
        holder.textView.text = prefix + item.text
    }
}