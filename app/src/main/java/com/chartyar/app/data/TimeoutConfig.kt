package com.chartyar.app.data

/**
 * تنظیمات Timeout برای درخواست‌های شبکه
 */
object TimeoutConfig {

    // زمان‌های پیش‌فرض (میلی‌ثانیه)
    const val CONNECT_TIMEOUT_DEFAULT = 15_000
    const val READ_TIMEOUT_DEFAULT = 15_000

    // برای Binance (سریع‌ترین)
    const val CONNECT_TIMEOUT_BINANCE = 10_000
    const val READ_TIMEOUT_BINANCE = 10_000

    // برای CoinGecko
    const val CONNECT_TIMEOUT_COINGECKO = 15_000
    const val READ_TIMEOUT_COINGECKO = 15_000

    // برای CoinPaprika
    const val CONNECT_TIMEOUT_COINPAPRIKA = 20_000
    const val READ_TIMEOUT_COINPAPRIKA = 20_000

    // برای CryptoCompare
    const val CONNECT_TIMEOUT_CRYPTOCOMPARE = 15_000
    const val READ_TIMEOUT_CRYPTOCOMPARE = 15_000

    // برای اخبار
    const val CONNECT_TIMEOUT_NEWS = 12_000
    const val READ_TIMEOUT_NEWS = 12_000

    // برای قیمت‌ها
    const val CONNECT_TIMEOUT_PRICES = 15_000
    const val READ_TIMEOUT_PRICES = 15_000

    /**
     * دریافت timeout بر اساس نوع سرویس
     */
    fun getTimeout(service: String): Pair<Int, Int> {
        return when (service) {
            "binance" -> CONNECT_TIMEOUT_BINANCE to READ_TIMEOUT_BINANCE
            "coingecko" -> CONNECT_TIMEOUT_COINGECKO to READ_TIMEOUT_COINGECKO
            "coinpaprika" -> CONNECT_TIMEOUT_COINPAPRIKA to READ_TIMEOUT_COINPAPRIKA
            "cryptocompare" -> CONNECT_TIMEOUT_CRYPTOCOMPARE to READ_TIMEOUT_CRYPTOCOMPARE
            "news" -> CONNECT_TIMEOUT_NEWS to READ_TIMEOUT_NEWS
            "prices" -> CONNECT_TIMEOUT_PRICES to READ_TIMEOUT_PRICES
            else -> CONNECT_TIMEOUT_DEFAULT to READ_TIMEOUT_DEFAULT
        }
    }
}