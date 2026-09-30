package com.chartyar.app.security

import android.content.Context
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * مدیریت سراسری خطاها
 * همه خطاها رو در فایل ذخیره می‌کنه برای بررسی
 */
class GlobalExceptionHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?,
    private val onCrash: ((Throwable) -> Unit)? = null
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // ذخیره خطا
            saveCrashLog(thread, throwable)

            // فراخوانی callback
            onCrash?.invoke(throwable)

        } catch (e: Exception) {
            // اگه حتی ذخیره خطا هم شکست خورد
        } finally {
            // فراخوانی handler پیش‌فرض
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun saveCrashLog(thread: Thread, throwable: Throwable) {
        try {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)

            val timestamp = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.US
            ).format(Date())

            val crashLog = buildString {
                appendLine("═══════════════════════════════")
                appendLine("🚨 CRASH REPORT")
                appendLine("═══════════════════════════════")
                appendLine("⏰ Time: $timestamp")
                appendLine("🧵 Thread: ${thread.name}")
                appendLine("📱 Android: ${android.os.Build.VERSION.SDK_INT}")
                appendLine("📱 Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                appendLine("📦 App Version: 1.0.0")
                appendLine("───────────────────────────────")
                appendLine(sw.toString())
                appendLine("═══════════════════════════════")
                appendLine()
            }

            val crashFile = File(context.filesDir, "crash_logs.txt")

            // اگه فایل بزرگ شد (بیشتر از ۵۰۰ کیلوبایت)، قدیمی‌ها رو پاک کن
            if (crashFile.exists() && crashFile.length() > 500_000) {
                val existing = crashFile.readText()
                val trimmed = existing.takeLast(200_000)
                crashFile.writeText(trimmed + "\n\n" + crashLog)
            } else {
                crashFile.appendText(crashLog)
            }
        } catch (e: Exception) {
            // silent
        }
    }

    companion object {
        /**
         * نصب handler
         */
        fun install(context: Context, onCrash: ((Throwable) -> Unit)? = null) {
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler(
                GlobalExceptionHandler(context.applicationContext, defaultHandler, onCrash)
            )
        }

        /**
         * خواندن همه crash logها
         */
        fun getAllCrashes(context: Context): String {
            return try {
                val file = File(context.filesDir, "crash_logs.txt")
                if (file.exists()) file.readText() else ""
            } catch (e: Exception) {
                ""
            }
        }

        /**
         * تعداد crashها
         */
        fun getCrashCount(context: Context): Int {
            return try {
                val content = getAllCrashes(context)
                if (content.isEmpty()) 0
                else content.split("🚨 CRASH REPORT").size - 1
            } catch (e: Exception) {
                0
            }
        }

        /**
         * پاک کردن همه crash logها
         */
        fun clearCrashes(context: Context) {
            try {
                File(context.filesDir, "crash_logs.txt").delete()
            } catch (e: Exception) {
                // silent
            }
        }
    }
}