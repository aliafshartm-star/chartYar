package com.chartyar.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.data.CoinPrice
import com.chartyar.app.data.PricesApi
import com.chartyar.app.data.WatchlistManager
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.AccentRed
import com.chartyar.app.ui.theme.AccentYellow
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary
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

    LaunchedEffect(isOnline) {
        while (isOnline) {
            delay(15_000)
            onRefresh()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "back", tint = TextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "بازار ارز دیجیتال",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    "قیمت‌های لحظه‌ای نوبیتکس",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            if (isOnline) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(AccentGreen, RoundedCornerShape(50))
                )
            }
        }

        if (!isOnline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccentYellow.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    "اتصال اینترنت برقرار نیست",
                    color = AccentYellow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("جستجو...", color = TextSecondary, fontSize = 13.sp) },
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showOnlyWatchlist,
                onClick = { showOnlyWatchlist = !showOnlyWatchlist },
                label = {
                    Text(
                        "علاقه‌مندی‌ها (${watchlistManager.size()})",
                        color = if (showOnlyWatchlist) TextPrimary else TextSecondary,
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentYellow.copy(alpha = 0.2f),
                    containerColor = DarkSurface
                )
            )
        }

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
                    }
                }
            }

            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                ) {
                    Text(
                        "ارز / ارزش بازار",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1.2f)
                    )
                    Text(
                        "آخرین قیمت",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1.4f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                    Text(
                        "24 ساعته",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.width(80.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(filtered, key = { it.symbol }) { coin ->
                        val isFavorite = remember(watchlistVersion) {
                            watchlistManager.contains(coin.symbol)
                        }
                        CoinPriceRowWallex(
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
private fun CoinPriceRowWallex(
    coin: CoinPrice,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val isUp = coin.isUp
    val changeColor = if (isUp) AccentGreen else AccentRed
    val changeSign = if (isUp) "+" else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable { onFavoriteToggle() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "fav",
                tint = if (isFavorite) AccentYellow else TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(Modifier.width(4.dp))

        Column(modifier = Modifier.weight(1.2f)) {
            Text(
                text = coin.symbol.replace("IRT", "").trim(),
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "تومان",
                color = TextSecondary,
                fontSize = 10.sp
            )
        }

        Column(
            modifier = Modifier.weight(1.4f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = PricesApi.formatPrice(coin.price),
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(2.dp))
            if (coin.priceUsd > 0) {
                Text(
                    text = "$" + PricesApi.formatPriceUsd(coin.priceUsd),
                    color = AccentBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    text = "تومان",
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .width(80.dp)
                .height(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(changeColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$changeSign${PricesApi.formatPercent(coin.changePercent24h).replace("%", "")}%",
                color = changeColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}