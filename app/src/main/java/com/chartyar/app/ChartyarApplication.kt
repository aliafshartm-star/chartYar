package com.chartyar.app

import android.app.Application
import android.util.Log
import com.chartyar.app.security.GlobalExceptionHandler

class ChartyarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // ============ مدیریت خطاهای سراسری ============
        GlobalExceptionHandler.install(applicationContext) { throwable ->
            Log.e("ChartYar", "Uncaught exception", throwable)
        }

        // ============ Tapsell ============
        // تپسل خودش به صورت خودکار از روی AndroidManifest راه‌اندازی می‌شه
        // نیازی به کد راه‌اندازی دستی نیست
    }
}