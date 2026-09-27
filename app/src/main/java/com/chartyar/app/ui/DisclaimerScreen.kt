package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.ui.theme.*

@Composable
fun DisclaimerScreen(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    var checked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = AccentYellow,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "سلب مسئولیت",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }

        Divider(color = Divider, thickness = 1.dp)

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            DisclaimerText()
        }

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { checked = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = AccentGreen,
                    uncheckedColor = TextSecondary,
                    checkmarkColor = TextPrimary
                )
            )
            Text(
                text = "شرایط را خوانده و می‌پذیرم",
                color = TextPrimary,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDecline,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AccentRed
                )
            ) {
                Text("رد و خروج")
            }

            Button(
                onClick = onAccept,
                enabled = checked,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen,
                    contentColor = TextPrimary,
                    disabledContainerColor = DarkCard,
                    disabledContentColor = TextSecondary
                )
            ) {
                Text("قبول و ورود", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DisclaimerText() {
    val paragraphs = listOf(
        "این نرم‌افزار فقط یک ابزار تحلیل تکنیکال است و هیچ‌گونه توصیه مالی، سرمایه‌گذاری یا سیگنال تضمینی ارائه نمی‌دهد.",
        "تمامی تحلیل‌ها صرفاً جنبه آموزشی و اطلاعاتی دارند و ممکن است با تحلیل‌های شخصی شما متفاوت باشد.",
        "تصمیم نهایی خرید و فروش، کاملاً بر عهده کاربر است و مسئولیت هرگونه سود یا ضرر مالی تنها متوجه کاربر می‌باشد.",
        "بازارهای مالی پر از ریسک هستند و ممکن است سرمایه شما از دست برود. فقط با پولی معامله کنید که توان از دست دادنش را دارید.",
        "تحلیل‌های تولیدشده توسط این نرم‌افزار جایگزین مشاوره تخصصی مالی نیستند. در صورت نیاز، با یک مشاور مالی معتبر مشورت کنید.",
        "استفاده از این نرم‌افزار به معنی پذیرش کامل این شرایط است."
    )

    paragraphs.forEach { p ->
        Text(
            text = "•  $p",
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 26.sp,
            modifier = Modifier.padding(vertical = 6.dp)
        )
    }
}