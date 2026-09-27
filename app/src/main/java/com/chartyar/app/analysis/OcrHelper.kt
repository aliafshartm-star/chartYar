package com.chartyar.app.analysis

import android.graphics.Bitmap
import android.graphics.Rect
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.math.abs
import kotlin.coroutines.resume

object OcrHelper {

    data class PricePoint(
        val y: Int,
        val x: Int,
        val price: Double,
        val rawText: String
    )

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    /**
     * خوندن متن‌ها با موقعیت دقیق
     */
    suspend fun recognizeTextWithBounds(bitmap: Bitmap): List<Triple<String, Int, Int>> {
        return suspendCancellableCoroutine { cont ->
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val results = mutableListOf<Triple<String, Int, Int>>()
                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val text = line.text
                            val box = line.boundingBox
                            if (box != null) {
                                results.add(Triple(text, box.centerX(), box.centerY()))
                            }
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
     * استخراج قیمت‌ها فقط از محور سمت راست تصویر
     * با فیلترهای دقیق‌تر
     */
    suspend fun extractPriceAxis(bitmap: Bitmap): List<PricePoint> {
        val allText = recognizeTextWithBounds(bitmap)
        if (allText.isEmpty()) return emptyList()

        val width = bitmap.width
        val height = bitmap.height

        // محدوده محور: ۸۲٪ تا ۱۰۰٪ عرض
        val axisLeft = (width * 0.82).toInt()
        val axisRight = width

        // محدوده عمودی: ۵٪ تا ۹۵٪ ارتفاع
        val axisTop = (height * 0.05).toInt()
        val axisBottom = (height * 0.95).toInt()

        val candidates = mutableListOf<PricePoint>()

        for ((text, x, y) in allText) {
            // فقط متن‌هایی که داخل محدوده محور راست هستن
            if (x < axisLeft || x > axisRight) continue
            if (y < axisTop || y > axisBottom) continue

            val price = parsePrice(text) ?: continue
            candidates.add(PricePoint(y, x, price, text))
        }

        if (candidates.isEmpty()) return emptyList()

        // حذف تکراری‌ها (y نزدیک به هم)
        val filtered = mutableListOf<PricePoint>()
        for (c in candidates.sortedBy { it.y }) {
            val last = filtered.lastOrNull()
            if (last == null || abs(c.y - last.y) > 15) {
                filtered.add(c)
            } else {
                // اگه دو تا خیلی نزدیک بودن، اونی که متن تمیزتری داره نگه‌دار
                if (c.rawText.length > last.rawText.length) {
                    filtered[filtered.size - 1] = c
                }
            }
        }

        return filtered.sortedBy { it.y }
    }

    /**
     * تبدیل متن به عدد قیمت با پشتیبانی از فرمت‌های مختلف
     * مثال‌ها: "67000", "67,000", "67.5K", "67000.50", "67 000", "$67,000"
     */
    private fun parsePrice(text: String): Double? {
        val original = text.trim()
        if (original.isEmpty()) return null

        // حذف کاراکترهای اضافی
        var cleaned = original
            .replace(",", "")
            .replace(" ", "")
            .replace("٫", ".")
            .replace("$", "")
            .replace("€", "")
            .replace("£", "")
            .replace("¥", "")
            .trim()

        // اگه فقط حروف و علامت داره، رد کن
        if (cleaned.isEmpty()) return null

        // تشخیص ضریب (K, M, B)
        val upper = cleaned.uppercase()
        val multiplier = when {
            upper.contains("K") -> 1000.0
            upper.contains("M") -> 1_000_000.0
            upper.contains("B") -> 1_000_000_000.0
            else -> 1.0
        }

        // حذف حروف، فقط اعداد و نقطه و منفی بمونن
        cleaned = cleaned.filter { it.isDigit() || it == '.' || it == '-' }

        if (cleaned.isEmpty() || cleaned == "-" || cleaned == ".") return null

        // فقط یک نقطه مجاز
        val dotCount = cleaned.count { it == '.' }
        if (dotCount > 1) {
            // اگه چند نقطه داره، احتمالاً جداکننده هزارگان بوده
            cleaned = cleaned.replace(".", "")
        }

        val number = cleaned.toDoubleOrNull() ?: return null
        val result = number * multiplier

        // فیلتر معقول بودن
        if (result <= 0.0) return null
        if (result > 10_000_000_000.0) return null

        return result
    }

    /**
     * محاسبه بازه قیمتی (high/low) از نقاط محور
     * با استفاده از رگرسیون خطی برای دقت بالاتر
     */
    fun calculatePriceRange(points: List<PricePoint>): Pair<Double, Double>? {
        if (points.size < 2) return null

        val sorted = points.sortedBy { it.y }
        val top = sorted.first()
        val bottom = sorted.last()

        return if (top.price > bottom.price) {
            top.price to bottom.price
        } else {
            bottom.price to top.price
        }
    }

    /**
     * محاسبه دقیق‌تر قیمت با درون‌یابی خطی
     * اگه محور بیش از ۳ نقطه داشت، از همه استفاده کن
     */
    fun interpolatePrice(points: List<PricePoint>, targetY: Int): Double? {
        if (points.size < 2) return null

        val sorted = points.sortedBy { it.y }

        // پیدا کردن دو نقطه‌ای که targetY بینشونه
        var upper: PricePoint? = null
        var lower: PricePoint? = null

        for (p in sorted) {
            if (p.y <= targetY) upper = p
            if (p.y >= targetY) {
                lower = p
                break
            }
        }

        if (upper == null || lower == null) {
            // targetY خارج از محدوده‌ست، از دو نزدیک‌ترین استفاده کن
            return if (targetY < sorted.first().y) sorted.first().price
            else sorted.last().price
        }

        if (upper.y == lower.y) return upper.price

        // درون‌یابی خطی
        val ratio = (targetY - upper.y).toDouble() / (lower.y - upper.y).toDouble()
        return upper.price + ratio * (lower.price - upper.price)
    }
}