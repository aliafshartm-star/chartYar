package com.chartyar.app.data

import com.chartyar.app.analysis.Candle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object CryptoCompareApi {

    private const val BASE_URL = "https://min-api.cryptocompare.com/data/v2"

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
     * endpoint مناسب بر اساس تایم‌فریم
     */
    private fun getEndpoint(timeframe: String): String {
        return when (timeframe) {
            "1m", "5m", "15m", "30m" -> "histominute"
            "1H", "4H" -> "histohour"
            "1D", "1W", "1M" -> "histoday"
            else -> "histohour"
        }
    }

    /**
     * aggregate مناسب بر اساس تایم‌فریم
     */
    private fun getAggregate(timeframe: String): Int {
        return when (timeframe) {
            "1m" -> 1
            "5m" -> 5
            "15m" -> 15
            "30m" -> 30
            "1H" -> 1
            "4H" -> 4
            "1D" -> 1
            "1W" -> 7
            "1M" -> 30
            else -> 1
        }
    }

    suspend fun fetchCandles(
        symbol: String,
        timeframe: String
    ): List<Candle>? = withContext(Dispatchers.IO) {
        try {
            val fsym = mapSymbol(symbol) ?: return@withContext null
            val endpoint = getEndpoint(timeframe)
            val aggregate = getAggregate(timeframe)
            val limit = 200

            val urlString = "$BASE_URL/$endpoint?fsym=$fsym&tsym=USD&limit=$limit&aggregate=$aggregate"
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
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

    private fun parseResponse(json: String): List<Candle>? {
        return try {
            val root = JSONObject(json)
            val dataObj = root.optJSONObject("Data") ?: return null
            val dataArray = dataObj.optJSONArray("Data") ?: return null

            val candles = mutableListOf<Candle>()
            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val timestamp = item.getLong("time") * 1000
                val open = item.optDouble("open", 0.0)
                val high = item.optDouble("high", 0.0)
                val low = item.optDouble("low", 0.0)
                val close = item.optDouble("close", 0.0)
                val volume = item.optDouble("volumefrom", 0.0)

                if (open <= 0.0 || close <= 0.0) continue

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