package com.chartyar.app.data

import com.chartyar.app.analysis.Candle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * سرویس دریافت داده از چند منبع
 * زنجیره Fallback: Cache → Binance → CoinGecko → CoinPaprika → CryptoCompare
 */
object MarketDataService {

    enum class Source {
        BINANCE,
        COINGECKO,
        COINPAPRIKA,
        CRYPTOCOMPARE,
        CACHE,
        NONE
    }

    data class MarketDataResult(
        val candles: List<Candle>?,
        val source: Source
    )

    /**
     * دریافت کندل‌ها با Fallback زنجیره‌ای
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
                val candles = RetryHelper.withRetry(maxAttempts = 2) {
                    BinanceApi.fetchCandles(symbol, timeframe, 200)
                }
                if (candles != null && candles.size >= 20) {
                    cache.saveCandles(symbol, timeframe, candles)
                    return@withContext MarketDataResult(candles, Source.BINANCE)
                }
            }
        } catch (e: Exception) {
            // ادامه بده
        }

        // مرحله ۳: تلاش برای CoinGecko
        try {
            val candles = RetryHelper.withRetry(maxAttempts = 1) {
                CoinGeckoApi.fetchCandles(symbol, timeframe)
            }
            if (candles != null && candles.size >= 20) {
                cache.saveCandles(symbol, timeframe, candles)
                return@withContext MarketDataResult(candles, Source.COINGECKO)
            }
        } catch (e: Exception) {
            // ادامه بده
        }

        // مرحله ۴: تلاش برای CoinPaprika
        try {
            val candles = RetryHelper.withRetry(maxAttempts = 1) {
                CoinPaprikaApi.fetchCandles(symbol, timeframe)
            }
            if (candles != null && candles.size >= 5) {
                cache.saveCandles(symbol, timeframe, candles)
                return@withContext MarketDataResult(candles, Source.COINPAPRIKA)
            }
        } catch (e: Exception) {
            // ادامه بده
        }

        // مرحله ۵: تلاش برای CryptoCompare
        try {
            val candles = RetryHelper.withRetry(maxAttempts = 1) {
                CryptoCompareApi.fetchCandles(symbol, timeframe)
            }
            if (candles != null && candles.size >= 20) {
                cache.saveCandles(symbol, timeframe, candles)
                return@withContext MarketDataResult(candles, Source.CRYPTOCOMPARE)
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
            Source.COINPAPRIKA -> "CoinPaprika"
            Source.CRYPTOCOMPARE -> "CryptoCompare"
            Source.CACHE -> "Cache محلی"
            Source.NONE -> "نامشخص"
        }
    }
}