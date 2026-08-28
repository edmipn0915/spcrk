package com.spcrk.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

private val testDark = darkColorScheme()
private val testLight = lightColorScheme()

/**
 * 外觀設置（主色 / 字體大小）的純邏輯測試。
 * 依 TDD 先寫失敗測試，再實作 [scaledTypography] 與 [applyAccentColor]。
 */
class ThemeLogicTest {

    @Test
    fun `scaled typography with scale 1 returns same instance`() {
        val base = Typography()
        assertSame(base, scaledTypography(base, 1f))
    }

    @Test
    fun `scaled typography multiplies font sizes`() {
        val base = Typography()
        val scaled = scaledTypography(base, 1.5f)
        assertEquals(base.bodyMedium.fontSize.value * 1.5f, scaled.bodyMedium.fontSize.value, 0.01f)
        assertEquals(base.titleLarge.fontSize.value * 1.5f, scaled.titleLarge.fontSize.value, 0.01f)
        assertEquals(base.labelSmall.fontSize.value * 1.5f, scaled.labelSmall.fontSize.value, 0.01f)
    }

    @Test
    fun `scaled typography keeps line heights untouched`() {
        val base = Typography()
        val scaled = scaledTypography(base, 2f)
        assertEquals(base.bodyMedium.lineHeight, scaled.bodyMedium.lineHeight)
    }

    @Test
    fun `accent color applied as primary and surface tint`() {
        val accent = Color(0xFF00BCD4)
        val scheme = applyAccentColor(testDark, accent, darkTheme = true)
        assertEquals(accent, scheme.primary)
        assertEquals(accent, scheme.surfaceTint)
        assertEquals(accent, scheme.secondary)
    }

    @Test
    fun `bright accent uses dark on-primary for contrast`() {
        val scheme = applyAccentColor(testDark, Color(0xFFFFEB3B), darkTheme = true)
        assertEquals(Color.Black, scheme.onPrimary)
    }

    @Test
    fun `dark accent uses light on-primary for contrast`() {
        val scheme = applyAccentColor(testDark, Color(0xFF0D47A1), darkTheme = true)
        assertEquals(Color.White, scheme.onPrimary)
    }

    @Test
    fun `light theme accent keeps surface intact`() {
        val scheme = applyAccentColor(testLight, Color(0xFF2196F3), darkTheme = false)
        assertEquals(testLight.background, scheme.background)
        assertEquals(Color(0xFF2196F3), scheme.primary)
    }
}
