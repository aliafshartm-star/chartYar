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
        private const val KEY_UNLOCKED_STRATEGIES = "unlocked_strategies"
        private const val KEY_ADS_SINCE_REWARD = "ads_since_reward"

        const val INITIAL_FREE = 3
        const val ADS_FOR_REWARD = 3
        const val ANALYSES_PER_REWARD = 2
        const val ADS_FOR_BONUS = 10
        const val BONUS_HOURS = 3
        const val DEFAULT_STRATEGY = "SWING"
    }

    data class LimitStatus(
        val remainingFree: Int,
        val adCountToday: Int,
        val adsSinceReward: Int,
        val isUnlimited: Boolean,
        val unlimitedUntil: Long,
        val canAnalyze: Boolean,
        val hasReachedBonus: Boolean,
        val adsNeededForBonus: Int,
        val adsNeededForNextReward: Int
    )

    data class AdWatchResult(
        val analysesGiven: Int,
        val bonusActivated: Boolean,
        val adsUntilNextReward: Int,
        val adsUntilBonus: Int
    )

    init {
        resetIfNewDay()
    }

    fun resetIfNewDay() {
        val today = getTodayKey()
        val lastReset = prefs.getString(KEY_LAST_RESET, null)

        if (lastReset != today) {
            prefs.edit {
                putString(KEY_LAST_RESET, today)
                putInt(KEY_REMAINING, INITIAL_FREE)
                putInt(KEY_AD_COUNT_TODAY, 0)
                putInt(KEY_ADS_SINCE_REWARD, 0)
                putLong(KEY_UNLIMITED_UNTIL, 0L)
                putStringSet(KEY_UNLOCKED_STRATEGIES, emptySet())
            }
        }
    }

    fun getStatus(): LimitStatus {
        resetIfNewDay()

        val remaining = prefs.getInt(KEY_REMAINING, INITIAL_FREE)
        val adCount = prefs.getInt(KEY_AD_COUNT_TODAY, 0)
        val adsSinceReward = prefs.getInt(KEY_ADS_SINCE_REWARD, 0)
        val unlimitedUntil = prefs.getLong(KEY_UNLIMITED_UNTIL, 0L)
        val now = System.currentTimeMillis()
        val isUnlimited = unlimitedUntil > now

        val canAnalyze = isUnlimited || remaining > 0
        val hasReachedBonus = adCount >= ADS_FOR_BONUS
        val adsNeededBonus = (ADS_FOR_BONUS - adCount).coerceAtLeast(0)
        val adsNeededReward = (ADS_FOR_REWARD - adsSinceReward).coerceAtLeast(0)

        return LimitStatus(
            remainingFree = remaining,
            adCountToday = adCount,
            adsSinceReward = adsSinceReward,
            isUnlimited = isUnlimited,
            unlimitedUntil = unlimitedUntil,
            canAnalyze = canAnalyze,
            hasReachedBonus = hasReachedBonus,
            adsNeededForBonus = adsNeededBonus,
            adsNeededForNextReward = adsNeededReward
        )
    }

    fun consumeAnalysis(): Boolean {
        resetIfNewDay()

        val status = getStatus()

        if (status.isUnlimited) return true

        if (status.remainingFree > 0) {
            prefs.edit {
                putInt(KEY_REMAINING, status.remainingFree - 1)
            }
            return true
        }

        return false
    }

    fun onAdWatched(): AdWatchResult {
        resetIfNewDay()

        val adsSinceRewardOld = prefs.getInt(KEY_ADS_SINCE_REWARD, 0)
        val adsSinceRewardNew = adsSinceRewardOld + 1
        val totalAdsOld = prefs.getInt(KEY_AD_COUNT_TODAY, 0)
        val totalAdsNew = totalAdsOld + 1
        val grandTotalAds = prefs.getInt(KEY_TOTAL_ADS_WATCHED, 0) + 1

        var analysesGiven = 0
        var bonusActivated = false

        if (adsSinceRewardNew >= ADS_FOR_REWARD) {
            val current = prefs.getInt(KEY_REMAINING, 0)
            analysesGiven = ANALYSES_PER_REWARD

            prefs.edit {
                putInt(KEY_REMAINING, current + ANALYSES_PER_REWARD)
                putInt(KEY_ADS_SINCE_REWARD, 0)
                putInt(KEY_AD_COUNT_TODAY, totalAdsNew)
                putInt(KEY_TOTAL_ADS_WATCHED, grandTotalAds)
            }
        } else {
            prefs.edit {
                putInt(KEY_ADS_SINCE_REWARD, adsSinceRewardNew)
                putInt(KEY_AD_COUNT_TODAY, totalAdsNew)
                putInt(KEY_TOTAL_ADS_WATCHED, grandTotalAds)
            }
        }

        if (totalAdsNew >= ADS_FOR_BONUS) {
            activateBonus()
            bonusActivated = true
        }

        val adsUntilNext = if (analysesGiven > 0) ADS_FOR_REWARD else ADS_FOR_REWARD - adsSinceRewardNew
        val adsUntilBonus = (ADS_FOR_BONUS - totalAdsNew).coerceAtLeast(0)

        return AdWatchResult(
            analysesGiven = analysesGiven,
            bonusActivated = bonusActivated,
            adsUntilNextReward = adsUntilNext,
            adsUntilBonus = adsUntilBonus
        )
    }

    fun isStrategyUnlocked(strategyType: String): Boolean {
        resetIfNewDay()

        if (strategyType == DEFAULT_STRATEGY) return true

        val set = prefs.getStringSet(KEY_UNLOCKED_STRATEGIES, emptySet()) ?: emptySet()
        return set.contains(strategyType)
    }

    fun unlockStrategy(strategyType: String) {
        resetIfNewDay()

        if (strategyType == DEFAULT_STRATEGY) return

        val current = prefs.getStringSet(KEY_UNLOCKED_STRATEGIES, emptySet()) ?: emptySet()
        val newSet = current + strategyType
        prefs.edit { putStringSet(KEY_UNLOCKED_STRATEGIES, newSet) }
    }

    fun getUnlockedStrategies(): Set<String> {
        resetIfNewDay()
        val set = prefs.getStringSet(KEY_UNLOCKED_STRATEGIES, emptySet()) ?: emptySet()
        return set + DEFAULT_STRATEGY
    }

    private fun activateBonus() {
        val now = System.currentTimeMillis()
        val bonusEnd = now + (BONUS_HOURS * 60 * 60 * 1000L)
        prefs.edit {
            putLong(KEY_UNLIMITED_UNTIL, bonusEnd)
        }
    }

    fun getRemainingFree(): Int {
        resetIfNewDay()
        return prefs.getInt(KEY_REMAINING, INITIAL_FREE)
    }

    fun getAdCountToday(): Int {
        resetIfNewDay()
        return prefs.getInt(KEY_AD_COUNT_TODAY, 0)
    }

    fun getAdsSinceReward(): Int {
        resetIfNewDay()
        return prefs.getInt(KEY_ADS_SINCE_REWARD, 0)
    }

    fun isUnlimitedActive(): Boolean {
        val until = prefs.getLong(KEY_UNLIMITED_UNTIL, 0L)
        return until > System.currentTimeMillis()
    }

    fun getUnlimitedUntil(): Long {
        return prefs.getLong(KEY_UNLIMITED_UNTIL, 0L)
    }

    fun getTotalAdsWatched(): Int {
        return prefs.getInt(KEY_TOTAL_ADS_WATCHED, 0)
    }

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