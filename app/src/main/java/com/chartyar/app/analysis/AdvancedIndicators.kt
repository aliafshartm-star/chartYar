package com.chartyar.app.analysis

import kotlin.math.max
import kotlin.math.min

/**
 * ۱۰ اندیکاتور پیشرفته برای استراتژی PRO
 */
object AdvancedIndicators {

    // ============================================================
    // ۱. Ichimoku Cloud
    // ============================================================
    data class Ichimoku(
        val tenkan: Double,
        val kijun: Double,
        val senkouA: Double,
        val senkouB: Double,
        val chikou: Double
    )

    fun ichimoku(
        candles: List<Candle>,
        tenkanPeriod: Int = 9,
        kijunPeriod: Int = 26,
        senkouPeriod: Int = 52
    ): Ichimoku? {
        if (candles.size < senkouPeriod) return null

        fun highLow(period: Int): Pair<Double, Double>? {
            val end = candles.size
            val start = end - period
            if (start < 0) return null
            var high = Double.MIN_VALUE
            var low = Double.MAX_VALUE
            for (i in start until end) {
                high = max(high, candles[i].high)
                low = min(low, candles[i].low)
            }
            return high to low
        }

        val tenkanHL = highLow(tenkanPeriod) ?: return null
        val kijunHL = highLow(kijunPeriod) ?: return null
        val senkouBHL = highLow(senkouPeriod) ?: return null

        val tenkan = (tenkanHL.first + tenkanHL.second) / 2.0
        val kijun = (kijunHL.first + kijunHL.second) / 2.0
        val senkouA = (tenkan + kijun) / 2.0
        val senkouB = (senkouBHL.first + senkouBHL.second) / 2.0
        val chikou = candles.last().close

        return Ichimoku(tenkan, kijun, senkouA, senkouB, chikou)
    }

    // ============================================================
    // ۲. OBV — On-Balance Volume
    // ============================================================
    fun obv(candles: List<Candle>): List<Double> {
        if (candles.size < 2) return emptyList()
        val result = mutableListOf(0.0)
        var running = 0.0
        for (i in 1 until candles.size) {
            val prev = candles[i - 1].close
            val curr = candles[i].close
            val vol = candles[i].volume
            running += when {
                curr > prev -> vol
                curr < prev -> -vol
                else -> 0.0
            }
            result.add(running)
        }
        return result
    }

    // ============================================================
    // ۳. MFI — Money Flow Index
    // ============================================================
    fun mfi(candles: List<Candle>, period: Int = 14): List<Double> {
        if (candles.size < period + 1) return emptyList()
        val result = mutableListOf<Double>()
        val typicalPrices = candles.map { (it.high + it.low + it.close) / 3.0 }
        val moneyFlows = candles.mapIndexed { i, c -> typicalPrices[i] * c.volume }

        for (i in period until candles.size) {
            var posFlow = 0.0
            var negFlow = 0.0
            for (j in (i - period + 1)..i) {
                if (typicalPrices[j] > typicalPrices[j - 1]) {
                    posFlow += moneyFlows[j]
                } else if (typicalPrices[j] < typicalPrices[j - 1]) {
                    negFlow += moneyFlows[j]
                }
            }
            val mfiValue = if (negFlow == 0.0) 100.0
            else 100.0 - (100.0 / (1.0 + posFlow / negFlow))
            result.add(mfiValue)
        }
        return result
    }

