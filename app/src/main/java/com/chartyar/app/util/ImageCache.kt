package com.chartyar.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.security.MessageDigest

object ImageCache {

    private const val CACHE_DIR = "image_cache"
    private const val MAX_CACHE_SIZE = 20 * 1024 * 1024L
    private const val MAX_AGE_MS = 3 * 24 * 60 * 60 * 1000L

    private fun getCacheDir(context: Context): File {
        val dir = File(context.cacheDir, CACHE_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun hashKey(key: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(key.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }.take(32)
        } catch (e: Exception) {
            key.hashCode().toString()
        }
    }

    fun save(context: Context, key: String, bitmap: Bitmap) {
        try {
            val file = File(getCacheDir(context), hashKey(key) + ".jpg")
            val stream = file.outputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            stream.close()
            enforceMaxSize(context)
        } catch (e: Exception) {
            // silent
        }
    }

    fun load(context: Context, key: String): Bitmap? {
        return try {
            val file = File(getCacheDir(context), hashKey(key) + ".jpg")
            if (!file.exists()) return null

            val age = System.currentTimeMillis() - file.lastModified()
            if (age > MAX_AGE_MS) {
                file.delete()
                return null
            }

            BitmapFactory.decodeFile(file.absolutePath)
        } catch (e: Exception) {
            null
        }
    }

    fun clear(context: Context) {
        try {
            getCacheDir(context).listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            // silent
        }
    }

    fun clearOld(context: Context) {
        try {
            val now = System.currentTimeMillis()
            getCacheDir(context).listFiles()?.forEach { file ->
                if (now - file.lastModified() > MAX_AGE_MS) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // silent
        }
    }

    fun getSize(context: Context): Long {
        return try {
            getCacheDir(context).listFiles()?.sumOf { it.length() } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun enforceMaxSize(context: Context) {
        try {
            val files = getCacheDir(context).listFiles() ?: return
            var totalSize = files.sumOf { it.length() }

            if (totalSize > MAX_CACHE_SIZE) {
                val sorted = files.sortedBy { it.lastModified() }
                var sizeToFree = totalSize - MAX_CACHE_SIZE
                for (file in sorted) {
                    if (sizeToFree <= 0) break
                    sizeToFree -= file.length()
                    file.delete()
                }
            }
        } catch (e: Exception) {
            // silent
        }
    }
}