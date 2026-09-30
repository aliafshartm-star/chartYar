package com.chartyar.app.security

import android.content.Context

/**
 * مدیریت تنظیمات با رمزنگاری
 * از SecureStorage برای رمزنگاری مقادیر استفاده می‌کنه
 */
class EncryptedPrefs(private val context: Context) {

    private val prefs = context.getSharedPreferences("encrypted_prefs", Context.MODE_PRIVATE)

    fun putString(key: String, value: String) {
        val encrypted = SecureStorage.encrypt(value)
        if (encrypted != null) {
            prefs.edit().putString(key, encrypted).apply()
        }
    }

    fun getString(key: String, defaultValue: String = ""): String {
        val encrypted = prefs.getString(key, null) ?: return defaultValue
        return SecureStorage.decrypt(encrypted) ?: defaultValue
    }

    fun putBoolean(key: String, value: Boolean) {
        putString(key, value.toString())
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return getString(key, defaultValue.toString()).toBoolean()
    }

    fun putInt(key: String, value: Int) {
        putString(key, value.toString())
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return getString(key, defaultValue.toString()).toIntOrNull() ?: defaultValue
    }

    fun putLong(key: String, value: Long) {
        putString(key, value.toString())
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return getString(key, defaultValue.toString()).toLongOrNull() ?: defaultValue
    }

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}