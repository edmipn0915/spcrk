package com.spcrk.app.ai.api

import android.graphics.Bitmap
import android.net.Uri

interface OcrService {
    suspend fun recognizeText(uri: Uri, recognizeChinese: Boolean): String
    suspend fun recognizeText(bitmap: Bitmap, recognizeChinese: Boolean): String
    suspend fun recognizeTextWithBlocks(uri: Uri, recognizeChinese: Boolean): List<OcrTextBlock>
    suspend fun recognizeTextWithBlocks(bitmap: Bitmap, recognizeChinese: Boolean): List<OcrTextBlock>
    fun close()
}
