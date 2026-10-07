package com.chartyar.app

import android.app.Application
import android.util.Log
import com.chartyar.app.security.GlobalExceptionHandler

class ChartyarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.e("ViaChart", "App onCreate started")

        // مدیریت خطاهای سراسری
        GlobalExceptionHandler.install(applicationContext) { throwable ->
            Log.e("ViaChart", "Uncaught exception", throwable)

            // تلاش برای ارسال به Firebase Crashlytics
            try {
                val clazz = Class.forName("com.google.firebase.crashlytics.FirebaseCrashlytics")
                val getInstance = clazz.getMethod("getInstance")
                val instance = getInstance.invoke(null)
                val recordMethod = clazz.getMethod("recordException", Throwable::class.java)
                recordMethod.invoke(instance, throwable)
            } catch (e: Exception) {
                // اگه Firebase نبود یا مشکل داشت، ساکت بمون
                Log.e("ViaChart", "Firebase record failed: ${e.message}")
            }
        }

        Log.e("ViaChart", "App onCreate completed")
    }
}