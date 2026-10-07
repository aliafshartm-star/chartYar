package com.chartyar.app.analysis

import com.chartyar.app.util.RemoteLogger
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
    val visionDominantTrend: Int = 0,
    val strategy: StrategyType = StrategyType.SWING,
    val engineVotes: List<StrategyEngine.EngineVote> = emptyList(),
    val volumeSpike: Boolean = false,
    val vwap: Double? = null,
    val fibonacciLevels: Map<String, Double> = emptyMap()
)

object SignalEngine {

    fun analyze(
        candles: List<Candle>,
        timeframe: String,
        strategy: StrategyType = StrategyType.SWING
    ): AnalysisResult? {
        try {
            if (candles.size < 50) {
                RemoteLogger.logError(
                    tag = "SignalEngine",
                    message = "analyze: not enough candles (${candles.size}) for $timeframe"
                )
                return null
            }

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
                        reasons.add("RSI oversold (${"%.1f".format(rsi)})")
                        70
                    }
                    rsi < 40 -> {
                        reasons.add("RSI weak (${"%.1f".format(rsi)})")
                        40
                    }
                    rsi in 40.0..60.0 -> 0
                    rsi < 70 -> {
                        reasons.add("RSI rising (${"%.1f".format(rsi)})")
                        -20
                    }
                    else -> {
                        reasons.add("RSI overbought (${"%.1f".format(rsi)})")
                        -70
                    }
                }
            }
            scores["RSI"] = rsiScore

            // ============ ۳. واگرایی RSI ============
            val divergence = TechnicalIndicators.detectDivergence(candles, rsiValues, 30)
            val divScore = when (divergence) {
                1 -> {
                    reasons.add("Bullish RSI divergence")
                    60
                }
                -1 -> {
                    reasons.add("Bearish RSI divergence")
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
                        reasons.add("MACD bullish cross")
                        50
                    }
                    macdLine < signalLine && histogram < 0 -> {
                        reasons.add("MACD bearish cross")
                        -50
                    }
                    else -> 0
                }
                val prevHist = macdResult.histogram.dropLast(1).lastOrNull()
                if (prevHist != null && histogram > prevHist && histogram > 0) {
                    macdScore += 10
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
                    reasons.add("EMA9 above EMA21")
                } else {
                    emaScore -= 30
                    reasons.add("EMA9 below EMA21")
                }
            }
            if (ema50 != null) {
                if (currentPrice > ema50) {
                    emaScore += 20
                    reasons.add("Price above EMA50")
                } else {
                    emaScore -= 20
                    reasons.add("Price below EMA50")
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
                        reasons.add("ADX strong (${"%.1f".format(adx)})")
                    }
                    adx > 25 -> {
                        adxScore = 15
                        reasons.add("ADX moderate")
                    }
                    else -> warnings.add("ADX weak - ranging market")
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
                        reasons.add("Stochastic oversold cross")
                    }
                    k > 80 && k < d -> {
                        stochScore = -40
                        reasons.add("Stochastic overbought cross")
                    }
                }
            }
            scores["Stochastic"] = stochScore

            // ============ ۸. Bollinger ============
            val bb = TechnicalIndicators.bollinger(closes, 20, 2.0)
            val upper = bb.upper.lastOrNull()
            val middle = bb.middle.lastOrNull()
            val lower = bb.lower.lastOrNull()
            var bbScore = 0
            if (upper != null && lower != null && middle != null) {
                bbScore = when {
                    currentPrice <= lower -> {
                        reasons.add("Price on lower Bollinger")
                        50
                    }
                    currentPrice >= upper -> {
                        reasons.add("Price on upper Bollinger")
                        -50
                    }
                    currentPrice > middle -> 15
                    else -> -15
                }
            }
            scores["Bollinger"] = bbScore

            // ============ ۹. Price Action ============
            val priceActionScore = analyzePriceAction(candles, reasons, warnings)
            scores["Price Action"] = priceActionScore

            // ============ ۱۰. الگوهای کلاسیک و کندلی ============
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
                reasons.add("Pattern ${bestPattern.name} - $dirText (${bestPattern.confidence}%)")
            }
            scores["Patterns"] = patternScore

            // ============ ۱۱. Vision ============
            val vision = ChartVisionAnalyzer.analyze(candles)
            val visionScore = when (vision.dominantTrend) {
                1 -> 45
                -1 -> -45
                else -> 0
            }
            scores["Vision"] = visionScore
            when (vision.dominantTrend) {
                1 -> reasons.add("Vision: bullish trend lines")
                -1 -> reasons.add("Vision: bearish trend lines")
            }

            // ============ ۱۲. Volume ============
            val volumeAnalysis = TechnicalIndicators.analyzeVolume(candles, 20)
            var volumeScore = 0
            if (volumeAnalysis.isSpike) {
                val priceChange = candles.last().close - candles[candles.size - 2].close
                if (priceChange > 0) {
                    volumeScore = 45
                    reasons.add("Volume spike - bullish")
                } else {
                    volumeScore = -45
                    reasons.add("Volume spike - bearish")
                }
            } else if (volumeAnalysis.trend == 1) {
                volumeScore = 20
                reasons.add("Volume trend rising")
            } else if (volumeAnalysis.trend == -1) {
                volumeScore = -20
            }
            scores["Volume"] = volumeScore

            // ============ ۱۳. VWAP ============
            val vwapList = TechnicalIndicators.vwap(candles)
            val vwap = vwapList.lastOrNull()
            var vwapScore = 0
            if (vwap != null && vwap > 0) {
                if (currentPrice > vwap) {
                    vwapScore = 30
                    reasons.add("Price above VWAP")
                } else {
                    vwapScore = -30
                    reasons.add("Price below VWAP")
                }
            }
            scores["VWAP"] = vwapScore

            // ============ ۱۴. CCI ============
            val cciList = TechnicalIndicators.cci(candles, 20)
            val cci = cciList.lastOrNull()
            var cciScore = 0
            if (cci != null) {
                cciScore = when {
                    cci < -100 -> {
                        reasons.add("CCI oversold")
                        45
                    }
                    cci > 100 -> {
                        reasons.add("CCI overbought")
                        -45
                    }
                    else -> 0
                }
            }
            scores["CCI"] = cciScore

            // ============ ۱۵. Williams %R ============
            val williamsList = TechnicalIndicators.williamsR(candles, 14)
            val williams = williamsList.lastOrNull()
            var williamsScore = 0
            if (williams != null) {
                williamsScore = when {
                    williams < -80 -> {
                        reasons.add("Williams %R oversold")
                        40
                    }
                    williams > -20 -> {
                        reasons.add("Williams %R overbought")
                        -40
                    }
                    else -> 0
                }
            }
            scores["WilliamsR"] = williamsScore

            // ============ ۱۰ اندیکاتور پیشرفته (فقط برای PRO) ============
            var ichimokuScore = 0
            var obvScore = 0
            var mfiScore = 0
            var psarScore = 0
            var aroonScore = 0
            var supertrendScore = 0
            var keltnerScore = 0
            var donchianScore = 0
            var pivotScore = 0
            var elderRayScore = 0

            if (strategy == StrategyType.PRO) {
                val ichimoku = AdvancedIndicators.ichimoku(candles)
                if (ichimoku != null) {
                    val (s, desc) = AdvancedIndicators.scoreIchimoku(ichimoku, currentPrice)
                    ichimokuScore = s
                    if (desc.isNotEmpty()) reasons.add("Ichimoku: $desc")
                }
                scores["Ichimoku"] = ichimokuScore

                val obvList = AdvancedIndicators.obv(candles)
                obvScore = AdvancedIndicators.scoreObv(obvList, 20)
                if (obvScore > 20) reasons.add("OBV rising")
                else if (obvScore < -20) reasons.add("OBV falling")
                scores["OBV"] = obvScore

                val mfiList = AdvancedIndicators.mfi(candles, 14)
                mfiScore = AdvancedIndicators.scoreMfi(mfiList)
                if (mfiScore > 20) reasons.add("MFI oversold")
                else if (mfiScore < -20) reasons.add("MFI overbought")
                scores["MFI"] = mfiScore

                val psarList = AdvancedIndicators.psar(candles)
                psarScore = AdvancedIndicators.scorePsar(psarList, candles)
                if (psarScore > 40) reasons.add("PSAR flip bullish")
                else if (psarScore < -40) reasons.add("PSAR flip bearish")
                scores["PSAR"] = psarScore

                val aroon = AdvancedIndicators.aroon(candles, 25)
                if (aroon != null) {
                    aroonScore = AdvancedIndicators.scoreAroon(aroon)
                    if (aroonScore > 30) reasons.add("Aroon strong uptrend")
                    else if (aroonScore < -30) reasons.add("Aroon strong downtrend")
                }
                scores["Aroon"] = aroonScore

                val supertrend = AdvancedIndicators.supertrend(candles, 10, 3.0)
                if (supertrend != null) {
                    supertrendScore = AdvancedIndicators.scoreSupertrend(supertrend)
                    if (supertrend.trend == 1) reasons.add("Supertrend bullish")
                    else reasons.add("Supertrend bearish")
                }
                scores["Supertrend"] = supertrendScore

                val keltner = AdvancedIndicators.keltner(candles, 20, 2.0)
                keltnerScore = AdvancedIndicators.scoreKeltner(keltner, currentPrice)
                scores["Keltner"] = keltnerScore

                val donchian = AdvancedIndicators.donchian(candles, 20)
                donchianScore = AdvancedIndicators.scoreDonchian(donchian, currentPrice)
                if (donchianScore > 30) reasons.add("Donchian breakout up")
                else if (donchianScore < -30) reasons.add("Donchian breakout down")
                scores["Donchian"] = donchianScore

                val pivots = AdvancedIndicators.pivotPoints(candles, 24)
                if (pivots != null) {
                    val (levelName, levelPrice) = AdvancedIndicators.nearestPivotLevel(pivots, currentPrice)
                    val diff = ((currentPrice - levelPrice) / levelPrice) * 100.0
                    pivotScore = when {
                        levelName.startsWith("S") && diff > -1.0 -> 30
                        levelName.startsWith("R") && diff < 1.0 -> -30
                        else -> 0
                    }
                    if (pivotScore != 0) {
                        reasons.add("Near pivot $levelName (${"%.2f".format(levelPrice)})")
                    }
                }
                scores["PivotPoints"] = pivotScore

                val elderRay = AdvancedIndicators.elderRay(candles, 13)
                elderRayScore = AdvancedIndicators.scoreElderRay(elderRay)
                scores["ElderRay"] = elderRayScore
            }

            // ============ وزن‌دهی بر اساس استراتژی ============
            val weights = StrategyEngine.getWeights(strategy, timeframe)

            var totalScore = 0.0
            var totalWeight = 0.0
            scores.forEach { (name, score) ->
                val w = weights[name] ?: 1.0
                totalScore += score * w
                totalWeight += w
            }
            val normalizedScore = if (totalWeight > 0) totalScore / totalWeight else 0.0

            // ============ سیگنال پایه ============
            var signal = when {
                normalizedScore >= 45 -> SignalType.STRONG_BUY
                normalizedScore >= 20 -> SignalType.BUY
                normalizedScore <= -45 -> SignalType.STRONG_SELL
                normalizedScore <= -20 -> SignalType.SELL
                else -> SignalType.NEUTRAL
            }

            // ============ رأی‌گیری Multi-AI ============
            var engineVotes: List<StrategyEngine.EngineVote> = emptyList()

            if (strategy == StrategyType.MULTI_AI) {
                val trendEngineScore = (
                    scores["Market Structure"]!! * 1.5 +
                    scores["EMA"]!! * 1.2 +
                    scores["ADX"]!! * 1.3
                ).toInt() / 4

                val momentumEngineScore = (
                    scores["RSI"]!! * 1.2 +
                    scores["MACD"]!! * 1.2 +
                    scores["Stochastic"]!! * 1.0 +
                    scores["CCI"]!! * 0.8 +
                    scores["WilliamsR"]!! * 0.8
                ).toInt() / 5

                val reversalEngineScore = (
                    scores["Bollinger"]!! * 1.0 +
                    scores["Divergence"]!! * 1.3 +
                    scores["Price Action"]!! * 1.2 +
                    scores["Patterns"]!! * 1.0
                ).toInt() / 4

                val volumeEngineScore = (
                    scores["Volume"]!! * 1.5 +
                    scores["VWAP"]!! * 1.2
                ).toInt() / 3

                val volatilityEngineScore = (
                    scores["Bollinger"]!! * 1.0 +
                    scores["Patterns"]!! * 1.2 +
                    scores["Price Action"]!! * 1.0
                ).toInt() / 3

                val multiAiResult = StrategyEngine.evaluateMultiAi(
                    structureScore = trendEngineScore,
                    momentumScore = momentumEngineScore,
                    reversalScore = reversalEngineScore,
                    volumeScore = volumeEngineScore,
                    volatilityScore = volatilityEngineScore
                )

                engineVotes = multiAiResult.votes

                if (multiAiResult.consensus >= 4) {
                    signal = multiAiResult.finalSignal
                    reasons.add("Multi-AI consensus: ${multiAiResult.consensus}/5 engines")
                } else {
                    signal = SignalType.NEUTRAL
                    warnings.add("Multi-AI: no consensus (${multiAiResult.consensus}/5)")
                }
            }

            val confidence = min(95, max(30, (abs(normalizedScore) + 40).toInt()))

            // ============ محاسبه ورود/حد ضرر/اهداف ============
            val atrList = TechnicalIndicators.atr(candles, 14)
            val atr = atrList.lastOrNull() ?: (currentPrice * 0.02)

            val slMultiplier = when (strategy) {
                StrategyType.SCALPING -> 1.0
                StrategyType.SWING -> 1.5
                StrategyType.POSITION -> 2.0
                StrategyType.MULTI_AI -> 1.5
                StrategyType.PRO -> 1.3
            }

            val isBuy = signal == SignalType.BUY || signal == SignalType.STRONG_BUY
            val entry = currentPrice
            val stopLoss = if (isBuy) entry - (atr * slMultiplier) else entry + (atr * slMultiplier)
            val risk = abs(entry - stopLoss)

            val target1 = if (isBuy) entry + risk * 1.5 else entry - risk * 1.5
            val target2 = if (isBuy) entry + risk * 2.5 else entry - risk * 2.5
            val target3 = if (isBuy) entry + risk * 4.0 else entry - risk * 4.0
            val rr = if (risk > 0) abs(target2 - entry) / risk else 0.0

            // ============ سطوح حمایت/مقاومت ============
            val (techSupports, techResistances) = TechnicalIndicators.findSupportResistance(candles, 50)
            val visionSupports = vision.supportLevels.map { it.price }
            val visionResistances = vision.resistanceLevels.map { it.price }
            val allSupports = (techSupports + visionSupports).distinct().sortedDescending().take(5)
            val allResistances = (techResistances + visionResistances).distinct().sorted().take(5)

            // ============ فیبوناچی ============
            val fibonacci = TechnicalIndicators.fibonacciLevels(candles, 50)

            // ============ هشدارها ============
            if (rr < 2.0) warnings.add("Risk/reward below 2")
            if (trend == TrendType.RANGE) warnings.add("Ranging market - signals may fail")

            detectedPatterns.firstOrNull()?.let { p ->
                if (p.direction != 0 && isBuy && p.direction == -1) {
                    warnings.add("Pattern ${p.name} is bearish but signal is BUY")
                }
                if (p.direction != 0 && !isBuy && p.direction == 1) {
                    warnings.add("Pattern ${p.name} is bullish but signal is SELL")
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
                visionDominantTrend = vision.dominantTrend,
                strategy = strategy,
                engineVotes = engineVotes,
                volumeSpike = volumeAnalysis.isSpike,
                vwap = vwap,
                fibonacciLevels = fibonacci
            )
        } catch (e: Exception) {
            RemoteLogger.logError(
                tag = "SignalEngine",
                message = "analyze $timeframe ($strategy): ${e.message ?: "unknown"}",
                throwable = e
            )
            return null
        }
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
                    reasons.add("Bullish structure HH+HL")
                    80
                }
                lowerHighs && lowerLows -> {
                    reasons.add("Bearish structure LH+LL")
                    -80
                }
                else -> 0
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
            reasons.add("Hammer pattern")
        }

        if (last.upperWick > last.body * 2 && last.lowerWick < last.body * 0.5 && last.isBearish) {
            score -= 40
            reasons.add("Shooting Star pattern")
        }

        if (prev.isBearish && last.isBullish &&
            last.open <= prev.close && last.close >= prev.open && last.body > prev.body) {
            score += 50
            reasons.add("Bullish Engulfing")
        }

        if (prev.isBullish && last.isBearish &&
            last.open >= prev.close && last.close <= prev.open && last.body > prev.body) {
            score -= 50
            reasons.add("Bearish Engulfing")
        }

        if (prev2.isBearish && prev.body < prev2.body * 0.5 &&
            last.isBullish && last.close > (prev2.open + prev2.close) / 2) {
            score += 45
            reasons.add("Morning Star")
        }

        if (prev2.isBullish && prev.body < prev2.body * 0.5 &&
            last.isBearish && last.close < (prev2.open + prev2.close) / 2) {
            score -= 45
            reasons.add("Evening Star")
        }

        if (last.body < last.range * 0.1) {
            warnings.add("Doji candle - indecision")
        }

        return score
    }
}