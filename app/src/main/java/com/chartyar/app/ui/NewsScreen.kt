package com.chartyar.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chartyar.app.data.NewsApi
import com.chartyar.app.data.NewsItem
import com.chartyar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    news: List<NewsItem>,
    isLoading: Boolean,
    isOnline: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("همه") }

    val categories = listOf("همه", "Bitcoin", "Ethereum", "Blockchain", "Trading", "Market")

    val filtered = remember(news, selectedCategory) {
        if (selectedCategory == "همه") news
        else news.filter { it.categories.contains(selectedCategory, ignoreCase = true) }
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
                "اخبار بازار",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, "به‌روزرسانی", tint = AccentBlue)
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
                    Text("📶", fontSize = 24.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "اتصال اینترنت برقرار نیست. اخبار جدید دریافت نمی‌شود.",
                        color = AccentYellow,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // دسته‌بندی‌ها
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                CategoryChip(
                    text = cat,
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat }
                )
            }
        }

        // محتوا
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = AccentBlue)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "در حال دریافت اخبار...",
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
                        Text("📰", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (news.isEmpty()) "خبری دریافت نشد" else "خبری در این دسته نیست",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "روی دکمه به‌روزرسانی بزن",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { item ->
                        NewsCard(
                            item = item,
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // silent
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) AccentBlue else DarkSurface
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                color = if (selected) TextPrimary else TextSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun NewsCard(item: NewsItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column {
            // تصویر
            if (!item.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                )
            }

            Column(modifier = Modifier.padding(14.dp)) {
                // منبع و زمان
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.source,
                        color = AccentBlue,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "•",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        NewsApi.formatRelativeTime(item.publishedAt),
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(Modifier.height(8.dp))

                // عنوان
                Text(
                    item.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(8.dp))

                // خلاصه
                if (item.body.isNotBlank()) {
                    val summary = if (item.body.length > 150) {
                        item.body.substring(0, 150) + "..."
                    } else item.body

                    Text(
                        summary,
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // لینک
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.OpenInNew,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "خواندن کامل خبر",
                        color = AccentBlue,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}