package com.chartyar.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * مدل قیمت ارز
 */
data class CoinPrice(
    val symbol: String,
    val name: String,
    val price: Double,
    val changePercent24h: Double,
    val volume: Double
) {
    val isUp: Boolean get() = changePercent24h >= 0
}

/**
 * Prices API — از نوبیتکس
 * عمومی، بدون API Key، از ایران قابل دسترس
 */
object PricesApi {

    private const val BASE_URL = "https://apiv2.nobitex.ir/market/stats"

    suspend fun fetchPrices(): List<CoinPrice>? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL?dstCurrency=rls")
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

            parsePrices(response)
        } catch (e: Exception) {
            null
        }
    }

    private fun parsePrices(json: String): List<CoinPrice>? {
        return try {
            val root = JSONObject(json)
            val stats = root.optJSONObject("stats") ?: return null

            val prices = mutableListOf<CoinPrice>()

            val keys = stats.keys()
            while (keys.hasNext()) {
                val key = keys.next()

                if (!key.endsWith("-rls")) continue

                val item = stats.optJSONObject(key) ?: continue

                val baseAsset = key.substringBefore("-rls").uppercase()
                val symbolFormat = "${baseAsset}IRT"
                val price = item.optDouble("latest", 0.0)
                val change = item.optDouble("dayChange", 0.0)
                val volume = item.optDouble("volumeDst", 0.0)

                if (price > 0) {
                    prices.add(
                        CoinPrice(
                            symbol = symbolFormat,
                            name = "$baseAsset (تومان)",
                            price = price / 10.0, // تبدیل ریال به تومان
                            changePercent24h = change,
                            volume = volume
                        )
                    )
                }
            }

            val sorted = prices.sortedByDescending { it.volume }

            if (sorted.isNotEmpty()) sorted else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * فرمت کردن قیمت
     */
    fun formatPrice(price: Double): String {
        return when {
            price >= 1_000_000 -> "%,.0f".format(price)
            price >= 1000 -> "%,.0f".format(price)
            price >= 1 -> "%,.2f".format(price)
            price >= 0.01 -> "%.4f".format(price)
            else -> "%.8f".format(price)
        }
    }

    /**
     * فرمت کردن درصد
     */
    fun formatPercent(percent: Double): String {
        val sign = if (percent >= 0) "+" else ""
        return "$sign%.2f%%".format(percent)
    }
}