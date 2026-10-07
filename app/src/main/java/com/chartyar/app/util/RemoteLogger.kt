package com.chartyar.app.util

import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Remote Logger — خطاها رو می‌فرسته به Cloudflare Worker
 * بعداً توی داشبورد می‌تونی ببینی: کدوم فایل، کدوم خط، چند کاربر
 */
object RemoteLogger {

    private const val BASE_URL = "https://chartyar-prices.chartyar-app.workers.dev"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * ثبت خطا
     * @param tag اسم بخش (مثلاً "PricesApi")
     * @param message توضیح خطا
     * @param throwable خطای واقعی (اختیاری)
     */
    fun logError(
        tag: String,
        message: String,
        throwable: Throwable? = null
    ) {
        scope.launch {
            try {
                // استخراج اسم فایل و خط از stack trace
                val element = throwable?.stackTrace?.firstOrNull()

                val payload = JSONObject().apply {
                    put("tag", tag)
                    put("message", message)
                    put("file", element?.fileName ?: "")
                    put("line", element?.lineNumber ?: 0)
                    put("function", element?.methodName ?: "")
                    put("device", "${Build.MANUFACTURER} ${Build.MODEL}")
                    put("android", Build.VERSION.SDK_INT)
                    put("timestamp", System.currentTimeMillis())
                }

                val url = URL("$BASE_URL/log/error")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("User-Agent", "ViaChart/1.0")

                connection.outputStream.use { it.write(payload.toString().toByteArray()) }
                connection.responseCode
                connection.disconnect()
            } catch (e: Exception) {
                // اگه خود Logger خطا داد، ساکت بمون (نخوایم loop بشه)
            }
        }
    }
}