package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentRed
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.GradientBlueStart
import com.chartyar.app.ui.theme.GradientPurpleEnd
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary

@Composable
fun AgeScreen(
    onConfirm: () -> Unit,
    onDecline: () -> Unit
) {
    var checkedAge by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    Brush.linearGradient(
                        listOf(GradientBlueStart, GradientPurpleEnd)
                    ),
                    RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("🔒", fontSize = 60.sp)
        }

        Spacer(Modifier.height(28.dp))

        Text(
            text = "تأیید سن قانونی",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "استفاده از ViaChart فقط برای افراد ۱۸ سال یا بیشتر مجاز است",
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkSurface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { checkedAge = !checkedAge }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = checkedAge,
                    onCheckedChange = { checkedAge = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AccentBlue,
                        uncheckedColor = TextSecondary,
                        checkmarkColor = Color.White
                    )
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "۱۸ سال یا بیشتر دارم و مسئولیت را می‌پذیرم",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onConfirm,
            enabled = checkedAge,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = Color.White,
                disabledContainerColor = DarkSurface,
                disabledContentColor = TextSecondary
            )
        ) {
            Text(
                text = "تأیید و ورود",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = onDecline,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "خروج از اپلیکیشن",
                color = AccentRed,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.weight(1f))
    }
}