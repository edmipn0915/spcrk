package com.spcrk.app.ui.l10n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 應用 UI 字串（多語言）。
 * 語言代碼：zh（簡中）/ zh-TW（繁體）/ en（英文）/ ja（日文）。
 * 目前優先遷移底部導航、設置頁與外觀設置頁；其餘頁面逐步遷移。
 */
interface AppStrings {
    // 底部導航
    val home: String
    val aiChat: String
    val knowledge: String
    val settings: String
    // 通用
    val back: String
    // 設置分類（標題 / 副標題）
    val appearanceTitle: String
    val appearanceSubtitle: String
    val providersTitle: String
    val providersSubtitle: String
    val modelConfigTitle: String
    val modelConfigSubtitle: String
    val mcpTitle: String
    val mcpSubtitle: String
    val skillsTitle: String
    val skillsSubtitle: String
    val dataTitle: String
    val dataSubtitle: String
    val dependenciesTitle: String
    val dependenciesSubtitle: String
    val localModelsTitle: String
    val localModelsSubtitle: String
    val fileProcessingTitle: String
    val fileProcessingSubtitle: String
    val searchTitle: String
    val searchSubtitle: String
    val statsTitle: String
    val statsSubtitle: String
    val schedulesTitle: String
    val schedulesSubtitle: String
    val notificationsTitle: String
    val notificationsSubtitle: String
    val aboutTitle: String
    val aboutSubtitle: String
    // 外觀設置頁
    val themeMode: String
    val followSystem: String
    val lightTheme: String
    val darkTheme: String
    val themeColor: String
    val currentColor: String
    val fontSize: String
    val fontSizeCurrentFormat: String
    val uiZoom: String
    val zoomCurrentFormat: String
    val languageSettings: String
    val zhSimplified: String
    val zhTraditional: String
    val english: String
    val japanese: String
    // ---- AI 助手入口（AiChatScreen）----
    val aiHubTitle: String
    val aiHubChat: String
    val aiHubChatDesc: String
    val aiHubTranslate: String
    val aiHubTranslateDesc: String
    val aiHubCode: String
    val aiHubCodeDesc: String
    val aiHubNotes: String
    val aiHubNotesDesc: String
    val aiHubSearch: String
    val aiHubSearchDesc: String
    val aiHubOcr: String
    val aiHubOcrDesc: String
    // ---- 對話頁（ChatScreen）----
    val chatDefaultTitle: String
    val selectModel: String
    val noModelConfigured: String
    val chatHistory: String
    val newChat: String
    val deleteChat: String
    val deleteChatMessage: String
    val send: String
    val inputPlaceholder: String
    val toggleSearchDesc: String
    val uploadDocument: String
    val uploadImage: String
    val docAttached: String
    val imageAttached: String
    val clearAttachments: String
    val myAvatar: String
    val searchSourcesCount: String // %1$d
    val selectSkill: String
    val noSkillsInstalled: String
    // ---- 知識庫頁（KnowledgeScreen）----
    val searchDocumentsPlaceholder: String
    val all: String
    val newCategory: String
    val removeCategory: String
    val add: String
    val emptySearchResult: String
    val emptyCategory: String
    val emptyKnowledge: String
    val emptyKnowledgeHint: String
    val categoryName: String
    val categoryType: String
    val ragMode: String
    val ragModeDesc: String
    val embeddingMode: String
    val embeddingModeDesc: String
    val create: String
    val deleteDocument: String
    val deleteDocumentMessage: String // %1$s
    val chunkCount: String // %1$d
    val delete: String
    val cancel: String
    // ---- 影片下載頁（VideoDownloadScreen）----
    val videoDownloadTitle: String
    val pasteVideoLinkHint: String
    val urlPlaceholder: String
    val paste: String
    val parseVideo: String
    val downloadAction: String
    val cancelDownload: String
    val parsingVideo: String
    val sourceFormat: String // %1$s = 平台名
    val videoQuality: String
    val downloadComplete: String
    val openFile: String
    val share: String
    val supportedPlatforms: String
    val supportedPlatformsList: String
    val fileNotFound: String
    val noVideoPlayer: String
    val shareVideo: String
    val shareFailedFormat: String // %1$s = 錯誤訊息
    // ---- 歷史紀錄頁（HistoryScreen）----
    val historyTitle: String
    val noDownloadHistory: String
    val deleteHistoryTitle: String
    val deleteHistoryMessage: String
    val deleteLocalFileToo: String
    // ---- 首頁（HomeScreen）----
    val homeFeatureVideoDownload: String
    val homeFeatureVideoDownloadDesc: String
    val homeFeatureHistory: String
    val homeFeatureHistoryDesc: String
    val homeEngineStatus: String
    val homeIconContentDescription: String
    // ---- 對話頁狀態（ChatScreen）----
    val done: String
    val error: String
    // ---- 數據管理（DataSettingsScreen）----
    val backupSettings: String
    val autoBackup: String
    val autoBackupDesc: String
    val backupPath: String
    val backupComingSoon: String
    val backupNow: String
    val webDavStorage: String
    val username: String
    val password: String
    val webDavComingSoon: String
    val testConnection: String
    val importExport: String
    val importComingSoon: String
    val importData: String
    val exportComingSoon: String
    val exportData: String
    val noteSync: String
    val noteSyncComingSoon: String
    val dataReset: String
    val resetDataWarning: String
    val dataResetComingSoon: String
    val resetAllData: String
    // ---- 依賴設定（DependenciesSettingsScreen）----
    val pythonEnvironment: String
    val pythonEnvironmentDesc: String
    val pythonPath: String
    val configured: String
    val notConfigured: String
    val nodeJsEnvironment: String
    val nodeJsEnvironmentDesc: String
    val nodeJsPath: String
    val ollamaLocalModel: String
    val ollamaLocalModelDesc: String
    val ollamaUrl: String
    val pullModel: String
    val lmStudio: String
    val lmStudioDesc: String
    val lmStudioUrl: String
    val environmentCheck: String
    val environmentCheckDesc: String
    // ---- 本地模型（LocalModelScreen）----
    val modelImportSuccess: String
    val modelImportFailed: String
    val modelExists: String
    val downloadFailed: String
    val localModelsManagerTitle: String
    val addModel: String
    val downloadFromHuggingFace: String
    val downloadFromModelScope: String
    val importLocalGguf: String
    val embeddingModel: String
    val llmModel: String
    val downloaded: String
    val deleted: String
    val noEmbeddingModels: String
    val noLlmModels: String
    val noDownloadedModels: String
    val recommended: String
    val download: String
    val downloadModelTitle: String
    val downloadUrlLabel: String
    val fileNameOptional: String
    val ggufDownloadHint: String
    // ---- 文件處理（FileProcessingSettingsScreen）----
    val pdfProcessing: String
    val selectPdfEngine: String
    val pdfBoxRecommended: String
    val pdfBoxDesc: String
    val mupdfDesc: String
    val nativePdfRenderer: String
    val nativePdfDesc: String
    val ocrSettings: String
    val selectOcrEngine: String
    val mlKitRecommended: String
    val mlKitDesc: String
    val paddleOcrDesc: String
    val tesseractDesc: String
    val documentParsing: String
    val autoChunking: String
    val autoChunkingDesc: String
    val extractImages: String
    val extractImagesDesc: String
    val preserveFormat: String
    val preserveFormatDesc: String
    val fileLimits: String
    val maxFileSize: String
    val maxFileSizeDesc: String
    val maxFileCount: String
    val maxFileCountDesc: String
    // ---- 搜尋設定（SearchSettingsScreen）----
    val searchEngineSection: String
    val selectEngine: String
    val visitWebsiteFormat: String
    val apiConfig: String
    val apiKeyLabel: String
    val enterApiKeyFormat: String
    val hide: String
    val show: String
    val apiEndpointFormat: String
    val exaMcpHint: String
    val searxngInstanceAddress: String
    val searxngInstanceHint: String
    val noApiKeyFormat: String
    val searchParams: String
    val maxResultsFormat: String
    val urlContentFetch: String
    val urlContentFetchDesc: String
    val builtinParser: String
    val builtinParserDesc: String
    val jina: String
    val jinaParserDesc: String
    val jinaWebsite: String
    val firecrawlParserDesc: String
    val firecrawlWebsite: String
    val supportedEngines: String
    val requiresApiKey: String
    val requiresInstance: String
    val free: String
    // ---- 關於（AboutSettingsScreen）----
    val aboutVersionFormat: String
    val aboutTagline: String
    val aboutFeatures: String
    val appInfoTitle: String
    val appNameLabel: String
    val versionLabel: String
    val packageNameLabel: String
    val buildTimeLabel: String
    val systemVersionLabel: String
    val deviceModelLabel: String
    val techStackTitle: String
    val devLanguageLabel: String
    val uiFrameworkLabel: String
    val databaseLabel: String
    val networkLibraryLabel: String
    val minSdkLabel: String
    val targetSdkLabel: String
    val actionsTitle: String
    val checkUpdate: String
    val exportDiagnostics: String
    val feedback: String
    val relatedLinksTitle: String
    val officialDocsLabel: String
    val privacyPolicyLabel: String
    val termsOfServiceLabel: String
    val builtWithLabel: String
    // ---- 通知（NotificationSettingsScreen）----
    val notificationSettingsTitle: String
    val notificationSwitchTitle: String
    val enableNotifications: String
    val enableNotificationsDesc: String
    val notificationSoundTitle: String
    val notificationSoundDesc: String
    val notificationVibrationTitle: String
    val notificationVibrationDesc: String
    val notificationTypesTitle: String
    val taskCompleteNotificationTitle: String
    val taskCompleteNotificationDesc: String
    val taskFailNotificationTitle: String
    val taskFailNotificationDesc: String
    val updateNotificationTitle: String
    val updateNotificationDesc: String
    val doNotDisturbTitle: String
    val enableDoNotDisturb: String
    val doNotDisturbDesc: String
    val doNotDisturbTimeFormat: String
    // ---- 排程任務（ScheduleScreen）----
    val noScheduledTasks: String
    val createTaskHint: String
    val nextRunTimeLabel: String
    val edit: String
    val deleteTaskTitle: String
    val deleteTaskMessage: String
    val newTaskTitle: String
    val editTaskTitle: String
    val taskNameLabel: String
    val executionFrequencyLabel: String
    val everyMinute: String
    val everyFiveMinutes: String
    val everyFifteenMinutes: String
    val everyHour: String
    val everyDayPreset: String
    val cronExpressionLabel: String
    val cronFormat: String
    val actionTypeLabel: String
    val actionParamsLabel: String
    val confirm: String
    // ---- 用量統計（StatsScreen）----
    val totalTokensLabel: String
    val totalCostLabel: String
    val totalCallsLabel: String
    val today: String
    val thisWeek: String
    val thisMonth: String
    val tokenUsageTrendTitle: String
    val noData: String
    val modelDistributionTitle: String
    val costEstimateTitle: String
    val currentPeriodCostLabel: String
    val avgCostPerCallLabel: String
    val tokenUnitPriceLabel: String
    // ---- 助理（AssistantsScreen）----
    val assistantsTitle: String
    val assistantsWip: String
    // ---- MCP（McpSettingsScreen / McpManageScreen）----
    val mcpServersTitle: String
    val addMcpServerHint: String
    val toolsListWip: String
    val connectionTestWip: String
    val tools: String
    val connectionType: String
    val urlLabel: String
    val mcpManageTitle: String
    val addServer: String
    val noMcpServers: String
    val reconnect: String
    val test: String
    val deleteServerTitle: String
    val deleteServerConfirm: String
    val connected: String
    val connecting: String
    val disconnected: String
    val unknown: String
    val quickAdd: String
    val jsonImport: String
    val dxtImport: String
    val mcpbImport: String
    val dxtParseFailed: String
    val fileReadFailed: String
    val mcpbParseFailed: String
    val addMcpServer: String
    val jsonParseFailed: String
    val serverName: String
    val connectionMethod: String
    val command: String
    val arguments: String
    val serverUrl: String
    val headersLabel: String
    val pasteMcpJsonHint: String
    val jsonConfig: String
    val parseAndAdd: String
    val selectDxtFile: String
    val selectFile: String
    val parseResult: String
    val name: String
    val type: String
    val selectMcpbFile: String
    val testToolTitle: String
    val noAvailableTools: String
    val selectTool: String
    val argsJsonLabel: String
    val execute: String
    val result: String
    val close: String
    // ---- Skill 管理（SkillManageScreen）----
    val skillManageTitle: String
    val addSkill: String
    val noSkills: String
    val addSkillHint: String
    val httpRequest: String
    val textTransform: String
    val description: String
    val triggerLabel: String
    val actionType: String
    val urlPlaceholderLabel: String
    val prefixLabel: String
    val suffixLabel: String
    // ---- 模型設置（ModelSettingsScreen）----
    val defaultModel: String
    val defaultModelDesc: String
    val quickModel: String
    val quickModelDesc: String
    val translateModel: String
    val translateModelDesc: String
    val topicNamingModel: String
    val topicNamingModelDesc: String
    val notSelected: String
    // ---- 模型管理（ModelManageScreen）----
    val modelManage: String
    val quickAddPresetModels: String
    val discoverOllamaModels: String
    val testing: String
    val default: String
    val editModel: String
    val provider: String
    val modelName: String
    val temperature: String
    val maxTokens: String
    val timeoutSeconds: String
    val save: String
    val selectOllamaModel: String
    val discoveringLocalModels: String
    val noModelsFound: String
    // ---- AI 供應商（ProviderSettingsScreen）----
    val addPresetModel: String
    val addCustomModel: String
    val noProvidersConfigured: String
    val noProvidersHint: String
    val modelCountFormat: String
    val notConfiguredAddModel: String
    val enterDetails: String
    val enableProvider: String
    val enableProviderDesc: String
    val presetModels: String
    val fetching: String
    val fetchModels: String
    val noModelsFetched: String
    val fetchModelsFailed: String
    val modelsTitleFormat: String
    val noModelsHint: String
    val modelsAddedFormat: String
    val noProviderConfigHint: String
    val providerType: String
    val noPresetModels: String
    val selectModelsToAdd: String
    val alreadyAdded: String
    val fetchedModelsTitle: String
    val etcModelsFormat: String
    val addAll: String
    val addProviderHint: String
    val editProviderConfig: String
    val editProviderHint: String
}

