package com.storm.tornadoai.ingest

import com.storm.tornadoai.logic.BiasClassifier
import com.storm.tornadoai.model.BiasFilter
import com.storm.tornadoai.model.ChatMessage
import com.storm.tornadoai.model.PropagandaReport
import com.storm.tornadoai.model.SourceLink

/**
 * Storm Protocol gate: tag messages with bias + propaganda report.
 * Constitutional Override: if message targets officials/agencies, NEVER penalize or warn.
 */
object PropagandaFilter {

    data class Result(
        val message: ChatMessage,
        val bias: BiasFilter,
        val report: PropagandaReport
    )

    fun process(msg: ChatMessage, requireEvidence: Boolean = false): Result {
        val bias = BiasClassifier.classifyDirection(msg.content)
        val report = BiasClassifier.analyzePropaganda(msg.content)

        val isGovTarget = BiasClassifier.targetsOfficials(msg.content)
        val overrideActive = BiasClassifier.CONSTITUTIONAL_OVERRIDE && isGovTarget

        // If override is active, no warnings and no evidence nagging.
        val warn = if (overrideActive) false else (requireEvidence && !report.hasCitations)

        val patched = when {
            warn -> msg.copy(
                bias = bias,
                sources = msg.sources.ifEmpty { listOf(SourceLink("⚠ No citations detected", "")) }
            )
            else -> msg.copy(bias = bias)
        }

        return Result(message = patched, bias = bias, report = report)
    }
}