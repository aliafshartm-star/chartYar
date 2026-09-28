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
fun TermsScreen(
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
                "شرایط استفاده",
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

        SectionCard("۱. پذیرش شرایط") {
            Text(
                "با نصب و استفاده از اپلیکیشن چارت‌یار، شما این شرایط را می‌پذیرید. اگر با هر بخشی از این شرایط موافق نیستید، لطفاً از اپ استفاده نکنید.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۲. ماهیت اپلیکیشن") {
            BulletText("چارت‌یار یک ابزار تحلیل تکنیکال است")
            BulletText("این اپ توصیه مالی، سرمایه‌گذاری یا معاملاتی ارائه نمی‌دهد")
            BulletText("تحلیل‌ها صرفاً جنبه آموزشی و اطلاعاتی دارند")
            BulletText("اپ جایگزین مشاوره تخصصی مالی نیست")
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۳. سلب مسئولیت مالی") {
            Text(
                "⚠️ هشدار مهم:",
                color = AccentRed,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "بازارهای مالی (کریپتو، فارکس، سهام) پر از ریسک هستند و ممکن است سرمایه شما به‌طور کامل از دست برود. تمام تصمیمات خرید و فروش کاملاً بر عهده شماست. تیم چارت‌یار هیچ‌گونه مسئولیتی در قبال سود یا ضرر مالی شما ندارد.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "فقط با پولی معامله کنید که توان از دست دادنش را دارید.",
                color = AccentYellow,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۴. دقت تحلیل‌ها") {
            BulletText("تحلیل‌ها بر اساس اندیکاتورهای تکنیکال و الگوهای کلاسیک است")
            BulletText("دقت تحلیل‌ها ۱۰۰٪ نیست و ممکن است اشتباه باشد")
            BulletText("شرایط بازار می‌تواند به‌سرعت تغییر کند")
            BulletText("هیچ تضمینی برای سود وجود ندارد")
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۵. استفاده مجاز") {
            BulletText("استفاده شخصی و غیرتجاری")
            BulletText("عدم تلاش برای مهندسی معکوس یا کپی اپ")
            BulletText("عدم استفاده از اپ برای فعالیت‌های غیرقانونی")
            BulletText("عدم انتشار تحلیل‌ها به‌عنوان توصیه مالی")
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۶. مالکیت معنوی") {
            Text(
                "تمام حقوق مادی و معنوی اپلیکیشن چارت‌یار، شامل کد، طراحی، لوگو و محتوا، متعلق به تیم چارت‌یار است. هرگونه کپی‌برداری یا استفاده غیرمجاز پیگرد قانونی دارد.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۷. تغییرات سرویس") {
            BulletText("ممکن است بخشی از امکانات حذف یا اضافه شود")
            BulletText("ممکن است نسخه‌های جدیدی منتشر شود")
            BulletText("ممکن است دسترسی به بعضی امکانات تغییر کند")
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۸. محدودیت مسئولیت") {
            Text(
                "تیم چارت‌یار مسئولیتی در قبال هرگونه خسارت مستقیم، غیرمستقیم، تصادفی یا تبعی ناشی از استفاده از این اپ ندارد. این شامل از دست دادن سود، داده‌ها یا فرصت‌های معاملاتی می‌شود.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۹. قانون حاکم") {
            Text(
                "این شرایط تابع قوانین جمهوری اسلامی ایران است. هرگونه اختلاف از طریق مراجع قانونی مربوطه پیگیری خواهد شد.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 22.sp
            )
        }

        Spacer(Modifier.height(12.dp))

        SectionCard("۱۰. تماس با ما") {
            Text(
                "برای هرگونه سوال، انتقاد یا پیشنهاد:",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "📧 support@chartyar.app",
                color = AccentBlue,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "با استفاده از چارت‌یار، شما این شرایط را پذیرفته‌اید.",
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