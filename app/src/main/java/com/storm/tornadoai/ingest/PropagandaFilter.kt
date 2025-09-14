package com.storm.tornadoai.ingest

import com.storm.tornadoai.logic.BiasClassifier
import com.storm.tornadoai.model.BiasFilter
import com.storm.tornadoai.model.ChatMessage
import com.storm.tornadoai.model.PropagandaReport
import com.storm.tornadoai.model.SourceLink

/**
 * Storm Protocol gate: tag messages with bias + propaganda report,
 * optionally strip naked claims with no citations.
 */
object PropagandaFilter {

    data class Result(
        val message: ChatMessage,
        val bias: BiasFilter,
        val report: PropagandaReport
    )

    /**
     * Analyze a message and return a tagged copy.
     * If requireEvidence is true, append a warning when no citations found.
     */
    fun process(msg: ChatMessage, requireEvidence: Boolean = false): Result {
        val bias = BiasClassifier.classifyDirection(msg.content)
        val report = BiasClassifier.analyzePropaganda(msg.content)

        val warn = requireEvidence && !report.hasCitations
        val patched = if (warn) {
            msg.copy(
                bias = bias,
                sources = msg.sources.ifEmpty { listOf(SourceLink("⚠ No citations detected", "")) }
            )
        } else {
            msg.copy(bias = bias)
        }

        return Result(message = patched, bias = bias, report = report)
    }
}