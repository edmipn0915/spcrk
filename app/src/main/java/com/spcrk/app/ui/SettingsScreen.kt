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
    onBackClick: () -> Unit
) {
    val s = com.spcrk.app.ui.l10n.appStrings()
    val categories = listOf(
        SettingsCategory(s.appearanceTitle, s.appearanceSubtitle, Icons.Outlined.Palette, "settings/appearance"),
        SettingsCategory(s.providersTitle, s.providersSubtitle, Icons.Outlined.Cloud, "settings/providers"),
        SettingsCategory(s.modelConfigTitle, s.modelConfigSubtitle, Icons.Outlined.Psychology, "settings/model-config"),
        SettingsCategory(s.mcpTitle, s.mcpSubtitle, Icons.Outlined.Hub, "settings/mcp"),
        SettingsCategory(s.skillsTitle, s.skillsSubtitle, Icons.Outlined.Extension, "settings/skills"),
        SettingsCategory(s.dataTitle, s.dataSubtitle, Icons.Outlined.Storage, "settings/data"),
        SettingsCategory(s.dependenciesTitle, s.dependenciesSubtitle, Icons.Outlined.Code, "settings/dependencies"),
        SettingsCategory(s.localModelsTitle, s.localModelsSubtitle, Icons.Outlined.Memory, "settings/local-models"),
        SettingsCategory(s.fileProcessingTitle, s.fileProcessingSubtitle, Icons.Outlined.Description, "settings/file-processing"),
        SettingsCategory(s.searchTitle, s.searchSubtitle, Icons.Outlined.Search, "settings/search"),
        SettingsCategory(s.statsTitle, s.statsSubtitle, Icons.Outlined.BarChart, "settings/stats"),
        SettingsCategory(s.schedulesTitle, s.schedulesSubtitle, Icons.Outlined.Schedule, "settings/schedules"),
        SettingsCategory(s.notificationsTitle, s.notificationsSubtitle, Icons.Outlined.Notifications, "settings/notifications"),
        SettingsCategory(s.aboutTitle, s.aboutSubtitle, Icons.Outlined.Info, "settings/about")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = s.settings,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
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
