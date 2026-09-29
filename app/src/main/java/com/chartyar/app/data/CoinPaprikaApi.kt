package com.chartyar.app.data

import com.chartyar.app.analysis.Candle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * CoinPaprika API — منبع سوم
 * رایگان: ۲۰,۰۰۰ درخواست در ماه
 * بدون نیاز به API Key
 */
object CoinPaprikaApi {

    private const val BASE_URL = "https://api.coinpaprika.com/v1"

    /**
     * تبدیل symbol به ID CoinPaprika
     */
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
     * دریافت کندل‌ها از CoinPaprika
     */
    suspend fun fetchCandles(
        symbol: String,
        timeframe: String
    ): List<Candle>? = withContext(Dispatchers.IO) {
        try {
            val coinId = mapSymbol(symbol) ?: return@withContext null
            // CoinPaprika فقط historical data داره، با روز محدود
            val urlString = "$BASE_URL/coins/$coinId/ohlcv/latest"
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
            val now = System.currentTimeMillis()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val open = item.getDouble("open")
                val high = item.getDouble("high")
                val low = item.getDouble("low")
                val close = item.getDouble("close")
                val volume = item.optDouble("volume", 0.0)

                candles.add(
                    Candle(
                        open = open,
                        high = high,
                        low = low,
                        close = close,
                        volume = volume,
                        timestamp = now - (array.length() - i) * 3600000L
                    )
                )
            }

            if (candles.size >= 5) candles else null
        } catch (e: Exception) {
            null
        }
    }
}