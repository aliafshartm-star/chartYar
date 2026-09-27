package com.chartyar.app.util

import android.content.Context
import android.content.Intent
import com.chartyar.app.analysis.AnalysisResult
import com.chartyar.app.analysis.SignalType
import com.chartyar.app.analysis.TrendType

object ShareHelper {

    /**
     * ساخت متن اشتراک‌گذاری از تحلیل
     */
    fun buildShareText(result: AnalysisResult): String {
        val signalText = when (result.signal) {
            SignalType.STRONG_BUY -> "خرید قوی 🟢"
            SignalType.BUY -> "خرید 🟢"
            SignalType.NEUTRAL -> "خنثی 🟡"
            SignalType.SELL -> "فروش 🔴"
            SignalType.STRONG_SELL -> "فروش قوی 🔴"
        }

        val trendText = when (result.trend) {
            TrendType.UPTREND -> "صعودی"
            TrendType.DOWNTREND -> "نزولی"
            TrendType.RANGE -> "رنج"
        }

        val sb = StringBuilder()
        sb.appendLine("📊 تحلیل چارت با چارت‌یار")
        sb.appendLine("━━━━━━━━━━━━━━━━━━")
        sb.appendLine("⏱ تایم‌فریم: ${result.timeframe}")
        sb.appendLine("📈 روند کلی: $trendText")
        sb.appendLine("🎯 سیگنال: $signalText")
        sb.appendLine("💯 اطمینان: ${result.confidence}%")
        sb.appendLine("━━━━━━━━━━━━━━━━━━")
        sb.appendLine("💰 قیمت فعلی: ${"%.2f".format(result.currentPrice)}")
        sb.appendLine("🚪 نقطه ورود: ${"%.2f".format(result.entry)}")
        sb.appendLine("🛑 حد ضرر: ${"%.2f".format(result.stopLoss)}")
        sb.appendLine("🎯 هدف اول: ${"%.2f".format(result.target1)}")
        sb.appendLine("🎯 هدف دوم: ${"%.2f".format(result.target2)}")
        sb.appendLine("🎯 هدف سوم: ${"%.2f".format(result.target3)}")
        sb.appendLine("⚖️ نسبت R:R: 1:${"%.1f".format(result.riskReward)}")

        if (result.patterns.isNotEmpty()) {
            sb.appendLine("━━━━━━━━━━━━━━━━━━")
            sb.appendLine("🔍 الگوهای شناسایی‌شده:")
            result.patterns.take(3).forEach { pattern ->
                val dirEmoji = when (pattern.direction) {
                    1 -> "🟢"
                    -1 -> "🔴"
                    else -> "🟡"
                }
                sb.appendLine("  $dirEmoji ${pattern.name}")
            }
        }

        if (result.reasons.isNotEmpty()) {
            sb.appendLine("━━━━━━━━━━━━━━━━━━")
            sb.appendLine("📋 دلایل تحلیل:")
            result.reasons.take(5).forEach { reason ->
                sb.appendLine("  • $reason")
            }
        }

        sb.appendLine("━━━━━━━━━━━━━━━━━━")
        sb.appendLine("⚠️ این تحلیل توصیه سرمایه‌گذاری نیست.")
        sb.appendLine("ساخته‌شده با چارت‌یار")

        return sb.toString()
    }

    /**
     * اشتراک‌گذاری متن
     */
    fun shareText(context: Context, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "تحلیل چارت - چارت‌یار")
        }
        val chooser = Intent.createChooser(intent, "اشتراک‌گذاری تحلیل")
        context.startActivity(chooser)
    }
}