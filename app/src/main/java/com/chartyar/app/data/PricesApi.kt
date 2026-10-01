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
    val changePercent: Double,
    val volume: Double
)

/**
 * Prices API — از صرافی نوبیتکس
 * عمومی، بدون API Key، از ایران قابل دسترسی
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

                val symbol = key.substringBefore("-rls").uppercase()
                val price = item.optDouble("latest", 0.0)
                val change = item.optDouble("dayChange", 0.0)
                val volume = item.optDouble("volumeDst", 0.0)

                if (price > 0) {
                    prices.add(
                        CoinPrice(
                            symbol = symbol,
                            name = symbol,
                            price = price,
                            changePercent = change,
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
}