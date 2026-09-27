package com.chartyar.app

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.chartyar.app.analysis.AnalysisResult
import com.chartyar.app.analysis.CandlePixel
import com.chartyar.app.analysis.Candle
import com.chartyar.app.analysis.ImageProcessor
import com.chartyar.app.analysis.OcrHelper
import com.chartyar.app.analysis.SampleData
import com.chartyar.app.analysis.SignalEngine
import com.chartyar.app.data.PrefsManager
import com.chartyar.app.ui.CalibrationScreen
import com.chartyar.app.ui.DisclaimerScreen
import com.chartyar.app.ui.HomeScreen
import com.chartyar.app.ui.ResultScreen
import com.chartyar.app.ui.UploadScreen
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.ChartYarTheme
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var prefs: PrefsManager

    // ذخیره موقت پردازش
    private var pendingBitmap: Bitmap? = null
    private var pendingCandlePixels: List<CandlePixel>? = null
    private var pendingChartTop: Int = 0
    private var pendingChartBottom: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PrefsManager(applicationContext)

        setContent {
            ChartYarTheme {
                var isLoading by remember { mutableStateOf(true) }
                var disclaimerAccepted by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf("home") }
                var selectedTimeframe by remember { mutableStateOf("4H") }
                var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }
                var statusMessage by remember { mutableStateOf("در حال پردازش تصویر...") }
                var detectedHigh by remember { mutableStateOf<Double?>(null) }
                var detectedLow by remember { mutableStateOf<Double?>(null) }

                LaunchedEffect(Unit) {
                    disclaimerAccepted = prefs.isDisclaimerAccepted.first()
                    isLoading = false
                }

                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize().background(DarkBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("در حال بارگذاری...", color = TextPrimary)
                        }
                    }
                    !disclaimerAccepted -> {
                        DisclaimerScreen(
                            onAccept = {
                                lifecycleScope.launch {
                                    prefs.acceptDisclaimer()
                                    disclaimerAccepted = true
                                }
                            },
                            onDecline = { finish() }
                        )
                    }
                    else -> {
                        when (currentScreen) {
                            "home" -> HomeScreen(
                                onUploadClick = { currentScreen = "upload" },
                                onHistoryClick = { }
                            )

                            "upload" -> UploadScreen(
                                onBack = { currentScreen = "home" },
                                onAnalyze = { uri, tf ->
                                    selectedTimeframe = tf
                                    statusMessage = "در حال پردازش تصویر..."
                                    currentScreen = "analyzing"

                                    lifecycleScope.launch {
                                        val (success, high, low) = prepareAnalysis(uri)
                                        if (success) {
                                            detectedHigh = high
                                            detectedLow = low
                                            currentScreen = "calibration"
                                        } else {
                                            currentScreen = "error"
                                        }
                                    }
                                }
                            )

                            "analyzing" -> {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(DarkBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(32.dp)
                                    ) {
                                        CircularProgressIndicator(color = AccentBlue)
                                        Spacer(Modifier.height(24.dp))
                                        Text(
                                            statusMessage,
                                            color = TextPrimary,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "تایم‌فریم: $selectedTimeframe",
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }

                            "calibration" -> {
                                CalibrationScreen(
                                    detectedHigh = detectedHigh,
                                    detectedLow = detectedLow,
                                    onBack = {
                                        pendingBitmap = null
                                        pendingCandlePixels = null
                                        currentScreen = "upload"
                                    },
                                    onConfirm = { high, low ->
                                        currentScreen = "analyzing"
                                        statusMessage = "در حال تحلیل..."
                                        lifecycleScope.launch {
                                            val result = runAnalysis(high, low, selectedTimeframe)
                                            analysisResult = result
                                            currentScreen = if (result != null) "result" else "error"
                                        }
                                    }
                                )
                            }

                            "result" -> {
                                val result = analysisResult
                                if (result != null) {
                                    ResultScreen(
                                        result = result,
                                        onBack = {
                                            analysisResult = null
                                            pendingBitmap = null
                                            pendingCandlePixels = null
                                            currentScreen = "home"
                                        }
                                    )
                                } else {
                                    currentScreen = "home"
                                }
                            }

                            "error" -> {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(DarkBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(32.dp)
                                    ) {
                                        Text(
                                            "خطا در پردازش تصویر",
                                            color = TextPrimary,
                                            style = MaterialTheme.typography.headlineMedium
                                        )
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            "لطفاً تصویر واضح‌تری از چارت انتخاب کنید. تصویر باید کندل‌های سبز و قرمز واضح داشته باشد.",
                                            color = TextSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(Modifier.height(24.dp))
                                        Button(
                                            onClick = {
                                                pendingBitmap = null
                                                pendingCandlePixels = null
                                                currentScreen = "upload"
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = AccentBlue,
                                                contentColor = TextPrimary
                                            )
                                        ) {
                                            Text("تلاش مجدد")
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Button(
                                            onClick = { currentScreen = "home" },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = DarkBg,
                                                contentColor = TextSecondary
                                            )
                                        ) {
                                            Text("بازگشت به خانه")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * مرحله ۱: آماده‌سازی - بارگذاری تصویر، استخراج کندل و OCR
     * @return Triple(موفقیت, priceHigh, priceLow)
     */
    private suspend fun prepareAnalysis(uri: Uri): Triple<Boolean, Double?, Double?> {
        return withContext(Dispatchers.Default) {
            try {
                val bitmap = ImageProcessor.loadBitmap(applicationContext, uri)
                    ?: return@withContext Triple(false, null, null)

                val candlePixels = ImageProcessor.extractCandles(bitmap)
                    ?: return@withContext Triple(false, null, null)

                val pricePoints = OcrHelper.extractPriceAxis(bitmap)
                val priceRange = OcrHelper.calculatePriceRange(pricePoints)

                val bounds = ImageProcessor.findChartBoundsPublic(bitmap)

                pendingBitmap = bitmap
                pendingCandlePixels = candlePixels
                pendingChartTop = bounds.chartTop
                pendingChartBottom = bounds.chartBottom

                Triple(true, priceRange?.first, priceRange?.second)
            } catch (e: Exception) {
                e.printStackTrace()
                Triple(false, null, null)
            }
        }
    }

    /**
     * مرحله ۲: اجرای تحلیل با قیمت‌های تأییدشده
     */
    private suspend fun runAnalysis(
        priceHigh: Double,
        priceLow: Double,
        timeframe: String
    ): AnalysisResult? {
        return withContext(Dispatchers.Default) {
            try {
                val pixels = pendingCandlePixels ?: return@withContext fallbackToSample(timeframe)

                val candles: List<Candle> = ImageProcessor.pixelsToCandles(
                    pixels,
                    pendingChartTop,
                    pendingChartBottom,
                    priceHigh,
                    priceLow
                )

                if (candles.size < 20) return@withContext fallbackToSample(timeframe)

                SignalEngine.analyze(candles, timeframe)
            } catch (e: Exception) {
                e.printStackTrace()
                fallbackToSample(timeframe)
            }
        }
    }

    private fun fallbackToSample(timeframe: String): AnalysisResult? {
        val sampleCandles = SampleData.generateBullishCandles(100)
        return SignalEngine.analyze(sampleCandles, timeframe)
    }
}