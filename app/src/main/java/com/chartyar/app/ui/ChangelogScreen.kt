package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider as DividerColor
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary

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
                    "🚀 استراتژی ترکیبی چند هوش مصنوعی (Multi-AI Strategy)",
                    "🧠 ترکیب ۳ موتور تحلیلی مستقل برای تأیید سیگنال",
                    "📊 تحلیل جفت ارز با الگوریتم پیشرفته هوش مصنوعی",
                    "🎯 تحلیل چند تایم‌فریمی (Multi-Timeframe)",
                    "⚡ بدون نیاز به آپلود عکس — مستقیم و دقیق",
                    "💰 قیمت لحظه‌ای ۲۰۰+ ارز به تومان و دلار",
                    "📈 صفحه جزئیات کامل هر ارز",
                    "🔔 هشدار قیمت هوشمند (تومان یا دلار)",
                    "⚡ چک خودکار هشدارها هر ۱۵ ثانیه",
                    "🏦 بخش صرافی‌ها با کد معرف و تخفیف کارمزد",
                    "📰 اخبار لحظه‌ای ارز دیجیتال",
                    "😱 شاخص ترس و طمع (Fear & Greed)",
                    "🧮 ماشین‌حساب فیوچرز (سود/زیان/لیکویید)",
                    "⛏ ماشین‌حساب استخراج بیت‌کوین",
                    "📚 دیکشنری اصطلاحات تریدینگ",
                    "🎓 درس‌های تحلیل تکنیکال از صفر",
                    "🕓 تاریخچه تحلیل‌ها برای پیگیری",
                    "🎨 طراحی لوکس به سبک Wallex",
                    "🌙 حالت تاریک حرفه‌ای",
                    "🔒 امنیت بالا — بدون ذخیره اطلاعات شخصی",
                    "🇮🇷 کاملاً فارسی و بومی‌سازی‌شده",
                    "📱 App Shortcuts و ویجت صفحه اصلی"
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "back", tint = TextPrimary)
            }
            Text(
                "تغییرات نسخه‌ها",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "📋", fontSize = 20.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "تاریخچه تغییرات اپ از ابتدا تا الان",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        for (entry in changelog) {
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
            Divider(color = DividerColor)
            Spacer(Modifier.height(12.dp))

            entry.changes.forEach { change ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = change,
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}