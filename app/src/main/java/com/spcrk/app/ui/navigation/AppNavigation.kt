package com.spcrk.app.ui.navigation

import android.app.Application
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spcrk.app.ui.HomeScreen
import com.spcrk.app.ui.AiChatScreen
import com.spcrk.app.ui.KnowledgeScreen
import com.spcrk.app.ui.SettingsScreen
import com.spcrk.app.ui.HistoryScreen
import com.spcrk.app.ui.AssistantsScreen
import com.spcrk.app.ui.NotesScreen
import com.spcrk.app.ui.NoteEditScreen
import com.spcrk.app.ui.SearchScreen
import com.spcrk.app.ui.SearchSettingsScreen
import com.spcrk.app.ui.ModelManageScreen
import com.spcrk.app.ui.McpManageScreen
import com.spcrk.app.ui.SkillManageScreen
import com.spcrk.app.ui.StatsScreen
import com.spcrk.app.ui.OcrScreen
import com.spcrk.app.ui.DocumentScreen
import com.spcrk.app.ui.ScheduleScreen
import com.spcrk.app.ui.VideoDownloadScreen
import com.spcrk.app.ui.AboutSettingsScreen
import com.spcrk.app.ui.AppearanceSettingsScreen
import com.spcrk.app.ui.DataSettingsScreen
import com.spcrk.app.ui.DependenciesSettingsScreen
import com.spcrk.app.ui.FileProcessingSettingsScreen
import com.spcrk.app.ui.ModelSettingsScreen
import com.spcrk.app.ui.McpSettingsScreen
import com.spcrk.app.ui.LocalModelScreen
import com.spcrk.app.ui.NotificationSettingsScreen
import com.spcrk.app.ui.ProviderSettingsScreen
import com.spcrk.app.ui.ai.TranslateScreen
import com.spcrk.app.ui.ai.CodeAssistantScreen
import com.spcrk.app.ui.ai.ChatScreen
import com.spcrk.app.ui.theme.AnimationDistances
import com.spcrk.app.ui.theme.fissionEnter
import com.spcrk.app.ui.theme.fusionExit

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AiChat : Screen("ai_chat")
    object Chat : Screen("chat")
    object Knowledge : Screen("knowledge")
    object Settings : Screen("settings")
    object History : Screen("history")
    object Assistants : Screen("assistants")
    object Notes : Screen("notes")
    object NoteEdit : Screen("note_edit")
    object Search : Screen("search")
    object ModelManage : Screen("model_manage")
    object McpManage : Screen("mcp_manage")
    object SkillManage : Screen("skill_manage")
    object Stats : Screen("stats")
    object Ocr : Screen("ocr")
    object Documents : Screen("documents")
    object Schedule : Screen("schedule")
    object SettingsMcp : Screen("settings/mcp")
    object SettingsSkills : Screen("settings/skills")
    object SettingsStats : Screen("settings/stats")
    object SettingsSchedules : Screen("settings/schedules")
    object SettingsAbout : Screen("settings/about")
    object SettingsAppearance : Screen("settings/appearance")
    object SettingsProviders : Screen("settings/providers")
    object SettingsModelConfig : Screen("settings/model-config")
    object SettingsData : Screen("settings/data")
    object SettingsDependencies : Screen("settings/dependencies")
    object SettingsFileProcessing : Screen("settings/file-processing")
    object SettingsNotifications : Screen("settings/notifications")
    object SettingsSearch : Screen("settings/search")
    object SettingsLocalModels : Screen("settings/local-models")
    object VideoDownload : Screen("video_download")
    object Translate : Screen("translate")
    object CodeAssistant : Screen("code_assistant")
}

