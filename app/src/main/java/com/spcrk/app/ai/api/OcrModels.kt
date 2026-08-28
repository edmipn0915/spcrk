package com.spcrk.app.ai.api

/**
 * Ocr 文本块信息。
 */
data class OcrTextBlock(
    val text: String,
    val cornerPoints: Array<out android.graphics.Point>?,
    val boundingBox: android.graphics.Rect?,
    val lines: List<OcrTextLine>
)

data class OcrTextLine(
    val text: String,
    val cornerPoints: Array<out android.graphics.Point>?,
    val boundingBox: android.graphics.Rect?
)
