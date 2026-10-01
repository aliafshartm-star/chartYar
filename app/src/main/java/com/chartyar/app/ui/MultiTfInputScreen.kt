package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chartyar.app.analysis.MultiTimeframeAnalyzer
import com.chartyar.app.analysis.MultiTimeframeResult
import com.chartyar.app.data.CacheManager
import com.chartyar.app.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MultiTfInputScreen(
    cache: CacheManager,
    onBack: () -> Unit,
    onResult: (MultiTimeframeResult) -> Unit
) {
    var symbol by remember { mutableStateOf("BTCUSDT") }
    var selectedTfs by remember { mutableStateOf(setOf("15m", "1H", "4H", "1D")) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val allTfs = listOf("5m", "15m", "30m", "1H", "4H", "1D", "1W")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // هدر
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "بازگشت", tint = TextPrimary)
            }
            Text(
                "تحلیل چند تایم‌فریمی",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // ورودی نماد
        OutlinedTextField(
            value = symbol,
            onValueChange = { symbol = it.uppercase() },
            label = { Text("نماد (مثلاً BTCUSDT)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = Divider,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedLabelColor = AccentBlue,
                unfocusedLabelColor = TextSecondary
            )
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "تایم‌فریم‌ها (حداقل ۲ تا انتخاب کن):",
            color = TextPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        // چیپ‌های انتخاب تایم‌فریم
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            allTfs.forEach { tf ->
                val isSelected = selectedTfs.contains(tf)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        selectedTfs = if (isSelected) selectedTfs - tf else selectedTfs + tf
                    },
                    label = {
                        Text(
                            tf,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentBlue,
                        containerColor = DarkSurface
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "انتخاب‌شده: ${selectedTfs.size} تایم‌فریم",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(24.dp))

        // دکمه شروع
        if (!isAnalyzing) {
            Button(
                onClick = {
                    if (selectedTfs.size < 2) {
                        errorMessage = "حداقل ۲ تایم‌فریم انتخاب کن."
                        return@Button
                    }
                    if (symbol.isBlank()) {
                        errorMessage = "نماد را وارد کن."
                        return@Button
                    }

                    isAnalyzing = true
                    errorMessage = ""

                    scope.launch {
                        val res = withContext(Dispatchers.IO) {
                            MultiTimeframeAnalyzer.analyze(
                                symbol = symbol.trim(),
                                timeframes = selectedTfs.toList(),
                                cache = cache
                            )
                        }
                        isAnalyzing = false
                        if (res != null) {
                            onResult(res)
                        } else {
                            errorMessage = "تحلیل ناموفق بود. اینترنت یا نماد را بررسی کن."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    contentColor = TextPrimary
                )
            ) {
                Text(
                    "شروع تحلیل",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        // حالت در حال تحلیل
        if (isAnalyzing) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "در حال دریافت و تحلیل ${selectedTfs.size} تایم‌فریم...",
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // پیام خطا
        if (errorMessage.isNotEmpty() && !isAnalyzing) {
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Text(
                    errorMessage,
                    color = AccentRed,
                    modifier = Modifier.padding(20.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "⚠️ این تحلیل صرفاً جنبه آموزشی دارد و توصیه سرمایه‌گذاری نیست.",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
        )
    }
}