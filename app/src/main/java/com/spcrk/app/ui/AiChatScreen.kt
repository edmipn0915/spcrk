package com.spcrk.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spcrk.app.ui.navigation.Screen
import com.spcrk.app.ui.theme.TechCard
import com.spcrk.app.ui.theme.techRipple

@Composable
fun AiChatScreen(navController: NavController) {
    val features = listOf(
        AiFeature(
            id = "chat",
            title = "对话",
            description = "与AI助手智能对话",
            iconEmoji = "💬",
            route = Screen.Chat.route
        ),
        AiFeature(
            id = "translate",
            title = "翻译",
            description = "多语言互译助手",
            iconEmoji = "🌐",
            route = Screen.Translate.route
        ),
        AiFeature(
            id = "code",
            title = "代码助手",
            description = "编程问题解答",
            iconEmoji = "💻",
            route = Screen.CodeAssistant.route
        ),
        AiFeature(
            id = "notes",
            title = "笔记",
            description = "记录和管理笔记",
            iconEmoji = "📝",
            route = Screen.Notes.route
        ),
        AiFeature(
            id = "search",
            title = "搜索",
            description = "全局搜索功能",
            iconEmoji = "🔍",
            route = Screen.Search.route
        ),
        AiFeature(
            id = "ocr",
            title = "OCR识别",
            description = "图片文字识别",
            iconEmoji = "📷",
            route = Screen.Ocr.route
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "AI 助手",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        features.forEach { feature ->
            AiFeatureCard(
                feature = feature,
                onClick = { navController.navigate(feature.route) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private data class AiFeature(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val route: String
)

@Composable
private fun AiFeatureCard(
    feature: AiFeature,
    onClick: () -> Unit
) {
    TechCard(
        modifier = Modifier
            .fillMaxWidth()
            .techRipple(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = feature.iconEmoji,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
