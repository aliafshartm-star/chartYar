package com.chartyar.app.analysis

/**
 * ساختار یک کندل قیمتی
 * @param open قیمت باز شدن
 * @param high بالاترین قیمت
 * @param low پایین‌ترین قیمت
 * @param close قیمت بسته شدن
 * @param volume حجم معاملات
 * @param timestamp زمان (اختیاری)
 */
data class Candle(
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 0.0,
    val timestamp: Long = 0L
) {
    val isBullish: Boolean get() = close > open
    val isBearish: Boolean get() = close < open
    val body: Double get() = kotlin.math.abs(close - open)
    val range: Double get() = high - low
    val upperWick: Double get() = high - maxOf(open, close)
    val lowerWick: Double get() = minOf(open, close) - low
}