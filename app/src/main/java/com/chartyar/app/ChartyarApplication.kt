package com.chartyar.app

import android.app.Application
import android.util.Log
import com.chartyar.app.security.GlobalExceptionHandler
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class ChartyarApplication : Application() {

    private var firebaseReady = false
    private var firebaseFailed = false

    override fun onCreate() {
        super.onCreate()

        Log.e("ViaChart", "App started")

        GlobalExceptionHandler.install(applicationContext) { throwable ->
            Log.e("ViaChart", "Uncaught exception", throwable)
            sendToFirebase(throwable)
        }

        Log.e("ViaChart", "App ready")
    }

    /**
     * Lazy Firebase initialization
     * Firebase فقط زمانی راه‌اندازی می‌شه که اولین خطا رخ داد.
     * اگه راه‌اندازی fail شد (مثلاً توی ایران)، اپ کرش نمی‌کنه.
     */
    private fun sendToFirebase(throwable: Throwable) {
        if (firebaseFailed) return

        // Lazy init — فقط بار اول
        if (!firebaseReady) {
            try {
                FirebaseApp.initializeApp(this)
                firebaseReady = true
                Log.e("ViaChart", "Firebase initialized")
            } catch (e: Exception) {
                firebaseFailed = true
                Log.e("ViaChart", "Firebase init failed: ${e.message}")
                return
            }
        }

        // ارسال خطا به Crashlytics
        try {
            FirebaseCrashlytics.getInstance().recordException(throwable)
            Log.e("ViaChart", "Error sent to Firebase")
        } catch (e: Exception) {
            Log.e("ViaChart", "Firebase record failed: ${e.message}")
        }
    }
}