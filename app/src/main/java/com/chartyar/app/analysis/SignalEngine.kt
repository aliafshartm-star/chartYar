package com.chartyar.app.analysis

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

enum class SignalType { STRONG_BUY, BUY, NEUTRAL, SELL, STRONG_SELL }
enum class TrendType { UPTREND, DOWNTREND, RANGE }

data class AnalysisResult(
    val signal: SignalType,
    val trend: TrendType,
    val confidence: Int,
    val currentPrice: Double,
    val entry: Double,
    val stopLoss: Double,
    val target1: Double,
    val target2: Double,
    val target3: Double,
    val riskReward: Double,
    val supportLevels: List<Double>,
    val resistanceLevels: List<Double>,
    val reasons: List<String>,
    val warnings: List<String>,
    val timeframe: String,
    val indicatorScores: Map<String, Int>,
    val patterns: List<DetectedPattern> = emptyList(),
    val visionTrendLines: List<ChartVisionAnalyzer.TrendLine> = emptyList(),
    val visionDominantTrend: Int = 0
)

object SignalEngine {

    fun analyze(candles: List<Candle>, timeframe: String): AnalysisResult? {
        if (candles.size < 50) return null

        val closes = candles.map { it.close }
        val currentPrice = closes.last()
        val reasons = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        val scores = mutableMapOf<String, Int>()

        // ============ ۱. ساختار بازار ============
        val trendScore = analyzeMarketStructure(candles, reasons)
        scores["Market Structure"] = trendScore

        val trend = when {
            trendScore >= 40 -> TrendType.UPTREND
            trendScore <= -40 -> TrendType.DOWNTREND
            else -> TrendType.RANGE
        }

        // ============ ۲. RSI ============
        val rsiValues = TechnicalIndicators.rsi(closes, 14)
        val rsi = rsiValues.lastOrNull()
        var rsiScore = 0
        if (rsi != null) {
            rsiScore = when {
                rsi < 30 -> {
                    reasons.add("RSI oversold (${"%.1f".format(rsi)}) - possible bullish reversal")
                    70
                }
                rsi < 40 -> {
                    reasons.add("RSI weak but improving (${"%.1f".format(rsi)})")
                    40
                }
                rsi in 40.0..60.0 -> 0
                rsi < 70 -> {
                    reasons.add("RSI normal but rising (${"%.1f".format(rsi)})")
                    -20
                }
                else -> {
                    reasons.add("RSI overbought (${"%.1f".format(rsi)}) - possible correction")
                    -70
                }
            }
        }
        scores["RSI"] = rsiScore

        // ============ ۳. واگرایی RSI ============
        val divergence = TechnicalIndicators.detectDivergence(candles, rsiValues, 30)
        val divScore = when (divergence) {
            1 -> {
                reasons.add("Bullish RSI divergence - reversal signal")
                60
            }
            -1 -> {
                reasons.add("Bearish RSI divergence - reversal signal")
                -60
            }
            else -> 0
        }
        scores["Divergence"] = divScore

        // ============ ۴. MACD ============
        val macdResult = TechnicalIndicators.macd(closes)
        val macdLine = macdResult.macdLine.lastOrNull()
        val signalLine = macdResult.signalLine.lastOrNull()
        val histogram = macdResult.histogram.lastOrNull()
        var macdScore = 0
        if (macdLine != null && signalLine != null && histogram != null) {
            macdScore = when {
                macdLine > signalLine && histogram > 0 -> {
                    reasons.add("MACD bullish cross - positive momentum")
                    50
                }
                macdLine < signalLine && histogram < 0 -> {
                    reasons.add("MACD bearish cross - negative momentum")
                    -50
                }
                else -> 0
            }
            val prevHist = macdResult.histogram.dropLast(1).lastOrNull()
            if (prevHist != null && histogram > prevHist && histogram > 0) {
                macdScore += 10
                reasons.add("MACD histogram increasing - trend strengthening")
            }
        }
        scores["MACD"] = macdScore

        // ============ ۵. EMA ============
        val ema9 = TechnicalIndicators.ema(closes, 9).lastOrNull()
        val ema21 = TechnicalIndicators.ema(closes, 21).lastOrNull()
        val ema50 = TechnicalIndicators.ema(closes, 50).lastOrNull()
        var emaScore = 0
        if (ema9 != null && ema21 != null) {
            if (ema9 > ema21) {
                emaScore += 30
                reasons.add("EMA9 above EMA21 - short-term uptrend")
            } else {
                emaScore -= 30
                reasons.add("EMA9 below EMA21 - short-term downtrend")
            }
        }
        if (ema50 != null) {
            if (currentPrice > ema50) {
                emaScore += 20
                reasons.add("Price above EMA50 - mid-term uptrend")
            } else {
                emaScore -= 20
                reasons.add("Price below EMA50 - mid-term downtrend")
            }
        }
        scores["EMA"] = emaScore

        // ============ ۶. ADX ============
        val adxList = TechnicalIndicators.adx(candles, 14)
        val adx = adxList.lastOrNull()
        var adxScore = 0
        if (adx != null) {
            when {
                adx > 40 -> {
                    adxScore = 30
                    reasons.add("ADX strong (${"%.1f".format(adx)}) - powerful trend")
                }
                adx > 25 -> {
                    adxScore = 15
                    reasons.add("ADX moderate (${"%.1f".format(adx)}) - active trend")
                }
                else -> {
                    warnings.add("ADX weak (${"%.1f".format(adx)}) - ranging market")
                }
            }
            if (ema9 != null && ema21 != null && ema9 < ema21) adxScore = -adxScore
        }
        scores["ADX"] = adxScore

        // ============ ۷. Stochastic ============
        val stoch = TechnicalIndicators.stochastic(candles, 14, 3)
        val k = stoch.k.lastOrNull()
        val d = stoch.d.lastOrNull()
        var stochScore = 0
        if (k != null && d != null) {
            when {
                k < 20 && k > d -> {
                    stochScore = 40
                    reasons.add("Stochastic oversold with bullish cross")
                }
                k > 80 && k < d -> {
                    stochScore = -40
                    reasons.add("Stochastic overbought with bearish cross")
                }
            }
        }
        scores["Stochastic"] = stochScore

        // ============ ۸. Bollinger Bands ============
        val bb = TechnicalIndicators.bollinger(closes, 20, 2.0)
        val upper = bb.upper.lastOrNull()
        val middle = bb.middle.lastOrNull()
        val lower = bb.lower.lastOrNull()
        var bbScore = 0
        if (upper != null && lower != null && middle != null) {
            bbScore = when {
                currentPrice <= lower -> {
                    reasons.add("Price on lower Bollinger Band - oversold")
                    50
                }
                currentPrice >= upper -> {
                    reasons.add("Price on upper Bollinger Band - overbought")
                    -50
                }
                currentPrice > middle -> 15
                else -> -15
            }
        }
        scores["Bollinger"] = bbScore

        // ============ ۹. تحلیل کندلی ============
        val priceActionScore = analyzePriceAction(candles, reasons, warnings)
        scores["Price Action"] = priceActionScore

        // ============ ۱۰. الگوهای کلاسیک ============
        val detectedPatterns = PatternDetector.detectAll(candles)
        var patternScore = 0
        if (detectedPatterns.isNotEmpty()) {
            val bestPattern = detectedPatterns.first()
            patternScore = (bestPattern.direction * bestPattern.confidence * 0.8).toInt()

            val dirText = when (bestPattern.direction) {
                1 -> "bullish"
                -1 -> "bearish"
                else -> "neutral"
            }
            reasons.add("Pattern ${bestPattern.name} - $dirText signal (confidence ${bestPattern.confidence}%)")

            if (detectedPatterns.size > 1) {
                detectedPatterns.drop(1).take(2).forEach {
                    reasons.add("Additional pattern: ${it.name}")
                }
            }
        }
        scores["Patterns"] = patternScore

        // ============ ۱۱. Computer Vision Analysis ============
        val vision = ChartVisionAnalyzer.analyze(candles)
        val visionScore = when (vision.dominantTrend) {
            1 -> 45
            -1 -> -45
            else -> 0
        }
        scores["Vision"] = visionScore

        when (vision.dominantTrend) {
            1 -> reasons.add("Vision detected bullish trend lines")
            -1 -> reasons.add("Vision detected bearish trend lines")
        }

        for (line in vision.trendLines) {
            when {
                line.isSupport && line.slope < 0 -> {
                    reasons.add("Rising support line detected (strength ${line.strength})")
                }
                !line.isSupport && line.slope > 0 -> {
                    reasons.add("Falling resistance line detected (strength ${line.strength})")
                }
                line.isSupport -> {
                    reasons.add("Descending support detected (strength ${line.strength})")
                }
                else -> {
                    reasons.add("Ascending resistance detected (strength ${line.strength})")
                }
            }
        }

        // ============ وزن‌دهی بر اساس تایم‌فریم ============
        val weights = getTimeframeWeights(timeframe)

        // ============ جمع‌بندی امتیاز ============
        var totalScore = 0.0
        var totalWeight = 0.0
        scores.forEach { (name, score) ->
            val w = weights[name] ?: 1.0
            totalScore += score * w
            totalWeight += w
        }
        val normalizedScore = if (totalWeight > 0) totalScore / totalWeight else 0.0

        // ============ سیگنال نهایی ============
        val signal = when {
            normalizedScore >= 45 -> SignalType.STRONG_BUY
            normalizedScore >= 20 -> SignalType.BUY
            normalizedScore <= -45 -> SignalType.STRONG_SELL
            normalizedScore <= -20 -> SignalType.SELL
            else -> SignalType.NEUTRAL
        }

        val confidence = min(95, max(30, (abs(normalizedScore) + 40).toInt()))

        // ============ محاسبه ورود، حد ضرر، اهداف ============
        val atrList = TechnicalIndicators.atr(candles, 14)
        val atr = atrList.lastOrNull() ?: (currentPrice * 0.02)

        val slMultiplier = when (timeframe) {
            "1m", "5m", "15m", "30m" -> 1.0
            "1H", "4H" -> 1.5
            else -> 2.0
        }

        val isBuy = signal == SignalType.BUY || signal == SignalType.STRONG_BUY
        val entry = currentPrice
        val stopLoss = if (isBuy) entry - (atr * slMultiplier) else entry + (atr * slMultiplier)
        val risk = abs(entry - stopLoss)

        val target1 = if (isBuy) entry + risk * 1.5 else entry - risk * 1.5
        val target2 = if (isBuy) entry + risk * 2.5 else entry - risk * 2.5
        val target3 = if (isBuy) entry + risk * 4.0 else entry - risk * 4.0
        val rr = if (risk > 0) abs(target2 - entry) / risk else 0.0

        // ============ سطوح (ترکیب تکنیکال + بینایی) ============
        val (techSupports, techResistances) = TechnicalIndicators.findSupportResistance(candles, 50)

        val visionSupports = vision.supportLevels.map { it.price }
        val visionResistances = vision.resistanceLevels.map { it.price }

        val allSupports = (techSupports + visionSupports).distinct().sortedDescending().take(5)
        val allResistances = (techResistances + visionResistances).distinct().sorted().take(5)

        // ============ هشدارها ============
        if (rr < 2.0) warnings.add("Risk to reward ratio below 2 - caution")
        if (trend == TrendType.RANGE) warnings.add("Market in range - signals may be false")

        detectedPatterns.firstOrNull()?.let { p ->
            if (p.direction != 0 && isBuy && p.direction == -1) {
                warnings.add("Pattern ${p.name} is bearish but signal is BUY - caution")
            }
            if (p.direction != 0 && !isBuy && p.direction == 1) {
                warnings.add("Pattern ${p.name} is bullish but signal is SELL - caution")
            }
        }

        return AnalysisResult(
            signal = signal,
            trend = trend,
            confidence = confidence,
            currentPrice = currentPrice,
            entry = entry,
            stopLoss = stopLoss,
            target1 = target1,
            target2 = target2,
            target3 = target3,
            riskReward = rr,
            supportLevels = allSupports,
            resistanceLevels = allResistances,
            reasons = reasons,
            warnings = warnings,
            timeframe = timeframe,
            indicatorScores = scores,
            patterns = detectedPatterns,
            visionTrendLines = vision.trendLines,
            visionDominantTrend = vision.dominantTrend
        )
    }

