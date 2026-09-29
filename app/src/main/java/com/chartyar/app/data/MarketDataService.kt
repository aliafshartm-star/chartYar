package com.chartyar.app.data

import com.chartyar.app.analysis.Candle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * سرویس دریافت داده از چند منبع
 * اگه Binance کار نکرد، خودکار می‌ره سراغ CoinGecko
 */
object MarketDataService {

    /**
     * منبع داده نهایی
     */
    enum class Source { BINANCE, COINGECKO, CACHE, NONE }

    data class MarketDataResult(
        val candles: List<Candle>?,
        val source: Source
    )

    /**
     * دریافت کندل‌ها با Fallback زنجیره‌ای
     * ۱. Cache
     * ۲. Binance
     * ۳. CoinGecko
     */
    suspend fun getCandles(
        symbol: String,
        timeframe: String,
        cache: CacheManager
    ): MarketDataResult = withContext(Dispatchers.IO) {

        // مرحله ۱: چک Cache محلی
        val cachedCandles = cache.loadCandles(symbol, timeframe)
        if (cachedCandles != null && cachedCandles.size >= 20) {
            return@withContext MarketDataResult(cachedCandles, Source.CACHE)
        }

        // مرحله ۲: تلاش برای Binance
        try {
            val binanceOnline = BinanceApi.isOnline()
            if (binanceOnline) {
                val binanceCandles = BinanceApi.fetchCandles(symbol, timeframe, 200)
                if (binanceCandles != null && binanceCandles.size >= 20) {
                    // ذخیره توی Cache
                    cache.saveCandles(symbol, timeframe, binanceCandles)
                    return@withContext MarketDataResult(binanceCandles, Source.BINANCE)
                }
            }
        } catch (e: Exception) {
            // ادامه بده به منبع بعدی
        }

        // مرحله ۳: تلاش برای CoinGecko
        try {
            val geckoCandles = CoinGeckoApi.fetchCandles(symbol, timeframe)
            if (geckoCandles != null && geckoCandles.size >= 20) {
                // ذخیره توی Cache
                cache.saveCandles(symbol, timeframe, geckoCandles)
                return@withContext MarketDataResult(geckoCandles, Source.COINGECKO)
            }
        } catch (e: Exception) {
            // ادامه بده
        }

        // هیچ منبعی جواب نداد
        return@withContext MarketDataResult(null, Source.NONE)
    }

    /**
     * تبدیل Source به متن فارسی
     */
    fun sourceLabel(source: Source): String {
        return when (source) {
            Source.BINANCE -> "Binance"
            Source.COINGECKO -> "CoinGecko"
            Source.CACHE -> "Cache"
            Source.NONE -> "نامشخص"
        }
    }
}