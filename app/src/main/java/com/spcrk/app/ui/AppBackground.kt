package com.spcrk.app.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 全 App 背景層。
 *
 * 依 SettingsStore 的 background key 決定顯示：
 * - bg1 / bg2 / bg3：程式占位星雲漸層（真正的三張圖之後再替換）
 * - custom：讀取上傳的本機圖片（ContentScale.Crop，含可讀性遮罩）
 *
 * 一律再疊一層依主題調整的遮罩，確保前景內容（文字/卡片）保持可讀。
 */
@Composable
fun AppBackground(
    background: String,
    customUri: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    Box(modifier = modifier.fillMaxSize()) {
        when (background) {
            "bg2" -> NebulaBackground(2)
            "bg3" -> NebulaBackground(3)
            "custom" -> if (customUri.isNotBlank()) {
                CustomImageBackground(customUri)
            } else {
                NebulaBackground(1)
            }
            else -> NebulaBackground(1) // 默認圖一
        }

        // 可讀性遮罩：暗色加深、亮色洗白，讓文字/卡片在圖片上前景清晰
        // 深色 alpha 調高，壓住占位星雲的亮光暈，避免內容區看起來發霧/文字難讀
        val scrim = if (isDark) {
            Color.Black.copy(alpha = 0.45f)
        } else {
            Color.White.copy(alpha = 0.72f)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(scrim)
        )
    }
}

/**
 * 三張占位星雲：深空直立漸層底 + 一團居中的 radial 光暈。
 * 1 = 青核心（圖一），2 = 紫綠交錯（圖二），3 = 下中亮藍行星（圖三）。
 */
@Composable
private fun NebulaBackground(variant: Int) {
    val baseStops: List<Pair<Float, Color>> = when (variant) {
        2 -> listOf(
            0.0f to Color(0xFF0A0E1A),
            0.35f to Color(0xFF1B2A4A),
            0.60f to Color(0xFF2A1B4A),
            0.85f to Color(0xFF163A3E),
            1.0f to Color(0xFF0B0E18)
        )
        3 -> listOf(
            0.0f to Color(0xFF080B12),
            0.45f to Color(0xFF10203A),
            0.62f to Color(0xFF1B3A66),
            0.85f to Color(0xFF2D6A9E),
            1.0f to Color(0xFF12233A)
        )
        else -> listOf(
            0.0f to Color(0xFF0B0E14),
            0.42f to Color(0xFF14213A),
            0.55f to Color(0xFF0E5A6B),
            0.72f to Color(0xFF1A2C4E),
            1.0f to Color(0xFF17102A)
        )
    }

    val glowColors: List<Color> = when (variant) {
        2 -> listOf(Color(0x5500E6B8), Color(0x1F7B2FBE), Color.Transparent)
        3 -> listOf(Color(0x662B8FFF), Color(0x1F3F51B5), Color.Transparent)
        else -> listOf(Color(0x6600D4FF), Color(0x2200D4FF), Color.Transparent)
    }
    val glowCenterY = when (variant) {
        3 -> 0.62f
        else -> 0.46f
    }
    val glowRadiusFactor = when (variant) {
        3 -> 0.62f
        else -> 0.72f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = baseStops.map { it.first to it.second }.toTypedArray()
                )
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = glowColors,
                        center = Offset(size.width * 0.5f, size.height * glowCenterY),
                        radius = size.maxDimension * glowRadiusFactor
                    ),
                    radius = size.maxDimension * glowRadiusFactor,
                    center = Offset(size.width * 0.5f, size.height * glowCenterY)
                )
            }
    )
}

/** 自訂上傳背景：以 Cover 縮放填滿，失敗則退回星雲一 */
@Composable
private fun CustomImageBackground(uri: String) {
    val bitmap = rememberBitmapFromUri(uri)
    if (bitmap == null) {
        NebulaBackground(1)
    } else {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * 從 content:// URI 讀取並解碼為 [ImageBitmap]。
 * 供自訂背景預覽與全頁顯示共用。
 */
@Composable
fun rememberBitmapFromUri(uri: String): ImageBitmap? {
    val context = LocalContext.current
    val state by produceState<ImageBitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val uriObj = Uri.parse(uri)
                val isFile = uriObj.scheme == "file"
                val path = uriObj.path

                // 先讀邊界計算 inSampleSize，避免大圖 OOM
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                if (isFile && path != null) {
                    BitmapFactory.decodeFile(path, bounds)
                } else {
                    context.contentResolver.openInputStream(uriObj)?.use { input ->
                        BitmapFactory.decodeStream(input, null, bounds)
                    }
                }
                val maxDim = 2560
                var sample = 1
                while (bounds.outWidth / sample > maxDim || bounds.outHeight / sample > maxDim) {
                    sample *= 2
                }
                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
                if (isFile && path != null) {
                    BitmapFactory.decodeFile(path, decodeOpts)
                } else {
                    context.contentResolver.openInputStream(uriObj)?.use { input ->
                        BitmapFactory.decodeStream(input, null, decodeOpts)
                    }
                }
            }.getOrNull()?.asImageBitmap()
        }
    }
    return state
}
