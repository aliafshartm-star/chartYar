package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.ui.theme.*
import kotlinx.coroutines.launch

data class TutorialPage(
    val emoji: String,
    val title: String,
    val description: String,
    val tips: List<String>
)

private val tutorialPages = listOf(
    TutorialPage(
        emoji = "📸",
        title = "۱. اسکرین‌شات چارت بگیر",
        description = "از چارت ارز موردنظرت توی TradingView یا هر پلتفرم دیگه یه اسکرین‌شات بگیر.",
        tips = listOf(
            "بهترین حالت: تم دارک TradingView",
            "مطمئن شو کندل‌ها واضح دیده می‌شن",
            "محور قیمت سمت راست باید مشخص باشه"
        )
    ),
    TutorialPage(
        emoji = "🎯",
        title = "۲. ارز رو انتخاب کن",
        description = "توی اپ، تصویر رو آپلود کن و از لیست، ارزی که تحلیلش می‌خوای رو انتخاب کن (مثل BTCUSDT).",
        tips = listOf(
            "داده واقعی از Binance دریافت می‌شه",
            "دقت تحلیل ۱۰۰٪ — بدون OCR",
            "بدون اینترنت هم کار می‌کنه (با OCR)"
        )
    ),
    TutorialPage(
        emoji = "⏱",
        title = "۳. تایم‌فریم رو انتخاب کن",
        description = "تایم‌فریم معاملاتت رو انتخاب کن: اسکالپ، سوئینگ یا پوزیشن.",
        tips = listOf(
            "اسکالپ: 1m, 5m, 15m, 30m",
            "سوئینگ: 1H, 4H",
            "پوزیشن: 1D, 1W, 1M"
        )
    ),
    TutorialPage(
        emoji = "📊",
        title = "۴. تحلیل حرفه‌ای بگیر",
        description = "اپ ۱۰+ اندیکاتور، ۱۱ الگو و ۴ استایل ترید رو بررسی می‌کنه و سیگنال نهایی رو می‌ده.",
        tips = listOf(
            "نقطه ورود، حد ضرر و ۳ هدف",
            "نسبت ریسک به ریوارد",
            "دلایل تحلیلی کامل + هشدارها"
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(
    isFirstTime: Boolean,
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { tutorialPages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // هدر (اگه اولین بار نیست، دکمه بازگشت نشون بده)
        if (!isFirstTime) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                IconButton(onClick = onFinish) {
                    Icon(Icons.Filled.ArrowBack, "بازگشت", tint = TextPrimary)
                }
                Text(
                    "راهنمای استفاده",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Spacer(Modifier.height(20.dp))
            Text(
                "خوش آمدی 👋",
                style = MaterialTheme.typography.headlineLarge,
                color = AccentBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "بذار توی ۴ قدم سریع، با چارت‌یار آشنا بشی",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        // اسلایدها
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            TutorialPageContent(tutorialPages[page])
        }

        // نقطه‌های راهنما
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(tutorialPages.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == pagerState.currentPage) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) AccentBlue
                            else Divider
                        )
                )
            }
        }

        // دکمه‌ها
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pagerState.currentPage > 0) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Text("قبلی")
                }
            }

            Button(
                onClick = {
                    if (pagerState.currentPage == tutorialPages.size - 1) {
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    if (pagerState.currentPage == tutorialPages.size - 1) "شروع کن!" else "بعدی",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        if (isFirstTime) {
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("رد کردن", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun TutorialPageContent(page: TutorialPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // ایموجی بزرگ
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(50))
                .background(AccentBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(page.emoji, fontSize = 72.sp)
        }

        Spacer(Modifier.height(32.dp))

        Text(
            page.title,
            color = TextPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            page.description,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(24.dp))

        // نکات
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                page.tips.forEach { tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("💡 ", fontSize = 14.sp)
                        Text(
                            tip,
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}