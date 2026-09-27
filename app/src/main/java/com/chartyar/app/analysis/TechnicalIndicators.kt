package com.chartyar.app.analysis

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object TechnicalIndicators {

    // ============ میانگین متحرک ساده (SMA) ============
    fun sma(values: List<Double>, period: Int): List<Double?> {
        if (values.size < period) return List(values.size) { null }
        val result = MutableList<Double?>(values.size) { null }
        var sum = 0.0
        for (i in values.indices) {
            sum += values[i]
            if (i >= period) sum -= values[i - period]
            if (i >= period - 1) result[i] = sum / period
        }
        return result
    }

    // ============ میانگین متحرک نمایی (EMA) ============
    fun ema(values: List<Double>, period: Int): List<Double?> {
        if (values.size < period) return List(values.size) { null }
        val result = MutableList<Double?>(values.size) { null }
        val multiplier = 2.0 / (period + 1)

        var sum = 0.0
        for (i in 0 until period) sum += values[i]
        result[period - 1] = sum / period

        for (i in period until values.size) {
            val prev = result[i - 1] ?: values[i]
            result[i] = (values[i] - prev) * multiplier + prev
        }
        return result
    }

    // ============ RSI ============
    fun rsi(closes: List<Double>, period: Int = 14): List<Double?> {
        if (closes.size < period + 1) return List(closes.size) { null }
        val result = MutableList<Double?>(closes.size) { null }

        var gainSum = 0.0
        var lossSum = 0.0
        for (i in 1..period) {
            val diff = closes[i] - closes[i - 1]
            if (diff >= 0) gainSum += diff else lossSum -= diff
        }
        var avgGain = gainSum / period
        var avgLoss = lossSum / period

        result[period] = if (avgLoss == 0.0) 100.0
        else 100.0 - (100.0 / (1.0 + avgGain / avgLoss))

        for (i in period + 1 until closes.size) {
            val diff = closes[i] - closes[i - 1]
            val gain = if (diff > 0) diff else 0.0
            val loss = if (diff < 0) -diff else 0.0
            avgGain = (avgGain * (period - 1) + gain) / period
            avgLoss = (avgLoss * (period - 1) + loss) / period
            result[i] = if (avgLoss == 0.0) 100.0
            else 100.0 - (100.0 / (1.0 + avgGain / avgLoss))
        }
        return result
    }

    // ============ MACD ============
    data class MacdResult(
        val macdLine: List<Double?>,
        val signalLine: List<Double?>,
        val histogram: List<Double?>
    )

    fun macd(
        closes: List<Double>,
        fastPeriod: Int = 12,
        slowPeriod: Int = 26,
        signalPeriod: Int = 9
    ): MacdResult {
        val fastEma = ema(closes, fastPeriod)
        val slowEma = ema(closes, slowPeriod)

        val macdLine = List(closes.size) { i ->
            val f = fastEma[i]; val s = slowEma[i]
            if (f != null && s != null) f - s else null
        }

        val validMacd = macdLine.filterNotNull()
        val signalRaw = ema(validMacd, signalPeriod)

        val signalLine = MutableList<Double?>(closes.size) { null }
        var idx = 0
        for (i in macdLine.indices) {
            if (macdLine[i] != null) {
                if (idx < signalRaw.size) signalLine[i] = signalRaw[idx]
                idx++
            }
        }

        val histogram = List(closes.size) { i ->
            val m = macdLine[i]; val s = signalLine[i]
            if (m != null && s != null) m - s else null
        }

        return MacdResult(macdLine, signalLine, histogram)
    }

    // ============ ATR ============
    fun atr(candles: List<Candle>, period: Int = 14): List<Double?> {
        if (candles.size < period + 1) return List(candles.size) { null }
        val trs = MutableList(candles.size) { 0.0 }
        trs[0] = candles[0].range
        for (i in 1 until candles.size) {
            val h = candles[i].high
            val l = candles[i].low
            val prevClose = candles[i - 1].close
            trs[i] = max(h - l, max(abs(h - prevClose), abs(l - prevClose)))
        }
        return sma(trs, period)
    }

    // ============ Bollinger Bands ============
    data class BollingerResult(
        val upper: List<Double?>,
        val middle: List<Double?>,
        val lower: List<Double?>
    )

    fun bollinger(closes: List<Double>, period: Int = 20, multiplier: Double = 2.0): BollingerResult {
        val middle = sma(closes, period)
        val upper = MutableList<Double?>(closes.size) { null }
        val lower = MutableList<Double?>(closes.size) { null }

        for (i in closes.indices) {
            val mid = middle[i] ?: continue
            if (i < period - 1) continue
            var sumSq = 0.0
            for (j in (i - period + 1)..i) {
                sumSq += (closes[j] - mid) * (closes[j] - mid)
            }
            val stdDev = sqrt(sumSq / period)
            upper[i] = mid + multiplier * stdDev
            lower[i] = mid - multiplier * stdDev
        }
        return BollingerResult(upper, middle, lower)
    }

    // ============ Stochastic ============
    data class StochResult(val k: List<Double?>, val d: List<Double?>)

    fun stochastic(candles: List<Candle>, kPeriod: Int = 14, dPeriod: Int = 3): StochResult {
        val k = MutableList<Double?>(candles.size) { null }
        for (i in candles.indices) {
            if (i < kPeriod - 1) continue
            var highest = candles[i].high
            var lowest = candles[i].low
            for (j in (i - kPeriod + 1)..i) {
                highest = max(highest, candles[j].high)
                lowest = min(lowest, candles[j].low)
            }
            val range = highest - lowest
            k[i] = if (range == 0.0) 50.0
            else ((candles[i].close - lowest) / range) * 100.0
        }
        val validK = k.filterNotNull()
        val dRaw = sma(validK, dPeriod)
        val d = MutableList<Double?>(candles.size) { null }
        var idx = 0
        for (i in k.indices) {
            if (k[i] != null) {
                if (idx < dRaw.size) d[i] = dRaw[idx]
                idx++
            }
        }
        return StochResult(k, d)
    }

    // ============ Williams %R ============
    fun williamsR(candles: List<Candle>, period: Int = 14): List<Double?> {
        val result = MutableList<Double?>(candles.size) { null }
        for (i in candles.indices) {
            if (i < period - 1) continue
            var highest = candles[i].high
            var lowest = candles[i].low
            for (j in (i - period + 1)..i) {
                highest = max(highest, candles[j].high)
                lowest = min(lowest, candles[j].low)
            }
            val range = highest - lowest
            result[i] = if (range == 0.0) -50.0
            else ((highest - candles[i].close) / range) * -100.0
        }
        return result
    }

    // ============ ADX (ساده‌شده) ============
    fun adx(candles: List<Candle>, period: Int = 14): List<Double?> {
        if (candles.size < period * 2) return List(candles.size) { null }
        val plusDM = MutableList(candles.size) { 0.0 }
        val minusDM = MutableList(candles.size) { 0.0 }
        val tr = MutableList(candles.size) { 0.0 }

        for (i in 1 until candles.size) {
            val upMove = candles[i].high - candles[i - 1].high
            val downMove = candles[i - 1].low - candles[i].low
            plusDM[i] = if (upMove > downMove && upMove > 0) upMove else 0.0
            minusDM[i] = if (downMove > upMove && downMove > 0) downMove else 0.0
            val h = candles[i].high; val l = candles[i].low
            val pc = candles[i - 1].close
            tr[i] = max(h - l, max(abs(h - pc), abs(l - pc)))
        }

        val atrList = sma(tr, period)
        val plusDI = MutableList<Double?>(candles.size) { null }
        val minusDI = MutableList<Double?>(candles.size) { null }
        val dx = MutableList<Double?>(candles.size) { null }

        val smoothPlus = sma(plusDM, period)
        val smoothMinus = sma(minusDM, period)

        for (i in candles.indices) {
            val a = atrList[i] ?: continue
            val sp = smoothPlus[i] ?: continue
            val sm = smoothMinus[i] ?: continue
            if (a == 0.0) continue
            val pdi = 100.0 * sp / a
            val mdi = 100.0 * sm / a
            plusDI[i] = pdi
            minusDI[i] = mdi
            val sum = pdi + mdi
            dx[i] = if (sum == 0.0) 0.0 else 100.0 * abs(pdi - mdi) / sum
        }

        val validDx = dx.filterNotNull()
        val adxRaw = sma(validDx, period)
        val adx = MutableList<Double?>(candles.size) { null }
        var idx = 0
        for (i in dx.indices) {
            if (dx[i] != null) {
                if (idx < adxRaw.size) adx[i] = adxRaw[idx]
                idx++
            }
        }
        return adx
    }

    // ============ تشخیص واگرایی ============
    /**
     * واگرایی صعودی: قیمت کف پایین‌تر، RSI کف بالاتر
     * واگرایی نزولی: قیمت سقف بالاتر، RSI سقف پایین‌تر
     * @return 1 = واگرایی صعودی، -1 = نزولی، 0 = هیچ
     */
    fun detectDivergence(candles: List<Candle>, indicator: List<Double?>, lookback: Int = 30): Int {
        if (candles.size < lookback || indicator.size < lookback) return 0
        val start = candles.size - lookback
        val closes = candles.map { it.close }

        val recentLows = mutableListOf<Pair<Int, Double>>()
        val recentHighs = mutableListOf<Pair<Int, Double>>()

        for (i in start + 2 until candles.size - 2) {
            val isLow = closes[i] < closes[i-1] && closes[i] < closes[i-2] &&
                        closes[i] < closes[i+1] && closes[i] < closes[i+2]
            val isHigh = closes[i] > closes[i-1] && closes[i] > closes[i-2] &&
                         closes[i] > closes[i+1] && closes[i] > closes[i+2]
            if (isLow) recentLows.add(i to closes[i])
            if (isHigh) recentHighs.add(i to closes[i])
        }

        // واگرایی صعودی
        if (recentLows.size >= 2) {
            val (i1, p1) = recentLows[recentLows.size - 2]
            val (i2, p2) = recentLows[recentLows.size - 1]
            val r1 = indicator[i1]; val r2 = indicator[i2]
            if (r1 != null && r2 != null && p2 < p1 && r2 > r1) return 1
        }

        // واگرایی نزولی
        if (recentHighs.size >= 2) {
            val (i1, p1) = recentHighs[recentHighs.size - 2]
            val (i2, p2) = recentHighs[recentHighs.size - 1]
            val r1 = indicator[i1]; val r2 = indicator[i2]
            if (r1 != null && r2 != null && p2 > p1 && r2 < r1) return -1
        }

        return 0
    }

    // ============ تشخیص سطوح حمایت و مقاومت ============
    fun findSupportResistance(candles: List<Candle>, lookback: Int = 50): Pair<List<Double>, List<Double>> {
        if (candles.isEmpty()) return emptyList<Double>() to emptyList<Double>()
        val recent = candles.takeLast(lookback)
        val highs = recent.map { it.high }.sortedDescending()
        val lows = recent.map { it.low }.sorted()

        val resistance = highs.take(3).distinct()
        val support = lows.take(3).distinct()
        return support to resistance
    }

    // ============ Fibonacci Retracement ============
    fun fibonacciLevels(candles: List<Candle>, lookback: Int = 50): Map<String, Double> {
        val recent = candles.takeLast(lookback)
        if (recent.isEmpty()) return emptyMap()
        val high = recent.maxOf { it.high }
        val low = recent.minOf { it.low }
        val diff = high - low
        return mapOf(
            "0.0" to high,
            "0.236" to high - diff * 0.236,
            "0.382" to high - diff * 0.382,
            "0.5" to high - diff * 0.5,
            "0.618" to high - diff * 0.618,
            "0.786" to high - diff * 0.786,
            "1.0" to low
        )
    }
}