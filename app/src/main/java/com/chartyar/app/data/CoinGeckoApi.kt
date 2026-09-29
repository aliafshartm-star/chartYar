package com.chartyar.app.data

import com.chartyar.app.analysis.Candle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * CoinGecko API — منبع جایگزین برای Binance
 * محدودیت رایگان: ۱۰,۰۰۰ درخواست در ماه
 */
object CoinGeckoApi {

    private const val BASE_URL = "https://api.coingecko.com/api/v3"

    /**
    * تبدیل سمبل Binance به CoinGecko
    * BTCUSDT → bitcoin
    * ETHUSDT → ethereum
    * SOLUSDT → solana
    */
    private fun mapSymbol(symbol: String): String? {
        val cleanSymbol = symbol.replace("USDT", "").replace("BUSD", "").lowercase()
        return when (cleanSymbol) {
            "btc" -> "bitcoin"
            "eth" -> "ethereum"
            "bnb" -> "binancecoin"
            "sol" -> "solana"
            "xrp" -> "ripple"
            "ada" -> "cardano"
            "doge" -> "dogecoin"
            "matic" -> "matic-network"
            "dot" -> "polkadot"
            "link" -> "chainlink"
            "avax" -> "avalanche-2"
            "uni" -> "uniswap"
            "atom" -> "cosmos"
            "ltc" -> "litecoin"
            "trx" -> "tron"
            "etc" -> "ethereum-classic"
            "fil" -> "filecoin"
            "near" -> "near"
            "apt" -> "aptos"
            "arb" -> "arbitrum"
            "op" -> "optimism"
            "inj" -> "injective-protocol"
            "sui" -> "sui"
            "sei" -> "sei-network"
            "ton" -> "the-open-network"
            "pepe" -> "pepe"
            "shib" -> "shiba-inu"
            "wif" -> "dogwifcoin"
            "bonk" -> "bonk"
            "fet" -> "fetch-ai"
            else -> null
        }
    }

    /**
    * تبدیل تایم‌فریم به روزهای CoinGecko
    * 1m, 5m, 15m, 30m → 1
    * 1H, 4H → 1
    * 1D → 30
    * 1W → 90
    * 1M → 365
    */
    private fun mapDays(timeframe: String): Int {
        return when (timeframe) {
            "1m", "5m", "15m", "30m", "1H", "4H" -> 1
            "1D" -> 30
            "1W" -> 90
            "1M" -> 365
            else -> 1
        }
    }

    /**
    * دریافت کندل‌ها از CoinGecko
    */
    suspend fun fetchCandles(
        symbol: String,
        timeframe: String
    ): List<Candle>? = withContext(Dispatchers.IO) {
        try {
            val coinId = mapSymbol(symbol) ?: return@withContext null
            val days = mapDays(timeframe)
            val urlString = "$BASE_URL/coins/$coinId/ohlc?vs_currency=usd&days=$days"
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

            parseOhlc(response)
        } catch (e: Exception) {
            null
        }
    }

    /**
    * فرمت CoinGecko OHLC:
    * [
    *   [timestamp, open, high, low, close],
    *   ...
    * ]
    */
    private fun parseOhlc(json: String): List<Candle>? {
        return try {
            val array = JSONArray(json)
            val candles = mutableListOf<Candle>()

            for (i in 0 until array.length()) {
                val item = array.getJSONArray(i)
                val timestamp = item.getLong(0)
                val open = item.getDouble(1)
                val high = item.getDouble(2)
                val low = item.getDouble(3)
                val close = item.getDouble(4)

                candles.add(
                    Candle(
                        open = open,
                        high = high,
                        low = low,
                        close = close,
                        volume = 0.0,
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