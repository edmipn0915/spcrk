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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    onBackClick: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val settingsStore = getAppContainer(LocalContext.current).settingsStore
    val themeMode by settingsStore.themeModeFlow.collectAsState(initial = "system")
    val fontSize by settingsStore.fontSizeFlow.collectAsState(initial = 14)
    val language by settingsStore.languageFlow.collectAsState(initial = "zh")
    val zoomFactor by settingsStore.zoomFactorFlow.collectAsState(initial = 1.0f)
    val showColorPicker by settingsStore.showColorPicker.collectAsState(initial = false)
    val selectedColor by settingsStore.selectedColorFlow.collectAsState(initial = 0xFF00BCD4)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("外观设置") },
                navigationIcon = {
                    IconButton(onClick = {},
modifier = Modifier.techRipple(onClick = onBackClick)) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
                    text = "主题模式",
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
                            label = "跟随系统",
                            selected = themeMode == "system",
                            onClick = { settingsStore.setThemeMode("system") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ThemeModeOption(
                            label = "亮色主题",
                            selected = themeMode == "light",
                            onClick = {
                                settingsStore.setThemeMode("light")
                                onToggleTheme(false)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ThemeModeOption(
                            label = "暗色主题",
                            selected = themeMode == "dark",
                            onClick = {
                                settingsStore.setThemeMode("dark")
                                onToggleTheme(true)
                            }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "主题颜色",
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
                        Text("当前颜色", style = MaterialTheme.typography.bodyMedium)
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
                    text = "字体大小",
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
                            text = "当前: ${fontSize}sp",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "界面缩放",
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
                            text = "当前: ${(zoomFactor * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "语言设置",
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
                        LanguageOption("简体中文", "zh", language) { settingsStore.setLanguage(it) }
                        LanguageOption("English", "en", language) { settingsStore.setLanguage(it) }
                        LanguageOption("日本語", "ja", language) { settingsStore.setLanguage(it) }
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
