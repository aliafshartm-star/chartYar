package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.data.CoinPrice
import com.chartyar.app.data.PricesApi
import com.chartyar.app.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PricesScreen(
    prices: List<CoinPrice>,
    isLoading: Boolean,
    isOnline: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onCoinClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var lastRefresh by remember { mutableStateOf(System.currentTimeMillis()) }

    // فیلتر جستجو
    val filtered = remember(prices, searchQuery) {
        if (searchQuery.isBlank()) prices
        else prices.filter {
            it.symbol.contains(searchQuery, ignoreCase = true) ||
                    it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    // آپدیت خودکار هر ۳۰ ثانیه
    LaunchedEffect(isOnline) {
        while (isOnline) {
            delay(30_000)
            onRefresh()
            lastRefresh = System.currentTimeMillis()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        // هدر
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "بازگشت", tint = TextPrimary)
            }
            Text(
                "قیمت لحظه‌ای",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (isOnline) {
                Text(
                    "🔄 خودکار",
                    color = AccentGreen,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // حالت آفلاین
        if (!isOnline) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AccentYellow.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📶", fontSize = 22.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "اتصال اینترنت برقرار نیست.",
                        color = AccentYellow,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // فیلد جستجو
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("جستجو...", color = TextSecondary) },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = Divider,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            )
        )

        // محتوا
        when {
            isLoading && prices.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AccentBlue)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "در حال دریافت قیمت‌ها...",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            filtered.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💹", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (prices.isEmpty()) "قیمتی دریافت نشد" else "ارزی پیدا نشد",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered, key = { it.symbol }) { coin ->
                        CoinPriceRow(coin = coin, onClick = { onCoinClick(coin.symbol) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CoinPriceRow(coin: CoinPrice, onClick: () -> Unit) {
    val changeColor = if (coin.isUp) AccentGreen else AccentRed
    val arrow = if (coin.isUp) "▲" else "▼"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // نام و نماد
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    coin.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    coin.symbol.replace("USDT", "/USDT"),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // قیمت و تغییر
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$" + PricesApi.formatPrice(coin.price),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        arrow,
                        color = changeColor,
                        fontSize = 10.sp
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        PricesApi.formatPercent(coin.changePercent24h),
                        color = changeColor,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}