package com.chartyar.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * مدیریت پورتفولیو (دارایی‌های کاربر)
 */
data class PortfolioItem(
    val symbol: String,
    val amount: Double,
    val buyPrice: Double,
    val addedAt: Long = System.currentTimeMillis()
) {
    fun currentValue(currentPrice: Double): Double = amount * currentPrice
    fun invested(): Double = amount * buyPrice
    fun profitLoss(currentPrice: Double): Double = currentValue(currentPrice) - invested()
    fun profitLossPercent(currentPrice: Double): Double {
        val inv = invested()
        return if (inv > 0) ((currentValue(currentPrice) - inv) / inv) * 100.0 else 0.0
    }
}

class PortfolioManager(private val context: Context) {

    private val file: File
        get() = File(context.filesDir, "portfolio.json")

    fun getAll(): List<PortfolioItem> {
        return try {
            if (!file.exists()) return emptyList()
            val json = JSONArray(file.readText())
            val result = mutableListOf<PortfolioItem>()

            for (i in 0 until json.length()) {
                val obj = json.getJSONObject(i)
                result.add(
                    PortfolioItem(
                        symbol = obj.optString("symbol", ""),
                        amount = obj.optDouble("amount", 0.0),
                        buyPrice = obj.optDouble("buyPrice", 0.0),
                        addedAt = obj.optLong("addedAt", 0L)
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun add(item: PortfolioItem) {
        try {
            val current = getAll().toMutableList()
            // اگه ارز قبلاً بود، آپدیت کن
            val existing = current.indexOfFirst { it.symbol == item.symbol }
            if (existing >= 0) {
                current[existing] = item
            } else {
                current.add(item)
            }
            save(current)
        } catch (e: Exception) {
            // silent
        }
    }

    fun remove(symbol: String) {
        try {
            val current = getAll().toMutableList()
            current.removeAll { it.symbol == symbol }
            save(current)
        } catch (e: Exception) {
            // silent
        }
    }

    fun getBySymbol(symbol: String): PortfolioItem? {
        return getAll().find { it.symbol == symbol }
    }

    fun clear() {
        try {
            file.delete()
        } catch (e: Exception) {
            // silent
        }
    }

    /**
     * محاسبه ارزش کل
     */
    fun getTotalValue(prices: Map<String, Double>): Double {
        return getAll().sumOf { item ->
            val price = prices[item.symbol] ?: item.buyPrice
            item.currentValue(price)
        }
    }

    /**
     * محاسبه سود/ضرر کل
     */
    fun getTotalProfitLoss(prices: Map<String, Double>): Double {
        return getAll().sumOf { item ->
            val price = prices[item.symbol] ?: item.buyPrice
            item.profitLoss(price)
        }
    }

    /**
     * درصد سود/ضرر کل
     */
    fun getTotalProfitLossPercent(prices: Map<String, Double>): Double {
        val totalInvested = getAll().sumOf { it.invested() }
        if (totalInvested == 0.0) return 0.0
        return (getTotalProfitLoss(prices) / totalInvested) * 100.0
    }

    private fun save(items: List<PortfolioItem>) {
        try {
            val json = JSONArray()
            items.forEach { item ->
                val obj = JSONObject().apply {
                    put("symbol", item.symbol)
                    put("amount", item.amount)
                    put("buyPrice", item.buyPrice)
                    put("addedAt", item.addedAt)
                }
                json.put(obj)
            }
            file.writeText(json.toString())
        } catch (e: Exception) {
            // silent
        }
    }
}