package com.chartyar.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * ردیابی دقت سیگنال‌ها
 */
data class SignalRecord(
    val timestamp: Long,
    val signalText: String,
    val timeframe: String,
    val entry: Double,
    val result: String,  // "correct", "wrong", "pending"
    val reviewedAt: Long = 0L
)

class SignalTrackingManager(private val context: Context) {

    private val file: File
        get() = File(context.filesDir, "signal_tracking.json")

    fun getAll(): List<SignalRecord> {
        return try {
            if (!file.exists()) return emptyList()
            val json = JSONArray(file.readText())
            val result = mutableListOf<SignalRecord>()

            for (i in 0 until json.length()) {
                val obj = json.getJSONObject(i)
                result.add(
                    SignalRecord(
                        timestamp = obj.optLong("timestamp", 0L),
                        signalText = obj.optString("signalText", ""),
                        timeframe = obj.optString("timeframe", ""),
                        entry = obj.optDouble("entry", 0.0),
                        result = obj.optString("result", "pending"),
                        reviewedAt = obj.optLong("reviewedAt", 0L)
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addSignal(record: SignalRecord) {
        try {
            val current = getAll().toMutableList()
            current.add(record)
            save(current)
        } catch (e: Exception) {
            // silent
        }
    }

    fun updateResult(timestamp: Long, result: String) {
        try {
            val current = getAll().toMutableList()
            val index = current.indexOfFirst { it.timestamp == timestamp }
            if (index >= 0) {
                current[index] = current[index].copy(
                    result = result,
                    reviewedAt = System.currentTimeMillis()
                )
                save(current)
            }
        } catch (e: Exception) {
            // silent
        }
    }

    fun clear() {
        try {
            file.delete()
        } catch (e: Exception) {
            // silent
        }
    }

    /**
     * آمار دقت
     */
    data class AccuracyStats(
        val total: Int,
        val correct: Int,
        val wrong: Int,
        val pending: Int,
        val accuracyPercent: Int
    )

    fun getStats(): AccuracyStats {
        val all = getAll()
        val correct = all.count { it.result == "correct" }
        val wrong = all.count { it.result == "wrong" }
        val pending = all.count { it.result == "pending" }
        val reviewed = correct + wrong
        val accuracy = if (reviewed > 0) (correct * 100 / reviewed) else 0

        return AccuracyStats(
            total = all.size,
            correct = correct,
            wrong = wrong,
            pending = pending,
            accuracyPercent = accuracy
        )
    }

    private fun save(items: List<SignalRecord>) {
        try {
            val json = JSONArray()
            items.forEach { item ->
                val obj = JSONObject().apply {
                    put("timestamp", item.timestamp)
                    put("signalText", item.signalText)
                    put("timeframe", item.timeframe)
                    put("entry", item.entry)
                    put("result", item.result)
                    put("reviewedAt", item.reviewedAt)
                }
                json.put(obj)
            }
            file.writeText(json.toString())
        } catch (e: Exception) {
            // silent
        }
    }
}