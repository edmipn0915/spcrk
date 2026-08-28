package com.spcrk.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

data class GlowColors(
    val start: Color,
    val end: Color
)

val LocalGlassTint = staticCompositionLocalOf<Color?> { null }
val LocalGlowColors = staticCompositionLocalOf { GlowColors(DarkGlowStart, DarkGlowEnd) }
val LocalSuccessColor = staticCompositionLocalOf { DarkSuccess }

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    surfaceTint = DarkPrimary,
    inverseSurface = DarkOnBackground,
    inverseOnSurface = DarkBackground,
    inversePrimary = DarkPrimary,
    scrim = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer,
    surfaceTint = LightPrimary,
    inverseSurface = LightOnBackground,
    inverseOnSurface = LightBackground,
    inversePrimary = LightPrimary,
    scrim = Color.Black
)

@Composable
fun VideoDownloaderTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    selectedColor: Color? = null,
    fontSize: Int = 14,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        selectedColor != null -> applyAccentColor(if (darkTheme) DarkColorScheme else LightColorScheme, selectedColor, darkTheme)
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val glassTint = if (darkTheme) DarkSurfaceGlass else LightSurfaceGlass
    val borderColor = if (darkTheme) DarkBorder else LightBorder
    val accent = selectedColor ?: if (darkTheme) DarkPrimary else LightPrimary
    val glowColors = GlowColors(accent, if (darkTheme) DarkGlowEnd else LightGlowEnd)
    val successColor = if (darkTheme) DarkSuccess else LightSuccess
    val statusBarColor = if (darkTheme) DarkStatusBar else LightStatusBar

    val typography = scaledTypography(Typography, fontSize / 14f)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = statusBarColor.toArgb()
            window.navigationBarColor = statusBarColor.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalSurfaceGlass provides glassTint,
        LocalBorderColor provides borderColor,
        LocalGlowColors provides glowColors,
        LocalSuccessColor provides successColor
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}

/**
 * 將使用者選擇的主色套用到 ColorScheme 的 primary / secondary / tertiary 系列。
 * onPrimary 依主色亮度自動選擇黑/白，確保可讀性。
 */
internal fun applyAccentColor(base: ColorScheme, accent: Color, darkTheme: Boolean): ColorScheme {
    val onAccent = if (accent.luminance() > 0.5f) Color.Black else Color.White
    val container = accent.copy(alpha = if (darkTheme) 0.25f else 0.15f)
    return base.copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = container,
        onPrimaryContainer = accent.copy(alpha = if (darkTheme) 0.9f else 0.8f),
        secondary = accent,
        onSecondary = onAccent,
        secondaryContainer = container,
        onSecondaryContainer = accent.copy(alpha = if (darkTheme) 0.9f else 0.8f),
        tertiary = accent,
        onTertiary = onAccent,
        surfaceTint = accent
    )
}

/**
 * 按比例縮放整個 Typography 的字體大小（字體大小設置）。
 * scale == 1f 時直接回傳原物件，避免無意義的重新建構。
 */
internal fun scaledTypography(base: Typography, scale: Float): Typography {
    if (scale == 1f) return base
    fun scaled(style: TextStyle) = style.copy(fontSize = (style.fontSize.value * scale).sp)
    return base.copy(
        displayLarge = scaled(base.displayLarge),
        displayMedium = scaled(base.displayMedium),
        displaySmall = scaled(base.displaySmall),
        headlineLarge = scaled(base.headlineLarge),
        headlineMedium = scaled(base.headlineMedium),
        headlineSmall = scaled(base.headlineSmall),
        titleLarge = scaled(base.titleLarge),
        titleMedium = scaled(base.titleMedium),
        titleSmall = scaled(base.titleSmall),
        bodyLarge = scaled(base.bodyLarge),
        bodyMedium = scaled(base.bodyMedium),
        bodySmall = scaled(base.bodySmall),
        labelLarge = scaled(base.labelLarge),
        labelMedium = scaled(base.labelMedium),
        labelSmall = scaled(base.labelSmall)
    )
}
