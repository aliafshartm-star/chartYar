package com.chartyar.app.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary

@Composable
fun RateAppDialog(
 }
    onRate: () -> Unit,
    onDontAsk: () -> Unit,
    onLater: () -> Unit
) {
    AlertDialog(
           onDismissRequest = onLater,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title )
 = {
            Text(
                "🌟 از چارت‌یار راضی هستی؟",
                fontWeight = FontWeight.Bold
            )
        },
        text}
 = {
            Column {
                Text(
                    "اگه از اپ لذت بردی، لطفاً با ی```

ه امتیاز ۵ ستاره از ما حمایت کن. این به ما کمک می**‌کنه بهتر بشیم!",
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "⭐⭐⭐⭐⭐",
                    fontSize = 24.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onRate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    contentColor = TextPrimary
                )
            ) {
                Text("امتیاز می‌دم", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onLater) {
                Text("بعداً", color = TextSecondary)
            }
       Commit changes** → دوباره **Commit changes**

---

## 🎯 حالا `MainActivity.kt` رو آپدیت کن

فقط این خطوط رو اضافه کن:

**۱. import:**
```kotlin
import com.chartyar.app.data.RateAppManager