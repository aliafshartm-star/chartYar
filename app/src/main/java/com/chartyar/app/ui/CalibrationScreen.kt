package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(
    detectedHigh: Double?,
    detectedLow: Double?,
    detectedCurrentPrice: Double?,
    confidence: Int,
    onBack: () -> Unit,
    onConfirm: (Double, Double) -> Unit
) {
    var highText by remember {
        mutableStateOf(detectedHigh?.let { "%.2f".format(it) } ?: "")
    }
    var lowText by remember {
        mutableStateOf(detectedLow?.let { "%.2f".format(it) } ?: "")
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val highValue = highText.toDoubleOrNull()
    val lowValue = lowText.toDoubleOrNull()
    val isValid = highValue != null && lowValue != null && highValue > lowValue

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "بازگشت", tint = TextPrimary)
            }
            Text(
                "کالیبراسیون دستی",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        ConfidenceCard(confidence = confidence, detectedCurrentPrice = detectedCurrentPrice)

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "📌 راهنما",
                    color = AccentYellow,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "نرم‌افزار خودکار قیمت‌های محور چارت را خوانده، اما اگر خطا داشت، می‌توانید قیمت بالا و پایین محدوده چارت را دستی وارد کنید.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "بالاترین قیمت (سقف چارت)",
            color = AccentGreen,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = highText,
            onValueChange = {
                highText = it
                errorMessage = null
            },
            placeholder = { Text("مثلاً 130.00", color = TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentGreen,
                unfocusedBorderColor = Divider,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            )
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "پایین‌ترین قیمت (کف چارت)",
            color = AccentRed,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = lowText,
            onValueChange = {
                lowText = it
                errorMessage = null
            },
            placeholder = { Text("مثلاً 80.00", color = TextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentRed,
                unfocusedBorderColor = Divider,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            )
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = AccentRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        errorMessage!!,
                        color = AccentRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (highValue == null || lowValue == null) {
                    errorMessage = "لطفاً هر دو قیمت را وارد کنید"
                    return@Button
                }
                if (highValue <= lowValue) {
                    errorMessage = "قیمت بالا باید بیشتر از قیمت پایین باشد"
                    return@Button
                }
                if (highValue / lowValue > 20.0) {
                    errorMessage = "فاصله قیمت‌ها غیرمعمول است. لطفاً چک کنید."
                    return@Button
                }
                onConfirm(highValue, lowValue)
            },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = TextPrimary,
                disabledContainerColor = DarkCard,
                disabledContentColor = TextSecondary
            )
        ) {
            Text(
                "تأیید و تحلیل",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "این اعداد را از محور سمت راست چارت خودتان بخوانید",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ConfidenceCard(confidence: Int, detectedCurrentPrice: Double?) {
    val (color, icon, text) = when {
        confidence >= 80 -> Triple(AccentGreen, Icons.Filled.CheckCircle, "تشخیص دقیق")
        confidence >= 50 -> Triple(AccentYellow, Icons.Filled.Warning, "تشخیص متوسط")
        else -> Triple(AccentRed, Icons.Filled.Warning, "تشخیص ضعیف")
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text,
                        color = color,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "اعتماد پردازش: $confidence%",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (detectedCurrentPrice != null) {
                Spacer(Modifier.height(12.dp))
                Divider(color = Divider)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "قیمت فعلی تشخیص‌داده‌شده:",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "%.2f".format(detectedCurrentPrice),
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            if (confidence < 80) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "⚠️ دقت تشخیص پایین است. لطفاً اعداد بالا و پایین را چک کنید.",
                    color = AccentYellow,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}