package com.chartyar.app.analysis

import com.chartyar.app.data.CacheManager
import com.chartyar.app.data.MarketDataService

data class TimeframeAnalysis(
    val timeframe: String,
    val signal: SignalType,
    val confidence: Int,
    val trend: TrendType,
    val score: Double
)

data class MultiTimeframeResult(
    val symbol: String,
    val analyses: List<TimeframeAnalysis>,
    val combinedSignal: SignalType,
    val combinedConfidence: Int,
    val alignment: Int,
    val summary: String
)

object MultiTimeframeAnalyzer {

    /**
     * انتخاب ۳ تایم‌فریم مرتبط بر اساس تایم فعلی
     */
    private fun getRelatedTimeframes(current: String): Triple<String, String, String> {
        return when (current) {
            "1m" -> Triple("15m", "1m", "1m")
            "5m" -> Triple("30m", "5m", "1m")
            "15m" -> Triple("1H", "15m", "5m")
            "30m" -> Triple("4H", "30m", "15m")
            "1H" -> Triple("4H", "1H", "15m")
            "4H" -> Triple("1D", "4H", "1H")
            "1D" -> Triple("1W", "1D", "4H")
            "1W" -> Triple("1M", "1W", "1D")
            "1M" -> Triple("1M", "1M", "1W")
            else -> Triple("1D", "4H", "1H")
        }
    }

    /**
     * وزن‌دهی تایم‌فریم‌ها
     */
    private fun getWeights(higher: String, current: String, lower: String): Triple<Double, Double, Double> {
        return if (higher == current && current == lower) {
            Triple(1.0, 0.0, 0.0)
        } else if (higher == current) {
            Triple(0.6, 0.0, 0.4)
        } else if (current == lower) {
            Triple(0.6, 0.4, 0.0)
        } else {
            Triple(0.5, 0.3, 0.2)
        }
    }

    /**
     * تبدیل سیگنال به امتیاز
     */
    private fun signalToScore(signal: SignalType): Double {
        return when (signal) {
            SignalType.STRONG_BUY -> 100.0
            SignalType.BUY -> 60.0
            SignalType.NEUTRAL -> 0.0
            SignalType.SELL -> -60.0
            SignalType.STRONG_SELL -> -100.0
        }
    }

    /**
     * تحلیل چند تایم‌فریم
     */
    suspend fun analyze(
        symbol: String,
        currentTimeframe: String,
        cache: CacheManager
    ): MultiTimeframeResult? {
        val (higher, current, lower) = getRelatedTimeframes(currentTimeframe)
        val (wHigher, wCurrent, wLower) = getWeights(higher, current, lower)

        val analyses = mutableListOf<TimeframeAnalysis>()

        // تحلیل تایم بالاتر
        val higherAnalysis = analyzeOneTimeframe(symbol, higher, cache)
        if (higherAnalysis != null) analyses.add(higherAnalysis)

        // تحلیل تایم فعلی
        if (current != higher) {
            val currentAnalysis = analyzeOneTimeframe(symbol, current, cache)
            if (currentAnalysis != null) analyses.add(currentAnalysis)
        }

        // تحلیل تایم پایین‌تر
        if (lower != higher && lower != current) {
            val lowerAnalysis = analyzeOneTimeframe(symbol, lower, cache)
            if (lowerAnalysis != null) analyses.add(lowerAnalysis)
        }

        if (analyses.isEmpty()) return null

        // محاسبه امتیاز ترکیبی
        var combinedScore = 0.0
        var totalWeight = 0.0

        analyses.forEach { analysis ->
            val weight = when (analysis.timeframe) {
                higher -> wHigher
                current -> wCurrent
                lower -> wLower
                else -> 0.0
            }
            combinedScore += signalToScore(analysis.signal) * weight
            totalWeight += weight
        }

        val finalScore = if (totalWeight > 0) combinedScore / totalWeight else 0.0

        // تعیین سیگنال نهایی
        val combinedSignal = when {
            finalScore >= 60 -> SignalType.STRONG_BUY
            finalScore >= 25 -> SignalType.BUY
            finalScore <= -60 -> SignalType.STRONG_SELL
            finalScore <= -25 -> SignalType.SELL
            else -> SignalType.NEUTRAL
        }

        val combinedConfidence = minOf(95, maxOf(30, (kotlin.math.abs(finalScore) + 40).toInt()))

        // محاسبه هماهنگی (Alignment)
        val buyCount = analyses.count { it.signal == SignalType.BUY || it.signal == SignalType.STRONG_BUY }
        val sellCount = analyses.count { it.signal == SignalType.SELL || it.signal == SignalType.STRONG_SELL }
        val total = analyses.size

        val alignment = when {
            total == 0 -> 0
            buyCount == total -> 100
            sellCount == total -> 100
            buyCount > sellCount -> (buyCount * 100) / total
            sellCount > buyCount -> (sellCount * 100) / total
            else -> 50
        }

        // خلاصه
        val summary = buildString {
            appendLine("تحلیل ${analyses.size} تایم‌فریم:")
            analyses.forEach { a ->
                val signalText = when (a.signal) {
                    SignalType.STRONG_BUY -> "خرید قوی"
                    SignalType.BUY -> "خرید"
                    SignalType.NEUTRAL -> "خنثی"
                    SignalType.SELL -> "فروش"
                    SignalType.STRONG_SELL -> "فروش قوی"
                }
                appendLine("• ${a.timeframe}: $signalText (${a.confidence}%)")
            }
            appendLine()
            when {
                alignment >= 80 -> append("✅ همه تایم‌فریم‌ها هم‌جهت هستن — سیگنال معتبر")
                alignment >= 60 -> append("⚠️ بیشتر تایم‌فریم‌ها هم‌جهت هستن — نسبتاً معتبر")
                else -> append("❌ تایم‌فریم‌ها هم‌جهت نیستن — احتیاط کن")
            }
        }

        return MultiTimeframeResult(
            symbol = symbol,
            analyses = analyses,
            combinedSignal = combinedSignal,
            combinedConfidence = combinedConfidence,
            alignment = alignment,
            summary = summary
        )
    }

    /**
     * تحلیل یه تایم‌فریم خاص
     */
    private suspend fun analyzeOneTimeframe(
        symbol: String,
        timeframe: String,
        cache: CacheManager
    ): TimeframeAnalysis? {
        return try {
            val dataResult = MarketDataService.getCandles(symbol, timeframe, cache)
            val candles = dataResult.candles ?: return null
            if (candles.size < 20) return null

            val result = SignalEngine.analyze(candles, timeframe) ?: return null

            val score = signalToScore(result.signal)

            TimeframeAnalysis(
                timeframe = timeframe,
                signal = result.signal,
                confidence = result.confidence,
                trend = result.trend,
                score = score
            )
        } catch (e: Exception) {
            null
        }
    }
}