package com.chartyar.app.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.data.NobitexApi
import com.chartyar.app.data.PriceAlert
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.AccentRed
import com.chartyar.app.ui.theme.AccentYellow
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkCard
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider as DividerColor
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertScreen(
    alerts: List<PriceAlert>,
    onBack: () -> Unit,
    onCreateAlert: (String, Double, String, String, String) -> Unit,
    onDeleteAlert: (Long) -> Unit,
    onToggleActive: (Long, Boolean) -> Unit,
    onResetAlert: (Long) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
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
                "هشدار قیمت",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, "افزودن", tint = AccentGreen)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    "💡 راهنما",
                    color = AccentYellow,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "هشدار قیمت بسازید تا وقتی قیمت به هدف رسید، نوتیفیکیشن بگیرید. می‌تونید قیمت رو به تومان یا دلار وارد کنید.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }

        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔔", fontSize = 56.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "هنوز هشداری نساختی",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "روی + بالا بزن",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(alerts, key = { it.id }) { alert ->
                    AlertCard(
                        alert = alert,
                        onDelete = { onDeleteAlert(alert.id) },
                        onToggleActive = { onToggleActive(alert.id, it) },
                        onReset = { onResetAlert(alert.id) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateAlertDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { symbol, price, condition, note, currency ->
                onCreateAlert(symbol, price, condition, note, currency)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun AlertCard(
    alert: PriceAlert,
    onDelete: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onReset: () -> Unit
) {
    val conditionText = if (alert.condition == "ABOVE") "بالاتر از" else "پایین‌تر از"
    val currencyLabel = if (alert.currency == "USD") "دلار" else "تومان"
    val statusColor = when {
        alert.isTriggered -> AccentYellow
        !alert.isActive -> TextSecondary
        else -> AccentGreen
    }
    val statusText = when {
        alert.isTriggered -> "فعال‌شده ✓"
        !alert.isActive -> "غیرفعال"
        else -> "فعال"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        alert.symbol,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "$conditionText ${"%.2f".format(alert.targetPrice)} $currencyLabel",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Switch(
                    checked = alert.isActive && !alert.isTriggered,
                    onCheckedChange = { onToggleActive(it) },
                    enabled = !alert.isTriggered,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TextPrimary,
                        checkedTrackColor = AccentGreen,
                        uncheckedThumbColor = TextSecondary,
                        uncheckedTrackColor = DarkCard
                    )
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, "حذف", tint = AccentRed)
                }
            }

            if (alert.note.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "📝 ${alert.note}",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(8.dp))
            Divider(color = DividerColor)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, RoundedCornerShape(50))
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (alert.isTriggered) {
                    TextButton(onClick = onReset) {
                        Text("فعال‌سازی مجدد", color = AccentBlue, fontSize = 12.sp)
                    }
                } else {
                    Text(
                        formatDate(alert.createdAt),
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateAlertDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Double, String, String, String) -> Unit
) {
    var selectedSymbol by remember { mutableStateOf("BTCUSDT") }
    var priceText by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("ABOVE") }
    var currency by remember { mutableStateOf("TOMAN") }
    var note by remember { mutableStateOf("") }
    var symbolExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var allSymbols by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var isLoadingSymbols by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoadingSymbols = true
        val symbols = withContext(Dispatchers.IO) {
            NobitexApi.fetchAllSymbols()
        }
        allSymbols = symbols
        isLoadingSymbols = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Text("هشدار جدید", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    "ارز",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                ExposedDropdownMenuBox(
                    expanded = symbolExpanded,
                    onExpandedChange = { symbolExpanded = !symbolExpanded }
                ) {
                    OutlinedTextField(
                        value = if (isLoadingSymbols) "در حال بارگذاری..." else selectedSymbol,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Filled.ArrowDropDown, null, tint = TextSecondary)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = DividerColor
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = symbolExpanded,
                        onDismissRequest = { symbolExpanded = false },
                        containerColor = DarkSurface
                    ) {
                        allSymbols.forEach { (sym, name) ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            sym,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            name,
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                },
                                onClick = {
                                    selectedSymbol = sym
                                    symbolExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "واحد قیمت",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConditionChip(
                        text = "تومان",
                        selected = currency == "TOMAN",
                        onClick = { currency = "TOMAN" },
                        modifier = Modifier.weight(1f)
                    )
                    ConditionChip(
                        text = "دلار",
                        selected = currency == "USD",
                        onClick = { currency = "USD" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "شرط",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ConditionChip(
                        text = "بالاتر از",
                        selected = condition == "ABOVE",
                        onClick = { condition = "ABOVE" },
                        modifier = Modifier.weight(1f)
                    )
                    ConditionChip(
                        text = "پایین‌تر از",
                        selected = condition == "BELOW",
                        onClick = { condition = "BELOW" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "قیمت هدف",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it
                        errorMessage = null
                    },
                    placeholder = {
                        Text(
                            if (currency == "USD") "مثلاً 95000" else "مثلاً 9500000000",
                            color = TextSecondary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DividerColor
                    )
                )

                if (currency == "USD") {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "قیمت دلاری به‌صورت خودکار به تومان تبدیل می‌شه.",
                        color = AccentBlue,
                        fontSize = 11.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    "یادداشت (اختیاری)",
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("مثلاً: نقطه ورود", color = TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = DividerColor
                    )
                )

                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        errorMessage!!,
                        color = AccentRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    if (price == null || price <= 0) {
                        errorMessage = "قیمت معتبر وارد کنید"
                        return@Button
                    }
                    val maxPrice = if (currency == "USD") 10_000_000.0 else 1_000_000_000_000.0
                    if (price > maxPrice) {
                        errorMessage = "قیمت خیلی بالاست"
                        return@Button
                    }
                    onCreate(selectedSymbol, price, condition, note, currency)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    contentColor = TextPrimary
                )
            ) {
                Text("ساخت", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("لغو", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun ConditionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) AccentBlue.copy(alpha = 0.2f) else DarkCard
        ),
        border = if (selected) {
            BorderStroke(1.dp, AccentBlue)
        } else null
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                color = if (selected) AccentBlue else TextSecondary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}