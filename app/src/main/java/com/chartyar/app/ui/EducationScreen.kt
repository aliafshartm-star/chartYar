package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider as DividerColor
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary

data class Lesson(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val content: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationScreen(
    onBack: () -> Unit
) {
    var selectedLesson by remember { mutableStateOf<Lesson?>(null) }

    val lessons = remember {
        listOf(
            Lesson(
                emoji = "📊",
                title = "۱. تحلیل تکنیکال چیه؟",
                subtitle = "پایه‌ای‌ترین مفهوم",
                content = listOf(
                    "تحلیل تکنیکال روشی برای پیش‌بینی قیمت با مطالعه چارت‌ها و الگوهای تاریخی است.",
                    "فرض اصلی: تاریخ تکرار می‌شود. رفتار قیمت در گذشته، احتمالاً در آینده هم تکرار می‌شود.",
                    "در تحلیل تکنیکال به جای اخبار و بنیادی، فقط به قیمت و حجم معاملات نگاه می‌کنیم.",
                    "🔹 چارت کندل: نمایش قیمت در بازه زمانی مشخص",
                    "🔹 بدنه سبز = قیمت بالا رفته (صعودی)",
                    "🔹 بدنه قرمز = قیمت پایین آمده (نزولی)",
                    "🔹 سایه‌ها = بالاترین و پایین‌ترین قیمت بازه"
                )
            ),
            Lesson(
                emoji = "📈",
                title = "۲. روند (Trend) چیه؟",
                subtitle = "جهت کلی بازار",
                content = listOf(
                    "روند صعودی: سقف‌ها و کف‌ها بالاتر می‌روند — خریداران قوی‌ترند.",
                    "روند نزولی: سقف‌ها و کف‌ها پایین‌تر می‌روند — فروشندگان قوی‌ترند.",
                    "رنج (Range): قیمت بین دو سطح نوسان می‌کند، بدون جهت مشخص.",
                    "🔸 قانون طلایی: همیشه در جهت روند معامله کن!",
                    "🔸 معامله مخالف روند، ریسک بالایی دارد.",
                    "🔸 در رنج، بهتره منتظر شکست بمانی و بعد ورود کنی."
                )
            ),
            Lesson(
                emoji = "🔍",
                title = "۳. اندیکاتورها چطور کمک می‌کنند؟",
                subtitle = "ابزارهای تحلیل",
                content = listOf(
                    "اندیکاتور = فرمول ریاضی که روی قیمت یا حجم اعمال می‌شود.",
                    "🔹 RSI: قدرت خرید/فروش را می‌سنجد (۰ تا ۱۰۰)",
                    "  • زیر ۳۰ = اشباع فروش (احتمال خرید)",
                    "  • بالای ۷۰ = اشباع خرید (احتمال فروش)",
                    "🔹 MACD: مومنتوم و کراس دو خط",
                    "🔹 EMA: میانگین قیمت در بازه زمانی",
                    "🔹 Bollinger Bands: نوسان قیمت",
                    "🔸 هیچ اندیکاتوری ۱۰۰٪ دقیق نیست. باید ترکیب کنیم."
                )
            ),
            Lesson(
                emoji = "🎯",
                title = "۴. سیگنال خرید و فروش",
                subtitle = "کِی وارد بشیم؟",
                content = listOf(
                    "سیگنال قوی خرید:",
                    "  ✅ RSI در اشباع فروش + بازگشت",
                    "  ✅ MACD کراس صعودی",
                    "  ✅ ساختار صعودی (سقف و کف بالاتر)",
                    "  ✅ حجم در حال افزایش",
                    "",
                    "سیگنال قوی فروش:",
                    "  ⚠️ RSI در اشباع خرید + بازگشت",
                    "  ⚠️ MACD کراس نزولی",
                    "  ⚠️ ساختار نزولی",
                    "  ⚠️ واگرایی نزولی",
                    "",
                    "🔸 ViaChart همه اینا رو خودکار بررسی می‌کنه!"
                )
            ),
            Lesson(
                emoji = "🛑",
                title = "۵. مدیریت ریسک",
                subtitle = "مهم‌ترین بخش!",
                content = listOf(
                    "🔴 هیچ‌وقت بدون حد ضرر معامله نکن!",
                    "",
                    "🔹 حد ضرر (Stop Loss): قیمتی که اگر برسد، معامله بسته می‌شود تا ضرر بیشتر نشود.",
                    "🔹 حد سود (Take Profit): قیمتی که در آن سود می‌گیریم.",
                    "🔹 نسبت R:R (ریسک به ریوارد):",
                    "  • ۱:۲ = ریسک ۱ واحد برای سود ۲ واحد",
                    "  • حداقل ۱:۲ قبول کن",
                    "",
                    "🔸 قانون ۱-۲٪: در هر معامله فقط ۱-۲٪ سرمایه‌ات رو ریسک کن.",
                    "🔸 پول نداشته باشی، نمی‌تونی معامله کنی!"
                )
            ),
            Lesson(
                emoji = "📱",
                title = "۶. چطور از ViaChart استفاده کنم؟",
                subtitle = "راهنمای گام‌به‌گام",
                content = listOf(
                    "تحلیل ارز:",
                    "  ۱. از صفحه اصلی، دکمه «انتخاب جفت ارز» رو بزن",
                    "  ۲. جفت ارز خودت رو انتخاب کن (مثل BTCUSDT)",
                    "  ۳. تایم‌فریم معاملاتت رو انتخاب کن (15m / 1H / 4H / 1D)",
                    "  ۴. تحلیل حرفه‌ای و کامل دریافت کن",
                    "",
                    "🔹 داده‌ها لحظه‌ای از نوبیتکس دریافت می‌شوند",
                    "🔹 تحلیل‌ها در تاریخچه ذخیره می‌شوند",
                    "🔹 می‌تونی یادداشت شخصی به هر تحلیل اضافه کنی"
                )
            ),
            Lesson(
                emoji = "🚀",
                title = "۷. امکانات ViaChart",
                subtitle = "همه‌ی قابلیت‌ها",
                content = listOf(
                    "📊 تحلیل جفت ارز:",
                    "  • ۲۵ اندیکاتور، ۱۱ الگو، سیگنال دقیق",
                    "  • نقطه ورود، حد ضرر، ۳ هدف",
                    "",
                    "🎯 ۵ استراتژی تحلیل:",
                    "  • اسکالپ (معاملات سریع)",
                    "  • سوئینگ (متعادل)",
                    "  • پوزیشن (بلندمدت)",
                    "  • Multi-AI (۵ موتور رأی‌گیری)",
                    "  • PRO (۲۵ اندیکاتور پیشرفته)",
                    "",
                    "💰 قیمت لحظه‌ای ارزها:",
                    "  • قیمت به تومان + دلار",
                    "  • نمایش درصد تغییر ۲۴ ساعته",
                    "  • لیست علاقه‌مندی‌ها",
                    "",
                    "🔔 هشدار قیمت:",
                    "  • هشدار بالاتر / پایین‌تر از قیمت هدف",
                    "  • پشتیبانی از تومان و دلار",
                    "",
                    "📊 شاخص‌های بازار:",
                    "  • دامیننس بیت‌کوین",
                    "  • شاخص آلت سیزن",
                    "  • لیکوئیدیتی DeFi",
                    "",
                    "📰 اخبار و شاخص ترس و طمع",
                    "",
                    "🧮 ماشین‌حساب فیوچرز و استخراج",
                    "",
                    "🏦 صرافی‌ها با کد معرف و تخفیف کارمزد",
                    "",
                    "📚 دیکشنری اصطلاحات + درس‌های آموزشی"
                )
            ),
            Lesson(
                emoji = "⚠️",
                title = "۸. اشتباهات رایج",
                subtitle = "از اینا دوری کن",
                content = listOf(
                    "❌ معامله بدون حد ضرر",
                    "❌ ورود به معامله بدون تحلیل",
                    "❌ FOMO (ترس از جا موندن)",
                    "❌ انتقام‌گیری از بازار بعد از ضرر",
                    "❌ ریسک بیش از ۲٪ در هر معامله",
                    "❌ معامله با پول قرضی",
                    "❌ اعتماد ۱۰۰٪ به یک اندیکاتور",
                    "",
                    "✅ صبر، انضباط، مدیریت ریسک — رمز موفقیت!"
                )
            )
        )
    }

    if (selectedLesson != null) {
        LessonDetailScreen(
            lesson = selectedLesson!!,
            onBack = { selectedLesson = null }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
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
                    "آموزش تحلیل",
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
                    Text("📚", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "۸ درس کوتاه برای یادگیری تحلیل تکنیکال از صفر",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(lessons) { lesson ->
                    LessonCard(lesson = lesson, onClick = { selectedLesson = lesson })
                }
            }
        }
    }
}

@Composable
private fun LessonCard(lesson: Lesson, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(lesson.emoji, fontSize = 32.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    lesson.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    lesson.subtitle,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text("›", color = AccentBlue, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LessonDetailScreen(lesson: Lesson, onBack: () -> Unit) {
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
                lesson.title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(lesson.emoji, fontSize = 56.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    lesson.subtitle,
                    color = AccentBlue,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(16.dp))
                Divider(color = DividerColor)
                Spacer(Modifier.height(16.dp))

                lesson.content.forEach { line ->
                    Text(
                        line,
                        color = if (line.isBlank()) TextSecondary else TextPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 26.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}