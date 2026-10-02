package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
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
        // ============ هدر ============
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "بازگشت", tint = TextPrimary)
            }
            Text(
                "ماشین حساب فیوچرز",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // ============ توضیح ============
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📊", fontSize = 24.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    "سود یا زیان معامله فیوچرزت رو حساب کن",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }

        // ============ نوع پوزیشن (لانگ/شورت) ============
        Text(
            "نوع پوزیشن:",
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
                text = "📈 لانگ (خرید)",
                selected = isLong,
                selectedColor = AccentGreen,
                onClick = { isLong = true },
                modifier = Modifier.weight(1f)
            )
            DirectionChip(
                text = "📉 شورت (فروش)",
                selected = !isLong,
                selectedColor = AccentRed,
                onClick = { isLong = false },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // ============ فیلدها ============
        CalculatorField(
            label = "قیمت ورود",
            placeholder = "مثلاً 90000",
            value = entryPrice,
            onValueChange = { entryPrice = it.filter { c -> c.isDigit() || c == '.' } },
            suffix = "USDT"
        )

        Spacer(Modifier.height(12.dp))

        CalculatorField(
            label = "قیمت خروج",
            placeholder = "مثلاً 95000",
            value = exitPrice,
            onValueChange = { exitPrice = it.filter { c -> c.isDigit() || c == '.' } },
            suffix = "USDT"
        )

        Spacer(Modifier.height(12.dp))

        CalculatorField(
            label = "اهرم (Leverage)",
            placeholder = "مثلاً 10",
            value = leverage,
            onValueChange = { leverage = it.filter { c -> c.isDigit() } },
            suffix = "x"
        )

        Spacer(Modifier.height(12.dp))

        CalculatorField(
            label = "سرمایه / مارجین",
            placeholder = "مثلاً 100",
            value = margin,
            onValueChange = { margin = it.filter { c -> c.isDigit() || c == '.' } },
            suffix = "USDT"
        )

        Spacer(Modifier.height(20.dp))

        // ============ دکمه محاسبه ============
        Button(
            onClick = {
                val entry = entryPrice.toDoubleOrNull()
                val exit = exitPrice.toDoubleOrNull()
                val lev = leverage.toDoubleOrNull()
                val mar = margin.toDoubleOrNull()

                if (entry == null || entry <= 0) {
                    errorMessage = "قیمت ورود معتبر وارد کن"
                    result = null
                    return@Button
                }
               
 if (exit == null || exit <= 0) {
                    errorMessage = "قیمت خروج معتبر وارد کن"
                    result            = null
                    return@Button
                }
                if (lev == null || lev < 1 || lev > )
 200) {
                    errorMessage = "اهرم باید بین ۱ تا ۲۰۰ باشه"
                    result =        null
                    return@Button
                }
                if (mar == null || mar <= 0) {
                    errorMessage = " }

سرمایه معتبر وارد کن"
                    result = null
                    return@Button
                }

                       errorMessage = ""
                result = calculatePosition(
                    entry = entry,
                    exit = exit,
                    leverage = lev,
                    margin = mar,
                    isLong = isLong
                )
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
                "محاسبه کن",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium // ============ پیام خطا ============
        if (errorMessage.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.15f))
            ) {
                Text(
                    errorMessage,
                    color = AccentRed,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ============ نتیجه ============
        result?.let { r ->
            Spacer(Modifier.height(16.dp))
            ResultCard(r)
        }

        Spacer(Modifier.height(24.dp))

        // ============ هشدار ============
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⚠️", fontSize = 20.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    "این محاسبه تقریبیه. کارمزد صرافی و فاندینگ ریت در نظر گرفته نشده.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ============ فیلد ورودی ============
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalculatorField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String = ""
) {
    Column {
        Text(
            label,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextSecondary, fontSize = 14.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            suffix = if (suffix.isNotEmpty()) {
                { Text(suffix, color = TextSecondary, fontWeight = FontWeight.SemiBold) }
            } else null,
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

// ============ چیپ لانگ/شورت ============
@Composable
private fun DirectionChip(
    text: String,
    selected: Boolean,
    selectedColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) selectedColor.copy(alpha = 0.2f) else DarkSurface
        ),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(1.5.dp, selectedColor)
        } else null
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                color = if (selected) selectedColor else TextSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// ============ کارت نتیجه ============
@Composable
private fun ResultCard(r: PositionResult) {
    val isProfit = r.pnl >= 0
    val mainColor = if (isProfit) AccentGreen else AccentRed
    val arrow = if (isProfit) "▲" else "▼"
    val sign = if (isProfit) "+" else ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            // ============ سود/زیان کلی ============
            Text(
                "سود / زیان کلی",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    arrow,
                    color = mainColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "$sign${"%.2f".format(r.pnl)} USDT",
                    color = mainColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "$sign${"%.2f".format(r.pnlPercent)}٪ تغییر قیمت",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(16.dp))
            Divider(color = Divider)
            Spacer(Modifier.height(16.dp))

            // ============ جزئیات ============
            ResultRow("قیمت ورود", "${"%.2f".format(r.entry)} USDT", TextPrimary)
            Spacer(Modifier.height(10.dp))
            ResultRow("قیمت خروج", "${"%.2f".format(r.exit)} USDT", TextPrimary)
            Spacer(Modifier.height(10.dp))
            ResultRow("اهرم", "${"%.0f".format(r.leverage)}x", AccentYellow)
            Spacer(Modifier.height(10.dp))
            ResultRow("سرمایه اولیه", "${"%.2f".format(r.margin)} USDT", TextPrimary)
            Spacer(Modifier.height(10.dp))
            ResultRow(
                "ارزش پوزیشن",
                "${"%.2f".format(r.positionSize)} USDT",
                TextPrimary
            )

            Spacer(Modifier.height(16.dp))
            Divider(color = Divider)
            Spacer(Modifier.height(16.dp))

            // ============ ROE (بازده سرمایه) ============
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
                        "ROE — بازده سرمایه",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "$sign${"%.2f".format(r.roe)}٪",
                        color = mainColor,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ============ موجودی نهایی ============
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
                        "موجودی نهایی",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${"%.2f".format(r.finalBalance)} USDT",
                        color = mainColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // ============ هشدار لیکویید ============
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
                        Text("🚨", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "لیکویید شدی!",
                                color = AccentRed,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "کل سرمایه از دست رفته",
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
private fun ResultRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            value,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// ============ مدل نتیجه ============
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

// ============ منطق محاسبه ============
private fun calculatePosition(
    entry: Double,
    exit: Double,
    leverage: Double,
    margin: Double,
    isLong: Boolean
): PositionResult {
    // ارزش کل پوزیشن
    val positionSize = margin * leverage

    // تغییر قیمت
    val priceChangePercent = ((exit - entry) / entry) * 100.0

    // P&L خام (بدون اهرم)
    val rawPnl = positionSize * (priceChangePercent / 100.0)

    // اگه شورت، معکوس
    val pnl = if (isLong) rawPnl else -rawPnl

    // P&L درصدی
    val pnlPercent = if (isLong) priceChangePercent else -priceChangePercent

    // ROE (بازده سرمایه)
    val roe = (pnl / margin) * 100.0

    // موجودی نهایی
    var finalBalance = margin + pnl
    var isLiquidated = false

    if (finalBalance <= 0) {
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