    // ============================================================
    // ۴. PSAR — Parabolic SAR
    // ============================================================
    fun psar(
        candles: List<Candle>,
        step: Double = 0.02,
        maxStep: Double = 0.2
    ): List<Double> {
        if (candles.size < 2) return emptyList()
        val result = mutableListOf<Double>()
        var isUpTrend = true
        var sar = candles[0].low
        var ep = candles[0].high
        var af = step

        for (i in 1 until candles.size) {
            val prev = candles[i - 1]
            val curr = candles[i]

            var newSar = sar + af * (ep - sar)

            if (isUpTrend) {
                if (curr.low < newSar) {
                    isUpTrend = false
                    newSar = ep
                    ep = curr.low
                    af = step
                } else {
                    if (curr.high > ep) {
                        ep = curr.high
                        af = min(af + step, maxStep)
                    }
                    newSar = min(newSar, prev.low)
                    val idx = max(0, i - 2)
                    newSar = min(newSar, candles[idx].low)
                }
            } else {
                if (curr.high > newSar) {
                    isUpTrend = true
                    newSar = ep
                    ep = curr.high
                    af = step
                } else {
                    if (curr.low < ep) {
                        ep = curr.low
                        af = min(af + step, maxStep)
                    }
                    newSar = max(newSar, prev.high)
                    val idx = max(0, i - 2)
                    newSar = max(newSar, candles[idx].high)
                }
            }

            sar = newSar
            result.add(sar)
        }
        return result
    }

    // ============================================================
    // ۵. Aroon
    // ============================================================
    data class Aroon(val up: Double, val down: Double)

    fun aroon(candles: List<Candle>, period: Int = 25): Aroon? {
        if (candles.size < period + 1) return null
        val recent = candles.takeLast(period + 1)
        var highestIndex = 0
        var lowestIndex = 0
        var highest = Double.MIN_VALUE
        var lowest = Double.MAX_VALUE

        recent.forEachIndexed { i, c ->
            if (c.high > highest) {
                highest = c.high
                highestIndex = i
            }
            if (c.low < lowest) {
                lowest = c.low
                lowestIndex = i
            }
        }

        val up = ((period - (period - highestIndex).toDouble()) / period) * 100.0
        val down = ((period - (period - lowestIndex).toDouble()) / period) * 100.0

        return Aroon(up, down)
    }

    // ============================================================
    // ۶. Supertrend
    // ============================================================
    data class Supertrend(val trend: Int, val value: Double)

    fun supertrend(
        candles: List<Candle>,
        period: Int = 10,
        multiplier: Double = 3.0
    ): Supertrend? {
        if (candles.size < period + 1) return null

        val atrList = TechnicalIndicators.atr(candles, period)
        if (atrList.isEmpty()) return null

        var upperBand = 0.0
        var lowerBand = 0.0
        var trend = 1
        var supertrendValue = 0.0

        for (i in candles.indices) {
            if (i < period) continue
            val atr = atrList.getOrNull(i - period + 1) ?: continue
            val hl2 = (candles[i].high + candles[i].low) / 2.0
            val basicUpper = hl2 + multiplier * atr
            val basicLower = hl2 - multiplier * atr

            if (i == period) {
                upperBand = basicUpper
                lowerBand = basicLower
            } else {
                upperBand = if (basicUpper < upperBand || candles[i - 1].close > upperBand)
                    basicUpper else upperBand
                lowerBand = if (basicLower > lowerBand || candles[i - 1].close < lowerBand)
                    basicLower else lowerBand
            }

            trend = when {
                candles[i].close > upperBand -> 1
                candles[i].close < lowerBand -> -1
                else -> trend
            }

            supertrendValue = if (trend == 1) lowerBand else upperBand
        }

        return Supertrend(trend, supertrendValue)
    }

    // ============================================================
    // ۷. Keltner Channels
    // ============================================================
    data class Keltner(
        val upper: List<Double>,
        val middle: List<Double>,
        val lower: List<Double>
    )

    fun keltner(
        candles: List<Candle>,
        period: Int = 20,
        multiplier: Double = 2.0
    ): Keltner {
        val closes = candles.map { it.close }
        val ema = TechnicalIndicators.ema(closes, period)
        val atr = TechnicalIndicators.atr(candles, period)

        val upper = mutableListOf<Double>()
        val middle = mutableListOf<Double>()
        val lower = mutableListOf<Double>()

        for (i in ema.indices) {
            val e = ema[i]
            if (e == null) continue
            val a = atr.getOrNull(i) ?: 0.0
            middle.add(e)
            upper.add(e + multiplier * a)
            lower.add(e - multiplier * a)
        }

        return Keltner(upper, middle, lower)
    }

