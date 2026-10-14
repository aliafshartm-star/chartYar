package com.chartyar.app

import android.app.Application
import android.util.Log
import com.chartyar.app.security.GlobalExceptionHandler
import ir.tapsell.mediation.Tapsell

class ChartyarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.d("ChartYar", "App started")

        // فعال‌سازی Debug Mode Tapsell برای دیدن لاگ‌ها
        try {
            Tapsell.setDebugMode(true)
            Log.d("ChartYar", "Tapsell debug mode enabled")
        } catch (e: Exception) {
            Log.e("ChartYar", "Tapsell debug failed: ${e.message}")
        }

        // نصب Global Exception Handler
        try {
            GlobalExceptionHandler.install(applicationContext) { throwable ->
                Log.e("ChartYar", "Uncaught exception", throwable)
            }
        } catch (e: Exception) {
            Log.e("ChartYar", "Exception handler failed: ${e.message}")
        }

        Log.d("ChartYar", "App ready")
    }
}