package com.chartyar.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat

data class CoinPrice(
    val symbol: String,
    val name: String,
    val price: Double,
    val change24h: Double,
    val changePercent24h: Double,
    val high24h: Double,
    val low24h: Double,
    val volume24h: Double
) {
    val isUp: Boolean get() = changePercent24h >= 0
}

object PricesApi {

    private const val BINANCE_URL = "https://api.binance.com/api/v3/ticker/24hr"

    private val popularSymbols = listOf(
        "BTCUSDT" to "بیت‌کوین",
        "ETHUSDT" to "اتریوم",
        "BNBUSDT" to "بایننس کوین",
        "SOLUSDT" to "سولانا",
        "XRPUSDT" to "ریپل",
        "ADAUSDT" to "کاردانو",
        "DOGEUSDT" to "دوج‌کوین",
        "MATICUSDT" to "پالیگان",
        "DOTUSDT" to "پولکادات",
        "LINKUSDT" to "چین‌لینک",
        "AVAXUSDT" to "آوالانچ",
        "UNIUSDT" to "یونی‌سواپ",
        "ATOMUSDT" to "کازموس",
        "LTCUSDT" to "لایت‌کوین",
        "TRXUSDT" to "ترون",
        "ETCUSDT" to "اتریوم کلاسیک",
        "FILUSDT" to "فایل‌کوین",
        "NEARUSDT" to "نیر",
        "APTUSDT" to "اپتوس",
        "ARBUSDT" to "آربیتروم",
        "OPUSDT" to "اپتیمیزم",
        "INJUSDT" to "اینجکتیو",
        "SUIUSDT" to "سویی",
        "TONUSDT" to "تون‌کوین",
        "PEPEUSDT" to "پپه",
        "SHIBUSDT" to "شیبا",
        "WIFUSDT" to "دوج‌ویف",
        "BONKUSDT" to "بونک",
        "FETUSDT" to "فچ",
        "SEIUSDT" to "سی"
    )

    suspend fun fetchPrices(): List<CoinPrice>? = withContext(Dispatchers.IO) {
        try {
            val symbolsParam = popularSymbols.joinToString(",") { "\"${it.first}\"" }
            val urlString = "$BINANCE_URL?symbols=[$symbolsParam]"
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

            parsePrices(response)
        } catch (e: Exception) {
            null
        }
    }

    private fun parsePrices(json: String): List<CoinPrice>? {
        return try {
            val array = JSONArray(json)
            val result = mutableListOf<CoinPrice>()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val symbol = item.optString("symbol", "")
                val price = item.optString("lastPrice", "0").toDoubleOrNull() ?: continue
                val change = item.optString("priceChange", "0").toDoubleOrNull() ?: 0.0
                val changePercent = item.optString("priceChangePercent", "0").toDoubleOrNull() ?: 0.0
                val high = item.optString("highPrice", "0").toDoubleOrNull() ?: 0.0
                val low = item.optString("lowPrice", "0").toDoubleOrNull() ?: 0.0
                val volume = item.optString("quoteVolume", "0").toDoubleOrNull() ?: 0.0

                val name = popularSymbols.find { it.first == symbol }?.second ?: symbol

                result.add(
                    CoinPrice(
                        symbol = symbol,
                        name = name,
                        price = price,
                        change24h = change,
                        changePercent24h = changePercent,
                        high24h = high,
                        low24h = low,
                        volume24h = volume
                    )
                )
            }

            val sorted = popularSymbols.mapNotNull { (sym, _) ->
                result.find { it.symbol == sym }
            }

            if (sorted.isNotEmpty()) sorted else null
        } catch (e: Exception) {
            null
        }
    }

    fun formatPrice(price: Double): String {
        val pattern = when {
            price >= 1000.0 -> "#,##0"
            price >= 100.0 -> "#,##0.00"
            price >= 1.0 -> "0.000"
            price >= 0.01 -> "0.00000"
            else -> "0.00000000"
        }
        val df = DecimalFormat(pattern)
        return df.format(price)
    }

    fun formatPercent(percent: Double): String {
        val sign = if (percent >= 0) "+" else ""
        val df = DecimalFormat("0.00")
        return sign + df.format(percent) + "%"
    }

    fun formatVolume(volume: Double): String {
        val df = DecimalFormat("0.00")
        return when {
            volume >= 1000000000.0 -> df.format(volume / 1000000000.0) + "B"
            volume >= 1000000.0 -> df.format(volume / 1000000.0) + "M"
            volume >= 1000.0 -> df.format(volume / 1000.0) + "K"
            else -> DecimalFormat("0").format(volume)
        }
    }
}