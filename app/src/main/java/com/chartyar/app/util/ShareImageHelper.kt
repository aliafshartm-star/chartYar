package com.chartyar.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.chartyar.app.analysis.AnalysisResult
import com.chartyar.app.analysis.SignalType
import com.chartyar.app.analysis.TrendType
import java.io.File
import java.io.FileOutputStream

/**
 * Helper برای ساخت تصویر از تحلیل و اشتراک‌گذاری
 */
object ShareImageHelper {

    /**
     * ساخت Bitmap از تحلیل
     */
    fun generateAnalysisBitmap(result: AnalysisResult): Bitmap {
        val width = 1080
        val height = 1920
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#0D1117"))

        // تنظیمات Paint
        val titlePaint = Paint().apply {
            color = Color.parseColor("#2962FF")
            textSize = 70f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val labelPaint = Paint().apply {
            color = Color.parseColor("#848E9C")
            textSize = 36f
            isAntiAlias = true
        }

        val valuePaint = Paint().apply {
            color = Color.parseColor("#EAECEF")
            textSize = 50f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val signalPaint = Paint().apply {
            textSize = 90f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        var y = 140f

        // هدر
        canvas.drawText("ChartYar - تحلیل چارت", 60f, y, titlePaint)
        y += 50f
        canvas.drawLine(60f, y, width - 60f, y, Paint().apply {
            color = Color.parseColor("#2B3139")
            strokeWidth = 2f
        })
        y += 100f

        // سیگنال
        val (signalText, signalColor) = when (result.signal) {
            SignalType.STRONG_BUY -> "خرید قوی" to "#2EBD85"
            SignalType.BUY -> "خرید" to "#2EBD85"
            SignalType.NEUTRAL -> "خنثی" to "#F0B90B"
            SignalType.SELL -> "فروش" to "#F6465D"
            SignalType.STRONG_SELL -> "فروش قوی" to "#F6465D"
        }
        signalPaint.color = Color.parseColor(signalColor)

        canvas.drawText("سیگنال:", 60f, y, labelPaint)
        y += 100f
        canvas.drawText(signalText, 60f, y, signalPaint)
        y += 140f

        // اطمینان و روند
        canvas.drawText("اطمینان:", 60f, y, labelPaint)
        canvas.drawText("${result.confidence}%", 60f, y + 60f, valuePaint)

        canvas.drawText("روند:", 500f, y, labelPaint)
        val trendText = when (result.trend) {
            TrendType.UPTREND -> "صعودی"
            TrendType.DOWNTREND -> "نزولی"
            TrendType.RANGE -> "رنج"
        }
        canvas.drawText(trendText, 500f, y + 60f, valuePaint)

        canvas.drawText("تایم‌فریم:", 800f, y, labelPaint)
        canvas.drawText(result.timeframe, 800f, y + 60f, valuePaint)

        y += 200f

        // خط جداکننده
        canvas.drawLine(60f, y, width - 60f, y, Paint().apply {
            color = Color.parseColor("#2B3139")
            strokeWidth = 2f
        })
        y += 80f

        // قیمت‌ها
        canvas.drawText("قیمت فعلی:", 60f, y, labelPaint)
        canvas.drawText("%.2f".format(result.currentPrice), 60f, y + 60f, valuePaint)
        y += 150f

        canvas.drawText("نقطه ورود:", 60f, y, labelPaint)
        canvas.drawText("%.2f".format(result.entry), 60f, y + 60f, Paint().apply {
            color = Color.parseColor("#2962FF")
            textSize = 50f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        })
        y += 150f

        canvas.drawText("حد ضرر:", 60f, y, labelPaint)
        canvas.drawText("%.2f".format(result.stopLoss), 60f, y + 60f, Paint().apply {
            color = Color.parseColor("#F6465D")
            textSize = 50f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        })
        y += 150f

        canvas.drawText("هدف اول:", 60f, y, labelPaint)
        canvas.drawText("%.2f".format(result.target1), 60f, y + 60f, Paint().apply {
            color = Color.parseColor("#2EBD85")
            textSize = 50f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        })
        y += 150f

        canvas.drawText("هدف دوم:", 60f, y, labelPaint)
        canvas.drawText("%.2f".format(result.target2), 60f, y + 60f, Paint().apply {
            color = Color.parseColor("#2EBD85")
            textSize = 50f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        })
        y += 150f

        canvas.drawText("هدف سوم:", 60f, y, labelPaint)
        canvas.drawText("%.2f".format(result.target3), 60f, y + 60f, Paint().apply {
            color = Color.parseColor("#2EBD85")
            textSize = 50f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        })
        y += 150f

        // R:R
        canvas.drawText("نسبت R:R:", 60f, y, labelPaint)
        canvas.drawText("1 : ${"%.1f".format(result.riskReward)}", 60f, y + 60f, valuePaint)
        y += 180f

        // خط جداکننده
        canvas.drawLine(60f, y, width - 60f, y, Paint().apply {
            color = Color.parseColor("#2B3139")
            strokeWidth = 2f
        })
        y += 80f

        // دلایل (فقط ۳ تا اول)
        canvas.drawText("دلایل تحلیل:", 60f, y, labelPaint)
        y += 70f

        result.reasons.take(3).forEach { reason ->
            val truncated = if (reason.length > 45) reason.substring(0, 45) + "..." else reason
            canvas.drawText("• $truncated", 80f, y, Paint().apply {
                color = Color.parseColor("#EAECEF")
                textSize = 38f
                isAntiAlias = true
            })
            y += 60f
        }

        // فوتر
        val footerPaint = Paint().apply {
            color = Color.parseColor("#848E9C")
            textSize = 32f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            "ساخته‌شده با چارت‌یار | این تحلیل توصیه سرمایه‌گذاری نیست",
            width / 2f,
            height - 80f,
            footerPaint
        )

        return bitmap
    }

    /**
     * اشتراک‌گذاری تصویر تحلیل
     */
    fun shareImage(context: Context, result: AnalysisResult) {
        try {
            val bitmap = generateAnalysisBitmap(result)

            // ذخیره در cache
            val cacheDir = File(context.cacheDir, "shared_images").apply {
                if (!exists()) mkdirs()
            }
            val imageFile = File(cacheDir, "chartyar_analysis_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            // ساخت Uri
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            // Intent اشتراک‌گذاری
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "تحلیل چارت با چارت‌یار")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "اشتراک‌گذاری تصویر تحلیل")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            // silent
        }
    }
}