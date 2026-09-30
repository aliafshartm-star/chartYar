package com.chartyar.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * مدیریت درخواست امتیازدهی
 */
class RateAppManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("rate_app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAUNCH_COUNT = "launch_count"
        private const val KEY_LAST_SHOWN = "last_shown"
        private const val KEY_ALREADY_RATED = "already_rated"
        private const val KEY_DONT_ASK = "dont_ask"

        private const val MIN_LAUNCHES = 5
        private const val DAYS_BETWEEN_PROMPTS = 7L
    }

    /**
     * ثبت باز شدن اپ
     */
    fun recordLaunch() {
        val count = prefs.getInt(KEY_LAUNCH_COUNT, 0) + 1
        prefs.edit().putInt(KEY_LAUNCH_COUNT, count).apply()
    }

    /**
     * آیا باید درخواست امتیاز نشون داده بشه؟
     */
    fun shouldShowRateDialog(): Boolean {
        if (prefs.getBoolean(KEY_ALREADY_RATED, false)) return false
        if (prefs.getBoolean(KEY_DONT_ASK, false)) return false

        val launches = prefs.getInt(KEY_LAUNCH_COUNT, 0)
        if (launches < MIN_LAUNCHES) return false

        val lastShown = prefs.getLong(KEY_LAST_SHOWN, 0L)
        val now = System.currentTimeMillis()
        val daysSince = (now - lastShown) / (24 * 60 * 60 * 1000)

        return daysSince >= DAYS_BETWEEN_PROMPTS
    }

    /**
     * ثبت نمایش دیالوگ
     */
    fun markShown() {
        prefs.edit().putLong(KEY_LAST_SHOWN, System.currentTimeMillis()).apply()
    }

    /**
     * ثبت اینکه کاربر امتیاز داد
     */
    fun markAsRated() {
        prefs.edit().putBoolean(KEY_ALREADY_RATED, true).apply()
    }

    /**
     * ثبت اینکه کاربر گفت «دیگه نپرس»
     */
    fun markDontAsk() {
        prefs.edit().putBoolean(KEY_DONT_ASK, true).apply()
    }

    /**
     * باز کردن صفحه اپ در کافه‌بازار
     */
    fun openInCafeBazaar() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("bazaar://details?id=${context.packageName}")
                setPackage("com.farsitel.bazaar")
            }
            context.startActivity(intent)
            markAsRated()
        } catch (e: Exception) {
            // کافه‌بازار نصب نیست، برو به مایکت
            openInMyket()
        }
    }

    /**
     * باز کردن صفحه اپ در مایکت
     */
    private fun openInMyket() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("myket://comment?id=${context.packageName}")
                setPackage("ir.mservices.market")
            }
            context.startActivity(intent)
            markAsRated()
        } catch (e: Exception) {
            // مایکت هم نیست
        }
    }
}