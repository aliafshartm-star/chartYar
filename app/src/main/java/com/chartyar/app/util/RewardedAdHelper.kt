package com.chartyar.app.util

import android.app.Activity
import java.lang.reflect.Proxy

/**
 * Helper برای نمایش تبلیغ جایزه‌ای Tapsell
 * از Reflection استفاده می‌کنه تا با نسخه‌های مختلف سازگار باشه
 */
object RewardedAdHelper {

    /**
     * درخواست و نمایش تبلیغ جایزه‌ای
     * @param onReward وقتی کاربر تبلیغ رو کامل دید صدا زده می‌شه
     * @param onFailed وقتی تبلیغ لود نشد یا خطا خورد
     */
    fun showRewardedAd(
        activity: Activity,
        zoneId: String,
        onReward: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        try {
            val listenerClass = Class.forName(
                "ir.tapsell.mediation.ad.request.RequestResultListener"
            )

            val listener = Proxy.newProxyInstance(
                listenerClass.classLoader,
                arrayOf(listenerClass)
            ) { _, method, args ->
                when (method.name) {
                    "onSuccess" -> {
                        val adId = args?.getOrNull(0) as? String
                        if (adId != null && adId.isNotEmpty()) {
                            tryShowRewarded(activity, adId, onReward, onFailed)
                        } else {
                            onFailed("شناسه تبلیغ خالی است")
                        }
                    }
                    "onFailure" -> {
                        val msg = args?.getOrNull(0) as? String ?: "خطا در دریافت تبلیغ"
                        onFailed(msg)
                    }
                }
                null
            }

            val tapsellClass = Class.forName("ir.tapsell.mediation.Tapsell")

            // تلاش با متد requestRewardedVideoAd
            val method = try {
                tapsellClass.getMethod(
                    "requestRewardedVideoAd",
                    String::class.java,
                    Activity::class.java,
                    listenerClass
                )
            } catch (e: NoSuchMethodException) {
                // fallback: requestRewardedAd
                try {
                    tapsellClass.getMethod(
                        "requestRewardedAd",
                        String::class.java,
                        Activity::class.java,
                        listenerClass
                    )
                } catch (e2: NoSuchMethodException) {
                    null
                }
            }

            if (method != null) {
                method.invoke(null, zoneId, activity, listener)
            } else {
                onFailed("متد درخواست تبلیغ جایزه‌ای پیدا نشد")
            }
        } catch (e: Exception) {
            onFailed(e.message ?: "خطای نامشخص")
        }
    }

    private fun tryShowRewarded(
        activity: Activity,
        adId: String,
        onReward: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        try {
            val showListenerClass = Class.forName(
                "ir.tapsell.mediation.ad.show.AdShowListener"
            )

            val showListener = Proxy.newProxyInstance(
                showListenerClass.classLoader,
                arrayOf(showListenerClass)
            ) { _, method, args ->
                when (method.name) {
                    "onAdClosed" -> {
                        val result = args?.getOrNull(0)
                        val rewarded = extractRewarded(result)
                        if (rewarded) {
                            onReward()
                        } else {
                            onFailed("تبلیغ کامل دیده نشد")
                        }
                    }
                    "onAdFailed" -> {
                        val err = args?.getOrNull(0)
                        val msg = err?.toString() ?: "خطا در نمایش تبلیغ"
                        onFailed(msg)
                    }
                }
                null
            }

            // روش ۱: از کلاس RewardedVideoAd
            val rewardedVideoAdClass = try {
                Class.forName("ir.tapsell.mediation.ad.views.rewarded.RewardedVideoAd")
            } catch (e: ClassNotFoundException) {
                null
            }

            if (rewardedVideoAdClass != null) {
                val constructor = rewardedVideoAdClass.getConstructor(Activity::class.java)
                val instance = constructor.newInstance(activity)
                val showMethod = rewardedVideoAdClass.getMethod(
                    "show",
                    String::class.java,
                    showListenerClass
                )
                showMethod.invoke(instance, adId, showListener)
                return
            }

            // روش ۲: از Tapsell مستقیم
            val tapsellClass = Class.forName("ir.tapsell.mediation.Tapsell")
            val showMethod = try {
                tapsellClass.getMethod(
                    "showRewardedVideoAd",
                    String::class.java,
                    Activity::class.java,
                    showListenerClass
                )
            } catch (e: NoSuchMethodException) {
                null
            }

            if (showMethod != null) {
                showMethod.invoke(null, adId, activity, showListener)
            } else {
                onFailed("متد نمایش تبلیغ پیدا نشد")
            }
        } catch (e: Exception) {
            onFailed(e.message ?: "خطا در نمایش تبلیغ")
        }
    }

    private fun extractRewarded(result: Any?): Boolean {
        if (result == null) return false
        return try {
            val method = result.javaClass.getMethod("isRewarded")
            method.invoke(result) as? Boolean ?: false
        } catch (e: Exception) {
            try {
                val method = result.javaClass.getMethod("getRewarded")
                method.invoke(result) as? Boolean ?: false
            } catch (e2: Exception) {
                // اگه متد پیدا نشد، فرض کن تبلیغ موفق بوده
                true
            }
        }
    }
}