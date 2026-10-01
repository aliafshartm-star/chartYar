package com.chartyar.app

import android.app.Application
import android.util.Log
import com.chartyar.app.security.GlobalExceptionHandler
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.TapsellPlusInitListener
import ir.tapsell.mediation.adnetworks.AdNetworkError
import ir.tapsell.mediation.adnetworks.AdNetworks

class ChartyarApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // ============ مدیریت خطاهای سراسری ============
        GlobalExceptionHandler.install(applicationContext) { throwable ->
            Log.e("ChartYar", "Uncaught exception", throwable)
        }

        // ============ راه‌اندازی تپسل ============
        try {
            Tapsell.setInitListener(object : TapsellPlusInitListener {
                override fun onInitializeSuccess(adNetworks: AdNetworks) {
                    Log.d("ChartYar", "Tapsell initialized successfully")
                }

                override fun onInitializeFailed(
                    adNetworks: AdNetworks,
                    adNetworkError: AdNetworkError
                ) {
                    Log.e("ChartYar", "Tapsell initialization failed: ${adNetworkError.name}")
                }
            })

            // برای تست، حالت دیباگ رو روشن کن (بعداً برای انتشار خاموش کن)
            // Tapsell.setDebugMode(true)
        } catch (e: Exception) {
            Log.e("ChartYar", "Tapsell init exception", e)
        }
    }
}