package com.chartyar.app.data

import kotlinx.coroutines.delay

/**
 * Helper برای تلاش مجدد خودکار
 * اگه درخواست شبکه شکست خورد، خودکار دوباره تلاش می‌کنه
 */
object RetryHelper {

    /**
     * حداکثر تعداد تلاش
     */
    private const val MAX_ATTEMPTS = 3

    /**
     * زمان انتظار بین تلاش‌ها (میلی‌ثانیه)
     * هر بار دو برابر می‌شه (Exponential Backoff)
     */
    private const val INITIAL_DELAY_MS = 500L

    /**
     * اجرای یه عملیات با تلاش مجدد
     * @param maxAttempts حداکثر تعداد تلاش
     * @param operation عملیاتی که باید اجرا بشه
     * @return نتیجه عملیات یا null اگه همه تلاش‌ها شکست خوردن
     */
    suspend fun <T> withRetry(
        maxAttempts: Int = MAX_ATTEMPTS,
        operation: suspend () -> T?
    ): T? {
        var lastException: Exception? = null
        var delayMs = INITIAL_DELAY_MS

        for (attempt in 1..maxAttempts) {
            try {
                val result = operation()
                if (result != null) return result
            } catch (e: Exception) {
                lastException = e
            }

            // اگه آخرین تلاش نیست، صبر کن
            if (attempt < maxAttempts) {
                delay(delayMs)
                delayMs *= 2  // Exponential Backoff
            }
        }

        return null
    }

    /**
     * چک کردن اینکه خطا قابل تلاش مجدد هست یا نه
     * خطاهای شبکه‌ای و Timeout قابل تلاش هستن
     * خطاهای 401, 403, 404 قابل تلاش نیستن
     */
    fun isRetryable(errorCode: Int): Boolean {
        return when (errorCode) {
            408, 429, 500, 502, 503, 504 -> true
            else -> false
        }
    }
}