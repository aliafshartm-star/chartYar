package com.chartyar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.chartyar.app.data.PrefsManager
import com.chartyar.app.ui.DisclaimerScreen
import com.chartyar.app.ui.HomeScreen
import com.chartyar.app.ui.theme.ChartYarTheme
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.TextPrimary
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PrefsManager(applicationContext)

        setContent {
            ChartYarTheme {
                var isLoading by remember { mutableStateOf(true) }
                var disclaimerAccepted by remember { mutableStateOf(false) }

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
                        HomeScreen(
                            onUploadClick = { },
                            onHistoryClick = { }
                        )
                    }
                }
            }
        }
    }
}