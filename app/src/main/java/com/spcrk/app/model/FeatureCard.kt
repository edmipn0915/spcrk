package com.spcrk.app.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class FeatureRoute {
    VIDEO_DOWNLOAD,
    COMING_SOON,
    HISTORY,
    AI_CHAT,
    SETTINGS
}

data class FeatureCard(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector? = null,
    val iconEmoji: String? = null,
    val iconBackgroundColor: Color,
    val route: FeatureRoute
)
