package com.chartyar.app.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.AccentRed
import com.chartyar.app.ui.theme.AccentYellow
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkCard
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PositionCalculatorScreen(
    onBack: () -> Unit
) {
    var entryPrice by remember { mutableStateOf("") }
    var exitPrice by remember { mutableStateOf("") }
    var leverage by remember { mutableStateOf("10") }
    var margin by remember { mutableStateOf("100") }
    var isLong by remember { mutableStateOf(true) }
    var result by remember { mutableStateOf<PositionResult?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
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
                text = "ماشین حساب فیوچرز",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // توضیح
        Card(
 style            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults =.cardColors(containerColor = DarkSurface)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment Material = Alignment.CenterVertically
            ) {
                Text(text = "📊", fontSize = 24.sp)
                Spacer(Modifier.width(Theme10.dp))
                Text(
                    text = "سود یا زیان معامله فیوچرز را حساب کن",
                   . color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
       typ }

        // نوع پوزیشن
        Text(
            text = "نوع پوزیشن:",
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DirectionChip(
                text = "لانگ",
                selected = isLong,
                selectedColor = AccentGreen,
                onClick = { isLong = true },
                modifier = Modifier.weight(1f)
            )
            DirectionChip(
                text = "شورت",
                selected = !isLong,
                selectedColor = AccentRed,
                onClick = { isLong = false },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // فیلد قیمت ورود
        CalcField(
            label = "قیمت ورود",
            placeholder = "90000",
            value = entryPrice,
            suffix = "USDT",
            onValueChange = { newValue ->
                entryPrice = newValue.filter { it.isDigit() || it == '.' }
            }
        )

        Spacer(Modifier.height(12.dp))

        // فیلد قیمت خروج
        CalcField(
            label = "قیمت خروج",
            placeholder = "95000",
            value = exitPrice,
            suffix = "USDT",
            onValueChange = { newValue ->
                exitPrice = newValue.filter { it.isDigit() || it == '.' }
            }
        )

        Spacer(Modifier.height(12.dp))

        // فیلد اهرم
        CalcField(
            label = "اهرم",
            placeholder = "10",
            value = leverage,
            suffix = "x",
            onValueChange = { newValue ->
                leverage = newValue.filter { it.isDigit() }
            }
        )

        Spacer(Modifier.height(12.dp))

        // فیلد سرمایه
        CalcField(
            label = "سرمایه",
            placeholder = "100",
            value = margin,
            suffix = "USDT",
            onValueChange = { newValue ->
                margin = newValue.filter { it.isDigit() || it == '.' }
            }
        )

        Spacer(Modifier.height(20.dp))

        // دکمه محاسبه
        Button(
            onClick = {
                val entry = entryPrice.toDoubleOrNull()
                val exit = exitPrice.toDoubleOrNull()
                val lev = leverage.toDoubleOrNull()
                val mar = margin.toDoubleOrNull()

                if (entry == null || entry <= 0.0) {
                    errorMessage = "قیمت ورود معتبر وارد کن"
                    result = null
                } else if (exit == null || exit <= 0.0) {
                    errorMessage = "قیمت خروج معتبر وارد کن"
                    result = null
                } else if (lev == null || lev < 1.0 || lev > 200.0) {
                    errorMessage = "اهرم باید بین 1 تا 200 باشد"
                    result = null
                } else if (mar == null || mar <= 0.0) {
                    errorMessage = "سرمایه معتبر وارد کن"
                    result = null
                } else {
                    errorMessage = ""
                    result = calculatePosition(
                        entry = entry,
                        exit = exit,
                        leverage = lev,
                        margin = mar,
                        isLong = isLong
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = TextPrimary
            )
        ) {
            Icon(Icons.Filled.Calculate, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "محاسبه کن",
                fontWeight = FontWeight.Bold,
               ography.titleMedium
            )
        }

        // پیام خطا
        if (errorMessage.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.15f))
            ) {
                Text(
                    text = errorMessage,
                    color = AccentRed,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // نتیجه
        result?.let { r ->
            Spacer(Modifier.height(16.dp))
            ResultCard(r)
        }

        Spacer(Modifier.height(24.dp))

        // هشدار
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "⚠️", fontSize = 20.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "این محاسبه تقریبی است. کارمزد صرافی در نظر گرفته نشده.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// فیلد ورودی
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalcField(
    label: String,
    placeholder: String,
    value: String,
    suffix: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(text = placeholder, color = TextSecondary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            suffix = { Text(text = suffix, color = TextSecondary, fontWeight = FontWeight.SemiBold) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = Divider,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            )
        )
    }
}

// چیپ لانگ و شورت
@Composable
private fun DirectionChip(
    text: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) selectedColor.copy(alpha = 0.2f) else DarkSurface
    val txtColor = if (selected) selectedColor else TextSecondary
    val weight = if (selected) FontWeight.Bold else FontWeight.Normal

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = txtColor,
                fontWeight = weight,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

// کارت نتیجه
@Composable
private fun ResultCard(r: PositionResult) {
    val isProfit = r.pnl >= 0.0
    val mainColor = if (isProfit) AccentGreen else AccentRed
    val arrow = if (isProfit) "▲" else "▼"
    val sign = if (isProfit) "+" else ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {

            Text(
                text = "سود یا زیان",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = arrow, color = mainColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "$sign${"%.2f".format(r.pnl)} USDT",
                    color = mainColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = "$sign${"%.2f".format(r.pnlPercent)} درصد",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(16.dp))
            Divider(color = Divider)
            Spacer(Modifier.height(16.dp))

            InfoRow("قیمت ورود", "${"%.2f".format(r.entry)} USDT", TextPrimary)
            Spacer(Modifier.height(10.dp))
            InfoRow("قیمت خروج", "${"%.2f".format(r.exit)} USDT", TextPrimary)
            Spacer(Modifier.height(10.dp))
            InfoRow("اهرم", "${"%.0f".format(r.leverage)}x", AccentYellow)
            Spacer(Modifier.height(10.dp))
            InfoRow("سرمایه", "${"%.2f".format(r.margin)} USDT", TextPrimary)
            Spacer(Modifier.height(10.dp))
            InfoRow("ارزش پوزیشن", "${"%.2f".format(r.positionSize)} USDT", TextPrimary)

            Spacer(Modifier.height(16.dp))
            Divider(color = Divider)
            Spacer(Modifier.height(16.dp))

            // ROE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = mainColor.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ROE - بازده سرمایه",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "$sign${"%.2f".format(r.roe)} درصد",
                        color = mainColor,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // موجودی نهایی
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "موجودی نهایی",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${"%.2f".format(r.finalBalance)} USDT",
                        color = mainColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            if (r.isLiquidated) {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🚨", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "لیکویید شدی!",
                                color = AccentRed,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "کل سرمایه از دست رفته",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, color = valueColor, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
    }
}

// مدل نتیجه
data class PositionResult(
    val entry: Double,
    val exit: Double,
    val leverage: Double,
    val margin: Double,
    val positionSize: Double,
    val pnl: Double,
    val pnlPercent: Double,
    val roe: Double,
    val finalBalance: Double,
    val isLiquidated: Boolean
)

// منطق محاسبه
private fun calculatePosition(
    entry: Double,
    exit: Double,
    leverage: Double,
    margin: Double,
    isLong: Boolean
): PositionResult {
    val positionSize = margin * leverage
    val priceChangePercent = ((exit - entry) / entry) * 100.0
    val rawPnl = positionSize * (priceChangePercent / 100.0)
    val pnl = if (isLong) rawPnl else -rawPnl
    val pnlPercent = if (isLong) priceChangePercent else -priceChangePercent
    val roe = (pnl / margin) * 100.0

    var finalBalance = margin + pnl
    var isLiquidated = false

    if (finalBalance <= 0.0) {
        finalBalance = 0.0
        isLiquidated = true
    }

    return PositionResult(
        entry = entry,
        exit = exit,
        leverage = leverage,
        margin = margin,
        positionSize = positionSize,
        pnl = pnl,
        pnlPercent = pnlPercent,
        roe = roe,
        finalBalance = finalBalance,
        isLiquidated = isLiquidated
    )
}