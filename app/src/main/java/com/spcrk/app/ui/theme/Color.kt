package com.spcrk.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Sparck 灵愿 — Dark theme color primitives
// ---------------------------------------------------------------------------

val DarkBackground = Color(0xFF0B0E14)
val DarkOnBackground = Color(0xFFF0F4FF)

val DarkSurface = Color(0xFF141824)
val DarkOnSurface = Color(0xFFF0F4FF)
val DarkSurfaceGlass = Color(0xA6141824) // rgba(20, 24, 36, 0.65)
val DarkSurfaceVariant = Color(0xFF1E2436)
val DarkOnSurfaceVariant = Color(0xFF8896B0)

val DarkPrimary = Color(0xFF00D4FF)
val DarkOnPrimary = Color(0xFFFFFFFF)
val DarkPrimaryContainer = Color(0xFF004D66)
val DarkOnPrimaryContainer = Color(0xFFB3EAFF)

val DarkSecondary = Color(0xFFFFB300)
val DarkOnSecondary = Color(0xFF1A1400)
val DarkSecondaryContainer = Color(0xFF5C4200)
val DarkOnSecondaryContainer = Color(0xFFFFE08A)

val DarkTertiary = Color(0xFF7B2FBE)
val DarkOnTertiary = Color(0xFFFFFFFF)

val DarkBorder = Color(0xFF2A3450)
val DarkOutline = Color(0xFF3A4460)
val DarkOutlineVariant = Color(0xFF2A3450)

val DarkError = Color(0xFFFF4D4F)
val DarkOnError = Color(0xFFFFFFFF)
val DarkErrorContainer = Color(0xFF662023)
val DarkOnErrorContainer = Color(0xFFFFDAD6)

val DarkSuccess = Color(0xFF00E676)
val DarkWarning = Color(0xFFFFB300)

val DarkStatusBar = Color(0xFF0B0E14)

val DarkGlowStart = Color(0xFF00D4FF)
val DarkGlowEnd = Color(0xFF7B2FBE)

// ---------------------------------------------------------------------------
// Sparck 灵愿 — Light theme color primitives
// ---------------------------------------------------------------------------

val LightBackground = Color(0xFFF4F6FA)
val LightOnBackground = Color(0xFF1A202C)

val LightSurface = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF1A202C)
val LightSurfaceGlass = Color(0x99FFFFFF) // rgba(255, 255, 255, 0.6)
val LightSurfaceVariant = Color(0xFFE8ECF2)
val LightOnSurfaceVariant = Color(0xFF5A6B80)

val LightPrimary = Color(0xFF0088CC)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFCCE8F5)
val LightOnPrimaryContainer = Color(0xFF00334D)

val LightSecondary = Color(0xFFD49B00)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFF5E6C0)
val LightOnSecondaryContainer = Color(0xFF4D3900)

val LightTertiary = Color(0xFF66B8FF)
val LightOnTertiary = Color(0xFF002A4D)

val LightBorder = Color(0xFFD0D8E4)
val LightOutline = Color(0xFFB0B8C8)
val LightOutlineVariant = Color(0xFFD0D8E4)

val LightError = Color(0xFFD83A3A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFCE8E8)
val LightOnErrorContainer = Color(0xFF4D1A1A)

val LightSuccess = Color(0xFF00C853)
val LightWarning = Color(0xFFD49B00)

val LightStatusBar = Color(0xFFF4F6FA)

val LightGlowStart = Color(0xFF0088CC)
val LightGlowEnd = Color(0xFF66B8FF)

// ---------------------------------------------------------------------------
// Semantic mapping — surfaceGlass / border on Material 3 ColorScheme
// ---------------------------------------------------------------------------

val LocalSurfaceGlass = staticCompositionLocalOf { DarkSurfaceGlass }
val LocalBorderColor = staticCompositionLocalOf { DarkBorder }

val ColorScheme.surfaceGlass: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalSurfaceGlass.current

val ColorScheme.border: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalBorderColor.current

