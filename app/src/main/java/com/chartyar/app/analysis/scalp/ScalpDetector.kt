package com.chartyar.app.analysis.scalp

import com.chartyar.app.analysis.Candle
import com.chartyar.app.analysis.TechnicalIndicators
import kotlin.math.max
import kotlin.math.min

object ScalpDetector {

    data class ScalpResult(
        val totalScore: Int,
        val layers: Map<String, Int>,
        val reasons: List<String>,
        val warnings: List<String>,
        val passed: Boolean
    )

    fun analyze(candles: List<Candle>): ScalpResult {
        val reasons = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val layers = mutableMapOf<String, Int>()

        if (candles.size < 30) {
            return ScalpResult(0, layers, reasons, listOf("کندل کافی نیست"), false)
        }

        val (trendScore, trendReasons, trendWarnings) = analyzeTrend(candles)
        layers["Trend"] = trendScore
        reasons.addAll(trendReasons)
        warnings.addAll(trendWarnings)

        val (momScore, momReasons, momWarnings) = analyzeMomentum(candles)
        layers["Momentum"] = momScore
        reasons.addAll(momReasons)
        warnings.addAll(momWarnings)

        val (confScore, confReasons, confWarnings) = analyzeConfirmation(candles)
        layers["Confirmation"] = confScore
        reasons.addAll(confReasons)
        warnings.addAll(confWarnings)

        val total = layers.values.sum()
        val passed = trendScore >= 10 && momScore >= 8 && confScore >= 5

        return ScalpResult(total, layers, reasons, warnings, passed)
    }

    private fun analyzeTrend(candles: List<Candle>): Triple<Int, List<String>, List<String>> {
        var score = 0
        val reasons = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        val closes = candles.map { it.close }
        val price = closes.last()

        val ema9 = TechnicalIndicators.ema(closes, 9).lastOrNull() ?: price
        val ema21 = TechnicalIndicators.ema(closes, 21).lastOrNull() ?: price
        val ema50 = TechnicalIndicators.ema(closes, 50).lastOrNull() ?: price

        if (ema9 > ema21) {
            score += 8
            reasons.add("روند صعودی (EMA9 > EMA21)")
        } else {
            warnings.add("EMA9 زیر EMA21 — روند نزولی")
        }

        if (price > ema50) {
            score += 6
            reasons.add("قیمت بالای EMA50")
        }

        val adx = TechnicalIndicators.adx(candles, 14).lastOrNull()
        if (adx != null) {
            when {
                adx > 25 -> {
                    score += 6
                    reasons.add("ADX قوی (${"%.0f".format(adx)})")
                }
                adx > 20 -> {
                    score += 3
                    reasons.add("ADX متوسط")
                }
                else -> {
                    warnings.add("ADX ضعیف — بازار رنج")
                    score -= 3
                }
            }
        }

        return Triple(max(0, min(20, score)), reasons, warnings)
    }

    private fun analyzeMomentum(candles: List<Candle>): Triple<Int, List<String>, List<String>> {
        var score = 0
        val reasons = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        val closes = candles.map { it.close }
        val price = closes.last()

        val rsi = TechnicalIndicators.rsi(closes, 14).lastOrNull() ?: 50.0
        when {
            rsi in 40.0..60.0 -> {
                score += 5
                reasons.add("RSI متعادل (${"%.0f".format(rsi)})")
            }
            rsi in 30.0..40.0 -> {
                score += 8
                reasons.add("RSI پایین — فرصت خرید (${"%.0f".format(rsi)})")
            }
            rsi > 70 -> {
                score -= 5
                warnings.add("RSI اشباع خرید (${"%.0f".format(rsi)})")
            }
            rsi < 30 -> {
                score -= 5
                warnings.add("RSI اشباع فروش (${"%.0f".format(rsi)})")
            }
        }

        val vwap = TechnicalIndicators.vwap(candles).lastOrNull()
        if (vwap != null && vwap > 0) {
            if (price > vwap) {
                score += 7
                reasons.add("قیمت بالای VWAP")
            } else {
                score -= 7
                warnings.add("قیمت زیر VWAP")
            }
        }

        val stoch = TechnicalIndicators.stochastic(candles, 14, 3)
        val k = stoch.k.lastOrNull()
        val d = stoch.d.lastOrNull()
        if (k != null && d != null) {
            if (k < 20 && k > d) {
                score += 5
                reasons.add("Stochastic — سیگنال خرید")
            } else if (k > 80 && k < d) {
                score -= 5
                warnings.add("Stochastic — سیگنال فروش")
            }
        }

        return Triple(max(0, min(20, score)), reasons, warnings)
    }

    private fun analyzeConfirmation(candles: List<Candle>): Triple<Int, List<String>, List<String>> {
        var score = 0
        val reasons = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        val lastVol = candles.last().volume
        val avgVol = candles.dropLast(1).takeLast(20).map { it.volume }.average()
        if (avgVol > 0) {
            val ratio = lastVol / avgVol
            when {
                ratio > 1.5 -> {
                    score += 8
                    reasons.add("حجم بالا (${"%.1f".format(ratio)}x)")
                }
                ratio > 1.2 -> {
                    score += 5
                    reasons.add("حجم در حال افزایش")
                }
                ratio < 0.7 -> {
                    warnings.add("حجم پایین")
                }
            }
        }

        val last = candles.last()
        if (last.isBullish && last.body > last.range * 0.4) {
            score += 5
            reasons.add("کندل صعودی قوی")
        }

        val closes = candles.map { it.close }
        val bb = TechnicalIndicators.bollinger(closes, 20, 2.0)
        val upper = bb.upper.lastOrNull()
        val lower = bb.lower.lastOrNull()
        val middle = bb.middle.lastOrNull()
        val price = closes.last()

        if (upper != null && lower != null && middle != null) {
            when {
                price <= lower -> {
                    score += 7
                    reasons.add("قیمت روی Bollinger پایین")
                }
                price > middle -> score += 3
                price >= upper -> {
                    score -= 5
                    warnings.add("قیمت روی Bollinger بالا")
                }
            }
        }

        return Triple(max(0, min(20, score)), reasons, warnings)
    }
}