package com.chartyar.app.data

import android.content.Context
import org.json.JSONArray
import java.io.File

/**
 * مدیریت لیست علاقه‌مندی ارزها
 */
class WatchlistManager(private val context: Context) {

    private val file: File
        get() = File(context.filesDir, "watchlist.json")

    /**
     * دریافت همه آیتم‌ها
     */
    fun getAll(): List<String> {
        return try {
            if (!file.exists()) return emptyList()
            val json = JSONArray(file.readText())
            val result = mutableListOf<String>()
            for (i in 0 until json.length()) {
                result.add(json.getString(i))
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * افزودن به لیست
     */
    fun add(symbol: String) {
        try {
            val current = getAll().toMutableList()
            if (!current.contains(symbol)) {
                current.add(symbol)
                save(current)
            }
        } catch (e: Exception) {
            // silent
        }
    }

    /**
     * حذف از لیست
     */
    fun remove(symbol: String) {
        try {
            val current = getAll().toMutableList()
            if (current.remove(symbol)) {
                save(current)
            }
        } catch (e: Exception) {
            // silent
        }
    }

    /**
     * چک وجود در لیست
     */
    fun contains(symbol: String): Boolean {
        return getAll().contains(symbol)
    }

    /**
     * toggle
     */
    fun toggle(symbol: String): Boolean {
        return if (contains(symbol)) {
            remove(symbol)
            false
        } else {
            add(symbol)
            true
        }
    }

    /**
     * تعداد
     */
    fun size(): Int = getAll().size

    /**
     * خالی بودن
     */
    fun isEmpty(): Boolean = getAll().isEmpty()

    /**
     * پاک کردن همه
     */
    fun clear() {
        try {
            file.delete()
        } catch (e: Exception) {
            // silent
        }
    }

    private fun save(items: List<String>) {
        try {
            val json = JSONArray()
            items.forEach { json.put(it) }
            file.writeText(json.toString())
        } catch (e: Exception) {
            // silent
        }
    }
}