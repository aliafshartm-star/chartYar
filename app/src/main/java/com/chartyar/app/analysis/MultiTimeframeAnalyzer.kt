package com.chartyar.app.analysis

import com.chartyar.app.data.CacheManager
import com.chartyar.app.data.MarketDataService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

object MultiTimeframeAnalyzer {

    val DEFAULT_TIMEFRAMES = listOf("15m", "1H", "4H", "1D")

    private fun weight(tf: String): Double = when (tf) {
        "1m" -> 0.8
        "5m" -> 1.0
        "15m" -> 1.2
        "30m" -> 1.5
        "1H" -> 2.0
        "4H" -> 3.0
        "1D" -> 3.5
        "1W" -> 4.0
        "1M" -> 5.0
        else -> 1.0
    }

    private fun signalValue(s: SignalType): Int = when (s) {
        SignalType.STRONG_BUY -> 2
        SignalType.BUY -> 1
        SignalType.NEUTRAL -> 0
        SignalType.SELL -> -1
        SignalType.STRONG_SELL -> -2
    }

    suspend fun analyze(
        symbol: String,
        timeframes: List<String> = DEFAULT_TIMEFRAMES,
        cache: CacheManager
    ): MultiTimeframeResult? = withContext(Dispatchers.IO) {

        // مرحله ۱: دریافت و تحلیل موازی همه تایم‌فریم‌ها
        val results: List<Pair<String, AnalysisResult?>> = coroutineScope {
            timeframes.map { tf ->
                async {
                    try {
                        val data = MarketDataService.getCandles(symbol, tf, cache)
                        val candles = data.candles
                        if (candles == null || candles.size < 50) {
                            tf to null
                        } else {
                            val r = withContext(Dispatchers.Default) {
                                SignalEngine.analyze(candles, tf)
                            }
                            tf to r
                        }
                    } catch (e: Exception) {
                        tf to null
                    }
                }
            }.awaitAll()
        }

        // مرحله ۲: جدا کردن موفق از ناموفق
        val success = mutableMapOf<String, AnalysisResult>()
        results.forEach { (tf, r) ->
            if (r != null) success[tf] = r
        }

        if (success.size < 2) return@withContext null

        // مرحله ۳: سیگنال ترکیبی وزنی
        var weightedSum = 0.0
        var totalWeight = 0.0
        success.forEach { (tf, r) ->
            val w = weight(tf)
            weightedSum += signalValue(r.signal) * w
            totalWeight += w
        }
        val normalized = if (totalWeight > 0) weightedSum / totalWeight else 0.0

        val combinedSignal = when {
            normalized >= 1.2 -> SignalType.STRONG_BUY
            normalized >= 0.4 -> SignalType.BUY
            normalized <= -1.2 -> SignalType.STRONG_SELL
            normalized <= -0.4 -> SignalType.SELL
            else -> SignalType.NEUTRAL
        }

        // مرحله ۴: اطمینان وزنی
        var weightedConf = 0.0
        success.forEach { (tf, r) ->
            weightedConf += r.confidence * weight(tf)
        }
        val avgConf = if (totalWeight > 0) weightedConf / totalWeight else 50.0

        // مرحله ۵: هماهنگی (چند تا هم‌جهت هستن)
        val overallDir = signalValue(combinedSignal).coerceIn(-1, 1)
        val aligned = success.count { (_, r) ->
            val d = signalValue(r.signal)
            when {
                overallDir > 0 -> d > 0
                overallDir < 0 -> d < 0
                else -> d == 0
            }
        }
        val alignment = ((aligned.toDouble() / success.size) * 100).toInt()

        // مرحله ۶: اطمینان نهایی با پاداش هماهنگی
        val finalConf = (avgConf + (alignment - 50) * 0.3).toInt().coerceIn(30, 95)

        // مرحله ۷: لیست تحلیل‌های هر TF
        val analyses = success.map { (tf, r) ->
            TimeframeAnalysis(
                timeframe = tf,
                signal = r.signal,
                trend = r.trend,
                confidence = r.confidence
            )
        }

        // مرحله ۸: خلاصه فارسی
        val dirText = when (combinedSignal) {
            SignalType.STRONG_BUY -> "صعودی قوی"
            SignalType.BUY -> "صعودی"
            SignalType.NEUTRAL -> "خنثی"
            SignalType.SELL -> "نزولی"
            SignalType.STRONG_SELL -> "نزولی قوی"
        }

        val summary = buildString {
            append("$aligned از ${success.size} تایم‌فریم هم‌جهت با سیگنال $dirText. ")
            when {
                alignment >= 80 -> append("هماهنگی بین تایم‌فریم‌ها عالیه و اعتبار سیگنال بالاست.")
                alignment >= 60 -> append("هماهنگی قابل قبول، ولی احتیاط کن.")
                else -> append("تایم‌فریم‌ها هم‌جهت نیستن؛ احتمال نوسان و سیگنال کاذب زیاده.")
            }
        }

        MultiTimeframeResult(
            symbol = symbol,
            combinedSignal = combinedSignal,
            combinedConfidence = finalConf,
            alignment = alignment,
            analyses = analyses,
            summary = summary
        )
    }
}