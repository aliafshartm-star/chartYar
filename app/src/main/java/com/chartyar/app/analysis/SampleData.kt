package com.chartyar.app.analysis

import kotlin.random.Random

/**
 * داده تستی برای آزمایش موتور تحلیل
 * در فاز بعدی، این با کندل‌های استخراج‌شده از تصویر جایگزین می‌شه
 */
object SampleData {

    fun generateBullishCandles(count: Int = 100, startPrice: Double = 67000.0): List<Candle> {
        val candles = mutableListOf<Candle>()
        var price = startPrice
        val random = Random(42)

        for (i in 0 until count) {
            // روند صعودی کلی با نوسان
            val trendUp = if (i > count / 2) 1.003 else 0.999
            price *= trendUp + (random.nextDouble() - 0.5) * 0.02

            val open = price * (1 + (random.nextDouble() - 0.5) * 0.005)
            val close = price
            val high = maxOf(open, close) * (1 + random.nextDouble() * 0.008)
            val low = minOf(open, close) * (1 - random.nextDouble() * 0.008)
            val volume = 1000.0 + random.nextDouble() * 5000

            candles.add(
                Candle(
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = volume,
                    timestamp = System.currentTimeMillis() - (count - i) * 3600000L
                )
            )
            price = close
        }
        return candles
    }

    fun generateBearishCandles(count: Int = 100, startPrice: Double = 67000.0): List<Candle> {
        val candles = mutableListOf<Candle>()
        var price = startPrice
        val random = Random(24)

        for (i in 0 until count) {
            val trendDown = if (i > count / 2) 0.997 else 1.001
            price *= trendDown + (random.nextDouble() - 0.5) * 0.02

            val open = price * (1 + (random.nextDouble() - 0.5) * 0.005)
            val close = price
            val high = maxOf(open, close) * (1 + random.nextDouble() * 0.008)
            val low = minOf(open, close) * (1 - random.nextDouble() * 0.008)
            val volume = 1000.0 + random.nextDouble() * 5000

            candles.add(
                Candle(
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = volume,
                    timestamp = System.currentTimeMillis() - (count - i) * 3600000L
                )
            )
            price = close
        }
        return candles
    }
}