package com.chartyar.app

import android.app.Application
import com.chartyar.app.security.GlobalExceptionHandler

class ChartyarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // نصب Global Exception Handler
        GlobalExceptionHandler.install(applicationContext) { throwable ->
            // اینجا می‌تونیم بعداً به سرور بفرستیم
        }
    }
}