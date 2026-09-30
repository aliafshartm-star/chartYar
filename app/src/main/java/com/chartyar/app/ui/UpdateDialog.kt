package com.chartyar.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chartyar.app.data.UpdateChecker
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary

@Composable
fun UpdateDialog(
    updateInfo: UpdateChecker.UpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Text(
                "🚀 نسخه جدید موجوده!",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    "نسخه فعلی: ${UpdateChecker.CURRENT_VERSION}",
                    color = TextSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "نسخه جدید: ${updateInfo.latestVersion}",
                    color = AccentGreen,
                    fontWeight = FontWeight.Bold
                )

                if (updateInfo.releaseNotes.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "تغییرات:",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    val notes = if (updateInfo.releaseNotes.length > 500) {
                        updateInfo.releaseNotes.take(500) + "..."
                    } else {
                        updateInfo.releaseNotes
                    }
                    Text(notes, color = TextSecondary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(updateInfo.downloadUrl)
                        )
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        // silent
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    contentColor = TextPrimary
                )
            ) {
                Text("دانلود", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("بعداً", color = TextSecondary)
            }
        }
    )
}