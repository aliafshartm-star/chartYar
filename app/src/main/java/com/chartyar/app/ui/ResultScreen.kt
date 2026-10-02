package com.chartyar.app.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.analysis.AnalysisResult
import com.chartyar.app.analysis.DetectedPattern
import com.chartyar.app.analysis.SignalType
import com.chartyar.app.analysis.TrendType
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.AccentRed
import com.chartyar.app.ui.theme.AccentYellow
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkCard
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider
import com.chartyar.app.ui.theme.GradientBlueEnd
import com.chartyar.app.ui.theme.GradientBlueStart
import com.chartyar.app.ui.theme.GradientGreenEnd
import com.chartyar.app.ui.theme.GradientGreenStart
import com.chartyar.app.ui.theme.GradientRedEnd
import com.chartyar.app.ui.theme.GradientRedStart
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
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 20.dp)
    ) {
        // ============ Header ============
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "back", tint = TextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "نتیجه تحلیل",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    "تایم‌فریم: ${result.timeframe}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            IconButton(
                onClick = {
                    val text = ShareHelper.buildShareText(result)
                    ShareHelper.shareText(context, text)
                }
            ) {
                Icon(Icons.Filled.Share, contentDescription = "share", tint = AccentBlue)
            }
        }

        // ============ Signal Hero Card (Gradient) ============
        SignalHeroCard(result)

        Spacer(Modifier.height(16.dp))

        // ============ Quick Stats Row ============
        QuickStatsRow(result)

        Spacer(Modifier.height(16.dp))

        // ============ Price Levels Card ============
        PriceLevelsCard(result)

        // ============ Patterns Card ============
        if (result.patterns.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            PatternsCard(result.patterns)
        }

        // ============ Vision Card ============
        if (result.visionTrendLines.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            VisionCard(result)
        }

        // ============ Reasons Card ============
        Spacer(Modifier.height(12.dp))
        ReasonsCard(result.reasons, "دلایل تحلیل", AccentGreen)

        // ============ Warnings Card ============
        if (result.warnings.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            ReasonsCard(result.warnings, "هشدارها", AccentYellow)
        }

        // ============ Support/Resistance Card ============
        Spacer(Modifier.height(12.dp))
        LevelsCard(result)

        // ============ Indicator Scores Card ============
        Spacer(Modifier.height(12.dp))
        ScoresCard(result)

        Spacer(Modifier.height(20.dp))

        // ============ Share Buttons ============
        Button(
            onClick = {
                val text = ShareHelper.buildShareText(result)
                ShareHelper.shareText(context, text)
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = TextPrimary
            )
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "اشتراک‌گذاری متنی",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = {
                ShareImageHelper.shareImage(context, result)
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGreen,
                contentColor = TextPrimary
            )
        ) {
            Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "اشتراک‌گذاری تصویری",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = "این تحلیل صرفاً جنبه آموزشی دارد و توصیه سرمایه‌گذاری نیست.",
            color = TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
    }
}

