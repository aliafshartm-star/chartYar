package com.chartyar.app.work

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.chartyar.app.data.AlertChecker
import com.chartyar.app.data.AppDatabase
import com.chartyar.app.data.CacheManager

class PriceCheckWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        return try {
            val context = applicationContext
            val db = AppDatabase.get(context)
            val cache = CacheManager(context)

            // چک کردن هشدارها (به صورت blocking)
            kotlinx.coroutines.runBlocking {
                AlertChecker.checkAllAlerts(context, db, cache)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}