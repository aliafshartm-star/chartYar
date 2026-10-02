package com.chartyar.app.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.chartyar.app.R
import com.chartyar.app.data.FearGreedApi
import com.chartyar.app.data.FearGreedData
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
import com.chartyar.app.ui.theme.GradientGoldEnd
import com.chartyar.app.ui.theme.GradientGoldStart
import com.chartyar.app.ui.theme.GradientGreenEnd
import com.chartyar.app.ui.theme.GradientGreenStart
import com.chartyar.app.ui.theme.GradientPurpleEnd
import com.chartyar.app.ui.theme.GradientPurpleStart
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.request.BannerSize
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.views.banner.BannerContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val TAPSELL_BANNER_ZONE_ID = "6abe61690a772b2b3f29c2a5"
private const val TELEGRAM_URL = "https://t.me/chartyar_app"
private const val INSTAGRAM_URL = "https://www.instagram.com/chartyar_app"

@Composable
fun HomeScreen(
    onUploadClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onNewsClick: () -> Unit,
    onPricesClick: () -> Unit,
    onMultiTfClick: () -> Unit,
    onCalculatorClick: () -> Unit,
    onMiningClick: () -> Unit,
    onExchangesClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    val fadeIn by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(600),
        label = "fadeIn"
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_app_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Text(
                    text = stringResource(R.string.home_app_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            HeaderIconButton(
                icon = Icons.Filled.TrendingUp,
                label = stringResource(R.string.home_nav_prices),
                tint = AccentBlue,
                onClick = onPricesClick
            )

            HeaderIconButton(
                icon = Icons.Filled.Article,
                label = stringResource(R.string.home_nav_news),
                tint = AccentGreen,
                onClick = onNewsClick
            )

            HeaderIconButton(
                icon = Icons.Filled.SwapHoriz,
                label = stringResource(R.string.home_nav_exchanges),
                tint = AccentYellow,
                onClick = onExchangesClick
            )

            HeaderIconButton(
                icon = Icons.Filled.Settings,
                label = stringResource(R.string.home_nav_settings),
                tint = TextSecondary,
                onClick = onSettingsClick
            )
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn)
        ) {
            AlertsCompactButton(onClick = onAlertsClick)
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn)
        ) {
            HeroAnalysisCard(onUploadClick = onUploadClick)
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MultiTfActionCard(onClick = onMultiTfClick)
            }
            Box(modifier = Modifier.weight(1f)) {
                CalculatorActionCard(onClick = onCalculatorClick)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MiningActionCard(onClick = onMiningClick)
            }
            Box(modifier = Modifier.weight(1f)) {
                HistoryActionCard(onClick = onHistoryClick)
            }
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn)
        ) {
            FearGreedCardWallex()
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn)
        ) {
            SocialLinksRow()
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn)
        ) {
            TapsellBannerAd(zoneId = TAPSELL_BANNER_ZONE_ID)
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.home_disclaimer),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    color = tint.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AlertsCompactButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(
                        color = AccentYellow.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = AccentYellow,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_alerts_title),
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.home_alerts_subtitle),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun HeroAnalysisCard(onUploadClick: () -> Unit) {
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
                    brush = Brush.linearGradient(
                        colors = listOf(GradientBlueStart, GradientPurpleEnd)
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📊", fontSize = 26.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.home_hero_title),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = stringResource(R.string.home_hero_subtitle),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.home_hero_description),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onUploadClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = GradientBlueStart
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.home_hero_button),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MultiTfActionCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientPurpleStart, GradientPurpleEnd)
                        ),
                        shape = RoundedCornerShape(11.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎯", fontSize = 20.sp)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.home_multi_tf_title),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = stringResource(R.string.home_multi_tf_sub),
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun CalculatorActionCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientBlueStart, GradientBlueEnd)
                        ),
                        shape = RoundedCornerShape(11.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Calculate,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.home_calc_title),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = stringResource(R.string.home_calc_sub),
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun MiningActionCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientGoldStart, GradientGoldEnd)
                        ),
                        shape = RoundedCornerShape(11.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "M", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.home_mining_title),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = stringResource(R.string.home_mining_sub),
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun HistoryActionCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientGreenStart, GradientGreenEnd)
                        ),
                        shape = RoundedCornerShape(11.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.home_history_title),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = stringResource(R.string.home_history_sub),
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun FearGreedCardWallex() {
    var data by remember { mutableStateOf<FearGreedData?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        val result = withContext(Dispatchers.IO) {
            FearGreedApi.fetchFearGreed()
        }
        data = result
        isLoading = false
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(30 * 60 * 1000L)
            val result = withContext(Dispatchers.IO) {
                FearGreedApi.fetchFearGreed()
            }
            if (result != null) data = result
        }
    }

    val value = data?.value ?: 0
    val color = when {
        value <= 25 -> AccentRed
        value <= 45 -> Color(0xFFFF8C42)
        value <= 55 -> AccentYellow
        value <= 75 -> Color(0xFF7BC97C)
        else -> AccentGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = AccentBlue,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (data == null) {
                    Text(
                        text = "-",
                        color = TextSecondary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    FearGreedArc(value = value, color = color)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = data!!.value.toString(),
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = data!!.classificationFa,
                            color = color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_fear_greed_title),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_fear_greed_subtitle),
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                if (data != null && !isLoading) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fear",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "Greed",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        AccentRed,
                                        Color(0xFFFF8C42),
                                        AccentYellow,
                                        Color(0xFF7BC97C),
                                        AccentGreen
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun FearGreedArc(value: Int, color: Color) {
    val redColor = AccentRed
    val orangeColor = Color(0xFFFF8C42)
    val yellowColor = AccentYellow
    val lightGreenColor = Color(0xFF7BC97C)
    val greenColor = AccentGreen
    val bgColor = DarkCard

    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 10.dp.toPx()
        val arcSize = Size(
            size.width - strokeWidth,
            size.height - strokeWidth
        )
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

        drawArc(
            color = bgColor,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        val segments = listOf(
            Triple(0f, 25f, redColor),
            Triple(25f, 45f, orangeColor),
            Triple(45f, 55f, yellowColor),
            Triple(55f, 75f, lightGreenColor),
            Triple(75f, 100f, greenColor)
        )
        segments.forEach { (start, end, c) ->
            drawArc(
                color = c.copy(alpha = 0.35f),
                startAngle = 180f + (start / 100f) * 180f,
                sweepAngle = ((end - start) / 100f) * 180f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
        }

        val sweep = (value / 100f) * 180f
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = sweep,
            useCenter = false,
            topLeft = topLeft,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        val angleRad = Math.toRadians((180f + sweep).toDouble())
        val radius = arcSize.width / 2
        val cx = topLeft.x + radius + (radius * kotlin.math.cos(angleRad)).toFloat()
        val cy = topLeft.y + radius + (radius * kotlin.math.sin(angleRad)).toFloat()
        drawCircle(color = color, radius = strokeWidth * 0.7f, center = Offset(cx, cy))
    }
}

@Composable
private fun SocialLinksRow() {
    val context = LocalContext.current

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_social_title),
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            SocialIcon(
                iconRes = R.drawable.ic_telegram,
                backgroundColor = Color(0xFF229ED9),
                contentDescription = "telegram",
                onClick = { openUrl(TELEGRAM_URL) }
            )

            Spacer(Modifier.width(10.dp))

            SocialIcon(
                iconRes = R.drawable.ic_instagram,
                backgroundColor = Color(0xFFE4405F),
                contentDescription = "instagram",
                onClick = { openUrl(INSTAGRAM_URL) }
            )
        }
    }
}

