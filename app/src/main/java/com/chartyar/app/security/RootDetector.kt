package com.chartyar.app.security

import android.content.Context
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader

object RootDetector {

    data class RootStatus(
        val isRooted: Boolean,
        val reasons: List<String>
    )

    fun checkRoot(context: Context): RootStatus {
        val reasons = mutableListOf<String>()

        val rootFiles = listOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su",
            "/magisk/.core/bin/su"
        )
        for (path in rootFiles) {
            if (File(path).exists()) {
                reasons.add("فایل روت: $path")
            }
        }

        val busyboxPaths = listOf(
            "/system/xbin/busybox",
            "/system/bin/busybox"
        )
        for (path in busyboxPaths) {
            if (File(path).exists()) {
                reasons.add("BusyBox پیدا شد")
            }
        }

        try {
            val buildTags = android.os.Build.TAGS
            if (buildTags != null && buildTags.contains("test-keys")) {
                reasons.add("Build tags: test-keys")
            }
        } catch (e: Exception) {
            // silent
        }

        if (canExecuteSu()) {
            reasons.add("دستور su قابل اجراست")
        }

        val rootApps = listOf(
            "com.topjohnwu.magisk",
            "com.koushikdutta.superuser",
            "com.noshufou.android.su",
            "eu.chainfire.supersu",
            "com.thirdparty.superuser",
            "com.yellowes.su"
        )
        for (pkg in rootApps) {
            try {
                context.packageManager.getPackageInfo(pkg, 0)
                reasons.add("اپ روت: $pkg")
            } catch (e: Exception) {
                // اپ نصب نیست
            }
        }

        return RootStatus(
            isRooted = reasons.isNotEmpty(),
            reasons = reasons
        )
    }

    private fun canExecuteSu(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("which", "su"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val result = reader.readLine()
            reader.close()
            result != null && result.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
}