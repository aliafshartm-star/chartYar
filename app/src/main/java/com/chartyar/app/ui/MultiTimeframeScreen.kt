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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.analysis.MultiTimeframeResult
import com.chartyar.app.analysis.SignalType
import com.chartyar.app.analysis.TimeframeAnalysis
import com.chartyar.app.analysis.TrendType
import com.chartyar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiTimeframeScreen(
    result: MultiTimeframeResult,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
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
                "تحلیل مولتی تایم فریم",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // سیگنال نهایی
        CombinedSignalCard(result)

        Spacer(Modifier.height(12.dp))

        // هماهنگی
        AlignmentCard(result.alignment)

        Spacer(Modifier.height(12.dp))

        // تحلیل هر تایم فریم
        result.analyses.forEach { analysis ->
            TimeframeCard(analysis)
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(12.dp))

        // خلاصه
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    "خلاصه",
                    color = AccentBlue,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    result.summary,
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CombinedSignalCard(result: MultiTimeframeResult) {
    val (signalText, signalColor) = when (result.combinedSignal) {
        SignalType.STRONG_BUY -> "خرید قوی" to AccentGreen
        SignalType.BUY -> "خرید" to AccentGreen
        SignalType.NEUTRAL -> "خنثی" to AccentYellow
        SignalType.SELL -> "فروش" to AccentRed
        SignalType.STRONG_SELL -> "فروش قوی" to AccentRed
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
                    Text(
                        "سیگنال ترکیبی",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        signalText,
                        color = signalColor,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "اطمینان",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${result.combinedConfidence}%",
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Divider(color = Divider)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "ارز: ${result.symbol}",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "تعداد TF: ${result.analyses.size}",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun AlignmentCard(alignment: Int) {
    val (text, color) = when {
        alignment >= 80 -> "هماهنگی عالی" to AccentGreen
        alignment >= 60 -> "هماهنگی خوب" to AccentYellow
        else -> "عدم هماهنگی" to AccentRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "هماهنگی تایم فریم ها",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "$alignment%",
                    color = color,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = alignment / 100f,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = color,
                trackColor = DarkCard
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text,
                color = color,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun TimeframeCard(analysis: TimeframeAnalysis) {
    val (signalText, signalColor) = when (analysis.signal) {
        SignalType.STRONG_BUY -> "خرید قوی" to AccentGreen
        SignalType.BUY -> "خرید" to AccentGreen
        SignalType.NEUTRAL -> "خنثی" to AccentYellow
        SignalType.SELL -> "فروش" to AccentRed
        SignalType.STRONG_SELL -> "فروش قوی" to AccentRed
    }

    val trendText = when (analysis.trend) {
        TrendType.UPTREND -> "صعودی"
        TrendType.DOWNTREND -> "نزولی"
        TrendType.RANGE -> "رنج"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // تایم فریم
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AccentBlue.copy(alpha = 0.15f)
                )
            ) {
                Text(
                    analysis.timeframe,
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // سیگنال و روند
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    signalText,
                    color = signalColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "روند: $trendText",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // اطمینان
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${analysis.confidence}%",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "اطمینان",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}