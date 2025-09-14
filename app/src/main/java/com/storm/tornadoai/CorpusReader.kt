package com.storm.tornadoai

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.util.Locale

class CorpusReader(private val context: Context) {
    fun search(query: String): List<String> {
        val dbFile = context.getFileStreamPath("corpus.db")
        if (dbFile.exists()) {
            try {
                val db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
                val hits = mutableListOf<String>()
                val tables = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
                while (tables.moveToNext()) {
                    val t = tables.getString(0)
                    val c = db.rawQuery("PRAGMA table_info($t)", null)
                    val textCols = mutableListOf<String>()
                    while (c.moveToNext()) {
                        val name = c.getString(1)
                        val type = (c.getString(2) ?: "").lowercase(Locale.US)
                        if (type.contains("text") || type.contains("char")) textCols += name
                    }
                    c.close()
                    if (textCols.isNotEmpty()) {
                        val where = textCols.joinToString(" OR ") { "$it LIKE ?" }
                        val args = arrayOf("%$query%")
                        val q = db.rawQuery("SELECT ${textCols.first()} FROM $t WHERE $where LIMIT 5", args)
                        while (q.moveToNext()) hits += q.getString(0)
                        q.close()
                        if (hits.size >= 5) { tables.close(); db.close(); return hits.take(5) }
                    }
                }
                tables.close(); db.close()
                return hits.take(5)
            } catch (_: Throwable) { /* ignore and fall through */ }
        }
        return runCatching {
            context.assets.open("corpus_stub.txt").bufferedReader().readLines()
                .filter { it.contains(query, ignoreCase = true) }
                .take(5)
        }.getOrElse { emptyList() }
    }
}