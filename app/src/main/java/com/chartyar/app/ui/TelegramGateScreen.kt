package com.chartyar.app.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.AccentRed
import com.chartyar.app.ui.theme.AccentYellow
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.GradientBlueStart
import com.chartyar.app.ui.theme.GradientPurpleEnd
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary
import com.chartyar.app.util.TelegramVerifier
import kotlinx.coroutines.delay

private const val TELEGRAM_CHANNEL_URL = "https://t.me/viachart_app"

@Composable
fun TelegramGateScreen(
    onConfirm: () -> Unit,
    onDecline: () -> Unit
) {
    val context = LocalContext.current

    var session by remember { mutableStateOf<String?>(null) }
    var botUrl by remember { mutableStateOf<String?>(null) }
    var isRequesting by remember { mutableStateOf(false) }
    var isWaiting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var openCount by remember { mutableStateOf(0) }

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
        }
    }

    // Polling برای چک تأیید
    LaunchedEffect(session, isWaiting) {
        val s = session
        if (s != null && isWaiting) {
            while (true) {
                delay(3000L)
                val result = TelegramVerifier.checkVerification(s)
                if (result.verified) {
                    onConfirm()
                    return@LaunchedEffect
                } else if (result.status == "expired") {
                    isWaiting = false
                    errorMessage = "زمان تأیید تمام شد. دوباره تلاش کن."
                    session = null
                    botUrl = null
                    return@LaunchedEffect
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        // آیکون اصلی
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Brush.linearGradient(
                        listOf(GradientBlueStart, GradientPurpleEnd)
                    ),
                    RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "عضویت اجباری در کانال",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "برای استفاده از ViaChart، باید در کانال رسمی ما عضو شوی.\nاین کار برای اطلاع‌رسانی نسخه‌های جدید و تحلیل‌های ویژه است.",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(24.dp))

        // حالت‌ها
        when {
            // حالت ۱: درخواست در حال ارسال
            isRequesting -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = AccentBlue)
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "در حال آماده‌سازی...",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // حالت ۲: منتظر تأیید
            isWaiting && session != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = AccentYellow,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "در حال انتظار برای تأیید...",
                                color = AccentYellow,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Text(
                            "کد اختصاصی تو:",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = session ?: "",
                            color = TextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            "اگه ربات پیام تأیید نداده، برگرد به ربات و دکمه «عضو شدم» رو بزن",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(Modifier.height(16.dp))

                        // دکمه باز کردن مجدد ربات
                        Button(
                            onClick = {
                                val url = botUrl
                                if (url != null) {
                                    openUrl(url)
                                    openCount++
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentBlue,
                                contentColor = Color.White
                            )
                        ) {
                            Text(
                                "باز کردن ربات تلگرام",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "بعد از تأیید، این صفحه خودکار می‌رود",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // حالت ۳: شروع (دکمه عضویت)
            else -> {
                // دکمه اصلی
                Button(
                    onClick = {
                        if (isRequesting) return@Button
                        isRequesting = true
                        errorMessage = null

                        // درخواست session
                        kotlinx.coroutines.MainScope().let { scope ->
                            // از LaunchedEffect استفاده نمی‌کنیم چون اینجا event-based است
                        }

                        // استفاده از یه coroutine دستی
                        val scope = kotlinx.coroutines.CoroutineScope(
                            kotlinx.coroutines.Dispatchers.Main
                        )
                        scope.launch {
                            val result = TelegramVerifier.requestSession()
                            isRequesting = false
                            if (result.success && result.session != null && result.botUrl != null) {
                                session = result.session
                                botUrl = result.botUrl
                                isWaiting = true
                                openUrl(result.botUrl)
                            } else {
                                errorMessage = result.error ?: "خطا در اتصال"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "عضویت در کانال",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                // راهنما
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "📋 مراحل تأیید:",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "1. دکمه «عضویت در کانال» رو بزن\n" +
                                    "2. تلگرام باز می‌شه → در کانال عضو شو\n" +
                                    "3. برگرد به ربات و دکمه «Start» یا «عضو شدم» رو بزن\n" +
                                    "4. اپ خودکار تأیید می‌کنه و می‌ره داخل",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // نمایش خطا
        if (errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.1f))
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = AccentRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // دکمه خروج
        TextButton(
            onClick = onDecline,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "خروج از اپلیکیشن",
                color = AccentRed,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}