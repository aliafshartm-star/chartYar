package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.analysis.StrategyInfo
import com.chartyar.app.analysis.StrategyType
import com.chartyar.app.data.NobitexApi
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.AccentYellow
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkCard
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymbolSelectorScreen(
    strategies: List<StrategyInfo>,
    selectedStrategy: StrategyType,
    defaultTimeframe: String,
    lockedStrategies: Set<StrategyType>,
    onStrategyChange: (StrategyType) -> Unit,
    onBack: () -> Unit,
    onSymbolSelected: (String, String, StrategyType) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTimeframe by remember { mutableStateOf(defaultTimeframe) }
    var allSymbols by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val timeframes = listOf("15m", "1H", "4H", "1D")

    LaunchedEffect(Unit) {
        isLoading = true
        val symbols = withContext(Dispatchers.IO) {
            NobitexApi.fetchAllSymbols()
        }
        allSymbols = symbols
        isLoading = false
    }

    val filtered = if (searchQuery.isBlank()) {
        allSymbols
    } else {
        allSymbols.filter { (symbol, name) ->
            symbol.contains(searchQuery, ignoreCase = true) ||
                    name.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "back", tint = TextPrimary)
            }
            Text(
                "تحلیل جدید",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // Strategy Buttons
        Text(
            "استراتژی معاملاتی",
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            strategies.forEach { strategy ->
                val isLocked = strategy.type in lockedStrategies
                val isSelected = strategy.type == selectedStrategy
                StrategyButton(
                    strategy = strategy,
                    isSelected = isSelected,
                    isLocked = isLocked,
                    onClick = { onStrategyChange(strategy.type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Strategy description
        val currentStrategy = strategies.firstOrNull { it.type == selectedStrategy }
        if (currentStrategy != null) {
            Text(
                text = currentStrategy.description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Timeframe
        Text(
            "تایم‌فریم",
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            timeframes.forEach { tf ->
                val selected = selectedTimeframe == tf
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(
                            color = if (selected) AccentBlue else DarkSurface,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedTimeframe = tf },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        tf,
                        color = if (selected) TextPrimary else TextSecondary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Search
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("جستجوی ارز...", color = TextSecondary, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = Divider,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            )
        )

        // Symbol list
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentBlue)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "در حال دریافت لیست ارزها...",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else if (filtered.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "ارزی پیدا نشد",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filtered) { (symbol, name) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSymbolSelected(symbol, selectedTimeframe, selectedStrategy)
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    name,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontSize = 14.sp
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    symbol,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                "›",
                                color = AccentBlue,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StrategyButton(
    strategy: StrategyInfo,
    isSelected: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) AccentBlue else Divider
    val bgColor = if (isSelected) AccentBlue.copy(alpha = 0.15f) else DarkCard

    Card(
        modifier = modifier
            .height(78.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = strategy.emoji,
                    fontSize = 22.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = strategy.displayName,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                if (isLocked) {
                    Spacer(Modifier.height(2.dp))
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "locked",
                        tint = AccentYellow,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}