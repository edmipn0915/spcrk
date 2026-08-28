package com.spcrk.app

import android.content.Context
import com.spcrk.app.ai.api.DocumentService
import com.spcrk.app.ai.api.LocalModelService
import com.spcrk.app.ai.api.McpService
import com.spcrk.app.ai.api.OcrService
import com.spcrk.app.ai.api.SkillService
import com.spcrk.app.data.ModelConfigStore
import com.spcrk.app.data.Repository
import com.spcrk.app.data.SettingsStore

interface AppContainer {
    val repository: Repository
    val aiManager: com.spcrk.app.ai.AIManager
    val searchEngine: com.spcrk.app.ai.SearchEngine
    val mcpService: McpService
    val skillService: SkillService
    val documentService: DocumentService
    val ocrService: OcrService
    val localModelService: LocalModelService
    val videoDownloader: com.spcrk.app.downloader.VideoDownloader
    val settingsStore: SettingsStore
    val modelConfigStore: ModelConfigStore
}

fun getAppContainer(context: Context): AppContainer =
    (context.applicationContext as VideoDownloaderApp).container

