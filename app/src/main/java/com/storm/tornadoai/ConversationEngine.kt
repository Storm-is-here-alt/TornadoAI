package com.storm.tornadoai

class ConversationEngine {
    fun handle(text: String): Responder.Intent {
        return when {
            text.contains("hello", ignoreCase = true) -> Responder.Intent.GREETING
            text.contains("help", ignoreCase = true) -> Responder.Intent.HELP
            else -> Responder.Intent.OTHER
        }
    }
}