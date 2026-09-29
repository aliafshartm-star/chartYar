package com.chartyar.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * مدل خبر
 */
data class NewsItem(
    val id: String,
    val title: String,
    val body: String,
    val source: String,
    val imageUrl: String?,
    val url: String,
    val publishedAt: Long,
    val categories: String
)

/**
 * News API — از CryptoCompare
 * رایگان، بدون API Key
 */
object NewsApi {

    private const val BASE_URL = "https://min-api.cryptocompare.com/data/v2/news/"

    /**
     * دریافت آخرین اخبار کریپتو
     */
    suspend fun fetchNews(limit: Int = 20): List<NewsItem>? = withContext(Dispatchers.IO) {
        try {
            val urlString = "$BASE_URL?lang=EN&sortOrder=latest"
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

            parseNews(response, limit)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * پارس JSON اخبار
     */
    private fun parseNews(json: String, limit: Int): List<NewsItem>? {
        return try {
            val root = JSONObject(json)
            val dataArray = root.optJSONArray("Data") ?: return null

            val news = mutableListOf<NewsItem>()
            val maxItems = minOf(limit, dataArray.length())

            for (i in 0 until maxItems) {
                val item = dataArray.getJSONObject(i)
                val id = item.optString("id", i.toString())
                val title = item.optString("title", "")
                val body = item.optString("body", "")
                val source = item.optString("source_info", "").let {
                    try {
                        JSONObject(it).optString("name", "CryptoCompare")
                    } catch (e: Exception) {
                        "CryptoCompare"
                    }
                }
                val imageUrl = item.optString("imageurl", "").takeIf { it.isNotBlank() }
                val url = item.optString("url", "")
                val publishedAt = item.optLong("published_on", 0L) * 1000
                val categories = item.optString("categories", "")

                if (title.isNotBlank()) {
                    news.add(
                        NewsItem(
                            id = id,
                            title = title,
                            body = body,
                            source = source,
                            imageUrl = imageUrl,
                            url = url,
                            publishedAt = publishedAt,
                            categories = categories
                        )
                    )
                }
            }

            if (news.isNotEmpty()) news else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * فیلتر اخبار بر اساس دسته
     */
    fun filterByCategory(news: List<NewsItem>, category: String): List<NewsItem> {
        if (category == "همه") return news
        return news.filter { it.categories.contains(category, ignoreCase = true) }
    }

    /**
     * زمان نسبی
     */
    fun formatRelativeTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60000
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "همین الان"
            minutes < 60 -> "$minutes دقیقه پیش"
            hours < 24 -> "$hours ساعت پیش"
            days < 7 -> "$days روز پیش"
            else -> "$days روز پیش"
        }
    }
}