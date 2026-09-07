package com.spcrk.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsStore private constructor() {
    private var prefs: SharedPreferences? = null

    private val _themeModeFlow = MutableStateFlow("system")
    val themeModeFlow: StateFlow<String> = _themeModeFlow.asStateFlow()

    private val _fontSizeFlow = MutableStateFlow(14)
    val fontSizeFlow: StateFlow<Int> = _fontSizeFlow.asStateFlow()

    private val _languageFlow = MutableStateFlow(systemDefaultLanguage())
    val languageFlow: StateFlow<String> = _languageFlow.asStateFlow()

    private val _zoomFactorFlow = MutableStateFlow(1.0f)
    val zoomFactorFlow: StateFlow<Float> = _zoomFactorFlow.asStateFlow()

    private val _showColorPicker = MutableStateFlow(false)
    val showColorPicker: StateFlow<Boolean> = _showColorPicker.asStateFlow()

    private val _selectedColorFlow = MutableStateFlow(0xFF00BCD4)
    val selectedColorFlow: StateFlow<Long> = _selectedColorFlow.asStateFlow()

    private val _defaultModelIdFlow = MutableStateFlow("")
    val defaultModelIdFlow: StateFlow<String> = _defaultModelIdFlow.asStateFlow()

    private val _quickModelIdFlow = MutableStateFlow("")
    val quickModelIdFlow: StateFlow<String> = _quickModelIdFlow.asStateFlow()

    private val _translateModelIdFlow = MutableStateFlow("")
    val translateModelIdFlow: StateFlow<String> = _translateModelIdFlow.asStateFlow()

    private val _topicNamingModelIdFlow = MutableStateFlow("")
    val topicNamingModelIdFlow: StateFlow<String> = _topicNamingModelIdFlow.asStateFlow()

    private val _notificationEnabledFlow = MutableStateFlow(true)
    val notificationEnabledFlow: StateFlow<Boolean> = _notificationEnabledFlow.asStateFlow()

    private val _notificationSoundFlow = MutableStateFlow(true)
    val notificationSoundFlow: StateFlow<Boolean> = _notificationSoundFlow.asStateFlow()

    private val _notificationVibrationFlow = MutableStateFlow(true)
    val notificationVibrationFlow: StateFlow<Boolean> = _notificationVibrationFlow.asStateFlow()

    private val _autoBackupFlow = MutableStateFlow(false)
    val autoBackupFlow: StateFlow<Boolean> = _autoBackupFlow.asStateFlow()

    private val _backupPathFlow = MutableStateFlow("")
    val backupPathFlow: StateFlow<String> = _backupPathFlow.asStateFlow()

    private val _webDavUrlFlow = MutableStateFlow("")
    val webDavUrlFlow: StateFlow<String> = _webDavUrlFlow.asStateFlow()

    private val _webDavUserFlow = MutableStateFlow("")
    val webDavUserFlow: StateFlow<String> = _webDavUserFlow.asStateFlow()

    private val _webDavPasswordFlow = MutableStateFlow("")
    val webDavPasswordFlow: StateFlow<String> = _webDavPasswordFlow.asStateFlow()

    private val _pythonPathFlow = MutableStateFlow("")
    val pythonPathFlow: StateFlow<String> = _pythonPathFlow.asStateFlow()

    private val _nodePathFlow = MutableStateFlow("")
    val nodePathFlow: StateFlow<String> = _nodePathFlow.asStateFlow()

    private val _ollamaUrlFlow = MutableStateFlow("http://localhost:11434")
    val ollamaUrlFlow: StateFlow<String> = _ollamaUrlFlow.asStateFlow()

    private val _ocrEngineFlow = MutableStateFlow("mlkit")
    val ocrEngineFlow: StateFlow<String> = _ocrEngineFlow.asStateFlow()

    private val _pdfRendererFlow = MutableStateFlow("pdfbox")
    val pdfRendererFlow: StateFlow<String> = _pdfRendererFlow.asStateFlow()

    private val _searchEngineFlow = MutableStateFlow("duckduckgo")
    val searchEngineFlow: StateFlow<String> = _searchEngineFlow.asStateFlow()

    private val _searchApiKeyFlow = MutableStateFlow("")
    val searchApiKeyFlow: StateFlow<String> = _searchApiKeyFlow.asStateFlow()

    private val _searchBaseUrlFlow = MutableStateFlow("")
    val searchBaseUrlFlow: StateFlow<String> = _searchBaseUrlFlow.asStateFlow()

    private val _searchMaxResultsFlow = MutableStateFlow(5)
    val searchMaxResultsFlow: StateFlow<Int> = _searchMaxResultsFlow.asStateFlow()

    private val _urlContentProviderFlow = MutableStateFlow("builtin")
    val urlContentProviderFlow: StateFlow<String> = _urlContentProviderFlow.asStateFlow()

    private fun systemDefaultLanguage(): String {
        val tag = java.util.Locale.getDefault().toLanguageTag()
        return when {
            tag.startsWith("zh") && (tag.contains("TW") || tag.contains("Hant")) -> "zh-TW"
            tag.startsWith("zh") -> "zh"
            tag.startsWith("en") -> "en"
            tag.startsWith("ja") -> "ja"
            else -> "zh"
        }
    }

    fun init(context: Context) {
        prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        prefs?.let { p ->
            _themeModeFlow.value = p.getString("theme_mode", "system") ?: "system"
            _fontSizeFlow.value = p.getInt("font_size", 14)
            val savedLanguage = p.getString("language", null)
            _languageFlow.value = if (savedLanguage.isNullOrBlank()) systemDefaultLanguage() else savedLanguage
            _zoomFactorFlow.value = p.getFloat("zoom_factor", 1.0f)
            _selectedColorFlow.value = p.getLong("selected_color", 0xFF00BCD4)
            _showColorPicker.value = p.getBoolean("show_color_picker", false)
            _defaultModelIdFlow.value = p.getString("default_model_id", "") ?: ""
            _quickModelIdFlow.value = p.getString("quick_model_id", "") ?: ""
            _translateModelIdFlow.value = p.getString("translate_model_id", "") ?: ""
            _topicNamingModelIdFlow.value = p.getString("topic_naming_model_id", "") ?: ""
            _notificationEnabledFlow.value = p.getBoolean("notification_enabled", true)
            _notificationSoundFlow.value = p.getBoolean("notification_sound", true)
            _notificationVibrationFlow.value = p.getBoolean("notification_vibration", true)
            _autoBackupFlow.value = p.getBoolean("auto_backup", false)
            _backupPathFlow.value = p.getString("backup_path", "") ?: ""
            _webDavUrlFlow.value = p.getString("webdav_url", "") ?: ""
            _webDavUserFlow.value = p.getString("webdav_user", "") ?: ""
            _webDavPasswordFlow.value = p.getString("webdav_password", "") ?: ""
            _pythonPathFlow.value = p.getString("python_path", "") ?: ""
            _nodePathFlow.value = p.getString("node_path", "") ?: ""
            _ollamaUrlFlow.value = p.getString("ollama_url", "http://localhost:11434") ?: "http://localhost:11434"
            _ocrEngineFlow.value = p.getString("ocr_engine", "mlkit") ?: "mlkit"
            _pdfRendererFlow.value = p.getString("pdf_renderer", "pdfbox") ?: "pdfbox"
            _searchEngineFlow.value = p.getString("search_engine", "duckduckgo") ?: "duckduckgo"
            _searchApiKeyFlow.value = p.getString("search_api_key", "") ?: ""
            _searchBaseUrlFlow.value = p.getString("search_base_url", "") ?: ""
            _searchMaxResultsFlow.value = p.getInt("search_max_results", 5)
            _urlContentProviderFlow.value = p.getString("url_content_provider", "builtin") ?: "builtin"
        }
    }

    fun setThemeMode(mode: String) {
        _themeModeFlow.value = mode
        prefs?.edit()?.putString("theme_mode", mode)?.apply()
    }

    fun setFontSize(size: Int) {
        _fontSizeFlow.value = size
        prefs?.edit()?.putInt("font_size", size)?.apply()
    }

    fun setLanguage(lang: String) {
        _languageFlow.value = lang
        prefs?.edit()?.putString("language", lang)?.apply()
    }

    fun setZoomFactor(factor: Float) {
        _zoomFactorFlow.value = factor
        prefs?.edit()?.putFloat("zoom_factor", factor)?.apply()
    }

    fun setShowColorPicker(show: Boolean) {
        _showColorPicker.value = show
        prefs?.edit()?.putBoolean("show_color_picker", show)?.apply()
    }

    fun setSelectedColor(color: Long) {
        _selectedColorFlow.value = color
        prefs?.edit()?.putLong("selected_color", color)?.apply()
    }

    fun setDefaultModelId(id: String) {
        _defaultModelIdFlow.value = id
        prefs?.edit()?.putString("default_model_id", id)?.apply()
    }

    fun setQuickModelId(id: String) {
        _quickModelIdFlow.value = id
        prefs?.edit()?.putString("quick_model_id", id)?.apply()
    }

    fun setTranslateModelId(id: String) {
        _translateModelIdFlow.value = id
        prefs?.edit()?.putString("translate_model_id", id)?.apply()
    }

    fun setTopicNamingModelId(id: String) {
        _topicNamingModelIdFlow.value = id
        prefs?.edit()?.putString("topic_naming_model_id", id)?.apply()
    }

    fun setNotificationEnabled(enabled: Boolean) {
        _notificationEnabledFlow.value = enabled
        prefs?.edit()?.putBoolean("notification_enabled", enabled)?.apply()
    }

    fun setNotificationSound(enabled: Boolean) {
        _notificationSoundFlow.value = enabled
        prefs?.edit()?.putBoolean("notification_sound", enabled)?.apply()
    }

    fun setNotificationVibration(enabled: Boolean) {
        _notificationVibrationFlow.value = enabled
        prefs?.edit()?.putBoolean("notification_vibration", enabled)?.apply()
    }

    fun setAutoBackup(enabled: Boolean) {
        _autoBackupFlow.value = enabled
        prefs?.edit()?.putBoolean("auto_backup", enabled)?.apply()
    }

    fun setBackupPath(path: String) {
        _backupPathFlow.value = path
        prefs?.edit()?.putString("backup_path", path)?.apply()
    }

    fun setWebDavUrl(url: String) {
        _webDavUrlFlow.value = url
        prefs?.edit()?.putString("webdav_url", url)?.apply()
    }

    fun setWebDavUser(user: String) {
        _webDavUserFlow.value = user
        prefs?.edit()?.putString("webdav_user", user)?.apply()
    }

    fun setWebDavPassword(password: String) {
        _webDavPasswordFlow.value = password
        prefs?.edit()?.putString("webdav_password", password)?.apply()
    }

    fun setPythonPath(path: String) {
        _pythonPathFlow.value = path
        prefs?.edit()?.putString("python_path", path)?.apply()
    }

    fun setNodePath(path: String) {
        _nodePathFlow.value = path
        prefs?.edit()?.putString("node_path", path)?.apply()
    }

    fun setOllamaUrl(url: String) {
        _ollamaUrlFlow.value = url
        prefs?.edit()?.putString("ollama_url", url)?.apply()
    }

    fun setOcrEngine(engine: String) {
        _ocrEngineFlow.value = engine
        prefs?.edit()?.putString("ocr_engine", engine)?.apply()
    }

    fun setPdfRenderer(renderer: String) {
        _pdfRendererFlow.value = renderer
        prefs?.edit()?.putString("pdf_renderer", renderer)?.apply()
    }

    fun setSearchEngine(engine: String) {
        _searchEngineFlow.value = engine
        prefs?.edit()?.putString("search_engine", engine)?.apply()
    }

    fun setSearchApiKey(apiKey: String) {
        _searchApiKeyFlow.value = apiKey
        prefs?.edit()?.putString("search_api_key", apiKey)?.apply()
    }

    fun setSearchBaseUrl(baseUrl: String) {
        _searchBaseUrlFlow.value = baseUrl
        prefs?.edit()?.putString("search_base_url", baseUrl)?.apply()
    }

    fun setSearchMaxResults(maxResults: Int) {
        _searchMaxResultsFlow.value = maxResults
        prefs?.edit()?.putInt("search_max_results", maxResults)?.apply()
    }

    fun setUrlContentProvider(provider: String) {
        _urlContentProviderFlow.value = provider
        prefs?.edit()?.putString("url_content_provider", provider)?.apply()
    }

    companion object {
        @Volatile
        private var INSTANCE: SettingsStore? = null

        fun getInstance(): SettingsStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsStore().also { INSTANCE = it }
            }
        }

        fun initInstance(context: Context) {
            getInstance().init(context)
        }
    }
}
