package com.chartyar.app

import android.Manifest
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.chartyar.app.analysis.*
import com.chartyar.app.analysis.ImageProcessor.CandlePixel
import com.chartyar.app.data.*
import com.chartyar.app.ui.*
import com.chartyar.app.ui.theme.*
import com.chartyar.app.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class PreparationResult(
    val success: Boolean,
    val high: Double? = null,
    val low: Double? = null,
    val currentPrice: Double? = null,
    val confidence: Int = 0,
    val bitmap: Bitmap? = null,
    val candlePixels: List<CandlePixel>? = null,
    val chartTop: Int = 0,
    val chartBottom: Int = 0
)

class MainActivity : ComponentActivity() {
    private lateinit var prefs: PrefsManager
    private lateinit var db: AppDatabase
    private lateinit var cache: CacheManager
    private var pendingBitmap: Bitmap? = null
    private var pendingCandlePixels: List<CandlePixel>? = null
    private var pendingChartTop: Int = 0
    private var pendingChartBottom: Int = 0
    private var pendingUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PrefsManager(applicationContext)
        db = AppDatabase.get(applicationContext)
        cache = CacheManager(applicationContext)

        NotificationHelper.createChannel(applicationContext)
        LocalAnalytics.logEvent(applicationContext, AnalyticsEvents.APP_OPEN)

        setContent {
            val themeFlow = remember { prefs.theme }
            val currentTheme by themeFlow.collectAsState(initial = "dark")
            val languageFlow = remember { prefs.language }
            val currentLanguage by languageFlow.collectAsState(initial = "fa")
            val isDark = currentTheme == "dark"

            ChartYarTheme(isDark = isDark) {
                var isLoading by remember { mutableStateOf(true) }
                var disclaimerAccepted by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf("home") }
                var selectedTimeframe by remember { mutableStateOf("4H") }
                var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }
                var statusMessage by remember { mutableStateOf("در حال پردازش...") }
                var analyzeProgress by remember { mutableStateOf(0f) }
                var errorMessage by remember { mutableStateOf("") }
                var detectedHigh by remember { mutableStateOf<Double?>(null) }
                var detectedLow by remember { mutableStateOf<Double?>(null) }
                var detectedCurrentPrice by remember { mutableStateOf<Double?>(null) }
                var detectedConfidence by remember { mutableStateOf(0) }
                var historyItems by remember { mutableStateOf<List<AnalysisHistory>>(emptyList()) }
                var alertItems by remember { mutableStateOf<List<PriceAlert>>(emptyList()) }
                var autoSaveHistory by remember { mutableStateOf(true) }
                var showWarnings by remember { mutableStateOf(true) }
                var defaultTimeframe by remember { mutableStateOf("4H") }
                var themeSetting by remember { mutableStateOf("dark") }
                var languageSetting by remember { mutableStateOf("fa") }
                var tutorialSeen by remember { mutableStateOf(true) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                LaunchedEffect(Unit) {
                    autoSaveHistory = prefs.autoSaveHistory.first()
                    showWarnings = prefs.showWarnings.first()
                    defaultTimeframe = prefs.defaultTimeframe.first()
                    themeSetting = prefs.theme.first()
                    languageSetting = prefs.language.first()
                    tutorialSeen = prefs.isTutorialSeen.first()
                    selectedTimeframe = defaultTimeframe
                    AlertChecker.checkAllAlerts(applicationContext, db, cache)
                }

                LaunchedEffect(currentScreen) {
                    if (currentScreen == "history") {
                        historyItems = db.historyDao().getAll().first()
                    }
                    if (currentScreen == "alerts") {
                        alertItems = db.alertDao().getAll().first()
                    }
                }

                LaunchedEffect(Unit) {
                    val startTime = System.currentTimeMillis()
                    disclaimerAccepted = prefs.isDisclaimerAccepted.first()
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < 1500) delay(1500 - elapsed)
                    isLoading = false
                }

