package com.storm.tornadoai

import com.storm.tornadoai.logic.Logic

class ChatRepository {
    fun replyTo(text: String): String = Logic.humanReply(text)
}