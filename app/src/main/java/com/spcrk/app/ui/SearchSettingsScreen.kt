package com.spcrk.app.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.spcrk.app.ai.SearchConfig
import com.spcrk.app.data.SettingsStore
import com.spcrk.app.ui.theme.TechCard

data class SearchEngineInfo(
    val id: String,
    val name: String,
    val apiEndpoint: String,
    val websiteUrl: String,
    val requiresApiKey: Boolean,
    val requiresBaseUrl: Boolean
)

val searchEngines = listOf(
    SearchEngineInfo("duckduckgo", "DuckDuckGo", "https://html.duckduckgo.com/html/", "", false, false),
    SearchEngineInfo("bing", "Bing", "https://www.bing.com/search", "", false, false),
    SearchEngineInfo("tavily", "Tavily", "https://api.tavily.com/search", "https://tavily.com/", true, false),
    SearchEngineInfo("searxng", "SearXNG", "自定义实例", "https://searxng.org/", false, true),
    SearchEngineInfo("exa", "Exa API", "https://api.exa.ai/search", "https://exa.ai/", true, false),
    SearchEngineInfo("exa-mcp", "Exa MCP（免费）", "https://mcp.exa.ai/mcp", "https://github.com/exa-labs/exa-mcp-server", true, false),
    SearchEngineInfo("bocha", "Bocha", "https://api.bocha.cn/v1/web-search", "https://bocha.cn/", true, false),
    SearchEngineInfo("jina", "Jina", "https://s.jina.ai/", "https://jina.ai/", false, false),
    SearchEngineInfo("zhipu", "智谱 (Zhipu)", "https://open.bigmodel.cn/api/paas/v4/tools", "https://open.bigmodel.cn/", true, false),
    SearchEngineInfo("firecrawl", "Firecrawl", "https://api.firecrawl.dev/v1/scrape", "https://firecrawl.dev/", true, false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSettingsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val settingsStore = remember { SettingsStore.getInstance() }

    var config by remember {
        mutableStateOf(
            SearchConfig(
                engine = settingsStore.searchEngineFlow.value,
                apiKey = settingsStore.searchApiKeyFlow.value,
                baseUrl = settingsStore.searchBaseUrlFlow.value,
                maxResults = settingsStore.searchMaxResultsFlow.value,
                urlContentProvider = settingsStore.urlContentProviderFlow.value
            )
        )
    }

    var engineExpanded by remember { mutableStateOf(false) }
    var showApiKey by remember { mutableStateOf(false) }

    val currentEngineInfo = searchEngines.find { it.id == config.engine } ?: searchEngines[0]

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("搜索设置") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "搜索引擎",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            TechCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    ExposedDropdownMenuBox(
                        expanded = engineExpanded,
                        onExpandedChange = { engineExpanded = !engineExpanded }
                    ) {
                        OutlinedTextField(
                            value = currentEngineInfo.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("选择引擎") },
                            leadingIcon = {
                                Icon(Icons.Outlined.Search, contentDescription = null)
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = engineExpanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = engineExpanded,
                            onDismissRequest = { engineExpanded = false }
                        ) {
                            searchEngines.forEach { engine ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(engine.name, style = MaterialTheme.typography.bodyLarge)
                                            Text(
                                                engine.apiEndpoint,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        }
                                    },
                                    onClick = {
                                        config = config.copy(engine = engine.id)
                                        settingsStore.setSearchEngine(engine.id)
                                        engineExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (currentEngineInfo.websiteUrl.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentEngineInfo.websiteUrl))
                                    context.startActivity(intent)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "访问 ${currentEngineInfo.name} 官网",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            Text(
                "API 配置",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            TechCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (currentEngineInfo.requiresApiKey) {
                        OutlinedTextField(
                            value = config.apiKey,
                            onValueChange = {
                                config = config.copy(apiKey = it)
                                settingsStore.setSearchApiKey(it)
                            },
                            label = { Text("API Key") },
                            placeholder = { Text("请输入 ${currentEngineInfo.name} API Key") },
                            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            trailingIcon = {
                                TextButton(onClick = { showApiKey = !showApiKey }) {
                                    Text(if (showApiKey) "隐藏" else "显示")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "API 地址: ${currentEngineInfo.apiEndpoint}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        if (currentEngineInfo.id == "exa-mcp") {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Exa MCP 为免费搜索服务，去 dashboard.exa.ai/api-keys 免费注册获取 API Key，无需付费",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else if (currentEngineInfo.requiresBaseUrl) {
                        OutlinedTextField(
                            value = config.baseUrl,
                            onValueChange = {
                                config = config.copy(baseUrl = it)
                                settingsStore.setSearchBaseUrl(it)
                            },
                            label = { Text("SearXNG 实例地址") },
                            placeholder = { Text("https://your-searxng-instance.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "请输入你的 SearXNG 实例完整地址",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    } else {
                        Text(
                            "${currentEngineInfo.name} 不需要 API Key",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Text(
                "搜索参数",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            TechCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "最大结果数: ${config.maxResults}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = config.maxResults.toFloat(),
                        onValueChange = {
                            val newVal = it.toInt()
                            config = config.copy(maxResults = newVal)
                            settingsStore.setSearchMaxResults(newVal)
                        },
                        valueRange = 1f..10f,
                        steps = 8,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text("10", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }

            Text(
                "URL 内容获取",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            TechCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "从网页提取正文的方式",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val urlProviders = listOf(
                        Triple("builtin", "内置解析", "Jsoup 本地解析，无需 API Key"),
                        Triple("jina", "Jina AI", "https://r.jina.ai/ 高质量提取"),
                        Triple("firecrawl", "Firecrawl", "API 调用，需 API Key")
                    )
                    urlProviders.forEach { (id, name, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    config = config.copy(urlContentProvider = id)
                                    settingsStore.setUrlContentProvider(id)
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = config.urlContentProvider == id,
                                onClick = {
                                    config = config.copy(urlContentProvider = id)
                                    settingsStore.setUrlContentProvider(id)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jina.ai/"))
                                context.startActivity(intent)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Jina AI 官网",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://firecrawl.dev/"))
                                context.startActivity(intent)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Firecrawl 官网",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Text(
                "支持的引擎",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            TechCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    searchEngines.forEach { engine ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (engine.websiteUrl.isNotEmpty()) {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(engine.websiteUrl))
                                        context.startActivity(intent)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    engine.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    engine.apiEndpoint,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                            if (engine.requiresApiKey) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        "需API Key",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (engine.requiresBaseUrl) {
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        "需实例",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        "免费",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        if (engine != searchEngines.last()) {
                            Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun VisualTransformation.Companion.None(): VisualTransformation {
    return VisualTransformation.None
}
