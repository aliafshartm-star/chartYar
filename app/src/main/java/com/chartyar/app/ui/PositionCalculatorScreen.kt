package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.R
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.AccentRed
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PositionCalculatorScreen(onBack: () -> Unit) {
    var entry by remember { mutableStateOf("") }
    var exit by remember { mutableStateOf("") }
    var lev by remember { mutableStateOf("10") }
    var margin by remember { mutableStateOf("100") }
    var isLong by remember { mutableStateOf(true) }
    var resultText by remember { mutableStateOf("") }
    var isProfit by remember { mutableStateOf(true) }

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
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "back",
                    tint = TextPrimary
                )
            }
            Text(
                text = stringResource(R.string.calculator_title),
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val longBg = if (isLong) AccentGreen.copy(alpha = 0.2f) else DarkSurface
            val longColor = if (isLong) AccentGreen else TextSecondary
            val shortBg = if (!isLong) AccentRed.copy(alpha = 0.2f) else DarkSurface
            val shortColor = if (!isLong) AccentRed else TextSecondary

            Card(
                modifier = Modifier.weight(1f).clickable { isLong = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = longBg)
            ) {
                Text(
                    text = stringResource(R.string.long_position),
                    color = longColor,
                    modifier = Modifier.padding(14.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            Card(
                modifier = Modifier.weight(1f).clickable { isLong = false },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = shortBg)
            ) {
                Text(
                    text = stringResource(R.string.short_position),
                    color = shortColor,
                    modifier = Modifier.padding(14.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        CalcField(
            label = stringResource(R.string.entry_price),
            value = entry,
            onValueChange = { entry = it.filter { c -> c.isDigit() || c == '.' } }
        )

        Spacer(Modifier.height(12.dp))

        CalcField(
            label = stringResource(R.string.exit_price),
            value = exit,
            onValueChange = { exit = it.filter { c -> c.isDigit() || c == '.' } }
        )

        Spacer(Modifier.height(12.dp))

        CalcField(
            label = stringResource(R.string.leverage_label),
            value = lev,
            onValueChange = { lev = it.filter { c -> c.isDigit() } }
        )

        Spacer(Modifier.height(12.dp))

        CalcField(
            label = stringResource(R.string.margin_label),
            value = margin,
            onValueChange = { margin = it.filter { c -> c.isDigit() || c == '.' } }
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                val e = entry.toDoubleOrNull() ?: 0.0
                val x = exit.toDoubleOrNull() ?: 0.0
                val l = lev.toDoubleOrNull() ?: 0.0
                val m = margin.toDoubleOrNull() ?: 0.0

                if (e <= 0.0 || x <= 0.0 || l <= 0.0 || m <= 0.0) {
                    resultText = "invalid"
                    isProfit = false
                } else {
                    val posSize = m * l
                    val changePct = ((x - e) / e) * 100.0
                    val rawPnl = posSize * (changePct / 100.0)
                    val pnl = if (isLong) rawPnl else -rawPnl
                    val roe = (pnl / m) * 100.0
                    val finalBal = m + pnl
                    isProfit = pnl >= 0.0
                    val prefix = if (isProfit) "+" else ""
                    val pnlStr = String.format("%.2f", pnl)
                    val roeStr = String.format("%.2f", roe)
                    val balStr = String.format("%.2f", finalBal)
                    resultText = "$prefix$pnlStr USDT / ROE: $prefix$roeStr% / $prefix$balStr USDT"
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
                text = stringResource(R.string.calculate_button),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (resultText.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            val color = if (isProfit) AccentGreen else AccentRed
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
            ) {
                Text(
                    text = resultText,
                    color = color,
                    modifier = Modifier.padding(20.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
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