    private fun analyzeMarketStructure(candles: List<Candle>, reasons: MutableList<String>): Int {
        if (candles.size < 20) return 0
        val recent = candles.takeLast(30)
        val highs = mutableListOf<Double>()
        val lows = mutableListOf<Double>()

        for (i in 2 until recent.size - 2) {
            val isHigh = recent[i].high > recent[i-1].high && recent[i].high > recent[i-2].high &&
                         recent[i].high > recent[i+1].high && recent[i].high > recent[i+2].high
            val isLow = recent[i].low < recent[i-1].low && recent[i].low < recent[i-2].low &&
                        recent[i].low < recent[i+1].low && recent[i].low < recent[i+2].low
            if (isHigh) highs.add(recent[i].high)
            if (isLow) lows.add(recent[i].low)
        }

        var score = 0
        if (highs.size >= 2 && lows.size >= 2) {
            val higherHighs = highs.last() > highs[highs.size - 2]
            val higherLows = lows.last() > lows[lows.size - 2]
            val lowerHighs = highs.last() < highs[highs.size - 2]
            val lowerLows = lows.last() < lows[lows.size - 2]

            score = when {
                higherHighs && higherLows -> {
                    reasons.add("Bullish structure: higher highs and higher lows")
                    80
                }
                lowerHighs && lowerLows -> {
                    reasons.add("Bearish structure: lower highs and lower lows")
                    -80
                }
                else -> {
                    reasons.add("Neutral structure - no clear direction")
                    0
                }
            }
        }
        return score
    }

