package com.chartyar.app.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Helper برای مدیریت Runtime Permissions
 */
object PermissionHelper {

    /**
     * مجوزهای لازم برای گالری بر اساس نسخه اندروید
     */
    fun getGalleryPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            // Android 12 و پایین‌تر
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /**
     * چک کن آیا مجوز گالری داده شده
     */
    fun hasGalleryPermission(context: Context): Boolean {
        val permissions = getGalleryPermissions()
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * آیا نیازی به درخواست مجوز هست؟
     * در Android 13+، Photo Picker نیاز به مجوز نداره
     */
    fun needsPermissionRequest(): Boolean {
        return false
    }
}