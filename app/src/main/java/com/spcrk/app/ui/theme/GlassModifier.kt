package com.spcrk.app.ui.theme

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Optional cross-component glass tint override.
 * When null (default), [glass] falls back to MaterialTheme.colorScheme.surfaceGlass.
 */
// LocalGlassTint is declared in Theme.kt as staticCompositionLocalOf<Color?> { null }

/**
 * Glassmorphism modifier.
 *
 * - API 31+: real background blur via [RenderEffect.createBlurEffect] + graphicsLayer.
 * - API 26–30: degrades gracefully to the translucent surfaceGlass tint (no blur).
 *
 * The version guard is critical: referencing RenderEffect on API < 31 causes
 * NoClassDefFoundError at class-verification time on Android 8–10.
 *
 * @param tint optional override for the glass surface color; falls back to
 *   [LocalGlassTint] then MaterialTheme.colorScheme.surfaceGlass.
 */
fun Modifier.glass(
    tint: Color? = null
): Modifier = composed {
    val shape = RoundedCornerShape(CornerRadiusTokens.m) // cornerLarge = 20dp
    val isDark = isSystemInDarkTheme()
    val resolvedTint = tint ?: LocalGlassTint.current ?: MaterialTheme.colorScheme.surfaceGlass
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.White.copy(alpha = 0.6f)
    }

    val blurLayer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Modifier.graphicsLayer {
            renderEffect = RenderEffect
                .createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
                .asComposeRenderEffect()
        }
    } else {
        Modifier
    }

    this
        .then(blurLayer)
        .clip(shape)
        .background(resolvedTint, shape)
        .border(width = 0.5.dp, color = borderColor, shape = shape)
}

/**
 * Convenience glass card. Applies [glass] with 16dp inner padding.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .glass()
            .padding(SpacingTokens.screenEdge), // 16dp
        content = content
    )
}
