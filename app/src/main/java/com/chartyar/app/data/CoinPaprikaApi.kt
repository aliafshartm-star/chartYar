package com.chartyar.app.data

import com.chartyar.app.analysis.Candle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object CoinPaprikaApi {

    private const val BASE_URL = "https://api.coinpaprika.com/v1"

    private fun mapSymbol(symbol: String): String? {
        val clean = symbol.replace("USDT", "").replace("BUSD", "").uppercase()
        return when (clean) {
            "BTC" -> "btc-bitcoin"
            "ETH" -> "eth-ethereum"
            "BNB" -> "bnb-binance-coin"
            "SOL" -> "sol-solana"
            "XRP" -> "xrp-xrp"
            "ADA" -> "ada-cardano"
            "DOGE" -> "doge-dogecoin"
            "MATIC" -> "matic-polygon"
            "DOT" -> "dot-polkadot"
            "LINK" -> "link-chainlink"
            "AVAX" -> "avax-avalanche"
            "UNI" -> "uni-uniswap"
            "ATOM" -> "atom-cosmos"
            "LTC" -> "ltc-litecoin"
            "TRX" -> "trx-tron"
            "ETC" -> "etc-ethereum-classic"
            "FIL" -> "fil-filecoin"
            "NEAR" -> "near-near-protocol"
            "APT" -> "apt-aptos"
            "ARB" -> "arb-arbitrum"
            "OP" -> "op-optimism"
            "INJ" -> "inj-injective-protocol"
            "SUI" -> "sui-sui"
            "TON" -> "ton-toncoin"
            "PEPE" -> "pepe-pepe"
            "SHIB" -> "shib-shiba-inu"
            else -> null
        }
    }

    /**
     * تعداد روز بر اساس تایم‌فریم
     */
    private fun mapDays(timeframe: String): Int {
        return when (timeframe) {
            "1m", "5m", "15m", "30m" -> 7
            "1H", "4H" -> 30
            "1D" -> 90
            "1W" -> 180
            "1M" -> 365
            else -> 30
        }
    }

    /**
     * تاریخ شروع به فرمت YYYY-MM-DD
     */
    private fun getStartDate(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    suspend fun fetchCandles(
        symbol: String,
        timeframe: String
    ): List<Candle>? = withContext(Dispatchers.IO) {
        try {
            val coinId = mapSymbol(symbol) ?: return@withContext null
            val days = mapDays(timeframe)
            val startDate = getStartDate(days)
            val urlString = "$BASE_URL/coins/$coinId/ohlcv/historical?start=$startDate"
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

            parseOhlcv(response)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * فرمت: [{"time_open":"...","open":67000,"high":67500,"low":66800,"close":67300,"volume":1000}]
     */
    private fun parseOhlcv(json: String): List<Candle>? {
        return try {
            val array = JSONArray(json)
            val candles = mutableListOf<Candle>()
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val open = item.optDouble("open", 0.0)
                val high = item.optDouble("high", 0.0)
                val low = item.optDouble("low", 0.0)
                val close = item.optDouble("close", 0.0)
                val volume = item.optDouble("volume", 0.0)
                val timeOpen = item.optString("time_open", "")

                if (open <= 0.0 || close <= 0.0) continue

                val timestamp = try {
                    sdf.parse(timeOpen)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }

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

            if (candles.size >= 10) candles else null
        } catch (e: Exception) {
            null
        }
    }
}