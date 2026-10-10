package com.cuac_xd.zencore.profiler

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Real-time execution profiler tracking microsecond performance per effect and trigger.
 */
object ZenProfiler {
    @JvmStatic
    var enabled: Boolean = false
        set(value) {
            field = value
            if (!value) reset()
        }

    @PublishedApi internal val executionTimes = ConcurrentHashMap<String, AtomicLong>()
    @PublishedApi internal val callCounts = ConcurrentHashMap<String, AtomicLong>()

    @JvmStatic
    inline fun <T> measure(id: String, block: () -> T): T {
        if (!enabled) return block()
        val start = System.nanoTime()
        try {
            return block()
        } finally {
            val elapsedNanos = System.nanoTime() - start
            executionTimes.computeIfAbsent(id) { AtomicLong(0) }.addAndGet(elapsedNanos)
            callCounts.computeIfAbsent(id) { AtomicLong(0) }.incrementAndGet()
        }
    }

    @JvmStatic
    fun reset() {
        executionTimes.clear()
        callCounts.clear()
    }

    @JvmStatic
    fun getReport(): Map<String, String> {
        val report = mutableMapOf<String, String>()
        for ((k, totalNanos) in executionTimes) {
            val calls = callCounts[k]?.get() ?: 1L
            val totalMicros = totalNanos.get() / 1000.0
            val avgMicros = totalMicros / calls
            report[k] = "Total: ${String.format("%.2f", totalMicros)}µs | Calls: $calls | Avg: ${String.format("%.2f", avgMicros)}µs"
        }
        return report
    }
}