                when {
                    isLoading -> SplashScreen()
                    !disclaimerAccepted -> DisclaimerScreen(
                        onAccept = {
                            lifecycleScope.launch {
                                prefs.acceptDisclaimer()
                                disclaimerAccepted = true
                                if (!tutorialSeen) currentScreen = "tutorial"
                            }
                        },
                        onDecline = { finish() }
                    )
                    currentScreen == "tutorial" -> TutorialScreen(
                        isFirstTime = !tutorialSeen,
                        onFinish = {
                            lifecycleScope.launch {
                                prefs.setTutorialSeen(true)
                                tutorialSeen = true
                                currentScreen = "home"
                            }
                        }
                    )
                    currentScreen == "privacy" -> PrivacyPolicyScreen(
                        onBack = { currentScreen = "settings" }
                    )
                    currentScreen == "terms" -> TermsScreen(
                        onBack = { currentScreen = "settings" }
                    )
                    else -> when (currentScreen) {
                        "home" -> HomeScreen(
                            onUploadClick = { currentScreen = "upload" },
                            onHistoryClick = { currentScreen = "history" },
                            onSettingsClick = { currentScreen = "settings" },
                            onAlertsClick = { currentScreen = "alerts" }
                        )
                        "upload" -> UploadScreen(
                            onBack = { currentScreen = "home" },
                            onAnalyze = { uri, tf ->
                                pendingUri = uri
                                selectedTimeframe = tf
                                currentScreen = "symbol"
                            }
                        )
                        "symbol" -> SymbolSelectorScreen(
                            onBack = { currentScreen = "upload" },
                            onSymbolSelected = { symbol ->
                                analyzeProgress = 0f
                                statusMessage = "در حال دریافت داده..."
                                errorMessage = ""
                                currentScreen = "analyzing"

                                lifecycleScope.launch {
                                    analyzeProgress = 0.3f
                                    statusMessage = "بررسی منابع داده..."

                                    val dataResult = MarketDataService.getCandles(
                                        symbol = symbol,
                                        timeframe = selectedTimeframe,
                                        cache = cache
                                    )

                                    val sourceName = MarketDataService.sourceLabel(dataResult.source)

                                    if (dataResult.candles != null && dataResult.candles.size >= 20) {
                                        analyzeProgress = 0.7f
                                        statusMessage = "داده از $sourceName"
                                        delay(300)
                                        analyzeProgress = 0.9f
                                        statusMessage = "اجرای تحلیل..."
                                        val result = withContext(Dispatchers.Default) {
                                            SignalEngine.analyze(dataResult.candles, selectedTimeframe)
                                        }
                                        analyzeProgress = 1f
                                        delay(200)
                                        analysisResult = result
                                        if (result != null) {
                                            if (autoSaveHistory) saveToHistory(result, selectedTimeframe)
                                            currentScreen = "result"
                                        } else {
                                            errorMessage = NetworkError.DATA_EMPTY.message
                                            currentScreen = "error"
                                        }
                                        return@launch
                                    }

                                    statusMessage = "پردازش تصویر..."
                                    analyzeProgress = 0.3f
                                    delay(300)
                                    val uri2 = pendingUri
                                    if (uri2 == null) {
                                        errorMessage = NetworkError.NO_INTERNET.message
                                        currentScreen = "error"
                                        return@launch
                                    }
                                    analyzeProgress = 0.5f
                                    statusMessage = "تشخیص کندل‌ها..."
                                    delay(200)
                                    val prep = prepareAnalysis(uri2)
                                    analyzeProgress = 0.85f
                                    statusMessage = "آماده‌سازی..."
                                    delay(300)
                                    analyzeProgress = 1f
                                    delay(200)
                                    if (prep.success) {
                                        detectedHigh = prep.high
                                        detectedLow = prep.low
                                        detectedCurrentPrice = prep.currentPrice
                                        detectedConfidence = prep.confidence
                                        pendingBitmap = prep.bitmap
                                        pendingCandlePixels = prep.candlePixels
                                        pendingChartTop = prep.chartTop
                                        pendingChartBottom = prep.chartBottom
                                        currentScreen = "calibration"
                                    } else {
                                        errorMessage = "نتوانستیم چارت را تشخیص دهیم."
                                        currentScreen = "error"
                                    }
                                }
                            }
                        )
                        "analyzing" -> ProgressScreen(
                            progress = analyzeProgress,
                            statusMessage = statusMessage,
                            timeframe = selectedTimeframe
                        )
                        "calibration" -> CalibrationScreen(
                            detectedHigh = detectedHigh,
                            detectedLow = detectedLow,
                            detectedCurrentPrice = detectedCurrentPrice,
                            confidence = detectedConfidence,
                            onBack = {
                                pendingBitmap = null
                                pendingCandlePixels = null
                                currentScreen = "symbol"
                            },
                            onConfirm = { high, low ->
                                currentScreen = "analyzing"
                                statusMessage = "در حال اجرای تحلیل..."
                                analyzeProgress = 0.4f
                                lifecycleScope.launch {
                                    delay(300)
                                    analyzeProgress = 0.7f
                                    statusMessage = "اجرای اندیکاتورها..."
                                    delay(300)
                                    analyzeProgress = 1f
                                    delay(200)
                                    val result = runAnalysis(high, low, selectedTimeframe)
                                    analysisResult = result
                                    if (result != null) {
                                        if (autoSaveHistory) saveToHistory(result, selectedTimeframe)
                                        currentScreen = "result"
                                    } else {
                                        errorMessage = "تحلیل با خطا مواجه شد."
                                        currentScreen = "error"
                                    }
                                }
                            }
                        )
                        "result" -> {
                            val result = analysisResult
                            if (result != null) ResultScreen(
                                result = result,
                                onBack = {
                                    analysisResult = null
                                    pendingBitmap = null
                                    pendingCandlePixels = null
                                    currentScreen = "home"
                                }
                            ) else currentScreen = "home"
                        }
                        "history" -> HistoryScreen(
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
                        "alerts" -> AlertScreen(
                            alerts = alertItems,
                            onBack = { currentScreen = "home" },
                            onCreateAlert = { symbol, price, condition, note ->
                                lifecycleScope.launch {
                                    db.alertDao().insert(
                                        AlertHelper.createAlert(symbol, price, condition, note)
                                    )
                                    alertItems = db.alertDao().getAll().first()
                                }
                            },
                            onDeleteAlert = { id ->
                                lifecycleScope.launch {
                                    db.alertDao().deleteById(id)
                                    alertItems = db.alertDao().getAll().first()
                                }
                            },
                            onToggleActive = { id, active ->
                                lifecycleScope.launch {
                                    db.alertDao().setActive(id, active)
                                    alertItems = db.alertDao().getAll().first()
                                }
                            },
                            onResetAlert = { id ->
                                lifecycleScope.launch {
                                    val alert = db.alertDao().getById(id)
                                    if (alert != null) {
                                        db.alertDao().update(
                                            alert.copy(isTriggered = false, triggeredAt = 0L)
                                        )
                                        alertItems = db.alertDao().getAll().first()
                                    }
                                }
                            }
                        )
                        "settings" -> SettingsScreen(
                            autoSaveHistory = autoSaveHistory,
                            showWarnings = showWarnings,
                            defaultTimeframe = defaultTimeframe,
                            theme = themeSetting,
                            language = languageSetting,
                            onBack = { currentScreen = "home" },
                            onShowTutorial = { currentScreen = "tutorial" },
                            onShowPrivacy = { currentScreen = "privacy" },
                            onShowTerms = { currentScreen = "terms" },
                            onAutoSaveChange = { v -> autoSaveHistory = v; lifecycleScope.launch { prefs.setAutoSaveHistory(v) } },
                            onShowWarningsChange = { v -> showWarnings = v; lifecycleScope.launch { prefs.setShowWarnings(v) } },
                            onDefaultTimeframeChange = { v -> defaultTimeframe = v; lifecycleScope.launch { prefs.setDefaultTimeframe(v) } },
                            onThemeChange = { v -> themeSetting = v; lifecycleScope.launch { prefs.setTheme(v) } },
                            onLanguageChange = { v ->
                                languageSetting = v
                                lifecycleScope.launch {
                                    prefs.setLanguage(v)
                                    applyLanguage(v)
                                }
                            },
                            onResetDisclaimer = {
                                lifecycleScope.launch {
                                    prefs.resetDisclaimer()
                                    disclaimerAccepted = false
                                    currentScreen = "home"
                                }
                            },
                            onClearHistory = {
                                lifecycleScope.launch {
                                    db.historyDao().clearAll()
                                    historyItems = emptyList()
                                }
                            },
                            onResetTutorial = {
                                lifecycleScope.launch {
                                    prefs.resetTutorial()
                                    tutorialSeen = false
                                }
                            }
                        )
                        "error" -> Box(
                            modifier = Modifier.fillMaxSize().background(DarkBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Text("⚠️", style = MaterialTheme.typography.headlineLarge)
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "خطا",
                                    color = TextPrimary,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    errorMessage.ifEmpty { "لطفاً دوباره تلاش کنید." },
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(24.dp))
                                Button(
                                    onClick = {
                                        pendingBitmap = null
                                        pendingCandlePixels = null
                                        errorMessage = ""
                                        currentScreen = "home"
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AccentBlue,
                                        contentColor = TextPrimary
                                    )
                                ) { Text("بازگشت به خانه") }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * اعمال زبان روی اپ
     */
    private fun applyLanguage(langCode: String) {
        val locale = Locale(langCode)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        resources.updateConfiguration(config, resources.displayMetrics)

        // Recreate activity to apply changes
        recreate()
    }

    private suspend fun prepareAnalysis(uri: Uri): PreparationResult = withContext(Dispatchers.Default) {
        try {
            val bitmap = ImageProcessor.loadBitmap(applicationContext, uri) ?: return@withContext PreparationResult(false)
            val candlePixels = ImageProcessor.extractCandles(bitmap) ?: return@withContext PreparationResult(false)
            val pricePoints = OcrHelper.extractPriceAxis(bitmap)
            if (pricePoints.size < 2) return@withContext PreparationResult(false)
            val priceRange = OcrHelper.calculatePriceRange(pricePoints) ?: return@withContext PreparationResult(false)
            val (high, low) = priceRange
            if (high <= low || high / low > 50.0) return@withContext PreparationResult(false)
            val confidence = when {
                pricePoints.size >= 5 -> 90
                pricePoints.size >= 4 -> 80
                pricePoints.size >= 3 -> 65
                pricePoints.size >= 2 -> 50
                else -> 30
            }
            val bounds = ImageProcessor.findChartBoundsPublic(bitmap)
            val lastCandle = candlePixels.lastOrNull()
            val currentPrice = lastCandle?.let { OcrHelper.pixelToPrice(pricePoints, it.bottom) }
            PreparationResult(true, high, low, currentPrice, confidence, bitmap, candlePixels, bounds.chartTop, bounds.chartBottom)
        } catch (e: Exception) {
            PreparationResult(false)
        }
    }

    private suspend fun runAnalysis(priceHigh: Double, priceLow: Double, timeframe: String): AnalysisResult? =
        withContext(Dispatchers.Default) {
            try {
                val pixels = pendingCandlePixels ?: return@withContext null
                val candles = ImageProcessor.pixelsToCandles(pixels, pendingChartTop, pendingChartBottom, priceHigh, priceLow)
                if (candles.size < 20) return@withContext null
                SignalEngine.analyze(candles, timeframe)
            } catch (e: Exception) {
                null
            }
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
            // silent
        }
    }
}