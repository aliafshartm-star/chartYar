package com.chartyar.app

import android.app.Application
import android.util.Log
import com.chartyar.app.security.GlobalExceptionHandler
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class ChartyarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // ============ Firebase Initialization ============
        try {
            FirebaseApp.initializeApp(this)
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        } catch (e: Exception) {
            Log.e("ViaChart", "Firebase init failed: ${e.message}")
        }

        // ============ مدیریت خطاهای سراسری ============
        GlobalExceptionHandler.install(applicationContext) { throwable ->
            Log.e("ViaChart", "Uncaught exception", throwable)
            // ارسال به Firebase Crashlytics
            try {
                FirebaseCrashlytics.getInstance().recordException(throwable)
            } catch (e: Exception) {
                // silent
            }
        }
    }
}