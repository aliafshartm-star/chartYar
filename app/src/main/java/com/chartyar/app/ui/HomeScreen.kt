package com.chartyar.app.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.chartyar.app.R
import com.chartyar.app.data.FearGreedApi
import com.chartyar.app.data.FearGreedData
import com.chartyar.app.ui.theme.*
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.request.BannerSize
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.views.banner.BannerContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

// ============ Banner Zone ID تپسل ============
private const val TAPSELL_BANNER_ZONE_ID = "6abe61690a772b2b3f29c2a5"

// ============ لینک‌های سوشال ============
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
        // ============ هدر ============
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

        // ============ کارت شروع تحلیل ============
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn)
        ) {
            MainUploadCard(onUploadClick = onUploadClick)
        }

        Spacer(Modifier.height(14.dp))

        // ============ کارت Multi-TF ============
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn)
        ) {
            MultiTfCard(onClick = onMultiTfClick)
        }

        Spacer(Modifier.height(14.dp))

        // ============ دکمه تاریخچه ============
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

        // ============ کارت شاخص ترس و طمع ============
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn)
        ) {
            FearGreedCard()
        }

        Spacer(Modifier.height(12.dp))

        // ============ ردیف شبکه‌های اجتماعی ============
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fadeIn)
        ) {
            SocialLinksRow()
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

// ============ کارت شاخص ترس و طمع ============
@Composable
private fun FearGreedCard() {
    var data by remember { mutableStateOf<FearGreedData?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // دریافت داده از API
    LaunchedEffect(Unit) {
        isLoading = true
        val result = withContext(Dispatchers.IO) {
            FearGreedApi.fetchFearGreed()
        }
        data = result
        isLoading = false
    }

    // آپدیت خودکار هر ۳۰ دقیقه
    LaunchedEffect(Unit) {
        while (true) {
            delay(30 * 60 * 1000L) // ۳۰ دقیقه
            val result = withContext(Dispatchers.IO) {
                FearGreedApi.fetchFearGreed()
            }
            if (result != null) data = result
        }
    }

    val value = data?.value ?: 0
    val color = when {
        value <= 25 -> AccentRed
        value <= 45 -> Color(0xFFE8A33D)
        value <= 55 -> AccentYellow
        value <= 75 -> Color(0xFF7BC97C)
        else -> AccentGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "شاخص ترس و طمع",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "حس کلی بازار کریپتو",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        color = AccentBlue,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (data == null) {
                    Text(
                        text = "ناموجود",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    // دایره با عدد
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${data!!.value}",
                            color = color,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (data != null && !isLoading) {
                Spacer(Modifier.height(12.dp))

                // نوار پیشرفت
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = value / 100f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color)
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ترس شدید",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = data!!.classificationFa,
                        color = color,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "طمع شدید",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// ============ ردیف شبکه‌های اجتماعی ============
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
            // silent
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ما را دنبال کنید:",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )

            // تلگرام
            SocialIcon(
                iconRes = R.drawable.ic_telegram,
                backgroundColor = Color(0xFF229ED9),
                contentDescription = "تلگرام",
                onClick = { openUrl(TELEGRAM_URL) }
            )

            Spacer(Modifier.width(12.dp))

            // اینستاگرام
            SocialIcon(
                iconRes = R.drawable.ic_instagram,
                backgroundColor = Color(0xFFE4405F),
                contentDescription = "اینستاگرام",
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
            .size(44.dp)
            .background(backgroundColor, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
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
                                    container.showAd(adId)
                                } catch (e: Exception) {
                                    // silent
                                }
                            }

                            override fun onFailure(message: String) {
                                // silent
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

// ============ کارت Multi-TF ============
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

// ============ کارت شروع تحلیل ============
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