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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.data.CoinPrice
import com.chartyar.app.data.PricesApi
import com.chartyar.app.data.WatchlistManager
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
    val context = LocalContext.current
    val watchlistManager = remember { WatchlistManager(context) }

    var searchQuery by remember { mutableStateOf("") }
    var showOnlyWatchlist by remember { mutableStateOf(false) }
    var watchlistVersion by remember { mutableStateOf(0) }

    val filtered = remember(prices, searchQuery, showOnlyWatchlist, watchlistVersion) {
        var result = prices

        if (showOnlyWatchlist) {
            val watchlist = watchlistManager.getAll()
            result = result.filter { watchlist.contains(it.symbol) }
        }

        if (searchQuery.isNotBlank()) {
            result = result.filter {
                it.symbol.contains(searchQuery, ignoreCase = true) ||
                        it.name.contains(searchQuery, ignoreCase = true)
            }
        }

        result
    }

    // آپدیت خودکار
    LaunchedEffect(isOnline) {
        while (isOnline) {
            delay(30_000)
            onRefresh()
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

        // فیلتر Watchlist
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = showOnlyWatchlist,
                onClick = { showOnlyWatchlist = !showOnlyWatchlist },
                label = {
                    Text(
                        "⭐ علاقه‌مندی‌ها (${watchlistManager.size()})",
                        color = if (showOnlyWatchlist) TextPrimary else TextSecondary
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentYellow.copy(alpha = 0.2f),
                    containerColor = DarkSurface
                )
            )
        }

        // جستجو
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
                        Text(
                            if (showOnlyWatchlist) "⭐" else "💹",
                            fontSize = 56.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            when {
                                prices.isEmpty() -> "قیمتی دریافت نشد"
                                showOnlyWatchlist -> "لیست علاقه‌مندی‌ها خالیه"
                                else -> "ارزی پیدا نشد"
                            },
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (showOnlyWatchlist && watchlistManager.isEmpty()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "روی ستاره ارزها بزن",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered, key = { it.symbol }) { coin ->
                        val isFavorite = remember(watchlistVersion) {
                            watchlistManager.contains(coin.symbol)
                        }

                        CoinPriceRow(
                            coin = coin,
                            isFavorite = isFavorite,
                            onClick = { onCoinClick(coin.symbol) },
                            onFavoriteToggle = {
                                watchlistManager.toggle(coin.symbol)
                                watchlistVersion++
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoinPriceRow(
    coin: CoinPrice,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
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
                .padding(start = 6.dp, end = 14.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ستاره
            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "علاقه‌مندی",
                    tint = if (isFavorite) AccentYellow else TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(4.dp))

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
                    coin.symbol.replace("IRT", "/تومان"),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // قیمت و تغییر
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    PricesApi.formatPrice(coin.price) + " تومان",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(arrow, color = changeColor, fontSize = 10.sp)
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