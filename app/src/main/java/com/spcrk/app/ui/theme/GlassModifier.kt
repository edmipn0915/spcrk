package com.spcrk.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Shape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Optional cross-component glass tint override.
 * When null (default), [glass] falls back to MaterialTheme.colorScheme.surfaceGlass.
 */
// LocalGlassTint is declared in Theme.kt as staticCompositionLocalOf<Color?> { null }

/**
 * Glassmorphism modifier.
 *
 * 以「半透明 tint + 邊框 + 圓角」模擬液態玻璃。
 *
 * 注意：這裡刻意「不」使用 RenderEffect blur。因為 renderEffect 是套在整個 layer 上，
 * 會把容器內自己畫的圖示/文字也一起糊掉，導致導航欄、抽屜等前景內容模糊難以閱讀。
 * 背景星雲圖本身就提供玻璃反射質感，因此改為純透色即可，前景內容保持清晰。
 *
 * @param tint optional override for the glass surface color; falls back to
 *   [LocalGlassTint] then MaterialTheme.colorScheme.surfaceGlass.
 * @param shape optional shape override, defaults to a 20dp rounded corner.
 */
fun Modifier.glass(
    tint: Color? = null,
    shape: Shape = RoundedCornerShape(CornerRadiusTokens.m)
): Modifier = composed {
    val isDark = isSystemInDarkTheme()
    val resolvedTint = tint ?: LocalGlassTint.current ?: MaterialTheme.colorScheme.surfaceGlass
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.08f)
    } else {
        Color.White.copy(alpha = 0.6f)
    }

    this
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
