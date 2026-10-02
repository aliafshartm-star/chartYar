package com.chartyar.app.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "price_alerts")
data class PriceAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val targetPrice: Double,
    val condition: String,
    val isActive: Boolean = true,
    val isTriggered: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val triggeredAt: Long = 0L,
    val note: String = "",
    val currency: String = "TOMAN"
) {
    fun conditionText(): String {
        return if (condition == "ABOVE") "بالاتر از" else "پایین‌تر از"
    }
}

@Dao
interface PriceAlertDao {

    @Query("SELECT * FROM price_alerts ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PriceAlert>>

    @Query("SELECT * FROM price_alerts WHERE isActive = 1 AND isTriggered = 0")
    suspend fun getActiveAlerts(): List<PriceAlert>

    @Query("SELECT * FROM price_alerts WHERE id = :id")
    suspend fun getById(id: Long): PriceAlert?

    @Insert
    suspend fun insert(alert: PriceAlert): Long

    @Update
    suspend fun update(alert: PriceAlert)

    @Query("DELETE FROM price_alerts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM price_alerts")
    suspend fun clearAll()

    @Query("UPDATE price_alerts SET isTriggered = 1, triggeredAt = :time WHERE id = :id")
    suspend fun markTriggered(id: Long, time: Long)

    @Query("UPDATE price_alerts SET isActive = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)
}

object AlertHelper {

    fun createAlert(
        symbol: String,
        targetPrice: Double,
        condition: String,
        note: String = "",
        currency: String = "TOMAN"
    ): PriceAlert {
        return PriceAlert(
            symbol = symbol,
            targetPrice = targetPrice,
            condition = condition,
            note = note,
            currency = currency
        )
    }

    /**
     * چک کن آیا قیمت به هدف رسیده
     * currentPriceToman = قیمت فعلی به تومان
     * targetPrice = قیمت هدف (اگه currency=USD، به دلار)
     * currency = TOMAN یا USD
     */
    fun shouldTrigger(
        alert: PriceAlert,
        currentPriceToman: Double,
        usdtTomanRate: Double = 0.0
    ): Boolean {
        if (!alert.isActive || alert.isTriggered) return false

        val targetToman: Double = when (alert.currency) {
            "USD" -> {
                if (usdtTomanRate <= 0.0) return false
                alert.targetPrice * usdtTomanRate
            }
            else -> alert.targetPrice
        }

        return when (alert.condition) {
            "ABOVE" -> currentPriceToman >= targetToman
            "BELOW" -> currentPriceToman <= targetToman
            else -> false
        }
    }

    /**
     * فاصله تا هدف (درصد)
     */
    fun distancePercent(
        alert: PriceAlert,
        currentPriceToman: Double,
        usdtTomanRate: Double = 0.0
    ): Double {
        if (currentPriceToman == 0.0) return 0.0

        val targetToman: Double = when (alert.currency) {
            "USD" -> {
                if (usdtTomanRate <= 0.0) return 0.0
                alert.targetPrice * usdtTomanRate
            }
            else -> alert.targetPrice
        }

        return ((targetToman - currentPriceToman) / currentPriceToman) * 100.0
    }
}