package com.storm.tornadoai.conversation

class ConversationEngine(
    private val state: DialogueState = DialogueState()
) {
    fun handle(text: String): String {
        val u = Utterance(text)
        return Responder.reply(u, state)
    }

    fun setUserName(name: String) { state.userName = name }
    fun getState(): DialogueState = state
}