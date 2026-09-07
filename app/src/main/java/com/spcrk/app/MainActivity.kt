package com.spcrk.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.ContextCompat
import com.spcrk.app.data.SettingsStore
import com.spcrk.app.ui.l10n.LocalAppStrings
import com.spcrk.app.ui.navigation.AppNavigation
import com.spcrk.app.ui.theme.VideoDownloaderTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SettingsStore.initInstance(applicationContext)
        checkAndRequestPermissions()

        setContent {
            val settingsStore = SettingsStore.getInstance()
            val themeMode by settingsStore.themeModeFlow.collectAsState(initial = "system")
            val selectedColor by settingsStore.selectedColorFlow.collectAsState(initial = 0xFF00BCD4)
            val fontSize by settingsStore.fontSizeFlow.collectAsState(initial = 14)
            val zoomFactor by settingsStore.zoomFactorFlow.collectAsState(initial = 1.0f)
            val language by settingsStore.languageFlow.collectAsState(initial = "zh")
            val background by settingsStore.backgroundFlow.collectAsState(initial = "bg1")
            val customBackgroundUri by settingsStore.customBackgroundUriFlow.collectAsState(initial = "")

            val isDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            // 界面縮放：調整整棵 UI 的 Density（含 dp/sp），
            // 與字體大小設置（Typography 縮放）疊加。
            val baseDensity = LocalDensity.current
            CompositionLocalProvider(
                LocalAppStrings provides com.spcrk.app.ui.l10n.appStringsFor(language),
                LocalDensity provides Density(
                    density = baseDensity.density * zoomFactor,
                    fontScale = baseDensity.fontScale
                )
            ) {
                VideoDownloaderTheme(
                    darkTheme = isDarkTheme,
                    selectedColor = Color(selectedColor),
                    fontSize = fontSize
                ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        com.spcrk.app.ui.AppBackground(
                            background = background,
                            customUri = customBackgroundUri
                        )
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            // 背景交由底层 AppBackground 渲染，根 Surface 保持透明
                            color = Color.Transparent
                        ) {
                            AppNavigation()
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
