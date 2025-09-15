package com.storm.tornadoai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Simple adapter that renders user messages with a green bubble and
 * assistant/bot messages with a dark bubble.
 *
 * It expects the data classes declared in your ChatViewModel file:
 *   data class ChatMessage(val role: Role, val content: String)
 *   enum class Role { USER, ASSISTANT }
 */
class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.VH>(Diff) {

    companion object {
        private const val TYPE_USER = 0
        private const val TYPE_BOT = 1

        private object Diff : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(old: ChatMessage, new: ChatMessage): Boolean {
                // No stable id in the model; compare by reference & content
                return old === new
            }
            override fun areContentsTheSame(old: ChatMessage, new: ChatMessage): Boolean {
                return old.role == new.role && old.content == new.content
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val msg = getItem(position)
        return if (msg.role == Role.USER) TYPE_USER else TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val layoutId = if (viewType == TYPE_USER) {
            R.layout.item_message_user
        } else {
            R.layout.item_message_bot
        }
        val view = inflater.inflate(layoutId, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position))
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text: TextView = itemView.findViewById(R.id.msg_text)
        fun bind(msg: ChatMessage) {
            text.text = msg.content
        }
    }
}