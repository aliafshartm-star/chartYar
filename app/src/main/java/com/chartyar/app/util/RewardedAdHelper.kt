package com.chartyar.app.util

import android.app.Activity
import android.util.Log
import ir.tapsell.mediation.AdStateListener
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.show.AdShowCompletionState

object RewardedAdHelper {

    private const val TAG = "RewardedAdHelper"

    private const val REWARDED_ZONE_ID = "6ac034978e1ca215ec56dc0f"

    @Volatile
    private var loadedAdId: String? = null

    @Volatile
    private var isLoading: Boolean = false

    fun loadRewardedAd(
        onLoaded: () -> Unit = {},
        onFailed: (String) -> Unit = {}
    ) {
        if (loadedAdId != null) {
            onLoaded()
            return
        }

        if (isLoading) {
            return
        }

        isLoading = true
        Log.d(TAG, "Requesting rewarded ad...")

        Tapsell.requestRewardedAd(
            REWARDED_ZONE_ID,
            object : RequestResultListener {
                override fun onSuccess(adId: String) {
                    Log.d(TAG, "Rewarded ad loaded successfully: $adId")
                    loadedAdId = adId
                    isLoading = false
                    onLoaded()
                }

                override fun onFailure(message: String) {
                    Log.e(TAG, "Failed to load rewarded ad: $message")
                    loadedAdId = null
                    isLoading = false
                    onFailed(message)
                }
            }
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onRewarded: () -> Unit = {},
        onClosed: () -> Unit = {},
        onFailed: (String) -> Unit = {}
    ) {
        val adId = loadedAdId

        if (adId == null) {
            Log.e(TAG, "No ad loaded. Call loadRewardedAd() first.")
            onFailed("No ad loaded")
            return
        }

        Log.d(TAG, "Showing rewarded ad: $adId")

        Tapsell.showRewardedAd(
            adId,
            activity,
            object : AdStateListener.Rewarded {
                override fun onAdImpression() {
                    Log.d(TAG, "Ad impression recorded")
                }

                override fun onAdClicked() {
                    Log.d(TAG, "Ad clicked")
                }

                override fun onAdClosed(completionState: AdShowCompletionState) {
                    Log.d(TAG, "Ad closed with state: $completionState")
                    loadedAdId = null
                    onClosed()
                }

                override fun onRewarded() {
                    Log.d(TAG, "User earned reward!")
                    onRewarded()
                }

                override fun onAdFailed(message: String) {
                    Log.e(TAG, "Ad show failed: $message")
                    loadedAdId = null
                    onFailed(message)
                }
            }
        )
    }

    fun isAdReady(): Boolean = loadedAdId != null

    fun clear() {
        loadedAdId = null
        isLoading = false
    }
}