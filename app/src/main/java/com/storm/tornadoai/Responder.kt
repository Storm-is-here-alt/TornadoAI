package com.storm.tornadoai.conversation

object Responder {

    fun reply(input: Utterance, state: DialogueState): String {
        val nlu = SimpleNlu.analyze(input)
        // update state from entities
        nlu.entities["name"]?.let { state.userName = it }

        val name = state.userName?.let { " $it" } ?: ""
        val empathyPrefix = when (nlu.sentiment) {
            Sentiment.NEGATIVE -> "I hear your frustration$name. "
            Sentiment.MIXED    -> "Got it$name—mixed feelings are normal. "
            Sentiment.POSITIVE -> ""
            Sentiment.NEUTRAL  -> ""
        }

        val message = when (nlu.intent) {
            Intent.GREETING -> listOf(
                "Hey$name! What are we building today?",
                "Hi$name! How can I help right now?"
            ).random()

            Intent.FAREWELL -> "See you soon$name. I’ll be here when you’re back."

            Intent.THANKS -> listOf(
                "Anytime$name!",
                "You’re welcome$name."
            ).random()

            Intent.COMPLIMENT -> "Appreciate it$name. Let’s keep the momentum."

            Intent.INSULT -> safeRedirect(name)

            Intent.HELP -> "Happy to help$name. Tell me what you’re trying to do, and I’ll break it into steps."

            Intent.QUESTION_APP -> """$empathyPrefixLet’s narrow it down$name:
1) What command or button did you last run?
2) What was the exact error or behavior?
3) What do you want to happen instead?
Give me 1–3 and I’ll fix it with you."""

            Intent.QUESTION_GENERAL -> "$empathyPrefixHere’s a quick take$name: I can explain, give examples, or show steps—what format do you prefer?"

            Intent.TASK_ADD -> "$empathyPrefixOkay$name. What’s the task, and when (date/time) should it be due?"

            Intent.TASK_STATUS -> "$empathyPrefixHere’s what I recall$name: ${state.shortTermMemory["last_task"] ?: "no tasks saved yet."}"

            Intent.UNKNOWN -> "$empathyPrefixNot 100% sure what you meant$name—can you say it another way?"
        }

        state.lastIntent = nlu.intent
        state.lastBotMessage = message
        return message
    }

    private fun safeRedirect(name: String): String =
        "I’m here to help$name. Let’s keep things constructive—what’s the problem you want solved?"
}