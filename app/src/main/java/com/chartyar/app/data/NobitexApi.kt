package com.chartyar.app.data

import com.chartyar.app.analysis.Candle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object NobitexApi {

    private const val BASE_URL = "https://apiv2.nobitex.ir"
    private const val MARKET_STATS_URL = "$BASE_URL/market/stats"
    private const val OHLC_URL = "$BASE_URL/market/udf/history"
    private const val PING_URL = "$BASE_URL/v2/orderbook/BTCUSDT"

    @Volatile
    private var cachedSymbols: List<Pair<String, String>>? = null

    @Volatile
    private var cacheTime: Long = 0L

    private const val CACHE_DURATION_MS = 24 * 60 * 60 * 1000L

    val popularSymbols = listOf(
        "BTCUSDT" to "بیت‌کوین (BTC)",
        "ETHUSDT" to "اتریوم (ETH)",
        "BNBUSDT" to "بایننس کوین (BNB)",
        "SOLUSDT" to "سولانا (SOL)",
        "XRPUSDT" to "ریپل (XRP)",
        "ADAUSDT" to "کاردانو (ADA)",
        "DOGEUSDT" to "دوج‌کوین (DOGE)",
        "TRXUSDT" to "ترون (TRX)",
        "LTCUSDT" to "لایت‌کوین (LTC)",
        "LINKUSDT" to "چین‌لینک (LINK)"
    )

    suspend fun fetchAllSymbols(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val cached = cachedSymbols
        if (cached != null && (now - cacheTime) < CACHE_DURATION_MS) {
            return@withContext cached
        }

        try {
            val url = URL("$MARKET_STATS_URL?dstCurrency=rls,usdt")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("User-Agent", "ChartYar/1.0")

            if (connection.responseCode != 200) {
                connection.disconnect()
                return@withContext cached ?: popularSymbols
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val parsed = parseMarketStats(response)
            if (parsed != null && parsed.isNotEmpty()) {
                cachedSymbols = parsed
                cacheTime = now
                return@withContext parsed
            }

            cached ?: popularSymbols
        } catch (e: Exception) {
            cached ?: popularSymbols
        }
    }

    suspend fun fetchCandles(
        symbol: String,
        timeframe: String,
        limit: Int = 200
    ): List<Candle>? = withContext(Dispatchers.IO) {
        try {
            val resolution = mapTimeframe(timeframe)
            val now = System.currentTimeMillis() / 1000
            val from = now - (limit * getTimeframeSeconds(resolution))

            val urlString = "$OHLC_URL?symbol=$symbol&resolution=$resolution&from=$from&to=$now"
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("User-Agent", "ChartYar/1.0")

            if (connection.responseCode != 200) {
                connection.disconnect()
                return@withContext null
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            parseOhlcResponse(response)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * چک کردن آنلاین بودن نوبیتکس
     */
    suspend fun isOnline(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(PING_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "ChartYar/1.0")
            val code = connection.responseCode
            connection.disconnect()
            code in 200..299
        } catch (e: Exception) {
            false
        }
    }

    private fun parseMarketStats(json: String): List<Pair<String, String>>? {
        return try {
            val root = JSONObject(json)
            val stats = root.optJSONObject("stats") ?: return null
            val result = mutableListOf<Pair<String, String>>()

            val keys = stats.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val item = stats.optJSONObject(key) ?: continue

                val volume = item.optDouble("volumeDst", 0.0)
                if (volume <= 0) continue

                val symbolFormat = key.replace("-", "").uppercase()
                val baseAsset = key.substringBefore("-").uppercase()
                val displayName = "$baseAsset ($symbolFormat)"

                result.add(symbolFormat to displayName)
            }

            result.sortedBy { it.first }.ifEmpty { null }
        } catch (e: Exception) {
            null
        }
    }

    private fun parseOhlcResponse(json: String): List<Candle>? {
        return try {
            val root = JSONObject(json)
            if (root.optString("s") != "ok") return null

            val times = root.optJSONArray("t") ?: return null
            val opens = root.optJSONArray("o") ?: return null
            val highs = root.optJSONArray("h") ?: return null
            val lows = root.optJSONArray("l") ?: return null
            val closes = root.optJSONArray("c") ?: return null
            val volumes = root.optJSONArray("v") ?: return null

            val candles = mutableListOf<Candle>()
            for (i in 0 until times.length()) {
                candles.add(
                    Candle(
                        open = opens.getDouble(i),
                        high = highs.getDouble(i),
                        low = lows.getDouble(i),
                        close = closes.getDouble(i),
                        volume = volumes.getDouble(i),
                        timestamp = times.getLong(i) * 1000
                    )
                )
            }
            if (candles.size >= 20) candles else null
        } catch (e: Exception) {
            null
        }
    }

    private fun mapTimeframe(tf: String): String {
        return when (tf) {
            "1m" -> "1"
            "5m" -> "5"
            "15m" -> "15"
            "30m" -> "30"
            "1H" -> "60"
            "4H" -> "240"
            "1D" -> "D"
            "1W" -> "D"
            else -> "240"
        }
    }

    private fun getTimeframeSeconds(resolution: String): Long {
        return when (resolution) {
            "1" -> 60L
            "5" -> 300L
            "15" -> 900L
            "30" -> 1800L
            "60" -> 3600L
            "240" -> 14400L
            "D" -> 86400L
            else -> 14400L
        }
    }
}