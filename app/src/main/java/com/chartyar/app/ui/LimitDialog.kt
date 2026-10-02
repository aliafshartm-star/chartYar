package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
import com.chartyar.app.ui.theme.GradientPurpleEnd
import com.chartyar.app.ui.theme.GradientPurpleStart
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary

enum class LimitDialogType {
    DAILY_LIMIT,
    LOCKED_STRATEGY
}

@Composable
fun LimitDialog(
    type: LimitDialogType,
    strategyName: String = "",
    strategyEmoji: String = "",
    adsCount: Int,
    adsForBonus: Int,
    isAdLoading: Boolean,
    isAdReady: Boolean,
    onWatchAd: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "close",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                val icon: ImageVector = when (type) {
                    LimitDialogType.DAILY_LIMIT -> Icons.Filled.Timer
                    LimitDialogType.LOCKED_STRATEGY -> Icons.Filled.Lock
                }

                val gradient: List<Color> = when (type) {
                    LimitDialogType.DAILY_LIMIT -> listOf(GradientBlueStart, GradientBlueEnd)
                    LimitDialogType.LOCKED_STRATEGY -> listOf(GradientPurpleStart, GradientPurpleEnd)
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.linearGradient(colors = gradient),
                            shape = RoundedCornerShape(40.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                val title = when (type) {
                    LimitDialogType.DAILY_LIMIT -> "سهمیه تحلیل‌های امروز تموم شد"
                    LimitDialogType.LOCKED_STRATEGY -> "استراتژی $strategyName قفله"
                }

                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                val subtitle = when (type) {
                    LimitDialogType.DAILY_LIMIT ->
                        "با تماشای یک ویدیو کوتاه، ۲ تحلیل دیگه دریافت کن"
                    LimitDialogType.LOCKED_STRATEGY ->
                        "برای استفاده از استراتژی $strategyName، یک ویدیو تماشا کن و ۲ تحلیل بگیر"
                }

                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(20.dp))

                LoyaltyCard(
                    adsCount = adsCount,
                    adsForBonus = adsForBonus
                )

                Spacer(Modifier.height(20.dp))

                val isWatchEnabled = !isAdLoading && isAdReady
                val buttonText = when {
                    isAdLoading -> "در حال بارگذاری تبلیغ..."
                    !isAdReady -> "تبلیغی موجود نیست"
                    else -> "تماشای ویدیو و دریافت ۲ تحلیل"
                }

                val buttonColor = if (isWatchEnabled) AccentGreen else DarkCard
                val buttonTextColor = if (isWatchEnabled) Color.White else TextSecondary

                Button(
                    onClick = onWatchAd,
                    enabled = isWatchEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor,
                        contentColor = buttonTextColor,
                        disabledContainerColor = DarkCard,
                        disabledContentColor = TextSecondary
                    )
                ) {
                    if (isWatchEnabled) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    } else if (!isAdLoading) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = buttonText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (isAdReady && !isAdLoading) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "تبلیغ آماده است",
                        color = AccentGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else if (!isAdLoading && !isAdReady) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "لطفاً چند لحظه دیگه دوباره امتحان کن",
                        color = AccentYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "سهمیه جدید: فردا صبح زود",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "بعداً",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LoyaltyCard(
    adsCount: Int,
    adsForBonus: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = AccentYellow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "پاداش وفاداری",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "$adsCount / $adsForBonus",
                    color = AccentYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        color = Divider,
                        shape = RoundedCornerShape(3.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            fraction = if (adsForBonus > 0) {
                                (adsCount.toFloat() / adsForBonus.toFloat())
                                    .coerceIn(0f, 1f)
                            } else 0f
                        )
                        .height(6.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(AccentYellow, Color(0xFFFF8C42))
                            ),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = if (adsCount >= adsForBonus) {
                    "🎉 پاداش فعال شد! ۱۲ ساعت نامحدود"
                } else {
                    "${adsForBonus - adsCount} تبلیغ دیگه = ۱۲ ساعت نامحدود!"
                },
                color = if (adsCount >= adsForBonus) AccentGreen else AccentYellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}