package com.chartyar.app.data

import android.content.Context
import com.chartyar.app.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AlertChecker {

    suspend fun checkAllAlerts(
        context: Context,
        db: AppDatabase,
        cache: CacheManager
    ): List<PriceAlert> = withContext(Dispatchers.IO) {
        val triggered = mutableListOf<PriceAlert>()

        try {
            val activeAlerts = db.alertDao().getActiveAlerts()
            if (activeAlerts.isEmpty()) return@withContext emptyList()

            val needsUsdRate = activeAlerts.any { it.currency == "USD" }
            val usdtTomanRate = if (needsUsdRate) {
                try {
                    PricesApi.fetchUsdtTomanRate()
                } catch (e: Exception) {
                    0.0
                }
            } else {
                0.0
            }

            val grouped = activeAlerts.groupBy { it.symbol }

            for ((symbol, alerts) in grouped) {
                val currentPrice = fetchCurrentPrice(context, symbol, cache)
                    ?: continue

                for (alert in alerts) {
                    if (AlertHelper.shouldTrigger(alert, currentPrice, usdtTomanRate)) {
                        db.alertDao().markTriggered(alert.id, System.currentTimeMillis())

                        NotificationHelper.showPriceAlertNotification(
                            context,
                            alert,
                            currentPrice
                        )

                        triggered.add(alert)
                    }
                }
            }
        } catch (e: Exception) {
        }

        triggered
    }

    private suspend fun fetchCurrentPrice(
        context: Context,
        symbol: String,
        cache: CacheManager
    ): Double? {
        return try {
            val directPrice = NobitexApi.fetchCurrentPrice(symbol)
            if (directPrice != null && directPrice > 0) {
                return directPrice
            }

            val candles = NobitexApi.fetchCandles(symbol, "1H", 5)
            if (candles != null && candles.isNotEmpty()) {
                return candles.last().close
            }

            val cachedCandles = cache.loadCandles(symbol, "1H")
            if (cachedCandles != null && cachedCandles.isNotEmpty()) {
                return cachedCandles.last().close
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun checkSingleAlert(
        context: Context,
        db: AppDatabase,
        cache: CacheManager,
        alertId: Long
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val alert = db.alertDao().getById(alertId) ?: return@withContext false
            if (!alert.isActive || alert.isTriggered) return@withContext false

            val currentPrice = fetchCurrentPrice(context, alert.symbol, cache)
                ?: return@withContext false

            val usdtTomanRate = if (alert.currency == "USD") {
                try {
                    PricesApi.fetchUsdtTomanRate()
                } catch (e: Exception) {
                    0.0
                }
            } else {
                0.0
            }

            if (AlertHelper.shouldTrigger(alert, currentPrice, usdtTomanRate)) {
                db.alertDao().markTriggered(alert.id, System.currentTimeMillis())
                NotificationHelper.showPriceAlertNotification(context, alert, currentPrice)
                return@withContext true
            }

            false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun countActiveAlerts(db: AppDatabase): Int = withContext(Dispatchers.IO) {
        try {
            db.alertDao().getActiveAlerts().size
        } catch (e: Exception) {
            0
        }
    }
}