package com.chartyar.app.data

import android.util.Xml
import com.chartyar.app.util.RemoteLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Arzdigital RSS News API
 * منبع خبری ایرانی — از ایران قابل دسترس
 */
object ArzdigitalNewsApi {

    // آدرس RSS ارزدیجیتال
    private const val RSS_URL = "https://arzdigital.com/feed/"

    suspend fun fetchNews(limit: Int = 20): List<NewsItem>? = withContext(Dispatchers.IO) {
        try {
            val url = URL(RSS_URL)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Android; ViaChart/1.0)")
            connection.setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml")

            if (connection.responseCode != 200) {
                val code = connection.responseCode
                connection.disconnect()
                RemoteLogger.logError(
                    tag = "ArzdigitalNewsApi",
                    message = "fetchNews HTTP $code"
                )
                return@withContext null
            }

            val inputStream = connection.inputStream
            val newsList = parseRss(inputStream, limit)
            connection.disconnect()

            if (newsList.isNotEmpty()) newsList else null

        } catch (e: Exception) {
            RemoteLogger.logError(
                tag = "ArzdigitalNewsApi",
                message = "fetchNews: ${e.message ?: "unknown"}",
                throwable = e
            )
            null
        }
    }

    private fun parseRss(inputStream: java.io.InputStream, limit: Int): List<NewsItem> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)

        val newsList = mutableListOf<NewsItem>()

        var title = ""
        var link = ""
        var description = ""
        var pubDate = ""
        var category = ""
        var currentTag = ""

        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT && newsList.size < limit) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    currentTag = parser.name
                    if (currentTag == "item") {
                        title = ""
                        link = ""
                        description = ""
                        pubDate = ""
                        category = ""
                    }
                }

                XmlPullParser.TEXT -> {
                    val text = parser.text ?: ""
                    when (currentTag) {
                        "title" -> title = text
                        "link" -> link = text
                        "description" -> description = text
                        "pubDate" -> pubDate = text
                        "category" -> category = text
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (parser.name == "item") {
                        val timestamp = parseDate(pubDate)

                        val cleanTitle = title.replace(Regex("<[^>]*>"), "").trim()
                        val cleanDesc = description
                            .replace(Regex("<[^>]*>"), "")
                            .replace("&nbsp;", " ")
                            .trim()

                        if (cleanTitle.isNotBlank()) {
                            newsList.add(
                                NewsItem(
                                    id = link.hashCode().toString(),
                                    title = cleanTitle,
                                    body = cleanDesc.take(500),
                                    source = "ارزدیجیتال",
                                    imageUrl = null,
                                    url = link,
                                    publishedAt = timestamp,
                                    categories = category
                                )
                            )
                        }
                    }
                    currentTag = ""
                }
            }
            eventType = parser.next()
        }

        return newsList
    }

    private fun parseDate(dateStr: String): Long {
        return try {
            val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
            format.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

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