package com.chartyar.app.data

/**
 * Rate Limiter ساده
 * جلوگیری از درخواست‌های پشت‌سرهم به Binance
 */
object RateLimiter {

    /**
     * حداقل فاصله بین درخواست‌ها (۱ ثانیه)
     */
    private const val MIN_INTERVAL_MS = 1000L

    /**
     * حداکثر تعداد درخواست در دقیقه
     */
    private const val MAX_REQUESTS_PER_MINUTE = 30

    private var lastRequestTime: Long = 0L
    private val requestTimestamps = mutableListOf<Long>()

    /**
     * چک کن آیا می‌تونه درخواست بفرسته
     * @return true اگه مجازه، false اگه باید صبر کنه
     */
    @Synchronized
    fun canMakeRequest(): Boolean {
        val now = System.currentTimeMillis()

        // چک فاصله بین دو درخواست
        if (now - lastRequestTime < MIN_INTERVAL_MS) {
            return false
        }

        // پاک کردن timestampهای قدیمی (بیشتر از ۱ دقیقه)
        requestTimestamps.removeAll { it < now - 60_000 }

        // چک تعداد درخواست در دقیقه
        if (requestTimestamps.size >= MAX_REQUESTS_PER_MINUTE) {
            return false
        }

        return true
    }

    /**
     * ثبت یه درخواست
     */
    @Synchronized
    fun recordRequest() {
        val now = System.currentTimeMillis()
        lastRequestTime = now
        requestTimestamps.add(now)
    }

    /**
     * زمان انتظار تا درخواست بعدی (میلی‌ثانیه)
     */
    @Synchronized
    fun getWaitTime(): Long {
        val now = System.currentTimeMillis()
        val elapsed = now - lastRequestTime
        return if (elapsed < MIN_INTERVAL_MS) {
            MIN_INTERVAL_MS - elapsed
        } else 0L
    }

    /**
     * ریست کردن
     */
    @Synchronized
    fun reset() {
        lastRequestTime = 0L
        requestTimestamps.clear()
    }
}