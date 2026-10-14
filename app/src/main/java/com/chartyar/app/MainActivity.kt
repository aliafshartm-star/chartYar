package com.chartyar.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.chartyar.app.analysis.AnalysisResult
import com.chartyar.app.analysis.MultiTimeframeResult
import com.chartyar.app.analysis.SignalEngine
import com.chartyar.app.analysis.SignalType
import com.chartyar.app.analysis.StrategyEngine
import com.chartyar.app.analysis.StrategyType
import com.chartyar.app.analysis.TrendType
import com.chartyar.app.data.AnalysisHistory
import com.chartyar.app.data.AnalysisLimitManager
import com.chartyar.app.data.AlertChecker
import com.chartyar.app.data.AlertHelper
import com.chartyar.app.data.AppDatabase
import com.chartyar.app.data.ArzdigitalNewsApi
import com.chartyar.app.data.CacheManager
import com.chartyar.app.data.CoinPrice
import com.chartyar.app.data.ConnectivityChecker
import com.chartyar.app.data.DataManager
import com.chartyar.app.data.FearGreedApi
import com.chartyar.app.data.LegalManager
import com.chartyar.app.data.LocalAnalytics
import com.chartyar.app.data.MarketDataService
import com.chartyar.app.data.NetworkError
import com.chartyar.app.data.NewsItem
import com.chartyar.app.data.PrefsManager
import com.chartyar.app.data.PriceAlert
import com.chartyar.app.data.PricesApi
import com.chartyar.app.data.RateAppManager
import com.chartyar.app.data.UpdateChecker
import com.chartyar.app.security.RootDetector
import com.chartyar.app.ui.AboutScreen
import com.chartyar.app.ui.AgeScreen
import com.chartyar.app.ui.AlertScreen
import com.chartyar.app.ui.ChangelogScreen
import com.chartyar.app.ui.CoinDetailScreen
import com.chartyar.app.ui.DisclaimerScreen
import com.chartyar.app.ui.EducationScreen
import com.chartyar.app.ui.ExchangesScreen
import com.chartyar.app.ui.ExitDialog
import com.chartyar.app.ui.FearGreedScreen
import com.chartyar.app.ui.GlossaryScreen
import com.chartyar.app.ui.HistoryScreen
import com.chartyar.app.ui.HomeScreen
import com.chartyar.app.ui.LatestAnalysesScreen
import com.chartyar.app.ui.LimitDialog
import com.chartyar.app.ui.LimitDialogType
import com.chartyar.app.ui.MarketIndicesScreen
import com.chartyar.app.ui.MiningCalculatorScreen
import com.chartyar.app.ui.MultiTfInputScreen
import com.chartyar.app.ui.MultiTimeframeScreen
import com.chartyar.app.ui.NewsScreen
import com.chartyar.app.ui.PositionCalculatorScreen
import com.chartyar.app.ui.PricesScreen
import com.chartyar.app.ui.PrivacyPolicyScreen
import com.chartyar.app.ui.ProgressScreen
import com.chartyar.app.ui.RateAppDialog
import com.chartyar.app.ui.ResultScreen
import com.chartyar.app.ui.SettingsScreen
import com.chartyar.app.ui.SplashScreen
import com.chartyar.app.ui.StrategyScreen
import com.chartyar.app.ui.SymbolSelectorScreen
import com.chartyar.app.ui.TelegramGateScreen
import com.chartyar.app.ui.TermsScreen
import com.chartyar.app.ui.TrendingCoinsScreen
import com.chartyar.app.ui.TutorialScreen
import com.chartyar.app.ui.UpdateDialog
import com.chartyar.app.ui.ViaChartBottomBar
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.ChartYarTheme
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.util.CameraHelper
import com.chartyar.app.util.NotificationHelper
import com.chartyar.app.util.RemoteLogger
import com.chartyar.app.util.RewardedAdHelper
import com.chartyar.app.work.AlertMonitorService
import com.chartyar.app.work.WorkScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAPSELL_REWARDED_ZONE_ID = "6ac034978e1ca215ec56dc0f"

