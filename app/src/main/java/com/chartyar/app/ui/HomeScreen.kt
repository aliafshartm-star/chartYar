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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.chartyar.app.R
import com.chartyar.app.data.ArzdigitalNewsApi
import com.chartyar.app.data.NewsItem
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

private const val TAG = "TapsellBanner"
private const val TAPSELL_BANNER_ZONE_ID = "6abe61690a772b2b3f29c2a5"
private const val TELEGRAM_URL = "https://t.me/viachart_app"
private const val INSTAGRAM_URL = "https://www.instagram.com/viachart_app"

@Composable
fun HomeScreen(
    remainingFree: Int,
    adsSinceReward: Int,
    adsPerReward: Int,
    onUploadClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onNewsClick: () -> Unit,
    onAnalysesClick: () -> Unit,
    onPricesClick: () -> Unit,
    onMultiTfClick: () -> Unit,
    onCalculatorClick: () -> Unit,
    onMiningClick: () -> Unit,
    onExchangesClick: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
    ) {
        TopHeader(
            onAlertsClick = onAlertsClick,
            onSettingsClick = onSettingsClick
        )

        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            TapsellBannerAd(zoneId = TAPSELL_BANNER_ZONE_ID)
        }

        Spacer(Modifier.height(16.dp))

        IconShortcutsRow(
            onCalculatorClick = onCalculatorClick,
            onHistoryClick = onHistoryClick,
            onNewsClick = onNewsClick,
            onPricesClick = onPricesClick,
            onMultiTfClick = onMultiTfClick
        )

        Spacer(Modifier.height(20.dp))

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            LatestAnalysesSection(
                onViewAll = onAnalysesClick,
                onItemClick = { url -> openExternalUrl(context, url) }
            )
        }

        Spacer(Modifier.height(20.dp))

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            LimitStatusCard(
                remainingFree = remainingFree,
                adsSinceReward = adsSinceReward,
                adsPerReward = adsPerReward
            )
        }

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            HeroAnalysisCard(onUploadClick)
        }

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            SocialLinksRow()
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.home_disclaimer),
            color = TextSecondary,
            textAlign = TextAlign.Center,
            fontSize = 11.sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun TopHeader(
    onAlertsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(
                    Brush.linearGradient(listOf(GradientBlueStart, GradientPurpleEnd)),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "V",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.home_app_title),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = stringResource(R.string.home_app_subtitle),
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        HeaderRoundIcon(
            icon = Icons.Filled.Notifications,
            contentDescription = stringResource(R.string.home_alerts_title),
            onClick = onAlertsClick
        )
        Spacer(Modifier.width(8.dp))
        HeaderRoundIcon(
            icon = Icons.Filled.Settings,
            contentDescription = stringResource(R.string.home_nav_settings),
            onClick = onSettingsClick
        )
    }
}

@Composable
private fun HeaderRoundIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(DarkSurface, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription, tint = TextPrimary, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun IconShortcutsRow(
    onCalculatorClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onNewsClick: () -> Unit,
    onPricesClick: () -> Unit,
    onMultiTfClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ShortcutIcon(
            icon = Icons.Filled.Calculate,
            label = stringResource(R.string.home_icon_futures),
            gradient = listOf(GradientBlueStart, GradientBlueEnd),
            onClick = onCalculatorClick
        )
        ShortcutIcon(
            icon = Icons.Filled.History,
            label = stringResource(R.string.home_history_title),
            gradient = listOf(GradientGoldStart, GradientGoldEnd),
            onClick = onHistoryClick
        )
        ShortcutIcon(
            icon = Icons.Filled.Insights,
            label = stringResource(R.string.home_icon_market),
            gradient = listOf(GradientGreenStart, GradientGreenEnd),
            onClick = onNewsClick
        )
        ShortcutIcon(
            icon = Icons.Filled.ShowChart,
            label = stringResource(R.string.home_icon_prices),
            gradient = listOf(GradientPurpleStart, GradientPurpleEnd),
            onClick = onPricesClick
        )
        ShortcutIcon(
            icon = Icons.Filled.Timer,
            label = stringResource(R.string.home_icon_fear_greed),
            gradient = listOf(Color(0xFFFF8C42), AccentYellow),
            onClick = onMultiTfClick
        )
    }
}

@Composable
private fun ShortcutIcon(
    icon: ImageVector,
    label: String,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(Brush.linearGradient(gradient), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = Color.White, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun LatestAnalysesSection(
    onViewAll: () -> Unit,
    onItemClick: (String) -> Unit
) {
    var items by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        val fetched = withContext(Dispatchers.IO) { ArzdigitalNewsApi.fetchNews(3) }
        if (fetched != null) items = fetched.take(3)
        isLoading = false
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_last_analyses_title),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(R.string.home_last_analyses_view_all),
                    color = AccentBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onViewAll() }
                )
            }

            Spacer(Modifier.height(10.dp))

            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AccentBlue,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                }
                items.isEmpty() -> Text(
                    text = stringResource(R.string.home_last_analyses_empty),
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                else -> {
                    items.forEachIndexed { index, item ->
                        LatestAnalysisRow(item = item, onClick = { onItemClick(item.url) })
                        if (index < items.size - 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Divider)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LatestAnalysisRow(item: NewsItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    Brush.linearGradient(listOf(GradientBlueStart, GradientBlueEnd)),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "A",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = item.title,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            lineHeight = 16.sp
        )
        Spacer(Modifier.width(6.dp))
        Icon(
            Icons.Filled.OpenInNew,
            null,
            tint = AccentBlue,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun LimitStatusCard(
    remainingFree: Int,
    adsSinceReward: Int,
    adsPerReward: Int
) {
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
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(Divider, RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            if (adsPerReward > 0)
                                (adsSinceReward.toFloat() / adsPerReward.toFloat()).coerceIn(0f, 1f)
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
                text = if (adsPerReward - adsSinceReward > 0)
                    "${adsPerReward - adsSinceReward} تبلیغ دیگه = ۳ تحلیل رایگان"
                else "پاداش آماده است",
                color = AccentYellow,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
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
                    ) { Text("Chart", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold) }
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

private fun openExternalUrl(context: android.content.Context, url: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
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
                Log.d(TAG, "Requesting banner for zone: $zoneId")

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
                                    Log.d(TAG, "loadAd called with id: $adId")
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
                    Log.e(TAG, "Exception during request: ${e.message}", e)
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}