    // ============================================================
    // ۸. Donchian Channels
    // ============================================================
    data class Donchian(
        val upper: List<Double>,
        val middle: List<Double>,
        val lower: List<Double>
    )

    fun donchian(candles: List<Candle>, period: Int = 20): Donchian {
        if (candles.size < period) return Donchian(emptyList(), emptyList(), emptyList())
        val upper = mutableListOf<Double>()
        val middle = mutableListOf<Double>()
        val lower = mutableListOf<Double>()

        for (i in (period - 1) until candles.size) {
            var hi = Double.MIN_VALUE
            var lo = Double.MAX_VALUE
            for (j in (i - period + 1)..i) {
                hi = max(hi, candles[j].high)
                lo = min(lo, candles[j].low)
            }
            upper.add(hi)
            lower.add(lo)
            middle.add((hi + lo) / 2.0)
        }
        return Donchian(upper, middle, lower)
    }

    // ============================================================
    // ۹. Pivot Points (Classic)
    // ============================================================
    data class PivotPoints(
        val pp: Double,
        val r1: Double,
        val r2: Double,
        val r3: Double,
        val s1: Double,
        val s2: Double,
        val s3: Double
    )

    fun pivotPoints(candles: List<Candle>, lookback: Int = 24): PivotPoints? {
        if (candles.size < lookback) return null
        val recent = candles.takeLast(lookback)
        val high = recent.maxOf { it.high }
        val low = recent.minOf { it.low }
        val close = recent.last().close

        val pp = (high + low + close) / 3.0
        val r1 = 2 * pp - low
        val s1 = 2 * pp - high
        val r2 = pp + (high - low)
        val s2 = pp - (high - low)
        val r3 = high + 2 * (pp - low)
        val s3 = low - 2 * (high - pp)

        return PivotPoints(pp, r1, r2, r3, s1, s2, s3)
    }

    // ============================================================
    // ۱۰. Elder Ray (Bull/Bear Power)
    // ============================================================
    data class ElderRay(val bullPower: List<Double>, val bearPower: List<Double>)

    fun elderRay(candles: List<Candle>, period: Int = 13): ElderRay {
        val closes = candles.map { it.close }
        val ema = TechnicalIndicators.ema(closes, period)
        val bullPower = mutableListOf<Double>()
        val bearPower = mutableListOf<Double>()

        for (i in ema.indices) {
            val e = ema[i] ?: continue
            bullPower.add(candles[i].high - e)
            bearPower.add(candles[i].low - e)
        }

        return ElderRay(bullPower, bearPower)
    }

    // ============================================================
    // امتیازدهی برای استراتژی PRO
    // ============================================================

    fun scoreIchimoku(ichimoku: Ichimoku, currentPrice: Double): Pair<Int, String> {
        var score = 0
        val reasons = mutableListOf<String>()

        if (currentPrice > ichimoku.senkouA && currentPrice > ichimoku.senkouB) {
            score += 30
            reasons.add("above cloud")
        } else if (currentPrice < ichimoku.senkouA && currentPrice < ichimoku.senkouB) {
            score -= 30
            reasons.add("below cloud")
        }

        if (ichimoku.tenkan > ichimoku.kijun) {
            score += 20
            reasons.add("tenkan>kijun")
        } else {
            score -= 20
            reasons.add("tenkan<kijun")
        }

        return score to reasons.joinToString(", ")
    }

