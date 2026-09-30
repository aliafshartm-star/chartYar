package com.chartyar.app.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * ذخیره امن داده‌ها با AES-256
 */
object SecureStorage {

    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "chartyar_secure_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val GCM_IV_LENGTH = 12

    /**
     * ساخت یا دریافت کلید از Keystore
     */
    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER)
        keyStore.load(null)

        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * رمزنگاری متن
     */
    fun encrypt(plainText: String): String? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())

            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * رمزگشایی متن
     */
    fun decrypt(encryptedText: String): String? {
        return try {
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)

            val iv = ByteArray(GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)

            val encryptedBytes = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), spec)

            String(cipher.doFinal(encryptedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * ذخیره امن در SharedPreferences
     */
    fun saveSecure(context: Context, key: String, value: String) {
        try {
            val encrypted = encrypt(value) ?: return
            val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString(key, encrypted).apply()
        } catch (e: Exception) {
            // silent
        }
    }

    /**
     * خواندن امن از SharedPreferences
     */
    fun readSecure(context: Context, key: String): String? {
        return try {
            val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
            val encrypted = prefs.getString(key, null) ?: return null
            decrypt(encrypted)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * حذف مقدار امن
     */
    fun removeSecure(context: Context, key: String) {
        try {
            val prefs = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
            prefs.edit().remove(key).apply()
        } catch (e: Exception) {
            // silent
        }
    }
}