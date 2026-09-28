package com.chartyar.app.data

import com.chartyar.app.analysis.Candle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

object BinanceApi {

    private const val BASE_URL = "https://api.binance.com/api/v3/klines"

    /**
     * لیست ارزهای محبوب
     */
    val popularSymbols = listOf(
        "BTCUSDT" to "بیت‌کوین (BTC)",
        "ETHUSDT" to "اتریوم (ETH)",
        "BNBUSDT" to "بایننس کوین (BNB)",
        "SOLUSDT" to "سولانا (SOL)",
        "XRPUSDT" to "ریپل (XRP)",
        "ADAUSDT" to "کاردانو (ADA)",
        "DOGEUSDT" to "دوج‌کوین (DOGE)",
        "MATICUSDT" to "پالیگان (MATIC)",
        "DOTUSDT" to "پولکادات (DOT)",
        "LINKUSDT" to "چین‌لینک (LINK)",
        "AVAXUSDT" to "آوالانچ (AVAX)",
        "UNIUSDT" to "یونی‌سواپ (UNI)",
        "ATOMUSDT" to "کازموس (ATOM)",
        "LTCUSDT" to "لایت‌کوین (LTC)",
        "TRXUSDT" to "ترون (TRX)",
        "ETCUSDT" to "اتریوم کلاسیک (ETC)",
        "FILUSDT" to "فایل‌کوین (FIL)",
        "NEARUSDT" to "نیر (NEAR)",
        "APTUSDT" to "اپتوس (APT)",
        "ARBUSDT" to "آربیتروم (ARB)",
        "OPUSDT" to "اپتیمیزم (OP)",
        "INJUSDT" to "اینجکتیو (INJ)",
        "SUIUSDT" to "سویی (SUI)",
        "SEIUSDT" to "سی (SEI)",
        "TONUSDT" to "تون‌کوین (TON)",
        "PEPEUSDT" to "پپه (PEPE)",
        "SHIBUSDT" to "شیبا (SHIB)",
        "WIFUSDT" to "دوج‌ویف (WIF)",
        "BONKUSDT" to "بونک (BONK)",
        "FETUSDT" to "فچ (FET)"
    )

    /**
     * تبدیل تایم‌فریم اپ به فرمت بایننس
     * 1m→1m, 5m→5m, 15m→15m, 30m→30m
     * 1H→1h, 4H→4h, 1D→1d, 1W→1w, 1M→1M
     */
    private fun mapInterval(timeframe: String): String {
        return when (timeframe) {
            "1m" -> "1m"
            "5m" -> "5m"
            "15m" -> "15m"
            "30m" -> "30m"
            "1H" -> "1h"
            "4H" -> "4h"
            "1D" -> "1d"
            "1W" -> "1w"
            "1M" -> "1M"
            else -> "4h"
        }
    }

    /**
     * دریافت کندل‌ها از بایننس
     * @param symbol جفت ارز مثل SOLUSDT
     * @param timeframe تایم‌فریم مثل 4H
     * @param limit تعداد کندل (حداکثر ۱۰۰۰)
     * @return لیست Candle یا null اگه خطا بود
     */
    suspend fun fetchCandles(
        symbol: String,
        timeframe: String,
        limit: Int = 200
    ): List<Candle>? = withContext(Dispatchers.IO) {
        try {
            val interval = mapInterval(timeframe)
            val urlString = "$BASE_URL?symbol=$symbol&interval=$interval&limit=$limit"
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            if (responseCode != 200) {
                connection.disconnect()
                return@withContext null
            }

            val response = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            parseKlines(response)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * تبدیل JSON بایننس به لیست Candle
     * فرمت هر کندل:
     * [
     *   openTime,
     *   open,
     *   high,
     *   low,
     *   close,
     *   volume,
     *   closeTime,
     *   ...
     * ]
     */
    private fun parseKlines(json: String): List<Candle>? {
        return try {
            val array = JSONArray(json)
            val candles = mutableListOf<Candle>()

            for (i in 0 until array.length()) {
                val kline = array.getJSONArray(i)
                val openTime = kline.getLong(0)
                val open = kline.getString(1).toDouble()
                val high = kline.getString(2).toDouble()
                val low = kline.getString(3).toDouble()
                val close = kline.getString(4).toDouble()
                val volume = kline.getString(5).toDouble()

                candles.add(
                    Candle(
                        open = open,
                        high = high,
                        low = low,
                        close = close,
                        volume = volume,
                        timestamp = openTime
                    )
                )
            }

            if (candles.size >= 20) candles else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * چک کردن وجود اینترنت
     */
    suspend fun isOnline(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.binance.com/api/v3/ping")
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.requestMethod = "GET"
            val code = connection.responseCode
            connection.disconnect()
            code == 200
        } catch (e: Exception) {
            false
        }
    }
}