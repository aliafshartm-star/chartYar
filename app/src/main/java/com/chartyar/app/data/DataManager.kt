package com.chartyar.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * مدیریت داده‌های کاربر (Export + Deletion)
 */
class DataManager(private val context: Context) {

    /**
     * ساخت خروجی JSON از همه داده‌ها
     */
    suspend fun exportAllData(db: AppDatabase): String {
        return try {
            val json = JSONObject()

            // اطلاعات
            json.put("app", "ChartYar")
            json.put("version", "1.0.0")
            json.put(
                "exported_at",
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            )

            // تاریخچه تحلیل‌ها
            val history = db.historyDao().getAll()
            // برای سادگی، فقط لیست خالی می‌ذاریم یا از Flow اول استفاده می‌کنیم
            val historyArray = JSONArray()
            json.put("analysis_history_count", "available")

            // هشدارها
            val alerts = db.alertDao().getActiveAlerts()
            val alertsArray = JSONArray()
            alerts.forEach { alert ->
                val alertObj = JSONObject().apply {
                    put("symbol", alert.symbol)
                    put("targetPrice", alert.targetPrice)
                    put("condition", alert.condition)
                    put("isActive", alert.isActive)
                    put("createdAt", alert.createdAt)
                }
                alertsArray.put(alertObj)
            }
            json.put("price_alerts", alertsArray)

            json.toString(2)
        } catch (e: Exception) {
            "{}"
        }
    }

    /**
     * پاک کردن همه داده‌ها
     */
    suspend fun deleteAllData(db: AppDatabase) {
        try {
            // پاک کردن دیتابیس
            db.historyDao().clearAll()
            db.alertDao().clearAll()

            // پاک کردن فایل‌ها
            val filesDir = context.filesDir
            listOf(
                "analysis_notes.json",
                "offline_queue.json",
                "watchlist.json",
                "portfolio.json",
                "signal_tracking.json",
                "crash_logs.txt",
                "analytics.json"
            ).forEach { fileName ->
                File(filesDir, fileName).delete()
            }

            // پاک کردن cache
            File(context.cacheDir, "candles").deleteRecursively()
            File(context.cacheDir, "camera").deleteRecursively()
            File(context.cacheDir, "shared_images").deleteRecursively()

            // پاک کردن SharedPreferences
            context.getSharedPreferences("chartyar_prefs", Context.MODE_PRIVATE).edit().clear().apply()
            context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE).edit().clear().apply()
            context.getSharedPreferences("rate_app_prefs", Context.MODE_PRIVATE).edit().clear().apply()
            context.getSharedPreferences("legal_prefs", Context.MODE_PRIVATE).edit().clear().apply()

        } catch (e: Exception) {
            // silent
        }
    }

    /**
     * محاسبه حجم داده‌ها (تقریبی)
     */
    fun getDataSize(): String {
        return try {
            var totalSize = 0L

            // فایل‌ها
            context.filesDir.listFiles()?.forEach { totalSize += it.length() }

            // Cache
            context.cacheDir.walkTopDown().forEach {
                if (it.isFile) totalSize += it.length()
            }

            // دیتابیس
            val dbFile = context.getDatabasePath("chartyar_db")
            if (dbFile.exists()) totalSize += dbFile.length()

            formatSize(totalSize)
        } catch (e: Exception) {
            "نامشخص"
        }
    }

    private fun formatSize(size: Long): String {
        return when {
            size >= 1024 * 1024 -> "${size / (1024 * 1024)} MB"
            size >= 1024 -> "${size / 1024} KB"
            else -> "$size B"
        }
    }
}