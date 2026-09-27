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
    val indicatorScores: Map<String, Int>
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
        scores["ساختار بازار"] = trendScore

        // تشخیص روند کلی
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
                rsi < 30 -> { reasons.add("RSI در اشباع فروش (${"%.1f".format(rsi)}) — احتمال برگشت صعودی"); 70 }
                rsi < 40 -> { reasons.add("RSI ضعیف ولی رو به بهبود (${"%.1f".format(rsi)})"); 40 }
                rsi in 40.0..60.0 -> 0
                rsi < 70 -> { reasons.add("RSI نرمال رو به بالا (${"%.1f".format(rsi)})"); -20 }
                else -> { reasons.add("⚠️ RSI در اشباع خرید (${"%.1f".format(rsi)}) — احتمال اصلاح"); -70 }
            }
        }
        scores["RSI"] = rsiScore

        // ============ ۳. واگرایی RSI ============
        val divergence = TechnicalIndicators.detectDivergence(candles, rsiValues, 30)
        val divScore = when (divergence) {
            1 -> { reasons.add("واگرایی صعودی RSI — سیگنال برگشت"); 60 }
            -1 -> { reasons.add("⚠️ واگرایی نزولی RSI — سیگنال برگشت نزولی"); -60 }
            else -> 0
        }
        scores["واگرایی"] = divScore

        // ============ ۴. MACD ============
        val macdResult = TechnicalIndicators.macd(closes)
        val macdLine = macdResult.macdLine.lastOrNull()
        val signalLine = macdResult.signalLine.lastOrNull()
        val histogram = macdResult.histogram.lastOrNull()
        var macdScore = 0
        if (macdLine != null && signalLine != null && histogram != null) {
            macdScore = when {
                macdLine > signalLine && histogram > 0 -> { reasons.add("MACD کراس صعودی — مومنتوم مثبت"); 50 }
                macdLine < signalLine && histogram < 0 -> { reasons.add("⚠️ MACD کراس نزولی — مومنتوم منفی"); -50 }
                else -> 0
            }
            // بررسی قدرت مومنتوم
            val prevHist = macdResult.histogram.dropLast(1).lastOrNull()
            if (prevHist != null && histogram > prevHist && histogram > 0) {
                macdScore += 10
                reasons.add("هیستوگرام MACD در حال افزایش — تقویت روند")
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
                reasons.add("EMA9 بالای EMA21 — روند کوتاه‌مدت صعودی")
            } else {
                emaScore -= 30
                reasons.add("⚠️ EMA9 زیر EMA21 — روند کوتاه‌مدت نزولی")
            }
        }
        if (ema50 != null) {
            if (currentPrice > ema50) {
                emaScore += 20
                reasons.add("قیمت بالای EMA50 — روند میان‌مدت صعودی")
            } else {
                emaScore -= 20
                reasons.add("⚠️ قیمت زیر EMA50 — روند میان‌مدت نزولی")
            }
        }
        scores["EMA"] = emaScore

        // ============ ۶. ADX ============
        val adxList = TechnicalIndicators.adx(candles, 14)
        val adx = adxList.lastOrNull()
        var adxScore = 0
        if (adx != null) {
            when {
                adx > 40 -> { adxScore = 30; reasons.add("ADX قوی (${"%.1f".format(adx)}) — روند قدرتمند") }
                adx > 25 -> { adxScore = 15; reasons.add("ADX متوسط (${"%.1f".format(adx)}) — روند فعال") }
                else -> { warnings.add("ADX ضعیف (${"%.1f".format(adx)}) — بازار رنج") }
            }
            // جهت ADX رو با EMA چک می‌کنیم
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
                k < 20 && k > d -> { stochScore = 40; reasons.add("Stochastic اشباع فروش با کراس صعودی") }
                k > 80 && k < d -> { stochScore = -40; reasons.add("⚠️ Stochastic اشباع خرید با کراس نزولی") }
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
                currentPrice <= lower -> { reasons.add("قیمت روی باند پایین بولینگر — اشباع فروش"); 50 }
                currentPrice >= upper -> { reasons.add("⚠️ قیمت روی باند بالای بولینگر — اشباع خرید"); -50 }
                currentPrice > middle -> 15
                else -> -15
            }
        }
        scores["بولینگر"] = bbScore

        // ============ ۹. تحلیل کندلی ============
        val priceActionScore = analyzePriceAction(candles, reasons, warnings)
        scores["Price Action"] = priceActionScore

        // ============ ۱۰. تایم‌فریم ============
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

        // ============ سطوح ============
        val (supports, resistances) = TechnicalIndicators.findSupportResistance(candles, 50)

        // ============ هشدارها ============
        if (rr < 2.0) warnings.add("نسبت ریسک به ریوارد کمتر از ۲ — احتیاط")
        if (trend == TrendType.RANGE) warnings.add("بازار در حالت رنج — سیگنال‌ها ممکنه کاذب باشن")

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
            supportLevels = supports,
            resistanceLevels = resistances,
            reasons = reasons,
            warnings = warnings,
            timeframe = timeframe,
            indicatorScores = scores
        )
    }

    // ============ تحلیل ساختار بازار ============
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
                higherHighs && higherLows -> { reasons.add("ساختار صعودی: سقف و کف بالاتر"); 80 }
                lowerHighs && lowerLows -> { reasons.add("⚠️ ساختار نزولی: سقف و کف پایین‌تر"); -80 }
                else -> { reasons.add("ساختار خنثی — بازار بدون جهت مشخص"); 0 }
            }
        }
        return score
    }

    // ============ تحلیل Price Action ============
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

        // Hammer در کف
        if (last.lowerWick > last.body * 2 && last.upperWick < last.body * 0.5 && last.isBullish) {
            score += 40
            reasons.add("الگوی Hammer — احتمال برگشت صعودی")
        }

        // Shooting Star در سقف
        if (last.upperWick > last.body * 2 && last.lowerWick < last.body * 0.5 && last.isBearish) {
            score -= 40
            reasons.add("⚠️ الگوی Shooting Star — احتمال برگشت نزولی")
        }

        // Bullish Engulfing
        if (prev.isBearish && last.isBullish &&
            last.open <= prev.close && last.close >= prev.open &&
            last.body > prev.body) {
            score += 50
            reasons.add("الگوی Bullish Engulfing — سیگنال قوی صعودی")
        }

        // Bearish Engulfing
        if (prev.isBullish && last.isBearish &&
            last.open >= prev.close && last.close <= prev.open &&
            last.body > prev.body) {
            score -= 50
            reasons.add("⚠️ الگوی Bearish Engulfing — سیگنال قوی نزولی")
        }

        // Morning Star
        if (prev2.isBearish && prev.body < prev2.body * 0.5 &&
            last.isBullish && last.close > (prev2.open + prev2.close) / 2) {
            score += 45
            reasons.add("الگوی Morning Star — برگشت صعودی")
        }

        // Evening Star
        if (prev2.isBullish && prev.body < prev2.body * 0.5 &&
            last.isBearish && last.close < (prev2.open + prev2.close) / 2) {
            score -= 45
            reasons.add("⚠️ الگوی Evening Star — برگشت نزولی")
        }

        // Doji
        if (last.body < last.range * 0.1) {
            warnings.add("کندل Doji — بلاتکلیفی بازار")
        }

        return score
    }

    // ============ وزن‌دهی بر اساس تایم‌فریم ============
    private fun getTimeframeWeights(timeframe: String): Map<String, Double> {
        return when (timeframe) {
            "1m", "5m", "15m", "30m" -> mapOf(
                // اسکالپ: مومنتوم و Stochastic مهم‌تر
                "ساختار بازار" to 0.8,
                "RSI" to 1.0,
                "واگرایی" to 0.8,
                "MACD" to 1.2,
                "EMA" to 1.0,
                "ADX" to 0.8,
                "Stochastic" to 1.5,
                "بولینگر" to 1.2,
                "Price Action" to 1.5
            )
            "1H", "4H" -> mapOf(
                // سوئینگ: تعادل
                "ساختار بازار" to 1.5,
                "RSI" to 1.2,
                "واگرایی" to 1.3,
                "MACD" to 1.0,
                "EMA" to 1.2,
                "ADX" to 1.0,
                "Stochastic" to 0.8,
                "بولینگر" to 0.8,
                "Price Action" to 1.0
            )
            else -> mapOf(
                // پوزیشن: ساختار و روند مهم‌تر
                "ساختار بازار" to 2.0,
                "RSI" to 1.0,
                "واگرایی" to 1.5,
                "MACD" to 1.2,
                "EMA" to 1.5,
                "ADX" to 1.5,
                "Stochastic" to 0.5,
                "بولینگر" to 0.5,
                "Price Action" to 0.8
            )
        }
    }
}