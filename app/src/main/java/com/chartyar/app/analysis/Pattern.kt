package com.chartyar.app.analysis

enum class PatternType {
    // برگشتی صعودی
    DOUBLE_BOTTOM,
    INVERSE_HEAD_SHOULDERS,
    FALLING_WEDGE,
    ASCENDING_TRIANGLE,
    BULLISH_FLAG,

    // برگشتی نزولی
    DOUBLE_TOP,
    HEAD_SHOULDERS,
    RISING_WEDGE,
    DESCENDING_TRIANGLE,
    BEARISH_FLAG,

    // ادامه‌دهنده
    SYMMETRICAL_TRIANGLE,
    PENNANT,

    // نامشخص
    NONE
}

data class DetectedPattern(
    val type: PatternType,
    val name: String,
    val direction: Int,       // 1 = صعودی، -1 = نزولی، 0 = خنثی
    val confidence: Int,      // 0-100
    val startIndex: Int,      // ایندکس شروع الگو در لیست کندل‌ها
    val endIndex: Int,        // ایندکس پایان
    val description: String,
    val targetPrice: Double? = null
)