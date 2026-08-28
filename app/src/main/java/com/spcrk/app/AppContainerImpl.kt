package com.spcrk.app

import android.app.Application
import com.spcrk.app.ai.AIManager
import com.spcrk.app.ai.DocumentManager
import com.spcrk.app.ai.LocalModelManager
import com.spcrk.app.ai.McpManager
import com.spcrk.app.ai.OcrManager
import com.spcrk.app.ai.SearchEngine
import com.spcrk.app.ai.SkillManager
import com.spcrk.app.ai.api.DocumentService
import com.spcrk.app.ai.api.LocalModelService
import com.spcrk.app.ai.api.McpService
import com.spcrk.app.ai.api.OcrService
import com.spcrk.app.ai.api.SkillService
import com.spcrk.app.data.ModelConfigStore
import com.spcrk.app.data.Repository
import com.spcrk.app.data.SettingsStore
import com.spcrk.app.downloader.VideoDownloader

class AppContainerImpl(private val app: Application) : AppContainer {

    override val repository: Repository by lazy { Repository(app) }
    override val aiManager: AIManager by lazy { AIManager() }
    override val searchEngine: SearchEngine by lazy { SearchEngine() }
    override val mcpService: McpService by lazy { McpManager() }
    override val skillService: SkillService by lazy { SkillManager(app, searchEngine) }
    override val documentService: DocumentService by lazy { DocumentManager(app, repository) }
    override val ocrService: OcrService by lazy { OcrManager(app) }
    override val localModelService: LocalModelService by lazy { LocalModelManager(app) }
    override val videoDownloader: VideoDownloader by lazy { VideoDownloader() }
    override val settingsStore: SettingsStore by lazy { SettingsStore.getInstance() }
    override val modelConfigStore: ModelConfigStore by lazy { ModelConfigStore(app) }

    suspend fun initializeBuiltInSkills() {
        skillService.initializeBuiltInSkills()
    }
}
