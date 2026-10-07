package com.chartyar.app.data

import com.chartyar.app.analysis.Candle
import com.chartyar.app.util.RemoteLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MarketDataService {

    enum class Source {
        NOBITEX,
        CACHE,
        NONE
    }

    data class MarketDataResult(
        val candles: List<Candle>?,
        val source: Source
    )

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

        // مرحله ۲: تلاش برای نوبیتکس
        try {
            val candles = RetryHelper.withRetry(maxAttempts = 2) {
                NobitexApi.fetchCandles(symbol, timeframe, 200)
            }
            if (candles != null && candles.size >= 20) {
                cache.saveCandles(symbol, timeframe, candles)
                return@withContext MarketDataResult(candles, Source.NOBITEX)
            } else {
                RemoteLogger.logError(
                    tag = "MarketDataService",
                    message = "getCandles: no valid data for $symbol ($timeframe)"
                )
            }
        } catch (e: Exception) {
            RemoteLogger.logError(
                tag = "MarketDataService",
                message = "getCandles $symbol ($timeframe): ${e.message ?: "unknown"}",
                throwable = e
            )
        }

        return@withContext MarketDataResult(null, Source.NONE)
    }

    fun sourceLabel(source: Source): String {
        return when (source) {
            Source.NOBITEX -> "نوبیتکس"
            Source.CACHE -> "Cache محلی"
            Source.NONE -> "نامشخص"
        }
    }
}