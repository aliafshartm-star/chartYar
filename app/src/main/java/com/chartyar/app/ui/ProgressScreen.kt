package com.chartyar.app.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.ui.theme.*

@Composable
fun ProgressScreen(
    progress: Float,
    statusMessage: String,
    timeframe: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(400),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚙️",
            fontSize = 64.sp
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "در حال تحلیل چارت",
            color = TextPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "تایم‌فریم: $timeframe",
            color = AccentBlue,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(40.dp))

        // نوار پیشرفت
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DarkCard)
        ) {
            LinearProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = AccentBlue,
                trackColor = DarkCard
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "${(animatedProgress * 100).toInt()}%",
            color = TextPrimary,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = statusMessage,
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        // راهنمای مراحل
        ProgressSteps(progress)
    }
}

@Composable
private fun ProgressSteps(progress: Float) {
    val steps = listOf(
        0.2f to "بارگذاری تصویر",
        0.4f to "تشخیص کندل‌ها",
        0.6f to "خوندن قیمت‌ها",
        0.8f to "اجرای اندیکاتورها",
        1.0f to "تشخیص الگوها"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        steps.forEach { (threshold, label) ->
            val isDone = progress >= threshold
            val isActive = progress >= threshold - 0.2f && progress < threshold

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            when {
                                isDone -> AccentGreen
                                isActive -> AccentBlue
                                else -> DarkCard
                            }
                        )
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (isDone) "✅ $label" else label,
                    color = when {
                        isDone -> AccentGreen
                        isActive -> TextPrimary
                        else -> TextSecondary
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}