val LocalAppStrings = staticCompositionLocalOf { AppStringsCatalog.zh }

@Composable
@ReadOnlyComposable
fun appStrings(): AppStrings = LocalAppStrings.current

/** 依語言代碼回傳翻譯表，未知代碼退回簡中。 */
fun appStringsFor(language: String): AppStrings = when (language) {
    "zh" -> AppStringsCatalog.zh
    "zh-TW" -> AppStringsCatalog.zhTW
    "en" -> AppStringsCatalog.en
    "ja" -> AppStringsCatalog.ja
    else -> AppStringsCatalog.zh
}

object AppStringsCatalog {

    val zh: AppStrings = object : AppStrings {
        override val home = "主页"
        override val aiChat = "AI聊天"
        override val knowledge = "知识库"
        override val settings = "设置"
        override val back = "返回"
        override val appearanceTitle = "外观"
        override val appearanceSubtitle = "主题、颜色、字体、语言、缩放"
        override val providersTitle = "AI 提供商"
        override val providersSubtitle = "API Key、Base URL、模型同步"
        override val modelConfigTitle = "模型设置"
        override val modelConfigSubtitle = "默认模型、快速模型、翻译模型"
        override val mcpTitle = "MCP 服务器"
        override val mcpSubtitle = "服务器管理、工具、资源、日志"
        override val skillsTitle = "Skill 管理"
        override val skillsSubtitle = "安装和管理 Skill 扩展"
        override val dataTitle = "数据管理"
        override val dataSubtitle = "备份、导入导出、云存储"
        override val dependenciesTitle = "依赖设置"
        override val dependenciesSubtitle = "Python、Node.js、本地模型"
        override val localModelsTitle = "本地模型"
        override val localModelsSubtitle = "嵌入模型、GGUF 下载、本地推理"
        override val fileProcessingTitle = "文件处理"
        override val fileProcessingSubtitle = "PDF 解析、OCR 设置"
        override val searchTitle = "搜索设置"
        override val searchSubtitle = "搜索引擎、API Key、自定义实例"
        override val statsTitle = "用量统计"
        override val statsSubtitle = "Token 用量和费用统计"
        override val schedulesTitle = "定时任务"
        override val schedulesSubtitle = "管理定时执行的任务"
        override val notificationsTitle = "通知"
        override val notificationsSubtitle = "通知开关和类型配置"
        override val aboutTitle = "关于"
        override val aboutSubtitle = "版本信息和开源许可"
        override val themeMode = "主题模式"
        override val followSystem = "跟随系统"
        override val lightTheme = "亮色主题"
        override val darkTheme = "暗色主题"
        override val themeColor = "主题颜色"
        override val currentColor = "当前颜色"
        override val fontSize = "字体大小"
        override val fontSizeCurrentFormat = "当前: %1\$dsp"
        override val uiZoom = "界面缩放"
        override val zoomCurrentFormat = "当前: %1\$d%%"
        override val languageSettings = "语言设置"
        override val zhSimplified = "简体中文"
        override val zhTraditional = "繁體中文"
        override val english = "English"
        override val japanese = "日本語"
        override val aiHubTitle = "AI 助手"
        override val aiHubChat = "对话"
        override val aiHubChatDesc = "与AI助手智能对话"
        override val aiHubTranslate = "翻译"
        override val aiHubTranslateDesc = "多语言互译助手"
        override val aiHubCode = "代码助手"
        override val aiHubCodeDesc = "编程问题解答"
        override val aiHubNotes = "笔记"
        override val aiHubNotesDesc = "记录和管理笔记"
        override val aiHubSearch = "搜索"
        override val aiHubSearchDesc = "全局搜索功能"
        override val aiHubOcr = "OCR识别"
        override val aiHubOcrDesc = "图片文字识别"
        override val chatDefaultTitle = "AI 聊天"
        override val selectModel = "选择模型"
        override val noModelConfigured = "未配置模型，请到设置中添加"
        override val chatHistory = "对话历史"
        override val newChat = "新建对话"
        override val deleteChat = "删除对话"
        override val deleteChatMessage = "确定要删除这条对话吗？此操作不可撤销。"
        override val send = "发送"
        override val inputPlaceholder = "输入消息..."
        override val toggleSearchDesc = "搜索增强"
        override val uploadDocument = "上传文档"
        override val uploadImage = "上传图片"
        override val docAttached = "文档已附加"
        override val imageAttached = "图片已附加"
        override val clearAttachments = "清除附件"
        override val myAvatar = "我"
        override val searchSourcesCount = "%1\$d 个搜索来源"
        override val selectSkill = "选择 Skill"
        override val noSkillsInstalled = "暂无已安装的 Skill"
        override val searchDocumentsPlaceholder = "搜索文档..."
        override val all = "全部"
        override val newCategory = "新建分类"
        override val removeCategory = "移除分类"
        override val add = "添加"
        override val emptySearchResult = "未找到相关文档"
        override val emptyCategory = "该分类下暂无文档"
        override val emptyKnowledge = "知识库为空"
        override val emptyKnowledgeHint = "点击右下角按钮上传文档或新建分类"
        override val categoryName = "分类名称"
        override val categoryType = "分类类型"
        override val ragMode = "RAG 模式"
        override val ragModeDesc = "直接检索文档片段注入上下文"
        override val embeddingMode = "嵌入模式"
        override val embeddingModeDesc = "使用嵌入模型向量化后语义检索"
        override val create = "创建"
        override val deleteDocument = "删除文档"
        override val deleteDocumentMessage = "确定要删除「%1\$s」吗？此操作不可恢复。"
        override val chunkCount = "%1\$d 个片段"
        override val delete = "删除"
        override val cancel = "取消"
        override val videoDownloadTitle = "视频下载"
        override val pasteVideoLinkHint = "粘贴视频链接即可下载"
        override val urlPlaceholder = "粘贴视频链接..."
        override val paste = "粘贴"
        override val parseVideo = "解析视频"
        override val downloadAction = "⬇ 下载"
        override val cancelDownload = "取消下载"
        override val parsingVideo = "正在解析视频..."
        override val sourceFormat = "来源: %1\$s"
        override val videoQuality = "画质"
        override val downloadComplete = "下载完成"
        override val openFile = "打开"
        override val share = "分享"
        override val supportedPlatforms = "支持平台"
        override val supportedPlatformsList = "• B站 (bilibili.com)\n• YouTube\n• 抖音\n• 快手\n• 优酷\n• 更多平台持续添加..."
        override val fileNotFound = "文件不存在"
        override val noVideoPlayer = "未找到可用的视频播放器"
        override val shareVideo = "分享视频"
        override val shareFailedFormat = "分享失败: %1\$s"
        override val historyTitle = "历史记录"
        override val noDownloadHistory = "暂无下载记录"
        override val deleteHistoryTitle = "确认删除"
        override val deleteHistoryMessage = "确定要删除这条下载记录吗？"
        override val deleteLocalFileToo = "同时删除本地文件"
        override val homeFeatureVideoDownload = "视频下载"
        override val homeFeatureVideoDownloadDesc = "下载在线视频到本地"
        override val homeFeatureHistory = "历史记录"
        override val homeFeatureHistoryDesc = "查看下载历史和浏览记录"
        override val homeEngineStatus = "本地引擎 · 运行中"
        override val homeIconContentDescription = "Sparck 灵愿"
        override val done = "完成"
        override val error = "错误"
        override val backupSettings = "备份设置"
        override val autoBackup = "自动备份"
        override val autoBackupDesc = "定期自动备份应用数据"
        override val backupPath = "备份路径"
        override val backupComingSoon = "备份功能即将推出"
        override val backupNow = "立即备份"
        override val webDavStorage = "云存储 (WebDAV)"
        override val username = "用户名"
        override val password = "密码"
        override val webDavComingSoon = "WebDAV 功能即将推出"
        override val testConnection = "测试连接"
        override val importExport = "数据导入导出"
        override val importComingSoon = "导入功能即将推出"
        override val importData = "导入"
        override val exportComingSoon = "导出功能即将推出"
        override val exportData = "导出"
        override val noteSync = "笔记同步"
        override val noteSyncComingSoon = "笔记同步功能即将推出"
        override val dataReset = "数据重置"
        override val resetDataWarning = "清除所有应用数据，操作不可恢复"
        override val dataResetComingSoon = "数据重置功能即将推出"
        override val resetAllData = "重置所有数据"
        override val pythonEnvironment = "Python 环境"
        override val pythonEnvironmentDesc = "配置 Python 解释器路径，用于运行 MCP 服务器"
        override val pythonPath = "Python 路径"
        override val configured = "已配置"
        override val notConfigured = "未配置"
        override val nodeJsEnvironment = "Node.js 环境"
        override val nodeJsEnvironmentDesc = "配置 Node.js 路径，用于运行 npx 命令"
        override val nodeJsPath = "Node.js 路径"
        override val ollamaLocalModel = "Ollama 本地模型"
        override val ollamaLocalModelDesc = "配置 Ollama 服务地址，使用本地大模型"
        override val ollamaUrl = "Ollama URL"
        override val pullModel = "拉取模型"
        override val lmStudio = "LM Studio"
        override val lmStudioDesc = "配置 LM Studio 服务地址"
        override val lmStudioUrl = "LM Studio URL"
        override val environmentCheck = "环境依赖检查"
        override val environmentCheckDesc = "检查所有环境依赖是否满足运行要求"
        override val modelImportSuccess = "模型导入成功"
        override val modelImportFailed = "模型导入失败"
        override val modelExists = "模型已存在"
        override val downloadFailed = "下载失败"
        override val localModelsManagerTitle = "本地模型管理"
        override val addModel = "添加模型"
        override val downloadFromHuggingFace = "从 Hugging Face 下载"
        override val downloadFromModelScope = "从 ModelScope 下载"
        override val importLocalGguf = "导入本地 GGUF"
        override val embeddingModel = "嵌入模型"
        override val llmModel = "LLM 模型"
        override val downloaded = "已下载"
        override val deleted = "已删除"
        override val noEmbeddingModels = "暂无可用的嵌入模型"
        override val noLlmModels = "暂无可用的 LLM 模型"
        override val noDownloadedModels = "暂无已下载的模型"
        override val recommended = "推荐"
        override val download = "下载"
        override val downloadModelTitle = "下载模型"
        override val downloadUrlLabel = "下载链接"
        override val fileNameOptional = "文件名 (可选)"
        override val ggufDownloadHint = "支持从 Hugging Face 或 ModelScope 下载 GGUF 格式模型"
        override val pdfProcessing = "PDF 处理"
        override val selectPdfEngine = "选择 PDF 解析引擎"
        override val pdfBoxRecommended = "PdfBox (推荐)"
        override val pdfBoxDesc = "Apache PdfBox，解析精度高"
        override val mupdfDesc = "轻量级，解析速度快"
        override val nativePdfRenderer = "系统原生"
        override val nativePdfDesc = "使用系统内置 PDF 渲染"
        override val ocrSettings = "OCR 设置"
        override val selectOcrEngine = "选择 OCR 识别引擎"
        override val mlKitRecommended = "ML Kit (推荐)"
        override val mlKitDesc = "Google ML Kit，支持多语言"
        override val paddleOcrDesc = "百度 PaddleOCR，中文识别优秀"
        override val tesseractDesc = "开源 OCR，支持 100+ 语言"
        override val documentParsing = "文档解析"
        override val autoChunking = "自动分块"
        override val autoChunkingDesc = "长文档自动分块处理"
        override val extractImages = "提取图片"
        override val extractImagesDesc = "从文档中提取嵌入的图片"
        override val preserveFormat = "保留格式"
        override val preserveFormatDesc = "保留原始文档格式信息"
        override val fileLimits = "文件限制"
        override val maxFileSize = "最大文件大小"
        override val maxFileSizeDesc = "单个文件上传限制"
        override val maxFileCount = "最大文件数"
        override val maxFileCountDesc = "单次上传文件数量"
        override val searchEngineSection = "搜索引擎"
        override val selectEngine = "选择引擎"
        override val visitWebsiteFormat = "访问 %1\$s 官网"
        override val apiConfig = "API 配置"
        override val apiKeyLabel = "API Key"
        override val enterApiKeyFormat = "请输入 %1\$s API Key"
        override val hide = "隐藏"
        override val show = "显示"
        override val apiEndpointFormat = "API 地址: %1\$s"
        override val exaMcpHint = "Exa MCP 为免费搜索服务，去 dashboard.exa.ai/api-keys 免费注册获取 API Key，无需付费"
        override val searxngInstanceAddress = "SearXNG 实例地址"
        override val searxngInstanceHint = "请输入你的 SearXNG 实例完整地址"
        override val noApiKeyFormat = "%1\$s 不需要 API Key"
        override val searchParams = "搜索参数"
        override val maxResultsFormat = "最大结果数: %1\$d"
        override val urlContentFetch = "URL 内容获取"
        override val urlContentFetchDesc = "从网页提取正文的方式"
        override val builtinParser = "内置解析"
        override val builtinParserDesc = "Jsoup 本地解析，无需 API Key"
        override val jina = "Jina"
        override val jinaParserDesc = "https://r.jina.ai/ 高质量提取"
        override val jinaWebsite = "Jina AI 官网"
        override val firecrawlParserDesc = "API 调用，需 API Key"
        override val firecrawlWebsite = "Firecrawl 官网"
        override val supportedEngines = "支持的引擎"
        override val requiresApiKey = "需API Key"
        override val requiresInstance = "需实例"
        override val free = "免费"
        override val aboutVersionFormat = "版本 %1\$s"
        override val aboutTagline = "一个功能强大的 AI 助手应用"
        override val aboutFeatures = "支持多模型对话、知识管理、文档处理、MCP 集成等功能"
        override val appInfoTitle = "应用信息"
        override val appNameLabel = "应用名称"
        override val versionLabel = "版本号"
        override val packageNameLabel = "包名"
        override val buildTimeLabel = "构建时间"
        override val systemVersionLabel = "系统版本"
        override val deviceModelLabel = "设备型号"
        override val techStackTitle = "技术栈"
        override val devLanguageLabel = "开发语言"
        override val uiFrameworkLabel = "UI 框架"
        override val databaseLabel = "数据库"
        override val networkLibraryLabel = "网络库"
        override val minSdkLabel = "最低 SDK"
        override val targetSdkLabel = "目标 SDK"
        override val actionsTitle = "操作"
        override val checkUpdate = "检查更新"
        override val exportDiagnostics = "导出诊断包"
        override val feedback = "意见反馈"
        override val relatedLinksTitle = "相关链接"
        override val officialDocsLabel = "官方文档"
        override val privacyPolicyLabel = "隐私政策"
        override val termsOfServiceLabel = "用户协议"
        override val builtWithLabel = "基于 Kotlin + Jetpack Compose 构建"
        override val notificationSettingsTitle = "通知设置"
        override val notificationSwitchTitle = "通知开关"
        override val enableNotifications = "启用通知"
        override val enableNotificationsDesc = "接收应用通知"
        override val notificationSoundTitle = "通知声音"
        override val notificationSoundDesc = "通知时播放声音"
        override val notificationVibrationTitle = "通知振动"
        override val notificationVibrationDesc = "通知时设备振动"
        override val notificationTypesTitle = "通知类型"
        override val taskCompleteNotificationTitle = "任务完成"
        override val taskCompleteNotificationDesc = "后台任务完成时通知"
        override val taskFailNotificationTitle = "任务失败"
        override val taskFailNotificationDesc = "后台任务失败时通知"
        override val updateNotificationTitle = "更新提醒"
        override val updateNotificationDesc = "有新版本时通知"
        override val doNotDisturbTitle = "免打扰模式"
        override val enableDoNotDisturb = "开启免打扰"
        override val doNotDisturbDesc = "设定时间段内不接收通知"
        override val doNotDisturbTimeFormat = "免打扰时间: %1\$s - %2\$s"
        override val noScheduledTasks = "暂无定时任务"
        override val createTaskHint = "点击右下角按钮创建新任务"
        override val nextRunTimeLabel = "下次执行"
        override val edit = "编辑"
        override val deleteTaskTitle = "删除任务"
        override val deleteTaskMessage = "确定要删除任务「%1\$s」吗？"
        override val newTaskTitle = "新建任务"
        override val editTaskTitle = "编辑任务"
        override val taskNameLabel = "任务名称"
        override val executionFrequencyLabel = "执行频率"
        override val everyMinute = "每分钟"
        override val everyFiveMinutes = "每5分钟"
        override val everyFifteenMinutes = "每15分钟"
        override val everyHour = "每小时"
        override val everyDayPreset = "每天 (9:00)"
        override val cronExpressionLabel = "Cron 表达式"
        override val cronFormat = "Cron: %1\$s"
        override val actionTypeLabel = "动作类型"
        override val actionParamsLabel = "动作参数"
        override val confirm = "确定"
        override val totalTokensLabel = "总 Token"
        override val totalCostLabel = "总费用"
        override val totalCallsLabel = "调用次数"
        override val today = "今日"
        override val thisWeek = "本周"
        override val thisMonth = "本月"
        override val tokenUsageTrendTitle = "Token 用量趋势"
        override val noData = "暂无数据"
        override val modelDistributionTitle = "按模型分布"
        override val costEstimateTitle = "费用估算"
        override val currentPeriodCostLabel = "当前周期费用"
        override val avgCostPerCallLabel = "平均每次调用"
        override val tokenUnitPriceLabel = "Token 单价参考"
        override val assistantsTitle = "助手管理"
        override val assistantsWip = "助手管理功能开发中..."
        override val mcpServersTitle = "MCP 服务器"
        override val addMcpServerHint = "点击右下角按钮添加服务器"
        override val toolsListWip = "工具列表功能开发中"
        override val connectionTestWip = "连接测试功能开发中"
        override val tools = "工具"
        override val connectionType = "连接类型"
        override val urlLabel = "URL (SSE 类型)"
        override val mcpManageTitle = "MCP 管理"
        override val addServer = "添加服务器"
        override val noMcpServers = "暂无 MCP 服务器"
        override val reconnect = "重连"
        override val test = "测试"
        override val deleteServerTitle = "删除服务器"
        override val deleteServerConfirm = "确定要删除「%1\$s」吗？"
        override val connected = "已连接"
        override val connecting = "连接中"
        override val disconnected = "未连接"
        override val unknown = "未知"
        override val quickAdd = "快速添加"
        override val jsonImport = "JSON 导入"
        override val dxtImport = "DXT 包导入"
        override val mcpbImport = "mcpb 包导入"
        override val dxtParseFailed = "DXT 文件解析失败"
        override val fileReadFailed = "文件读取失败: %1\$s"
        override val mcpbParseFailed = "mcpb 文件解析失败"
        override val addMcpServer = "添加 MCP 服务器"
        override val jsonParseFailed = "JSON 解析失败"
        override val serverName = "服务器名称"
        override val connectionMethod = "连接方式"
        override val command = "命令 (uvx/npx)"
        override val arguments = "参数"
        override val serverUrl = "服务器 URL"
        override val headersLabel = "Headers (JSON 格式)"
        override val pasteMcpJsonHint = "粘贴 MCP 服务器 JSON 配置"
        override val jsonConfig = "JSON 配置"
        override val parseAndAdd = "解析并添加"
        override val selectDxtFile = "选择 DXT 文件 (ZIP 格式)"
        override val selectFile = "选择文件"
        override val parseResult = "解析结果"
        override val name = "名称"
        override val type = "类型"
        override val selectMcpbFile = "选择 mcpb 文件 (二进制包)"
        override val testToolTitle = "测试工具 - %1\$s"
        override val noAvailableTools = "暂无可用工具"
        override val selectTool = "选择工具:"
        override val argsJsonLabel = "参数 (JSON)"
        override val execute = "执行"
        override val result = "结果:"
        override val close = "关闭"
        override val skillManageTitle = "Skill 管理"
        override val addSkill = "添加 Skill"
        override val noSkills = "暂无 Skill"
        override val addSkillHint = "点击 + 添加自定义 Skill"
        override val httpRequest = "HTTP 请求"
        override val textTransform = "文本转换"
        override val description = "描述"
        override val triggerLabel = "触发词"
        override val actionType = "动作类型"
        override val urlPlaceholderLabel = "URL (使用 {input} 作为占位符)"
        override val prefixLabel = "前缀"
        override val suffixLabel = "后缀"
        override val defaultModel = "默认模型"
        override val defaultModelDesc = "新建对话时使用的模型"
        override val quickModel = "快速模型"
        override val quickModelDesc = "快捷回复使用的模型"
        override val translateModel = "翻译模型"
        override val translateModelDesc = "翻译功能使用的模型"
        override val topicNamingModel = "话题命名模型"
        override val topicNamingModelDesc = "自动生成话题标题使用的模型"
        override val notSelected = "未选择"
        override val modelManage = "模型管理"
        override val quickAddPresetModels = "快速添加预设模型"
        override val discoverOllamaModels = "发现 Ollama 本地模型"
        override val testing = "测试中"
        override val default = "默认"
        override val editModel = "编辑模型"
        override val provider = "提供商"
        override val modelName = "模型名称"
        override val temperature = "温度"
        override val maxTokens = "最大 Token"
        override val timeoutSeconds = "超时(秒)"
        override val save = "保存"
        override val selectOllamaModel = "选择 Ollama 模型"
        override val discoveringLocalModels = "正在发现本地模型..."
        override val noModelsFound = "未发现可用模型"
        override val addPresetModel = "添加预置模型"
        override val addCustomModel = "添加自定义模型"
        override val noProvidersConfigured = "暂无厂商配置"
        override val noProvidersHint = "点击右下角按钮添加厂商，点击卡片进入详情"
        override val modelCountFormat = "%1\$d 个模型 · %2\$s"
        override val notConfiguredAddModel = "尚未配置 · 点击添加模型"
        override val enterDetails = "进入详情"
        override val enableProvider = "启用该厂商"
        override val enableProviderDesc = "默认关闭，开启后该厂商的模型才可用"
        override val presetModels = "预置模型"
        override val fetching = "获取中"
        override val fetchModels = "获取模型"
        override val noModelsFetched = "未获取到模型列表"
        override val fetchModelsFailed = "获取模型失败"
        override val modelsTitleFormat = "模型 (%1\$d)"
        override val noModelsHint = "暂无模型，点击上方按钮添加预置模型或从 API 获取"
        override val modelsAddedFormat = "已添加 %1\$d 个模型"
        override val noProviderConfigHint = "暂无厂商配置，请先通过[添加自定义模型]添加厂商"
        override val providerType = "厂商类型"
        override val noPresetModels = "该厂商暂无预置模型清单"
        override val selectModelsToAdd = "勾选要添加的模型："
        override val alreadyAdded = "已添加"
        override val fetchedModelsTitle = "获取到 %1\$d 个模型"
        override val etcModelsFormat = "等 %1\$d 个模型"
        override val addAll = "全部添加"
        override val addProviderHint = "添加后默认关闭，可在详情页打开该厂商"
        override val editProviderConfig = "编辑 %1\$s API 配置"
        override val editProviderHint = "修改后将应用到该厂商下的所有模型"

    }

