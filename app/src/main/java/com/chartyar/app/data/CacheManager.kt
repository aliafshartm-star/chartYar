package com.chartyar.app.data

import android.content.Context
import com.chartyar.app.analysis.Candle
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * مدیریت Cache کندل‌ها
 * داده‌ها توی فایل‌های JSON ذخیره می‌شن
 */
class CacheManager(private val context: Context) {

    private val cacheDir: File
        get() = File(context.cacheDir, "candles").apply {
            if (!exists()) mkdirs()
        }

    /**
     * مدت اعتبار Cache (۵ دقیقه)
     */
    private val cacheValidityMs = 5 * 60 * 1000L

    /**
     * ذخیره کندل‌ها
     */
    fun saveCandles(symbol: String, timeframe: String, candles: List<Candle>) {
        try {
            val file = getCacheFile(symbol, timeframe)
            val jsonArray = JSONArray()
            candles.forEach { candle ->
                val obj = JSONObject().apply {
                    put("o", candle.open)
                    put("h", candle.high)
                    put("l", candle.low)
                    put("c", candle.close)
                    put("v", candle.volume)
                    put("t", candle.timestamp)
                }
                jsonArray.put(obj)
            }
            file.writeText(jsonArray.toString())
        } catch (e: Exception) {
            // silent fail
        }
    }

    /**
     * خواندن کندل‌ها از Cache
     * @return null اگه Cache نبود یا منقضی شده بود
     */
    fun loadCandles(symbol: String, timeframe: String): List<Candle>? {
        try {
            val file = getCacheFile(symbol, timeframe)
            if (!file.exists()) return null

            // چک کردن اعتبار
            val age = System.currentTimeMillis() - file.lastModified()
            if (age > cacheValidityMs) {
                file.delete()
                return null
            }

            val json = file.readText()
            val jsonArray = JSONArray(json)
            val candles = mutableListOf<Candle>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                candles.add(
                    Candle(
                        open = obj.getDouble("o"),
                        high = obj.getDouble("h"),
                        low = obj.getDouble("l"),
                        close = obj.getDouble("c"),
                        volume = obj.optDouble("v", 0.0),
                        timestamp = obj.optLong("t", 0L)
                    )
                )
            }

            return if (candles.size >= 20) candles else null
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * پاک کردن Cache یه ارز خاص
     */
    fun clearCache(symbol: String, timeframe: String) {
        try {
            getCacheFile(symbol, timeframe).delete()
        } catch (e: Exception) {
            // silent fail
        }
    }

    /**
     * پاک کردن همه Cache
     */
    fun clearAllCache() {
        try {
            cacheDir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            // silent fail
        }
    }

    /**
     * مسیر فایل Cache
     */
    private fun getCacheFile(symbol: String, timeframe: String): File {
        return File(cacheDir, "${symbol}_${timeframe}.json")
    }
}