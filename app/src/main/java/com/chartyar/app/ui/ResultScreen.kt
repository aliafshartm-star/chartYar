package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.analysis.AnalysisResult
import com.chartyar.app.analysis.SignalType
import com.chartyar.app.analysis.TrendType
import com.chartyar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    result: AnalysisResult,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // هدر
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "بازگشت", tint = TextPrimary)
            }
            Text(
                "نتیجه تحلیل",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // کارت سیگنال
        SignalCard(result)

        Spacer(Modifier.height(12.dp))

        // کارت قیمت‌ها
        PriceLevelsCard(result)

        Spacer(Modifier.height(12.dp))

        // کارت دلایل
        ReasonsCard(result.reasons, "دلایل تحلیل", AccentGreen)

        if (result.warnings.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            ReasonsCard(result.warnings, "هشدارها", AccentYellow)
        }

        Spacer(Modifier.height(12.dp))

        // سطوح حمایت و مقاومت
        LevelsCard(result)

        Spacer(Modifier.height(12.dp))

        // امتیاز اندیکاتورها
        ScoresCard(result)

        Spacer(Modifier.height(24.dp))

        // یادآوری
        Text(
            text = "⚠️ این تحلیل صرفاً جنبه آموزشی دارد و توصیه سرمایه‌گذاری نیست.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SignalCard(result: AnalysisResult) {
    val (signalText, signalColor) = when (result.signal) {
        SignalType.STRONG_BUY -> "خرید قوی" to AccentGreen
        SignalType.BUY -> "خرید" to AccentGreen
        SignalType.NEUTRAL -> "خنثی / انتظار" to AccentYellow
        SignalType.SELL -> "فروش" to AccentRed
        SignalType.STRONG_SELL -> "فروش قوی" to AccentRed
    }

    val trendText = when (result.trend) {
        TrendType.UPTREND -> "صعودی"
        TrendType.DOWNTREND -> "نزولی"
        TrendType.RANGE -> "رنج"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("سیگنال", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        signalText,
                        color = signalColor,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("اطمینان", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${result.confidence}%",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("روند کلی: $trendText", color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                Text("تایم‌فریم: ${result.timeframe}", color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun PriceLevelsCard(result: AnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                "سطوح معاملاتی",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            PriceRow("قیمت فعلی", result.currentPrice, TextPrimary)
            PriceRow("نقطه ورود", result.entry, AccentBlue)
            PriceRow("حد ضرر", result.stopLoss, AccentRed)
            PriceRow("هدف اول", result.target1, AccentGreen)
            PriceRow("هدف دوم", result.target2, AccentGreen)
            PriceRow("هدف سوم", result.target3, AccentGreen)

            Spacer(Modifier.height(8.dp))
            Divider(color = Divider)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("نسبت ریسک به ریوارد", color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "1 : ${"%.1f".format(result.riskReward)}",
                    color = if (result.riskReward >= 2.0) AccentGreen else AccentYellow,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun PriceRow(label: String, value: Double, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
        Text(
            "%.2f".format(value),
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ReasonsCard(reasons: List<String>, title: String, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                title,
                color = accentColor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            reasons.forEach { reason ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("•  ", color = accentColor, style = MaterialTheme.typography.bodyLarge)
                    Text(reason, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun LevelsCard(result: AnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                "سطوح کلیدی",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            Text("مقاومت‌ها", color = AccentRed, fontWeight = FontWeight.SemiBold)
            result.resistanceLevels.forEach { level ->
                Text("•  ${"%.2f".format(level)}", color = TextPrimary, modifier = Modifier.padding(vertical = 2.dp))
            }

            Spacer(Modifier.height(8.dp))

            Text("حمایت‌ها", color = AccentGreen, fontWeight = FontWeight.SemiBold)
            result.supportLevels.forEach { level ->
                Text("•  ${"%.2f".format(level)}", color = TextPrimary, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

@Composable
private fun ScoresCard(result: AnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                "امتیاز اندیکاتورها",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            result.indicatorScores.forEach { (name, score) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(name, color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (score > 0) "+$score" else "$score",
                        color = when {
                            score > 20 -> AccentGreen
                            score < -20 -> AccentRed
                            else -> TextSecondary
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}