    val zhTW: AppStrings = object : AppStrings {
        override val home = "首頁"
        override val aiChat = "AI 聊天"
        override val knowledge = "知識庫"
        override val settings = "設定"
        override val back = "返回"
        override val appearanceTitle = "外觀"
        override val appearanceSubtitle = "主題、顏色、字型、語言、縮放"
        override val providersTitle = "AI 供應商"
        override val providersSubtitle = "API Key、Base URL、模型同步"
        override val modelConfigTitle = "模型設定"
        override val modelConfigSubtitle = "預設模型、快速模型、翻譯模型"
        override val mcpTitle = "MCP 伺服器"
        override val mcpSubtitle = "伺服器管理、工具、資源、日誌"
        override val skillsTitle = "Skill 管理"
        override val skillsSubtitle = "安裝和管理 Skill 擴充功能"
        override val dataTitle = "資料管理"
        override val dataSubtitle = "備份、匯入匯出、雲端儲存"
        override val dependenciesTitle = "相依設定"
        override val dependenciesSubtitle = "Python、Node.js、本機模型"
        override val localModelsTitle = "本機模型"
        override val localModelsSubtitle = "嵌入模型、GGUF 下載、本機推論"
        override val fileProcessingTitle = "檔案處理"
        override val fileProcessingSubtitle = "PDF 解析、OCR 設定"
        override val searchTitle = "搜尋設定"
        override val searchSubtitle = "搜尋引擎、API Key、自訂執行個體"
        override val statsTitle = "用量統計"
        override val statsSubtitle = "Token 用量與費用統計"
        override val schedulesTitle = "排程任務"
        override val schedulesSubtitle = "管理排程執行的任務"
        override val notificationsTitle = "通知"
        override val notificationsSubtitle = "通知開關與類型設定"
        override val aboutTitle = "關於"
        override val aboutSubtitle = "版本資訊與開放原始碼授權"
        override val themeMode = "主題模式"
        override val followSystem = "跟隨系統"
        override val lightTheme = "亮色主題"
        override val darkTheme = "暗色主題"
        override val themeColor = "主題顏色"
        override val currentColor = "目前顏色"
        override val fontSize = "字型大小"
        override val fontSizeCurrentFormat = "目前: %1\$dsp"
        override val uiZoom = "介面縮放"
        override val zoomCurrentFormat = "目前: %1\$d%%"
        override val languageSettings = "語言設定"
        override val zhSimplified = "簡體中文"
        override val zhTraditional = "繁體中文"
        override val english = "English"
        override val japanese = "日本語"
        override val aiHubTitle = "AI 助手"
        override val aiHubChat = "對話"
        override val aiHubChatDesc = "與 AI 助手智慧對話"
        override val aiHubTranslate = "翻譯"
        override val aiHubTranslateDesc = "多語言互譯助手"
        override val aiHubCode = "程式碼助手"
        override val aiHubCodeDesc = "程式設計問題解答"
        override val aiHubNotes = "筆記"
        override val aiHubNotesDesc = "記錄與管理筆記"
        override val aiHubSearch = "搜尋"
        override val aiHubSearchDesc = "全域搜尋功能"
        override val aiHubOcr = "OCR 辨識"
        override val aiHubOcrDesc = "圖片文字辨識"
        override val chatDefaultTitle = "AI 聊天"
        override val selectModel = "選擇模型"
        override val noModelConfigured = "尚未設定模型，請到設定中新增"
        override val chatHistory = "對話歷史"
        override val newChat = "新增對話"
        override val deleteChat = "刪除對話"
        override val deleteChatMessage = "確定要刪除這則對話嗎？此操作無法復原。"
        override val send = "傳送"
        override val inputPlaceholder = "輸入訊息..."
        override val toggleSearchDesc = "搜尋增強"
        override val uploadDocument = "上傳文件"
        override val uploadImage = "上傳圖片"
        override val docAttached = "文件已附加"
        override val imageAttached = "圖片已附加"
        override val clearAttachments = "清除附件"
        override val myAvatar = "我"
        override val searchSourcesCount = "%1\$d 個搜尋來源"
        override val selectSkill = "選擇 Skill"
        override val noSkillsInstalled = "尚無已安裝的 Skill"
        override val searchDocumentsPlaceholder = "搜尋文件..."
        override val all = "全部"
        override val newCategory = "新增分類"
        override val removeCategory = "移除分類"
        override val add = "新增"
        override val emptySearchResult = "找不到相關文件"
        override val emptyCategory = "此分類下尚無文件"
        override val emptyKnowledge = "知識庫為空"
        override val emptyKnowledgeHint = "點擊右下角按鈕上傳文件或新增分類"
        override val categoryName = "分類名稱"
        override val categoryType = "分類類型"
        override val ragMode = "RAG 模式"
        override val ragModeDesc = "直接檢索文件片段並注入上下文"
        override val embeddingMode = "嵌入模式"
        override val embeddingModeDesc = "用嵌入模型向量化後進行語意檢索"
        override val create = "建立"
        override val deleteDocument = "刪除文件"
        override val deleteDocumentMessage = "確定要刪除「%1\$s」嗎？此操作無法復原。"
        override val chunkCount = "%1\$d 個片段"
        override val delete = "刪除"
        override val cancel = "取消"
        override val videoDownloadTitle = "影片下載"
        override val pasteVideoLinkHint = "貼上影片連結即可下載"
        override val urlPlaceholder = "貼上影片連結..."
        override val paste = "貼上"
        override val parseVideo = "解析影片"
        override val downloadAction = "⬇ 下載"
        override val cancelDownload = "取消下載"
        override val parsingVideo = "正在解析影片..."
        override val sourceFormat = "來源: %1\$s"
        override val videoQuality = "畫質"
        override val downloadComplete = "下載完成"
        override val openFile = "開啟"
        override val share = "分享"
        override val supportedPlatforms = "支援平台"
        override val supportedPlatformsList = "• B站 (bilibili.com)\n• YouTube\n• 抖音\n• 快手\n• 優酷\n• 更多平台陸續新增..."
        override val fileNotFound = "檔案不存在"
        override val noVideoPlayer = "找不到可用的影片播放器"
        override val shareVideo = "分享影片"
        override val shareFailedFormat = "分享失敗：%1\$s"
        override val historyTitle = "歷史紀錄"
        override val noDownloadHistory = "尚無下載紀錄"
        override val deleteHistoryTitle = "確認刪除"
        override val deleteHistoryMessage = "確定要刪除這筆下載紀錄嗎？"
        override val deleteLocalFileToo = "一併刪除本機檔案"
        override val homeFeatureVideoDownload = "影片下載"
        override val homeFeatureVideoDownloadDesc = "下載線上影片到本機"
        override val homeFeatureHistory = "歷史紀錄"
        override val homeFeatureHistoryDesc = "查看下載歷史與瀏覽紀錄"
        override val homeEngineStatus = "本機引擎 · 運行中"
        override val homeIconContentDescription = "Sparck 靈願"
        override val done = "完成"
        override val error = "錯誤"
        override val backupSettings = "備份設定"
        override val autoBackup = "自動備份"
        override val autoBackupDesc = "定期自動備份應用程式資料"
        override val backupPath = "備份路徑"
        override val backupComingSoon = "備份功能即將推出"
        override val backupNow = "立即備份"
        override val webDavStorage = "雲端儲存 (WebDAV)"
        override val username = "使用者名稱"
        override val password = "密碼"
        override val webDavComingSoon = "WebDAV 功能即將推出"
        override val testConnection = "測試連線"
        override val importExport = "資料匯入匯出"
        override val importComingSoon = "匯入功能即將推出"
        override val importData = "匯入"
        override val exportComingSoon = "匯出功能即將推出"
        override val exportData = "匯出"
        override val noteSync = "筆記同步"
        override val noteSyncComingSoon = "筆記同步功能即將推出"
        override val dataReset = "資料重設"
        override val resetDataWarning = "清除所有應用程式資料，此操作無法復原"
        override val dataResetComingSoon = "資料重設功能即將推出"
        override val resetAllData = "重設所有資料"
        override val pythonEnvironment = "Python 環境"
        override val pythonEnvironmentDesc = "設定 Python 解譯器路徑，用於執行 MCP 伺服器"
        override val pythonPath = "Python 路徑"
        override val configured = "已設定"
        override val notConfigured = "未設定"
        override val nodeJsEnvironment = "Node.js 環境"
        override val nodeJsEnvironmentDesc = "設定 Node.js 路徑，用於執行 npx 指令"
        override val nodeJsPath = "Node.js 路徑"
        override val ollamaLocalModel = "Ollama 本機模型"
        override val ollamaLocalModelDesc = "設定 Ollama 服務位址，使用本機大型模型"
        override val ollamaUrl = "Ollama URL"
        override val pullModel = "拉取模型"
        override val lmStudio = "LM Studio"
        override val lmStudioDesc = "設定 LM Studio 服務位址"
        override val lmStudioUrl = "LM Studio URL"
        override val environmentCheck = "環境相依性檢查"
        override val environmentCheckDesc = "檢查所有環境相依性是否符合執行需求"
        override val modelImportSuccess = "模型匯入成功"
        override val modelImportFailed = "模型匯入失敗"
        override val modelExists = "模型已存在"
        override val downloadFailed = "下載失敗"
        override val localModelsManagerTitle = "本機模型管理"
        override val addModel = "新增模型"
        override val downloadFromHuggingFace = "從 Hugging Face 下載"
        override val downloadFromModelScope = "從 ModelScope 下載"
        override val importLocalGguf = "匯入本機 GGUF"
        override val embeddingModel = "嵌入模型"
        override val llmModel = "LLM 模型"
        override val downloaded = "已下載"
        override val deleted = "已刪除"
        override val noEmbeddingModels = "暫無可用的嵌入模型"
        override val noLlmModels = "暫無可用的 LLM 模型"
        override val noDownloadedModels = "暫無已下載的模型"
        override val recommended = "推薦"
        override val download = "下載"
        override val downloadModelTitle = "下載模型"
        override val downloadUrlLabel = "下載連結"
        override val fileNameOptional = "檔案名稱（可選）"
        override val ggufDownloadHint = "支援從 Hugging Face 或 ModelScope 下載 GGUF 格式模型"
        override val pdfProcessing = "PDF 處理"
        override val selectPdfEngine = "選擇 PDF 解析引擎"
        override val pdfBoxRecommended = "PdfBox（推薦）"
        override val pdfBoxDesc = "Apache PdfBox，解析精度高"
        override val mupdfDesc = "輕量級，解析速度快"
        override val nativePdfRenderer = "系統內建"
        override val nativePdfDesc = "使用系統內建 PDF 渲染"
        override val ocrSettings = "OCR 設定"
        override val selectOcrEngine = "選擇 OCR 辨識引擎"
        override val mlKitRecommended = "ML Kit（推薦）"
        override val mlKitDesc = "Google ML Kit，支援多語言"
        override val paddleOcrDesc = "百度 PaddleOCR，中文辨識優異"
        override val tesseractDesc = "開源 OCR，支援 100+ 語言"
        override val documentParsing = "文件解析"
        override val autoChunking = "自動分塊"
        override val autoChunkingDesc = "長文件自動分塊處理"
        override val extractImages = "擷取圖片"
        override val extractImagesDesc = "從文件中擷取內嵌圖片"
        override val preserveFormat = "保留格式"
        override val preserveFormatDesc = "保留原始文件格式資訊"
        override val fileLimits = "檔案限制"
        override val maxFileSize = "最大檔案大小"
        override val maxFileSizeDesc = "單一檔案上傳限制"
        override val maxFileCount = "最大檔案數量"
        override val maxFileCountDesc = "單次上傳檔案數量"
        override val searchEngineSection = "搜尋引擎"
        override val selectEngine = "選擇引擎"
        override val visitWebsiteFormat = "前往 %1\$s 官方網站"
        override val apiConfig = "API 設定"
        override val apiKeyLabel = "API Key"
        override val enterApiKeyFormat = "請輸入 %1\$s API Key"
        override val hide = "隱藏"
        override val show = "顯示"
        override val apiEndpointFormat = "API 網址: %1\$s"
        override val exaMcpHint = "Exa MCP 為免費搜尋服務，前往 dashboard.exa.ai/api-keys 免費註冊取得 API Key，無需付費"
        override val searxngInstanceAddress = "SearXNG 執行個體位址"
        override val searxngInstanceHint = "請輸入您的 SearXNG 執行個體完整位址"
        override val noApiKeyFormat = "%1\$s 不需要 API Key"
        override val searchParams = "搜尋參數"
        override val maxResultsFormat = "最大結果數: %1\$d"
        override val urlContentFetch = "URL 內容取得"
        override val urlContentFetchDesc = "從網頁擷取內文的方式"
        override val builtinParser = "內建解析"
        override val builtinParserDesc = "Jsoup 本機解析，無需 API Key"
        override val jina = "Jina"
        override val jinaParserDesc = "https://r.jina.ai/ 高品質擷取"
        override val jinaWebsite = "Jina AI 官方網站"
        override val firecrawlParserDesc = "API 呼叫，需 API Key"
        override val firecrawlWebsite = "Firecrawl 官方網站"
        override val supportedEngines = "支援的引擎"
        override val requiresApiKey = "需 API Key"
        override val requiresInstance = "需執行個體"
        override val free = "免費"
        override val aboutVersionFormat = "版本 %1\$s"
        override val aboutTagline = "一個功能強大的 AI 助手應用程式"
        override val aboutFeatures = "支援多模型對話、知識管理、文件處理、MCP 整合等功能"
        override val appInfoTitle = "應用程式資訊"
        override val appNameLabel = "應用程式名稱"
        override val versionLabel = "版本號"
        override val packageNameLabel = "套件名稱"
        override val buildTimeLabel = "建置時間"
        override val systemVersionLabel = "系統版本"
        override val deviceModelLabel = "裝置型號"
        override val techStackTitle = "技術棧"
        override val devLanguageLabel = "開發語言"
        override val uiFrameworkLabel = "UI 框架"
        override val databaseLabel = "資料庫"
        override val networkLibraryLabel = "網路函式庫"
        override val minSdkLabel = "最低 SDK"
        override val targetSdkLabel = "目標 SDK"
        override val actionsTitle = "操作"
        override val checkUpdate = "檢查更新"
        override val exportDiagnostics = "匯出診斷包"
        override val feedback = "意見回饋"
        override val relatedLinksTitle = "相關連結"
        override val officialDocsLabel = "官方文件"
        override val privacyPolicyLabel = "隱私權政策"
        override val termsOfServiceLabel = "使用者條款"
        override val builtWithLabel = "基於 Kotlin + Jetpack Compose 建置"
        override val notificationSettingsTitle = "通知設定"
        override val notificationSwitchTitle = "通知開關"
        override val enableNotifications = "啟用通知"
        override val enableNotificationsDesc = "接收應用程式通知"
        override val notificationSoundTitle = "通知音效"
        override val notificationSoundDesc = "通知時播放音效"
        override val notificationVibrationTitle = "通知震動"
        override val notificationVibrationDesc = "通知時裝置震動"
        override val notificationTypesTitle = "通知類型"
        override val taskCompleteNotificationTitle = "任務完成"
        override val taskCompleteNotificationDesc = "背景任務完成時通知"
        override val taskFailNotificationTitle = "任務失敗"
        override val taskFailNotificationDesc = "背景任務失敗時通知"
        override val updateNotificationTitle = "更新提醒"
        override val updateNotificationDesc = "有新版本時通知"
        override val doNotDisturbTitle = "勿擾模式"
        override val enableDoNotDisturb = "開啟勿擾"
        override val doNotDisturbDesc = "在設定時段內不接收通知"
        override val doNotDisturbTimeFormat = "勿擾時間: %1\$s - %2\$s"
        override val noScheduledTasks = "暫無排程任務"
        override val createTaskHint = "點擊右下角按鈕建立新任務"
        override val nextRunTimeLabel = "下次執行"
        override val edit = "編輯"
        override val deleteTaskTitle = "刪除任務"
        override val deleteTaskMessage = "確定要刪除任務「%1\$s」嗎？"
        override val newTaskTitle = "新增任務"
        override val editTaskTitle = "編輯任務"
        override val taskNameLabel = "任務名稱"
        override val executionFrequencyLabel = "執行頻率"
        override val everyMinute = "每分鐘"
        override val everyFiveMinutes = "每5分鐘"
        override val everyFifteenMinutes = "每15分鐘"
        override val everyHour = "每小時"
        override val everyDayPreset = "每天（9:00）"
        override val cronExpressionLabel = "Cron 表達式"
        override val cronFormat = "Cron: %1\$s"
        override val actionTypeLabel = "動作類型"
        override val actionParamsLabel = "動作參數"
        override val confirm = "確定"
        override val totalTokensLabel = "總 Token"
        override val totalCostLabel = "總費用"
        override val totalCallsLabel = "呼叫次數"
        override val today = "今日"
        override val thisWeek = "本週"
        override val thisMonth = "本月"
        override val tokenUsageTrendTitle = "Token 用量趨勢"
        override val noData = "暫無資料"
        override val modelDistributionTitle = "依模型分佈"
        override val costEstimateTitle = "費用估算"
        override val currentPeriodCostLabel = "目前週期費用"
        override val avgCostPerCallLabel = "平均每次呼叫"
        override val tokenUnitPriceLabel = "Token 單價參考"
        override val assistantsTitle = "助手管理"
        override val assistantsWip = "助手管理功能開發中..."
        override val mcpServersTitle = "MCP 伺服器"
        override val addMcpServerHint = "點擊右下角按鈕新增伺服器"
        override val toolsListWip = "工具列表功能開發中"
        override val connectionTestWip = "連線測試功能開發中"
        override val tools = "工具"
        override val connectionType = "連線類型"
        override val urlLabel = "URL（SSE 類型）"
        override val mcpManageTitle = "MCP 管理"
        override val addServer = "新增伺服器"
        override val noMcpServers = "暫無 MCP 伺服器"
        override val reconnect = "重新連線"
        override val test = "測試"
        override val deleteServerTitle = "刪除伺服器"
        override val deleteServerConfirm = "確定要刪除「%1\$s」嗎？"
        override val connected = "已連線"
        override val connecting = "連線中"
        override val disconnected = "未連線"
        override val unknown = "未知"
        override val quickAdd = "快速新增"
        override val jsonImport = "JSON 匯入"
        override val dxtImport = "DXT 套件匯入"
        override val mcpbImport = "mcpb 套件匯入"
        override val dxtParseFailed = "DXT 檔案解析失敗"
        override val fileReadFailed = "檔案讀取失敗: %1\$s"
        override val mcpbParseFailed = "mcpb 檔案解析失敗"
        override val addMcpServer = "新增 MCP 伺服器"
        override val jsonParseFailed = "JSON 解析失敗"
        override val serverName = "伺服器名稱"
        override val connectionMethod = "連線方式"
        override val command = "指令（uvx/npx）"
        override val arguments = "參數"
        override val serverUrl = "伺服器 URL"
        override val headersLabel = "Headers（JSON 格式）"
        override val pasteMcpJsonHint = "貼上 MCP 伺服器 JSON 設定"
        override val jsonConfig = "JSON 設定"
        override val parseAndAdd = "解析並新增"
        override val selectDxtFile = "選擇 DXT 檔案（ZIP 格式）"
        override val selectFile = "選擇檔案"
        override val parseResult = "解析結果"
        override val name = "名稱"
        override val type = "類型"
        override val selectMcpbFile = "選擇 mcpb 檔案（二進位套件）"
        override val testToolTitle = "測試工具 - %1\$s"
        override val noAvailableTools = "暫無可用工具"
        override val selectTool = "選擇工具:"
        override val argsJsonLabel = "參數（JSON）"
        override val execute = "執行"
        override val result = "結果:"
        override val close = "關閉"
        override val skillManageTitle = "Skill 管理"
        override val addSkill = "新增 Skill"
        override val noSkills = "暫無 Skill"
        override val addSkillHint = "點擊 + 新增自訂 Skill"
        override val httpRequest = "HTTP 請求"
        override val textTransform = "文字轉換"
        override val description = "描述"
        override val triggerLabel = "觸發詞"
        override val actionType = "動作類型"
        override val urlPlaceholderLabel = "URL（使用 {input} 作為佔位符）"
        override val prefixLabel = "前綴"
        override val suffixLabel = "後綴"
        override val defaultModel = "預設模型"
        override val defaultModelDesc = "新增對話時使用的模型"
        override val quickModel = "快速模型"
        override val quickModelDesc = "快速回覆使用的模型"
        override val translateModel = "翻譯模型"
        override val translateModelDesc = "翻譯功能使用的模型"
        override val topicNamingModel = "話題命名模型"
        override val topicNamingModelDesc = "自動產生話題標題使用的模型"
        override val notSelected = "未選擇"
        override val modelManage = "模型管理"
        override val quickAddPresetModels = "快速新增預設模型"
        override val discoverOllamaModels = "探索 Ollama 本機模型"
        override val testing = "測試中"
        override val default = "預設"
        override val editModel = "編輯模型"
        override val provider = "廠商"
        override val modelName = "模型名稱"
        override val temperature = "溫度"
        override val maxTokens = "最大 Token"
        override val timeoutSeconds = "逾時（秒）"
        override val save = "儲存"
        override val selectOllamaModel = "選擇 Ollama 模型"
        override val discoveringLocalModels = "正在探索本機模型..."
        override val noModelsFound = "未找到可用模型"
        override val addPresetModel = "新增預設模型"
        override val addCustomModel = "新增自訂模型"
        override val noProvidersConfigured = "暫無廠商設定"
        override val noProvidersHint = "點擊右下角按鈕新增廠商，點擊卡片進入詳情"
        override val modelCountFormat = "%1\$d 個模型 · %2\$s"
        override val notConfiguredAddModel = "尚未設定 · 點擊新增模型"
        override val enterDetails = "進入詳情"
        override val enableProvider = "啟用該廠商"
        override val enableProviderDesc = "預設關閉，開啟後該廠商的模型才可用"
        override val presetModels = "預設模型"
        override val fetching = "取得中"
        override val fetchModels = "取得模型"
        override val noModelsFetched = "未取得模型列表"
        override val fetchModelsFailed = "取得模型失敗"
        override val modelsTitleFormat = "模型（%1\$d）"
        override val noModelsHint = "暫無模型，點擊上方按鈕新增預設模型或從 API 取得"
        override val modelsAddedFormat = "已新增 %1\$d 個模型"
        override val noProviderConfigHint = "暫無廠商設定，請先透過[新增自訂模型]新增廠商"
        override val providerType = "廠商類型"
        override val noPresetModels = "該廠商暫無預設模型清單"
        override val selectModelsToAdd = "勾選要新增的模型："
        override val alreadyAdded = "已新增"
        override val fetchedModelsTitle = "取得 %1\$d 個模型"
        override val etcModelsFormat = "等 %1\$d 個模型"
        override val addAll = "全部新增"
        override val addProviderHint = "新增後預設關閉，可在詳情頁開啟該廠商"
        override val editProviderConfig = "編輯 %1\$s API 設定"
        override val editProviderHint = "修改後將套用到該廠商的所有模型"

    }

