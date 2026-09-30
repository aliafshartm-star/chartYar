package com.chartyar.app.data

import kotlinx.coroutines.delay
import kotlin.random.Random

object RetryHelper {

    private const val DEFAULT_MAX_ATTEMPTS = 3
    private const val INITIAL_DELAY_MS = 500L
    private const val MAX_DELAY_MS = 10_000L
    private const val BACKOFF_MULTIPLIER = 2.0
    private const val JITTER_FACTOR = 0.3

    /**
     * اجرای عملیات با Retry و Exponential Backoff
     */
    suspend fun <T> withRetry(
        maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
        operation: suspend () -> T?
    ): T? {
        var delayMs = INITIAL_DELAY_MS

        for (attempt in 1..maxAttempts) {
            try {
                val result = operation()
                if (result != null) return result
            } catch (e: Exception) {
                // silent
            }

            if (attempt < maxAttempts) {
                // Jitter برای جلوگیری از thundering herd
                val jitter = (delayMs * JITTER_FACTOR * (Random.nextDouble() * 2 - 1)).toLong()
                val actualDelay = (delayMs + jitter).coerceIn(100L, MAX_DELAY_MS)
                delay(actualDelay)
                delayMs = (delayMs * BACKOFF_MULTIPLIER).toLong().coerceAtMost(MAX_DELAY_MS)
            }
        }

        return null
    }

    /**
     * چک آیا خطای HTTP قابل Retry هست
     */
    fun isRetryable(errorCode: Int): Boolean {
        return when (errorCode) {
            408, 429, 500, 502, 503, 504 -> true
            else -> false
        }
    }

    /**
     * محاسبه زمان انتظار برای Retry
     */
    fun calculateDelay(attempt: Int): Long {
        var delay = INITIAL_DELAY_MS
        repeat(attempt) {
            delay = (delay * BACKOFF_MULTIPLIER).toLong()
        }
        return delay.coerceAtMost(MAX_DELAY_MS)
    }
}