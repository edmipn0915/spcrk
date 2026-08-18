package com.spcrk.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.spcrk.app.ui.theme.techRipple

data class SettingsCategory(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onBackClick: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit
) {
    val categories = listOf(
        SettingsCategory("外观", "主题、颜色、字体、语言、缩放", Icons.Outlined.Palette, "settings/appearance"),
        SettingsCategory("AI 提供商", "API Key、Base URL、模型同步", Icons.Outlined.Cloud, "settings/providers"),
        SettingsCategory("模型设置", "默认模型、快速模型、翻译模型", Icons.Outlined.Psychology, "settings/model-config"),
        SettingsCategory("MCP 服务器", "服务器管理、工具、资源、日志", Icons.Outlined.Hub, "settings/mcp"),
        SettingsCategory("Skill 管理", "安装和管理 Skill 扩展", Icons.Outlined.Extension, "settings/skills"),
        SettingsCategory("数据管理", "备份、导入导出、云存储", Icons.Outlined.Storage, "settings/data"),
        SettingsCategory("依赖设置", "Python、Node.js、本地模型", Icons.Outlined.Code, "settings/dependencies"),
        SettingsCategory("本地模型", "嵌入模型、GGUF 下载、本地推理", Icons.Outlined.Memory, "settings/local-models"),
        SettingsCategory("文件处理", "PDF 解析、OCR 设置", Icons.Outlined.Description, "settings/file-processing"),
        SettingsCategory("搜索设置", "搜索引擎、API Key、自定义实例", Icons.Outlined.Search, "settings/search"),
        SettingsCategory("用量统计", "Token 用量和费用统计", Icons.Outlined.BarChart, "settings/stats"),
        SettingsCategory("定时任务", "管理定时执行的任务", Icons.Outlined.Schedule, "settings/schedules"),
        SettingsCategory("通知", "通知开关和类型配置", Icons.Outlined.Notifications, "settings/notifications"),
        SettingsCategory("关于", "版本信息和开源许可", Icons.Outlined.Info, "settings/about")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "设置",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                SettingsCategoryCard(
                    category = category,
                    onClick = { navController.navigate(category.route) }
                )
            }
        }
    }
}

@Composable
private fun SettingsCategoryCard(
    category: SettingsCategory,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .techRipple(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
