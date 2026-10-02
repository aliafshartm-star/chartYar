package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.analysis.AnalysisResult
import com.chartyar.app.analysis.ChartVisionAnalyzer
import com.chartyar.app.analysis.DetectedPattern
import com.chartyar.app.analysis.SignalType
import com.chartyar.app.analysis.TrendType
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.AccentRed
import com.chartyar.app.ui.theme.AccentYellow
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary
import com.chartyar.app.util.ShareHelper
import com.chartyar.app.util.ShareImageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    result: AnalysisResult,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "back", tint = TextPrimary)
            }
            Text(
                "نتیجه تحلیل",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    val text = ShareHelper.buildShareText(result)
                    ShareHelper.shareText(context, text)
                }
            ) {
                Icon(
                    Icons.Filled.Share,
                    contentDescription = "share",
                    tint = AccentBlue
                )
            }
        }

        SignalCard(result)

        Spacer(Modifier.height(12.dp))

        PriceLevelsCard(result)

        if (result.patterns.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            PatternsCard(result.patterns)
        }

        if (result.visionTrendLines.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            VisionCard(result)
        }

        Spacer(Modifier.height(12.dp))

        ReasonsCard(result.reasons, "دلایل تحلیل", AccentGreen)

        if (result.warnings.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            ReasonsCard(result.warnings, "هشدارها", AccentYellow)
        }

        Spacer(Modifier.height(12.dp))

        LevelsCard(result)

        Spacer(Modifier.height(12.dp))

        ScoresCard(result)

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val text = ShareHelper.buildShareText(result)
                ShareHelper.shareText(context, text)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = TextPrimary
            )
        ) {
            Icon(Icons.Filled.Share, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                "اشتراک‌گذاری متنی",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                ShareImageHelper.shareImage(context, result)
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGreen,
                contentColor = TextPrimary
            )
        ) {
            Icon(Icons.Filled.Image, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                "اشتراک‌گذاری تصویری",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "این تحلیل صرفاً جنبه آموزشی دارد و توصیه سرمایه‌گذاری نیست.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun SignalCard(result: AnalysisResult) {
    val signalText = when (result.signal) {
        SignalType.STRONG_BUY -> "خرید قوی"
        SignalType.BUY -> "خرید"
        SignalType.NEUTRAL -> "خنثی / انتظار"
        SignalType.SELL -> "فروش"
        SignalType.STRONG_SELL -> "فروش قوی"
    }
    val signalColor = when (result.signal) {
        SignalType.STRONG_BUY -> AccentGreen
        SignalType.BUY -> AccentGreen
        SignalType.NEUTRAL -> AccentYellow
        SignalType.SELL -> AccentRed
        SignalType.STRONG_SELL -> AccentRed
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
private fun PatternsCard(patterns: List<DetectedPattern>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                "الگوهای شناسایی‌شده",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            patterns.forEach { pattern ->
                val dirText = when (pattern.direction) {
                    1 -> "صعودی"
                    -1 -> "نزولی"
                    else -> "خنثی"
                }
                val dirColor = when (pattern.direction) {
                    1 -> AccentGreen
                    -1 -> AccentRed
                    else -> AccentYellow
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            pattern.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            dirText,
                            color = dirColor,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "اطمینان: ${pattern.confidence}%",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        pattern.description,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                    pattern.targetPrice?.let { target ->
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "هدف الگو: ${"%.2f".format(target)}",
                            color = dirColor,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Divider(color = Divider)
                }
            }
        }
    }
}

// ============ کارت جدید: تحلیل بینایی ============
@Composable
private fun VisionCard(result: AnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Text(
                "تحلیل بینایی چارت",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "تشخیص خودکار خطوط روند و سطوح کلیدی",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(12.dp))

            val dominantText = when (result.visionDominantTrend) {
                1 -> "روند صعودی"
                -1 -> "روند نزولی"
                else -> "بدون روند مشخص"
            }
            val dominantColor = when (result.visionDominantTrend) {
                1 -> AccentGreen
                -1 -> AccentRed
                else -> AccentYellow
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "روند کلی (بینایی)",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    dominantText,
                    color = dominantColor,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = Divider)
            Spacer(Modifier.height(12.dp))

            Text(
                "خطوط روند شناسایی‌شده",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))

            result.visionTrendLines.forEach { line ->
                val lineType = if (line.isSupport) "حمایت" else "مقاومت"
                val slopeType = when {
                    line.slope > 0.5 -> "صعودی"
                    line.slope < -0.5 -> "نزولی"
                    else -> "افقی"
                }
                val lineColor = if (line.isSupport) AccentGreen else AccentRed

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "خط $lineType ($slopeType)",
                        color = lineColor,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "قدرت: ${line.strength}",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
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