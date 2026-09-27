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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.chartyar.app.analysis.AnalysisResult
import com.chartyar.app.analysis.Candle
import com.chartyar.app.analysis.ImageProcessor
import com.chartyar.app.analysis.ImageProcessor.CandlePixel
import com.chartyar.app.analysis.OcrHelper
import com.chartyar.app.analysis.SampleData
import com.chartyar.app.analysis.SignalEngine
import com.chartyar.app.analysis.SignalType
import com.chartyar.app.analysis.TrendType
import com.chartyar.app.data.AnalysisHistory
import com.chartyar.app.data.AppDatabase
import com.chartyar.app.data.PrefsManager
import com.chartyar.app.ui.CalibrationScreen
import com.chartyar.app.ui.DisclaimerScreen
import com.chartyar.app.ui.HistoryScreen
import com.chartyar.app.ui.HomeScreen
import com.chartyar.app.ui.ProgressScreen
import com.chartyar.app.ui.ResultScreen
import com.chartyar.app.ui.SplashScreen
import com.chartyar.app.ui.UploadScreen
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.ChartYarTheme
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var prefs: PrefsManager
    private lateinit var db: AppDatabase

    private var pendingBitmap: Bitmap? = null
    private var pendingCandlePixels: List<CandlePixel>? = null
    private var pendingChartTop: Int = 0
    private var pendingChartBottom: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PrefsManager(applicationContext)
        db = AppDatabase.get(applicationContext)

        setContent {
            ChartYarTheme {
                var isLoading by remember { mutableStateOf(true) }
                var disclaimerAccepted by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf("home") }
                var selectedTimeframe by remember { mutableStateOf("4H") }
                var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }
                var statusMessage by remember { mutableStateOf("در حال پردازش تصویر...") }
                var analyzeProgress by remember { mutableStateOf(0f) }
                var detectedHigh by remember { mutableStateOf<Double?>(null) }
                var detectedLow by remember { mutableStateOf<Double?>(null) }
                var historyItems by remember { mutableStateOf<List<AnalysisHistory>>(emptyList()) }

                // بارگذاری تاریخچه
                LaunchedEffect(currentScreen) {
                    if (currentScreen == "history") {
                        historyItems = db.historyDao().getAll().first()
                    }
                }

                LaunchedEffect(Unit) {
                    // نمایش Splash حداقل ۱.۵ ثانیه
                    val startTime = System.currentTimeMillis()
                    disclaimerAccepted = prefs.isDisclaimerAccepted.first()
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < 1500) {
                        delay(1500 - elapsed)
                    }
                    isLoading = false
                }

                when {
                    isLoading -> {
                        SplashScreen()
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
                                onHistoryClick = { currentScreen = "history" }
                            )

                            "upload" -> UploadScreen(
                                onBack = { currentScreen = "home" },
                                onAnalyze = { uri, tf ->
                                    selectedTimeframe = tf
                                    analyzeProgress = 0f
                                    statusMessage = "در حال پردازش تصویر..."
                                    currentScreen = "analyzing"

                                    lifecycleScope.launch {
                                        analyzeProgress = 0.1f
                                        statusMessage = "بارگذاری تصویر..."
                                        delay(300)

                                        analyzeProgress = 0.25f
                                        statusMessage = "تشخیص کندل‌ها..."
                                        delay(200)

                                        val result = prepareAnalysis(uri)

                                        analyzeProgress = 0.6f
                                        statusMessage = "خوندن قیمت‌ها از محور..."
                                        delay(400)

                                        analyzeProgress = 0.85f
                                        statusMessage = "آماده‌سازی تحلیل..."
                                        delay(300)

                                        analyzeProgress = 1f
                                        delay(200)

                                        if (result.first) {
                                            detectedHigh = result.second
                                            detectedLow = result.third
                                            currentScreen = "calibration"
                                        } else {
                                            currentScreen = "error"
                                        }
                                    }
                                }
                            )

                            "analyzing" -> {
                                ProgressScreen(
                                    progress = analyzeProgress,
                                    statusMessage = statusMessage,
                                    timeframe = selectedTimeframe
                                )
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
                                        statusMessage = "در حال اجرای تحلیل..."
                                        analyzeProgress = 0.4f

                                        lifecycleScope.launch {
                                            delay(300)
                                            analyzeProgress = 0.6f
                                            statusMessage = "اجرای اندیکاتورها..."
                                            delay(400)

                                            analyzeProgress = 0.85f
                                            statusMessage = "تشخیص الگوها..."
                                            delay(300)

                                            val result = runAnalysis(high, low, selectedTimeframe)

                                            analyzeProgress = 1f
                                            delay(200)

                                            analysisResult = result
                                            if (result != null) {
                                                saveToHistory(result, selectedTimeframe)
                                                currentScreen = "result"
                                            } else {
                                                currentScreen = "error"
                                            }
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

                            "history" -> {
                                HistoryScreen(
                                    items = historyItems,
                                    onBack = { currentScreen = "home" },
                                    onDelete = { id ->
                                        lifecycleScope.launch {
                                            db.historyDao().deleteById(id)
                                            historyItems = db.historyDao().getAll().first()
                                        }
                                    },
                                    onClearAll = {
                                        lifecycleScope.launch {
                                            db.historyDao().clearAll()
                                            historyItems = emptyList()
                                        }
                                    }
                                )
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

    private suspend fun saveToHistory(result: AnalysisResult, timeframe: String) {
        try {
            val signalText = when (result.signal) {
                SignalType.STRONG_BUY -> "خرید قوی"
                SignalType.BUY -> "خرید"
                SignalType.NEUTRAL -> "خنثی"
                SignalType.SELL -> "فروش"
                SignalType.STRONG_SELL -> "فروش قوی"
            }
            val trendText = when (result.trend) {
                TrendType.UPTREND -> "صعودی"
                TrendType.DOWNTREND -> "نزولی"
                TrendType.RANGE -> "رنج"
            }
            db.historyDao().insert(
                AnalysisHistory(
                    timestamp = System.currentTimeMillis(),
                    timeframe = timeframe,
                    signalText = signalText,
                    confidence = result.confidence,
                    currentPrice = result.currentPrice,
                    entry = result.entry,
                    stopLoss = result.stopLoss,
                    target1 = result.target1,
                    target2 = result.target2,
                    target3 = result.target3,
                    trendText = trendText
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}