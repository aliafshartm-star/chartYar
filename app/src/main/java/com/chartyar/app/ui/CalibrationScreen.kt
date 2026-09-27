package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chartyar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(
    detectedHigh: Double?,
    detectedLow: Double?,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
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
                "کالیبراسیون دستی",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // توضیح
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
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // فیلد قیمت بالا
        Text(
            "بالاترین قیمت (سقف چارت)",
            color = TextPrimary,
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
            placeholder = { Text("مثلاً 67000", color = TextSecondary) },
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

        // فیلد قیمت پایین
        Text(
            "پایین‌ترین قیمت (کف چارت)",
            color = TextPrimary,
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
            placeholder = { Text("مثلاً 63000", color = TextSecondary) },
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
            Text(
                errorMessage!!,
                color = AccentRed,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.weight(1f))

        // دکمه تأیید
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

        // متن کمکی
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