package com.chartyar.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * تأیید عضویت کاربر در کانال تلگرام
 * از طریق Cloudflare Worker + Telegram Bot
 */
object TelegramVerifier {

    private const val BASE_URL = "https://chartyar-prices.chartyar-app.workers.dev"

    data class SessionResult(
        val success: Boolean,
        val session: String? = null,
        val botUrl: String? = null,
        val error: String? = null
    )

    data class CheckResult(
        val status: String,
        val verified: Boolean,
        val firstName: String? = null,
        val error: String? = null
    )

    /**
     * درخواست Session جدید از سرور
     * @return SessionResult با کد ۸ رقمی و لینک ربات
     */
    suspend fun requestSession(): SessionResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/telegram/new-session")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "ViaChart/1.0")

            if (connection.responseCode != 200) {
                connection.disconnect()
                return@withContext SessionResult(
                    success = false,
                    error = "خطا در اتصال به سرور"
                )
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val root = JSONObject(response)
            val success = root.optBoolean("success", false)

            if (!success) {
                return@withContext SessionResult(
                    success = false,
                    error = "سرور پاسخ درست نداد"
                )
            }

            val session = root.optString("session", "")
            val botUrl = root.optString("bot_url", "")

            if (session.isEmpty() || botUrl.isEmpty()) {
                return@withContext SessionResult(
                    success = false,
                    error = "پاسخ ناقص"
                )
            }

            SessionResult(
                success = true,
                session = session,
                botUrl = botUrl
            )
        } catch (e: Exception) {
            SessionResult(
                success = false,
                error = e.message ?: "خطای نامشخص"
            )
        }
    }

    /**
     * چک کردن وضعیت تأیید
     * @param session کد ۸ رقمی
     */
    suspend fun checkVerification(session: String): CheckResult = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/telegram/check?session=$session")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "ViaChart/1.0")

            if (connection.responseCode != 200) {
                connection.disconnect()
                return@withContext CheckResult(
                    status = "error",
                    verified = false,
                    error = "خطا در اتصال"
                )
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val root = JSONObject(response)
            val status = root.optString("status", "pending")

            if (status == "verified") {
                CheckResult(
                    status = "verified",
                    verified = true,
                    firstName = root.optString("first_name", null)
                )
            } else {
                CheckResult(
                    status = status,
                    verified = false
                )
            }
        } catch (e: Exception) {
            CheckResult(
                status = "error",
                verified = false,
                error = e.message
            )
        }
    }
}