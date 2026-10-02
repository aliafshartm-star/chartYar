package com.chartyar.app.util

import android.content.Context
import android.content.Intent
import com.chartyar.app.analysis.AnalysisResult
import com.chartyar.app.analysis.SignalType
import com.chartyar.app.analysis.TrendType

object ShareHelper {

    fun buildShareText(result: AnalysisResult): String {
        val sb = StringBuilder()

        sb.appendLine("📊 تحلیل چارت‌یار")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine()

        // Signal
        val signalText = when (result.signal) {
            SignalType.STRONG_BUY -> "🟢 خرید قوی"
            SignalType.BUY -> "🟢 خرید"
            SignalType.NEUTRAL -> "🟡 خنثی"
            SignalType.SELL -> "🔴 فروش"
            SignalType.STRONG_SELL -> "🔴 فروش قوی"
        }
        sb.appendLine("سیگنال: $signalText")
        sb.appendLine("اطمینان: ${result.confidence}%")

        val trendText = when (result.trend) {
            TrendType.UPTREND -> "صعودی"
            TrendType.DOWNTREND -> "نزولی"
            TrendType.RANGE -> "رنج"
        }
        sb.appendLine("روند: $trendText")
        sb.appendLine("تایم‌فریم: ${result.timeframe}")
        sb.appendLine()

        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("💰 سطوح معاملاتی")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("قیمت فعلی: ${"%.2f".format(result.currentPrice)}")
        sb.appendLine("نقطه ورود: ${"%.2f".format(result.entry)}")
        sb.appendLine("حد ضرر: ${"%.2f".format(result.stopLoss)}")
        sb.appendLine("هدف اول: ${"%.2f".format(result.target1)}")
        sb.appendLine("هدف دوم: ${"%.2f".format(result.target2)}")
        sb.appendLine("هدف سوم: ${"%.2f".format(result.target3)}")
        sb.appendLine("ریسک به ریوارد: 1 : ${"%.1f".format(result.riskReward)}")
        sb.appendLine()

        // Patterns
        if (result.patterns.isNotEmpty()) {
            sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")
            sb.appendLine("🔍 الگوهای شناسایی‌شده")
            sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")
            result.patterns.take(3).forEach { p ->
                val dir = when (p.direction) {
                    1 -> "صعودی"
                    -1 -> "نزولی"
                    else -> "خنثی"
                }
                sb.appendLine("• ${p.name} ($dir - ${p.confidence}%)")
            }
            sb.appendLine()
        }

        // Vision Analysis
        if (result.visionTrendLines.isNotEmpty() || result.visionDominantTrend != 0) {
            sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")
            sb.appendLine("👁️ تحلیل بینایی چارت")
            sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")

            val visionTrendText = when (result.visionDominantTrend) {
                1 -> "روند صعودی"
                -1 -> "روند نزولی"
                else -> "بدون روند مشخص"
            }
            sb.appendLine("روند کلی: $visionTrendText")

            if (result.visionTrendLines.isNotEmpty()) {
                sb.appendLine("خطوط روند:")
                result.visionTrendLines.forEach { line ->
                    val lineType = if (line.isSupport) "حمایت" else "مقاومت"
                    val slopeType = when {
                        line.slope > 0.5 -> "صعودی"
                        line.slope < -0.5 -> "نزولی"
                        else -> "افقی"
                    }
                    sb.appendLine("• $lineType $slopeType (قدرت ${line.strength})")
                }
            }
            sb.appendLine()
        }

        // Reasons (top 5)
        if (result.reasons.isNotEmpty()) {
            sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")
            sb.appendLine("✅ دلایل تحلیل")
            sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")
            result.reasons.take(5).forEach { reason ->
                sb.appendLine("• $reason")
            }
            sb.appendLine()
        }

        // Warnings
        if (result.warnings.isNotEmpty()) {
            sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")
            sb.appendLine("⚠️ هشدارها")
            sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")
            result.warnings.forEach { warning ->
                sb.appendLine("• $warning")
            }
            sb.appendLine()
        }

        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("📱 اپ چارت‌یار")
        sb.appendLine("https://t.me/chartyar_app")
        sb.appendLine()
        sb.appendLine("⚠️ این تحلیل توصیه سرمایه‌گذاری نیست.")

        return sb.toString()
    }

    fun shareText(context: Context, text: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            val chooser = Intent.createChooser(intent, "اشتراک‌گذاری تحلیل")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
        }
    }
}