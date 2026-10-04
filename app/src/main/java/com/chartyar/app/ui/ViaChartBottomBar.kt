package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chartyar.app.R
import com.chartyar.app.ui.theme.AccentBlue
import com.chartyar.app.ui.theme.DarkSurface
import com.chartyar.app.ui.theme.TextSecondary

@Composable
fun ViaChartBottomBar(
    currentRoute: String,
    onHomeClick: () -> Unit,
    onTrendingClick: () -> Unit,
    onExchangesClick: () -> Unit,
    onAnalysesClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomBarItem(
            icon = Icons.Filled.Home,
            label = stringResource(R.string.bottom_nav_home),
            selected = currentRoute == "home",
            onClick = onHomeClick
        )
        BottomBarItem(
            icon = Icons.Filled.LocalFireDepartment,
            label = stringResource(R.string.bottom_nav_trending),
            selected = currentRoute == "trending",
            onClick = onTrendingClick
        )
        BottomBarItem(
            icon = Icons.Filled.AccountBalance,
            label = stringResource(R.string.bottom_nav_exchanges),
            selected = currentRoute == "exchanges",
            onClick = onExchangesClick
        )
        BottomBarItem(
            icon = Icons.Filled.Insights,
            label = stringResource(R.string.bottom_nav_analyses),
            selected = currentRoute == "analyses",
            onClick = onAnalysesClick
        )
    }
}

@Composable
private fun BottomBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) AccentBlue else TextSecondary
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = tint,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}