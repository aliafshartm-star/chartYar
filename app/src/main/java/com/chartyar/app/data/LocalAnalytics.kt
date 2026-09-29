package com.chartyar.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Analytics محلی — بدون Firebase
 * رویدادها رو روی گوشی ذخیره می‌کنه
 * بعداً می‌تونه به سرور فرستاده بشه
 */
object LocalAnalytics {

    private const val MAX_EVENTS = 500
    private const val FILE_NAME = "analytics.json"

    /**
     * ثبت یه رویداد
     */
    fun logEvent(context: Context, eventName: String, params: Map<String, String> = emptyMap()) {
        try {
            val event = JSONObject().apply {
                put("event", eventName)
                put("timestamp", System.currentTimeMillis())
                put("date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))

                val paramsObj = JSONObject()
                params.forEach { (key, value) -> paramsObj.put(key, value) }
                put("params", paramsObj)
            }

            val file = File(context.filesDir, FILE_NAME)
            val events = if (file.exists()) {
                try {
                    JSONArray(file.readText())
                } catch (e: Exception) {
                    JSONArray()
                }
            } else JSONArray()

            events.put(event)

            // اگه از حد گذشت، قدیمی‌ها رو حذف کن
            val trimmed = if (events.length() > MAX_EVENTS) {
                val newArray = JSONArray()
                for (i in (events.length() - MAX_EVENTS) until events.length()) {
                    newArray.put(events.getJSONObject(i))
                }
                newArray
            } else events

            file.writeText(trimmed.toString())
        } catch (e: Exception) {
            // silent fail
        }
    }

    /**
     * خوندن همه رویدادها
     */
    fun getAllEvents(context: Context): JSONArray {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) JSONArray(file.readText()) else JSONArray()
        } catch (e: Exception) {
            JSONArray()
        }
    }

    /**
     * پاک کردن همه رویدادها
     */
    fun clearAll(context: Context) {
        try {
            File(context.filesDir, FILE_NAME).delete()
        } catch (e: Exception) {
            // silent fail
        }
    }

    /**
     * تعداد کل رویدادها
     */
    fun getEventCount(context: Context): Int {
        return getAllEvents(context).length()
    }
}

/**
 * لیست رویدادهای استاندارد
 */
object AnalyticsEvents {
    const val APP_OPEN = "app_open"
    const val ANALYSIS_STARTED = "analysis_started"
    const val ANALYSIS_COMPLETED = "analysis_completed"
    const val ANALYSIS_FAILED = "analysis_failed"
    const val SYMBOL_SELECTED = "symbol_selected"
    const val TIMEFRAME_SELECTED = "timeframe_selected"
    const val THEME_CHANGED = "theme_changed"
    const val SHARE_CLICKED = "share_clicked"
    const val HISTORY_VIEWED = "history_viewed"
    const val TUTORIAL_COMPLETED = "tutorial_completed"
    const val SETTINGS_OPENED = "settings_opened"
}