@Composable
private fun SocialIcon(
    iconRes: Int,
    backgroundColor: Color,
    contentDescription: String,
    onClick: () -> Unit
) {
    val painter: Painter = painterResource(id = iconRes)

    Box(
        modifier = Modifier
            .size(38.dp)
            .background(backgroundColor, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun TapsellBannerAd(zoneId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? Activity

    var adShown by remember { mutableStateOf(false) }

    AndroidView(
        factory = { ctx ->
            BannerContainer(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        },
        update = { container ->
            if (activity != null && !adShown) {
                try {
                    Tapsell.requestBannerAd(
                        zoneId,
                        BannerSize.BANNER_320_50,
                        activity,
                        object : RequestResultListener {
                            override fun onSuccess(adId: String) {
                                adShown = tryShowAd(container, adId)
                            }

                            override fun onFailure(message: String) {
                                adShown = false
                            }
                        }
                    )
                } catch (e: Exception) {
                    adShown = false
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

private fun tryShowAd(container: BannerContainer, adId: String): Boolean {
    val methodNames = listOf(
        "showAd",
        "loadAd",
        "setAd",
        "setAdId",
        "show",
        "displayAd"
    )
    for (name in methodNames) {
        try {
            val method = container.javaClass.getMethod(name, String::class.java)
            method.invoke(container, adId)
            return true
        } catch (e: NoSuchMethodException) {
            continue
        } catch (e: Exception) {
            continue
        }
    }
    return false
}