    private fun analyzePriceAction(
        candles: List<Candle>,
        reasons: MutableList<String>,
        warnings: MutableList<String>
    ): Int {
        if (candles.size < 3) return 0
        val last = candles.last()
        val prev = candles[candles.size - 2]
        val prev2 = candles[candles.size - 3]

        var score = 0

        if (last.lowerWick > last.body * 2 && last.upperWick < last.body * 0.5 && last.isBullish) {
            score += 40
            reasons.add("Hammer pattern - possible bullish reversal")
        }

        if (last.upperWick > last.body * 2 && last.lowerWick < last.body * 0.5 && last.isBearish) {
            score -= 40
            reasons.add("Shooting Star pattern - possible bearish reversal")
        }

        if (prev.isBearish && last.isBullish &&
            last.open <= prev.close && last.close >= prev.open &&
            last.body > prev.body) {
            score += 50
            reasons.add("Bullish Engulfing - strong bullish signal")
        }

        if (prev.isBullish && last.isBearish &&
            last.open >= prev.close && last.close <= prev.open &&
            last.body > prev.body) {
            score -= 50
            reasons.add("Bearish Engulfing - strong bearish signal")
        }

        if (prev2.isBearish && prev.body < prev2.body * 0.5 &&
            last.isBullish && last.close > (prev2.open + prev2.close) / 2) {
            score += 45
            reasons.add("Morning Star pattern - bullish reversal")
        }

        if (prev2.isBullish && prev.body < prev2.body * 0.5 &&
            last.isBearish && last.close < (prev2.open + prev2.close) / 2) {
            score -= 45
            reasons.add("Evening Star pattern - bearish reversal")
        }

        if (last.body < last.range * 0.1) {
            warnings.add("Doji candle - market indecision")
        }

        return score
    }

