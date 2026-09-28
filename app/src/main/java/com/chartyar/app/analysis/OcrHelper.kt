package com.chartyar.app.analysis

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.coroutines.resume

object OcrHelper {

    data class PricePoint(
        val y: Int,
        val x: Int,
        val price: Double,
        val rawText: String,
        val isAxisLabel: Boolean
    )

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

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
     * تشخیص اعداد محور (رند .00) vs اعداد باکس (اعشاری)
     */
    private fun isAxisLabelNumber(text: String): Boolean {
        val cleaned = text.trim().replace(",", "")
        // محور: "80.00", "90.00", "100.00", "130.00", "80", "90"
        // باکس: "118.37", "118.36", "03:58:26"
        if (cleaned.contains(":")) return false // زمان
        val regex = Regex("^\\d+\\.?\\d*$")
        if (!regex.matches(cleaned)) return false

        val parts = cleaned.split(".")
        if (parts.size == 2) {
            val decimal = parts[1]
            // محور: .00 یا .0 (رند)
            // باکس: .37, .36 (غیر رند)
            if (decimal.length >= 2) {
                val decValue = decimal.toIntOrNull() ?: return false
                return decValue == 0 // فقط .00 قبوله
            }
        }
        return true // اعداد ساده مثل "80", "90"
    }

    suspend fun extractPriceAxis(bitmap: Bitmap): List<PricePoint> {
        val allText = recognizeTextWithBounds(bitmap)
        if (allText.isEmpty()) return emptyList()

        val width = bitmap.width
        val height = bitmap.height

        // محور سمت راست: ۸۵٪ تا ۱۰۰٪ عرض
        val axisLeft = (width * 0.85).toInt()
        // محدوده عمودی: ۳٪ تا ۹۷٪
        val axisTop = (height * 0.03).toInt()
        val axisBottom = (height * 0.97).toInt()

        val candidates = mutableListOf<PricePoint>()

        for ((text, x, y) in allText) {
            if (x < axisLeft) continue
            if (y < axisTop || y > axisBottom) continue

            val price = parsePrice(text) ?: continue
            val isAxis = isAxisLabelNumber(text)

            candidates.add(PricePoint(y, x, price, text, isAxis))
        }

        if (candidates.isEmpty()) return emptyList()

        // فیلتر: فقط محور
        val axisOnly = candidates.filter { it.isAxisLabel }

        // اگه کمتر از ۳ نقطه محور داریم، از همه استفاده کن
        val pointsToUse = if (axisOnly.size >= 3) axisOnly else candidates

        // حذف تکراری‌های نزدیک
        val filtered = mutableListOf<PricePoint>()
        for (c in pointsToUse.sortedBy { it.y }) {
            val last = filtered.lastOrNull()
            if (last == null || abs(c.y - last.y) > 15) {
                filtered.add(c)
            } else {
                // اگه دو تا نزدیک، اون که عدد رندتره رو نگه‌دار
                if (c.isAxisLabel && !last.isAxisLabel) {
                    filtered[filtered.size - 1] = c
                }
            }
        }

        return filtered.sortedBy { it.y }
    }

    private fun parsePrice(text: String): Double? {
        val original = text.trim()
        if (original.isEmpty()) return null

        var cleaned = original
            .replace(",", "")
            .replace(" ", "")
            .replace("٫", ".")
            .replace("$", "")
            .replace("€", "")
            .replace("£", "")
            .replace("¥", "")
            .trim()

        if (cleaned.isEmpty()) return null
        if (cleaned.contains(":")) return null // زمان

        val upper = cleaned.uppercase()
        val multiplier = when {
            upper.contains("K") -> 1000.0
            upper.contains("M") -> 1_000_000.0
            upper.contains("B") -> 1_000_000_000.0
            else -> 1.0
        }

        cleaned = cleaned.filter { it.isDigit() || it == '.' || it == '-' }

        if (cleaned.isEmpty() || cleaned == "-" || cleaned == ".") return null

        val dotCount = cleaned.count { it == '.' }
        if (dotCount > 1) {
            cleaned = cleaned.replace(".", "")
        }

        val number = cleaned.toDoubleOrNull() ?: return null
        val result = number * multiplier

        if (result <= 0.0) return null
        if (result > 10_000_000_000.0) return null

        return result
    }

