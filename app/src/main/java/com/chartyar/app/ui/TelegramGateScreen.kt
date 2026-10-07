package com.chartyar.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import kotlinx.coroutines.delay

private const val TELEGRAM_CHANNEL_URL = "https://t.me/viachart_app"
private const val WAIT_SECONDS = 15

@Composable
fun TelegramGateScreen(
    onConfirm: () -> Unit,
    onDecline: () -> Unit
) {
    val context = LocalContext.current

    // step:
    // 0 = صفحه اول
    // 1 = در حال بررسی (بار اول)
    // 2 = هنوز عضو نیستی
    // 3 = در حال تأیید (۱۵ ثانیه)
    // 4 = تأیید شد، آماده ورود
    var step by remember { mutableStateOf(0) }
    var countdown by remember { mutableStateOf(WAIT_SECONDS) }

    fun openChannel() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_CHANNEL_URL)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
        }
    }

    LaunchedEffect(step) {
        when (step) {
            1 -> {
                // بار اول: ۲ ثانیه بررسی → پیام خطا
                delay(2000)
                step = 2
            }
            3 -> {
                // بار دوم: ۱۵ ثانیه شمارش معکوس
                countdown = WAIT_SECONDS
                while (countdown > 0) {
                    delay(1000)
                    countdown--
                }
                step = 4
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

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Brush.linearGradient(listOf(GradientBlueStart, GradientPurpleEnd)),
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
            text = "برای استفاده از ViaChart باید در کانال رسمی ما عضو شوی",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(32.dp))

        when (step) {
            0 -> InitialStep(
                onJoinClick = { openChannel() },
                onCheckClick = { step = 1 }
            )
            1 -> CheckingStep(message = "در حال بررسی عضویت...")
            2 -> NotMemberStep(
                onJoinClick = { openChannel() },
                onCheckClick = { step = 3 }
            )
            3 -> WaitingStep(countdown = countdown)
            4 -> ReadyStep(onEnterClick = { onConfirm() })
        }

        Spacer(Modifier.weight(1f))

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

@Composable
private fun InitialStep(
    onJoinClick: () -> Unit,
    onCheckClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onJoinClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Filled.Send, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("عضویت در کانال", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = onCheckClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGreen,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("عضو شدم", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun CheckingStep(message: String) {
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
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun NotMemberStep(
    onJoinClick: () -> Unit,
    onCheckClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("❌", fontSize = 42.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "هنوز عضو کانال نیستی!",
                    color = AccentRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "لطفاً اول در کانال عضو شو، بعد برگرد و دوباره بررسی کن",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onJoinClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Filled.Send, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("برو تلگرام، عضو شو", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(Modifier.height(10.dp))

        Button(
            onClick = onCheckClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGreen,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("عضو شدم", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun WaitingStep(countdown: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = AccentYellow)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "در حال تأیید عضویت...",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "لطفاً صبر کنید",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "$countdown",
                color = AccentYellow,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "ثانیه",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ReadyStep(onEnterClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("✅", fontSize = 42.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "عضویت تأیید شد!",
                    color = AccentGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "حالا می‌تونی وارد اپ بشی",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onEnterClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGreen,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("عضو شدم، وارد شو", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}