class MainActivity : ComponentActivity() {
    private lateinit var prefs: PrefsManager
    private lateinit var db: AppDatabase
    private lateinit var cache: CacheManager
    private lateinit var rateAppManager: RateAppManager
    private lateinit var legalManager: LegalManager
    private lateinit var dataManager: DataManager
    private lateinit var limitManager: AnalysisLimitManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PrefsManager(applicationContext)
        db = AppDatabase.get(applicationContext)
        cache = CacheManager(applicationContext)
        rateAppManager = RateAppManager(applicationContext)
        legalManager = LegalManager(applicationContext)
        dataManager = DataManager(applicationContext)
        limitManager = AnalysisLimitManager(applicationContext)

        NotificationHelper.createChannel(applicationContext)
        LocalAnalytics.logEvent(applicationContext, "APP_OPEN")

        CameraHelper.clearOldCameraFiles(applicationContext)
        cache.clearOldCache()

        rateAppManager.recordLaunch()

        WorkScheduler.schedule(applicationContext)
        AlertMonitorService.start(applicationContext)

        val shortcutAction = intent?.getStringExtra("shortcut_action")

        val deepLinkUri = intent?.data
        val deepLinkHost = deepLinkUri?.host
        val deepLinkSymbol = deepLinkUri?.getQueryParameter("symbol") ?: ""
        val deepLinkTimeframe = deepLinkUri?.getQueryParameter("timeframe") ?: "4H"

        setContent {
            val themeFlow = remember { prefs.theme }
            val currentTheme by themeFlow.collectAsState(initial = "dark")
            val isDark = currentTheme == "dark"

            ChartYarTheme(isDark = isDark) {
                var isLoading by remember { mutableStateOf(true) }
                var disclaimerAccepted by remember { mutableStateOf(false) }
                var termsAccepted by remember { mutableStateOf(false) }
                var ageConfirmed by remember { mutableStateOf(false) }
                var telegramGatePassed by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf("home") }
                var selectedTimeframe by remember { mutableStateOf("4H") }
                var selectedStrategy by remember { mutableStateOf(StrategyType.SWING) }
                var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }
                var statusMessage by remember { mutableStateOf("...") }
                var analyzeProgress by remember { mutableStateOf(0f) }
                var errorMessage by remember { mutableStateOf("") }
                var historyItems by remember { mutableStateOf<List<AnalysisHistory>>(emptyList()) }
                var alertItems by remember { mutableStateOf<List<PriceAlert>>(emptyList()) }
                var newsItems by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
                var isNewsLoading by remember { mutableStateOf(false) }
                var priceItems by remember { mutableStateOf<List<CoinPrice>>(emptyList()) }
                var isPricesLoading by remember { mutableStateOf(false) }
                var autoSaveHistory by remember { mutableStateOf(true) }
                var showWarnings by remember { mutableStateOf(true) }
                var defaultTimeframe by remember { mutableStateOf("4H") }
                var themeSetting by remember { mutableStateOf("dark") }
                var tutorialSeen by remember { mutableStateOf(true) }
                var isOnline by remember { mutableStateOf(true) }
                var updateInfo by remember { mutableStateOf<UpdateChecker.UpdateInfo?>(null) }
                var updateDialogShown by remember { mutableStateOf(false) }
                var showRateDialog by remember { mutableStateOf(false) }
                var showExitDialog by remember { mutableStateOf(false) }
                var multiTfResult by remember { mutableStateOf<MultiTimeframeResult?>(null) }
                var selectedCoin by remember { mutableStateOf<CoinPrice?>(null) }

                var limitStatus by remember { mutableStateOf(limitManager.getStatus()) }
                var unlockedStrategies by remember { mutableStateOf(limitManager.getUnlockedStrategies()) }
                var showLimitDialog by remember { mutableStateOf(false) }
                var limitDialogType by remember { mutableStateOf(LimitDialogType.DAILY_LIMIT) }
                var lockedStrategyNameForDialog by remember { mutableStateOf("") }
                var lockedStrategyTypeForDialog by remember { mutableStateOf<StrategyType?>(null) }
                var strategyRefreshTick by remember { mutableStateOf(0) }

                fun refreshLimitStatus() {
                    limitStatus = limitManager.getStatus()
                    unlockedStrategies = limitManager.getUnlockedStrategies()
                    strategyRefreshTick++
                }

                fun openDailyLimitDialog() {
                    limitDialogType = LimitDialogType.DAILY_LIMIT
                    lockedStrategyNameForDialog = ""
                    lockedStrategyTypeForDialog = null
                    showLimitDialog = true
                }

