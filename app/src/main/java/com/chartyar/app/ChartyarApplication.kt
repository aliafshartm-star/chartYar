package com.chartyar.app

import android.app.Application
import android.util.Log
import com.chartyar.app.security.GlobalExceptionHandler
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import ir.tapsell.mediation.Tapsell

class ChartyarApplication : Application() {

    private var firebaseReady = false
    private var firebaseFailed = false

    override fun onCreate() {
        super.onCreate()

        Log.e("ViaChart", "App started")

        // فعال‌سازی حالت دیباگ Tapsell برای دیدن لاگ‌ها
        try {
            Tapsell.setDebugMode(true)
            Log.e("ViaChart", "Tapsell debug mode enabled")
        } catch (e: Exception) {
            Log.e("ViaChart", "Tapsell debug failed: ${e.message}")
        }

        GlobalExceptionHandler.install(applicationContext) { throwable ->
            Log.e("ViaChart", "Uncaught exception", throwable)
            sendToFirebase(throwable)
        }

        Log.e("ViaChart", "App ready")
    }

    /**
     * Lazy Firebase initialization
     */
    private fun sendToFirebase(throwable: Throwable) {
        if (firebaseFailed) return

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

        try {
            FirebaseCrashlytics.getInstance().recordException(throwable)
            Log.e("ViaChart", "Error sent to Firebase")
        } catch (e: Exception) {
            Log.e("ViaChart", "Firebase record failed: ${e.message}")
        }
    }
}