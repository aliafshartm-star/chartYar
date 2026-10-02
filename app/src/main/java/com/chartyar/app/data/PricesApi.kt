package com.chartyar.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class CoinPrice(
    val symbol: String,
    val name: String,
    val price: Double,
    val priceUsd: Double,
    val changePercent24h: Double,
    val volume: Double
) {
    val isUp: Boolean get() = changePercent24h >= 0
}

object PricesApi {

    private const val BASE_URL = "https://apiv2.nobitex.ir/market/stats"

    @Volatile
    private var cachedUsdtToman: Double = 0.0

    @Volatile
    private var cachedUsdtTime: Long = 0L

    private const val USDT_CACHE_MS = 60_000L

    suspend fun fetchUsdtTomanRate(): Double = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (cachedUsdtToman > 0 && (now - cachedUsdtTime) < USDT_CACHE_MS) {
            return@withContext cachedUsdtToman
        }
        try {
            val url = URL("$BASE_URL?srcCurrency=usdt&dstCurrency=rls")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "ChartYar/1.0")

            if (connection.responseCode != 200) {
                connection.disconnect()
                return@withContext cachedUsdtToman
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val root = JSONObject(response)
            val stats = root.optJSONObject("stats") ?: return@withContext cachedUsdtToman
            val key = stats.keys().asSequence().firstOrNull() ?: return@withContext cachedUsdtToman
            val item = stats.optJSONObject(key) ?: return@withContext cachedUsdtToman
            val latest = item.optString("latest", "0").toDoubleOrNull() ?: 0.0
            if (latest <= 0) return@withContext cachedUsdtToman

            val tomanRate = latest / 10.0
            cachedUsdtToman = tomanRate
            cachedUsdtTime = now
            tomanRate
        } catch (e: Exception) {
            cachedUsdtToman
        }
    }

    suspend fun fetchPrices(): List<CoinPrice>? = withContext(Dispatchers.IO) {
        try {
            val usdtToman = fetchUsdtTomanRate()

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

            parsePrices(response, usdtToman)
        } catch (e: Exception) {
            null
        }
    }

    private fun parsePrices(json: String, usdtToman: Double): List<CoinPrice>? {
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
                val priceRls = item.optDouble("latest", 0.0)
                val change = item.optDouble("dayChange", 0.0)
                val volume = item.optDouble("volumeDst", 0.0)

                if (priceRls > 0) {
                    val priceToman = priceRls / 10.0
                    val priceUsd = if (usdtToman > 0) priceToman / usdtToman else 0.0

                    prices.add(
                        CoinPrice(
                            symbol = symbolFormat,
                            name = "$baseAsset (تومان)",
                            price = priceToman,
                            priceUsd = priceUsd,
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

    fun formatPrice(price: Double): String {
        return when {
            price >= 1_000_000 -> "%,.0f".format(price)
            price >= 1000 -> "%,.0f".format(price)
            price >= 1 -> "%,.2f".format(price)
            price >= 0.01 -> "%.4f".format(price)
            else -> "%.8f".format(price)
        }
    }

    fun formatPriceUsd(price: Double): String {
        return when {
            price >= 1_000_000 -> "%,.0f".format(price)
            price >= 1000 -> "%,.2f".format(price)
            price >= 1 -> "%,.2f".format(price)
            price >= 0.01 -> "%.4f".format(price)
            price >= 0.0001 -> "%.6f".format(price)
            else -> "%.8f".format(price)
        }
    }

    fun formatPercent(percent: Double): String {
        val sign = if (percent >= 0) "+" else ""
        return "$sign%.2f%%".format(percent)
    }
}