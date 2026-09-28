package com.chartyar.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chartyar.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    autoSaveHistory: Boolean,
    showWarnings: Boolean,
    defaultTimeframe: String,
    theme: String,
    onBack: () -> Unit,
    onShowTutorial: () -> Unit,
    onShowPrivacy: () -> Unit,
    onShowTerms: () -> Unit,
    onAutoSaveChange: (Boolean) -> Unit,
    onShowWarningsChange: (Boolean) -> Unit,
    onDefaultTimeframeChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onResetDisclaimer: () -> Unit,
    onClearHistory: () -> Unit,
    onResetTutorial: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "بازگشت", tint = TextPrimary)
            }
            Text(
                "تنظیمات",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
        SectionTitle("⚙️ تحلیل")
        SettingSwitch(
            title = "ذخیره خودکار در تاریخچه",
            subtitle = "هر تحلیل به‌طور خودکار ذخیره شود",
            checked = autoSaveHistory,
            onCheckedChange = onAutoSaveChange
        )
        SettingSwitch(
            title = "نمایش هشدارها",
            subtitle = "هشدارهای تحلیلی نمایش داده شود",
            checked = showWarnings,
            onCheckedChange = onShowWarningsChange
        )
        Spacer(Modifier.height(12.dp))
        TimeframeDropdown(
            current = defaultTimeframe,
            onSelect = onDefaultTimeframeChange
        )
        Spacer(Modifier.height(20.dp))
        SectionTitle("🎨 ظاهر")
        ThemeSelector(current = theme, onSelect = onThemeChange)
        Spacer(Modifier.height(20.dp))
        SectionTitle("📚 راهنما")
        DangerButton(
            title = "مشاهده راهنمای استفاده",
            subtitle = "آموزش ۴ مرحله‌ای چطور از اپ استفاده کنی",
            color = AccentBlue,
            onClick = onShowTutorial
        )
        Spacer(Modifier.height(20.dp))
        SectionTitle("📄 قوانین")
        DangerButton(
            title = "حریم خصوصی",
            subtitle = "سیاست حفظ حریم خصوصی کاربران",
            color = AccentBlue,
            onClick = onShowPrivacy
        )
        Spacer(Modifier.height(8.dp))
        DangerButton(
            title = "شرایط استفاده",
            subtitle = "قوانین و شرایط استفاده از اپ",
            color = AccentBlue,
            onClick = onShowTerms
        )
        Spacer(Modifier.height(20.dp))
        SectionTitle("🗄️ داده‌ها")
        DangerButton(
            title = "پاک کردن تاریخچه",
            subtitle = "همه تحلیل‌های ذخیره‌شده حذف می‌شوند",
            color = AccentYellow,
            onClick = onClearHistory
        )
        Spacer(Modifier.height(8.dp))
        DangerButton(
            title = "نمایش مجدد سلب مسئولیت",
            subtitle = "در اجرای بعدی صفحه شرایط نمایش داده می‌شود",
            color = AccentYellow,
            onClick = onResetDisclaimer
        )
        Spacer(Modifier.height(8.dp))
        DangerButton(
            title = "نمایش مجدد راهنما",
            subtitle = "در اجرای بعدی صفحه راهنما نمایش داده می‌شود",
            color = AccentYellow,
            onClick = onResetTutorial
        )
        Spacer(Modifier.height(20.dp))
        SectionTitle("ℹ️ درباره")
        InfoCard(
            title = "چارت‌یار",
            lines = listOf(
                "نسخه: ۱.۰.۰",
                "ابزار تحلیل تکنیکال چارت",
                "موتور: ۱۰+ اندیکاتور، ۱۱ الگو، ۴ استایل ترید",
                "بدون توصیه مالی — فقط کمک به تصمیم‌گیری"
            )
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = AccentBlue,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextPrimary,
                    checkedTrackColor = AccentGreen,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = DarkCard
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeframeDropdown(current: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val timeframes = listOf("1m", "5m", "15m", "30m", "1H", "4H", "1D", "1W", "1M")
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("تایم‌فریم پیش‌فرض", color = TextPrimary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = current,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null, tint = TextSecondary) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Divider,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = DarkSurface
                ) {
                    timeframes.forEach { tf ->
                        DropdownMenuItem(
                            text = { Text(tf, color = TextPrimary) },
                            onClick = {
                                onSelect(tf)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSelector(current: String, onSelect: (String) -> Unit) {
    val themes = listOf("dark" to "دارک (پیش‌فرض)", "light" to "روشن")
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("تم برنامه", color = TextPrimary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            themes.forEach { (key, label) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(key) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = current == key,
                        onClick = { onSelect(key) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = AccentGreen,
                            unselectedColor = TextSecondary
                        )
                    )
                    Text(label, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun DangerButton(
    title: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, color = color, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun InfoCard(title: String, lines: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, color = AccentBlue, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            lines.forEach { line ->
                Text(
                    "•  $line",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}