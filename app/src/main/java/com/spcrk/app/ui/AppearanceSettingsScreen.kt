package com.spcrk.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import com.spcrk.app.ui.theme.techRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spcrk.app.getAppContainer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    onBackClick: () -> Unit
) {
    val settingsStore = getAppContainer(LocalContext.current).settingsStore
    val s = com.spcrk.app.ui.l10n.appStrings()
    val themeMode by settingsStore.themeModeFlow.collectAsState(initial = "system")
    val fontSize by settingsStore.fontSizeFlow.collectAsState(initial = 14)
    val language by settingsStore.languageFlow.collectAsState(initial = "zh")
    val zoomFactor by settingsStore.zoomFactorFlow.collectAsState(initial = 1.0f)
    val showColorPicker by settingsStore.showColorPicker.collectAsState(initial = false)
    val selectedColor by settingsStore.selectedColorFlow.collectAsState(initial = 0xFF00BCD4)
    val background by settingsStore.backgroundFlow.collectAsState(initial = "bg1")

    val context = LocalContext.current
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> pickBackgroundImage(context, uri, settingsStore) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.appearanceTitle) },
                navigationIcon = {
                    IconButton(onClick = {},
modifier = Modifier.techRipple(onClick = onBackClick)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = s.back)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = s.themeMode,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ThemeModeOption(
                            label = s.followSystem,
                            selected = themeMode == "system",
                            onClick = { settingsStore.setThemeMode("system") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ThemeModeOption(
                            label = s.lightTheme,
                            selected = themeMode == "light",
                            onClick = { settingsStore.setThemeMode("light") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ThemeModeOption(
                            label = s.darkTheme,
                            selected = themeMode == "dark",
                            onClick = { settingsStore.setThemeMode("dark") }
                        )
                    }
                }
            }

            item {
                Text(
                    text = s.backgroundTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        BackgroundOption(s.bgNebula1, "bg1", background) { settingsStore.setBackground(it) }
                        BackgroundOption(s.bgNebula2, "bg2", background) { settingsStore.setBackground(it) }
                        BackgroundOption(s.bgNebula3, "bg3", background) { settingsStore.setBackground(it) }
                        BackgroundOption(s.bgCustom, "custom", background) { settingsStore.setBackground(it) }
                        if (background == "custom") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { pickImageLauncher.launch("image/*") },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Outlined.Image, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(s.uploadBackground)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = s.themeColor,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .techRipple(onClick = { settingsStore.setShowColorPicker(!showColorPicker) }),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(s.currentColor, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(selectedColor))
                        )
                    }
                }
                if (showColorPicker) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ColorPickerGrid(
                        selectedColor = selectedColor,
                        onColorSelected = { settingsStore.setSelectedColor(it) }
                    )
                }
            }

            item {
                Text(
                    text = s.fontSize,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("A", style = MaterialTheme.typography.bodySmall)
                            Slider(
                                value = fontSize.toFloat(),
                                onValueChange = { settingsStore.setFontSize(it.toInt()) },
                                valueRange = 10f..24f,
                                steps = 13,
                                modifier = Modifier.weight(1f)
                            )
                            Text("A", style = MaterialTheme.typography.titleLarge)
                        }
                        Text(
                            text = String.format(s.fontSizeCurrentFormat, fontSize),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            item {
                Text(
                    text = s.uiZoom,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Slider(
                            value = zoomFactor,
                            onValueChange = { settingsStore.setZoomFactor(it) },
                            valueRange = 0.5f..2.0f,
                            steps = 9
                        )
                        Text(
                            text = String.format(s.zoomCurrentFormat, (zoomFactor * 100).toInt()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            item {
                Text(
                    text = s.languageSettings,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LanguageOption(s.zhSimplified, "zh", language) { settingsStore.setLanguage(it) }
                        LanguageOption(s.zhTraditional, "zh-TW", language) { settingsStore.setLanguage(it) }
                        LanguageOption(s.english, "en", language) { settingsStore.setLanguage(it) }
                        LanguageOption(s.japanese, "ja", language) { settingsStore.setLanguage(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeModeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .techRipple(onClick = { onClick() })
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BackgroundOption(
    label: String,
    key: String,
    current: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .techRipple(onClick = { onSelect(key) })
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = current == key,
            onClick = { onSelect(key) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun pickBackgroundImage(
    context: android.content.Context,
    uri: android.net.Uri?,
    settingsStore: com.spcrk.app.data.SettingsStore
) {
    if (uri == null) return
    val file = File(context.filesDir, "background_custom_${System.currentTimeMillis()}.png")
    val copied = runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } != null
    }.getOrDefault(false)
    if (copied) {
        settingsStore.setCustomBackgroundUri(android.net.Uri.fromFile(file).toString())
        settingsStore.setBackground("custom")
    }
}

@Composable
private fun LanguageOption(
    label: String,
    code: String,
    currentLanguage: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .techRipple(onClick = { onSelect(code) })
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = currentLanguage == code,
            onClick = { onSelect(code) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ColorPickerGrid(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit
) {
    val colors = listOf(
        0xFF00BCD4, 0xFF2196F3, 0xFF3F51B5, 0xFF9C27B0, 0xFFE91E63,
        0xFFF44336, 0xFFFF9800, 0xFFFFC107, 0xFF4CAF50, 0xFF009688,
        0xFF795548, 0xFF607D8B, 0xFF000000, 0xFFFFFFFF
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            colors.chunked(5).forEach { rowColors ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    rowColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .then(
                                    if (color == selectedColor) {
                                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    } else {
                                        Modifier.border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                                    }
                                )
                                .techRipple(onClick = { onColorSelected(color) })
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
