package com.chartyar.app.analysis.pump

import com.chartyar.app.analysis.Candle
import com.chartyar.app.analysis.TechnicalIndicators
import kotlin.math.max
import kotlin.math.min

/**
 * PumpDetector — ۵ نیرو برای تشخیص ارزهای آماده رشد
 * کاملاً آفلاین، فقط با کندل‌های نوبیتکس کار می‌کنه
 */
object PumpDetector {

    data class PumpResult(
        val totalScore: Int,
        val forces: Map<String, Int>,
        val reasons: List<String>,
        val warnings: List<String>
    )

    fun analyze(
        candles: List<Candle>,
        btcChange24h: Double = 0.0,
        fearGreed: Int = 50
    ): PumpResult {
        val reasons = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val forces = mutableMapOf<String, Int>()

        // نیرو ۱: Volume Accumulation
        val (volScore, volReasons, volWarnings) = detectVolumeAccumulation(candles)
        forces["Volume"] = volScore
        reasons.addAll(volReasons)
        warnings.addAll(volWarnings)

        // نیرو ۲: Trend Strength
        val (trendScore, trendReasons) = detectTrendStrength(candles)
        forces["Trend"] = trendScore
        reasons.addAll(trendReasons)

        // نیرو ۳: News (بر اساس Volume Spike)
        val (newsScore, newsReasons) = detectNewsSignal(candles)
        forces["News"] = newsScore
        reasons.addAll(newsReasons)

        // نیرو ۴: Momentum
        val (momScore, momReasons) = detectMomentum(candles)
        forces["Momentum"] = momScore
        reasons.addAll(momReasons)

        // نیرو ۵: Regime (BTC + Fear&Greed)
        val (regScore, regReasons) = detectMarketRegime(btcChange24h, fearGreed)
        forces["Regime"] = regScore
        reasons.addAll(regReasons)

        return PumpResult(
            totalScore = forces.values.sum(),
            forces = forces,
            reasons = reasons,
            warnings = warnings
        )
    }

    // ═══════ نیرو ۱: Volume Accumulation ═══════
    private fun detectVolumeAccumulation(
        candles: List<Candle>
    ): Triple<Int, List<String>, List<String>> {
        if (candles.size < 60) return Triple(0, emptyList(), emptyList())
        var score = 0
        val reasons = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // Relative Volume: ۷ روز اخیر vs ۳۰ روز اخیر
        val vol7d = candles.takeLast(42).map { it.volume }.average()
        val vol30d = candles.takeLast(min(candles.size, 180)).map { it.volume }.average()

        if (vol30d > 0) {
            val ratio = vol7d / vol30d
            when {
                ratio > 1.5 -> {
                    score += 8
                    reasons.add("حجم ۷ روزه ${"%.1f".format(ratio)}x میانگین")
                }
                ratio > 1.2 -> {
                    score += 5
                    reasons.add("حجم در حال افزایش (${"%.1f".format(ratio)}x)")
                }
                ratio > 1.0 -> score += 3
                ratio < 0.7 -> warnings.add("حجم کمتر از حد معمول")
            }
        }

        // Higher Lows
        val swingLows = findSwingLows(candles, 5)
        if (swingLows.size >= 3) {
            val last3 = swingLows.takeLast(3)
            if (last3[0] < last3[1] && last3[1] < last3[2]) {
                score += 7
                reasons.add("Higher Lows صعودی")
            }
        }

        // کف‌های اخیر بالاتر
        if (candles.size >= 30) {
            val recentLow = candles.takeLast(5).minOf { it.low }
            val prevLow = candles.dropLast(5).takeLast(25).minOf { it.low }
            if (prevLow > 0 && recentLow > prevLow * 1.01) {
                score += 5
                reasons.add("کف‌ها بالاتر از قبل")
            }
        }

        return Triple(min(20, score), reasons, warnings)
    }

