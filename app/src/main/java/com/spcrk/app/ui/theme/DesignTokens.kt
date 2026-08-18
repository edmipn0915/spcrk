package com.spcrk.app.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ---------------------------------------------------------------------------
// Corner Radius
// ---------------------------------------------------------------------------

data class CornerRadius(
    val xs: Dp,
    val s: Dp,
    val m: Dp,
    val l: Dp,
    val xl: Dp
)

val CornerRadiusTokens = CornerRadius(
    xs = 4.dp,
    s = 12.dp,
    m = 20.dp,
    l = 28.dp,
    xl = 999.dp
)

// ---------------------------------------------------------------------------
// Spacing
// ---------------------------------------------------------------------------

data class Spacing(
    val grid: Dp,
    val screenEdge: Dp,
    val section: Dp,
    val block: Dp,
    val itemSpacing: Dp
)

val SpacingTokens = Spacing(
    grid = 4.dp,
    screenEdge = 16.dp,
    section = 24.dp,
    block = 32.dp,
    itemSpacing = 8.dp
)

// ---------------------------------------------------------------------------
// Dialog Scrim
// ---------------------------------------------------------------------------

val DialogScrim = Color(0xB3000000) // rgba(0, 0, 0, 0.7)

// ---------------------------------------------------------------------------
// Shimmer Colors
// ---------------------------------------------------------------------------

data class ShimmerColors(
    val base: Color,
    val highlight: Color
)

val DarkShimmerColors = ShimmerColors(
    base = Color(0xFF141824),
    highlight = Color(0xFF1E2436)
)

val LightShimmerColors = ShimmerColors(
    base = Color(0xFFE8ECF2),
    highlight = Color(0xFFFFFFFF)
)

// ---------------------------------------------------------------------------
// Shadow Elevation
// Spec: dark  low 0 2px 8px rgba(0,0,0,0.5)
//            medium 0 4px 16px rgba(0,0,0,0.6)
//            high 0 8px 32px rgba(0,0,0,0.7)
//            glow 0 0 20px rgba(0,212,255,0.15)
//       light low 0 2px 8px rgba(0,0,0,0.06)
//            medium 0 4px 16px rgba(0,0,0,0.08)
//            high 0 8px 32px rgba(0,0,0,0.10)
//            glow 0 0 16px rgba(0,136,204,0.12)
// ---------------------------------------------------------------------------

data class ShadowElevation(
    val offsetX: Dp,
    val offsetY: Dp,
    val blur: Dp,
    val color: Color
)

data class ShadowElevationSet(
    val low: ShadowElevation,
    val medium: ShadowElevation,
    val high: ShadowElevation,
    val glow: ShadowElevation
)

val DarkShadowElevation = ShadowElevationSet(
    low = ShadowElevation(
        offsetX = 0.dp,
        offsetY = 2.dp,
        blur = 8.dp,
        color = Color(0x80000000)
    ),
    medium = ShadowElevation(
        offsetX = 0.dp,
        offsetY = 4.dp,
        blur = 16.dp,
        color = Color(0x99000000)
    ),
    high = ShadowElevation(
        offsetX = 0.dp,
        offsetY = 8.dp,
        blur = 32.dp,
        color = Color(0xB3000000)
    ),
    glow = ShadowElevation(
        offsetX = 0.dp,
        offsetY = 0.dp,
        blur = 20.dp,
        color = Color(0x2600D4FF)
    )
)

val LightShadowElevation = ShadowElevationSet(
    low = ShadowElevation(
        offsetX = 0.dp,
        offsetY = 2.dp,
        blur = 8.dp,
        color = Color(0x0F000000)
    ),
    medium = ShadowElevation(
        offsetX = 0.dp,
        offsetY = 4.dp,
        blur = 16.dp,
        color = Color(0x14000000)
    ),
    high = ShadowElevation(
        offsetX = 0.dp,
        offsetY = 8.dp,
        blur = 32.dp,
        color = Color(0x1A000000)
    ),
    glow = ShadowElevation(
        offsetX = 0.dp,
        offsetY = 0.dp,
        blur = 16.dp,
        color = Color(0x1F0088CC)
    )
)

// ---------------------------------------------------------------------------
// Composition Locals for Design Tokens
// ---------------------------------------------------------------------------

val LocalCornerRadius = staticCompositionLocalOf { CornerRadiusTokens }
val LocalSpacing = staticCompositionLocalOf { SpacingTokens }
val LocalDialogScrim = staticCompositionLocalOf { DialogScrim }
val LocalShimmerColors = staticCompositionLocalOf { DarkShimmerColors }
val LocalShadowElevation = staticCompositionLocalOf { DarkShadowElevation }
