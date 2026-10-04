package com.chartyar.app.ui

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

data class TutorialPage(
    val emoji: String,
    val title: String,
    val description: String,
    val tips: List<String>
)

@Composable
private fun tutorialPages(): List<TutorialPage> = listOf(
    TutorialPage(
        emoji = "🎯",
        title = "۱. جفت ارز رو انتخاب کن",
        description = "از لیست ارزهای نوبیتکس، ارزی که می‌خوای تحلیلش کنی رو انتخاب کن (مثل BTCUSDT).",
        tips = listOf(
            "داده واقعی از نوبیتکس — بدون تأخیر",
            "بیش از ۲۰۰ جفت ارز فعال",
            "جستجوی سریع با نام یا نماد"
        )
    ),
    TutorialPage(
        emoji = "⏱",
        title = "۲. تایم‌فریم رو انتخاب کن",
        description = "قبل از انتخاب ارز، تایم‌فریم معاملاتت رو انتخاب کن: اسکالپ، سوئینگ یا پوزیشن.",
        tips = listOf(
            "اسکالپ: 15m",
            "سوئینگ: 1H و 4H",
            "پوزیشن: 1D"
        )
    ),
    TutorialPage(
        emoji = "📊",
        title = "۳. تحلیل حرفه‌ای بگیر",
        description = "اپ ۲۵ اندیکاتور، ۱۱ الگو و ۵ استراتژی ترید رو بررسی می‌کنه و سیگنال نهایی رو می‌ده.",
        tips = listOf(
            "نقطه ورود، حد ضرر و ۳ هدف",
            "نسبت ریسک به ریوارد",
            "دلایل تحلیلی کامل + هشدارها"
        )
    ),
    TutorialPage(
        emoji = "🚀",
        title = "۴. از همه امکانات استفاده کن",
        description = "تحلیل چند تایم‌فریمی، ماشین‌حساب فیوچرز، شاخص‌های بازار، هشدار قیمت و اخبار لحظه‌ای.",
        tips = listOf(
            "هشدار خودکار قیمت",
            "شاخص‌های بازار (BTC Dominance)",
            "محاسبه سود و لیکویید فیوچرز"
        )
    )
)

@Composable
fun TutorialScreen(
    isFirstTime: Boolean,
    onFinish: () -> Unit
) {
    val pages = tutorialPages()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        if (!isFirstTime) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                IconButton(onClick = onFinish) {
                    Icon(Icons.Filled.ArrowBack, "back", tint = TextPrimary)
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
                "بذار توی ۴ قدم سریع، با ViaChart آشنا بشی",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(16.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            TutorialPageContent(pages[page])
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pages.size) { index ->
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
                    if (pagerState.currentPage == pages.size - 1) {
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
                    if (pagerState.currentPage == pages.size - 1) "شروع کن!" else "بعدی",
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