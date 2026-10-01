package com.chartyar.app.analysis

/**
 * نتیجه تحلیل یک تایم‌فریم در حالت Multi-TF
 */
data class TimeframeAnalysis(
    val timeframe: String,
    val signal: SignalType,
    val trend: TrendType,
    val confidence: Int
)

/**
 * نتیجه تحلیل چند تایم‌فریمی
 */
data class MultiTimeframeResult(
    val symbol: String,
    val combinedSignal: SignalType,
    val combinedConfidence: Int,
    val alignment: Int,
    val analyses: List<TimeframeAnalysis>,
    val summary: String
)