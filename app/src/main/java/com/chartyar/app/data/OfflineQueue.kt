package com.chartyar.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class OfflineQueue(private val context: Context) {

    private val queueFile: File
        get() = File(context.filesDir, "offline_queue.json")

    private val maxQueueSize = 50

    data class QueuedRequest(
        val id: String,
        val type: String,
        val data: String,
        val timestamp: Long
    )

    fun enqueue(type: String, data: String) {
        try {
            val queue = loadQueue().toMutableList()

            if (queue.size >= maxQueueSize) {
                queue.removeAt(0)
            }

            queue.add(
                QueuedRequest(
                    id = System.currentTimeMillis().toString(),
                    type = type,
                    data = data,
                    timestamp = System.currentTimeMillis()
                )
            )

            saveQueue(queue)
        } catch (e: Exception) {
            // silent
        }
    }

    fun loadQueue(): List<QueuedRequest> {
        return try {
            if (!queueFile.exists()) return emptyList()
            val json = JSONArray(queueFile.readText())
            val result = mutableListOf<QueuedRequest>()

            for (i in 0 until json.length()) {
                val item = json.getJSONObject(i)
                result.add(
                    QueuedRequest(
                        id = item.optString("id", ""),
                        type = item.optString("type", ""),
                        data = item.optString("data", ""),
                        timestamp = item.optLong("timestamp", 0L)
                    )
                )
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun remove(id: String) {
        try {
            val queue = loadQueue().toMutableList()
            queue.removeAll { it.id == id }
            saveQueue(queue)
        } catch (e: Exception) {
            // silent
        }
    }

    fun clear() {
        try {
            queueFile.delete()
        } catch (e: Exception) {
            // silent
        }
    }

    fun size(): Int = loadQueue().size

    fun isEmpty(): Boolean = loadQueue().isEmpty()

    private fun saveQueue(queue: List<QueuedRequest>) {
        try {
            val json = JSONArray()
            queue.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("type", item.type)
                    put("data", item.data)
                    put("timestamp", item.timestamp)
                }
                json.put(obj)
            }
            queueFile.writeText(json.toString())
        } catch (e: Exception) {
            // silent
        }
    }
}