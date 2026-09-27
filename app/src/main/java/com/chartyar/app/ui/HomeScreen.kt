package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chartyar.app.ui.theme.*

@Composable
fun HomeScreen(
    onUploadClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "چارت‌یار",
                style = MaterialTheme.typography.headlineLarge,
                color = AccentBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { }) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = "درباره",
                    tint = TextSecondary
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "تحلیل حرفه‌ای چارت با یک کلیک",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )

        Spacer(Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "شروع تحلیل",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "اسکرین‌شات چارت خود را از گالری انتخاب کنید",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onUploadClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "انتخاب تصویر چارت",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        OutlinedButton(
            onClick = onHistoryClick,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
        ) {
            Text("تاریخچه تحلیل‌ها", style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "این اپ توصیه مالی نیست | تحلیل صرفاً جهت کمک به تصمیم‌گیری",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}