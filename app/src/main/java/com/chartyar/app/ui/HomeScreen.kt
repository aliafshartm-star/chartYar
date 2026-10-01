package com.chartyar.app.ui

import android.app.Activity
import android.view.ViewGroup
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.chartyar.app.ui.theme.*
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.BannerSize
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.views.banner.BannerContainer
import kotlinx.coroutines.delay

// ============ Banner Zone ID تپسل ============
private const val TAPSELL_BANNER_ZONE_ID = "6abe61690a772b2b3f29c2a5"

@Composable
fun HomeScreen(
    onUploadClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAlertsClick: () -> Unit,
    onNewsClick: () -> Unit,
    onPricesClick: () -> Unit,
    onMultiTfClick: () -> Unit
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "چارت‌یار",
                style = MaterialTheme.typography.headlineLarge,
                color = AccentBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onPricesClick) {
                Icon(
                    imageVector = Icons.Filled.TrendingUp,
                    contentDescription = "قیمت‌ها",
                    tint = AccentBlue
                )
            }
            IconButton(onClick = onNewsClick) {
                Icon(
                    imageVector = Icons.Filled.Article,
                    contentDescription = "اخبار",
                    tint = AccentGreen
                )
            }
            IconButton(onClick = onAlertsClick) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "هشدار قیمت",
                    tint = AccentYellow
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "تنظیمات",
                    tint = TextSecondary
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "تحلیل حرفه‌ای چارت با یک کلیک",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier.alpha(fadeIn)
        )

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn)
        ) {
            MainUploadCard(onUploadClick = onUploadClick)
        }

        Spacer(Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn)
        ) {
            MultiTfCard(onClick = onMultiTfClick)
        }

        Spacer(Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn)
        ) {
            OutlinedButton(
                onClick = onHistoryClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("تاریخچه تحلیل‌ها", style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn)
        ) {
            QuickStatsRow()
        }

        Spacer(Modifier.height(12.dp))

        // ============ بنر تبلیغاتی تپسل ============
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn)
        ) {
            TapsellBannerAd(zoneId = TAPSELL_BANNER_ZONE_ID)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "این اپ توصیه مالی نیست | تحلیل صرفاً جهت کمک به تصمیم‌گیری",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============ بنر تبلیغاتی تپسل ============
@Composable
fun TapsellBannerAd(zoneId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? Activity

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
            if (activity != null) {
                try {
                    Tapsell.requestBannerAd(
                        zoneId,
                        BannerSize.BANNER_320_50,
                        object : RequestResultListener {
                            override fun onSuccess(adId: String) {
                                try {
                                    Tapsell.showBannerAd(
                                        adId,
                                        container,
                                        activity,
                                        object : AdStateListener.Banner {
                                            override fun onAdImpression() {
                                            }

                                            override fun onAdClicked() {
                                            }

                                            override fun onAdFailed(message: String) {
                                            }
                                        }
                                    )
                                } catch (e: Exception) {
                                    // silent
                                }
                            }

                            override fun onFailure(message: String) {
                                // تبلیغ در دسترس نیست
                            }
                        }
                    )
                } catch (e: Exception) {
                    // silent
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun MultiTfCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = AccentGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎯", fontSize = 24.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "تحلیل چند تایم‌فریمی",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "۴ تایم‌فریم همزمان + تشخیص هماهنگی",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun MainUploadCard(onUploadClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulseScale)
                    .background(
                        color = AccentBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📊", fontSize = 42.sp)
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "شروع تحلیل",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "اسکرین‌شات چارت خود را از گالری انتخاب کنید",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onUploadClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    contentColor = TextPrimary
                )
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "انتخاب تصویر چارت",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}

@Composable
private fun QuickStatsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(14.dp))
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickStat("⚡", "۱۰+", "اندیکاتور")
        Box(
            modifier = Modifier
                .height(40.dp)
                .width(1.dp)
                .background(Divider)
        )
        QuickStat("🔍", "۱۱", "الگو")
        Box(
            modifier = Modifier
                .height(40.dp)
                .width(1.dp)
                .background(Divider)
        )
        QuickStat("🎯", "۴", "استایل")
    }
}

@Composable
private fun QuickStat(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            color = AccentBlue,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}