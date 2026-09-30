package com.chartyar.app.security

import android.content.Context
import android.os.Debug
import android.provider.Settings
import java.io.File

object AntiDebug {

    data class DebugStatus(
        val isDebuggable: Boolean,
        val reasons: List<String>
    )

    fun checkDebug(context: Context): DebugStatus {
        val reasons = mutableListOf<String>()

        // ۱. چک Debugger متصل
        if (Debug.isDebuggerConnected()) {
            reasons.add("Debugger متصل است")
        }

        // ۲. چک ADB فعال
        try {
            val adbEnabled = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.ADB_ENABLED,
                0
            )
            if (adbEnabled == 1) {
                reasons.add("ADB فعال است")
            }
        } catch (e: Exception) {
            // silent
        }

        // ۳. چک فایل‌های debug
        val debugFiles = listOf(
            "/system/bin/gdbserver",
            "/data/local/tmp/gdbserver",
            "/system/bin/strace"
        )
        for (path in debugFiles) {
            if (File(path).exists()) {
                reasons.add("ابزار debug پیدا شد: $path")
            }
        }

        // ۴. چک محیط Emulator
        if (isEmulator()) {
            reasons.add("در حال اجرا روی Emulator")
        }

        return DebugStatus(
            isDebuggable = reasons.isNotEmpty(),
            reasons = reasons
        )
    }

    /**
     * چک Emulator
     */
    fun isEmulator(): Boolean {
        return (
            android.os.Build.FINGERPRINT.startsWith("generic") ||
                    android.os.Build.FINGERPRINT.startsWith("unknown") ||
                    android.os.Build.MODEL.contains("google_sdk") ||
                    android.os.Build.MODEL.contains("Emulator") ||
                    android.os.Build.MODEL.contains("Android SDK built for") ||
                    android.os.Build.MANUFACTURER.contains("Genymotion") ||
                    android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic") ||
                    "google_sdk" == android.os.Build.PRODUCT
            )
    }

    /**
     * چک سریع (فقط برای Release)
     */
    fun isDebugBuild(): Boolean {
        return (0 != (android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE and 0x2))
    }
}