    fun scoreObv(obvList: List<Double>, period: Int = 20): Int {
        if (obvList.size < period) return 0
        val recent = obvList.takeLast(period)
        val first = recent.first()
        val last = recent.last()
        val change = last - first
        val range = (recent.maxOrNull() ?: 0.0) - (recent.minOrNull() ?: 0.0)
        if (range == 0.0) return 0
        val normalized = (change / range) * 100.0
        return when {
            normalized > 30 -> 40
            normalized > 10 -> 20
            normalized < -30 -> -40
            normalized < -10 -> -20
            else -> 0
        }
    }

    fun scoreMfi(mfiList: List<Double>): Int {
        val mfi = mfiList.lastOrNull() ?: return 0
        return when {
            mfi < 20 -> 50
            mfi < 40 -> 25
            mfi > 80 -> -50
            mfi > 60 -> -25
            else -> 0
        }
    }

    fun scorePsar(psarList: List<Double>, candles: List<Candle>): Int {
        if (psarList.isEmpty() || candles.size < psarList.size) return 0
        val lastPsar = psarList.last()
        val lastClose = candles.last().close
        val prevPsar = psarList.getOrNull(psarList.size - 2) ?: return 0
        val prevClose = candles[candles.size - 2].close

        val wasAbove = prevClose > prevPsar
        val isAbove = lastClose > lastPsar

        return when {
            wasAbove && isAbove -> 25
            !wasAbove && !isAbove -> -25
            wasAbove && !isAbove -> -60
            !wasAbove && isAbove -> 60
            else -> 0
        }
    }

    fun scoreAroon(aroon: Aroon): Int {
        return when {
            aroon.up > 70 && aroon.down < 30 -> 45
            aroon.down > 70 && aroon.up < 30 -> -45
            aroon.up > aroon.down -> 15
            aroon.down > aroon.up -> -15
            else -> 0
        }
    }

    fun scoreSupertrend(st: Supertrend): Int {
        return if (st.trend == 1) 50 else -50
    }

    fun scoreKeltner(k: Keltner, currentPrice: Double): Int {
        if (k.upper.isEmpty() || k.lower.isEmpty() || k.middle.isEmpty()) return 0
        val upper = k.upper.last()
        val lower = k.lower.last()
        val middle = k.middle.last()
        return when {
            currentPrice > upper -> -30
            currentPrice < lower -> 30
            currentPrice > middle -> 15
            else -> -15
        }
    }

    fun scoreDonchian(d: Donchian, currentPrice: Double): Int {
        if (d.upper.isEmpty() || d.lower.isEmpty()) return 0
        val upper = d.upper.last()
        val lower = d.lower.last()
        val range = upper - lower
        if (range <= 0) return 0
        val pos = (currentPrice - lower) / range
        return when {
            pos > 0.9 -> 35
            pos < 0.1 -> -35
            pos > 0.6 -> 15
            pos < 0.4 -> -15
            else -> 0
        }
    }

    fun scoreElderRay(er: ElderRay): Int {
        val bull = er.bullPower.lastOrNull() ?: return 0
        val bear = er.bearPower.lastOrNull() ?: return 0
        return when {
            bull > 0 && bear > 0 -> 30
            bull < 0 && bear < 0 -> -30
            bull > 0 && bear < 0 -> 10
            else -> -10
        }
    }

    fun nearestPivotLevel(pp: PivotPoints, price: Double): Pair<String, Double> {
        val levels = listOf(
            "R3" to pp.r3,
            "R2" to pp.r2,
            "R1" to pp.r1,
            "PP" to pp.pp,
            "S1" to pp.s1,
            "S2" to pp.s2,
            "S3" to pp.s3
        )
        val nearest = levels.minByOrNull { kotlin.math.abs(it.second - price) } ?: ("PP" to pp.pp)
        return nearest
    }

    fun weightedScore(entries: Map<String, Pair<Int, Double>>): Int {
        var total = 0.0
        var weightSum = 0.0
        entries.forEach { (_, pair) ->
            total += pair.first * pair.second
            weightSum += pair.second
        }
        return if (weightSum > 0) (total / weightSum).toInt() else 0
    }
}