package com.chartyar.app.analysis

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object PatternDetector {

    fun detectAll(candles: List<Candle>): List<DetectedPattern> {
        if (candles.size < 30) return emptyList()
        val results = mutableListOf<DetectedPattern>()

        detectDoubleTop(candles)?.let { results.add(it) }
        detectDoubleBottom(candles)?.let { results.add(it) }
        detectHeadShoulders(candles)?.let { results.add(it) }
        detectInverseHeadShoulders(candles)?.let { results.add(it) }
        detectTripleTop(candles)?.let { results.add(it) }
        detectTripleBottom(candles)?.let { results.add(it) }
        detectAscendingTriangle(candles)?.let { results.add(it) }
        detectDescendingTriangle(candles)?.let { results.add(it) }
        detectSymmetricalTriangle(candles)?.let { results.add(it) }
        detectRisingWedge(candles)?.let { results.add(it) }
        detectFallingWedge(candles)?.let { results.add(it) }
        detectBullishFlag(candles)?.let { results.add(it) }
        detectBearishFlag(candles)?.let { results.add(it) }
        detectPennant(candles)?.let { results.add(it) }
        detectCupHandle(candles)?.let { results.add(it) }

        results.addAll(detectCandlestickPatterns(candles))

        return results.sortedByDescending { it.confidence }
    }

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

    // ==================== الگوهای کندلی ====================

    private fun detectCandlestickPatterns(candles: List<Candle>): List<DetectedPattern> {
        val results = mutableListOf<DetectedPattern>()
        if (candles.size < 3) return results

        val last = candles.last()
        val prev = candles[candles.size - 2]
        val prev2 = candles[candles.size - 3]
        val idx = candles.size - 1

        // --- Doji ---
        if (last.range > 0 && last.body < last.range * 0.1) {
            if (last.upperWick > last.body * 3 && last.lowerWick < last.body) {
                results.add(candlestickPattern(
                    PatternType.GRAVESTONE_DOJI, "دوجی سنگ قبر (Gravestone Doji)", -1, 65, idx,
                    "دوجی با سایه بالایی بلند — نشانه ضعف خریداران و احتمال بازگشت نزولی"
                ))
            } else if (last.lowerWick > last.body * 3 && last.upperWick < last.body) {
                results.add(candlestickPattern(
                    PatternType.DRAGONFLY_DOJI, "دوجی سنجاقک (Dragonfly Doji)", 1, 65, idx,
                    "دوجی با سایه پایینی بلند — نشانه قدرت خریداران و احتمال بازگشت صعودی"
                ))
            } else {
                results.add(candlestickPattern(
                    PatternType.DOJI, "دوجی (Doji)", 0, 50, idx,
                    "کندل بی‌تصمیم — بلاتکلیفی بازار"
                ))
            }
        }

        // --- Spinning Top ---
        if (last.body > 0 && last.body < last.range * 0.3 &&
            last.upperWick > last.body * 0.5 && last.lowerWick > last.body * 0.5
        ) {
            results.add(candlestickPattern(
                PatternType.SPINNING_TOP, "فرفره (Spinning Top)", 0, 45, idx,
                "فرفره — نشانه تردید بین خریداران و فروشندگان"
            ))
        }

        // --- Hammer ---
        if (last.lowerWick > last.body * 2 && last.upperWick < last.body * 0.5 &&
            last.body > 0 && last.isBullish
        ) {
            results.add(candlestickPattern(
                PatternType.HAMMER, "چکش (Hammer)", 1, 72, idx,
                "چکش در پایین روند نزولی — احتمال بازگشت صعودی"
            ))
        }

        // --- Inverted Hammer ---
        if (last.upperWick > last.body * 2 && last.lowerWick < last.body * 0.5 &&
            last.body > 0 && last.isBullish
        ) {
            results.add(candlestickPattern(
                PatternType.INVERTED_HAMMER, "چکش معکوس (Inverted Hammer)", 1, 68, idx,
                "چکش معکوس — سیگنال اولیه بازگشت صعودی"
            ))
        }

        // --- Hanging Man ---
        if (last.lowerWick > last.body * 2 && last.upperWick < last.body * 0.5 &&
            last.body > 0 && last.isBearish
        ) {
            results.add(candlestickPattern(
                PatternType.HANGING_MAN, "مرد آویزان (Hanging Man)", -1, 68, idx,
                "مرد آویزان در بالای روند صعودی — هشدار بازگشت نزولی"
            ))
        }

        // --- Shooting Star ---
        if (last.upperWick > last.body * 2 && last.lowerWick < last.body * 0.5 &&
            last.body > 0 && last.isBearish
        ) {
            results.add(candlestickPattern(
                PatternType.SHOOTING_STAR, "ستاره ثاقب (Shooting Star)", -1, 72, idx,
                "ستاره ثاقب — احتمال بازگشت نزولی"
            ))
        }

        // --- Bullish Engulfing ---
        if (prev.isBearish && last.isBullish &&
            last.open <= prev.close && last.close >= prev.open &&
            last.body > prev.body
        ) {
            results.add(candlestickPattern(
                PatternType.BULLISH_ENGULFING, "پوشای صعودی (Bullish Engulfing)", 1, 80, idx,
                "کندل صعودی کندل نزولی قبلی را کاملاً پوشانده — سیگنال خرید قوی"
            ))
        }

        // --- Bearish Engulfing ---
        if (prev.isBullish && last.isBearish &&
            last.open >= prev.close && last.close <= prev.open &&
            last.body > prev.body
        ) {
            results.add(candlestickPattern(
                PatternType.BEARISH_ENGULFING, "پوشای نزولی (Bearish Engulfing)", -1, 80, idx,
                "کندل نزولی کندل صعودی قبلی را کاملاً پوشانده — سیگنال فروش قوی"
            ))
        }

        // --- Piercing Line ---
        if (prev.isBearish && last.isBullish &&
            last.open < prev.low &&
            last.close > (prev.open + prev.close) / 2 &&
            last.close < prev.open
        ) {
            results.add(candlestickPattern(
                PatternType.PIERCING_LINE, "خط نافذ (Piercing Line)", 1, 75, idx,
                "کندل صعودی بیش از نیمی از کندل نزولی قبلی را پوشانده"
            ))
        }

        // --- Dark Cloud Cover ---
        if (prev.isBullish && last.isBearish &&
            last.open > prev.high &&
            last.close < (prev.open + prev.close) / 2 &&
            last.close > prev.open
        ) {
            results.add(candlestickPattern(
                PatternType.DARK_CLOUD_COVER, "ابر تیره (Dark Cloud Cover)", -1, 75, idx,
                "کندل نزولی بیش از نیمی از کندل صعودی قبلی را پوشانده"
            ))
        }

        // --- Bullish Harami ---
        if (prev.isBearish && last.isBullish &&
            last.open > prev.close && last.close < prev.open &&
            last.body < prev.body * 0.7
        ) {
            results.add(candlestickPattern(
                PatternType.BULLISH_HARAMI, "هارامی صعودی (Bullish Harami)", 1, 65, idx,
                "کندل صعودی کوچک داخل کندل نزولی قبلی"
            ))
        }

        // --- Bearish Harami ---
        if (prev.isBullish && last.isBearish &&
            last.open < prev.close && last.close > prev.open &&
            last.body < prev.body * 0.7
        ) {
            results.add(candlestickPattern(
                PatternType.BEARISH_HARAMI, "هارامی نزولی (Bearish Harami)", -1, 65, idx,
                "کندل نزولی کوچک داخل کندل صعودی قبلی"
            ))
        }

        // --- Tweezer Bottom ---
        if (prev.isBearish && last.isBullish &&
            abs(prev.low - last.low) / prev.low < 0.003
        ) {
            results.add(candlestickPattern(
                PatternType.TWEEZER_BOTTOM, "انبرکف (Tweezer Bottom)", 1, 68, idx,
                "دو کندل با کف تقریباً برابر — حمایت قوی"
            ))
        }

        // --- Tweezer Top ---
        if (prev.isBullish && last.isBearish &&
            abs(prev.high - last.high) / prev.high < 0.003
        ) {
            results.add(candlestickPattern(
                PatternType.TWEEZER_TOP, "انبرسقف (Tweezer Top)", -1, 68, idx,
                "دو کندل با سقف تقریباً برابر — مقاومت قوی"
            ))
        }

        // --- Morning Star ---
        if (prev2.isBearish && prev.body < prev2.body * 0.5 &&
            last.isBullish && last.close > (prev2.open + prev2.close) / 2
        ) {
            results.add(candlestickPattern(
                PatternType.MORNING_STAR, "ستاره صبح (Morning Star)", 1, 82, idx,
                "الگوی ستاره صبح سه‌کندلی — بازگشت صعودی قوی"
            ))
        }

        // --- Evening Star ---
        if (prev2.isBullish && prev.body < prev2.body * 0.5 &&
            last.isBearish && last.close < (prev2.open + prev2.close) / 2
        ) {
            results.add(candlestickPattern(
                PatternType.EVENING_STAR, "ستاره شب (Evening Star)", -1, 82, idx,
                "الگوی ستاره شب سه‌کندلی — بازگشت نزولی قوی"
            ))
        }

        // --- Three White Soldiers ---
        if (prev2.isBullish && prev.isBullish && last.isBullish &&
            prev.close > prev2.close && last.close > prev.close &&
            prev.open > prev2.open && last.open > prev.open
        ) {
            results.add(candlestickPattern(
                PatternType.THREE_WHITE_SOLDIERS, "سه سرباز سفید (Three White Soldiers)", 1, 78, idx,
                "سه کندل صعودی متوالی — روند صعودی قوی"
            ))
        }

        // --- Three Black Crows ---
        if (prev2.isBearish && prev.isBearish && last.isBearish &&
            prev.close < prev2.close && last.close < prev.close &&
            prev.open < prev2.open && last.open < prev.open
        ) {
            results.add(candlestickPattern(
                PatternType.THREE_BLACK_CROWS, "سه کلاغ سیاه (Three Black Crows)", -1, 78, idx,
                "سه کندل نزولی متوالی — روند نزولی قوی"
            ))
        }

        return results
    }

    private fun candlestickPattern(
        type: PatternType,
        name: String,
        direction: Int,
        confidence: Int,
        idx: Int,
        description: String
    ): DetectedPattern {
        return DetectedPattern(
            type = type,
            name = name,
            direction = direction,
            confidence = confidence,
            startIndex = idx,
            endIndex = idx,
            description = description,
            targetPrice = null
        )
    }

    // ==================== الگوهای کلاسیک ====================

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
            if (diff > 0.02) continue
            var neckline = Double.MAX_VALUE
            for (j in i1 until i2) if (candles[j].low < neckline) neckline = candles[j].low
            val height = ((p1 + p2) / 2) - neckline
            val target = neckline - height
            return DetectedPattern(
                type = PatternType.DOUBLE_TOP,
                name = "سقف دوقلو (Double Top)",
                direction = -1,
                confidence = 75,
                startIndex = i1,
                endIndex = i2,
                description = "دو سقف تقریباً برابر در محدوده ${"%.2f".format(p1)} — شکست خط گردن در ${"%.2f".format(neckline)} هدف نزولی می‌ده.",
                targetPrice = target
            )
        }
        return null
    }

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
            for (j in i1 until i2) if (candles[j].high > neckline) neckline = candles[j].high
            val height = neckline - ((p1 + p2) / 2)
            val target = neckline + height
            return DetectedPattern(
                type = PatternType.DOUBLE_BOTTOM,
                name = "کف دوقلو (Double Bottom)",
                direction = 1,
                confidence = 78,
                startIndex = i1,
                endIndex = i2,
                description = "دو کف تقریباً برابر در محدوده ${"%.2f".format(p1)} — شکست خط گردن در ${"%.2f".format(neckline)} هدف صعودی می‌ده.",
                targetPrice = target
            )
        }
        return null
    }

    private fun detectHeadShoulders(candles: List<Candle>): DetectedPattern? {
        val highs = findSwingHighs(candles)
        if (highs.size < 3) return null
        val recent = highs.takeLast(5)
        for (i in 2 until recent.size) {
            val l = recent[i - 2]; val h = recent[i - 1]; val r = recent[i]
            val left = candles[l].high; val head = candles[h].high; val right = candles[r].high
            if (head <= left || head <= right) continue
            if (abs(left - right) / max(left, right) > 0.05) continue
            var n1 = Double.MAX_VALUE; var n2 = Double.MAX_VALUE
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
                description = "الگوی سر و شانه کامل — هدف نزولی تا ${"%.2f".format(target)}.",
                targetPrice = target
            )
        }
        return null
    }

    private fun detectInverseHeadShoulders(candles: List<Candle>): DetectedPattern? {
        val lows = findSwingLows(candles)
        if (lows.size < 3) return null
        val recent = lows.takeLast(5)
        for (i in 2 until recent.size) {
            val l = recent[i - 2]; val h = recent[i - 1]; val r = recent[i]
            val left = candles[l].low; val head = candles[h].low; val right = candles[r].low
            if (head >= left || head >= right) continue
            if (abs(left - right) / min(left, right) > 0.05) continue
            var n1 = Double.MIN_VALUE; var n2 = Double.MIN_VALUE
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
                description = "الگوی سر و شانه معکوس — هدف صعودی تا ${"%.2f".format(target)}.",
                targetPrice = target
            )
        }
        return null
    }

    private fun detectTripleTop(candles: List<Candle>): DetectedPattern? {
        val highs = findSwingHighs(candles)
        if (highs.size < 3) return null
        val recent = highs.takeLast(3)
        val p1 = candles[recent[0]].high
        val p2 = candles[recent[1]].high
        val p3 = candles[recent[2]].high
        val avg = (p1 + p2 + p3) / 3.0
        val maxDiff = maxOf(abs(p1 - avg), abs(p2 - avg), abs(p3 - avg)) / avg
        if (maxDiff > 0.03) return null
        var neckline = Double.MAX_VALUE
        for (j in recent[0]..recent[2]) if (candles[j].low < neckline) neckline = candles[j].low
        val target = neckline - (avg - neckline)
        return DetectedPattern(
            type = PatternType.TRIPLE_TOP,
            name = "سقف سه‌قلو (Triple Top)",
            direction = -1,
            confidence = 78,
            startIndex = recent[0],
            endIndex = recent[2],
            description = "سه سقف تقریباً برابر در محدوده ${"%.2f".format(avg)} — سیگنال نزولی قوی.",
            targetPrice = target
        )
    }

    private fun detectTripleBottom(candles: List<Candle>): DetectedPattern? {
        val lows = findSwingLows(candles)
        if (lows.size < 3) return null
        val recent = lows.takeLast(3)
        val p1 = candles[recent[0]].low
        val p2 = candles[recent[1]].low
        val p3 = candles[recent[2]].low
        val avg = (p1 + p2 + p3) / 3.0
        val maxDiff = maxOf(abs(p1 - avg), abs(p2 - avg), abs(p3 - avg)) / avg
        if (maxDiff > 0.03) return null
        var neckline = Double.MIN_VALUE
        for (j in recent[0]..recent[2]) if (candles[j].high > neckline) neckline = candles[j].high
        val target = neckline + (neckline - avg)
        return DetectedPattern(
            type = PatternType.TRIPLE_BOTTOM,
            name = "کف سه‌قلو (Triple Bottom)",
            direction = 1,
            confidence = 80,
            startIndex = recent[0],
            endIndex = recent[2],
            description = "سه کف تقریباً برابر در محدوده ${"%.2f".format(avg)} — سیگنال صعودی قوی.",
            targetPrice = target
        )
    }

    private fun detectAscendingTriangle(candles: List<Candle>): DetectedPattern? {
        val recent = candles.takeLast(40)
        if (recent.size < 20) return null
        val highs = findSwingHighs(recent)
        val lows = findSwingLows(recent)
        if (highs.size < 2 || lows.size < 2) return null
        val highPrices = highs.map { recent[it].high }
        val lowPrices = lows.map { recent[it].low }
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
                description = "مقاومت افقی در ${"%.2f".format(resistance)} و کف‌های صعودی — شکست مقاومت هدف صعودی می‌ده.",
                targetPrice = target
            )
        }
        return null
    }

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
                description = "حمایت افقی در ${"%.2f".format(support)} و سقف‌های نزولی — شکست حمایت هدف نزولی می‌ده.",
                targetPrice = target
            )
        }
        return null
    }

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
                description = "سقف‌های نزولی و کف‌های صعودی — منتظر شکست بمان.",
                targetPrice = null
            )
        }
        return null
    }

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
                description = "گوه صعودی — احتمال شکست نزولی و برگشت.",
                targetPrice = lowPrices.first()
            )
        }
        return null
    }

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
                description = "گوه نزولی — احتمال شکست صعودی. فرصت خرید.",
                targetPrice = highPrices.first()
            )
        }
        return null
    }

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
        if (flagRange < 0.08 && flag.last().close < flagHigh) {
            val target = flagHigh + poleMove
            return DetectedPattern(
                type = PatternType.BULLISH_FLAG,
                name = "پرچم صعودی (Bullish Flag)",
                direction = 1,
                confidence = 75,
                startIndex = candles.size - recent.size,
                endIndex = candles.size - 1,
                description = "پرچم صعودی پس از یه میله قوی — هدف ${"%.2f".format(target)}.",
                targetPrice = target
            )
        }
        return null
    }

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
                description = "پرچم نزولی پس از یه میله قوی — هدف ${"%.2f".format(target)}.",
                targetPrice = target
            )
        }
        return null
    }

    private fun detectPennant(candles: List<Candle>): DetectedPattern? {
        if (candles.size < 30) return null
        val recent = candles.takeLast(30)
        val pole = recent.take(8)
        val pennant = recent.takeLast(20)

        val poleMove = pole.last().close - pole.first().close
        val polePercent = abs(poleMove) / pole.first().close
        if (polePercent < 0.04) return null

        val highPrices = pennant.map { it.high }
        val lowPrices = pennant.map { it.low }
        val highsFalling = highPrices.last() < highPrices.first()
        val lowsRising = lowPrices.last() > lowPrices.first()

        if (highsFalling && lowsRising) {
            val direction = if (poleMove > 0) 1 else -1
            val target = if (direction > 0) pennant.maxOf { it.high } + abs(poleMove)
            else pennant.minOf { it.low } - abs(poleMove)
            return DetectedPattern(
                type = PatternType.PENNANT,
                name = "آویز (Pennant)",
                direction = direction,
                confidence = 70,
                startIndex = candles.size - recent.size,
                endIndex = candles.size - 1,
                description = "آویز بعد از حرکت قوی — احتمال ادامه روند.",
                targetPrice = target
            )
        }
        return null
    }

    private fun detectCupHandle(candles: List<Candle>): DetectedPattern? {
        if (candles.size < 60) return null
        val recent = candles.takeLast(60)
        val cupPart = recent.take(45)
        val handlePart = recent.takeLast(15)

        val cupLow = cupPart.minOf { it.low }
        val cupHigh = cupPart.maxOf { it.high }
        val cupLeft = cupPart.take(5).maxOf { it.high }
        val cupRight = cupPart.drop(40).take(5).maxOf { it.high }

        val similarEdges = abs(cupLeft - cupRight) / max(cupLeft, cupRight) < 0.05
        val depth = (cupHigh - cupLow) / cupHigh

        if (similarEdges && depth > 0.1) {
            val handleLow = handlePart.minOf { it.low }
            val handleHigh = handlePart.maxOf { it.high }
            val handleDepth = (handleHigh - handleLow) / handleHigh

            if (handleDepth < depth * 0.5) {
                val target = cupHigh + (cupHigh - cupLow)
                return DetectedPattern(
                    type = PatternType.CUP_HANDLE,
                    name = "فنجان و دسته (Cup & Handle)",
                    direction = 1,
                    confidence = 76,
                    startIndex = candles.size - recent.size,
                    endIndex = candles.size - 1,
                    description = "الگوی فنجان و دسته — هدف صعودی ${"%.2f".format(target)}.",
                    targetPrice = target
                )
            }
        }
        return null
    }
}