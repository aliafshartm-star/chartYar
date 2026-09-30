package com.chartyar.app.util

import android.app.ActivityManager
import android.content.Context

/**
 * نظارت بر مصرف حافظه
 */
object MemoryMonitor {

    data class MemoryInfo(
        val usedMB: Long,
        val totalMB: Long,
        val availableMB: Long,
        val usagePercent: Int,
        val isLowMemory: Boolean,
        val isCritical: Boolean
    )

    fun getMemoryInfo(context: Context): MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMB = memoryInfo.totalMem / (1024 * 1024)
        val availableMB = memoryInfo.availMem / (1024 * 1024)
        val usedMB = totalMB - availableMB
        val usagePercent = if (totalMB > 0) ((usedMB * 100) / totalMB).toInt() else 0

        return MemoryInfo(
            usedMB = usedMB,
            totalMB = totalMB,
            availableMB = availableMB,
            usagePercent = usagePercent,
            isLowMemory = memoryInfo.lowMemory,
            isCritical = usagePercent > 90
        )
    }

    /**
     * چک اگه حافظه بحرانیه
     */
    fun isCritical(context: Context): Boolean {
        return getMemoryInfo(context).isCritical
    }

    /**
     * پاک کردن حافظه (فقط برای مواقع اضطراری)
     */
    fun requestGC() {
        System.gc()
        Runtime.getRuntime().gc()
    }
}