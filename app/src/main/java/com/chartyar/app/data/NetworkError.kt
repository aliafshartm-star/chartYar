package com.chartyar.app.data

/**
 * انواع خطاهای شبکه با پیام فارسی مناسب
 */
enum class NetworkError(val message: String) {
    NO_INTERNET("اتصال اینترنت برقرار نیست. لطفاً اتصال خود را بررسی کنید."),
    TIMEOUT("زمان انتظار به پایان رسید. لطفاً دوباره تلاش کنید."),
    SERVER_ERROR("خطای سرور. لطفاً چند لحظه بعد دوباره تلاش کنید."),
    RATE_LIMITED("تعداد درخواست‌ها زیاد است. لطفاً چند دقیقه صبر کنید."),
    SYMBOL_NOT_FOUND("این ارز در منبع داده موجود نیست. لطفاً ارز دیگری انتخاب کنید."),
    DATA_EMPTY("داده‌ای برای این ارز دریافت نشد."),
    UNKNOWN("خطای نامشخص. لطفاً دوباره تلاش کنید.")
}

/**
 * تشخیص نوع خطا از روی کد پاسخ HTTP
 */
object ErrorDetector {

    fun detectFromCode(responseCode: Int): NetworkError {
        return when (responseCode) {
            408 -> NetworkError.TIMEOUT
            429 -> NetworkError.RATE_LIMITED
            400, 404 -> NetworkError.SYMBOL_NOT_FOUND
            in 500..599 -> NetworkError.SERVER_ERROR
            else -> NetworkError.UNKNOWN
        }
    }

    fun detectFromException(e: Exception): NetworkError {
        val message = e.message?.lowercase() ?: ""
        return when {
            message.contains("timeout") -> NetworkError.TIMEOUT
            message.contains("unknownhost") -> NetworkError.NO_INTERNET
            message.contains("connect") -> NetworkError.NO_INTERNET
            message.contains("socket") -> NetworkError.NO_INTERNET
            else -> NetworkError.UNKNOWN
        }
    }
}