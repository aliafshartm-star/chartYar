package com.chartyar.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.chartyar.app.analysis.AnalysisResult
import com.chartyar.app.analysis.SignalType
import com.chartyar.app.analysis.TrendType
import java.io.File
import java.io.FileOutputStream

object ShareImageHelper {

    private const val IMAGE_WIDTH = 1080
    private const val IMAGE_HEIGHT = 1920
    private const val BG_COLOR = 0xFF0D1117.toInt()
    private const val CARD_COLOR = 0xFF161B22.toInt()
    private const val TEXT_PRIMARY = 0xFFEAECEF.toInt()
    private const val TEXT_SECONDARY = 0xFF848E9C.toInt()
    private const val ACCENT_BLUE = 0xFF2962FF.toInt()
    private const val ACCENT_GREEN = 0xFF2EBD85.toInt()
    private const val ACCENT_RED = 0xFFF6465D.toInt()
    private const val ACCENT_YELLOW = 0xFFF0B90B.toInt()

    fun shareImage(context: Context, result: AnalysisResult) {
        try {
            val bitmap = generateImage(result)
            val file = saveImageToCache(context, bitmap)
            if (file != null) {
                shareFile(context, file)
            }
        } catch (e: Exception) {
        }
    }

    private fun generateImage(result: AnalysisResult): Bitmap {
        val bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(BG_COLOR)

        var y = 80f

        // ============ Header ============
        val titlePaint = Paint().apply {
            color = ACCENT_BLUE
            textSize = 72f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ChartYar", IMAGE_WIDTH / 2f, y, titlePaint)
        y += 60f

        val subtitlePaint = Paint().apply {
            color = TEXT_SECONDARY
            textSize = 32f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Technical Chart Analysis", IMAGE_WIDTH / 2f, y, subtitlePaint)
        y += 80f

        // ============ Signal Card ============
        val signalText = when (result.signal) {
            SignalType.STRONG_BUY -> "STRONG BUY"
            SignalType.BUY -> "BUY"
            SignalType.NEUTRAL -> "NEUTRAL"
            SignalType.SELL -> "SELL"
            SignalType.STRONG_SELL -> "STRONG SELL"
        }
        val signalColor = when (result.signal) {
            SignalType.STRONG_BUY -> ACCENT_GREEN
            SignalType.BUY -> ACCENT_GREEN
            SignalType.NEUTRAL -> ACCENT_YELLOW
            SignalType.SELL -> ACCENT_RED
            SignalType.STRONG_SELL -> ACCENT_RED
        }

        drawCard(canvas, 60f, y, IMAGE_WIDTH - 120f, 280f)

        val labelPaint = Paint().apply {
            color = TEXT_SECONDARY
            textSize = 32f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = TEXT_PRIMARY
            textSize = 64f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val signalPaint = Paint().apply {
            color = signalColor
            textSize = 72f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        canvas.drawText("Signal", 100f, y + 70f, labelPaint)
        canvas.drawText(signalText, 100f, y + 160f, signalPaint)

        canvas.drawText("Confidence", IMAGE_WIDTH - 400f, y + 70f, labelPaint)
        canvas.drawText("${result.confidence}%", IMAGE_WIDTH - 400f, y + 160f, valuePaint)

        val trendText = when (result.trend) {
            TrendType.UPTREND -> "Uptrend"
            TrendType.DOWNTREND -> "Downtrend"
            TrendType.RANGE -> "Ranging"
        }

        val smallPaint = Paint().apply {
            color = TEXT_PRIMARY
            textSize = 36f
            isAntiAlias = true
        }

        canvas.drawText("Trend: $trendText", 100f, y + 230f, smallPaint)
        canvas.drawText("TF: ${result.timeframe}", IMAGE_WIDTH - 300f, y + 230f, smallPaint)

        y += 320f

        // ============ Price Levels Card ============
        drawCard(canvas, 60f, y, IMAGE_WIDTH - 120f, 480f)

        val cardTitlePaint = Paint().apply {
            color = TEXT_PRIMARY
            textSize = 44f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Price Levels", 100f, y + 60f, cardTitlePaint)

        val rowLabelPaint = Paint().apply {
            color = TEXT_SECONDARY
            textSize = 36f
            isAntiAlias = true
        }
        val rowValuePaint = Paint().apply {
            color = TEXT_PRIMARY
            textSize = 36f
            isAntiAlias = true
            textAlign = Paint.Align.RIGHT
        }

        var rowY = y + 130f
        val rowGap = 55f

        drawRow(canvas, "Current Price", formatPrice(result.currentPrice), TEXT_PRIMARY, rowY, rowLabelPaint, rowValuePaint)
        rowY += rowGap
        drawRow(canvas, "Entry", formatPrice(result.entry), ACCENT_BLUE, rowY, rowLabelPaint, rowValuePaint)
        rowY += rowGap
        drawRow(canvas, "Stop Loss", formatPrice(result.stopLoss), ACCENT_RED, rowY, rowLabelPaint, rowValuePaint)
        rowY += rowGap
        drawRow(canvas, "Target 1", formatPrice(result.target1), ACCENT_GREEN, rowY, rowLabelPaint, rowValuePaint)
        rowY += rowGap
        drawRow(canvas, "Target 2", formatPrice(result.target2), ACCENT_GREEN, rowY, rowLabelPaint, rowValuePaint)
        rowY += rowGap
        drawRow(canvas, "Target 3", formatPrice(result.target3), ACCENT_GREEN, rowY, rowLabelPaint, rowValuePaint)

        y += 520f

        // ============ Risk/Reward ============
        drawCard(canvas, 60f, y, IMAGE_WIDTH - 120f, 120f)

        canvas.drawText("Risk / Reward", 100f, y + 70f, rowLabelPaint)

        val rrPaint = Paint().apply {
            color = if (result.riskReward >= 2.0) ACCENT_GREEN else ACCENT_YELLOW
            textSize = 48f
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("1 : ${"%.1f".format(result.riskReward)}", IMAGE_WIDTH - 100f, y + 75f, rrPaint)

        y += 160f

        // ============ Vision Trend Lines ============
        if (result.visionTrendLines.isNotEmpty()) {
            val linesCount = result.visionTrendLines.size.coerceAtMost(3)
            val cardHeight = 120f + linesCount * 60f
            drawCard(canvas, 60f, y, IMAGE_WIDTH - 120f, cardHeight)

            canvas.drawText("Vision Analysis", 100f, y + 60f, cardTitlePaint)

            val visionTrendText = when (result.visionDominantTrend) {
                1 -> "Bullish trend detected"
                -1 -> "Bearish trend detected"
                else -> "No clear trend"
            }
            val visionTrendColor = when (result.visionDominantTrend) {
                1 -> ACCENT_GREEN
                -1 -> ACCENT_RED
                else -> ACCENT_YELLOW
            }
            val visionPaint = Paint().apply {
                color = visionTrendColor
                textSize = 32f
                isAntiAlias = true
            }
            canvas.drawText(visionTrendText, 100f, y + 105f, visionPaint)

            var lineY = y + 165f
            result.visionTrendLines.take(3).forEach { line ->
                val lineType = if (line.isSupport) "Support" else "Resistance"
                val slopeType = when {
                    line.slope > 0.5 -> "Rising"
                    line.slope < -0.5 -> "Falling"
                    else -> "Flat"
                }
                val lineColor = if (line.isSupport) ACCENT_GREEN else ACCENT_RED

                val linePaint = Paint().apply {
                    color = lineColor
                    textSize = 32f
                    isAntiAlias = true
                }
                val strengthPaint = Paint().apply {
                    color = TEXT_SECONDARY
                    textSize = 30f
                    isAntiAlias = true
                    textAlign = Paint.Align.RIGHT
                }

                canvas.drawText("$lineType ($slopeType)", 140f, lineY, linePaint)
                canvas.drawText("strength ${line.strength}", IMAGE_WIDTH - 100f, lineY, strengthPaint)
                lineY += 55f
            }

            y += cardHeight + 40f
        }

        // ============ Patterns ============
        if (result.patterns.isNotEmpty()) {
            val patternsCount = result.patterns.size.coerceAtMost(2)
            val cardHeight = 110f + patternsCount * 60f
            drawCard(canvas, 60f, y, IMAGE_WIDTH - 120f, cardHeight)

            canvas.drawText("Detected Patterns", 100f, y + 60f, cardTitlePaint)

            var patY = y + 120f
            result.patterns.take(2).forEach { p ->
                val dirText = when (p.direction) {
                    1 -> "Bullish"
                    -1 -> "Bearish"
                    else -> "Neutral"
                }
                val dirColor = when (p.direction) {
                    1 -> ACCENT_GREEN
                    -1 -> ACCENT_RED
                    else -> ACCENT_YELLOW
                }

                val namePaint = Paint().apply {
                    color = TEXT_PRIMARY
                    textSize = 34f
                    isAntiAlias = true
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                val dirPaint = Paint().apply {
                    color = dirColor
                    textSize = 32f
                    isAntiAlias = true
                    textAlign = Paint.Align.RIGHT
                }

                canvas.drawText(p.name, 140f, patY, namePaint)
                canvas.drawText("$dirText ${p.confidence}%", IMAGE_WIDTH - 100f, patY, dirPaint)
                patY += 60f
            }

            y += cardHeight + 40f
        }

        // ============ Reasons (Top 3) ============
        if (result.reasons.isNotEmpty()) {
            val reasonsCount = result.reasons.size.coerceAtMost(3)
            val cardHeight = 110f + reasonsCount * 55f
            drawCard(canvas, 60f, y, IMAGE_WIDTH - 120f, cardHeight)

            canvas.drawText("Key Reasons", 100f, y + 60f, cardTitlePaint)

            val reasonPaint = Paint().apply {
                color = TEXT_PRIMARY
                textSize = 30f
                isAntiAlias = true
            }

            var rY = y + 120f
            result.reasons.take(3).forEach { r ->
                val shortReason = if (r.length > 55) r.substring(0, 55) + "..." else r
                canvas.drawText("• $shortReason", 120f, rY, reasonPaint)
                rY += 55f
            }

            y += cardHeight + 40f
        }

        // ============ Footer ============
        val footerPaint = Paint().apply {
            color = TEXT_SECONDARY
            textSize = 28f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ChartYar App", IMAGE_WIDTH / 2f, IMAGE_HEIGHT - 100f, footerPaint)
        canvas.drawText("t.me/chartyar_app", IMAGE_WIDTH / 2f, IMAGE_HEIGHT - 60f, footerPaint)

        val disclaimerPaint = Paint().apply {
            color = TEXT_SECONDARY
            textSize = 22f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Educational analysis - Not investment advice", IMAGE_WIDTH / 2f, IMAGE_HEIGHT - 25f, disclaimerPaint)

        return bitmap
    }

    private fun drawCard(canvas: Canvas, x: Float, y: Float, width: Float, height: Float) {
        val paint = Paint().apply {
            color = CARD_COLOR
            isAntiAlias = true
        }
        val rect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(rect, 24f, 24f, paint)
    }

    private fun drawRow(
        canvas: Canvas,
        label: String,
        value: String,
        valueColor: Int,
        y: Float,
        labelPaint: Paint,
        valuePaintBase: Paint
    ) {
        canvas.drawText(label, 140f, y, labelPaint)

        val valuePaint = Paint(valuePaintBase).apply {
            color = valueColor
        }
        canvas.drawText(value, IMAGE_WIDTH - 120f, y, valuePaint)
    }

    private fun formatPrice(price: Double): String {
        return "%.2f".format(price)
    }

    private fun saveImageToCache(context: Context, bitmap: Bitmap): File? {
        return try {
            val cacheDir = File(context.cacheDir, "shared_images").apply {
                if (!exists()) mkdirs()
            }
            val file = File(cacheDir, "analysis_${System.currentTimeMillis()}.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
            bitmap.recycle()
            file
        } catch (e: Exception) {
            null
        }
    }

    private fun shareFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share Analysis")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
        }
    }
}