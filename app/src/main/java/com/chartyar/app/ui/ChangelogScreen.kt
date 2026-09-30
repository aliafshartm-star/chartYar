package com.chartyar.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.ui.theme.*

data class ChangelogEntry(
    val version: String,
    val date: String,
    val isLatest: Boolean,
    val changes: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen(
    onBack: () -> Unit
) {
    val changelog = remember {
        listOf(
            ChangelogEntry(
                version = "1.0.0",
                date = "1403/07/09",
                isLatest = true,
                changes = listOf(
                    "انتشار اولیه چارت یار",
                    "10+ اندیکاتور تکنیکال",
                    "11 الگوی کلاسیک",
                    "تحلیل با 4 صرافی (Binance, CoinGecko, CoinPaprika, CryptoCompare)",
                    "هشدار قیمت با نوتیفیکیشن",
                    "قیمت لحظه ای 30 ارز",
                    "اخبار بازار",
                    "دیکشنری اصطلاحات",
                    "آموزش تحلیل (7 درس)",
                    "پشتیبانی از فارسی و انگلیسی",
                    "حالت آفلاین",
                    "اشتراک گذاری متنی و تصویری",
                    "ویجت صفحه اصلی",
                    "App Shortcuts"
                )
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
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
                "تغییرات نسخه ها",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // توضیح
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📋", fontSize = 24.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "تاریخچه تغییرات اپ از ابتدا تا الان",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // لیست تغییرات
        changelog.forEach { entry ->
            ChangelogCard(entry = entry)
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ChangelogCard(entry: ChangelogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            // هدر کارت
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "نسخه ${entry.version}",
                            color = AccentBlue,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (entry.isLatest) {
                            Spacer(Modifier.width(8.dp))
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = AccentGreen.copy(alpha = 0.2f)
                                )
                            ) {
                                Text(
                                    text = "جدید",
                                    color = AccentGreen,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(
                                        horizontal = 8.dp,
                                        vertical = 2.dp
                                    )
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = entry.date,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = Divider)
            Spacer(Modifier.height(12.dp))

            // تغییرات
            entry.changes.forEach { change ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        color = AccentGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = change,
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}