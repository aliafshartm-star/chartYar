package com.chartyar.app.analysis

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Computer Vision analysis for chart images.
 * Detects trend lines, support and resistance levels.
 * Pure Kotlin implementation - no external dependencies.
 */
object ChartVisionAnalyzer {

    data class TrendLine(
        val slope: Double,
        val intercept: Double,
        val strength: Int,
        val isSupport: Boolean,
        val startIndex: Int,
        val endIndex: Int
    )

    data class KeyLevel(
        val price: Double,
        val strength: Int
    )

    data class VisionResult(
        val trendLines: List<TrendLine>,
        val supportLevels: List<KeyLevel>,
        val resistanceLevels: List<KeyLevel>,
        val dominantTrend: Int
    )

    fun analyze(candles: List<Candle>): VisionResult {
        if (candles.size < 20) {
            return VisionResult(emptyList(), emptyList(), emptyList(), 0)
        }
        val lines = detectTrendLines(candles)
        val levels = detectKeyLevels(candles)
        val trend = detectDominantTrend(lines, candles)
        return VisionResult(
            trendLines = lines,
            supportLevels = levels.first,
            resistanceLevels = levels.second,
            dominantTrend = trend
        )
    }

    // ==========================================
    // TREND LINES
    // ==========================================

    private fun detectTrendLines(candles: List<Candle>): List<TrendLine> {
        val result = mutableListOf<TrendLine>()
        val swingLows = findSwingLows(candles)
        val swingHighs = findSwingHighs(candles)

        if (swingLows.size >= 3) {
            val line = bestFitLine(swingLows, candles, isSupport = true)
            if (line != null) result.add(line)
        }

        if (swingHighs.size >= 3) {
            val line = bestFitLine(swingHighs, candles, isSupport = false)
            if (line != null) result.add(line)
        }

        return result
    }

    private fun findSwingLows(candles: List<Candle>): List<Pair<Int, Double>> {
        val result = mutableListOf<Pair<Int, Double>>()
        val window = 3
        for (i in window until candles.size - window) {
            val c = candles[i]
            var isSwing = true
            for (j in 1..window) {
                if (candles[i - j].low < c.low || candles[i + j].low < c.low) {
                    isSwing = false
                    break
                }
            }
            if (isSwing) result.add(i to c.low)
        }
        return result
    }

    private fun findSwingHighs(candles: List<Candle>): List<Pair<Int, Double>> {
        val result = mutableListOf<Pair<Int, Double>>()
        val window = 3
        for (i in window until candles.size - window) {
            val c = candles[i]
            var isSwing = true
            for (j in 1..window) {
                if (candles[i - j].high > c.high || candles[i + j].high > c.high) {
                    isSwing = false
                    break
                }
            }
            if (isSwing) result.add(i to c.high)
        }
        return result
    }

    private fun bestFitLine(
        points: List<Pair<Int, Double>>,
        candles: List<Candle>,
        isSupport: Boolean
    ): TrendLine? {
        if (points.size < 3) return null

        val n = points.size.toDouble()
        val sumX = points.sumOf { it.first.toDouble() }
        val sumY = points.sumOf { it.second }
        val sumXY = points.sumOf { it.first.toDouble() * it.second }
        val sumX2 = points.sumOf { it.first.toDouble() * it.first.toDouble() }

        val denom = n * sumX2 - sumX * sumX
        if (abs(denom) < 0.0001) return null

        val slope = (n * sumXY - sumX * sumY) / denom
        val intercept = (sumY - slope * sumX) / n

        val priceRange = candles.maxOf { it.high } - candles.minOf { it.low }
        val tolerance = priceRange * 0.02

        var strength = 0
        for ((idx, price) in points) {
            val linePrice = slope * idx + intercept
            if (abs(linePrice - price) < tolerance * 3) strength++
        }

        if (strength < 3) return null

        val firstIdx = points.first().first
        val lastIdx = points.last().first

        return TrendLine(
            slope = slope,
            intercept = intercept,
            strength = strength,
            isSupport = isSupport,
            startIndex = firstIdx,
            endIndex = lastIdx
        )
    }

    // ==========================================
    // KEY LEVELS (Support / Resistance)
    // ==========================================

    private fun detectKeyLevels(candles: List<Candle>): Pair<List<KeyLevel>, List<KeyLevel>> {
        if (candles.isEmpty()) return emptyList<KeyLevel>() to emptyList()

        val priceHigh = candles.maxOf { it.high }
        val priceLow = candles.minOf { it.low }
        val range = priceHigh - priceLow
        if (range <= 0.0) return emptyList<KeyLevel>() to emptyList()

        val bands = 50
        val bandHeight = range / bands
        val histogram = IntArray(bands)

        for (c in candles) {
            val highBand = ((c.high - priceLow) / bandHeight).toInt().coerceIn(0, bands - 1)
            val lowBand = ((c.low - priceLow) / bandHeight).toInt().coerceIn(0, bands - 1)
            for (b in lowBand..highBand) {
                histogram[b]++
            }
        }

        val levels = mutableListOf<Pair<Int, Double>>()
        for (i in 1 until bands - 1) {
            if (histogram[i] > histogram[i - 1] && histogram[i] > histogram[i + 1]) {
                if (histogram[i] >= max(3, candles.size / 5)) {
                    val price = priceLow + (i + 0.5) * bandHeight
                    levels.add(histogram[i] to price)
                }
            }
        }

        val sorted = levels.sortedByDescending { it.first }
        val currentPrice = candles.last().close

        val supports = sorted
            .filter { it.second < currentPrice }
            .take(3)
            .map { KeyLevel(it.second, it.first) }

        val resistances = sorted
            .filter { it.second > currentPrice }
            .take(3)
            .map { KeyLevel(it.second, it.first) }

        return supports to resistances
    }

    // ==========================================
    // DOMINANT TREND
    // ==========================================

    private fun detectDominantTrend(
        trendLines: List<TrendLine>,
        candles: List<Candle>
    ): Int {
        if (trendLines.isEmpty()) {
            if (candles.size < 20) return 0
            val recent = candles.takeLast(10).map { it.close }.average()
            val earlier = candles.dropLast(10).takeLast(10).map { it.close }.average()
            return when {
                recent > earlier * 1.01 -> 1
                recent < earlier * 0.99 -> -1
                else -> 0
            }
        }

        var bullScore = 0
        var bearScore = 0
        for (line in trendLines) {
            if (line.slope < 0.0) {
                bullScore += line.strength
            } else {
                bearScore += line.strength
            }
        }

        return when {
            bullScore > bearScore * 1.5 -> 1
            bearScore > bullScore * 1.5 -> -1
            else -> 0
        }
    }

    // ==========================================
    // HELPER - CHECK IF PRICE CROSSED A LEVEL
    // ==========================================

    fun isNearLevel(price: Double, level: Double, tolerancePercent: Double = 0.01): Boolean {
        if (level <= 0.0) return false
        val diff = abs(price - level) / level
        return diff < tolerancePercent
    }

    fun distanceToLevelPercent(price: Double, level: Double): Double {
        if (level <= 0.0) return 0.0
        return ((price - level) / level) * 100.0
    }
}