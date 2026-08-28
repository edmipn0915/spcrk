package com.spcrk.app.ui.l10n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 語言設置的翻譯表測試。
 * 依 TDD：先定義四種語言都必須齊全，且代表性字串翻譯正確。
 */
class AppStringsTest {

    private fun AppStrings.assertNoBlankFields() {
        javaClass.declaredFields.forEach { field ->
            field.isAccessible = true
            val value = field.get(this) as? String ?: return@forEach
            assertTrue("${field.name} 在 ${this::class.simpleName} 中為空白", value.isNotBlank())
        }
    }

    @Test
    fun `all four languages have every field filled`() {
        AppStringsCatalog.zh.assertNoBlankFields()
        AppStringsCatalog.zhTW.assertNoBlankFields()
        AppStringsCatalog.en.assertNoBlankFields()
        AppStringsCatalog.ja.assertNoBlankFields()
    }

    @Test
    fun `zh provides simplified chinese labels`() {
        assertEquals("主页", AppStringsCatalog.zh.home)
        assertEquals("设置", AppStringsCatalog.zh.settings)
        assertEquals("跟随系统", AppStringsCatalog.zh.followSystem)
    }

    @Test
    fun `zhTW provides traditional chinese labels`() {
        assertEquals("首頁", AppStringsCatalog.zhTW.home)
        assertEquals("設定", AppStringsCatalog.zhTW.settings)
        assertEquals("跟隨系統", AppStringsCatalog.zhTW.followSystem)
        assertEquals("介面縮放", AppStringsCatalog.zhTW.uiZoom)
    }

    @Test
    fun `en provides english labels`() {
        assertEquals("Home", AppStringsCatalog.en.home)
        assertEquals("Settings", AppStringsCatalog.en.settings)
        assertEquals("Follow System", AppStringsCatalog.en.followSystem)
    }

    @Test
    fun `ja provides japanese labels`() {
        assertEquals("ホーム", AppStringsCatalog.ja.home)
        assertEquals("設定", AppStringsCatalog.ja.settings)
        assertEquals("システムに従う", AppStringsCatalog.ja.followSystem)
    }

    @Test
    fun `unknown language falls back to simplified chinese`() {
        assertSame(AppStringsCatalog.zh, appStringsFor("fr"))
        assertSame(AppStringsCatalog.zh, appStringsFor(""))
    }

    @Test
    fun `zhTW maps to traditional chinese`() {
        assertSame(AppStringsCatalog.zhTW, appStringsFor("zh-TW"))
    }

    @Test
    fun `settings store codes map correctly`() {
        assertSame(AppStringsCatalog.zh, appStringsFor("zh"))
        assertSame(AppStringsCatalog.en, appStringsFor("en"))
        assertSame(AppStringsCatalog.ja, appStringsFor("ja"))
    }
}
