package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.data.AnalysisHistory
import com.chartyar.app.data.NoteManager
import com.chartyar.app.ui.theme.*
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
                Icon(Icons.Filled.ArrowBack, "بازگشت", tint = TextPrimary)
            }
            Text(
                "تاریخچه تحلیل‌ها",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (items.isNotEmpty()) {
                IconButton(onClick = onClearAll) {
                    Icon(Icons.Filled.Delete, "پاک کردن همه", tint = AccentRed)
                }
            }
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📭", fontSize = 48.sp)
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

    // دیالوگ ویرایش یادداشت
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
                        editingItem?.symbol() ?: "",
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
                                "مثلاً: ورود زدم در ۶۷,۵۰۰ | حد ضرر ۶۶,۰۰۰",
                                color = TextSecondary
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentBlue,
                            unfocusedBorderColor = Divider
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
                        contentColor = TextPrimary
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
}

@Composable
private fun HistoryCard(
    item: AnalysisHistory,
    note: String,
    onDelete: () -> Unit,
    onEditNote: () -> Unit
) {
    val signalColor = when {
        item.signalText.contains("خرید") -> AccentGreen
        item.signalText.contains("فروش") -> AccentRed
        else -> AccentYellow
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
                        item.signalText,
                        color = signalColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "تایم‌فریم: ${item.timeframe} | اطمینان: ${item.confidence}%",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                IconButton(onClick = onEditNote) {
                    Icon(Icons.Filled.Edit, "یادداشت", tint = AccentYellow)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, "حذف", tint = AccentRed)
                }
            }

            Spacer(Modifier.height(8.dp))
            Divider(color = Divider)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SmallStat("ورود", item.entry, AccentBlue)
                SmallStat("حد ضرر", item.stopLoss, AccentRed)
                SmallStat("هدف ۱", item.target1, AccentGreen)
            }

            // یادداشت (اگه وجود داره)
            if (note.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Text(
                        "📝 $note",
                        color = AccentYellow,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                formatDate(item.timestamp),
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SmallStat(label: String, value: Double, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Text(
            "%.2f".format(value),
            color = color,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun AnalysisHistory.symbol(): String {
    return "تحلیل $timeframe — $signalText"
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}