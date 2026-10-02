package com.chartyar.app.analysis

enum class PatternType {
    // ============ الگوهای برگشتی صعودی (کلاسیک) ============
    DOUBLE_BOTTOM,
    INVERSE_HEAD_SHOULDERS,
    FALLING_WEDGE,
    ASCENDING_TRIANGLE,
    BULLISH_FLAG,
    TRIPLE_BOTTOM,
    CUP_HANDLE,

    // ============ الگوهای برگشتی نزولی (کلاسیک) ============
    DOUBLE_TOP,
    HEAD_SHOULDERS,
    RISING_WEDGE,
    DESCENDING_TRIANGLE,
    BEARISH_FLAG,
    TRIPLE_TOP,

    // ============ الگوهای ادامه‌دهنده ============
    SYMMETRICAL_TRIANGLE,
    PENNANT,

    // ============ الگوهای کندلی - برگشتی صعودی ============
    HAMMER,
    INVERTED_HAMMER,
    BULLISH_ENGULFING,
    MORNING_STAR,
    PIERCING_LINE,
    BULLISH_HARAMI,
    TWEEZER_BOTTOM,
    DRAGONFLY_DOJI,
    THREE_WHITE_SOLDIERS,

    // ============ الگوهای کندلی - برگشتی نزولی ============
    SHOOTING_STAR,
    HANGING_MAN,
    BEARISH_ENGULFING,
    EVENING_STAR,
    DARK_CLOUD_COVER,
    BEARISH_HARAMI,
    TWEEZER_TOP,
    GRAVESTONE_DOJI,
    THREE_BLACK_CROWS,

    // ============ الگوهای کندلی - خنثی ============
    DOJI,
    SPINNING_TOP,

    // ============ نامشخص ============
    NONE
}

data class DetectedPattern(
    val type: PatternType,
    val name: String,
    val direction: Int,       // 1 = صعودی، -1 = نزولی، 0 = خنثی
    val confidence: Int,      // 0-100
    val startIndex: Int,
    val endIndex: Int,
    val description: String,
    val targetPrice: Double? = null
)