    private fun getTimeframeWeights(timeframe: String): Map<String, Double> {
        return when (timeframe) {
            "1m", "5m", "15m", "30m" -> mapOf(
                "Market Structure" to 0.8,
                "RSI" to 1.0,
                "Divergence" to 0.8,
                "MACD" to 1.2,
                "EMA" to 1.0,
                "ADX" to 0.8,
                "Stochastic" to 1.5,
                "Bollinger" to 1.2,
                "Price Action" to 1.5,
                "Patterns" to 1.0,
                "Vision" to 0.7
            )
            "1H", "4H" -> mapOf(
                "Market Structure" to 1.5,
                "RSI" to 1.2,
                "Divergence" to 1.3,
                "MACD" to 1.0,
                "EMA" to 1.2,
                "ADX" to 1.0,
                "Stochastic" to 0.8,
                "Bollinger" to 0.8,
                "Price Action" to 1.0,
                "Patterns" to 1.5,
                "Vision" to 1.3
            )
            else -> mapOf(
                "Market Structure" to 2.0,
                "RSI" to 1.0,
                "Divergence" to 1.5,
                "MACD" to 1.2,
                "EMA" to 1.5,
                "ADX" to 1.5,
                "Stochastic" to 0.5,
                "Bollinger" to 0.5,
                "Price Action" to 0.8,
                "Patterns" to 1.8,
                "Vision" to 1.5
            )
        }
    }
}