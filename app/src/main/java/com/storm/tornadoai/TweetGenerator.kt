package com.storm.tornadoai

object TweetGenerator {
    fun splitIntoTweets(text: String, maxLen: Int = 270): List<String> {
        val words = text.replace("\n", " ").split(" ")
        val out = mutableListOf<String>()
        var cur = StringBuilder()
        for (w in words) {
            if (w.isBlank()) continue
            if (cur.length + 1 + w.length > maxLen) {
                if (cur.isNotBlank()) out += cur.toString().trim()
                cur = StringBuilder(w)
            } else {
                if (cur.isEmpty()) cur.append(w) else cur.append(' ').append(w)
            }
        }
        if (cur.isNotBlank()) out += cur.toString().trim()
        return out.take(20)
    }
}