// ============ Signal Hero Card ============
@Composable
private fun SignalHeroCard(result: AnalysisResult) {
    val signalText = when (result.signal) {
        SignalType.STRONG_BUY -> "خرید قوی"
        SignalType.BUY -> "خرید"
        SignalType.NEUTRAL -> "خنثی"
        SignalType.SELL -> "فروش"
        SignalType.STRONG_SELL -> "فروش قوی"
    }

    val (gradientStart, gradientEnd) = when (result.signal) {
        SignalType.STRONG_BUY, SignalType.BUY -> GradientGreenStart to GradientGreenEnd
        SignalType.STRONG_SELL, SignalType.SELL -> GradientRedStart to GradientRedEnd
        else -> GradientBlueStart to GradientBlueEnd
    }

    val trendText = when (result.trend) {
        TrendType.UPTREND -> "صعودی"
        TrendType.DOWNTREND -> "نزولی"
        TrendType.RANGE -> "رنج"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(colors = listOf(gradientStart, gradientEnd))
                )
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "سیگنال",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = signalText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${result.confidence}%",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "اطمینان",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "روند",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = trendText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "قیمت فعلی",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "%.2f".format(result.currentPrice),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// ============ Quick Stats Row (Risk/Reward + Entry) ============
@Composable
private fun QuickStatsRow(result: AnalysisResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Risk/Reward card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, Divider)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Text(
                    text = "ریسک به ریوارد",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "1 : ${"%.1f".format(result.riskReward)}",
                    color = if (result.riskReward >= 2.0) AccentGreen else AccentYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        // Entry card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, Divider)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Text(
                    text = "نقطه ورود",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "%.2f".format(result.entry),
                    color = AccentBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

// ============ Price Levels Card ============
@Composable
private fun PriceLevelsCard(result: AnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                "سطوح معاملاتی",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(12.dp))

            PriceRow("حد ضرر", result.stopLoss, AccentRed)
            Spacer(Modifier.height(4.dp))
            Divider(color = Divider)
            Spacer(Modifier.height(4.dp))
            PriceRow("هدف اول", result.target1, AccentGreen)
            Spacer(Modifier.height(4.dp))
            Divider(color = Divider)
            Spacer(Modifier.height(4.dp))
            PriceRow("هدف دوم", result.target2, AccentGreen)
            Spacer(Modifier.height(4.dp))
            Divider(color = Divider)
            Spacer(Modifier.height(4.dp))
            PriceRow("هدف سوم", result.target3, AccentGreen)
        }
    }
}

@Composable
private fun PriceRow(label: String, value: Double, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(
            "%.2f".format(value),
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

// ============ Patterns Card ============
@Composable
private fun PatternsCard(patterns: List<DetectedPattern>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔍", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "الگوهای شناسایی‌شده",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(Modifier.height(12.dp))

            patterns.forEachIndexed { index, pattern ->
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

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            pattern.name,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            pattern.description,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 2
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                color = dirColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = dirText,
                            color = dirColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                if (index < patterns.size - 1) {
                    Divider(color = Divider)
                }
            }
        }
    }
}

// ============ Vision Card ============
@Composable
private fun VisionCard(result: AnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👁", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "تحلیل بینایی چارت",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(Modifier.height(10.dp))

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
                    "روند کلی",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    dominantText,
                    color = dominantColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = Divider)
            Spacer(Modifier.height(12.dp))

            Text(
                "خطوط روند",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
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
                        fontSize = 12.sp
                    )
                    Text(
                        "قدرت: ${line.strength}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ============ Reasons / Warnings Card ============
@Composable
private fun ReasonsCard(reasons: List<String>, title: String, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(accentColor, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            reasons.forEach { reason ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("•  ", color = accentColor, fontSize = 14.sp)
                    Text(reason, color = TextPrimary, fontSize = 12.sp)
                }
            }
        }
    }
}

// ============ Support / Resistance Card ============
@Composable
private fun LevelsCard(result: AnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                "سطوح کلیدی",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(12.dp))

            if (result.resistanceLevels.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AccentRed, RoundedCornerShape(50))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "مقاومت‌ها",
                        color = AccentRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                result.resistanceLevels.forEach { level ->
                    Text(
                        "  •  ${"%.2f".format(level)}",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            if (result.supportLevels.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(AccentGreen, RoundedCornerShape(50))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "حمایت‌ها",
                        color = AccentGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.height(6.dp))
                result.supportLevels.forEach { level ->
                    Text(
                        "  •  ${"%.2f".format(level)}",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

// ============ Indicator Scores Card ============
@Composable
private fun ScoresCard(result: AnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                "امتیاز اندیکاتورها",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(12.dp))
            result.indicatorScores.forEach { (name, score) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(name, color = TextSecondary, fontSize = 12.sp)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Progress bar
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(DarkCard)
                        ) {
                            val fraction = (kotlin.math.abs(score).coerceAtMost(100) / 100f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        when {
                                            score > 20 -> AccentGreen
                                            score < -20 -> AccentRed
                                            else -> TextSecondary
                                        }
                                    )
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (score > 0) "+$score" else "$score",
                            color = when {
                                score > 20 -> AccentGreen
                                score < -20 -> AccentRed
                                else -> TextSecondary
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.width(40.dp)
                        )
                    }
                }
            }
        }
    }
}