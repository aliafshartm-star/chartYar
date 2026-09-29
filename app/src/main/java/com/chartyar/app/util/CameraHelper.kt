package com.chartyar.app.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper برای کار با دوربین
 */
object CameraHelper {

    /**
     * ساخت Uri برای ذخیره عکس دوربین
     */
    fun createImageUri(context: Context): Uri? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val imageFileName = "CHARTS_${timeStamp}_"
            val storageDir = File(context.cacheDir, "camera").apply {
                if (!exists()) mkdirs()
            }
            val imageFile = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
            )

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * پاک کردن عکس‌های قدیمی دوربین
     */
    fun clearOldCameraFiles(context: Context) {
        try {
            val cameraDir = File(context.cacheDir, "camera")
            if (cameraDir.exists()) {
                val files = cameraDir.listFiles() ?: return
                val oneDayAgo = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
                files.forEach { file ->
                    if (file.lastModified() < oneDayAgo) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            // silent fail
        }
    }
}