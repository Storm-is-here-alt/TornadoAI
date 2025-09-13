package com.storm.tornadoai

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.RandomAccessFile
import kotlin.math.max

data class SystemStats(
    val cpuPercent: Float,
    val memUsedMB: Long,
    val memAvailMB: Long,
    val memTotalMB: Long,
)

object SystemMonitor {

    fun observe(context: Context, intervalMs: Long = 1000L): Flow<SystemStats> = flow {
        var prevIdle: Long? = null
        var prevTotal: Long? = null

        while (true) {
            val (cpuPct, newIdle, newTotal) = withContext(Dispatchers.IO) {
                safeCpuPercent(prevIdle, prevTotal)
            }
            prevIdle = newIdle
            prevTotal = newTotal

            val (usedMB, availMB, totalMB) = safeMem(context)

            emit(SystemStats(cpuPct, usedMB, availMB, totalMB))
            delay(intervalMs)
        }
    }

    private fun safeCpuPercent(prevIdle: Long?, prevTotal: Long?): Triple<Float, Long?, Long?> {
        return try {
            readCpuPercent(prevIdle, prevTotal)
        } catch (_: Throwable) {
            Triple(0f, prevIdle, prevTotal)
        }
    }

    // Returns Triple: (cpu%, idleAll, total)
    private fun readCpuPercent(prevIdle: Long?, prevTotal: Long?): Triple<Float, Long, Long> {
        val raf = RandomAccessFile("/proc/stat", "r")
        val line = raf.readLine()
        raf.close()

        // "cpu  user nice system idle iowait irq softirq steal guest guest_nice"
        val toks = line.trim().split(Regex("\\s+"))
        val user = toks.getOrNull(1)?.toLongOrNull() ?: 0L
        val nice = toks.getOrNull(2)?.toLongOrNull() ?: 0L
        val system = toks.getOrNull(3)?.toLongOrNull() ?: 0L
        val idle = toks.getOrNull(4)?.toLongOrNull() ?: 0L
        val iowait = toks.getOrNull(5)?.toLongOrNull() ?: 0L
        val irq = toks.getOrNull(6)?.toLongOrNull() ?: 0L
        val softirq = toks.getOrNull(7)?.toLongOrNull() ?: 0L
        val steal = toks.getOrNull(8)?.toLongOrNull() ?: 0L

        val idleAll = idle + iowait
        val nonIdle = user + nice + system + irq + softirq + steal
        val total = idleAll + nonIdle

        return if (prevIdle == null || prevTotal == null) {
            Triple(0f, idleAll, total)
        } else {
            val totald = max(1L, total - prevTotal)
            val idled = max(0L, idleAll - prevIdle)
            val cpuPct = ((totald - idled) * 100f) / totald.toFloat()
            Triple(cpuPct, idleAll, total)
        }
    }

    private fun safeMem(context: Context): Triple<Long, Long, Long> {
        return try {
            readMem(context)
        } catch (_: Throwable) {
            Triple(0, 0, 0)
        }
    }

    private fun readMem(context: Context): Triple<Long, Long, Long> {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)

        val totalMB = (mi.totalMem / (1024 * 1024))
        val availMB = (mi.availMem / (1024 * 1024))
        val usedMB = (totalMB - availMB).coerceAtLeast(0)
        return Triple(usedMB, availMB, totalMB)
    }
}