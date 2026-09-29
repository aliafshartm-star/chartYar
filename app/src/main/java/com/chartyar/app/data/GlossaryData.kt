package com.chartyar.app.data

/**
 * دیکشنری اصطلاحات تریدینگ
 */
data class GlossaryItem(
    val term: String,
    val englishTerm: String,
    val description: String,
    val category: String
)

object GlossaryData {

    val items = listOf(
        // اندیکاتورها
        GlossaryItem(
            term = "RSI",
            englishTerm = "Relative Strength Index",
            description = "شاخص قدرت نسبی — بین ۰ تا ۱۰۰ نوسان می‌کند. زیر ۳۰ = اشباع فروش (احتمال خرید)، بالای ۷۰ = اشباع خرید (احتمال فروش).",
            category = "اندیکاتور"
        ),
        GlossaryItem(
            term = "MACD",
            englishTerm = "Moving Average Convergence Divergence",
            description = "واگرایی و همگرایی میانگین متحرک — از دو خط MACD و Signal تشکیل شده. کراس صعودی = سیگنال خرید، کراس نزولی = سیگنال فروش.",
            category = "اندیکاتور"
        ),
        GlossaryItem(
            term = "EMA",
            englishTerm = "Exponential Moving Average",
            description = "میانگین متحرک نمایی — به قیمت‌های اخیر وزن بیشتری می‌دهد. معمولاً از EMA9, EMA21, EMA50, EMA200 استفاده می‌شود.",
            category = "اندیکاتور"
        ),
        GlossaryItem(
            term = "SMA",
            englishTerm = "Simple Moving Average",
            description = "میانگین متحرک ساده — میانگین ساده قیمت در بازه مشخص. برای تشخیص روند استفاده می‌شود.",
            category = "اندیکاتور"
        ),
        GlossaryItem(
            term = "ADX",
            englishTerm = "Average Directional Index",
            description = "شاخص میانگین جهت‌دار — قدرت روند را می‌سنجد. بالای ۲۵ = روند قوی، زیر ۲۰ = بازار رنج.",
            category = "اندیکاتور"
        ),
        GlossaryItem(
            term = "Bollinger Bands",
            englishTerm = "Bollinger Bands",
            description = "باندهای بولینگر — سه خط (میانی، بالا، پایین) که نوسان قیمت را نشان می‌دهند. وقتی قیمت به باند بالا برسد = اشباع خرید.",
            category = "اندیکاتور"
        ),
        GlossaryItem(
            term = "Stochastic",
            englishTerm = "Stochastic Oscillator",
            description = "اسیلاتور تصادفی — بین ۰ تا ۱۰۰ نوسان می‌کند. زیر ۲۰ = اشباع فروش، بالای ۸۰ = اشباع خرید.",
            category = "اندیکاتور"
        ),
        GlossaryItem(
            term = "ATR",
            englishTerm = "Average True Range",
            description = "میانگین دامنه واقعی — نوسان‌پذیری بازار را می‌سنجد. برای تعیین حد ضرر استفاده می‌شود.",
            category = "اندیکاتور"
        ),

        // الگوها
        GlossaryItem(
            term = "کندل",
            englishTerm = "Candlestick",
            description = "شمع — نمایش قیمت در یک بازه زمانی. شامل Open، High، Low، Close. بدنه سبز = صعودی، بدنه قرمز = نزولی.",
            category = "الگو"
        ),
        GlossaryItem(
            term = "سر و شانه",
            englishTerm = "Head and Shoulders",
            description = "الگوی برگشتی نزولی — سه قله که وسطی بالاتر است. سیگنال فروش قوی.",
            category = "الگو"
        ),
        GlossaryItem(
            term = "کف دوقلو",
            englishTerm = "Double Bottom",
            description = "الگوی برگشتی صعودی — دو کف در یک سطح. سیگنال خرید.",
            category = "الگو"
        ),
        GlossaryItem(
            term = "سقف دوقلو",
            englishTerm = "Double Top",
            description = "الگوی برگشتی نزولی — دو سقف در یک سطح. سیگنال فروش.",
            category = "الگو"
        ),
        GlossaryItem(
            term = "مثلث صعودی",
            englishTerm = "Ascending Triangle",
            description = "الگوی ادامه‌دهنده صعودی — مقاومت افقی و کف‌های صعودی. شکست به بالا = ادامه روند صعودی.",
            category = "الگو"
        ),
        GlossaryItem(
            term = "پرچم",
            englishTerm = "Flag",
            description = "الگوی ادامه‌دهنده — بعد از یک حرکت قوی، یه استراحت کوتاه. بعدش حرکت ادامه می‌یابد.",
            category = "الگو"
        ),
        GlossaryItem(
            term = "Doji",
            englishTerm = "Doji",
            description = "کندل بی‌تصمیم — بدنه خیلی کوچک یا صفر. نشانه بلاتکلیفی بازار.",
            category = "الگو"
        ),
        GlossaryItem(
            term = "Hammer",
            englishTerm = "Hammer",
            description = "چکش — کندل با سایه پایینی بلند در کف. سیگنال برگشت صعودی.",
            category = "الگو"
        ),

        // سطوح و مفاهیم
        GlossaryItem(
            term = "حمایت",
            englishTerm = "Support",
            description = "سطح حمایت — ناحیه‌ای که قیمت به آن رسیده و بالا رفته. در این ناحیه خریداران قوی‌تر هستند.",
            category = "مفاهیم"
        ),
        GlossaryItem(
            term = "مقاومت",
            englishTerm = "Resistance",
            description = "سطح مقاومت — ناحیه‌ای که قیمت به آن رسیده و پایین آمده. در این ناحیه فروشندگان قوی‌تر هستند.",
            category = "مفاهیم"
        ),
        GlossaryItem(
            term = "روند",
            englishTerm = "Trend",
            description = "جهت کلی بازار. صعودی (Uptrend) = سقف و کف بالاتر، نزولی (Downtrend) = سقف و کف پایین‌تر.",
            category = "مفاهیم"
        ),
        GlossaryItem(
            term = "رنج",
            englishTerm = "Range",
            description = "بازار بدون روند — قیمت بین دو سطح حمایت و مقاومت نوسان می‌کند.",
            category = "مفاهیم"
        ),
        GlossaryItem(
            term = "واگرایی",
            englishTerm = "Divergence",
            description = "ناهماهنگی قیمت و اندیکاتور — مثلاً قیمت سقف بالاتر می‌زند ولی RSI سقف پایین‌تر. نشانه ضعف روند.",
            category = "مفاهیم"
        ),
        GlossaryItem(
            term = "شکست",
            englishTerm = "Breakout",
            description = "خروج قیمت از یک ناحیه مهم — مثلاً شکست مقاومت. اگر با حجم بالا باشد، معتبر است.",
            category = "مفاهیم"
        ),
        GlossaryItem(
            term = "فیبوناچی",
            englishTerm = "Fibonacci",
            description = "ابزار اصلاح قیمت — سطوح 0.382, 0.5, 0.618, 0.786 برای تشخیص نقاط ورود/خروج.",
            category = "مفاهیم"
        ),

        // مدیریت ریسک
        GlossaryItem(
            term = "حد ضرر",
            englishTerm = "Stop Loss",
            description = "حد ضرر — قیمتی که اگر برسد، معامله به‌طور خودکار بسته می‌شود تا ضرر بیشتر نشود.",
            category = "مدیریت ریسک"
        ),
        GlossaryItem(
            term = "حد سود",
            englishTerm = "Take Profit",
            description = "حد سود — قیمتی که اگر برسد، معامله بسته می‌شود و سود می‌گیریم.",
            category = "مدیریت ریسک"
        ),
        GlossaryItem(
            term = "نقطه ورود",
            englishTerm = "Entry Point",
            description = "قیمتی که معامله را شروع می‌کنیم — می‌تواند خرید یا فروش باشد.",
            category = "مدیریت ریسک"
        ),
        GlossaryItem(
            term = "R:R",
            englishTerm = "Risk to Reward",
            description = "نسبت ریسک به ریوارد — مثلاً ۱:۲ یعنی اگر ۱ واحد ریسک کنیم، ۲ واحد سود انتظار داریم. حداقل ۱:۲ توصیه می‌شود.",
            category = "مدیریت ریسک"
        ),
        GlossaryItem(
            term = "اشباع خرید",
            englishTerm = "Overbought",
            description = "حالت اشباع خرید — قیمت خیلی بالا رفته و احتمال اصلاح (پایین آمدن) هست.",
            category = "مدیریت ریسک"
        ),
        GlossaryItem(
            term = "اشباع فروش",
            englishTerm = "Oversold",
            description = "حالت اشباع فروش — قیمت خیلی پایین آمده و احتمال برگشت (بالا آمدن) هست.",
            category = "مدیریت ریسک"
        ),

        // ارز دیجیتال
        GlossaryItem(
            term = "بیت‌کوین",
            englishTerm = "Bitcoin (BTC)",
            description = "اولین و بزرگ‌ترین ارز دیجیتال — در سال ۲۰۰۹ توسط ساتوشی ناکاموتو ساخته شد.",
            category = "ارز دیجیتال"
        ),
        GlossaryItem(
            term = "آلت‌کوین",
            englishTerm = "Altcoin",
            description = "همه ارزهای دیجیتال به‌جز بیت‌کوین — مثل اتریوم، سولانا، ریپل و ...",
            category = "ارز دیجیتال"
        ),
        GlossaryItem(
            term = "استیبل کوین",
            englishTerm = "Stablecoin",
            description = "ارز دیجیتال با ارزش ثابت — معمولاً به دلار وصل است. مثل USDT, USDC.",
            category = "ارز دیجیتال"
        ),
        GlossaryItem(
            term = "اسپات",
            englishTerm = "Spot",
            description = "معامله نقدی — خرید و فروش مستقیم ارز بدون اهرم.",
            category = "ارز دیجیتال"
        ),
        GlossaryItem(
            term = "فیوچرز",
            englishTerm = "Futures",
            description = "معامله آتی — با اهرم (Leverage) که سود و ضرر را چند برابر می‌کند.",
            category = "ارز دیجیتال"
        ),
        GlossaryItem(
            term = "تایم‌فریم",
            englishTerm = "Timeframe",
            description = "بازه زمانی هر کندل — مثلاً 1H یعنی هر کندل یک ساعت، 4H یعنی چهار ساعت.",
            category = "ارز دیجیتال"
        )
    )

    val categories = listOf("همه", "اندیکاتور", "الگو", "مفاهیم", "مدیریت ریسک", "ارز دیجیتال")

    fun search(query: String, category: String): List<GlossaryItem> {
        var filtered = items

        if (category != "همه") {
            filtered = filtered.filter { it.category == category }
        }

        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.term.contains(query, ignoreCase = true) ||
                        it.englishTerm.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }

        return filtered
    }
}