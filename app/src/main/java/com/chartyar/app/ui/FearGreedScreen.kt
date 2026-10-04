package com.chartyar.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.R
import com.chartyar.app.data.FearGreedApi
import com.chartyar.app.data.FearGreedData
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.AccentRed
import com.chartyar.app.ui.theme.AccentYellow
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FearGreedScreen(
    onBack: () -> Unit
) {
    var data by remember { mutableStateOf<FearGreedData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        isLoading = true
        data = withContext(Dispatchers.IO) { FearGreedApi.fetchFearGreed() }
        isLoading = false
    }

    val value = data?.value ?: 0
    val color = when {
        value <= 25 -> AccentRed
        value <= 45 -> Color(0xFFFF8C42)
        value <= 55 -> AccentYellow
        value <= 75 -> Color(0xFF7BC97C)
        else -> AccentGreen
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, null, tint = TextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.fear_greed_screen_title),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    stringResource(R.string.home_fear_greed_subtitle),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            IconButton(onClick = { refreshKey++ }) {
                Icon(Icons.Filled.Refresh, null, tint = TextPrimary)
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, Divider)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = value.toString(),
                            color = color,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = data?.classificationFa ?: "-",
                            color = color,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            AccentRed,
                                            Color(0xFFFF8C42),
                                            AccentYellow,
                                            Color(0xFF7BC97C),
                                            AccentGreen
                                        )
                                    )
                                )
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("ترس شدید", color = TextSecondary, fontSize = 10.sp)
                            Spacer(Modifier.weight(1f))
                            Text("طمع شدید", color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}