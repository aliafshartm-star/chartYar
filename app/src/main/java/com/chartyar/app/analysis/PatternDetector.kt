package com.chartyar.app.analysis

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object PatternDetector {

    /**
     * تشخیص همه الگوها در لیست کندل‌ها
     */
    fun detectAll(candles: List<Candle>): List<DetectedPattern> {
        if (candles.size < 30) return emptyList()
        val results = mutableListOf<DetectedPattern>()

        detectDoubleTop(candles)?.let { results.add(it) }
        detectDoubleBottom(candles)?.let { results.add(it) }
        detectHeadShoulders(candles)?.let { results.add(it) }
        detectInverseHeadShoulders(candles)?.let { results.add(it) }
        detectAscendingTriangle(candles)?.let { results.add(it) }
        detectDescendingTriangle(candles)?.let { results.add(it) }
        detectSymmetricalTriangle(candles)?.let { results.add(it) }
        detectRisingWedge(candles)?.let { results.add(it) }
        detectFallingWedge(candles)?.let { results.add(it) }
        detectBullishFlag(candles)?.let { results.add(it) }
        detectBearishFlag(candles)?.let { results.add(it) }

        return results.sortedByDescending { it.confidence }
    }

    /**
     * پیدا کردن قله‌های محلی (Swing Highs)
     */
    private fun findSwingHighs(candles: List<Candle>, lookback: Int = 3): List<Int> {
        val indices = mutableListOf<Int>()
        for (i in lookback until candles.size - lookback) {
            val h = candles[i].high
            var isHigh = true
            for (j in (i - lookback)..(i + lookback)) {
                if (j != i && candles[j].high >= h) {
                    isHigh = false
                    break
                }
            }
            if (isHigh) indices.add(i)
        }
        return indices
    }

    /**
     * پیدا کردن دره‌های محلی (Swing Lows)
     */
    private fun findSwingLows(candles: List<Candle>, lookback: Int = 3): List<Int> {
        val indices = mutableListOf<Int>()
        for (i in lookback until candles.size - lookback) {
            val l = candles[i].low
            var isLow = true
            for (j in (i - lookback)..(i + lookback)) {
                if (j != i && candles[j].low <= l) {
                    isLow = false
                    break
                }
            }
            if (isLow) indices.add(i)
        }
        return indices
    }

    // ==================== الگوهای برگشتی ====================

    /**
     * Double Top (سقف دوقلو) - الگوی نزولی
     */
    private fun detectDoubleTop(candles: List<Candle>): DetectedPattern? {
        val highs = findSwingHighs(candles)
        if (highs.size < 2) return null

        val recent = highs.takeLast(5)
        for (i in recent.size - 1 downTo 1) {
            val i2 = recent[i]
            val i1 = recent[i - 1]
            if (i2 - i1 < 8) continue

            val p1 = candles[i1].high
            val p2 = candles[i2].high
            val diff = abs(p1 - p2) / max(p1, p2)
            if (diff > 0.02) continue  // دو سقف باید نزدیک باشن

            // پیدا کردن کف بین دو سقف
            var neckline = Double.MAX_VALUE
            for (j in i1 until i2) {
                if (candles[j].low < neckline) neckline = candles[j].low
            }
            val height = ((p1 + p2) / 2) - neckline
            val target = neckline - height

            return DetectedPattern(
                type = PatternType.DOUBLE_TOP,
                name = "سقف دوقلو (Double Top)",
                direction = -1,
                confidence = 75,
                startIndex = i1,
                endIndex = i2,
                description = "دو سقف تقریباً برابر در محدوده ${"%.2f".format(p1)} شکل گرفته. شکست خط گردن در ${"%.2f".format(neckline)} هدف نزولی می‌ده.",
                targetPrice = target
            )
        }
        return null
    }

    /**
     * Double Bottom (کف دوقلو) - الگوی صعودی
     */
    private fun detectDoubleBottom(candles: List<Candle>): DetectedPattern? {
        val lows = findSwingLows(candles)
        if (lows.size < 2) return null

        val recent = lows.takeLast(5)
        for (i in recent.size - 1 downTo 1) {
            val i2 = recent[i]
            val i1 = recent[i - 1]
            if (i2 - i1 < 8) continue

            val p1 = candles[i1].low
            val p2 = candles[i2].low
            val diff = abs(p1 - p2) / min(p1, p2)
            if (diff > 0.02) continue

            var neckline = Double.MIN_VALUE
            for (j in i1 until i2) {
                if (candles[j].high > neckline) neckline = candles[j].high
            }
            val height = neckline - ((p1 + p2) / 2)
            val target = neckline + height

            return DetectedPattern(
                type = PatternType.DOUBLE_BOTTOM,
                name = "کف دوقلو (Double Bottom)",
                direction = 1,
                confidence = 78,
                startIndex = i1,
                endIndex = i2,
                description = "دو کف تقریباً برابر در محدوده ${"%.2f".format(p1)} شکل گرفته. شکست خط گردن در ${"%.2f".format(neckline)} هدف صعودی می‌ده.",
                targetPrice = target
            )
        }
        return null
    }

    /**
     * Head & Shoulders (سر و شانه) - الگوی نزولی
     */
    private fun detectHeadShoulders(candles: List<Candle>): DetectedPattern? {
        val highs = findSwingHighs(candles)
        if (highs.size < 3) return null

        val recent = highs.takeLast(5)
        for (i in 2 until recent.size) {
            val l = recent[i - 2]
            val h = recent[i - 1]
            val r = recent[i]

            val left = candles[l].high
            val head = candles[h].high
            val right = candles[r].high

            // شانه‌ها باید نزدیک باشن، سر بالاتر
            if (head <= left || head <= right) continue
            if (abs(left - right) / max(left, right) > 0.05) continue

            // محاسبه خط گردن
            var n1 = Double.MAX_VALUE
            var n2 = Double.MAX_VALUE
            for (j in l until h) if (candles[j].low < n1) n1 = candles[j].low
            for (j in h until r) if (candles[j].low < n2) n2 = candles[j].low
            val neckline = (n1 + n2) / 2
            val height = head - neckline
            val target = neckline - height

            return DetectedPattern(
                type = PatternType.HEAD_SHOULDERS,
                name = "سر و شانه (Head & Shoulders)",
                direction = -1,
                confidence = 82,
                startIndex = l,
                endIndex = r,
                description = "الگوی سر و شانه کامل. سر در ${"%.2f".format(head)} و شانه‌ها در ${"%.2f".format(left)} و ${"%.2f".format(right)}. هدف نزولی تا ${"%.2f".format(target)}.",
                targetPrice = target
            )
        }
        return null
    }

    /**
     * Inverse Head & Shoulders (سر و شانه معکوس) - صعودی
     */
    private fun detectInverseHeadShoulders(candles: List<Candle>): DetectedPattern? {
        val lows = findSwingLows(candles)
        if (lows.size < 3) return null

        val recent = lows.takeLast(5)
        for (i in 2 until recent.size) {
            val l = recent[i - 2]
            val h = recent[i - 1]
            val r = recent[i]

            val left = candles[l].low
            val head = candles[h].low
            val right = candles[r].low

            if (head >= left || head >= right) continue
            if (abs(left - right) / min(left, right) > 0.05) continue

            var n1 = Double.MIN_VALUE
            var n2 = Double.MIN_VALUE
            for (j in l until h) if (candles[j].high > n1) n1 = candles[j].high
            for (j in h until r) if (candles[j].high > n2) n2 = candles[j].high
            val neckline = (n1 + n2) / 2
            val height = neckline - head
            val target = neckline + height

            return DetectedPattern(
                type = PatternType.INVERSE_HEAD_SHOULDERS,
                name = "سر و شانه معکوس (Inverse H&S)",
                direction = 1,
                confidence = 80,
                startIndex = l,
                endIndex = r,
                description = "الگوی سر و شانه معکوس. سر در ${"%.2f".format(head)}. هدف صعودی تا ${"%.2f".format(target)}.",
                targetPrice = target
            )
        }
        return null
    }

    // ==================== مثلث‌ها ====================

    /**
     * Ascending Triangle (مثلث صعودی)
     */
    private fun detectAscendingTriangle(candles: List<Candle>): DetectedPattern? {
        val recent = candles.takeLast(40)
        if (recent.size < 20) return null

        val highs = findSwingHighs(recent)
        val lows = findSwingLows(recent)
        if (highs.size < 2 || lows.size < 2) return null

        val highPrices = highs.map { recent[it].high }
        val lowPrices = lows.map { recent[it].low }

        // سقف‌ها باید تقریباً افقی، کف‌ها صعودی
        val highsFlat = (highPrices.maxOrNull()!! - highPrices.minOrNull()!!) / highPrices.maxOrNull()!! < 0.03
        val lowsRising = lowPrices.last() > lowPrices.first()

        if (highsFlat && lowsRising) {
            val resistance = highPrices.average()
            val target = resistance + (resistance - lowPrices.first()) * 0.8
            return DetectedPattern(
                type = PatternType.ASCENDING_TRIANGLE,
                name = "مثلث صعودی (Ascending Triangle)",
                direction = 1,
                confidence = 72,
                startIndex = candles.size - recent.size,
                endIndex = candles.size - 1,
                description = "مقاومت افقی در ${"%.2f".format(resistance)} و کف‌های صعودی. شکست مقاومت هدف صعودی می‌ده.",
                targetPrice = target
            )
        }
        return null
    }

    /**
     * Descending Triangle (مثلث نزولی)
     */
    private fun detectDescendingTriangle(candles: List<Candle>): DetectedPattern? {
        val recent = candles.takeLast(40)
        if (recent.size < 20) return null

        val highs = findSwingHighs(recent)
        val lows = findSwingLows(recent)
        if (highs.size < 2 || lows.size < 2) return null

        val highPrices = highs.map { recent[it].high }
        val lowPrices = lows.map { recent[it].low }

        val lowsFlat = (lowPrices.maxOrNull()!! - lowPrices.minOrNull()!!) / lowPrices.maxOrNull()!! < 0.03
        val highsFalling = highPrices.last() < highPrices.first()

        if (lowsFlat && highsFalling) {
            val support = lowPrices.average()
            val target = support - (highPrices.first() - support) * 0.8
            return DetectedPattern(
                type = PatternType.DESCENDING_TRIANGLE,
                name = "مثلث نزولی (Descending Triangle)",
                direction = -1,
                confidence = 72,
                startIndex = candles.size - recent.size,
                endIndex = candles.size - 1,
                description = "حمایت افقی در ${"%.2f".format(support)} و سقف‌های نزولی. شکست حمایت هدف نزولی می‌ده.",
                targetPrice = target
            )
        }
        return null
    }

    /**
     * Symmetrical Triangle (مثلث متقارن)
     */
    private fun detectSymmetricalTriangle(candles: List<Candle>): DetectedPattern? {
        val recent = candles.takeLast(40)
        if (recent.size < 20) return null

        val highs = findSwingHighs(recent)
        val lows = findSwingLows(recent)
        if (highs.size < 2 || lows.size < 2) return null

        val highPrices = highs.map { recent[it].high }
        val lowPrices = lows.map { recent[it].low }

        val highsFalling = highPrices.last() < highPrices.first()
        val lowsRising = lowPrices.last() > lowPrices.first()

        if (highsFalling && lowsRising) {
            return DetectedPattern(
                type = PatternType.SYMMETRICAL_TRIANGLE,
                name = "مثلث متقارن (Symmetrical Triangle)",
                direction = 0,
                confidence = 60,
                startIndex = candles.size - recent.size,
                endIndex = candles.size - 1,
                description = "سقف‌های نزولی و کف‌های صعودی. الگو معمولاً ادامه‌دهنده است. منتظر شکست بمان.",
                targetPrice = null
            )
        }
        return null
    }

    // ==================== گوه‌ها ====================

    /**
     * Rising Wedge (گوه صعودی) - نزولی
     */
    private fun detectRisingWedge(candles: List<Candle>): DetectedPattern? {
        val recent = candles.takeLast(40)
        if (recent.size < 20) return null

        val highs = findSwingHighs(recent)
        val lows = findSwingLows(recent)
        if (highs.size < 2 || lows.size < 2) return null

        val highPrices = highs.map { recent[it].high }
        val lowPrices = lows.map { recent[it].low }

        val bothRising = highPrices.last() > highPrices.first() && lowPrices.last() > lowPrices.first()
        if (!bothRising) return null

        // شیب کف باید بیشتر از سقف باشه (همگرا)
        val highSlope = (highPrices.last() - highPrices.first()) / highPrices.first()
        val lowSlope = (lowPrices.last() - lowPrices.first()) / lowPrices.first()

        if (lowSlope > highSlope * 1.3) {
            return DetectedPattern(
                type = PatternType.RISING_WEDGE,
                name = "گوه صعودی (Rising Wedge)",
                direction = -1,
                confidence = 70,
                startIndex = candles.size - recent.size,
                endIndex = candles.size - 1,
                description = "الگوی گوه صعودی — احتمال شکست نزولی و برگشت. محتاط باش.",
                targetPrice = lowPrices.first()
            )
        }
        return null
    }

    /**
     * Falling Wedge (گوه نزولی) - صعودی
     */
    private fun detectFallingWedge(candles: List<Candle>): DetectedPattern? {
        val recent = candles.takeLast(40)
        if (recent.size < 20) return null

        val highs = findSwingHighs(recent)
        val lows = findSwingLows(recent)
        if (highs.size < 2 || lows.size < 2) return null

        val highPrices = highs.map { recent[it].high }
        val lowPrices = lows.map { recent[it].low }

        val bothFalling = highPrices.last() < highPrices.first() && lowPrices.last() < lowPrices.first()
        if (!bothFalling) return null

        val highSlope = (highPrices.first() - highPrices.last()) / highPrices.first()
        val lowSlope = (lowPrices.first() - lowPrices.last()) / lowPrices.first()

        if (highSlope > lowSlope * 1.3) {
            return DetectedPattern(
                type = PatternType.FALLING_WEDGE,
                name = "گوه نزولی (Falling Wedge)",
                direction = 1,
                confidence = 72,
                startIndex = candles.size - recent.size,
                endIndex = candles.size - 1,
                description = "الگوی گوه نزولی — احتمال شکست صعودی. فرصت خرید.",
                targetPrice = highPrices.first()
            )
        }
        return null
    }

    // ==================== پرچم‌ها ====================

    /**
     * Bullish Flag (پرچم صعودی)
     */
    private fun detectBullishFlag(candles: List<Candle>): DetectedPattern? {
        if (candles.size < 30) return null
        val recent = candles.takeLast(30)
        val pole = recent.take(10)
        val flag = recent.takeLast(20)

        val poleMove = pole.last().close - pole.first().close
        if (poleMove <= 0) return null
        val polePercent = poleMove / pole.first().close
        if (polePercent < 0.05) return null

        val flagHigh = flag.maxOf { it.high }
        val flagLow = flag.minOf { it.low }
        val flagRange = (flagHigh - flagLow) / flagLow

        // پرچم باید رنج کوچک و کمی نزولی/خنثی باشه
        if (flagRange < 0.08 && flag.last().close < flagHigh) {
            val target = flagHigh + poleMove
            return DetectedPattern(
                type = PatternType.BULLISH_FLAG,
                name = "پرچم صعودی (Bullish Flag)",
                direction = 1,
                confidence = 75,
                startIndex = candles.size - recent.size,
                endIndex = candles.size - 1,
                description = "پرچم صعودی پس از یه میله صعودی قوی. شکست سقف پرچم هدف ${"%.2f".format(target)} می‌ده.",
                targetPrice = target
            )
        }
        return null
    }

    /**
     * Bearish Flag (پرچم نزولی)
     */
    private fun detectBearishFlag(candles: List<Candle>): DetectedPattern? {
        if (candles.size < 30) return null
        val recent = candles.takeLast(30)
        val pole = recent.take(10)
        val flag = recent.takeLast(20)

        val poleMove = pole.last().close - pole.first().close
        if (poleMove >= 0) return null
        val polePercent = abs(poleMove) / pole.first().close
        if (polePercent < 0.05) return null

        val flagHigh = flag.maxOf { it.high }
        val flagLow = flag.minOf { it.low }
        val flagRange = (flagHigh - flagLow) / flagLow

        if (flagRange < 0.08 && flag.last().close > flagLow) {
            val target = flagLow + poleMove
            return DetectedPattern(
                type = PatternType.BEARISH_FLAG,
                name = "پرچم نزولی (Bearish Flag)",
                direction = -1,
                confidence = 75,
                startIndex = candles.size - recent.size,
                endIndex = candles.size - 1,
                description = "پرچم نزولی پس از یه میله نزولی قوی. شکست کف پرچم هدف ${"%.2f".format(target)} می‌ده.",
                targetPrice = target
            )
        }
        return null
    }
}