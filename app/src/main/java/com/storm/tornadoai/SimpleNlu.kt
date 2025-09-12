package com.storm.tornadoai.conversation

object SimpleNlu {
    private val positiveWords = setOf("great","good","awesome","love","thanks","thank you","cool","nice")
    private val negativeWords = setOf("bad","hate","annoy","stupid","terrible","dumb","wtf","angry","mad","upset")
    private val insultWords   = setOf("idiot","dumb","stupid","useless","trash","suck","wtf")
    private val greetingWords = setOf("hi","hello","hey","yo","sup","good morning","good evening")
    private val farewellWords = setOf("bye","goodbye","see ya","later")
    private val helpWords     = setOf("help","how to","how do i","what do i do","guide","instructions")
    private val thanksWords   = setOf("thanks","thank you","thx","appreciate")

    fun analyze(u: Utterance): NluResult {
        val t = u.text.trim().lowercase()

        val intent = when {
            greetingWords.any { t.startsWith(it) || " $it " in " $t " } -> Intent.GREETING
            farewellWords.any { " $it " in " $t " || t.endsWith(it) }   -> Intent.FAREWELL
            thanksWords.any { " $it " in " $t " }                        -> Intent.THANKS
            insultWords.any { " $it " in " $t " }                        -> Intent.INSULT
            t.contains("?") && containsAny(t, listOf("app","apk","build","error","issue","bug"))
                                                                      -> Intent.QUESTION_APP
            t.contains("?")                                             -> Intent.QUESTION_GENERAL
            containsAny(t, listOf("add task","remind me","remember","todo","to-do"))
                                                                      -> Intent.TASK_ADD
            containsAny(t, listOf("status","progress","done","finished","what's left"))
                                                                      -> Intent.TASK_STATUS
            containsAny(t, listOf("help","how to","guide","explain"))   -> Intent.HELP
            containsAny(t, listOf("nice","great","awesome","love"))     -> Intent.COMPLIMENT
            else                                                        -> Intent.UNKNOWN
        }

        val posCount = positiveWords.count { " $it " in " $t " }
        val negCount = negativeWords.count { " $it " in " $t " }
        val sentiment = when {
            posCount > 0 && negCount > 0 -> Sentiment.MIXED
            posCount > 0                 -> Sentiment.POSITIVE
            negCount > 0                 -> Sentiment.NEGATIVE
            else                         -> Sentiment.NEUTRAL
        }

        val name = extractName(t)
        val entities = buildMap {
            if (name != null) put("name", name)
        }

        val confidence = when (intent) {
            Intent.UNKNOWN -> 0.3f
            Intent.INSULT  -> 0.9f
            else           -> 0.7f
        }

        return NluResult(intent, sentiment, entities, confidence)
    }

    private fun containsAny(t: String, keys: List<String>) =
        keys.any { k -> " $k " in " $t " || t.startsWith(k) || t.endsWith(k) }

    private fun extractName(t: String): String? {
        // crude: “i am X”, “i'm X”, “my name is X”
        val rx = Regex("""\b(i am|i'm|my name is)\s+([a-z][a-z0-9._-]{1,20})\b""")
        return rx.find(t)?.groupValues?.getOrNull(2)
    }
}