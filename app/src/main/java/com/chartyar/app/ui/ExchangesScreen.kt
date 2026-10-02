package com.chartyar.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.R
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.AccentGreen
import com.chartyar.app.ui.theme.DarkBg
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.Divider
import com.chartyar.app.ui.theme.TextPrimary
import com.chartyar.app.ui.theme.TextSecondary

data class Exchange(
    val id: String,
    val nameFa: String,
    val nameEn: String,
    val description: String,
    val referralUrl: String,
    val referralCode: String,
    val brandColor: Color
)

private val exchanges = listOf(
    Exchange(
        id = "nobitex",
        nameFa = "نوبیتکس",
        nameEn = "Nobitex",
        description = "بزرگ‌ترین صرافی ایرانی",
        referralUrl = "https://nobitex.ir/signup/?refcode=5CA6F10",
        referralCode = "5CA6F10",
        brandColor = AccentBlue
    ),
    Exchange(
        id = "kcex",
        nameFa = "KCEX",
        nameEn = "KCEX",
        description = "صرافی بین‌المللی با کارمزد پایین",
        referralUrl = "https://www.kcex.com/register?inviteCode=0N0V0M",
        referralCode = "0N0V0M",
        brandColor = AccentGreen
    )
)

private fun openExchangeUrl(context: Context, url: String, errorMsg: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
    }
}

private fun copyReferralCode(context: Context, code: String, successMsg: String, failMsg: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Referral Code", code)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, successMsg, Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, failMsg, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ExchangesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val linkFailedMsg = stringResource(R.string.exchanges_link_failed)
    val copySuccessMsg = stringResource(R.string.exchanges_copy_success)
    val copyFailedMsg = stringResource(R.string.exchanges_copy_failed)

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
                stringResource(R.string.exchanges_title),
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Text(
                stringResource(R.string.exchanges_subtitle),
                color = TextSecondary,
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(exchanges) { exchange ->
                ExchangeCard(
                    exchange = exchange,
                    onOpen = {
                        openExchangeUrl(context, exchange.referralUrl, linkFailedMsg)
                    },
                    onCopy = {
                        copyReferralCode(context, exchange.referralCode, copySuccessMsg, copyFailedMsg)
                    }
                )
            }
        }
    }
}

@Composable
private fun ExchangeCard(
    exchange: Exchange,
    onOpen: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = exchange.brandColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = exchange.nameEn.take(1).uppercase(),
                        color = exchange.brandColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        exchange.nameFa,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        exchange.description,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = DarkBg,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.exchanges_referral_code),
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                    Text(
                        exchange.referralCode,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                IconButton(onClick = onCopy) {
                    Icon(
                        Icons.Filled.ContentCopy,
                        contentDescription = "copy",
                        tint = exchange.brandColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = exchange.brandColor,
                    contentColor = TextPrimary
                )
            ) {
                Icon(
                    Icons.Filled.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.exchanges_register) + " " + exchange.nameFa,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}