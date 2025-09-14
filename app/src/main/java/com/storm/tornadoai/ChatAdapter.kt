package com.storm.tornadoai

import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.util.LinkifyCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.storm.tornadoai.databinding.ItemBotBinding
import com.storm.tornadoai.databinding.ItemSourceCardBinding
import com.storm.tornadoai.databinding.ItemUserBinding

class ChatAdapter(
    private val onCopyTweet: (String) -> Unit
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(DIFF) {

    companion object {
        private const val TYPE_USER = 1
        private const val TYPE_BOT = 2
        val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(o: ChatMessage, n: ChatMessage) = o === n
            override fun areContentsTheSame(o: ChatMessage, n: ChatMessage) = o == n
        }
    }

    override fun getItemViewType(position: Int) =
        if (getItem(position).role == Role.User) TYPE_USER else TYPE_BOT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_USER) {
            UserVH(ItemUserBinding.inflate(inf, parent, false))
        } else {
            BotVH(ItemBotBinding.inflate(inf, parent, false), onCopyTweet)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        if (holder is UserVH) holder.bind(msg)
        if (holder is BotVH) holder.bind(msg)
    }

    class UserVH(private val vb: ItemUserBinding) : RecyclerView.ViewHolder(vb.root) {
        fun bind(m: ChatMessage) {
            vb.text.text = m.content
            LinkifyCompat.addLinks(vb.text, Linkify.WEB_URLS)
            vb.text.movementMethod = LinkMovementMethod.getInstance()
        }
    }

    class BotVH(
        private val vb: ItemBotBinding,
        private val onCopyTweet: (String) -> Unit
    ) : RecyclerView.ViewHolder(vb.root) {

        fun bind(m: ChatMessage) {
            vb.text.text = m.content
            LinkifyCompat.addLinks(vb.text, Linkify.WEB_URLS)
            vb.text.movementMethod = LinkMovementMethod.getInstance()

            if (m.isTweetDraft) {
                vb.copyButton.visibility = View.VISIBLE
                vb.copyButton.setOnClickListener { onCopyTweet(m.content) }
            } else {
                vb.copyButton.visibility = View.GONE
            }

            vb.sourcesContainer.removeAllViews()
            if (m.sources.isEmpty()) {
                vb.sourcesContainer.visibility = View.GONE
            } else {
                vb.sourcesContainer.visibility = View.VISIBLE
                m.sources.forEach { s ->
                    val card = ItemSourceCardBinding.inflate(
                        LayoutInflater.from(vb.root.context), vb.sourcesContainer, false
                    )
                    card.title.text = s.title
                    card.snippet.text = s.snippet
                    card.url.text = s.url
                    LinkifyCompat.addLinks(card.url, Linkify.WEB_URLS)
                    card.url.movementMethod = LinkMovementMethod.getInstance()
                    val color = SourceCard.PALETTE[s.colorIndex % SourceCard.PALETTE.size]
                    card.root.setCardBackgroundColor(color)
                    vb.sourcesContainer.addView(card.root)
                }
            }
        }
    }
}