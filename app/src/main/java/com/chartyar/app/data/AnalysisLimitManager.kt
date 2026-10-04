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
        private const val KEY_ADS_SINCE_REWARD = "ads_since_reward"
        private const val KEY_TOTAL_ADS_WATCHED = "total_ads_watched"
        private const val KEY_UNLOCKED_STRATEGIES = "unlocked_strategies"

        const val INITIAL_FREE = 3
        const val ADS_PER_REWARD = 5
        const val ANALYSES_PER_REWARD = 3
        const val DEFAULT_STRATEGY = "SWING"

        // برای سازگاری با کدهای قدیمی — معادل ADS_PER_REWARD
        const val ADS_FOR_REWARD = 5
        const val ADS_FOR_BONUS = 5
    }

    data class LimitStatus(
        val remainingFree: Int,
        val adsSinceReward: Int,
        val canAnalyze: Boolean,
        val adsNeededForNextReward: Int
    )

    data class AdWatchResult(
        val analysesGiven: Int,
        val adsUntilNextReward: Int
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
                putInt(KEY_ADS_SINCE_REWARD, 0)
                putStringSet(KEY_UNLOCKED_STRATEGIES, emptySet())
            }
        }
    }

    fun getStatus(): LimitStatus {
        resetIfNewDay()
        val remaining = prefs.getInt(KEY_REMAINING, INITIAL_FREE)
        val adsSinceReward = prefs.getInt(KEY_ADS_SINCE_REWARD, 0)
        val canAnalyze = remaining > 0
        val adsNeeded = (ADS_PER_REWARD - adsSinceReward).coerceAtLeast(0)
        return LimitStatus(
            remainingFree = remaining,
            adsSinceReward = adsSinceReward,
            canAnalyze = canAnalyze,
            adsNeededForNextReward = adsNeeded
        )
    }

    fun consumeAnalysis(): Boolean {
        resetIfNewDay()
        val status = getStatus()
        if (status.remainingFree > 0) {
            prefs.edit { putInt(KEY_REMAINING, status.remainingFree - 1) }
            return true
        }
        return false
    }

    fun onAdWatched(): AdWatchResult {
        resetIfNewDay()

        val adsSinceRewardOld = prefs.getInt(KEY_ADS_SINCE_REWARD, 0)
        val adsSinceRewardNew = adsSinceRewardOld + 1
        val grandTotalAds = prefs.getInt(KEY_TOTAL_ADS_WATCHED, 0) + 1

        var analysesGiven = 0
        val newAdsSinceReward: Int

        if (adsSinceRewardNew >= ADS_PER_REWARD) {
            val current = prefs.getInt(KEY_REMAINING, 0)
            analysesGiven = ANALYSES_PER_REWARD
            newAdsSinceReward = 0
            prefs.edit {
                putInt(KEY_REMAINING, current + ANALYSES_PER_REWARD)
                putInt(KEY_ADS_SINCE_REWARD, 0)
                putInt(KEY_TOTAL_ADS_WATCHED, grandTotalAds)
            }
        } else {
            newAdsSinceReward = adsSinceRewardNew
            prefs.edit {
                putInt(KEY_ADS_SINCE_REWARD, adsSinceRewardNew)
                putInt(KEY_TOTAL_ADS_WATCHED, grandTotalAds)
            }
        }

        val adsUntilNext = if (analysesGiven > 0) ADS_PER_REWARD
        else ADS_PER_REWARD - newAdsSinceReward

        return AdWatchResult(
            analysesGiven = analysesGiven,
            adsUntilNextReward = adsUntilNext
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

    fun getRemainingFree(): Int {
        resetIfNewDay()
        return prefs.getInt(KEY_REMAINING, INITIAL_FREE)
    }

    fun getAdsSinceReward(): Int {
        resetIfNewDay()
        return prefs.getInt(KEY_ADS_SINCE_REWARD, 0)
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