    // ═══════ نیرو ۲: Trend Strength ═══════
    private fun detectTrendStrength(candles: List<Candle>): Pair<Int, List<String>> {
        if (candles.size < 60) return 0 to emptyList()
        var score = 0
        val reasons = mutableListOf<String>()

        val closes = candles.map { it.close }
        val price = closes.last()

        val ema20 = TechnicalIndicators.ema(closes, 20).lastOrNull() ?: price
        val ema50 = TechnicalIndicators.ema(closes, 50).lastOrNull() ?: price

        if (price > ema20 && ema20 > ema50) {
            score += 8
            reasons.add("روند صعودی (EMA20 > EMA50)")
        } else if (price > ema20) {
            score += 4
            reasons.add("قیمت بالای EMA20")
        }

        // رشد ۷ روزه
        if (candles.size >= 42) {
            val change7d = (closes.last() / closes[closes.size - 42]) - 1.0
            when {
                change7d > 0.10 -> {
                    score += 6
                    reasons.add("رشد ۷ روزه +${"%.1f".format(change7d * 100)}%")
                }
                change7d > 0.03 -> {
                    score += 4
                    reasons.add("رشد ۷ روزه +${"%.1f".format(change7d * 100)}%")
                }
                change7d > 0 -> score += 2
            }
        }

        // مومنتوم ۲۴ ساعته (۶ کندل 4H)
        if (candles.size >= 6) {
            val change24h = (closes.last() / closes[closes.size - 6]) - 1.0
            if (change24h > 0.02) {
                score += 6
                reasons.add("مومنتوم ۲۴ ساعته مثبت")
            } else if (change24h > 0) score += 3
        }

        return min(20, score) to reasons
    }

    // ═══════ نیرو ۳: News (Volume Spike) ═══════
    private fun detectNewsSignal(candles: List<Candle>): Pair<Int, List<String>> {
        if (candles.size < 20) return 0 to emptyList()
        var score = 0
        val reasons = mutableListOf<String>()

        val lastVol = candles.last().volume
        val avgVol = candles.dropLast(1).takeLast(20).map { it.volume }.average()

        if (avgVol > 0) {
            val ratio = lastVol / avgVol
            when {
                ratio > 2.0 -> {
                    score += 12
                    reasons.add("Volume Spike 🔥")
                }
                ratio > 1.5 -> {
                    score += 8
                    reasons.add("حجم بالا")
                }
                ratio > 1.2 -> score += 4
            }
        }

        return min(20, score) to reasons
    }

    // ═══════ نیرو ۴: Momentum ═══════
    private fun detectMomentum(candles: List<Candle>): Pair<Int, List<String>> {
        if (candles.size < 20) return 0 to emptyList()
        var score = 0
        val reasons = mutableListOf<String>()

        val closes = candles.map { it.close }
        val rsi = TechnicalIndicators.rsi(closes, 14).lastOrNull() ?: 50.0

        when {
            rsi in 40.0..60.0 -> {
                score += 5
                reasons.add("RSI متعادل (${"%.0f".format(rsi)})")
            }
            rsi in 30.0..40.0 -> {
                score += 7
                reasons.add("RSI پایین — فرصت (${"%.0f".format(rsi)})")
            }
        }

        // فاصله از مقاومت
        val recentHigh = candles.takeLast(20).maxOf { it.high }
        val price = closes.last()
        val distToHigh = (recentHigh - price) / price

        when {
            distToHigh < 0.02 -> {
                score += 8
                reasons.add("نزدیک مقاومت — آماده شکست")
            }
            distToHigh < 0.05 -> {
                score += 4
                reasons.add("فاصله کم از مقاومت")
            }
        }

        return min(20, score) to reasons
    }

    // ═══════ نیرو ۵: Regime ═══════
    private fun detectMarketRegime(btcChange: Double, fearGreed: Int): Pair<Int, List<String>> {
        var score = 0
        val reasons = mutableListOf<String>()

        when {
            btcChange > 1 -> {
                score += 8
                reasons.add("BTC مثبت (${"%+.1f".format(btcChange)}%)")
            }
            btcChange > -1 -> {
                score += 6
                reasons.add("BTC پایدار (${"%+.1f".format(btcChange)}%)")
            }
            btcChange > -3 -> score += 3
            else -> {
                score -= 5
                reasons.add("BTC نزولی (${"%+.1f".format(btcChange)}%)")
            }
        }

        when {
            fearGreed in 25..65 -> {
                score += 7
                reasons.add("Fear & Greed: $fearGreed")
            }
            fearGreed < 25 -> {
                score += 10
                reasons.add("Fear & Greed: $fearGreed (فرصت)")
            }
            fearGreed > 80 -> score -= 3
            else -> score += 4
        }

        return max(0, min(20, score)) to reasons
    }

    // ═══════ ابزارها ═══════
    private fun findSwingLows(candles: List<Candle>, lookback: Int): List<Double> {
        val lows = mutableListOf<Double>()
        if (candles.size < lookback * 2 + 1) return lows

        for (i in lookback until candles.size - lookback) {
            val current = candles[i].low
            var isLow = true
            for (j in (i - lookback)..(i + lookback)) {
                if (j != i && candles[j].low < current) {
                    isLow = false
                    break
                }
            }
            if (isLow) lows.add(current)
        }
        return lows
    }
}