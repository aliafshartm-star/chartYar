package com.chartyar.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * بررسی نسخه جدید اپ از GitHub Releases
 */
object UpdateChecker {

    // آدرس GitHub Releases
    private const val RELEASES_URL = "https://api.github.com/repos/aliafshartm-star/chartYar/releases/latest"

    // نسخه فعلی اپ
    const val CURRENT_VERSION = "1.0.0"

    data class UpdateInfo(
        val hasUpdate: Boolean,
        val latestVersion: String,
        val downloadUrl: String,
        val releaseNotes: String,
        val publishedAt: String
    )

    /**
     * چک کردن نسخه جدید
     */
    suspend fun checkForUpdate(): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val url = URL(RELEASES_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("Accept", "application/vnd.github+json")
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

    private fun parseResponse(json: String): UpdateInfo? {
        return try {
            val obj = JSONObject(json)
            val tagName = obj.optString("tag_name", "")
            val body = obj.optString("body", "")
            val publishedAt = obj.optString("published_at", "")
            val htmlUrl = obj.optString("html_url", "")

            // استخراج شماره نسخه از tag (مثلاً v1.0.1)
            val latestVersion = tagName.removePrefix("v").trim()

            val hasUpdate = isNewerVersion(latestVersion, CURRENT_VERSION)

            UpdateInfo(
                hasUpdate = hasUpdate,
                latestVersion = latestVersion,
                downloadUrl = htmlUrl,
                releaseNotes = body,
                publishedAt = publishedAt
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * مقایسه نسخه‌ها
     * @return true اگه latest جدیدتر باشه
     */
    private fun isNewerVersion(latest: String, current: String): Boolean {
        return try {
            val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
            val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }

            val maxLen = maxOf(latestParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val l = latestParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (l > c) return true
                if (l < c) return false
            }
            false
        } catch (e: Exception) {
            false
        }
    }
}