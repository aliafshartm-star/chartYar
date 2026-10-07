package com.chartyar.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

@Composable
fun TelegramGateScreen(
    onConfirm: () -> Unit,
    onDecline: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var session by remember { mutableStateOf<String?>(null) }
    var botUrl by remember { mutableStateOf<String?>(null) }
    var isRequesting by remember { mutableStateOf(false) }
    var isChecking by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
        }
    }

    fun verifyMembership() {
        val s = session ?: return
        if (isChecking) return

        isChecking = true
        errorMessage = null
        infoMessage = null

        scope.launch {
            val result = TelegramVerifier.checkVerification(s)
            isChecking = false

            when {
                result.verified -> {
                    onConfirm()
                }
                result.status == "expired" -> {
                    errorMessage = "زمان تأیید تمام شد. دوباره شروع کن."
                    session = null
                    botUrl = null
                }
                result.status == "pending" -> {
                    errorMessage = "هنوز عضو کانال نشدی! اول عضو شو، بعد دوباره بررسی کن."
                }
                else -> {
                    errorMessage = "خطا در بررسی. دوباره تلاش کن."
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

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
            text = "برای استفاده از ViaChart باید در کانال رسمی ما عضو شوی",
            color = TextSecondary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(24.dp))

        when {
            session == null -> {
                // حالت ۱: هنوز شروع نکرده
                Button(
                    onClick = {
                        if (isRequesting) return@Button
                        isRequesting = true
                        errorMessage = null

                        scope.launch {
                            val result = TelegramVerifier.requestSession()
                            isRequesting = false
                            if (result.success && result.session != null && result.botUrl != null) {
                                session = result.session
                                botUrl = result.botUrl
                                openUrl(result.botUrl)
                            } else {
                                errorMessage = result.error ?: "خطا در اتصال"
                            }
                        }
                    },
                    enabled = !isRequesting,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = Color.White
                    )
                ) {
                    if (isRequesting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("در حال آماده‌سازی...")
                    } else {
                        Icon(Icons.Filled.Send, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("عضویت در کانال", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(16.dp))

                InfoCard()
            }

            else -> {
                // حالت ۲: رفته تلگرام، منتظر تأیید
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "مراحل باقی‌مونده:",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        StepRow("۱", "توی تلگرام عضو کانال شو", true)
                        StepRow("۲", "برگرد به اپ و دکمه زیر رو بزن", false)

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "کد اختصاصی تو:",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = session ?: "",
                            color = AccentBlue,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // دکمه اصلی: بررسی مجدد
                Button(
                    onClick = { verifyMembership() },
                    enabled = !isChecking,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        contentColor = Color.White
                    )
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("در حال بررسی...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("عضو شدم، تأیید کن", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(10.dp))

                // دکمه کمکی: باز کردن مجدد تلگرام
                TextButton(
                    onClick = {
                        val url = botUrl
                        if (url != null) openUrl(url)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        null,
                        tint = AccentYellow,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "باز کردن مجدد تلگرام",
                        color = AccentYellow,
                        fontSize = 14.sp
                    )
                }
            }
        }

        if (errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AccentRed.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = AccentRed,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

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

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StepRow(number: String, text: String, isDone: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    if (isDone) AccentGreen else AccentBlue.copy(alpha = 0.3f),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "مراحل تأیید:",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "1. دکمه عضویت رو بزن\n" +
                        "2. توی تلگرام عضو کانال شو\n" +
                        "3. برگرد به ViaChart\n" +
                        "4. روی «عضو شدم، تأیید کن» بزن\n" +
                        "5. وارد اپ می‌شی ✅",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 20.sp
            )
        }
    }
}