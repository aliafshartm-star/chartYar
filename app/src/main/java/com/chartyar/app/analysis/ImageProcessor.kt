package com.chartyar.app.analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.InputStream

object ImageProcessor {

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
                if (it.config != Bitmap.Config.ARGB_8888) {
                    val converted = it.copy(Bitmap.Config.ARGB_8888, false)
                    it.recycle()
                    converted
                } else it
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun detectCandleColor(r: Int, g: Int, b: Int): Int {
        val isGreen = g > 90 && g > r + 15 && g > b + 15
        val isRed = r > 90 && r > g + 15 && r > b + 15
        return when {
            isGreen -> 1
            isRed -> -1
            else -> 0
        }
    }

    data class CandlePixel(
        val x: Int,
        val top: Int,
        val bottom: Int,
        val isBullish: Boolean
    )

    data class ChartBounds(
        val chartTop: Int,
        val chartBottom: Int,
        val chartLeft: Int,
        val chartRight: Int
    )

    private fun findChartBounds(bitmap: Bitmap): ChartBounds {
        val width = bitmap.width
        val height = bitmap.height
        val top = (height * 0.08).toInt().coerceAtLeast(0)
        val bottom = (height * 0.92).toInt().coerceAtMost(height - 1)
        val left = (width * 0.02).toInt().coerceAtLeast(0)
        val right = (width * 0.88).toInt().coerceAtMost(width - 1)
        return ChartBounds(top, bottom, left, right)
    }

    fun extractCandles(bitmap: Bitmap): List<CandlePixel>? {
        return try {
            val bounds = findChartBounds(bitmap)
            val scanTop = bounds.chartTop
            val scanBottom = bounds.chartBottom
            val scanLeft = bounds.chartLeft
            val scanRight = bounds.chartRight

            if (scanBottom <= scanTop || scanRight <= scanLeft) return null

            val rowCount = scanBottom - scanTop
            val colCount = scanRight - scanLeft
            val pixels = IntArray(rowCount * colCount)
            bitmap.getPixels(
                pixels,
                0,
                colCount,
                scanLeft,
                scanTop,
                colCount,
                rowCount
            )

            val columnHasCandle = BooleanArray(colCount)
            for (cx in 0 until colCount) {
                var colorCount = 0
                for (cy in 0 until rowCount) {
                    val px = pixels[cy * colCount + cx]
                    val r = (px shr 16) and 0xFF
                    val g = (px shr 8) and 0xFF
                    val b = px and 0xFF
                    if (detectCandleColor(r, g, b) != 0) colorCount++
                    if (colorCount > 2) break
                }
                columnHasCandle[cx] = colorCount > 2
            }

            val groups = mutableListOf<Pair<Int, Int>>()
            var start = -1
            var last = -1
            for (cx in 0 until colCount) {
                if (columnHasCandle[cx]) {
                    if (start == -1) {
                        start = cx
                        last = cx
                    } else if (cx - last <= 3) {
                        last = cx
                    } else {
                        groups.add(start to last)
                        start = cx
                        last = cx
                    }
                }
            }
            if (start != -1) groups.add(start to last)

            if (groups.size < 10) return null

            val candles = mutableListOf<CandlePixel>()
            for ((gStart, gEnd) in groups) {
                if (gEnd - gStart < 1) continue

                var topY = -1
                var bottomY = -1
                var greenCount = 0
                var redCount = 0

                for (cy in 0 until rowCount) {
                    for (cx in gStart..gEnd) {
                        val px = pixels[cy * colCount + cx]
                        val r = (px shr 16) and 0xFF
                        val g = (px shr 8) and 0xFF
                        val b = px and 0xFF
                        val c = detectCandleColor(r, g, b)
                        if (c != 0) {
                            if (topY == -1) topY = cy
                            bottomY = cy
                            if (c == 1) greenCount++ else redCount++
                        }
                    }
                }

                if (topY != -1 && bottomY > topY) {
                    candles.add(
                        CandlePixel(
                            x = (gStart + gEnd) / 2 + scanLeft,
                            top = topY + scanTop,
                            bottom = bottomY + scanTop,
                            isBullish = greenCount >= redCount
                        )
                    )
                }
            }

            if (candles.size >= 10) candles else null
        } catch (e: Exception) {
            e.printStackTrace()
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
            val open = if (p.isBullish) low else high
            val close = if (p.isBullish) high else low
            Candle(
                open = open,
                high = high,
                low = low,
                close = close,
                volume = 0.0
            )
        }
    }
}