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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

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
                Icon(Icons.Filled.ArrowBack, "بازگشت", tint = TextPrimary)
            }
            Text(
                "حریم خصوصی",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            "آخرین به‌روزرسانی: ۱۴۰۳/۰۷/۰۸",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(16.dp))

        SectionCard("۱. اطلاعاتی که جمع‌آوری می‌کنیم") {
            BulletText("تحلیل‌های انجام‌شده شما (به‌صورت محلی روی دستگاه)")
            BulletText("تنظیمات ترجیحی شما (تم، تایم‌فریم پیش‌فرض، ...)")
            BulletText("تاریخچه تحلیل‌های اخیر (به‌صورت محلی)")
            Spacer(Modifier.height(8.dp))
            Text(
                "⚠️ هیچ اطلاعات هویتی شما (نام، ایمیل، شماره تلفن) جمع‌آوری نمی‌شود.",
                color = AccentYellow,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۲. اطلاعاتی که جمع‌آوری نمی‌کنیم") {
            BulletText("نام، ایمیل، شماره تلفن یا اطلاعات هویتی")
            BulletText("موقعیت مکانی (GPS)")
            BulletText("مخاطبین، پیام‌ها، تصاویر یا فایل‌های شخصی")
            BulletText("اطلاعات حساب‌های کاربری")
            BulletText("شناسه‌های تبلیغاتی دستگاه")
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۳. استفاده از اینترنت") {
            BulletText("داده‌های قیمت و کندل از Binance API دریافت می‌شود")
            BulletText("این اتصال فقط برای تحلیل دقیق‌تر انجام می‌شود")
            BulletText("هیچ داده‌ای از شما به Binance ارسال نمی‌شود")
            BulletText("در صورت عدم دسترسی به اینترنت، تحلیل آفلاین انجام می‌شود")
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۴. ذخیره‌سازی داده‌ها") {
            BulletText("تمام داده‌ها به‌صورت محلی روی دستگاه شما ذخیره می‌شود")
            BulletText("هیچ سروری از ما داده‌های شما را ذخیره نمی‌کند")
            BulletText("با حذف اپ، تمام داده‌ها پاک می‌شود")
            BulletText("می‌توانید از تنظیمات، تاریخچه را پاک کنید")
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۵. اشتراک‌گذاری با شخص ثالث") {
            BulletText("ما هیچ اطلاعاتی را با شخص ثالث به اشتراک نمی‌گذاریم")
            BulletText("تبلیغات شخصی‌سازی‌شده نداریم")
            BulletText("از داده‌های شما برای هیچ هدف تجاری استفاده نمی‌کنیم")
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۶. حق شما") {
            BulletText("می‌توانید هر زمان اپ را حذف کنید")
            BulletText("می‌توانید تاریخچه تحلیل‌ها را پاک کنید")
            BulletText("می‌توانید تنظیمات را به حالت پیش‌فرض برگردانید")
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۷. تماس با ما") {
            Text(
                "در صورت هرگونه سوال یا نگرانی درباره حریم خصوصی، از طریق ایمیل زیر با ما تماس بگیرید:",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "📧 support@chartyar.app",
                color = AccentBlue,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۸. تغییرات این سیاست") {
            Text(
                "ما ممکن است این سیاست را هر از چند گاه به‌روزرسانی کنیم. تغییرات مهم از طریق اپ اطلاع‌رسانی خواهد شد. استفاده مداوم شما از اپ به معنای پذیرش سیاست جدید است.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "با استفاده از چارت‌یار، شما این سیاست حریم خصوصی را می‌پذیرید.",
            color = AccentBlue,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                title,
                color = AccentBlue,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun BulletText(text: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text("•  ", color = AccentBlue, fontSize = 14.sp)
        Text(
            text,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 20.sp
        )
    }
}