package com.spcrk.app.ui

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.spcrk.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 全 App 背景層。
 *
 * 依 SettingsStore 的 background key 決定顯示：
 * - bg1 / bg2 / bg3：讀取 drawable-nodpi 的三張星雲圖（ContentScale.Crop 填滿）
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
            "bg2" -> NebulaImageBackground(R.drawable.bg_nebula2)
            "bg3" -> NebulaImageBackground(R.drawable.bg_nebula3)
            "custom" -> if (customUri.isNotBlank()) {
                CustomImageBackground(customUri)
            } else {
                NebulaImageBackground(R.drawable.bg_nebula1)
            }
            else -> NebulaImageBackground(R.drawable.bg_nebula1) // 默認圖一
        }

        // 可讀性遮罩：暗色加深、亮色洗白，讓文字/卡片在圖片上前景清晰
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
 * 單張星雲背景：以 Cover 縮放填滿（手機直立會裁切填滿、平板同樣覆蓋縮放）。
 */
@Composable
private fun NebulaImageBackground(@DrawableRes resId: Int) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

/** 自訂上傳背景：以 Cover 縮放填滿，失敗則退回星雲一 */
@Composable
private fun CustomImageBackground(uri: String) {
    val bitmap = rememberBitmapFromUri(uri)
    if (bitmap == null) {
        NebulaImageBackground(R.drawable.bg_nebula1)
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
 * 從 content:// 或 file:// URI 讀取並解碼為 [ImageBitmap]。
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
