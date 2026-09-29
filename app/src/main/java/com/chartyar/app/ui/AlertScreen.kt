package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.data.AlertHelper
import com.chartyar.app.data.BinanceApi
import com.chartyar.app.data.PriceAlert
import com.chartyar.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertScreen(
    alerts: List<PriceAlert>,
    onBack: () -> Unit,
    onCreateAlert: (String, Double, String, String) -> Unit,
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
        // هدر
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

        // توضیح
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
                    "هشدار قیمت بسازید تا وقتی قیمت به هدف رسید، نوتیفیکیشن بگیرید. هر بار که اپ باز می‌شه، قیمت‌ها چک می‌شن.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
        }

        // لیست هشدارها
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

    // Dialog ساخت هشدار
    if (showCreateDialog) {
        CreateAlertDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { symbol, price, condition, note ->
                onCreateAlert(symbol, price, condition, note)
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
            // هدر کارت
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
                        "$conditionText ${"%.2f".format(alert.targetPrice)}",
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
            Divider(color = Divider)
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
    onCreate: (String, Double, String, String) -> Unit
) {
    var selectedSymbol by remember { mutableStateOf("BTCUSDT") }
    var priceText by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("ABOVE") }
    var note by remember { mutableStateOf("") }
    var symbolExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                // انتخاب ارز
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
                        value = selectedSymbol,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Filled.Add, null, tint = TextSecondary)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = Divider
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = symbolExpanded,
                        onDismissRequest = { symbolExpanded = false },
                        containerColor = DarkSurface
                    ) {
                        BinanceApi.popularSymbols.forEach { (sym, name) ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(sym, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                        Text(name, color = TextSecondary, fontSize = 11.sp)
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

                // نوع شرط
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

                // قیمت هدف
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
                    placeholder = { Text("مثلاً 70000", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Divider
                    )
                )

                Spacer(Modifier.height(12.dp))

                // یادداشت (اختیاری)
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
                        unfocusedBorderColor = Divider
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
                    if (price > 100_000_000) {
                        errorMessage = "قیمت خیلی بالاست"
                        return@Button
                    }
                    onCreate(selectedSymbol, price, condition, note)
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
            androidx.compose.foundation.BorderStroke(1.dp, AccentBlue)
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