    val en: AppStrings = object : AppStrings {
        override val home = "Home"
        override val aiChat = "AI Chat"
        override val knowledge = "Knowledge"
        override val settings = "Settings"
        override val back = "Back"
        override val appearanceTitle = "Appearance"
        override val appearanceSubtitle = "Theme, color, font, language, zoom"
        override val providersTitle = "AI Providers"
        override val providersSubtitle = "API key, Base URL, model sync"
        override val modelConfigTitle = "Model Settings"
        override val modelConfigSubtitle = "Default, quick and translation models"
        override val mcpTitle = "MCP Servers"
        override val mcpSubtitle = "Server management, tools, resources, logs"
        override val skillsTitle = "Skill Manager"
        override val skillsSubtitle = "Install and manage Skill extensions"
        override val dataTitle = "Data Management"
        override val dataSubtitle = "Backup, import/export, cloud storage"
        override val dependenciesTitle = "Dependencies"
        override val dependenciesSubtitle = "Python, Node.js, local models"
        override val localModelsTitle = "Local Models"
        override val localModelsSubtitle = "Embedding models, GGUF download, local inference"
        override val fileProcessingTitle = "File Processing"
        override val fileProcessingSubtitle = "PDF parsing, OCR settings"
        override val searchTitle = "Search Settings"
        override val searchSubtitle = "Search engine, API key, custom instance"
        override val statsTitle = "Usage Stats"
        override val statsSubtitle = "Token usage and cost statistics"
        override val schedulesTitle = "Scheduled Tasks"
        override val schedulesSubtitle = "Manage scheduled tasks"
        override val notificationsTitle = "Notifications"
        override val notificationsSubtitle = "Notification toggles and types"
        override val aboutTitle = "About"
        override val aboutSubtitle = "Version info and open-source licenses"
        override val themeMode = "Theme Mode"
        override val followSystem = "Follow System"
        override val lightTheme = "Light Theme"
        override val darkTheme = "Dark Theme"
        override val themeColor = "Theme Color"
        override val currentColor = "Current Color"
        override val fontSize = "Font Size"
        override val fontSizeCurrentFormat = "Current: %1\$dsp"
        override val uiZoom = "UI Zoom"
        override val zoomCurrentFormat = "Current: %1\$d%%"
        override val languageSettings = "Language"
        override val zhSimplified = "Simplified Chinese"
        override val zhTraditional = "Traditional Chinese"
        override val english = "English"
        override val japanese = "Japanese"
        override val aiHubTitle = "AI Assistant"
        override val aiHubChat = "Chat"
        override val aiHubChatDesc = "Smart conversations with the AI assistant"
        override val aiHubTranslate = "Translate"
        override val aiHubTranslateDesc = "Multilingual translation assistant"
        override val aiHubCode = "Code Assistant"
        override val aiHubCodeDesc = "Answers to programming questions"
        override val aiHubNotes = "Notes"
        override val aiHubNotesDesc = "Record and manage notes"
        override val aiHubSearch = "Search"
        override val aiHubSearchDesc = "Global search"
        override val aiHubOcr = "OCR"
        override val aiHubOcrDesc = "Extract text from images"
        override val chatDefaultTitle = "AI Chat"
        override val selectModel = "Select model"
        override val noModelConfigured = "No model configured. Add one in Settings."
        override val chatHistory = "Conversation history"
        override val newChat = "New chat"
        override val deleteChat = "Delete conversation"
        override val deleteChatMessage = "Delete this conversation? This action cannot be undone."
        override val send = "Send"
        override val inputPlaceholder = "Type a message..."
        override val toggleSearchDesc = "Web search"
        override val uploadDocument = "Upload document"
        override val uploadImage = "Upload image"
        override val docAttached = "Document attached"
        override val imageAttached = "Image attached"
        override val clearAttachments = "Clear attachments"
        override val myAvatar = "Me"
        override val searchSourcesCount = "%1\$d search sources"
        override val selectSkill = "Select a Skill"
        override val noSkillsInstalled = "No Skills installed yet"
        override val searchDocumentsPlaceholder = "Search documents..."
        override val all = "All"
        override val newCategory = "New category"
        override val removeCategory = "Remove category"
        override val add = "Add"
        override val emptySearchResult = "No matching documents"
        override val emptyCategory = "No documents in this category"
        override val emptyKnowledge = "Knowledge base is empty"
        override val emptyKnowledgeHint = "Tap the button at the bottom right to upload documents or create a category"
        override val categoryName = "Category name"
        override val categoryType = "Category type"
        override val ragMode = "RAG mode"
        override val ragModeDesc = "Retrieve document chunks and inject them into context"
        override val embeddingMode = "Embedding mode"
        override val embeddingModeDesc = "Vectorize with an embedding model for semantic search"
        override val create = "Create"
        override val deleteDocument = "Delete document"
        override val deleteDocumentMessage = "Delete \"%1\$s\"? This action cannot be undone."
        override val chunkCount = "%1\$d chunks"
        override val delete = "Delete"
        override val cancel = "Cancel"
        override val videoDownloadTitle = "Video Download"
        override val pasteVideoLinkHint = "Paste a video link to download"
        override val urlPlaceholder = "Paste video link..."
        override val paste = "Paste"
        override val parseVideo = "Parse video"
        override val downloadAction = "⬇ Download"
        override val cancelDownload = "Cancel download"
        override val parsingVideo = "Parsing video..."
        override val sourceFormat = "Source: %1\$s"
        override val videoQuality = "Quality"
        override val downloadComplete = "Download complete"
        override val openFile = "Open"
        override val share = "Share"
        override val supportedPlatforms = "Supported platforms"
        override val supportedPlatformsList = "• Bilibili (bilibili.com)\n• YouTube\n• Douyin\n• Kuaishou\n• Youku\n• More platforms coming soon..."
        override val fileNotFound = "File not found"
        override val noVideoPlayer = "No video player available"
        override val shareVideo = "Share video"
        override val shareFailedFormat = "Share failed: %1\$s"
        override val historyTitle = "Download History"
        override val noDownloadHistory = "No download history yet"
        override val deleteHistoryTitle = "Delete Record"
        override val deleteHistoryMessage = "Delete this download record?"
        override val deleteLocalFileToo = "Also delete the local file"
        override val homeFeatureVideoDownload = "Video Download"
        override val homeFeatureVideoDownloadDesc = "Download online videos to your device"
        override val homeFeatureHistory = "Download History"
        override val homeFeatureHistoryDesc = "View downloads and browsing history"
        override val homeEngineStatus = "Local engine · Running"
        override val homeIconContentDescription = "Sparck"
        override val done = "Done"
        override val error = "Error"
        override val backupSettings = "Backup Settings"
        override val autoBackup = "Auto Backup"
        override val autoBackupDesc = "Regularly back up app data automatically"
        override val backupPath = "Backup Path"
        override val backupComingSoon = "Backup feature is coming soon"
        override val backupNow = "Back Up Now"
        override val webDavStorage = "Cloud Storage (WebDAV)"
        override val username = "Username"
        override val password = "Password"
        override val webDavComingSoon = "WebDAV feature is coming soon"
        override val testConnection = "Test Connection"
        override val importExport = "Data Import / Export"
        override val importComingSoon = "Import feature is coming soon"
        override val importData = "Import"
        override val exportComingSoon = "Export feature is coming soon"
        override val exportData = "Export"
        override val noteSync = "Note Sync"
        override val noteSyncComingSoon = "Note sync feature is coming soon"
        override val dataReset = "Data Reset"
        override val resetDataWarning = "Clears all app data. This action cannot be undone."
        override val dataResetComingSoon = "Data reset feature is coming soon"
        override val resetAllData = "Reset All Data"
        override val pythonEnvironment = "Python Environment"
        override val pythonEnvironmentDesc = "Configure the Python interpreter path used to run MCP servers"
        override val pythonPath = "Python Path"
        override val configured = "Configured"
        override val notConfigured = "Not Configured"
        override val nodeJsEnvironment = "Node.js Environment"
        override val nodeJsEnvironmentDesc = "Configure the Node.js path used to run npx commands"
        override val nodeJsPath = "Node.js Path"
        override val ollamaLocalModel = "Ollama Local Models"
        override val ollamaLocalModelDesc = "Configure the Ollama service address to use local large models"
        override val ollamaUrl = "Ollama URL"
        override val pullModel = "Pull Model"
        override val lmStudio = "LM Studio"
        override val lmStudioDesc = "Configure the LM Studio service address"
        override val lmStudioUrl = "LM Studio URL"
        override val environmentCheck = "Environment Dependencies Check"
        override val environmentCheckDesc = "Check whether all environment dependencies meet the runtime requirements"
        override val modelImportSuccess = "Model imported successfully"
        override val modelImportFailed = "Failed to import model"
        override val modelExists = "Model already exists"
        override val downloadFailed = "Download failed"
        override val localModelsManagerTitle = "Local Model Management"
        override val addModel = "Add Model"
        override val downloadFromHuggingFace = "Download from Hugging Face"
        override val downloadFromModelScope = "Download from ModelScope"
        override val importLocalGguf = "Import Local GGUF"
        override val embeddingModel = "Embedding Model"
        override val llmModel = "LLM Model"
        override val downloaded = "Downloaded"
        override val deleted = "Deleted"
        override val noEmbeddingModels = "No embedding models available"
        override val noLlmModels = "No LLM models available"
        override val noDownloadedModels = "No downloaded models yet"
        override val recommended = "Recommended"
        override val download = "Download"
        override val downloadModelTitle = "Download Model"
        override val downloadUrlLabel = "Download URL"
        override val fileNameOptional = "File Name (optional)"
        override val ggufDownloadHint = "Supports downloading GGUF models from Hugging Face or ModelScope"
        override val pdfProcessing = "PDF Processing"
        override val selectPdfEngine = "Select PDF Parsing Engine"
        override val pdfBoxRecommended = "PdfBox (Recommended)"
        override val pdfBoxDesc = "Apache PdfBox, high parsing accuracy"
        override val mupdfDesc = "Lightweight and fast parsing"
        override val nativePdfRenderer = "System Native"
        override val nativePdfDesc = "Use the system's built-in PDF rendering"
        override val ocrSettings = "OCR Settings"
        override val selectOcrEngine = "Select OCR Engine"
        override val mlKitRecommended = "ML Kit (Recommended)"
        override val mlKitDesc = "Google ML Kit, supports multiple languages"
        override val paddleOcrDesc = "Baidu PaddleOCR, excellent Chinese OCR"
        override val tesseractDesc = "Open-source OCR, supports 100+ languages"
        override val documentParsing = "Document Parsing"
        override val autoChunking = "Auto Chunking"
        override val autoChunkingDesc = "Automatically chunk long documents for processing"
        override val extractImages = "Extract Images"
        override val extractImagesDesc = "Extract embedded images from documents"
        override val preserveFormat = "Preserve Format"
        override val preserveFormatDesc = "Preserve the original document formatting"
        override val fileLimits = "File Limits"
        override val maxFileSize = "Max File Size"
        override val maxFileSizeDesc = "Limit for a single file upload"
        override val maxFileCount = "Max File Count"
        override val maxFileCountDesc = "Number of files per upload"
        override val searchEngineSection = "Search Engine"
        override val selectEngine = "Select Engine"
        override val visitWebsiteFormat = "Visit the %1\$s website"
        override val apiConfig = "API Configuration"
        override val apiKeyLabel = "API Key"
        override val enterApiKeyFormat = "Please enter your %1\$s API Key"
        override val hide = "Hide"
        override val show = "Show"
        override val apiEndpointFormat = "API Endpoint: %1\$s"
        override val exaMcpHint = "Exa MCP is a free search service. Sign up for a free API Key at dashboard.exa.ai/api-keys, no payment required"
        override val searxngInstanceAddress = "SearXNG Instance Address"
        override val searxngInstanceHint = "Please enter the full address of your SearXNG instance"
        override val noApiKeyFormat = "%1\$s does not require an API Key"
        override val searchParams = "Search Parameters"
        override val maxResultsFormat = "Max results: %1\$d"
        override val urlContentFetch = "URL Content Fetching"
        override val urlContentFetchDesc = "How to extract the main content from a web page"
        override val builtinParser = "Built-in Parser"
        override val builtinParserDesc = "Jsoup local parsing, no API Key required"
        override val jina = "Jina"
        override val jinaParserDesc = "https://r.jina.ai/ high-quality extraction"
        override val jinaWebsite = "Jina AI Website"
        override val firecrawlParserDesc = "API calls, requires an API Key"
        override val firecrawlWebsite = "Firecrawl Website"
        override val supportedEngines = "Supported Engines"
        override val requiresApiKey = "Requires API Key"
        override val requiresInstance = "Requires Instance"
        override val free = "Free"
        override val aboutVersionFormat = "Version %1\$s"
        override val aboutTagline = "A powerful AI assistant app"
        override val aboutFeatures = "Supports multi-model chat, knowledge management, document processing, MCP integration, and more"
        override val appInfoTitle = "App Info"
        override val appNameLabel = "App Name"
        override val versionLabel = "Version"
        override val packageNameLabel = "Package Name"
        override val buildTimeLabel = "Build Time"
        override val systemVersionLabel = "System Version"
        override val deviceModelLabel = "Device Model"
        override val techStackTitle = "Tech Stack"
        override val devLanguageLabel = "Development Language"
        override val uiFrameworkLabel = "UI Framework"
        override val databaseLabel = "Database"
        override val networkLibraryLabel = "Network Library"
        override val minSdkLabel = "Min SDK"
        override val targetSdkLabel = "Target SDK"
        override val actionsTitle = "Actions"
        override val checkUpdate = "Check for Updates"
        override val exportDiagnostics = "Export Diagnostics"
        override val feedback = "Feedback"
        override val relatedLinksTitle = "Related Links"
        override val officialDocsLabel = "Official Docs"
        override val privacyPolicyLabel = "Privacy Policy"
        override val termsOfServiceLabel = "Terms of Service"
        override val builtWithLabel = "Built with Kotlin + Jetpack Compose"
        override val notificationSettingsTitle = "Notification Settings"
        override val notificationSwitchTitle = "Notification Toggle"
        override val enableNotifications = "Enable Notifications"
        override val enableNotificationsDesc = "Receive app notifications"
        override val notificationSoundTitle = "Notification Sound"
        override val notificationSoundDesc = "Play a sound on notification"
        override val notificationVibrationTitle = "Notification Vibration"
        override val notificationVibrationDesc = "Vibrate on notification"
        override val notificationTypesTitle = "Notification Types"
        override val taskCompleteNotificationTitle = "Task Completed"
        override val taskCompleteNotificationDesc = "Notify when a background task completes"
        override val taskFailNotificationTitle = "Task Failed"
        override val taskFailNotificationDesc = "Notify when a background task fails"
        override val updateNotificationTitle = "Update Reminder"
        override val updateNotificationDesc = "Notify when a new version is available"
        override val doNotDisturbTitle = "Do Not Disturb"
        override val enableDoNotDisturb = "Enable Do Not Disturb"
        override val doNotDisturbDesc = "Do not receive notifications during the set time period"
        override val doNotDisturbTimeFormat = "Do Not Disturb time: %1\$s - %2\$s"
        override val noScheduledTasks = "No scheduled tasks yet"
        override val createTaskHint = "Tap the button at the bottom right to create a new task"
        override val nextRunTimeLabel = "Next Run"
        override val edit = "Edit"
        override val deleteTaskTitle = "Delete Task"
        override val deleteTaskMessage = "Are you sure you want to delete task \"%1\$s\"?"
        override val newTaskTitle = "New Task"
        override val editTaskTitle = "Edit Task"
        override val taskNameLabel = "Task Name"
        override val executionFrequencyLabel = "Execution Frequency"
        override val everyMinute = "Every Minute"
        override val everyFiveMinutes = "Every 5 Minutes"
        override val everyFifteenMinutes = "Every 15 Minutes"
        override val everyHour = "Every Hour"
        override val everyDayPreset = "Every Day (9:00)"
        override val cronExpressionLabel = "Cron Expression"
        override val cronFormat = "Cron: %1\$s"
        override val actionTypeLabel = "Action Type"
        override val actionParamsLabel = "Action Params"
        override val confirm = "Confirm"
        override val totalTokensLabel = "Total Tokens"
        override val totalCostLabel = "Total Cost"
        override val totalCallsLabel = "Total Calls"
        override val today = "Today"
        override val thisWeek = "This Week"
        override val thisMonth = "This Month"
        override val tokenUsageTrendTitle = "Token Usage Trend"
        override val noData = "No data available"
        override val modelDistributionTitle = "By Model"
        override val costEstimateTitle = "Cost Estimate"
        override val currentPeriodCostLabel = "Current Period Cost"
        override val avgCostPerCallLabel = "Average Cost per Call"
        override val tokenUnitPriceLabel = "Token Unit Price Reference"
        override val assistantsTitle = "Assistant Management"
        override val assistantsWip = "Assistant management feature is in development..."
        override val mcpServersTitle = "MCP Servers"
        override val addMcpServerHint = "Tap the button at the bottom right to add a server"
        override val toolsListWip = "Tools list feature is in development"
        override val connectionTestWip = "Connection test feature is in development"
        override val tools = "Tools"
        override val connectionType = "Connection Type"
        override val urlLabel = "URL (SSE)"
        override val mcpManageTitle = "MCP Management"
        override val addServer = "Add Server"
        override val noMcpServers = "No MCP servers yet"
        override val reconnect = "Reconnect"
        override val test = "Test"
        override val deleteServerTitle = "Delete Server"
        override val deleteServerConfirm = "Are you sure you want to delete \"%1\$s\"?"
        override val connected = "Connected"
        override val connecting = "Connecting"
        override val disconnected = "Disconnected"
        override val unknown = "Unknown"
        override val quickAdd = "Quick Add"
        override val jsonImport = "JSON Import"
        override val dxtImport = "DXT Package Import"
        override val mcpbImport = "mcpb Package Import"
        override val dxtParseFailed = "Failed to parse DXT file"
        override val fileReadFailed = "Failed to read file: %1\$s"
        override val mcpbParseFailed = "Failed to parse mcpb file"
        override val addMcpServer = "Add MCP Server"
        override val jsonParseFailed = "Failed to parse JSON"
        override val serverName = "Server Name"
        override val connectionMethod = "Connection Method"
        override val command = "Command (uvx/npx)"
        override val arguments = "Arguments"
        override val serverUrl = "Server URL"
        override val headersLabel = "Headers (JSON format)"
        override val pasteMcpJsonHint = "Paste the MCP server JSON configuration"
        override val jsonConfig = "JSON Configuration"
        override val parseAndAdd = "Parse and Add"
        override val selectDxtFile = "Select DXT File (ZIP format)"
        override val selectFile = "Select File"
        override val parseResult = "Parse Result"
        override val name = "Name"
        override val type = "Type"
        override val selectMcpbFile = "Select mcpb File (binary package)"
        override val testToolTitle = "Test Tool - %1\$s"
        override val noAvailableTools = "No tools available"
        override val selectTool = "Select Tool:"
        override val argsJsonLabel = "Arguments (JSON)"
        override val execute = "Execute"
        override val result = "Result:"
        override val close = "Close"
        override val skillManageTitle = "Skill Management"
        override val addSkill = "Add Skill"
        override val noSkills = "No Skills"
        override val addSkillHint = "Tap + to add a custom Skill"
        override val httpRequest = "HTTP Request"
        override val textTransform = "Text Transform"
        override val description = "Description"
        override val triggerLabel = "Trigger Word"
        override val actionType = "Action Type"
        override val urlPlaceholderLabel = "URL (use {input} as a placeholder)"
        override val prefixLabel = "Prefix"
        override val suffixLabel = "Suffix"
        override val defaultModel = "Default Model"
        override val defaultModelDesc = "Model used when starting a new chat"
        override val quickModel = "Quick Model"
        override val quickModelDesc = "Model used for quick replies"
        override val translateModel = "Translation Model"
        override val translateModelDesc = "Model used for translation"
        override val topicNamingModel = "Topic Naming Model"
        override val topicNamingModelDesc = "Model used to auto-generate conversation titles"
        override val notSelected = "Not Selected"
        override val modelManage = "Model Management"
        override val quickAddPresetModels = "Quick Add Preset Models"
        override val discoverOllamaModels = "Discover Ollama Local Models"
        override val testing = "Testing"
        override val default = "Default"
        override val editModel = "Edit Model"
        override val provider = "Provider"
        override val modelName = "Model Name"
        override val temperature = "Temperature"
        override val maxTokens = "Max Tokens"
        override val timeoutSeconds = "Timeout (seconds)"
        override val save = "Save"
        override val selectOllamaModel = "Select Ollama Model"
        override val discoveringLocalModels = "Discovering local models..."
        override val noModelsFound = "No models found"
        override val addPresetModel = "Add Preset Model"
        override val addCustomModel = "Add Custom Model"
        override val noProvidersConfigured = "No providers configured"
        override val noProvidersHint = "Tap the button at the bottom right to add a provider, then tap the card for details"
        override val modelCountFormat = "%1\$d models · %2\$s"
        override val notConfiguredAddModel = "Not configured · Tap to add a model"
        override val enterDetails = "View Details"
        override val enableProvider = "Enable This Provider"
        override val enableProviderDesc = "Disabled by default; the provider's models are only available once enabled"
        override val presetModels = "Preset Models"
        override val fetching = "Fetching"
        override val fetchModels = "Fetch Models"
        override val noModelsFetched = "No model list retrieved"
        override val fetchModelsFailed = "Failed to fetch models"
        override val modelsTitleFormat = "Models (%1\$d)"
        override val noModelsHint = "No models yet. Tap the button above to add preset models or fetch them from the API"
        override val modelsAddedFormat = "Added %1\$d models"
        override val noProviderConfigHint = "No providers configured. Add a provider via \"Add Custom Model\" first"
        override val providerType = "Provider Type"
        override val noPresetModels = "This provider has no preset model list"
        override val selectModelsToAdd = "Select the models to add:"
        override val alreadyAdded = "Already Added"
        override val fetchedModelsTitle = "Fetched %1\$d models"
        override val etcModelsFormat = "and %1\$d more models"
        override val addAll = "Add All"
        override val addProviderHint = "Disabled by default after adding; you can enable the provider on its detail page"
        override val editProviderConfig = "Edit %1\$s API Configuration"
        override val editProviderHint = "Changes will apply to all models under this provider"

    }

