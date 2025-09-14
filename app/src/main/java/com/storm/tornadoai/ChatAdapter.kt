package com.storm.tornadoai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.storm.tornadoai.model.BiasFilter
import com.storm.tornadoai.model.ChatMessage
import com.storm.tornadoai.model.Role

class ChatAdapter(
    private val data: MutableList<ChatMessage> = mutableListOf()
) : RecyclerView.Adapter<ChatAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val role: TextView = itemView.findViewById(R.id.chat_role)
        val content: TextView = itemView.findViewById(R.id.chat_content)
        val meta: TextView = itemView.findViewById(R.id.chat_meta)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val msg = data[position]

        holder.role.text = when (msg.role) {
            Role.User -> "You"
            Role.Bot -> "TornadoAI"
        }

        holder.content.text = msg.content

        val parts = mutableListOf<String>()
        if (msg.isTweetDraft) parts.add("Tweet Draft")
        if (msg.sources.isNotEmpty()) {
            parts.add("Sources: " + msg.sources.joinToString { it.title })
        }
        when (msg.bias) {
            BiasFilter.Left -> parts.add("Bias: Left")
            BiasFilter.Right -> parts.add("Bias: Right")
            BiasFilter.Establishment -> parts.add("Bias: Establishment")
            BiasFilter.AntiEstablishment -> parts.add("Bias: Anti-Establishment")
            BiasFilter.Unknown -> parts.add("Bias: Unknown")
            BiasFilter.None -> {}
        }
        val metaText = parts.joinToString(" • ")
        holder.meta.text = metaText
        holder.meta.visibility = if (metaText.isEmpty()) View.GONE else View.VISIBLE
    }

    fun submitList(newList: List<ChatMessage>) {
        data.clear()
        data.addAll(newList)
        notifyDataSetChanged()
    }

    fun append(msg: ChatMessage) {
        data.add(msg)
        notifyItemInserted(data.lastIndex)
    }
}