package com.chartyar.app.analysis

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * خوندن اعداد قیمت از محور چارت با ML Kit
 */
object OcrHelper {

    data class PricePoint(
        val y: Int,
        val price: Double
    )

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * کل تصویر رو می‌خونه و اعداد قیمتی رو برمی‌گردونه
     */
    suspend fun recognizeText(bitmap: Bitmap): List<Pair<String, Int>> {
        return suspendCancellableCoroutine { cont ->
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val results = mutableListOf<Pair<String, Int>>()
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val text = line.text
                            val y = line.boundingBox?.centerY() ?: 0
                            results.add(text to y)
                        }
                    }
                    cont.resume(results)
                }
                .addOnFailureListener {
                    cont.resume(emptyList())
                }
        }
    }

    /**
     * استخراج قیمت‌ها از محور سمت راست تصویر
     * @param bitmap تصویر چارت
     * @return لیست نقاط قیمت (y پیکسل → قیمت)
     */
    suspend fun extractPriceAxis(bitmap: Bitmap): List<PricePoint> {
        val allText = recognizeText(bitmap)
        if (allText.isEmpty()) return emptyList()

        val width = bitmap.width
        val axisStartX = (width * 0.85).toInt() // فقط سمت راست تصویر

        val pricePoints = mutableListOf<PricePoint>()

        for ((text, y) in allText) {
            // فقط خطوطی که توی محدوده محور راست هستن
            // ولی چون ML Kit مختصات دقیق X رو جدا نمی‌ده، فقط متن رو بررسی می‌کنیم
            val price = parsePrice(text)
            if (price != null) {
                pricePoints.add(PricePoint(y, price))
            }
        }

        // حذف تکراری‌ها (نزدیک به هم) و مرتب‌سازی
        return pricePoints
            .distinctBy { it.y / 20 }
            .sortedBy { it.y }
    }

    /**
     * تبدیل متن به عدد قیمت
     * مثال‌ها: "67000", "67,000", "67.5K", "67000.50", "٦٧٠٠٠"
     */
    private fun parsePrice(text: String): Double? {
        // پاکسازی: حذف فاصله، کاما، حروف اضافه
        var cleaned = text
            .replace(",", "")
            .replace(" ", "")
            .replace("٫", ".")
            .trim()

        // حذف کاراکترهای غیرعددی به جز نقطه و منفی
        cleaned = cleaned.filter { it.isDigit() || it == '.' || it == '-' }

        if (cleaned.isEmpty()) return null

        // پشتیبانی از K, M (مثلاً 67.5K)
        val originalUpper = text.uppercase()
        val multiplier = when {
            originalUpper.contains("K") -> 1000.0
            originalUpper.contains("M") -> 1_000_000.0
            originalUpper.contains("B") -> 1_000_000_000.0
            else -> 1.0
        }

        val number = cleaned.toDoubleOrNull() ?: return null
        val result = number * multiplier

        // فیلتر: قیمت باید معقول باشه (> 0.0001)
        return if (result > 0.0001 && result < 100_000_000) result else null
    }

    /**
     * محاسبه بازه قیمتی (high/low) از نقاط محور
     */
    fun calculatePriceRange(points: List<PricePoint>): Pair<Double, Double>? {
        if (points.size < 2) return null
        val sorted = points.sortedBy { it.y }
        val topPrice = sorted.first().price
        val bottomPrice = sorted.last().price
        return if (topPrice > bottomPrice) topPrice to bottomPrice else bottomPrice to topPrice
    }
}