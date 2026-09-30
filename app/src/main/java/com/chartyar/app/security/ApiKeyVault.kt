package com.chartyar.app.security

import android.util.Base64
import java.nio.charset.StandardCharsets

/**
 * نگه‌داری امن کلیدهای API
 * کلیدها به‌صورت تکه‌تکه ذخیره می‌شن و در runtime مونتاژ می‌شن
 */
object ApiKeyVault {

    // تکه‌های رمزنگاری‌شده کلیدها (Base64)
    // در آینده اگه API Key اضافه شد، اینا رو پر می‌کنیم
    private val pieces = mapOf(
        "binance" to listOf(
            "cGFydF8xXw==",
            "cGFydF8yXw=="
        ),
        "coingecko" to listOf(
            "Y29pbl8xXw==",
            "Y29pbl8yXw=="
        )
    )

    /**
     * دریافت کلید مونتاژشده
     */
    fun getKey(name: String): String {
        val parts = pieces[name] ?: return ""
        return try {
            parts.joinToString("") { part ->
                String(Base64.decode(part, Base64.NO_WRAP), StandardCharsets.UTF_8)
            }
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * چک وجود کلید
     */
    fun hasKey(name: String): Boolean {
        return pieces.containsKey(name)
    }
}