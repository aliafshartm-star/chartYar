package com.chartyar.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class MarketIndices(
    val btcDominance: Double = 0.0,
    val altcoinSeasonIndex: Int = 0,
    val defiTvlUsd: Double = 0.0,
    val defiChange24h: Double = 0.0
)

object MarketIndicesApi {

    suspend fun fetchAll(): MarketIndices = withContext(Dispatchers.IO) {
        val btcDom = fetchBtcDominance()
        val altSeason = fetchAltcoinSeason()
        val tvl = fetchDefiTvl()

        MarketIndices(
            btcDominance = btcDom,
            altcoinSeasonIndex = altSeason,
            defiTvlUsd = tvl.first,
            defiChange24h = tvl.second
        )
    }

    private suspend fun fetchBtcDominance(): Double = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.coingecko.com/api/v3/global")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("User-Agent", "ViaChart/1.0")

            if (conn.responseCode != 200) {
                conn.disconnect()
                return@withContext 0.0
            }

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val root = JSONObject(response)
            val data = root.optJSONObject("data") ?: return@withContext 0.0
            val marketCapPct = data.optJSONObject("market_cap_percentage") ?: return@withContext 0.0
            marketCapPct.optDouble("btc", 0.0)
        } catch (e: Exception) {
            0.0
        }
    }

    private suspend fun fetchAltcoinSeason(): Int = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://pro-api.coinmarketcap.com/public-api/v1/altcoin-season-index/latest")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("User-Agent", "ViaChart/1.0")

            if (conn.responseCode != 200) {
                conn.disconnect()
                return@withContext 0
            }

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val root = JSONObject(response)
            val data = root.optJSONObject("data") ?: return@withContext 0
            data.optInt("altcoin_index", 0)
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun fetchDefiTvl(): Pair<Double, Double> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.llama.fi/v2/chains")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10000
            conn.readTimeout = 10000
            conn.setRequestProperty("Accept", "application/json")
            conn.setRequestProperty("User-Agent", "ViaChart/1.0")

            if (conn.responseCode != 200) {
                conn.disconnect()
                return@withContext Pair(0.0, 0.0)
            }

            val response = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val arr = JSONArray(response)
            var totalTvl = 0.0
            for (i in 0 until arr.length()) {
                val chain = arr.optJSONObject(i) ?: continue
                totalTvl += chain.optDouble("tvl", 0.0)
            }
            Pair(totalTvl, 0.0)
        } catch (e: Exception) {
            Pair(0.0, 0.0)
        }
    }

    fun formatTvl(value: Double): String {
        return when {
            value >= 1_000_000_000_000 -> String.format("$%.2fT", value / 1_000_000_000_000)
            value >= 1_000_000_000 -> String.format("$%.2fB", value / 1_000_000_000)
            value >= 1_000_000 -> String.format("$%.2fM", value / 1_000_000)
            value >= 1_000 -> String.format("$%.2fK", value / 1_000)
            else -> String.format("$%.2f", value)
        }
    }
}