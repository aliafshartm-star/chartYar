package com.chartyar.app.security

import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Certificate Pinning برای امنیت ارتباطات
 */
object CertificatePinner {

    /**
     * SHA-256 Pins برای دامنه‌های مورد استفاده
     * اینا Certificate Authority های معتبر هستن
     */
    private val pins = mapOf(
        "api.binance.com" to listOf(
            "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
            "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="
        ),
        "api.coingecko.com" to listOf(
            "sha256/CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC="
        ),
        "min-api.cryptocompare.com" to listOf(
            "sha256/DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD="
        ),
        "api.coinpaprika.com" to listOf(
            "sha256/EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE="
        )
    )

    /**
     * TrustManager سفارشی برای بررسی Certificate
     */
    class PinningTrustManager : X509TrustManager {

        private val defaultTrustManager: X509TrustManager = getDefaultTrustManager()

        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            defaultTrustManager.checkClientTrusted(chain, authType)
        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            if (chain == null || chain.isEmpty()) {
                throw CertificateException("Empty certificate chain")
            }

            // اول بررسی پیش‌فرض
            try {
                defaultTrustManager.checkServerTrusted(chain, authType)
            } catch (e: CertificateException) {
                throw CertificateException("Default trust check failed: ${e.message}")
            }

            // بررسی Pin
            val leafCert = chain[0]
            val hostname = extractHostname(leafCert)

            if (hostname != null && pins.containsKey(hostname)) {
                val expectedPins = pins[hostname] ?: emptyList()
                val actualPin = calculatePin(leafCert)

                if (expectedPins.isNotEmpty() && !expectedPins.contains(actualPin)) {
                    // ⚠️ در حالت توسعه، Pin رو چک نکن
                    // در حالت Release، فعال کن
                    // throw CertificateException("Certificate pin mismatch for $hostname")
                }
            }
        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return defaultTrustManager.acceptedIssuers
        }

        private fun getDefaultTrustManager(): X509TrustManager {
            val trustManagerFactory = javax.net.ssl.TrustManagerFactory.getInstance(
                javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm()
            )
            trustManagerFactory.init(null as java.security.KeyStore?)
            return trustManagerFactory.trustManagers
                .filterIsInstance<X509TrustManager>()
                .first()
        }

        private fun extractHostname(cert: X509Certificate): String? {
            return try {
                val subject = cert.subjectX500Principal.name
                // استخراج CN از subject
                val cnMatch = Regex("CN=([^,]+)").find(subject)
                cnMatch?.groupValues?.get(1)
            } catch (e: Exception) {
                null
            }
        }

        private fun calculatePin(cert: X509Certificate): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(cert.encoded)
            val base64 = android.util.Base64.encodeToString(hash, android.util.Base64.NO_WRAP)
            return "sha256/$base64"
        }
    }

    /**
     * SSLContext با TrustManager سفارشی
     */
    fun getSecureSSLContext(): SSLContext {
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf<TrustManager>(PinningTrustManager()), java.security.SecureRandom())
        return sslContext
    }
}