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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.R
import com.chartyar.app.data.MarketIndices
import com.chartyar.app.data.MarketIndicesApi
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.AccentRed
import com.chartyar.app.ui.theme.AccentYellow
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider
import com.chartyar.app.ui.theme.GradientBlueEnd
import com.chartyar.app.ui.theme.GradientBlueStart
import com.chartyar.app.ui.theme.GradientGreenEnd
import com.chartyar.app.ui.theme.GradientGreenStart
import com.chartyar.app.ui.theme.GradientPurpleEnd
import com.chartyar.app.ui.theme.GradientPurpleStart
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MarketIndicesScreen(
    onBack: () -> Unit
) {
    var indices by remember { mutableStateOf<MarketIndices?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        isLoading = true
        indices = withContext(Dispatchers.IO) { MarketIndicesApi.fetchAll() }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
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
                    stringResource(R.string.market_indices_title),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    stringResource(R.string.market_indices_subtitle),
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
                modifier = Modifier.fillMaxWidth().height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentBlue)
            }
        } else {
            val data = indices ?: MarketIndices()

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                IndexCard(
                    title = stringResource(R.string.index_btc_dominance),
                    subtitle = stringResource(R.string.index_btc_dominance_desc),
                    value = "${String.format("%.2f", data.btcDominance)}%",
                    accentColor = AccentYellow,
                    gradient = listOf(GradientPurpleStart, GradientPurpleEnd)
                )

                Spacer(Modifier.height(12.dp))

                IndexCard(
                    title = stringResource(R.string.index_altcoin_season),
                    subtitle = altcoinLabel(data.altcoinSeasonIndex),
                    value = "${data.altcoinSeasonIndex}/100",
                    accentColor = if (data.altcoinSeasonIndex >= 75) AccentGreen
                    else if (data.altcoinSeasonIndex <= 25) AccentRed
                    else AccentYellow,
                    gradient = listOf(GradientBlueStart, GradientBlueEnd)
                )

                Spacer(Modifier.height(12.dp))

                IndexCard(
                    title = stringResource(R.string.index_defi_tvl),
                    subtitle = stringResource(R.string.index_defi_tvl_desc),
                    value = MarketIndicesApi.formatTvl(data.defiTvlUsd),
                    accentColor = AccentGreen,
                    gradient = listOf(GradientGreenStart, GradientGreenEnd)
                )

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun IndexCard(
    title: String,
    subtitle: String,
    value: String,
    accentColor: Color,
    gradient: List<Color>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Brush.linearGradient(gradient), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value.take(3),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Text(
                text = value,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

private fun altcoinLabel(index: Int): String {
    return when {
        index >= 75 -> "آلت سیزن"
        index <= 25 -> "بیت‌کوین سیزن"
        else -> "بازار خنثی"
    }
}