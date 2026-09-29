package com.chartyar.app.data

import com.chartyar.app.analysis.Candle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * CryptoCompare API — منبع چهارم
 * رایگان: ۱۰۰,۰۰۰ درخواست در ماه
 * بدون نیاز به API Key
 */
object CryptoCompareApi {

    private const val BASE_URL = "https://min-api.cryptocompare.com/data/v2"

    /**
     * تبدیل symbol به فرمت CryptoCompare
     */
    private fun mapSymbol(symbol: String): String? {
        val clean = symbol.replace("USDT", "").replace("BUSD", "").uppercase()
        return when (clean) {
            "BTC", "ETH", "BNB", "SOL", "XRP", "ADA", "DOGE", "MATIC",
            "DOT", "LINK", "AVAX", "UNI", "ATOM", "LTC", "TRX", "ETC",
            "FIL", "NEAR", "APT", "ARB", "OP", "INJ", "SUI", "TON",
            "PEPE", "SHIB" -> clean
            else -> null
        }
    }

    /**
     * تبدیل تایم‌فریم به دقیقه برای CryptoCompare
     */
    private fun mapTimeframe(timeframe: String): Int? {
        return when (timeframe) {
            "1m" -> 1
            "5m" -> 5
            "15m" -> 15
            "30m" -> 30
            "1H" -> 60
            "4H" -> 240
            "1D" -> 1440
            "1W" -> 10080
            "1M" -> 43200
            else -> 60
        }
    }

    /**
     * دریافت کندل‌ها از CryptoCompare
     */
    suspend fun fetchCandles(
        symbol: String,
        timeframe: String
    ): List<Candle>? = withContext(Dispatchers.IO) {
        try {
            val fsym = mapSymbol(symbol) ?: return@withContext null
            val minutes = mapTimeframe(timeframe) ?: return@withContext null
            val limit = 200

            val urlString = "$BASE_URL/histominute?fsym=$fsym&tsym=USD&limit=$limit&aggregate=$minutes"
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "ChartYar/1.0")

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                connection.disconnect()
                return@withContext null
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            parseResponse(response)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * فرمت: {"Response":"Success","Data":{"Data":[{"time":...,"open":...,"high":...,"low":...,"close":...,"volumefrom":...}]}}
     */
    private fun parseResponse(json: String): List<Candle>? {
        return try {
            val root = JSONObject(json)
            val dataObj = root.optJSONObject("Data") ?: return null
            val dataArray = dataObj.optJSONArray("Data") ?: return null

            val candles = mutableListOf<Candle>()
            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val timestamp = item.getLong("time") * 1000
                val open = item.getDouble("open")
                val high = item.getDouble("high")
                val low = item.getDouble("low")
                val close = item.getDouble("close")
                val volume = item.optDouble("volumefrom", 0.0)

                candles.add(
                    Candle(
                        open = open,
                        high = high,
                        low = low,
                        close = close,
                        volume = volume,
                        timestamp = timestamp
                    )
                )
            }

            if (candles.size >= 20) candles else null
        } catch (e: Exception) {
            null
        }
    }
}