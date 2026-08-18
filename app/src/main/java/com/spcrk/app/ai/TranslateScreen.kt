package com.spcrk.app.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spcrk.app.ui.theme.TechCard
import com.spcrk.app.ui.theme.TechPrimaryButton
import com.spcrk.app.ui.theme.TechTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateScreen(
    onBackClick: () -> Unit = {},
    viewModel: TranslateViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLangSelector by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                title = {
                    Text(
                        text = "翻译",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TechCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "源文本",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TechTextField(
                        value = uiState.sourceText,
                        onValueChange = viewModel::updateSourceText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp),
                        placeholder = "请输入要翻译的文本...",
                        singleLine = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TechCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "目标语言",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box {
                        OutlinedButton(
                            onClick = { showLangSelector = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                            )
                        ) {
                            Text(text = uiState.targetLang, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "选择语言",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = showLangSelector,
                            onDismissRequest = { showLangSelector = false }
                        ) {
                            viewModel.languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang, color = MaterialTheme.colorScheme.onSurface) },
                                    onClick = {
                                        viewModel.setTargetLang(lang)
                                        showLangSelector = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TechPrimaryButton(
                text = if (uiState.isTranslating) "翻译中..." else "翻译",
                onClick = { viewModel.translate() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.sourceText.isNotBlank() && !uiState.isTranslating
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.translatedText.isNotEmpty() || uiState.isTranslating) {
                TechCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "翻译结果",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.translatedText.ifEmpty { "..." },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            uiState.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                TechCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠ $error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
