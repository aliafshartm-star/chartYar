package com.chartyar.app.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
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
import kotlinx.coroutines.withContext

private const val TAG = "ChartYarHome"
private const val TAPSELL_BANNER_ZONE_ID = "6abe61690a772b2b3f29c2a5"
private const val TELEGRAM_URL = "https://t.me/chartyar_app"
private const val INSTAGRAM_URL = "https://www.instagram.com/chartyar_app"

@Composable
fun HomeScreen(
    remainingFree: Int,
    adCountToday: Int,
    adsForBonus: Int,
    isUnlimited: Boolean,
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_app_title),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Text(
                    text = stringResource(R.string.home_app_subtitle),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            HeaderIconButton(Icons.Filled.TrendingUp, stringResource(R.string.home_nav_prices), AccentBlue, onPricesClick)
            HeaderIconButton(Icons.Filled.Article, stringResource(R.string.home_nav_news), AccentGreen, onNewsClick)
            HeaderIconButton(Icons.Filled.SwapHoriz, stringResource(R.string.home_nav_exchanges), AccentYellow, onExchangesClick)
            HeaderIconButton(Icons.Filled.Settings, stringResource(R.string.home_nav_settings), TextSecondary, onSettingsClick)
        }

        Spacer(Modifier.height(14.dp))

        LimitStatusCard(remainingFree, adCountToday, adsForBonus, isUnlimited)

        Spacer(Modifier.height(12.dp))

        AlertsCompactButton(onClick = onAlertsClick)

        Spacer(Modifier.height(16.dp))

        HeroAnalysisCard(onUploadClick)

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) { MultiTfActionCard(onMultiTfClick) }
            Box(modifier = Modifier.weight(1f)) { CalculatorActionCard(onCalculatorClick) }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) { MiningActionCard(onMiningClick) }
            Box(modifier = Modifier.weight(1f)) { HistoryActionCard(onHistoryClick) }
        }

        Spacer(Modifier.height(20.dp))

        FearGreedCard()

        Spacer(Modifier.height(16.dp))

        SocialLinksRow()

        Spacer(Modifier.height(12.dp))

        TapsellBannerAd(zoneId = TAPSELL_BANNER_ZONE_ID)

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.home_disclaimer),
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
        modifier = Modifier.clickable { onClick() }.padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(tint.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(3.dp))
        Text(label, color = tint, fontSize = 9.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun LimitStatusCard(
    remainingFree: Int,
    adCountToday: Int,
    adsForBonus: Int,
    isUnlimited: Boolean
) {
    if (isUnlimited) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            Brush.linearGradient(listOf(GradientGreenStart, GradientGreenEnd)),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) { Text("🎁", fontSize = 18.sp) }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("پاداش ویژه فعاله!", color = AccentGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("بدون محدودیت تحلیل کن", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }
        return
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(AccentBlue.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Timer, null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("سهمیه تحلیل امروز", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        "$remainingFree از ۳",
                        color = if (remainingFree > 0) AccentGreen else AccentRed,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Icon(Icons.Filled.Star, null, tint = AccentYellow, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("$adCountToday/$adsForBonus", color = AccentYellow, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }

            if (adCountToday > 0) {
                Spacer(Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .background(Divider, RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(
                                if (adsForBonus > 0)
                                    (adCountToday.toFloat() / adsForBonus.toFloat()).coerceIn(0f, 1f)
                                else 0f
                            )
                            .height(5.dp)
                            .background(
                                Brush.horizontalGradient(listOf(AccentYellow, Color(0xFFFF8C42))),
                                RoundedCornerShape(3.dp)
                            )
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    if (adsForBonus - adCountToday > 0)
                        "${adsForBonus - adCountToday} تبلیغ دیگه = ۳ ساعت نامحدود!"
                    else "🎉 پاداش فعال شد!",
                    color = AccentYellow,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AlertsCompactButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(AccentYellow.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Notifications, null, tint = AccentYellow, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.home_alerts_title), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(stringResource(R.string.home_alerts_subtitle), color = TextSecondary, fontSize = 11.sp)
            }
            Icon(Icons.Filled.ArrowForward, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
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
                .background(Brush.linearGradient(listOf(GradientBlueStart, GradientPurpleEnd)))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("📊", fontSize = 26.sp) }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(stringResource(R.string.home_hero_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(stringResource(R.string.home_hero_subtitle), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.home_hero_description),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onUploadClick,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = GradientBlueStart)
                ) {
                    Icon(Icons.Filled.TrendingUp, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.home_hero_button), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun MultiTfActionCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Brush.linearGradient(listOf(GradientPurpleStart, GradientPurpleEnd)), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) { Text("🎯", fontSize = 20.sp) }
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.home_multi_tf_title), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(stringResource(R.string.home_multi_tf_sub), color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun CalculatorActionCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Brush.linearGradient(listOf(GradientBlueStart, GradientBlueEnd)), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Calculate, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.home_calc_title), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(stringResource(R.string.home_calc_sub), color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun MiningActionCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Brush.linearGradient(listOf(GradientGoldStart, GradientGoldEnd)), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) { Text("M", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.home_mining_title), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(stringResource(R.string.home_mining_sub), color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun HistoryActionCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Brush.linearGradient(listOf(GradientGreenStart, GradientGreenEnd)), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Refresh, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.home_history_title), color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(stringResource(R.string.home_history_sub), color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun FearGreedCard() {
    var data by remember { mutableStateOf<FearGreedData?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        data = withContext(Dispatchers.IO) { FearGreedApi.fetchFearGreed() }
        isLoading = false
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
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = AccentBlue, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                } else if (data != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            data!!.value.toString(),
                            color = TextPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            data!!.classificationFa,
                            color = color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text("-", color = TextSecondary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.home_fear_greed_title), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.home_fear_greed_subtitle), color = TextSecondary, fontSize = 11.sp)
                if (data != null && !isLoading) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AccentRed, Color(0xFFFF8C42), AccentYellow, Color(0xFF7BC97C), AccentGreen)
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SocialLinksRow() {
    val context = LocalContext.current
    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
        } catch (e: Exception) {}
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.home_social_title),
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            SocialIcon(R.drawable.ic_telegram, Color(0xFF229ED9), "telegram") { openUrl(TELEGRAM_URL) }
            Spacer(Modifier.width(10.dp))
            SocialIcon(R.drawable.ic_instagram, Color(0xFFE4405F), "instagram") { openUrl(INSTAGRAM_URL) }
        }
    }
}

@Composable
private fun SocialIcon(iconRes: Int, backgroundColor: Color, contentDescription: String, onClick: () -> Unit) {
    val painter: Painter = painterResource(id = iconRes)
    Box(
        modifier = Modifier
            .size(38.dp)
            .background(backgroundColor, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(painter, contentDescription, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun TapsellBannerAd(zoneId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? Activity
    var requestDone by remember { mutableStateOf(false) }

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
            if (activity != null && !requestDone) {
                requestDone = true
                Log.d(TAG, "Requesting banner ad for zone: $zoneId")

                try {
                    Tapsell.requestBannerAd(
                        zoneId,
                        BannerSize.BANNER_320_50,
                        activity,
                        object : RequestResultListener {
                            override fun onSuccess(adId: String) {
                                Log.d(TAG, "Banner loaded successfully: $adId")
                                try {
                                    container.loadAd(adId)
                                    Log.d(TAG, "Banner loadAd called")
                                } catch (e: Exception) {
                                    Log.e(TAG, "loadAd failed: ${e.message}", e)
                                }
                            }

                            override fun onFailure(message: String) {
                                Log.e(TAG, "Banner request failed: $message")
                            }
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Banner exception: ${e.message}", e)
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}