package com.chartyar.app.data

import android.content.Context

/**
 * مدیریت قوانین و شرایط
 */
class LegalManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("legal_prefs", Context.MODE_PRIVATE)

    companion object {
        const val CURRENT_TERMS_VERSION = 1
        const val CURRENT_PRIVACY_VERSION = 1
        const val MIN_AGE = 18

        private const val KEY_TERMS_VERSION = "terms_version"
        private const val KEY_PRIVACY_VERSION = "privacy_version"
        private const val KEY_AGE_CONFIRMED = "age_confirmed"
    }

    /**
     * چک آیا کاربر شرایط جدید رو دیده
     */
    fun hasAcceptedLatestTerms(): Boolean {
        val accepted = prefs.getInt(KEY_TERMS_VERSION, 0)
        return accepted >= CURRENT_TERMS_VERSION
    }

    /**
     * ثبت پذیرش شرایط
     */
    fun acceptTerms() {
        prefs.edit().putInt(KEY_TERMS_VERSION, CURRENT_TERMS_VERSION).apply()
    }

    /**
     * چک آیا کاربر حریم خصوصی جدید رو دیده
     */
    fun hasAcceptedLatestPrivacy(): Boolean {
        val accepted = prefs.getInt(KEY_PRIVACY_VERSION, 0)
        return accepted >= CURRENT_PRIVACY_VERSION
    }

    /**
     * ثبت پذیرش حریم خصوصی
     */
    fun acceptPrivacy() {
        prefs.edit().putInt(KEY_PRIVACY_VERSION, CURRENT_PRIVACY_VERSION).apply()
    }

    /**
     * چک آیا کاربر سن رو تأیید کرده
     */
    fun hasConfirmedAge(): Boolean {
        return prefs.getBoolean(KEY_AGE_CONFIRMED, false)
    }

    /**
     * ثبت تأیید سن
     */
    fun confirmAge() {
        prefs.edit().putBoolean(KEY_AGE_CONFIRMED, true).apply()
    }

    /**
     * ریست
     */
    fun reset() {
        prefs.edit().clear().apply()
    }
}