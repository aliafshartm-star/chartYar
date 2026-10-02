package com.chartyar.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "analysis_history",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["timeframe"]),
        Index(value = ["signalText"])
    ]
)
data class AnalysisHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val timeframe: String,
    val signalText: String,
    val confidence: Int,
    val currentPrice: Double,
    val entry: Double,
    val stopLoss: Double,
    val target1: Double,
    val target2: Double,
    val target3: Double,
    val trendText: String,
    val imageUri: String? = null
)

@Dao
interface AnalysisHistoryDao {

    @Query("SELECT * FROM analysis_history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<AnalysisHistory>>

    @Query("SELECT * FROM analysis_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<AnalysisHistory>>

    @Query("SELECT COUNT(*) FROM analysis_history")
    suspend fun getCount(): Int

    @Insert
    suspend fun insert(item: AnalysisHistory): Long

    @Query("DELETE FROM analysis_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM analysis_history")
    suspend fun clearAll()
}

@Database(
    entities = [AnalysisHistory::class, PriceAlert::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): AnalysisHistoryDao
    abstract fun alertDao(): PriceAlertDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chartyar_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}