data class BottomNavTab(
    val screen: Screen,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun bottomNavTabs(): List<BottomNavTab> {
    val s = com.spcrk.app.ui.l10n.appStrings()
    return listOf(
        BottomNavTab(Screen.Home, s.home, Icons.Default.Home),
        BottomNavTab(Screen.AiChat, s.aiChat, Icons.Default.Chat),
        BottomNavTab(Screen.Knowledge, s.knowledge, Icons.Default.MenuBook),
        BottomNavTab(Screen.Settings, s.settings, Icons.Default.Settings)
    )
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tabs = bottomNavTabs()
    val showBottomBar = currentRoute in tabs.map { it.screen.route }

    val topLevelRoutes = tabs.map { it.screen.route }.toSet()

    // 最近一次二级页面点击的屏幕坐标，供 fissionEnter 计算“从点击原点向外炸开”方向
    var lastClickOffset by rememberSaveable(stateSaver = OffsetSaver) { mutableStateOf<Offset?>(null) }

    // 在 @Composable 作用域换算转场所需的像素值（转场 lambda 非 @Composable，不能读 CompositionLocal）
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val fissionSlidePx = with(density) { AnimationDistances.FISSION_SLIDE.toPx() }
    val fusionExitPx = with(density) { AnimationDistances.FUSION_SLIDE.toPx() }
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            selected = currentRoute == tab.screen.route,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            onClick = {
                                navController.navigate(tab.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                val isTab = targetState.destination.route in topLevelRoutes
                if (isTab) {
                    // Tab 切换：淡入淡出，避免对已选页面做“炸开”扰动
                    fadeIn(animationSpec = tween(220))
                } else {
                    // 二级页面：裂变入（点击原点炸开）
                    fissionEnter(
                        initialOffset = lastClickOffset,
                        slidePx = fissionSlidePx,
                        screenWidthPx = screenWidthPx,
                        screenHeightPx = screenHeightPx
                    )
                }
            },
            exitTransition = {
                val isTab = targetState.destination.route in topLevelRoutes
                if (isTab) {
                    fadeOut(animationSpec = tween(180))
                } else {
                    // 二级页面：聚变出（收缩淡出）
                    fusionExit(exitPx = fusionExitPx)
                }
            },
            popEnterTransition = {
                val isTab = targetState.destination.route in topLevelRoutes
                if (isTab) {
                    fadeIn(animationSpec = tween(220))
                } else {
                    // 返回二级页面时裂变入
                    fissionEnter(
                        initialOffset = lastClickOffset,
                        slidePx = fissionSlidePx,
                        screenWidthPx = screenWidthPx,
                        screenHeightPx = screenHeightPx
                    )
                }
            },
            popExitTransition = {
                val isTab = targetState.destination.route in topLevelRoutes
                if (isTab) {
                    fadeOut(animationSpec = tween(180))
                } else {
                    fusionExit(exitPx = fusionExitPx)
                }
            }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToVideoDownload = { offset ->
                        lastClickOffset = offset
                        navController.navigate(Screen.VideoDownload.route)
                    },
                    onNavigateToHistory = { offset ->
                        lastClickOffset = offset
                        navController.navigate(Screen.History.route)
                    }
                )
            }
            composable(Screen.AiChat.route) {
                AiChatScreen(navController = navController)
            }
            composable(Screen.Knowledge.route) {
                KnowledgeScreen(
                    navController = navController,
                    viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.spcrk.app.ui.KnowledgeViewModelFactory(LocalContext.current.applicationContext as Application)
                    )
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    navController = navController,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.Assistants.route) {
                AssistantsScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateToChat = { navController.navigate(Screen.Chat.route) }
                )
            }
            composable(Screen.Notes.route) {
                NotesScreen(
                    navController = navController,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = "${Screen.NoteEdit.route}/{noteId}",
                arguments = listOf(navArgument("noteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val noteIdStr = backStackEntry.arguments?.getString("noteId")
                val noteId = if (noteIdStr == "new") null else noteIdStr?.toLongOrNull()
                NoteEditScreen(
                    noteId = noteId,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onBackClick = { navController.popBackStack() },
                    onNavigateToNote = { noteId ->
                        navController.navigate("${Screen.NoteEdit.route}/$noteId")
                    },
                    onNavigateToChat = { convId ->
                        navController.navigate(Screen.Chat.route)
                    },
                    onNavigateToHistory = { historyId ->
                        navController.navigate(Screen.History.route)
                    }
                )
            }
            composable(Screen.ModelManage.route) {
                ModelManageScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.McpManage.route) {
                McpManageScreen(
                    onBackClick = { navController.popBackStack() },
                    viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.spcrk.app.ui.McpManageViewModelFactory(LocalContext.current.applicationContext as Application)
                    )
                )
            }
            composable(Screen.SettingsMcp.route) {
                McpSettingsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.Stats.route) {
                StatsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.Ocr.route) {
                OcrScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Documents.route) {
                DocumentScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Schedule.route) {
                ScheduleScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.SettingsSkills.route) {
                SkillManageScreen(
                    onBackClick = { navController.popBackStack() },
                    viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.spcrk.app.ui.SkillManageViewModelFactory(LocalContext.current.applicationContext as Application)
                    )
                )
            }
            composable(Screen.SettingsStats.route) {
                StatsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.SettingsSchedules.route) {
                ScheduleScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.SettingsAbout.route) {
                AboutSettingsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.SettingsAppearance.route) {
                AppearanceSettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.SettingsProviders.route) {
                ProviderSettingsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.SettingsModelConfig.route) {
                ModelSettingsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.SettingsData.route) {
                DataSettingsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.SettingsDependencies.route) {
                DependenciesSettingsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.SettingsFileProcessing.route) {
                FileProcessingSettingsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.SettingsNotifications.route) {
                NotificationSettingsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.SettingsSearch.route) {
                SearchSettingsScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.SettingsLocalModels.route) {
                LocalModelScreen(
                    onBackClick = { navController.popBackStack() },
                    viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = com.spcrk.app.ui.LocalModelViewModelFactory(LocalContext.current.applicationContext as Application)
                    )
                )
            }
            composable(Screen.VideoDownload.route) {
                VideoDownloadScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.Translate.route) {
                TranslateScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.CodeAssistant.route) {
                CodeAssistantScreen(onBackClick = { navController.popBackStack() })
            }
            composable(Screen.Chat.route) {
                ChatScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}

/**
 * 让 [Offset] 可在 rememberSaveable 中跨配置变更保留。
 * 用 java.lang.Float 的位运算序列化，避免 Kotlin companion 扩展解析问题。
 */
private val OffsetSaver: Saver<Offset?, *> = Saver(
    save = { offset ->
        if (offset == null) {
            ""
        } else {
            val bits = java.lang.Float.floatToRawIntBits(offset.x) to
                java.lang.Float.floatToRawIntBits(offset.y)
            "${bits.first}_${bits.second}"
        }
    },
    restore = { str ->
        if ((str as? String).isNullOrEmpty()) {
            null
        } else {
            val parts = str.split("_")
            if (parts.size != 2) {
                null
            } else {
                val xBits = parts[0].toIntOrNull()
                val yBits = parts[1].toIntOrNull()
                if (xBits == null || yBits == null) {
                    null
                } else {
                    Offset(
                        java.lang.Float.intBitsToFloat(xBits),
                        java.lang.Float.intBitsToFloat(yBits)
                    )
                }
            }
        }
    }
)
