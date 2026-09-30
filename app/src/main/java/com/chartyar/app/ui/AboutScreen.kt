package com.chartyar.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // هدر
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "بازگشت", tint = TextPrimary)
            }
            Text(
                "درباره چارت یار",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // لوگو و نام
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "chart", fontSize = 64.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "چارت یار",
                    color = AccentBlue,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "نسخه 1.0.0",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "تحلیل حرفه ای چارت با هوش مصنوعی",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // اطلاعات
        InfoCard(
            title = "درباره اپ",
            lines = listOf(
                "چارت یار یک ابزار تحلیل تکنیکال است",
                "با 10+ اندیکاتور و 11 الگوی کلاسیک",
                "داده های واقعی از Binance و صرافی های دیگر",
                "بدون توصیه مالی - فقط کمک به تصمیم گیری"
            )
        )

        Spacer(Modifier.height(12.dp))

        InfoCard(
            title = "امکانات",
            lines = listOf(
                "RSI، MACD، EMA، Bollinger، Stochastic",
                "تشخیص الگوهای کلاسیک",
                "هشدار قیمت با نوتیفیکیشن",
                "اخبار بازار",
                "قیمت لحظه ای 30 ارز",
                "تاریخچه تحلیل ها",
                "دیکشنری اصطلاحات"
            )
        )

        Spacer(Modifier.height(12.dp))

        // تماس
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "تماس با ما",
                    color = AccentBlue,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(10.dp))

                ContactRow(
                    label = "ایمیل پشتیبانی",
                    value = "support@chartyar.app",
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:support@chartyar.app")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // silent
                        }
                    }
                )

                Spacer(Modifier.height(8.dp))

                ContactRow(
                    label = "گیت هاب",
                    value = "github.com/chartyar",
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://github.com/chartyar")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // silent
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // قوانین
        InfoCard(
            title = "قوانین",
            lines = listOf(
                "استفاده از اپ به معنی پذیرش شرایط است",
                "تحلیل ها جنبه آموزشی دارند",
                "مسئولیت معاملات بر عهده کاربر است",
                "اطلاعات شخصی شما جمع آوری نمی شود"
            )
        )

        Spacer(Modifier.height(12.dp))

        // کپی رایت
        Text(
            text = "ساخته شده با عشق در ایران",
            color = TextSecondary,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "تمام حقوق محفوظ است © 1403",
            color = TextSecondary,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun InfoCard(title: String, lines: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = title,
                color = AccentBlue,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            lines.forEach { line ->
                Text(
                    text = "• $line",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun ContactRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                color = AccentBlue,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(text = ">", color = AccentBlue, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}