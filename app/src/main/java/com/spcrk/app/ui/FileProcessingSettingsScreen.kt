package com.spcrk.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.spcrk.app.getAppContainer
import com.spcrk.app.data.SettingsStore
import com.spcrk.app.ui.l10n.appStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileProcessingSettingsScreen(
    onBackClick: () -> Unit
) {
    val settingsStore = getAppContainer(LocalContext.current).settingsStore
    val ocrEngine by settingsStore.ocrEngineFlow.collectAsState(initial = "mlkit")
    val pdfRenderer by settingsStore.pdfRendererFlow.collectAsState(initial = "pdfbox")
    val s = appStrings()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.fileProcessingTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsSection(title = s.pdfProcessing, icon = Icons.Outlined.PictureAsPdf) {
                    Text(
                        s.selectPdfEngine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RadioOption(
                        label = s.pdfBoxRecommended,
                        description = s.pdfBoxDesc,
                        selected = pdfRenderer == "pdfbox",
                        onSelect = { settingsStore.setPdfRenderer("pdfbox") }
                    )
                    RadioOption(
                        label = "MuPDF",
                        description = s.mupdfDesc,
                        selected = pdfRenderer == "mupdf",
                        onSelect = { settingsStore.setPdfRenderer("mupdf") }
                    )
                    RadioOption(
                        label = s.nativePdfRenderer,
                        description = s.nativePdfDesc,
                        selected = pdfRenderer == "native",
                        onSelect = { settingsStore.setPdfRenderer("native") }
                    )
                }
            }

            item {
                SettingsSection(title = s.ocrSettings, icon = Icons.Outlined.DocumentScanner) {
                    Text(
                        s.selectOcrEngine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RadioOption(
                        label = s.mlKitRecommended,
                        description = s.mlKitDesc,
                        selected = ocrEngine == "mlkit",
                        onSelect = { settingsStore.setOcrEngine("mlkit") }
                    )
                    RadioOption(
                        label = "PaddleOCR",
                        description = s.paddleOcrDesc,
                        selected = ocrEngine == "paddle",
                        onSelect = { settingsStore.setOcrEngine("paddle") }
                    )
                    RadioOption(
                        label = "Tesseract",
                        description = s.tesseractDesc,
                        selected = ocrEngine == "tesseract",
                        onSelect = { settingsStore.setOcrEngine("tesseract") }
                    )
                }
            }

            item {
                SettingsSection(title = s.documentParsing, icon = Icons.Outlined.Description) {
                    SwitchSettingRow(
                        title = s.autoChunking,
                        description = s.autoChunkingDesc,
                        checked = true,
                        onCheckedChange = { }
                    )
                    SwitchSettingRow(
                        title = s.extractImages,
                        description = s.extractImagesDesc,
                        checked = true,
                        onCheckedChange = { }
                    )
                    SwitchSettingRow(
                        title = s.preserveFormat,
                        description = s.preserveFormatDesc,
                        checked = false,
                        onCheckedChange = { }
                    )
                }
            }

            item {
                SettingsSection(title = s.fileLimits, icon = Icons.Outlined.DataUsage) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(s.maxFileSize, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                s.maxFileSizeDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Text("10 MB", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(s.maxFileCount, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                s.maxFileCountDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Text("20", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun RadioOption(
    label: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SwitchSettingRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
