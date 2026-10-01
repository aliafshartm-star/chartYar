package com.chartyar.app

import android.app.Application
import com.chartyar.app.security.GlobalExceptionHandler

class ChartyarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        GlobalExceptionHandler.install(applicationContext) { throwable ->
            // placeholder
        }
    }
}