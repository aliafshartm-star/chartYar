package com.chartyar.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentRed
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary

@Composable
fun TermsScreen(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var checked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        Text(
            text = "شرایط استفاده از ViaChart",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "لطفاً با دقت مطالعه کنید",
            color = TextSecondary,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(12.dp))

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, Divider)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                TermsContent()
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { checked = !checked }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { checked = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = AccentBlue,
                    uncheckedColor = TextSecondary,
                    checkmarkColor = Color.White
                )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "با تمام شرایط و قوانین بالا موافقم",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = onAccept,
            enabled = checked,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = Color.White,
                disabledContainerColor = DarkSurface,
                disabledContentColor = TextSecondary
            )
        ) {
            Text("ادامه", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(Modifier.height(4.dp))

        TextButton(
            onClick = onDecline,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("خروج از اپلیکیشن", color = AccentRed, fontSize = 13.sp)
        }
    }
}

@Composable
private fun TermsContent() {
    val sections = listOf(
        "📜 شرایط استفاده از ViaChart",

        "【 ۱. ماهیت اپلیکیشن 】\n\nViaChart یک ابزار تحلیل تکنیکال و اطلاع‌رسانی است و هیچ‌گونه مشاوره مالی، سرمایه‌گذاری، حقوقی یا مالیاتی ارائه نمی‌دهد. تمام محتوای این اپلیکیشن صرفاً جنبه آموزشی و اطلاع‌رسانی دارد.",

        "【 ۲. سلب مسئولیت کامل 】\n\nکاربر با پذیرش این شرایط، تصدیق می‌کند که:\n\n• تمام تصمیمات معاملاتی و سرمایه‌گذاری خود را مستقلاً و بر اساس تشخیص شخصی اتخاذ می‌کند.\n\n• هیچ‌یک از تحلیل‌ها، سیگنال‌ها، پیشنهادات، اعداد، نمودارها و محتوای ViaChart به‌عنوان توصیه خرید یا فروش تلقی نمی‌شود.\n\n• مسئولیت هرگونه سود، زیان، خسارت مالی، از دست رفتن سرمایه یا عواقب ناشی از معاملات، منحصراً بر عهده کاربر است.\n\n• ViaChart، مالک، توسعه‌دهندگان، همکاران و وابستگان آن، در قبال هیچ‌گونه خسارت مستقیم، غیرمستقیم، اتفاقی، تبعی یا تنبیهی مسئولیتی ندارند.",

        "【 ۳. عدم تضمین دقت 】\n\n• تحلیل‌های ViaChart توسط الگوریتم‌های خودکار تولید می‌شوند و ممکن است ناقص، نادرست یا گمراه‌کننده باشند.\n\n• هیچ تضمینی برای صحت، دقت، کامل بودن یا به‌روز بودن داده‌ها وجود ندارد.\n\n• نتایج گذشته، تضمینی برای نتایج آینده نیست.\n\n• استفاده از اپلیکیشن به معنی پذیرش این ریسک است.",

        "【 ۴. ریسک بازار ارزهای دیجیتال 】\n\nبازار ارزهای دیجیتال بسیار پرنوسان، پرخطر و غیرقابل‌پیش‌بینی است. امکان از دست دادن کل سرمایه وجود دارد. فقط با سرمایه‌ای معامله کنید که توان از دست دادن آن را دارید.",

        "【 ۵. داده‌های شخص ثالث 】\n\n• داده‌های قیمت، تحلیل و اخبار از منابع شخص ثالث (نوبیتکس، کوین‌گکو، ارزدیجیتال و ...) دریافت می‌شوند.\n\n• ViaChart هیچ‌گونه کنترل، تأیید یا مسئولیتی در قبال صحت، قطعی، تأخیر یا خطای این منابع ندارد.\n\n• قطع دسترسی به این منابع، تغییری در خدمات اپ ایجاد نمی‌کند و مسئولیتی متوجه ViaChart نیست.",

        "【 ۶. محدودیت سنی 】\n\nاستفاده از ViaChart فقط برای افراد ۱۸ سال یا بیشتر مجاز است. مسئولیت هرگونه تخلف از این شرط، بر عهده کاربر و سرپرست قانونی اوست.",

        "【 ۷. تبلیغات 】\n\nViaChart از سرویس‌های تبلیغاتی شخص ثالث (مانند تپسل) استفاده می‌کند. محتوای تبلیغات مربوط به ViaChart نیست و هیچ مسئولیتی در قبال آن‌ها پذیرفته نمی‌شود.",

        "【 ۸. حریم خصوصی 】\n\n• تمام داده‌های کاربر (تاریخچه، تنظیمات، یادداشت‌ها) فقط روی دستگاه خودش ذخیره می‌شود.\n\n• ViaChart به این داده‌ها دسترسی ندارد و آن‌ها را به هیچ سروری ارسال نمی‌کند.\n\n• حفظ امنیت دستگاه و اطلاعات حساب، بر عهده کاربر است.",

        "【 ۹. تغییرات خدمات 】\n\nViaChart حق دارد هر زمان، بدون اطلاع قبلی:\n\n• خدمات، قابلیت‌ها یا قیمت‌گذاری را تغییر دهد\n• دسترسی کاربران را محدود یا قطع کند\n• این شرایط را به‌روزرسانی کند\n\nادامه استفاده از اپ به معنی پذیرش شرایط جدید است.",

        "【 ۱۰. تعهد کاربر 】\n\nکاربر متعهد می‌شود:\n\n• از ViaChart فقط برای اهداف قانونی استفاده کند.\n• قوانین جمهوری اسلامی ایران را رعایت کند.\n• اپلیکیشن را مهندسی معکوس، کپی یا توزیع نکند.\n• از سیستم پاداش تبلیغات سوءاستفاده نکند.",

        "【 ۱۱. پذیرش نهایی 】\n\nبا تیک زدن گزینه «موافقم»، تصدیق می‌کنید که:\n\n✓ تمام شرایط بالا را مطالعه و کاملاً پذیرفته‌اید\n✓ مسئولیت کامل تصمیمات معاملاتی خود را می‌پذیرید\n✓ از هیچ‌گونه ادعا علیه ViaChart صرف‌نظر می‌کنید\n✓ در صورت عدم موافقت، اپلیکیشن را ترک می‌کنید"
    )

    sections.forEach { section ->
        Text(
            text = section,
            color = TextPrimary,
            fontSize = 12.sp,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(16.dp))
    }
}