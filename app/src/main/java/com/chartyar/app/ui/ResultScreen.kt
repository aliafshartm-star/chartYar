package com.chartyar.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.analysis.AnalysisResult
import com.chartyar.app.analysis.DetectedPattern
import com.chartyar.app.analysis.SignalType
import com.chartyar.app.analysis.StrategyEngine
import com.chartyar.app.analysis.StrategyType
import com.chartyar.app.analysis.TrendType
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.AccentRed
import com.chartyar.app.ui.theme.AccentYellow
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkCard
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider as DividerColor
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
import kotlin.math.abs

@Composable
fun ResultScreen(
    result: AnalysisResult,
    onBack: () -> Unit,
    onChangeStrategy: () -> Unit = {}
) {
    val context = LocalContext.current
    val strategyInfo = StrategyEngine.getStrategy(result.strategy)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 20.dp)
    ) {
        ResultHeader(
            timeframe = result.timeframe,
            onBack = onBack,
            onShare = {
                ShareHelper.shareText(context, ShareHelper.buildShareText(result))
            }
        )

        StrategyHeaderCard(
            emoji = strategyInfo.emoji,
            name = strategyInfo.displayName,
            description = strategyInfo.shortDescription,
            color = strategyInfo.iconColor,
            onChangeStrategy = onChangeStrategy
        )

        Spacer(Modifier.height(12.dp))

        SignalHeroCard(result)

        Spacer(Modifier.height(16.dp))

        TradeSetupCard(result)

        Spacer(Modifier.height(12.dp))

        if (result.strategy == StrategyType.MULTI_AI && result.engineVotes.isNotEmpty()) {
            EngineVotesCard(result)
            Spacer(Modifier.height(12.dp))
        }

        if (result.patterns.isNotEmpty()) {
            PatternsCard(result.patterns)
            Spacer(Modifier.height(12.dp))
        }

        if (result.visionTrendLines.isNotEmpty()) {
            VisionCard(result)
            Spacer(Modifier.height(12.dp))
        }

        ReasonsCard(result.reasons, "دلایل تحلیل", AccentGreen)

        if (result.warnings.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            ReasonsCard(result.warnings, "هشدارها", AccentYellow)
        }

        Spacer(Modifier.height(12.dp))

        LevelsCard(result)

        Spacer(Modifier.height(12.dp))

        ScoresCard(result)

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                ShareHelper.shareText(context, ShareHelper.buildShareText(result))
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = TextPrimary)
        ) {
            Icon(Icons.Filled.Share, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("اشتراک‌گذاری متنی", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = { ShareImageHelper.shareImage(context, result) },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen, contentColor = TextPrimary)
        ) {
            Icon(Icons.Filled.Image, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("اشتراک‌گذاری تصویری", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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

@Composable
private fun ResultHeader(
    timeframe: String,
    onBack: () -> Unit,
    onShare: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, "back", tint = TextPrimary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("نتیجه تحلیل", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("تایم‌فریم: $timeframe", color = TextSecondary, fontSize = 11.sp)
        }
        IconButton(onClick = onShare) {
            Icon(Icons.Filled.Share, "share", tint = AccentBlue)
        }
    }
}

@Composable
private fun StrategyHeaderCard(
    emoji: String,
    name: String,
    description: String,
    color: Long,
    onChangeStrategy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Color(color).copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("استراتژی: $name", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(description, color = TextSecondary, fontSize = 10.sp)
            }
            Box(
                modifier = Modifier
                    .background(Color(color).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    .clickable { onChangeStrategy() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.SwapHoriz, "change", tint = Color(color), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("تغییر", color = Color(color), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SignalHeroCard(result: AnalysisResult) {
    val signalText = when (result.signal) {
        SignalType.STRONG_BUY -> "خرید قوی"
        SignalType.BUY -> "خرید"
        SignalType.NEUTRAL -> "خنثی"
        SignalType.SELL -> "فروش"
        SignalType.STRONG_SELL -> "فروش قوی"
    }

    val signalIcon = when (result.signal) {
        SignalType.STRONG_BUY -> "🚀"
        SignalType.BUY -> "📈"
        SignalType.NEUTRAL -> "⏸"
        SignalType.SELL -> "📉"
        SignalType.STRONG_SELL -> "🔻"
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

    val trendColor = when (result.trend) {
        TrendType.UPTREND -> AccentGreen
        TrendType.DOWNTREND -> AccentRed
        TrendType.RANGE -> AccentYellow
    }

    var animatedConfidence by remember { mutableStateOf(0f) }
    LaunchedEffect(result.confidence) {
        animatedConfidence = result.confidence / 100f
    }
    val progress by animateFloatAsState(
        targetValue = animatedConfidence,
        animationSpec = tween(1200),
        label = "confidence"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(gradientStart, gradientEnd)))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(signalIcon, fontSize = 30.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("سیگنال تحلیل", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(signalText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 26.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("اطمینان", color = Color.White.copy(alpha = 0.75f), fontSize = 10.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "${result.confidence}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color.White)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("روند", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        Spacer(Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(trendText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("قیمت فعلی", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "%.2f".format(result.currentPrice),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TradeSetupCard(result: AnalysisResult) {
    val isBuy = result.signal == SignalType.BUY || result.signal == SignalType.STRONG_BUY
    val isNeutral = result.signal == SignalType.NEUTRAL

    val current = result.currentPrice
    val entry = result.entry
    val sl = result.stopLoss
    val t1 = result.target1
    val t2 = result.target2
    val t3 = result.target3

    fun pctFrom(price: Double): String {
        if (current <= 0.0) return "0%"
        val pct = ((price - current) / current) * 100.0
        val sign = if (pct >= 0) "+" else ""
        return "$sign${"%.2f".format(pct)}%"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(AccentBlue, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "ستاپ معاملاتی",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                if (isNeutral) "پیشنهاد: منتظر سیگنال واضح‌تر بمان"
                else if (isBuy) "پیشنهاد: ورود در جهت صعودی"
                else "پیشنهاد: ورود در جهت نزولی",
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(Modifier.height(14.dp))

            PriceLevelRow(
                label = "نقطه ورود",
                sublabel = "ورود پیشنهادی",
                price = entry,
                percent = pctFrom(entry),
                color = AccentBlue,
                isHighlighted = true
            )

            LevelDivider()

            PriceLevelRow(
                label = "حد ضرر",
                sublabel = "خروج اضطراری",
                price = sl,
                percent = pctFrom(sl),
                color = AccentRed,
                isHighlighted = false
            )

            LevelDivider()

            PriceLevelRow(
                label = "هدف اول",
                sublabel = "سود سبک",
                price = t1,
                percent = pctFrom(t1),
                color = AccentGreen,
                isHighlighted = false
            )

            LevelDivider()

            PriceLevelRow(
                label = "هدف دوم",
                sublabel = "سود متوسط",
                price = t2,
                percent = pctFrom(t2),
                color = AccentGreen,
                isHighlighted = false
            )

            LevelDivider()

            PriceLevelRow(
                label = "هدف سوم",
                sublabel = "سود حداکثری",
                price = t3,
                percent = pctFrom(t3),
                color = AccentGreen,
                isHighlighted = false
            )

            Spacer(Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccentBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ریسک به ریوارد", color = TextSecondary, fontSize = 11.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "1 : ${"%.2f".format(result.riskReward)}",
                            color = if (result.riskReward >= 2.0) AccentGreen else AccentYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (result.riskReward >= 2.0) AccentGreen.copy(alpha = 0.2f)
                                else AccentYellow.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            if (result.riskReward >= 2.0) "مطلوب" else "مرزی",
                            color = if (result.riskReward >= 2.0) AccentGreen else AccentYellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceLevelRow(
    label: String,
    sublabel: String,
    price: Double,
    percent: String,
    color: Color,
    isHighlighted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (isHighlighted) 12.dp else 8.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                color = TextPrimary,
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
                fontSize = if (isHighlighted) 14.sp else 13.sp
            )
            Text(sublabel, color = TextSecondary, fontSize = 10.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "%.2f".format(price),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    percent,
                    color = color,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun LevelDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DividerColor.copy(alpha = 0.5f))
    )
}

@Composable
private fun EngineVotesCard(result: AnalysisResult) {
    val bullish = result.engineVotes.count { it.vote == 1 }
    val bearish = result.engineVotes.count { it.vote == -1 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🤖", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text("رأی موتورهای هوش مصنوعی", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(12.dp))

            result.engineVotes.forEach { vote ->
                val color = when (vote.vote) {
                    1 -> AccentGreen
                    -1 -> AccentRed
                    else -> TextSecondary
                }
                val icon = when (vote.vote) {
                    1 -> "✅"
                    -1 -> "❌"
                    else -> "⬜"
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(icon, fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(vote.engineName, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(vote.description, color = color, fontSize = 11.sp)
                    }
                    Text(
                        if (vote.score > 0) "+${vote.score}" else "${vote.score}",
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Divider(color = DividerColor)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("نتیجه رأی‌گیری", color = TextSecondary, fontSize = 12.sp)
                Text(
                    "$bullish صعودی / $bearish نزولی",
                    color = when {
                        bullish > bearish -> AccentGreen
                        bearish > bullish -> AccentRed
                        else -> AccentYellow
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
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
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("الگوهای شناسایی‌شده", fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
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
                        Text(pattern.name, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(pattern.description, color = TextSecondary, fontSize = 11.sp, maxLines = 2)
                    }
                    Box(
                        modifier = Modifier
                            .background(dirColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(dirText, color = dirColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
                if (index < patterns.size - 1) Divider(color = DividerColor)
            }
        }
    }
}

@Composable
private fun VisionCard(result: AnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("تحلیل بینایی چارت", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                Text("روند کلی", color = TextSecondary, fontSize = 12.sp)
                Text(dominantText, color = dominantColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = DividerColor)
            Spacer(Modifier.height(12.dp))

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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("خط $lineType ($slopeType)", color = lineColor, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Text("قدرت: ${line.strength}", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ReasonsCard(reasons: List<String>, title: String, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(4.dp).background(accentColor, RoundedCornerShape(2.dp)))
                Spacer(Modifier.width(8.dp))
                Text(title, color = accentColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
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

@Composable
private fun LevelsCard(result: AnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("سطوح کلیدی", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(12.dp))

            if (result.resistanceLevels.isNotEmpty()) {
                Text("مقاومت‌ها", color = AccentRed, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                result.resistanceLevels.forEach {
                    Text("  •  ${"%.2f".format(it)}", color = TextPrimary, fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
            }

            if (result.supportLevels.isNotEmpty()) {
                Text("حمایت‌ها", color = AccentGreen, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                result.supportLevels.forEach {
                    Text("  •  ${"%.2f".format(it)}", color = TextPrimary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ScoresCard(result: AnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("امتیاز اندیکاتورها", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(12.dp))
            result.indicatorScores.forEach { (name, score) ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(name, color = TextSecondary, fontSize = 12.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(DarkCard)
                        ) {
                            val fraction = (abs(score).coerceAtMost(100) / 100f)
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