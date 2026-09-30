package com.chartyar.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary

@Composable
fun TermsVersionDialog(
    onAccept: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Text(
                text = "به روزرسانی قوانین",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "شرایط استفاده و حریم خصوصی چارت یار به روز شده است.",
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "لطفا برای ادامه، شرایط جدید را مطالعه و بپذیرید.",
                    color = TextPrimary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    contentColor = TextPrimary
                )
            ) {
                Text(text = "می پذیرم", fontWeight = FontWeight.Bold)
            }
        }
    )
}