    val ja: AppStrings = object : AppStrings {
        override val home = "ホーム"
        override val aiChat = "AIチャット"
        override val knowledge = "ナレッジベース"
        override val settings = "設定"
        override val back = "戻る"
        override val appearanceTitle = "外観"
        override val appearanceSubtitle = "テーマ、色、フォント、言語、ズーム"
        override val providersTitle = "AIプロバイダー"
        override val providersSubtitle = "APIキー、ベースURL、モデル同期"
        override val modelConfigTitle = "モデル設定"
        override val modelConfigSubtitle = "デフォルト、クイック、翻訳モデル"
        override val mcpTitle = "MCPサーバー"
        override val mcpSubtitle = "サーバー管理、ツール、リソース、ログ"
        override val skillsTitle = "Skill管理"
        override val skillsSubtitle = "Skill拡張機能のインストールと管理"
        override val dataTitle = "データ管理"
        override val dataSubtitle = "バックアップ、インポート/エクスポート、クラウドストレージ"
        override val dependenciesTitle = "依存設定"
        override val dependenciesSubtitle = "Python、Node.js、ローカルモデル"
        override val localModelsTitle = "ローカルモデル"
        override val localModelsSubtitle = "埋め込みモデル、GGUFダウンロード、ローカル推論"
        override val fileProcessingTitle = "ファイル処理"
        override val fileProcessingSubtitle = "PDF解析、OCR設定"
        override val searchTitle = "検索設定"
        override val searchSubtitle = "検索エンジン、APIキー、カスタムインスタンス"
        override val statsTitle = "使用量統計"
        override val statsSubtitle = "トークン使用量とコスト統計"
        override val schedulesTitle = "スケジュール"
        override val schedulesSubtitle = "スケジュール実行の管理"
        override val notificationsTitle = "通知"
        override val notificationsSubtitle = "通知スイッチとタイプ設定"
        override val aboutTitle = "情報"
        override val aboutSubtitle = "バージョン情報とオープンソースライセンス"
        override val themeMode = "テーマモード"
        override val followSystem = "システムに従う"
        override val lightTheme = "ライトテーマ"
        override val darkTheme = "ダークテーマ"
        override val themeColor = "テーマカラー"
        override val currentColor = "現在の色"
        override val fontSize = "フォントサイズ"
        override val fontSizeCurrentFormat = "現在: %1\$dsp"
        override val uiZoom = "UIズーム"
        override val zoomCurrentFormat = "現在: %1\$d%%"
        override val languageSettings = "言語設定"
        override val zhSimplified = "簡体字中国語"
        override val zhTraditional = "繁体字中国語"
        override val english = "English"
        override val japanese = "日本語"
        override val aiHubTitle = "AIアシスタント"
        override val aiHubChat = "チャット"
        override val aiHubChatDesc = "AIアシスタントとスマートに会話"
        override val aiHubTranslate = "翻訳"
        override val aiHubTranslateDesc = "多言語翻訳アシスタント"
        override val aiHubCode = "コードアシスタント"
        override val aiHubCodeDesc = "プログラミングの質問に回答"
        override val aiHubNotes = "メモ"
        override val aiHubNotesDesc = "メモの記録と管理"
        override val aiHubSearch = "検索"
        override val aiHubSearchDesc = "全体検索"
        override val aiHubOcr = "OCR"
        override val aiHubOcrDesc = "画像から文字を抽出"
        override val chatDefaultTitle = "AIチャット"
        override val selectModel = "モデルを選択"
        override val noModelConfigured = "モデルが設定されていません。設定から追加してください"
        override val chatHistory = "会話履歴"
        override val newChat = "新しい会話"
        override val deleteChat = "会話を削除"
        override val deleteChatMessage = "この会話を削除しますか？この操作は元に戻せません。"
        override val send = "送信"
        override val inputPlaceholder = "メッセージを入力..."
        override val toggleSearchDesc = "検索強化"
        override val uploadDocument = "ドキュメントをアップロード"
        override val uploadImage = "画像をアップロード"
        override val docAttached = "ドキュメント添付済み"
        override val imageAttached = "画像添付済み"
        override val clearAttachments = "添付をクリア"
        override val myAvatar = "私"
        override val searchSourcesCount = "%1\$d 件の検索ソース"
        override val selectSkill = "Skill を選択"
        override val noSkillsInstalled = "インストール済みの Skill はありません"
        override val searchDocumentsPlaceholder = "ドキュメントを検索..."
        override val all = "すべて"
        override val newCategory = "新しいカテゴリ"
        override val removeCategory = "カテゴリを削除"
        override val add = "追加"
        override val emptySearchResult = "該当するドキュメントがありません"
        override val emptyCategory = "このカテゴリにはドキュメントがありません"
        override val emptyKnowledge = "ナレッジベースは空です"
        override val emptyKnowledgeHint = "右下のボタンからドキュメントをアップロードするか、カテゴリを作成してください"
        override val categoryName = "カテゴリ名"
        override val categoryType = "カテゴリの種類"
        override val ragMode = "RAG モード"
        override val ragModeDesc = "ドキュメントの断片を直接検索してコンテキストに注入"
        override val embeddingMode = "埋め込みモード"
        override val embeddingModeDesc = "埋め込みモデルでベクトル化して意味検索"
        override val create = "作成"
        override val deleteDocument = "ドキュメントを削除"
        override val deleteDocumentMessage = "「%1\$s」を削除しますか？この操作は元に戻せません。"
        override val chunkCount = "%1\$d チャンク"
        override val delete = "削除"
        override val cancel = "キャンセル"
        override val videoDownloadTitle = "動画ダウンロード"
        override val pasteVideoLinkHint = "動画リンクを貼り付けてダウンロード"
        override val urlPlaceholder = "動画リンクを貼り付け..."
        override val paste = "貼り付け"
        override val parseVideo = "動画を解析"
        override val downloadAction = "⬇ ダウンロード"
        override val cancelDownload = "ダウンロードをキャンセル"
        override val parsingVideo = "動画を解析中..."
        override val sourceFormat = "出典: %1\$s"
        override val videoQuality = "画質"
        override val downloadComplete = "ダウンロード完了"
        override val openFile = "開く"
        override val share = "共有"
        override val supportedPlatforms = "対応プラットフォーム"
        override val supportedPlatformsList = "• Bilibili (bilibili.com)\n• YouTube\n• Douyin\n• Kuaishou\n• Youku\n• 他のプラットフォームも順次追加予定..."
        override val fileNotFound = "ファイルが存在しません"
        override val noVideoPlayer = "動画プレーヤーが見つかりません"
        override val shareVideo = "動画を共有"
        override val shareFailedFormat = "共有に失敗しました: %1\$s"
        override val historyTitle = "ダウンロード履歴"
        override val noDownloadHistory = "ダウンロード履歴はありません"
        override val deleteHistoryTitle = "履歴を削除"
        override val deleteHistoryMessage = "このダウンロード履歴を削除しますか？"
        override val deleteLocalFileToo = "ローカルファイルも削除する"
        override val homeFeatureVideoDownload = "動画ダウンロード"
        override val homeFeatureVideoDownloadDesc = "オンライン動画を端末に保存"
        override val homeFeatureHistory = "ダウンロード履歴"
        override val homeFeatureHistoryDesc = "ダウンロードと閲覧履歴を表示"
        override val homeEngineStatus = "ローカルエンジン · 稼働中"
        override val homeIconContentDescription = "Sparck"
        override val done = "完了"
        override val error = "エラー"
        override val backupSettings = "バックアップ設定"
        override val autoBackup = "自動バックアップ"
        override val autoBackupDesc = "アプリデータを定期的に自動バックアップ"
        override val backupPath = "バックアップ先パス"
        override val backupComingSoon = "バックアップ機能は近日公開予定"
        override val backupNow = "今すぐバックアップ"
        override val webDavStorage = "クラウドストレージ (WebDAV)"
        override val username = "ユーザー名"
        override val password = "パスワード"
        override val webDavComingSoon = "WebDAV 機能は近日公開予定"
        override val testConnection = "接続テスト"
        override val importExport = "データのインポート・エクスポート"
        override val importComingSoon = "インポート機能は近日公開予定"
        override val importData = "インポート"
        override val exportComingSoon = "エクスポート機能は近日公開予定"
        override val exportData = "エクスポート"
        override val noteSync = "ノート同期"
        override val noteSyncComingSoon = "ノート同期機能は近日公開予定"
        override val dataReset = "データリセット"
        override val resetDataWarning = "すべてのアプリデータを削除します。この操作は元に戻せません"
        override val dataResetComingSoon = "データリセット機能は近日公開予定"
        override val resetAllData = "すべてのデータをリセット"
        override val pythonEnvironment = "Python 環境"
        override val pythonEnvironmentDesc = "MCP サーバーを実行するための Python インタープリタのパスを設定します"
        override val pythonPath = "Python パス"
        override val configured = "設定済み"
        override val notConfigured = "未設定"
        override val nodeJsEnvironment = "Node.js 環境"
        override val nodeJsEnvironmentDesc = "npx コマンドを実行するための Node.js パスを設定します"
        override val nodeJsPath = "Node.js パス"
        override val ollamaLocalModel = "Ollama ローカルモデル"
        override val ollamaLocalModelDesc = "Ollama サービスのURLを設定して、ローカルの大規模モデルを使用します"
        override val ollamaUrl = "Ollama URL"
        override val pullModel = "モデルを取得"
        override val lmStudio = "LM Studio"
        override val lmStudioDesc = "LM Studio サービスのURLを設定します"
        override val lmStudioUrl = "LM Studio URL"
        override val environmentCheck = "環境依存関係の確認"
        override val environmentCheckDesc = "すべての環境依存関係が実行要件を満たしているか確認します"
        override val modelImportSuccess = "モデルのインポートに成功しました"
        override val modelImportFailed = "モデルのインポートに失敗しました"
        override val modelExists = "モデルは既に存在します"
        override val downloadFailed = "ダウンロードに失敗しました"
        override val localModelsManagerTitle = "ローカルモデル管理"
        override val addModel = "モデルを追加"
        override val downloadFromHuggingFace = "Hugging Face からダウンロード"
        override val downloadFromModelScope = "ModelScope からダウンロード"
        override val importLocalGguf = "ローカル GGUF をインポート"
        override val embeddingModel = "埋め込みモデル"
        override val llmModel = "LLM モデル"
        override val downloaded = "ダウンロード済み"
        override val deleted = "削除済み"
        override val noEmbeddingModels = "利用可能な埋め込みモデルがありません"
        override val noLlmModels = "利用可能な LLM モデルがありません"
        override val noDownloadedModels = "ダウンロード済みのモデルがありません"
        override val recommended = "おすすめ"
        override val download = "ダウンロード"
        override val downloadModelTitle = "モデルをダウンロード"
        override val downloadUrlLabel = "ダウンロードリンク"
        override val fileNameOptional = "ファイル名 (任意)"
        override val ggufDownloadHint = "Hugging Face または ModelScope から GGUF 形式のモデルをダウンロードできます"
        override val pdfProcessing = "PDF 処理"
        override val selectPdfEngine = "PDF 解析エンジンを選択"
        override val pdfBoxRecommended = "PdfBox (おすすめ)"
        override val pdfBoxDesc = "Apache PdfBox、解析精度が高い"
        override val mupdfDesc = "軽量で、解析速度が速い"
        override val nativePdfRenderer = "システムネイティブ"
        override val nativePdfDesc = "システム内蔵の PDF レンダリングを使用"
        override val ocrSettings = "OCR 設定"
        override val selectOcrEngine = "OCR 認識エンジンを選択"
        override val mlKitRecommended = "ML Kit (おすすめ)"
        override val mlKitDesc = "Google ML Kit、多言語対応"
        override val paddleOcrDesc = "百度 PaddleOCR、中国語認識に優れる"
        override val tesseractDesc = "オープンソース OCR、100 以上の言語に対応"
        override val documentParsing = "ドキュメント解析"
        override val autoChunking = "自動分割"
        override val autoChunkingDesc = "長いドキュメントを自動で分割処理"
        override val extractImages = "画像を抽出"
        override val extractImagesDesc = "ドキュメントから埋め込まれた画像を抽出"
        override val preserveFormat = "フォーマットを保持"
        override val preserveFormatDesc = "元のドキュメントのフォーマット情報を保持"
        override val fileLimits = "ファイル制限"
        override val maxFileSize = "最大ファイルサイズ"
        override val maxFileSizeDesc = "1 ファイルあたりのアップロード上限"
        override val maxFileCount = "最大ファイル数"
        override val maxFileCountDesc = "1 回のアップロードで指定できるファイル数"
        override val searchEngineSection = "検索エンジン"
        override val selectEngine = "エンジンを選択"
        override val visitWebsiteFormat = "%1\$s の公式サイトへアクセス"
        override val apiConfig = "API 設定"
        override val apiKeyLabel = "API Key"
        override val enterApiKeyFormat = "%1\$s の API Key を入力してください"
        override val hide = "非表示"
        override val show = "表示"
        override val apiEndpointFormat = "API アドレス: %1\$s"
        override val exaMcpHint = "Exa MCP は無料の検索サービスです。dashboard.exa.ai/api-keys で無料登録して API Key を取得してください。支払いは不要です"
        override val searxngInstanceAddress = "SearXNG インスタンスのアドレス"
        override val searxngInstanceHint = "SearXNG インスタンスの完全なアドレスを入力してください"
        override val noApiKeyFormat = "%1\$s は API Key 不要です"
        override val searchParams = "検索パラメータ"
        override val maxResultsFormat = "最大結果数: %1\$d"
        override val urlContentFetch = "URL コンテンツ取得"
        override val urlContentFetchDesc = "ウェブページから本文を抽出する方法"
        override val builtinParser = "内蔵パーサー"
        override val builtinParserDesc = "Jsoup によるローカル解析、API Key 不要"
        override val jina = "Jina"
        override val jinaParserDesc = "https://r.jina.ai/ による高品質な抽出"
        override val jinaWebsite = "Jina AI 公式サイト"
        override val firecrawlParserDesc = "API 呼び出し、API Key が必要"
        override val firecrawlWebsite = "Firecrawl 公式サイト"
        override val supportedEngines = "対応エンジン"
        override val requiresApiKey = "API Key 必要"
        override val requiresInstance = "インスタンス必要"
        override val free = "無料"
        override val aboutVersionFormat = "バージョン %1\$s"
        override val aboutTagline = "強力な AI アシスタントアプリ"
        override val aboutFeatures = "マルチモデル対話、ナレッジ管理、ドキュメント処理、MCP 連携などの機能に対応"
        override val appInfoTitle = "アプリ情報"
        override val appNameLabel = "アプリ名"
        override val versionLabel = "バージョン番号"
        override val packageNameLabel = "パッケージ名"
        override val buildTimeLabel = "ビルド時間"
        override val systemVersionLabel = "システムバージョン"
        override val deviceModelLabel = "デバイスモデル"
        override val techStackTitle = "テクノロジースタック"
        override val devLanguageLabel = "開発言語"
        override val uiFrameworkLabel = "UI フレームワーク"
        override val databaseLabel = "データベース"
        override val networkLibraryLabel = "ネットワークライブラリ"
        override val minSdkLabel = "最小 SDK"
        override val targetSdkLabel = "ターゲット SDK"
        override val actionsTitle = "操作"
        override val checkUpdate = "アップデートを確認"
        override val exportDiagnostics = "診断パッケージをエクスポート"
        override val feedback = "フィードバック"
        override val relatedLinksTitle = "関連リンク"
        override val officialDocsLabel = "公式ドキュメント"
        override val privacyPolicyLabel = "プライバシーポリシー"
        override val termsOfServiceLabel = "利用規約"
        override val builtWithLabel = "Kotlin + Jetpack Compose で構築"
        override val notificationSettingsTitle = "通知設定"
        override val notificationSwitchTitle = "通知スイッチ"
        override val enableNotifications = "通知を有効にする"
        override val enableNotificationsDesc = "アプリの通知を受け取る"
        override val notificationSoundTitle = "通知音"
        override val notificationSoundDesc = "通知時にサウンドを再生"
        override val notificationVibrationTitle = "通知振動"
        override val notificationVibrationDesc = "通知時にデバイスを振動させる"
        override val notificationTypesTitle = "通知タイプ"
        override val taskCompleteNotificationTitle = "タスク完了"
        override val taskCompleteNotificationDesc = "バックグラウンドタスク完了時に通知"
        override val taskFailNotificationTitle = "タスク失敗"
        override val taskFailNotificationDesc = "バックグラウンドタスク失敗時に通知"
        override val updateNotificationTitle = "アップデート通知"
        override val updateNotificationDesc = "新バージョンがあるときに通知"
        override val doNotDisturbTitle = "おやすみモード"
        override val enableDoNotDisturb = "おやすみモードを有効にする"
        override val doNotDisturbDesc = "指定した時間帯は通知を受け取らない"
        override val doNotDisturbTimeFormat = "おやすみ時間: %1\$s - %2\$s"
        override val noScheduledTasks = "スケジュールされたタスクがありません"
        override val createTaskHint = "右下のボタンをタップして新しいタスクを作成"
        override val nextRunTimeLabel = "次回実行"
        override val edit = "編集"
        override val deleteTaskTitle = "タスクを削除"
        override val deleteTaskMessage = "タスク「%1\$s」を削除してもよろしいですか？"
        override val newTaskTitle = "新しいタスク"
        override val editTaskTitle = "タスクを編集"
        override val taskNameLabel = "タスク名"
        override val executionFrequencyLabel = "実行頻度"
        override val everyMinute = "毎分"
        override val everyFiveMinutes = "5 分ごと"
        override val everyFifteenMinutes = "15 分ごと"
        override val everyHour = "毎時"
        override val everyDayPreset = "毎日 (9:00)"
        override val cronExpressionLabel = "Cron 式"
        override val cronFormat = "Cron: %1\$s"
        override val actionTypeLabel = "アクションタイプ"
        override val actionParamsLabel = "アクションパラメータ"
        override val confirm = "OK"
        override val totalTokensLabel = "合計 Token"
        override val totalCostLabel = "合計費用"
        override val totalCallsLabel = "呼び出し回数"
        override val today = "今日"
        override val thisWeek = "今週"
        override val thisMonth = "今月"
        override val tokenUsageTrendTitle = "Token 使用量の推移"
        override val noData = "データがありません"
        override val modelDistributionTitle = "モデル別分布"
        override val costEstimateTitle = "費用の見積もり"
        override val currentPeriodCostLabel = "現在の期間の費用"
        override val avgCostPerCallLabel = "平均呼び出しあたりの費用"
        override val tokenUnitPriceLabel = "Token 単価の参考"
        override val assistantsTitle = "アシスタント管理"
        override val assistantsWip = "アシスタント管理機能は開発中です..."
        override val mcpServersTitle = "MCP サーバー"
        override val addMcpServerHint = "右下のボタンをタップしてサーバーを追加"
        override val toolsListWip = "ツール一覧機能は開発中です"
        override val connectionTestWip = "接続テスト機能は開発中です"
        override val tools = "ツール"
        override val connectionType = "接続タイプ"
        override val urlLabel = "URL (SSE タイプ)"
        override val mcpManageTitle = "MCP 管理"
        override val addServer = "サーバーを追加"
        override val noMcpServers = "MCP サーバーがありません"
        override val reconnect = "再接続"
        override val test = "テスト"
        override val deleteServerTitle = "サーバーを削除"
        override val deleteServerConfirm = "「%1\$s」を削除してもよろしいですか？"
        override val connected = "接続済み"
        override val connecting = "接続中"
        override val disconnected = "未接続"
        override val unknown = "不明"
        override val quickAdd = "クイック追加"
        override val jsonImport = "JSON インポート"
        override val dxtImport = "DXT パッケージインポート"
        override val mcpbImport = "mcpb パッケージインポート"
        override val dxtParseFailed = "DXT ファイルの解析に失敗しました"
        override val fileReadFailed = "ファイルの読み込みに失敗しました: %1\$s"
        override val mcpbParseFailed = "mcpb ファイルの解析に失敗しました"
        override val addMcpServer = "MCP サーバーを追加"
        override val jsonParseFailed = "JSON の解析に失敗しました"
        override val serverName = "サーバー名"
        override val connectionMethod = "接続方法"
        override val command = "コマンド (uvx/npx)"
        override val arguments = "引数"
        override val serverUrl = "サーバー URL"
        override val headersLabel = "Headers (JSON 形式)"
        override val pasteMcpJsonHint = "MCP サーバーの JSON 設定を貼り付け"
        override val jsonConfig = "JSON 設定"
        override val parseAndAdd = "解析して追加"
        override val selectDxtFile = "DXT ファイルを選択 (ZIP 形式)"
        override val selectFile = "ファイルを選択"
        override val parseResult = "解析結果"
        override val name = "名前"
        override val type = "タイプ"
        override val selectMcpbFile = "mcpb ファイルを選択 (バイナリパッケージ)"
        override val testToolTitle = "ツールテスト - %1\$s"
        override val noAvailableTools = "利用可能なツールがありません"
        override val selectTool = "ツールを選択:"
        override val argsJsonLabel = "パラメータ (JSON)"
        override val execute = "実行"
        override val result = "結果:"
        override val close = "閉じる"
        override val skillManageTitle = "Skill 管理"
        override val addSkill = "Skill を追加"
        override val noSkills = "Skill がありません"
        override val addSkillHint = "+ をタップしてカスタム Skill を追加"
        override val httpRequest = "HTTP リクエスト"
        override val textTransform = "テキスト変換"
        override val description = "説明"
        override val triggerLabel = "トリガーワード"
        override val actionType = "アクションタイプ"
        override val urlPlaceholderLabel = "URL ({input} をプレースホルダとして使用)"
        override val prefixLabel = "プレフィックス"
        override val suffixLabel = "サフィックス"
        override val defaultModel = "デフォルトモデル"
        override val defaultModelDesc = "新しい会話で使用するモデル"
        override val quickModel = "クイックモデル"
        override val quickModelDesc = "クイック返信で使用するモデル"
        override val translateModel = "翻訳モデル"
        override val translateModelDesc = "翻訳機能で使用するモデル"
        override val topicNamingModel = "トピック命名モデル"
        override val topicNamingModelDesc = "会話タイトルを自動生成するときに使用するモデル"
        override val notSelected = "未選択"
        override val modelManage = "モデル管理"
        override val quickAddPresetModels = "プリセットモデルをクイック追加"
        override val discoverOllamaModels = "Ollama ローカルモデルを検出"
        override val testing = "テスト中"
        override val default = "デフォルト"
        override val editModel = "モデルを編集"
        override val provider = "プロバイダー"
        override val modelName = "モデル名"
        override val temperature = "温度"
        override val maxTokens = "最大 Token"
        override val timeoutSeconds = "タイムアウト (秒)"
        override val save = "保存"
        override val selectOllamaModel = "Ollama モデルを選択"
        override val discoveringLocalModels = "ローカルモデルを検出中..."
        override val noModelsFound = "利用可能なモデルが見つかりませんでした"
        override val addPresetModel = "プリセットモデルを追加"
        override val addCustomModel = "カスタムモデルを追加"
        override val noProvidersConfigured = "プロバイダーが設定されていません"
        override val noProvidersHint = "右下のボタンをタップしてプロバイダーを追加し、カードをタップして詳細を表示"
        override val modelCountFormat = "%1\$d 個のモデル · %2\$s"
        override val notConfiguredAddModel = "未設定 · タップしてモデルを追加"
        override val enterDetails = "詳細を見る"
        override val enableProvider = "このプロバイダーを有効にする"
        override val enableProviderDesc = "デフォルトでは無効です。有効にするとこのプロバイダーのモデルが使用可能になります"
        override val presetModels = "プリセットモデル"
        override val fetching = "取得中"
        override val fetchModels = "モデルを取得"
        override val noModelsFetched = "モデル一覧を取得できませんでした"
        override val fetchModelsFailed = "モデルの取得に失敗しました"
        override val modelsTitleFormat = "モデル (%1\$d)"
        override val noModelsHint = "モデルがありません。上のボタンからプリセットモデルを追加するか、API から取得してください"
        override val modelsAddedFormat = "%1\$d 個のモデルを追加しました"
        override val noProviderConfigHint = "プロバイダーが設定されていません。[カスタムモデルを追加]からプロバイダーを追加してください"
        override val providerType = "プロバイダータイプ"
        override val noPresetModels = "このプロバイダーにはプリセットモデルがありません"
        override val selectModelsToAdd = "追加するモデルを選択:"
        override val alreadyAdded = "追加済み"
        override val fetchedModelsTitle = "%1\$d 個のモデルを取得しました"
        override val etcModelsFormat = "など %1\$d 個のモデル"
        override val addAll = "すべて追加"
        override val addProviderHint = "追加後はデフォルトで無効です。詳細ページでこのプロバイダーを有効にできます"
        override val editProviderConfig = "%1\$s API 設定を編集"
        override val editProviderHint = "変更すると、このプロバイダー配下のすべてのモデルに適用されます"

    }
}
