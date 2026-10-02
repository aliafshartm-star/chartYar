package com.chartyar.app.ui

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.R
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary

private const val TELEGRAM_URL = "https://t.me/chartyar_app"
private const val INSTAGRAM_URL = "https://www.instagram.com/chartyar_app"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
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
                "درباره چارت‌یار",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "📊", fontSize = 64.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "چارت‌یار",
                    color = AccentBlue,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "هوشمندترین دستیار تحلیل ارز دیجیتال",
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "دقیق، سریع و حرفه‌ای",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        InfoCard(
            title = "چرا چارت‌یار؟",
            lines = listOf(
                "🚀 استراتژی ترکیبی چند هوش مصنوعی (Multi-AI Strategy)",
                "🎯 نرخ موفقیت بهینه با مدیریت ریسک پیشرفته",
                "🧠 ترکیب ۳ موتور تحلیلی مستقل برای تأیید سیگنال",
                "⚡ دقت بالاتر از تحلیل انسانی در تست‌های مقایسه‌ای",
                "📊 داده‌های لحظه‌ای و به‌روز بازار",
                "🔒 امن، بدون ذخیره اطلاعات شخصی",
                "💎 طراحی لوکس و رابط کاربری مدرن",
                "🇮🇷 کاملاً فارسی و بومی‌سازی‌شده",
                "🆓 کاملاً رایگان — بدون نیاز به اشتراک"
            )
        )

        Spacer(Modifier.height(12.dp))

        InfoCard(
            title = "امکانات بی‌نظیر",
            lines = listOf(
                "تحلیل جفت ارز با الگوریتم پیشرفته هوش مصنوعی",
                "تحلیل چند تایم‌فریمی برای تأیید سیگنال قوی‌تر",
                "قیمت لحظه‌ای ۲۰۰+ ارز به تومان و دلار",
                "صفحه جزئیات کامل هر ارز",
                "هشدار قیمت هوشمند (تومان یا دلار)",
                "چک خودکار هر ۱۵ ثانیه برای پامپ‌ها",
                "اخبار لحظه‌ای ارز دیجیتال",
                "شاخص ترس و طمع برای درک حس بازار",
                "ماشین‌حساب فیوچرز (سود/زیان/لیکویید)",
                "ماشین‌حساب استخراج بیت‌کوین",
                "صرافی‌ها با تخفیف کارمزد اختصاصی",
                "دیکشنری کامل اصطلاحات تریدینگ",
                "درس‌های آموزشی تحلیل تکنیکال از صفر",
                "تاریخچه تحلیل‌ها برای پیگیری",
                "حالت تاریک حرفه‌ای (Wallex Style)"
            )
        )

        Spacer(Modifier.height(12.dp))

        InfoCard(
            title = "برای چه کسانی؟",
            lines = listOf(
                "✅ تریدرهای تازه‌کار که به دنبال یادگیری هستند",
                "✅ معامله‌گران حرفه‌ای که دنبال تحلیل سریع‌تر هستند",
                "✅ سرمایه‌گذاران بلندمدت برای بررسی نقاط ورود",
                "✅ هر کسی که می‌خواهد در بازار کریپتو موفق‌تر باشد"
            )
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "ارتباط با ما",
                    color = AccentBlue,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "سؤالی داری؟ پیشنهادی؟ با ما در تماس باش",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SocialContactButton(
                        iconRes = R.drawable.ic_telegram,
                        label = "تلگرام",
                        backgroundColor = Color(0xFF229ED9),
                        onClick = { openUrl(TELEGRAM_URL) },
                        modifier = Modifier.weight(1f)
                    )
                    SocialContactButton(
                        iconRes = R.drawable.ic_instagram,
                        label = "اینستاگرام",
                        backgroundColor = Color(0xFFE4405F),
                        onClick = { openUrl(INSTAGRAM_URL) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        InfoCard(
            title = "قوانین",
            lines = listOf(
                "استفاده از اپ به معنی پذیرش شرایط است",
                "تحلیل‌ها جنبه آموزشی دارند",
                "مسئولیت معاملات بر عهده کاربر است",
                "اطلاعات شخصی شما جمع‌آوری نمی‌شود"
            )
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "ساخته شده با ❤️ در ایران",
            color = TextSecondary,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "تمام حقوق محفوظ است",
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
                    text = line,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun SocialContactButton(
    iconRes: Int,
    label: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val painter: Painter = painterResource(id = iconRes)

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkBg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painter,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = label,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}