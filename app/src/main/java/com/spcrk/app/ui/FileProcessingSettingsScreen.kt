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
import androidx.compose.ui.unit.dp
import com.spcrk.app.data.SettingsStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileProcessingSettingsScreen(
    onBackClick: () -> Unit
) {
    val settingsStore = remember { SettingsStore.getInstance() }
    val ocrEngine by settingsStore.ocrEngineFlow.collectAsState(initial = "mlkit")
    val pdfRenderer by settingsStore.pdfRendererFlow.collectAsState(initial = "pdfbox")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("文件处理") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsSection(title = "PDF 处理", icon = Icons.Outlined.PictureAsPdf) {
                    Text(
                        "选择 PDF 解析引擎",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RadioOption(
                        label = "PdfBox (推荐)",
                        description = "Apache PdfBox，解析精度高",
                        selected = pdfRenderer == "pdfbox",
                        onSelect = { settingsStore.setPdfRenderer("pdfbox") }
                    )
                    RadioOption(
                        label = "MuPDF",
                        description = "轻量级，解析速度快",
                        selected = pdfRenderer == "mupdf",
                        onSelect = { settingsStore.setPdfRenderer("mupdf") }
                    )
                    RadioOption(
                        label = "系统原生",
                        description = "使用系统内置 PDF 渲染",
                        selected = pdfRenderer == "native",
                        onSelect = { settingsStore.setPdfRenderer("native") }
                    )
                }
            }

            item {
                SettingsSection(title = "OCR 设置", icon = Icons.Outlined.DocumentScanner) {
                    Text(
                        "选择 OCR 识别引擎",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    RadioOption(
                        label = "ML Kit (推荐)",
                        description = "Google ML Kit，支持多语言",
                        selected = ocrEngine == "mlkit",
                        onSelect = { settingsStore.setOcrEngine("mlkit") }
                    )
                    RadioOption(
                        label = "PaddleOCR",
                        description = "百度 PaddleOCR，中文识别优秀",
                        selected = ocrEngine == "paddle",
                        onSelect = { settingsStore.setOcrEngine("paddle") }
                    )
                    RadioOption(
                        label = "Tesseract",
                        description = "开源 OCR，支持 100+ 语言",
                        selected = ocrEngine == "tesseract",
                        onSelect = { settingsStore.setOcrEngine("tesseract") }
                    )
                }
            }

            item {
                SettingsSection(title = "文档解析", icon = Icons.Outlined.Description) {
                    SwitchSettingRow(
                        title = "自动分块",
                        description = "长文档自动分块处理",
                        checked = true,
                        onCheckedChange = { }
                    )
                    SwitchSettingRow(
                        title = "提取图片",
                        description = "从文档中提取嵌入的图片",
                        checked = true,
                        onCheckedChange = { }
                    )
                    SwitchSettingRow(
                        title = "保留格式",
                        description = "保留原始文档格式信息",
                        checked = false,
                        onCheckedChange = { }
                    )
                }
            }

            item {
                SettingsSection(title = "文件限制", icon = Icons.Outlined.DataUsage) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("最大文件大小", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "单个文件上传限制",
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
                            Text("最大文件数", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "单次上传文件数量",
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
