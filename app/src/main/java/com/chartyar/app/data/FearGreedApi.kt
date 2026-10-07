package com.chartyar.app.data

import com.chartyar.app.util.RemoteLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * مدل شاخص ترس و طمع
 */
data class FearGreedData(
    val value: Int,
    val classification: String,
    val classificationFa: String,
    val updateTime: String
)

/**
 * Fear & Greed API — از CoinMarketCap Public API
 */
object FearGreedApi {

    private const val API_URL =
        "https://pro-api.coinmarketcap.com/public-api/v3/fear-and-greed/latest"

    suspend fun fetchFearGreed(): FearGreedData? = withContext(Dispatchers.IO) {
        try {
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "ViaChart/1.0")

            if (connection.responseCode != 200) {
                val code = connection.responseCode
                connection.disconnect()
                RemoteLogger.logError(
                    tag = "FearGreedApi",
                    message = "fetchFearGreed HTTP $code"
                )
                return@withContext null
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            parseResponse(response)
        } catch (e: Exception) {
            RemoteLogger.logError(
                tag = "FearGreedApi",
                message = "fetchFearGreed: ${e.message ?: "unknown"}",
                throwable = e
            )
            null
        }
    }

    private fun parseResponse(json: String): FearGreedData? {
        return try {
            val root = JSONObject(json)
            val data = root.optJSONObject("data") ?: return null

            val value = data.optInt("value", -1)
            if (value < 0) return null

            val classification = data.optString("value_classification", "Neutral")
            val updateTime = data.optString("update_time", "")

            FearGreedData(
                value = value,
                classification = classification,
                classificationFa = toPersian(classification),
                updateTime = updateTime
            )
        } catch (e: Exception) {
            RemoteLogger.logError(
                tag = "FearGreedApi",
                message = "parseResponse: ${e.message ?: "unknown"}",
                throwable = e
            )
            null
        }
    }

    private fun toPersian(classification: String): String {
        return when (classification.lowercase().trim()) {
            "extreme fear" -> "ترس شدید"
            "fear" -> "ترس"
            "neutral" -> "خنثی"
            "greed" -> "طمع"
            "extreme greed" -> "طمع شدید"
            else -> "نامشخص"
        }
    }

    fun getColor(value: Int): Long {
        return when {
            value <= 20 -> 0xFFF6465DL
            value <= 40 -> 0xFFF6465DL
            value <= 60 -> 0xFFF0B90BL
            value <= 80 -> 0xFF2EBD85L
            else -> 0xFF2EBD85L
        }
    }
}