                fun openLockedStrategyDialog(strategy: StrategyType) {
                    val info = StrategyEngine.getStrategy(strategy)
                    limitDialogType = LimitDialogType.LOCKED_STRATEGY
                    lockedStrategyNameForDialog = info.displayName
                    lockedStrategyTypeForDialog = strategy
                    showLimitDialog = true
                }

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
                    tutorialSeen = prefs.isTutorialSeen.first()
                    telegramGatePassed = prefs.isTelegramGatePassed.first()
                    selectedTimeframe = defaultTimeframe

                    val savedStrategy = prefs.defaultStrategy.first()
                    selectedStrategy = try {
                        StrategyType.valueOf(savedStrategy)
                    } catch (e: Exception) {
                        StrategyType.SWING
                    }

                    isOnline = ConnectivityChecker.isOnline(applicationContext)

                    if (isOnline) {
                        AlertChecker.checkAllAlerts(applicationContext, db, cache)
                    }

                    withContext(Dispatchers.IO) {
                        RootDetector.checkRoot(applicationContext)
                    }

                    if (isOnline) {
                        val info = withContext(Dispatchers.IO) {
                            UpdateChecker.checkForUpdate()
                        }
                        if (info != null && info.hasUpdate) {
                            updateInfo = info
                        }
                    }

                    if (rateAppManager.shouldShowRateDialog()) {
                        showRateDialog = true
                        rateAppManager.markShown()
                    }

                    termsAccepted = legalManager.hasAcceptedLatestTerms()
                    ageConfirmed = legalManager.hasConfirmedAge()

