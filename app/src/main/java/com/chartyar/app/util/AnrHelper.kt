package com.chartyar.app.util

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

/**
 * مدیریت ANR (Application Not Responding)
 * با timeout برای کارهای طولانی
 */
object AnrHelper {

    /**
     * اجرای عملیات با timeout
     */
    suspend fun <T> withTimeoutOrNull(
        timeoutMs: Long = 30_000L,
        operation: suspend () -> T?
    ): T? {
        return try {
            withTimeout(timeoutMs) {
                operation()
            }
        } catch (e: TimeoutCancellationException) {
            null
        } catch (e: Exception) {
            null
        }
    }
}