package com.chartyar.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chartyar.app.data.AlertChecker
import com.chartyar.app.data.AppDatabase
import com.chartyar.app.data.CacheManager

class PriceCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            val db = AppDatabase.get(context)
            val cache = CacheManager(context)

            AlertChecker.checkAllAlerts(context, db, cache)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}