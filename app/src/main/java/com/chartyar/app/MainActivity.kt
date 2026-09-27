package com.chartyar.app

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.chartyar.app.analysis.AnalysisResult
import com.chartyar.app.analysis.SampleData
import com.chartyar.app.analysis.SignalEngine
import com.chartyar.app.data.PrefsManager
import com.chartyar.app.ui.DisclaimerScreen
import com.chartyar.app.ui.HomeScreen
import com.chartyar.app.ui.ResultScreen
import com.chartyar.app.ui.UploadScreen
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.ChartYarTheme
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.TextPrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PrefsManager(applicationContext)

        setContent {
            ChartYarTheme {
                var isLoading by remember { mutableStateOf(true) }
                var disclaimerAccepted by remember { mutableStateOf(false) }
                var currentScreen by remember { mutableStateOf("home") }
                var selectedUri by remember { mutableStateOf<Uri?>(null) }
                var selectedTimeframe by remember { mutableStateOf("4H") }
                var analysisResult by remember { mutableStateOf<AnalysisResult?>(null) }
                var isAnalyzing by remember { mutableStateOf(false) }

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
                                    selectedUri = uri
                                    selectedTimeframe = tf
                                    isAnalyzing = true
                                    currentScreen = "analyzing"

                                    lifecycleScope.launch {
                                        val result = withContext(Dispatchers.Default) {
                                            val sampleCandles = SampleData.generateBullishCandles(100)
                                            SignalEngine.analyze(sampleCandles, tf)
                                        }
                                        analysisResult = result
                                        isAnalyzing = false
                                        currentScreen = "result"
                                    }
                                }
                            )

                            "analyzing" -> {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(DarkBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = AccentBlue)
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            "در حال تحلیل چارت...",
                                            color = TextPrimary
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "تایم‌فریم: $selectedTimeframe",
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }

                            "result" -> {
                                analysisResult?.let { result ->
                                    ResultScreen(
                                        result = result,
                                        onBack = {
                                            analysisResult = null
                                            currentScreen = "home"
                                        }
                                    )
                                } ?: run {
                                    // اگه نتیجه null بود، برگرد به خونه
                                    currentScreen = "home"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}