                    refreshLimitStatus()
                }

                LaunchedEffect(currentScreen) {
                    if (currentScreen == "history") {
                        historyItems = db.historyDao().getAll().first()
                    }
                    if (currentScreen == "alerts") {
                        alertItems = db.alertDao().getAll().first()
                    }
                    if (currentScreen == "news" && newsItems.isEmpty()) {
                        isNewsLoading = true
                        val fetched = withContext(Dispatchers.IO) {
                            ArzdigitalNewsApi.fetchNews(30)
                        }
                        if (fetched != null) newsItems = fetched
                        isNewsLoading = false
                    }
                    if (currentScreen == "prices" && priceItems.isEmpty()) {
                        isPricesLoading = true
                        val fetched = withContext(Dispatchers.IO) { PricesApi.fetchPrices() }
                        if (fetched != null) priceItems = fetched
                        isPricesLoading = false
                    }
                }

                LaunchedEffect(Unit) {
                    while (true) {
                        delay(15_000L)
                        if (disclaimerAccepted) {
                            val online = ConnectivityChecker.isOnline(applicationContext)
                            isOnline = online
                            if (online) {
                                try {
                                    AlertChecker.checkAllAlerts(applicationContext, db, cache)
                                } catch (e: Exception) {
                                }
                            }
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    val startTime = System.currentTimeMillis()
                    disclaimerAccepted = prefs.isDisclaimerAccepted.first()
                    val elapsed = System.currentTimeMillis() - startTime
                    if (elapsed < 4500) delay(4500 - elapsed)
                    isLoading = false

                    when (shortcutAction) {
                        "new_analysis" -> currentScreen = "symbol"
                        "alerts" -> currentScreen = "alerts"
                        "glossary" -> currentScreen = "glossary"
                    }

                    when (deepLinkHost) {
                        "open" -> { }
                        "analysis" -> {
                            if (deepLinkSymbol.isNotEmpty()) {
                                selectedTimeframe = deepLinkTimeframe
                                currentScreen = "symbol"
                            }
                        }
                    }
                }

                BackHandler(
                    enabled = !isLoading && disclaimerAccepted && termsAccepted &&
                            ageConfirmed && telegramGatePassed
                ) {
                    when (currentScreen) {
                        "home" -> showExitDialog = true
                        "privacy" -> currentScreen = "settings"
                        "terms" -> currentScreen = "settings"
                        "glossary" -> currentScreen = "settings"
                        "education" -> currentScreen = "settings"
                        "about" -> currentScreen = "settings"
                        "changelog" -> currentScreen = "settings"
                        "settings" -> currentScreen = "home"
                        "history" -> currentScreen = "home"
                        "alerts" -> currentScreen = "home"
                        "news" -> currentScreen = "home"
                        "prices" -> currentScreen = "home"
                        "coin_detail" -> currentScreen = "prices"
                        "symbol" -> currentScreen = "home"
                        "result" -> currentScreen = "home"
                        "multi_tf" -> currentScreen = "home"
                        "multi_tf_result" -> currentScreen = "multi_tf"
                        "calculator" -> currentScreen = "home"
                        "mining" -> currentScreen = "home"
                        "exchanges" -> currentScreen = "home"
                        "trending" -> currentScreen = "home"
                        "strategy" -> currentScreen = "home"
                        "market_indices" -> currentScreen = "home"
                        "fear_greed" -> currentScreen = "home"
                        else -> showExitDialog = true
                    }
                }

                if (updateInfo != null && !updateDialogShown) {
                    UpdateDialog(
                        updateInfo = updateInfo!!,
                        onDismiss = { updateDialogShown = true }
                    )
                }

                if (showRateDialog) {
                    RateAppDialog(
                        onRate = {
                            rateAppManager.openInCafeBazaar()
                            showRateDialog = false
                        },
                        onDontAsk = {
                            rateAppManager.markDontAsk()
                            showRateDialog = false
                        },
                        onLater = { showRateDialog = false }
                    )
                }

                if (showExitDialog) {
                    ExitDialog(
                        onConfirm = {
                            showExitDialog = false
                            finish()
                        },
                        onDismiss = { showExitDialog = false }
                    )
                }

                if (showLimitDialog) {
                    val strategyAdsWatched = lockedStrategyTypeForDialog?.let {
                        limitManager.getStrategyAdsProgress(it.name)
                    } ?: 0

                    val strategyAdsRequired = lockedStrategyTypeForDialog?.let {
                        limitManager.getStrategyUnlockCost(it.name)
                    } ?: 0

                    LimitDialog(
                        type = limitDialogType,
                        strategyName = lockedStrategyNameForDialog,
                        adsSinceReward = limitStatus.adsSinceReward,
                        adsPerReward = AnalysisLimitManager.ADS_PER_REWARD,
                        strategyAdsWatched = strategyAdsWatched,
                        strategyAdsRequired = strategyAdsRequired,
                        onWatchAd = {
                            val isLockedStrategyFlow =
                                limitDialogType == LimitDialogType.LOCKED_STRATEGY &&
                                        lockedStrategyTypeForDialog != null

                            showLimitDialog = false

                            RewardedAdHelper.showRewardedAd(
                                activity = this@MainActivity,
                                zoneId = TAPSELL_REWARDED_ZONE_ID,
                                onReward = {
                                    runOnUiThread {
                                        if (isLockedStrategyFlow) {
                                            val strategy = lockedStrategyTypeForDialog
                                            if (strategy != null) {
                                                val unlocked = limitManager.onStrategyUnlockAdWatched(strategy.name)
                                                refreshLimitStatus()

                                                if (unlocked) {
                                                    selectedStrategy = strategy
                                                    lifecycleScope.launch {
                                                        prefs.setDefaultStrategy(strategy.name)
                                                    }
                                                } else {
                                                    openLockedStrategyDialog(strategy)
                                                }
                                            }
                                        } else {
                                            limitManager.onAdWatched()
                                            refreshLimitStatus()
                                        }
                                    }
                                },
                                onFailed = { msg ->
                                    runOnUiThread {
                                        errorMessage = "تبلیغ نمایش داده نشد: $msg"
                                    }
                                }
                            )
                        },
                        onDismiss = {
                            showLimitDialog = false
                        }
                    )
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

                    !termsAccepted -> TermsScreen(
                        onAccept = {
                            lifecycleScope.launch {
                                legalManager.acceptTerms()
                                legalManager.acceptPrivacy()
                                termsAccepted = true
                            }
                        },
                        onDecline = { finish() }
                    )

                    !ageConfirmed -> AgeScreen(
                        onConfirm = {
                            lifecycleScope.launch {
                                legalManager.confirmAge()
                                ageConfirmed = true
                            }
                        },
                        onDecline = { finish() }
                    )

                    !telegramGatePassed -> TelegramGateScreen(
                        onConfirm = {
                            lifecycleScope.launch {
                                prefs.setTelegramGatePassed(true)
                                telegramGatePassed = true
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
                    currentScreen == "glossary" -> GlossaryScreen(
                        onBack = { currentScreen = "settings" }
                    )
                    currentScreen == "education" -> EducationScreen(
                        onBack = { currentScreen = "settings" }
                    )
                    currentScreen == "about" -> AboutScreen(
                        onBack = { currentScreen = "settings" }
                    )
                    currentScreen == "changelog" -> ChangelogScreen(
                        onBack = { currentScreen = "settings" }
                    )
                    else -> {
                        val mainTabs = setOf("home", "trending", "exchanges", "strategy")
                        val showBottomBar = currentScreen in mainTabs

                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f)) {
                                when (currentScreen) {
                                    "home" -> HomeScreen(
                                        remainingFree = limitStatus.remainingFree,
                                        adsSinceReward = limitStatus.adsSinceReward,
                                        adsPerReward = AnalysisLimitManager.ADS_PER_REWARD,
                                        onUploadClick = { currentScreen = "symbol" },
                                        onHistoryClick = { currentScreen = "history" },
                                        onSettingsClick = { currentScreen = "settings" },
                                        onAlertsClick = { currentScreen = "alerts" },
                                        onNewsClick = { currentScreen = "market_indices" },
                                        onAnalysesClick = { currentScreen = "news" },
                                        onPricesClick = { currentScreen = "prices" },
                                        onMultiTfClick = { currentScreen = "fear_greed" },
                                        onCalculatorClick = { currentScreen = "calculator" },
                                        onMiningClick = { currentScreen = "mining" },
                                        onExchangesClick = { currentScreen = "exchanges" }
                                    )
                                    "strategy" -> {
                                        val _refresh = strategyRefreshTick
                                        StrategyScreen(
                                            unlockedStrategies = unlockedStrategies,
                                            strategyAdProgress = { strategyName ->
                                                val cost = limitManager.getStrategyUnlockCost(strategyName)
                                                val watched = limitManager.getStrategyAdsProgress(strategyName)
                                                watched to cost
                                            },
                                            onStrategyClick = { strategyType ->
                                                val isUnlocked = unlockedStrategies.contains(strategyType.name)
                                                if (isUnlocked) {
                                                    selectedStrategy = strategyType
                                                    currentScreen = "symbol"
                                                } else {
                                                    openLockedStrategyDialog(strategyType)
                                                }
                                            }
                                        )
                                    }
                                    "market_indices" -> MarketIndicesScreen(
                                        onBack = { currentScreen = "home" }
                                    )
                                    "fear_greed" -> FearGreedScreen(
                                        onBack = { currentScreen = "home" }
                                    )
                                    "trending" -> TrendingCoinsScreen(
                                        onBack = { currentScreen = "home" },
                                        onCoinClick = { currentScreen = "prices" }
                                    )
                                    "analyses" -> LatestAnalysesScreen(
                                        onBack = { currentScreen = "home" }
                                    )
                                    "calculator" -> PositionCalculatorScreen(
                                        onBack = { currentScreen = "home" }
                                    )
                                    "mining" -> MiningCalculatorScreen(
                                        onBack = { currentScreen = "home" }
                                    )
                                    "exchanges" -> ExchangesScreen(
                                        onBack = { currentScreen = "home" }
                                    )
                                    "multi_tf" -> MultiTfInputScreen(
                                        cache = cache,
                                        onBack = { currentScreen = "home" },
                                        onResult = { res ->
                                            multiTfResult = res
                                            currentScreen = "multi_tf_result"
                                        }
                                    )
                                    "multi_tf_result" -> {
                                        val r = multiTfResult
                                        if (r != null) {
                                            MultiTimeframeScreen(
                                                result = r,
                                                onBack = { currentScreen = "multi_tf" }
                                            )
                                        } else {
                                            currentScreen = "multi_tf"
                                        }
                                    }
                                    "symbol" -> SymbolSelectorScreen(
                                        strategies = StrategyEngine.allStrategies,
                                        selectedStrategy = selectedStrategy,
                                        defaultTimeframe = defaultTimeframe,
                                        lockedStrategies = StrategyEngine.allStrategies
                                            .filter { !unlockedStrategies.contains(it.type.name) }
                                            .map { it.type }
                                            .toSet(),
                                        onStrategyChange = { newStrategy ->
                                            val isUnlocked = unlockedStrategies.contains(newStrategy.name)
                                            if (isUnlocked) {
                                                selectedStrategy = newStrategy
                                                lifecycleScope.launch {
                                                    prefs.setDefaultStrategy(newStrategy.name)
                                                }
                                            } else {
                                                openLockedStrategyDialog(newStrategy)
                                            }
                                        },
                                        onBack = { currentScreen = "home" },
                                        onSymbolSelected = { symbol, tf, strategy ->
                                            if (!limitManager.consumeAnalysis()) {
                                                openDailyLimitDialog()
                                                refreshLimitStatus()
                                            } else {
                                                refreshLimitStatus()

                                                selectedTimeframe = tf
                                                selectedStrategy = strategy
                                                analyzeProgress = 0f
                                                statusMessage = "..."
                                                errorMessage = ""
                                                currentScreen = "analyzing"

                                                lifecycleScope.launch {
                                                    val online = ConnectivityChecker.isOnline(applicationContext)
                                                    isOnline = online

                                                    if (!online) {
                                                        errorMessage = NetworkError.NO_INTERNET.message
                                                        currentScreen = "error"
                                                        return@launch
                                                    }

                                                    analyzeProgress = 0.3f
                                                    statusMessage = "در حال دریافت کندل‌ها"
                                                    delay(200)

                                                    val dataResult = MarketDataService.getCandles(
                                                        symbol = symbol,
                                                        timeframe = selectedTimeframe,
                                                        cache = cache
                                                    )

                                                    analyzeProgress = 0.5f
                                                    statusMessage = "در حال محاسبه اندیکاتورها"
                                                    delay(300)

                                                    if (dataResult.candles != null && dataResult.candles.isNotEmpty()) {
                                                        analyzeProgress = 0.7f
                                                        statusMessage = "در حال تحلیل روند"
                                                        delay(300)
                                                        analyzeProgress = 0.9f
                                                        statusMessage = "در حال بررسی الگوها"

                                                        // ─── فقط برای PRO: دریافت Fear&Greed + BTC Change ───
                                                        var btcChange = 0.0
                                                        var fearGreedValue = 50

                                                        if (strategy == StrategyType.PRO) {
                                                            try {
                                                                fearGreedValue = withContext(Dispatchers.IO) {
                                                                    FearGreedApi.fetchFearGreed()
                                                                }?.value ?: 50
                                                            } catch (e: Exception) { fearGreedValue = 50 }

                                                            try {
                                                                val btcResult = withContext(Dispatchers.IO) {
                                                                    MarketDataService.getCandles("BTCUSDT", "4H", cache)
                                                                }
                                                                val btcCandles = btcResult.candles
                                                                if (btcCandles != null && btcCandles.size >= 6) {
                                                                    val now = btcCandles.last().close
                                                                    val before = btcCandles[btcCandles.size - 6].close
                                                                    if (before > 0) btcChange = ((now - before) / before) * 100
                                                                }
                                                            } catch (e: Exception) { btcChange = 0.0 }
                                                        }

                                                        val result = withContext(Dispatchers.Default) {
                                                            SignalEngine.analyze(
                                                                dataResult.candles,
                                                                selectedTimeframe,
                                                                strategy,
                                                                btcChange,
                                                                fearGreedValue
                                                            )
                                                        }
                                                        analyzeProgress = 1f
                                                        statusMessage = "در حال آماده‌سازی نتیجه"
                                                        delay(200)
                                                        analysisResult = result
                                                        if (result != null) {
                                                            if (autoSaveHistory) saveToHistory(result, selectedTimeframe)
                                                            currentScreen = "result"
                                                        } else {
                                                            errorMessage = "کندل کافی برای این استراتژی وجود نداره"
                                                            currentScreen = "error"
                                                        }
                                                    } else {
                                                        errorMessage = NetworkError.DATA_EMPTY.message
                                                        currentScreen = "error"
                                                    }
                                                }
                                            }
                                        }
                                    )
                                    "analyzing" -> ProgressScreen(
                                        progress = analyzeProgress,
                                        statusMessage = statusMessage,
                                        timeframe = selectedTimeframe
                                    )
                                    "result" -> {
                                        val result = analysisResult
                                        if (result != null) ResultScreen(
                                            result = result,
                                            onBack = {
                                                analysisResult = null
                                                currentScreen = "home"
                                            },
                                            onChangeStrategy = {
                                                analysisResult = null
                                                currentScreen = "symbol"
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
                                        onCreateAlert = { symbol, price, condition, note, currency ->
                                            lifecycleScope.launch {
                                                db.alertDao().insert(
                                                    AlertHelper.createAlert(symbol, price, condition, note, currency)
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
                                    "news" -> NewsScreen(
                                        news = newsItems,
                                        isLoading = isNewsLoading,
                                        isOnline = isOnline,
                                        onBack = { currentScreen = "home" },
                                        onRefresh = {
                                            lifecycleScope.launch {
                                                isNewsLoading = true
                                                val fetched = withContext(Dispatchers.IO) {
                                                    ArzdigitalNewsApi.fetchNews(30)
                                                }
                                                if (fetched != null) newsItems = fetched
                                                isNewsLoading = false
                                            }
                                        }
                                    )
                                    "prices" -> PricesScreen(
                                        prices = priceItems,
                                        isLoading = isPricesLoading,
                                        isOnline = isOnline,
                                        onBack = { currentScreen = "home" },
                                        onRefresh = {
                                            lifecycleScope.launch {
                                                isPricesLoading = true
                                                val fetched = withContext(Dispatchers.IO) { PricesApi.fetchPrices() }
                                                if (fetched != null) priceItems = fetched
                                                isPricesLoading = false
                                            }
                                        },
                                        onCoinClick = { symbol ->
                                            val coin = priceItems.firstOrNull { it.symbol == symbol }
                                            selectedCoin = coin
                                            currentScreen = "coin_detail"
                                        }
                                    )
                                    "coin_detail" -> CoinDetailScreen(
                                        coin = selectedCoin,
                                        onBack = { currentScreen = "prices" },
                                        onAnalyze = { symbol ->
                                            if (!limitManager.consumeAnalysis()) {
                                                openDailyLimitDialog()
                                                refreshLimitStatus()
                                            } else {
                                                refreshLimitStatus()

                                                selectedTimeframe = "4H"
                                                selectedStrategy = StrategyType.SWING
                                                analyzeProgress = 0f
                                                statusMessage = "..."
                                                errorMessage = ""
                                                currentScreen = "analyzing"

                                                lifecycleScope.launch {
                                                    val online = ConnectivityChecker.isOnline(applicationContext)
                                                    isOnline = online

                                                    if (!online) {
                                                        errorMessage = NetworkError.NO_INTERNET.message
                                                        currentScreen = "error"
                                                        return@launch
                                                    }

                                                    analyzeProgress = 0.3f
                                                    statusMessage = "در حال دریافت کندل‌ها"
                                                    delay(200)

                                                    val dataResult = MarketDataService.getCandles(
                                                        symbol = symbol,
                                                        timeframe = selectedTimeframe,
                                                        cache = cache
                                                    )

                                                    analyzeProgress = 0.5f
                                                    statusMessage = "در حال محاسبه اندیکاتورها"
                                                    delay(300)

                                                    if (dataResult.candles != null && dataResult.candles.isNotEmpty()) {
                                                        analyzeProgress = 0.7f
                                                        statusMessage = "در حال تحلیل روند"
                                                        delay(300)
                                                        analyzeProgress = 0.9f
                                                        statusMessage = "در حال بررسی الگوها"

                                                        var btcChange = 0.0
                                                        var fearGreedValue = 50

                                                        if (selectedStrategy == StrategyType.PRO) {
                                                            try {
                                                                fearGreedValue = withContext(Dispatchers.IO) {
                                                                    FearGreedApi.fetchFearGreed()
                                                                }?.value ?: 50
                                                            } catch (e: Exception) { fearGreedValue = 50 }

                                                            try {
                                                                val btcResult = withContext(Dispatchers.IO) {
                                                                    MarketDataService.getCandles("BTCUSDT", "4H", cache)
                                                                }
                                                                val btcCandles = btcResult.candles
                                                                if (btcCandles != null && btcCandles.size >= 6) {
                                                                    val now = btcCandles.last().close
                                                                    val before = btcCandles[btcCandles.size - 6].close
                                                                    if (before > 0) btcChange = ((now - before) / before) * 100
                                                                }
                                                            } catch (e: Exception) { btcChange = 0.0 }
                                                        }

                                                        val result = withContext(Dispatchers.Default) {
                                                            SignalEngine.analyze(
                                                                dataResult.candles,
                                                                selectedTimeframe,
                                                                selectedStrategy,
                                                                btcChange,
                                                                fearGreedValue
                                                            )
                                                        }
                                                        analyzeProgress = 1f
                                                        statusMessage = "در حال آماده‌سازی نتیجه"
                                                        delay(200)
                                                        analysisResult = result
                                                        if (result != null) {
                                                            if (autoSaveHistory) saveToHistory(result, selectedTimeframe)
                                                            currentScreen = "result"
                                                        } else {
                                                            errorMessage = "کندل کافی برای این استراتژی وجود نداره"
                                                            currentScreen = "error"
                                                        }
                                                    } else {
                                                        errorMessage = NetworkError.DATA_EMPTY.message
                                                        currentScreen = "error"
                                                    }
                                                }
                                            }
                                        },
                                        onSetAlert = { _, _ ->
                                            selectedCoin = null
                                            currentScreen = "alerts"
                                        }
                                    )
                                    "settings" -> SettingsScreen(
                                        autoSaveHistory = autoSaveHistory,
                                        showWarnings = showWarnings,
                                        defaultTimeframe = defaultTimeframe,
                                        theme = themeSetting,
                                        onBack = { currentScreen = "home" },
                                        onShowTutorial = { currentScreen = "tutorial" },
                                        onShowGlossary = { currentScreen = "glossary" },
                                        onShowEducation = { currentScreen = "education" },
                                        onShowAbout = { currentScreen = "about" },
                                        onShowChangelog = { currentScreen = "changelog" },
                                        onShowPrivacy = { currentScreen = "privacy" },
                                        onShowTerms = { currentScreen = "terms" },
                                        onAutoSaveChange = { v ->
                                            autoSaveHistory = v
                                            lifecycleScope.launch { prefs.setAutoSaveHistory(v) }
                                        },
                                        onShowWarningsChange = { v ->
                                            showWarnings = v
                                            lifecycleScope.launch { prefs.setShowWarnings(v) }
                                        },
                                        onDefaultTimeframeChange = { v ->
                                            defaultTimeframe = v
                                            lifecycleScope.launch { prefs.setDefaultTimeframe(v) }
                                        },
                                        onThemeChange = { v ->
                                            themeSetting = v
                                            lifecycleScope.launch { prefs.setTheme(v) }
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
                                                currentScreen = "tutorial"
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
                                            Text(
                                                text = errorMessage,
                                                color = TextPrimary,
                                                style = MaterialTheme.typography.headlineMedium
                                            )
                                            Spacer(Modifier.height(24.dp))
                                            Button(
                                                onClick = {
                                                    errorMessage = ""
                                                    currentScreen = "home"
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = AccentBlue,
                                                    contentColor = TextPrimary
                                                )
                                            ) { Text("OK") }
                                        }
                                    }
                                }
                            }

                            if (showBottomBar) {
                                ViaChartBottomBar(
                                    currentRoute = currentScreen,
                                    onHomeClick = { currentScreen = "home" },
                                    onTrendingClick = { currentScreen = "trending" },
                                    onExchangesClick = { currentScreen = "exchanges" },
                                    onStrategyClick = { currentScreen = "strategy" }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        try {
            AlertMonitorService.stop(applicationContext)
        } catch (e: Exception) {
        }
        super.onDestroy()
    }

    private suspend fun saveToHistory(result: AnalysisResult, timeframe: String) {
        try {
            val signalText = when (result.signal) {
                SignalType.STRONG_BUY -> "STRONG_BUY"
                SignalType.BUY -> "BUY"
                SignalType.NEUTRAL -> "NEUTRAL"
                SignalType.SELL -> "SELL"
                SignalType.STRONG_SELL -> "STRONG_SELL"
            }
            val trendText = when (result.trend) {
                TrendType.UPTREND -> "UP"
                TrendType.DOWNTREND -> "DOWN"
                TrendType.RANGE -> "RANGE"
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
            RemoteLogger.logError(
                tag = "MainActivity",
                message = "saveToHistory: ${e.message ?: "unknown"}",
                throwable = e
            )
        }
    }
}