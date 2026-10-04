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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.chartyar.app.data.AnalysisHistory
import com.chartyar.app.data.NoteManager
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.AccentRed
import com.chartyar.app.ui.theme.AccentYellow
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkCard
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider as DividerColor
import com.chartyar.app.ui.theme.GradientBlueEnd
import com.chartyar.app.ui.theme.GradientBlueStart
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    items: List<AnalysisHistory>,
    onBack: () -> Unit,
    onDelete: (Long) -> Unit,
    onClearAll: () -> Unit
) {
    val context = LocalContext.current
    val noteManager = remember { NoteManager(context) }

    var editingItem by remember { mutableStateOf<AnalysisHistory?>(null) }
    var noteText by remember { mutableStateOf("") }
    var detailItem by remember { mutableStateOf<AnalysisHistory?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "back", tint = TextPrimary)
            }
            Text(
                "تاریخچه تحلیل ها",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (items.isNotEmpty()) {
                IconButton(onClick = onClearAll) {
                    Icon(Icons.Filled.Delete, "clear all", tint = AccentRed)
                }
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("خالی", fontSize = 32.sp, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "هنوز تحلیلی ذخیره نشده",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    val note = remember(item.timestamp) { noteManager.getNote(item.timestamp) }
                    HistoryCard(
                        item = item,
                        note = note,
                        onClick = { detailItem = item },
                        onDelete = { onDelete(item.id) },
                        onEditNote = {
                            editingItem = item
                            noteText = note
                        }
                    )
                }
            }
        }
    }

    if (editingItem != null) {
        AlertDialog(
            onDismissRequest = { editingItem = null },
            containerColor = DarkSurface,
            titleContentColor = TextPrimary,
            title = {
                Text("یادداشت تحلیل", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        editingItem?.timeframe ?: "",
                        color = AccentBlue,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        placeholder = {
                            Text(
                                "یادداشت خود را بنویسید",
                                color = TextSecondary
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = DividerColor
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingItem?.let {
                            noteManager.saveNote(it.timestamp, noteText)
                        }
                        editingItem = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        contentColor = Color.White
                    )
                ) {
                    Text("ذخیره", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) {
                    Text("لغو", color = TextSecondary)
                }
            }
        )
    }

    if (detailItem != null) {
        val item = detailItem!!
        val note = noteManager.getNote(item.timestamp)

        Dialog(onDismissRequest = { detailItem = null }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "جزئیات تحلیل",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { detailItem = null }) {
                            Icon(Icons.Filled.Close, "close", tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "تایم فریم: " + item.timeframe,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        "تاریخ: " + formatDate(item.timestamp),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    Spacer(Modifier.height(12.dp))

                    DetailRow(
                        label = "سیگنال",
                        value = item.signalText,
                        valueColor = signalColor(item.signalText)
                    )
                    DetailRow(
                        label = "اطمینان",
                        value = item.confidence.toString() + "%",
                        valueColor = AccentBlue
                    )
                    DetailRow(
                        label = "قیمت فعلی",
                        value = formatPrice(item.currentPrice),
                        valueColor = TextPrimary
                    )

                    Spacer(Modifier.height(8.dp))
                    Divider(color = DividerColor)
                    Spacer(Modifier.height(8.dp))

                    DetailRow(
                        label = "نقطه ورود",
                        value = formatPrice(item.entry),
                        valueColor = AccentBlue
                    )
                    DetailRow(
                        label = "حد ضرر",
                        value = formatPrice(item.stopLoss),
                        valueColor = AccentRed
                    )
                    DetailRow(
                        label = "هدف اول",
                        value = formatPrice(item.target1),
                        valueColor = AccentGreen
                    )
                    DetailRow(
                        label = "هدف دوم",
                        value = formatPrice(item.target2),
                        valueColor = AccentGreen
                    )
                    DetailRow(
                        label = "هدف سوم",
                        value = formatPrice(item.target3),
                        valueColor = AccentGreen
                    )

                    if (note.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkCard)
                        ) {
                            Text(
                                note,
                                color = AccentYellow,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { detailItem = null },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("بستن", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 13.sp)
        Text(value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun HistoryCard(
    item: AnalysisHistory,
    note: String,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEditNote: () -> Unit
) {
    val color = signalColor(item.signalText)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(GradientBlueStart, GradientBlueEnd)
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("A", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.signalText,
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "تایم فریم: " + item.timeframe + " | اطمینان: " + item.confidence + "%",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = onEditNote) {
                    Icon(Icons.Filled.Edit, "note", tint = AccentYellow, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, "delete", tint = AccentRed, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(10.dp))
            Divider(color = DividerColor)
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SmallStat("ورود", item.entry, AccentBlue)
                SmallStat("حد ضرر", item.stopLoss, AccentRed)
                SmallStat("هدف 1", item.target1, AccentGreen)
            }

            if (note.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Text(
                        note,
                        color = AccentYellow,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                formatDate(item.timestamp),
                color = TextSecondary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun SmallStat(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontSize = 10.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            formatPrice(value),
            color = color,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
    }
}

private fun signalColor(signalText: String): Color {
    return when {
        signalText.contains("STRONG_BUY") || signalText.contains("BUY") -> AccentGreen
        signalText.contains("STRONG_SELL") || signalText.contains("SELL") -> AccentRed
        signalText.contains("خرید") -> AccentGreen
        signalText.contains("فروش") -> AccentRed
        else -> AccentYellow
    }
}

private fun formatPrice(value: Double): String {
    return "%.2f".format(value)
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}