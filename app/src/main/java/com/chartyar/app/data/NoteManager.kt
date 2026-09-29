package com.chartyar.app.data

import android.content.Context
import org.json.JSONObject
import java.io.File

/**
 * مدیریت یادداشت‌های تحلیل
 * هر تحلیل (بر اساس timestamp) می‌تونه یه یادداشت داشته باشه
 */
class NoteManager(private val context: Context) {

    private val notesFile: File
        get() = File(context.filesDir, "analysis_notes.json")

    /**
     * ذخیره یادداشت برای یه تحلیل
     */
    fun saveNote(timestamp: Long, note: String) {
        try {
            val notes = loadAllNotes().toMutableMap()
            if (note.isBlank()) {
                notes.remove(timestamp.toString())
            } else {
                notes[timestamp.toString()] = note
            }
            writeNotes(notes)
        } catch (e: Exception) {
            // silent
        }
    }

    /**
     * خوندن یادداشت یه تحلیل
     */
    fun getNote(timestamp: Long): String {
        return try {
            loadAllNotes()[timestamp.toString()] ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * حذف یادداشت
     */
    fun deleteNote(timestamp: Long) {
        try {
            val notes = loadAllNotes().toMutableMap()
            notes.remove(timestamp.toString())
            writeNotes(notes)
        } catch (e: Exception) {
            // silent
        }
    }

    /**
     * پاک کردن همه یادداشت‌ها
     */
    fun clearAll() {
        try {
            notesFile.delete()
        } catch (e: Exception) {
            // silent
        }
    }

    private fun loadAllNotes(): Map<String, String> {
        return try {
            if (!notesFile.exists()) return emptyMap()
            val json = JSONObject(notesFile.readText())
            val map = mutableMapOf<String, String>()
            json.keys().forEach { key ->
                map[key] = json.getString(key)
            }
            map
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun writeNotes(notes: Map<String, String>) {
        try {
            val json = JSONObject()
            notes.forEach { (key, value) ->
                json.put(key, value)
            }
            notesFile.writeText(json.toString())
        } catch (e: Exception) {
            // silent
        }
    }
}