    /**
     * محاسبه بازه قیمتی با رگرسیون خطی (Line Fit)
     * این روش دقیق‌تره چون از همه نقاط استفاده می‌کنه
     */
    fun calculatePriceRange(points: List<PricePoint>): Pair<Double, Double>? {
        if (points.size < 2) return null

        // اگه فقط ۲ نقطه داریم، ساده
        if (points.size == 2) {
            val sorted = points.sortedBy { it.y }
            val top = sorted.first()
            val bottom = sorted.last()
            return if (top.price > bottom.price) {
                top.price to bottom.price
            } else {
                bottom.price to top.price
            }
        }

        // رگرسیون خطی: price = a * y + b
        // y = pixel position, price = actual price
        val n = points.size.toDouble()
        val sumY = points.sumOf { it.y.toDouble() }
        val sumP = points.sumOf { it.price }
        val sumYP = points.sumOf { it.y.toDouble() * it.price }
        val sumYY = points.sumOf { it.y.toDouble() * it.y.toDouble() }

        val denominator = n * sumYY - sumY * sumY
        if (abs(denominator) < 0.0001) return null

        val a = (n * sumYP - sumY * sumP) / denominator
        val b = (sumP - a * sumY) / n

        // محاسبه R² برای اعتبارسنجی
        val meanP = sumP / n
        var ssTotal = 0.0
        var ssResidual = 0.0
        for (p in points) {
            val predicted = a * p.y + b
            ssTotal += (p.price - meanP) * (p.price - meanP)
            ssResidual += (p.price - predicted) * (p.price - predicted)
        }
        val rSquared = if (ssTotal > 0) 1.0 - (ssResidual / ssTotal) else 0.0

        // اگه R² خیلی پایین بود، احتمالاً نقاط پرت داریم
        // فیلتر مجدد: نقاطی که خیلی از خط فاصله دارن رو حذف کن
        if (rSquared < 0.99) {
            val filtered = points.filter { p ->
                val predicted = a * p.y + b
                val error = abs(p.price - predicted) / p.price
                error < 0.02 // خطای کمتر از ۲٪
            }
            if (filtered.size >= 2 && filtered.size < points.size) {
                return calculatePriceRange(filtered)
            }
        }

        // بازه: بالاترین و پایین‌ترین نقطه از خط
        val minY = points.minOf { it.y }
        val maxY = points.maxOf { it.y }
        val priceAtMinY = a * minY + b
        val priceAtMaxY = a * maxY + b

        return if (priceAtMinY > priceAtMaxY) {
            priceAtMinY to priceAtMaxY
        } else {
            priceAtMaxY to priceAtMinY
        }
    }

    /**
     * تبدیل Y پیکسل به قیمت با استفاده از همه نقاط
     */
    fun pixelToPrice(points: List<PricePoint>, targetY: Int): Double? {
        if (points.size < 2) return null

        val n = points.size.toDouble()
        val sumY = points.sumOf { it.y.toDouble() }
        val sumP = points.sumOf { it.price }
        val sumYP = points.sumOf { it.y.toDouble() * it.price }
        val sumYY = points.sumOf { it.y.toDouble() * it.y.toDouble() }

        val denominator = n * sumYY - sumY * sumY
        if (abs(denominator) < 0.0001) return null

        val a = (n * sumYP - sumY * sumP) / denominator
        val b = (sumP - a * sumY) / n

        return a * targetY + b
    }

    fun calculatePriceRangeSimple(points: List<PricePoint>): Pair<Double, Double>? {
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
}