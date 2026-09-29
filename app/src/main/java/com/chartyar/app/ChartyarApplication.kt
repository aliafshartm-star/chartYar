package com.chartyar.app

import android.app.Application
import android.content.Context
import com.chartyar.app.data.LocalAnalytics
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * کلاس Application اصلی
 * برای مدیریت خطاهای سراسری و Crash Reporting
 */
class ChartyarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // نصب Crash Handler
        installCrashHandler()
    }

    private fun installCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                saveCrashLog(applicationContext, throwable)
            } catch (e: Exception) {
                // silent
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun saveCrashLog(context: Context, throwable: Throwable) {
        try {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)

            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

            val crashLog = buildString {
                appendLine("=== CRASH REPORT ===")
                appendLine("Time: $timestamp")
                appendLine("Thread: ${Thread.currentThread().name}")
                appendLine("Android: ${android.os.Build.VERSION.SDK_INT}")
                appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                appendLine("App Version: 1.0.0")
                appendLine("---")
                appendLine(sw.toString())
            }

            val crashFile = File(context.filesDir, "crash_logs.txt")
            crashFile.appendText(crashLog + "\n\n")

            // ثبت توی Analytics
            LocalAnalytics.logEvent(
                context,
                "app_crash",
                mapOf("message" to (throwable.message ?: "unknown"))
            )
        } catch (e: Exception) {
            // silent
        }
    }
}