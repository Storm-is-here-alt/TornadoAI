package com.storm.tornadoai.conversation

enum class Sentiment { POSITIVE, NEUTRAL, NEGATIVE, MIXED }
enum class Intent {
    GREETING, FAREWELL, THANKS, HELP, COMPLIMENT, INSULT,
    QUESTION_APP, QUESTION_GENERAL, TASK_ADD, TASK_STATUS,
    UNKNOWN
}

data class Utterance(
    val text: String,
    val lang: String = "en",          // reserved for future i18n
    val timestampMs: Long = System.currentTimeMillis()
)

data class NluResult(
    val intent: Intent,
    val sentiment: Sentiment,
    val entities: Map<String, String> = emptyMap(),
    val confidence: Float = 0.6f
)

data class DialogueState(
    var userName: String? = null,
    var lastIntent: Intent = Intent.UNKNOWN,
    var lastBotMessage: String? = null,
    var shortTermMemory: MutableMap<String, String> = mutableMapOf()
)