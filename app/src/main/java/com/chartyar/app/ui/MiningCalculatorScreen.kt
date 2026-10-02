package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.R
import com.chartyar.app.data.NobitexApi
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.AccentRed
import com.chartyar.app.ui.theme.AccentYellow
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiningCalculatorScreen(onBack: () -> Unit) {
    var hashrate by remember { mutableStateOf("95") }
    var power by remember { mutableStateOf("3250") }
    var electricityPrice by remember { mutableStateOf("3000") }
    var poolFee by remember { mutableStateOf("2") }
    var btcPrice by remember { mutableStateOf("") }

    var isLoadingPrice by remember { mutableStateOf(true) }
    var priceError by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf("") }

    var dailyIncome by remember { mutableStateOf("") }
    var dailyElectricity by remember { mutableStateOf("") }
    var dailyProfit by remember { mutableStateOf("") }
    var monthlyProfit by remember { mutableStateOf("") }
    var yearlyProfit by remember { mutableStateOf("") }
    var isProfitPositive by remember { mutableStateOf(true) }
    var hasResult by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    suspend fun loadBtcPrice() {
        isLoadingPrice = true
        priceError = false
        val price = withContext(Dispatchers.IO) {
            NobitexApi.fetchCurrentPrice("BTCRLS")
        }
        if (price != null && price > 0.0) {
            btcPrice = String.format(Locale.US, "%.0f", price)
            priceError = false
        } else {
            priceError = true
        }
        isLoadingPrice = false
    }

    LaunchedEffect(Unit) {
        loadBtcPrice()
    }

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
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "back",
                    tint = TextPrimary
                )
            }
            Text(
                text = stringResource(R.string.mining_title),
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Text(
                text = stringResource(R.string.mining_subtitle),
                color = TextSecondary,
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        CalcField(
            label = stringResource(R.string.hashrate_label),
            value = hashrate,
            onValueChange = { hashrate = it.filter { c -> c.isDigit() || c == '.' } }
        )

        Spacer(Modifier.height(12.dp))

        CalcField(
            label = stringResource(R.string.power_label),
            value = power,
            onValueChange = { power = it.filter { c -> c.isDigit() || c == '.' } }
        )

        Spacer(Modifier.height(12.dp))

        CalcField(
            label = stringResource(R.string.electricity_price_label),
            value = electricityPrice,
            onValueChange = { electricityPrice = it.filter { c -> c.isDigit() || c == '.' } }
        )

        Spacer(Modifier.height(12.dp))

        CalcField(
            label = stringResource(R.string.pool_fee_label),
            value = poolFee,
            onValueChange = { poolFee = it.filter { c -> c.isDigit() || c == '.' } }
        )

        Spacer(Modifier.height(12.dp))

        CalcField(
            label = stringResource(R.string.btc_price_label),
            value = btcPrice,
            onValueChange = { btcPrice = it.filter { c -> c.isDigit() || c == '.' } }
        )

        if (isLoadingPrice) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.mining_fetching_price),
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        } else if (priceError) {
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.mining_fetch_failed),
                    color = AccentYellow,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = {
                    scope.launch { loadBtcPrice() }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "retry",
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = "Retry",
                        color = AccentBlue,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val hr = hashrate.toDoubleOrNull() ?: 0.0
                val pw = power.toDoubleOrNull() ?: 0.0
                val elPrice = electricityPrice.toDoubleOrNull() ?: 0.0
                val fee = poolFee.toDoubleOrNull() ?: 0.0
                val btc = btcPrice.toDoubleOrNull() ?: 0.0

                validationError = when {
                    hr <= 0.0 -> "Enter valid hashrate"
                    pw <= 0.0 -> "Enter valid power"
                    elPrice <= 0.0 -> "Enter valid electricity price"
                    btc <= 0.0 -> "BTC price is missing"
                    else -> ""
                }

                if (validationError.isEmpty()) {
                    val result = calculateMining(
                        hashrate = hr,
                        power = pw,
                        electricityPrice = elPrice,
                        poolFeePercent = fee,
                        btcPriceToman = btc
                    )
                    dailyIncome = result.dailyIncomeTomanStr
                    dailyElectricity = result.dailyElectricityStr
                    dailyProfit = result.dailyProfitStr
                    monthlyProfit = result.monthlyProfitStr
                    yearlyProfit = result.yearlyProfitStr
                    isProfitPositive = result.isPositive
                    hasResult = true
                } else {
                    hasResult = false
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
                text = stringResource(R.string.mining_calculate),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (validationError.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AccentRed.copy(alpha = 0.15f)
                )
            ) {
                Text(
                    text = validationError,
                    color = AccentRed,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (hasResult) {
            Spacer(Modifier.height(20.dp))

            ResultRow(
                label = stringResource(R.string.daily_income),
                value = dailyIncome,
                color = TextPrimary
            )

            Spacer(Modifier.height(10.dp))

            ResultRow(
                label = stringResource(R.string.daily_electricity),
                value = dailyElectricity,
                color = AccentRed
            )

            Spacer(Modifier.height(10.dp))

            ResultRow(
                label = stringResource(R.string.daily_profit),
                value = dailyProfit,
                color = if (isProfitPositive) AccentGreen else AccentRed
            )

            Spacer(Modifier.height(10.dp))

            ResultRow(
                label = stringResource(R.string.monthly_profit),
                value = monthlyProfit,
                color = if (isProfitPositive) AccentGreen else AccentRed
            )

            Spacer(Modifier.height(10.dp))

            ResultRow(
                label = stringResource(R.string.yearly_profit),
                value = yearlyProfit,
                color = if (isProfitPositive) AccentGreen else AccentRed
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CalcField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

@Composable
private fun ResultRow(label: String, value: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private data class MiningResult(
    val dailyIncomeTomanStr: String,
    val dailyElectricityStr: String,
    val dailyProfitStr: String,
    val monthlyProfitStr: String,
    val yearlyProfitStr: String,
    val isPositive: Boolean
)

private fun calculateMining(
    hashrate: Double,
    power: Double,
    electricityPrice: Double,
    poolFeePercent: Double,
    btcPriceToman: Double
): MiningResult {
    val blockReward = 3.125
    val networkHashrate = 750000000.0
    val poolFeeMultiplier = 1.0 - (poolFeePercent / 100.0)

    val dailyIncomeBtc = (hashrate / networkHashrate) * (blockReward * 144.0) * poolFeeMultiplier
    val dailyIncomeToman = dailyIncomeBtc * btcPriceToman

    val dailyKwh = (power * 24.0) / 1000.0
    val dailyElectricityToman = dailyKwh * electricityPrice

    val dailyProfitToman = dailyIncomeToman - dailyElectricityToman
    val monthlyProfitToman = dailyProfitToman * 30.0
    val yearlyProfitToman = dailyProfitToman * 365.0

    val isPositive = dailyProfitToman > 0.0
    val prefix = if (isPositive) "+" else ""

    return MiningResult(
        dailyIncomeTomanStr = formatToman(dailyIncomeToman),
        dailyElectricityStr = "-" + formatToman(dailyElectricityToman),
        dailyProfitStr = prefix + formatToman(dailyProfitToman),
        monthlyProfitStr = prefix + formatToman(monthlyProfitToman),
        yearlyProfitStr = prefix + formatToman(yearlyProfitToman),
        isPositive = isPositive
    )
}

private fun formatToman(value: Double): String {
    val absValue = kotlin.math.abs(value)
    return when {
        absValue >= 1_000_000_000 -> String.format(Locale.US, "%,.2f B T", value / 1_000_000_000.0)
        absValue >= 1_000_000 -> String.format(Locale.US, "%,.2f M T", value / 1_000_000.0)
        absValue >= 1_000 -> String.format(Locale.US, "%,.1f K T", value / 1_000.0)
        else -> String.format(Locale.US, "%,.0f T", value)
    }
}