package com.chartyar.app.data

import android.content.Context
import androidx.core.content.edit
import java.util.Calendar

class AnalysisLimitManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("analysis_limit_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_RESET = "last_reset_date"
        private const val KEY_REMAINING = "remaining_analyses"
        private const val KEY_AD_COUNT_TODAY = "ad_count_today"
        private const val KEY_UNLIMITED_UNTIL = "unlimited_until"
        private const val KEY_TOTAL_ADS_WATCHED = "total_ads_watched"

        const val INITIAL_FREE = 3
        const val ADS_FOR_REWARD = 3
        const val ANALYSES_PER_REWARD = 2
        const val ADS_FOR_BONUS = 10
        const val BONUS_HOURS = 12
    }

    data class LimitStatus(
        val remainingFree: Int,
        val adCountToday: Int,
        val isUnlimited: Boolean,
        val unlimitedUntil: Long,
        val canAnalyze: Boolean,
        val hasReachedBonus: Boolean,
        val adsNeededForBonus: Int
    )

    init {
        resetIfNewDay()
    }

    /**
     * چک کن آیا روز جدید شده و ریست کن
     */
    fun resetIfNewDay() {
        val today = getTodayKey()
        val lastReset = prefs.getString(KEY_LAST_RESET, null)

        if (lastReset != today) {
            prefs.edit {
                putString(KEY_LAST_RESET, today)
                putInt(KEY_REMAINING, INITIAL_FREE)
                putInt(KEY_AD_COUNT_TODAY, 0)
                // پاداش نامحدود هم پاک می‌شه
                putLong(KEY_UNLIMITED_UNTIL, 0L)
            }
        }
    }

    /**
     * بررسی وضعیت فعلی
     */
    fun getStatus(): LimitStatus {
        resetIfNewDay()

        val remaining = prefs.getInt(KEY_REMAINING, INITIAL_FREE)
        val adCount = prefs.getInt(KEY_AD_COUNT_TODAY, 0)
        val unlimitedUntil = prefs.getLong(KEY_UNLIMITED_UNTIL, 0L)
        val now = System.currentTimeMillis()
        val isUnlimited = unlimitedUntil > now

        val canAnalyze = isUnlimited || remaining > 0
        val hasReachedBonus = adCount >= ADS_FOR_BONUS
        val adsNeeded = (ADS_FOR_BONUS - adCount).coerceAtLeast(0)

        return LimitStatus(
            remainingFree = remaining,
            adCountToday = adCount,
            isUnlimited = isUnlimited,
            unlimitedUntil = unlimitedUntil,
            canAnalyze = canAnalyze,
            hasReachedBonus = hasReachedBonus,
            adsNeededForBonus = adsNeeded
        )
    }

    /**
     * قبل از تحلیل، این رو صدا بزن
     * @return true اگه اجازه داشت
     */
    fun consumeAnalysis(): Boolean {
        resetIfNewDay()

        val status = getStatus()

        // اگه پاداش نامحدود فعاله → اجازه بده
        if (status.isUnlimited) return true

        // اگه سهمیه داره → کم کن
        if (status.remainingFree > 0) {
            prefs.edit {
                putInt(KEY_REMAINING, status.remainingFree - 1)
            }
            return true
        }

        // سهمیه تموم شده
        return false
    }

    /**
     * وقتی کاربر تبلیغ دید، این رو صدا بزن
     * @return true اگه کاربر به پاداش رسید
     */
    fun onAdWatched(): Boolean {
        resetIfNewDay()

        val current = prefs.getInt(KEY_AD_COUNT_TODAY, 0)
        val newCount = current + 1
        val totalAds = prefs.getInt(KEY_TOTAL_ADS_WATCHED, 0) + 1

        prefs.edit {
            putInt(KEY_AD_COUNT_TODAY, newCount)
            putInt(KEY_TOTAL_ADS_WATCHED, totalAds)
        }

        // چک کن آیا به پاداش وفاداری رسید
        if (newCount == ADS_FOR_BONUS) {
            activateBonus()
            return true
        }

        return false
    }

    /**
     * وقتی کاربر به تعداد تبلیغ کافی رسید، ۱۲ ساعت نامحدود بده
     */
    private fun activateBonus() {
        val now = System.currentTimeMillis()
        val bonusEnd = now + (BONUS_HOURS * 60 * 60 * 1000L)
        prefs.edit {
            putLong(KEY_UNLIMITED_UNTIL, bonusEnd)
        }
    }

    /**
     * هر تبلیغ = ۳ تحلیل اضافه (پس از دیدنش)
     * این بعد از تبلیغ صدا زده می‌شه
     */
    fun addAnalysesFromReward() {
        val current = prefs.getInt(KEY_REMAINING, 0)
        prefs.edit {
            putInt(KEY_REMAINING, current + ANALYSES_PER_REWARD)
        }
    }

    /**
     * تعداد تبلیغ‌های امروز
     */
    fun getAdCountToday(): Int {
        resetIfNewDay()
        return prefs.getInt(KEY_AD_COUNT_TODAY, 0)
    }

    /**
     * تعداد تحلیل باقی‌مونده
     */
    fun getRemainingFree(): Int {
        resetIfNewDay()
        return prefs.getInt(KEY_REMAINING, INITIAL_FREE)
    }

    /**
     * چک کن آیا پاداش نامحدود فعاله
     */
    fun isUnlimitedActive(): Boolean {
        val until = prefs.getLong(KEY_UNLIMITED_UNTIL, 0L)
        return until > System.currentTimeMillis()
    }

    /**
     * کل زمانی که کاربر تبلیغ دیده
     */
    fun getTotalAdsWatched(): Int {
        return prefs.getInt(KEY_TOTAL_ADS_WATCHED, 0)
    }

    /**
     * برای دیباگ — همه چی رو ریست کن
     */
    fun resetAll() {
        prefs.edit { clear() }
    }

    private fun getTodayKey(): String {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        return "$year-$month-$day"
    }
}