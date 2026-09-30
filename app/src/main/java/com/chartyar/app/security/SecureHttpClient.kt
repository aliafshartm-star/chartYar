package com.chartyar.app.security

import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object SecureHttpClient {

    private const val DEFAULT_TIMEOUT = 15000

    fun createConnection(urlString: String, timeoutMs: Int = DEFAULT_TIMEOUT): HttpURLConnection? {
        return try {
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            if (connection is HttpsURLConnection) {
                connection.sslSocketFactory = CertificatePinner.getSecureSSLContext().socketFactory
            }

            connection.requestMethod = "GET"
            connection.connectTimeout = timeoutMs
            connection.readTimeout = timeoutMs
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "ChartYar/1.0")

            connection
        } catch (e: Exception) {
            null
        }
    }

    fun get(urlString: String, timeoutMs: Int = DEFAULT_TIMEOUT): String? {
        return try {
            val connection = createConnection(urlString, timeoutMs) ?: return null
            val responseCode = connection.responseCode

            val response = if (responseCode == 200) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }

            connection.disconnect()
            response
        } catch (e: Exception) {
            null
        }
    }
}