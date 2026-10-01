package com.chartyar.app.analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.chartyar.app.util.ImageOptimizer
import java.io.InputStream
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object ImageProcessor {

    enum class Theme { DARK, LIGHT }

    data class CandlePixel(
        val x: Int,
        val top: Int,
        val bottom: Int,
        val bodyTop: Int,
        val bodyBottom: Int,
        val isBullish: Boolean
    )

    data class ChartBounds(
        val chartTop: Int,
        val chartBottom: Int,
        val chartLeft: Int,
        val chartRight: Int
    )

    fun loadBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inScaled = false
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            bitmap?.let {
                val converted = if (it.config != Bitmap.Config.ARGB_8888) {
                    val c = it.copy(Bitmap.Config.ARGB_8888, false)
                    it.recycle()
                    c
                } else it

                ImageOptimizer.optimizeForOcr(converted)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun detectTheme(bitmap: Bitmap): Theme {
        val w = bitmap.width
        val h = bitmap.height
        val samples = listOf(
            w / 20 to h / 20,
            w - w / 20 to h / 20,
            w / 20 to h - h / 20,
            w - w / 20 to h - h / 20,
            w / 2 to h / 20,
            w / 2 to h - h / 20
        )
        var totalLum = 0.0
        var count = 0
        for ((x, y) in samples) {
            if (x in 0 until w && y in 0 until h) {
                val px = bitmap.getPixel(x, y)
                val r = (px shr 16) and 0xFF
                val g = (px shr 8) and 0xFF
                val b = px and 0xFF
                val lum = 0.299 * r + 0.587 * g + 0.114 * b
                totalLum += lum
                count++
            }
        }
        val avgLum = if (count > 0) totalLum / count else 0.0
        return if (avgLum < 128) Theme.DARK else Theme.LIGHT
    }

    private fun detectCandleColor(r: Int, g: Int, b: Int, theme: Theme): Int {
        val maxC = max(r, max(g, b))
        val minC = min(r, min(g, b))
        val saturation = if (maxC == 0) 0 else (maxC - minC) * 100 / maxC

        if (saturation < 20) return 0

        val isGreen = g > r && g > b && g - r > 25 && g - b > 25
        val isRed = r > g && r > b && r - g > 25 && r - b > 25

        return when {
            isGreen -> 1
            isRed -> -1
            else -> 0
        }
    }

    private fun findChartBounds(bitmap: Bitmap): ChartBounds {
        val width = bitmap.width
        val height = bitmap.height
        val top = (height * 0.06).toInt().coerceAtLeast(0)
        val bottom = (height * 0.94).toInt().coerceAtMost(height - 1)
        val left = (width * 0.01).toInt().coerceAtLeast(0)
        val right = (width * 0.88).toInt().coerceAtMost(width - 1)
        return ChartBounds(top, bottom, left, right)
    }

    fun findChartBoundsPublic(bitmap: Bitmap): ChartBounds = findChartBounds(bitmap)

    fun extractCandles(bitmap: Bitmap): List<CandlePixel>? {
        return try {
            val theme = detectTheme(bitmap)
            val bounds = findChartBounds(bitmap)
            val scanTop = bounds.chartTop
            val scanBottom = bounds.chartBottom
            val scanLeft = bounds.chartLeft
            val scanRight = bounds.chartRight

            if (scanBottom <= scanTop || scanRight <= scanLeft) return null

            val rowCount = scanBottom - scanTop
            val colCount = scanRight - scanLeft
            if (rowCount < 50 || colCount < 100) return null

            val pixels = IntArray(rowCount * colCount)
            bitmap.getPixels(pixels, 0, colCount, scanLeft, scanTop, colCount, rowCount)

            val columnColor = IntArray(colCount)
            for (cx in 0 until colCount) {
                var greenCount = 0
                var redCount = 0
                for (cy in 0 until rowCount) {
                    val px = pixels[cy * colCount + cx]
                    val r = (px shr 16) and 0xFF
                    val g = (px shr 8) and 0xFF
                    val b = px and 0xFF
                    val c = detectCandleColor(r, g, b, theme)
                    when (c) {
                        1 -> greenCount++
                        -1 -> redCount++
                    }
                }
                columnColor[cx] = when {
                    greenCount > 2 && redCount > 2 -> if (greenCount > redCount) 1 else -1
                    greenCount > 2 -> 1
                    redCount > 2 -> -1
                    else -> 0
                }
            }

            data class RawGroup(val start: Int, val end: Int, val color: Int)
            val rawGroups = mutableListOf<RawGroup>()
            var gStart = -1
            var gColor = 0
            for (cx in 0 until colCount) {
                val c = columnColor[cx]
                if (c != 0) {
                    if (gStart == -1) {
                        gStart = cx
                        gColor = c
                    } else if (c == gColor) {
                        // continue
                    } else {
                        rawGroups.add(RawGroup(gStart, cx - 1, gColor))
                        gStart = cx
                        gColor = c
                    }
                } else {
                    if (gStart != -1) {
                        rawGroups.add(RawGroup(gStart, cx - 1, gColor))
                        gStart = -1
                    }
                }
            }
            if (gStart != -1) rawGroups.add(RawGroup(gStart, colCount - 1, gColor))

            if (rawGroups.size < 10) return null

            val candles = mutableListOf<CandlePixel>()
            for (group in rawGroups) {
                if (group.end - group.start < 1) continue

                var topY = -1
                var bottomY = -1
                var bodyTopY = -1
                var bodyBottomY = -1
                var greenCount = 0
                var redCount = 0

                val width = group.end - group.start + 1

                for (cy in 0 until rowCount) {
                    var greenInRow = 0
                    var redInRow = 0
                    for (cx in group.start..group.end) {
                        val px = pixels[cy * colCount + cx]
                        val r = (px shr 16) and 0xFF
                        val g = (px shr 8) and 0xFF
                        val b = px and 0xFF
                        val c = detectCandleColor(r, g, b, theme)
                        when (c) {
                            1 -> { greenInRow++; greenCount++ }
                            -1 -> { redInRow++; redCount++ }
                        }
                    }
                    val totalInRow = greenInRow + redInRow
                    if (totalInRow > 0) {
                        if (topY == -1) topY = cy
                        bottomY = cy

                        if (totalInRow >= width * 0.5) {
                            if (bodyTopY == -1) bodyTopY = cy
                            bodyBottomY = cy
                        }
                    }
                }

                if (topY == -1 || bottomY <= topY) continue

                if (bodyTopY == -1) {
                    bodyTopY = topY
                    bodyBottomY = bottomY
                }

                val isBullish = greenCount >= redCount

                candles.add(
                    CandlePixel(
                        x = (group.start + group.end) / 2 + scanLeft,
                        top = topY + scanTop,
                        bottom = bottomY + scanTop,
                        bodyTop = bodyTopY + scanTop,
                        bodyBottom = bodyBottomY + scanTop,
                        isBullish = isBullish
                    )
                )
            }

            if (candles.size >= 10) candles else null
        } catch (e: Exception) {
            null
        }
    }

    fun pixelsToCandles(
        pixels: List<CandlePixel>,
        chartTop: Int,
        chartBottom: Int,
        priceHigh: Double,
        priceLow: Double
    ): List<Candle> {
        if (pixels.isEmpty()) return emptyList()
        val pixelRange = (chartBottom - chartTop).toDouble()
        if (pixelRange <= 0) return emptyList()
        val priceRange = priceHigh - priceLow

        fun pixelToPrice(y: Int): Double {
            val ratio = (chartBottom - y) / pixelRange
            return priceLow + ratio * priceRange
        }

        return pixels.map { p ->
            val high = pixelToPrice(p.top)
            val low = pixelToPrice(p.bottom)
            val bodyTopPrice = pixelToPrice(p.bodyTop)
            val bodyBottomPrice = pixelToPrice(p.bodyBottom)
            val open = if (p.isBullish) bodyBottomPrice else bodyTopPrice
            val close = if (p.isBullish) bodyTopPrice else bodyBottomPrice
            Candle(
                open = open,
                high = max(high, max(open, close)),
                low = min(low, min(open, close)),
                close = close,
                volume = 0.0
            )
        }
    }
}