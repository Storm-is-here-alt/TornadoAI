package com.storm.tornadoai

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter :
    ListAdapter<ChatMessage, ChatAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ChatMessage>() {
            override fun areItemsTheSame(o: ChatMessage, n: ChatMessage) = o === n
            override fun areContentsTheSame(o: ChatMessage, n: ChatMessage) = o == n
        }
    }

    inner class VH(val bubble: TextView) : RecyclerView.ViewHolder(bubble)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = TextView(parent.context).apply {
            setTextColor(0xFFEDEDED.toInt())
            textSize = 16f
        }
        val lp = FrameLayout.LayoutParams(
            (parent.measuredWidth * 0.85f).toInt(),
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        val container = FrameLayout(parent.context)
        container.layoutParams = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        container.addView(tv, lp)
        return VH(tv)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = getItem(position)
        holder.bubble.text = m.content

        val lp = holder.bubble.layoutParams as FrameLayout.LayoutParams
        if (m.role == Role.USER) {
            holder.bubble.setBackgroundResource(R.drawable.bg_bubble_user)
            lp.gravity = Gravity.END
        } else {
            holder.bubble.setBackgroundResource(R.drawable.bg_bubble_bot)
            lp.gravity = Gravity.START
        }
        holder.bubble.layoutParams = lp
    }
}