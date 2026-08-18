package com.spcrk.app.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OcrManager(private val context: Context) {
    private val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val chineseRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    suspend fun recognizeText(uri: Uri, recognizeChinese: Boolean = false): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap == null) return@withContext ""
            recognizeText(bitmap, recognizeChinese)
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun recognizeText(bitmap: Bitmap, recognizeChinese: Boolean = false): String = withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = if (recognizeChinese) chineseRecognizer else latinRecognizer
            val result = Tasks.await(recognizer.process(image))
            result.text
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun recognizeTextWithBlocks(uri: Uri, recognizeChinese: Boolean = false): List<OcrTextBlock> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap == null) return@withContext emptyList()
            recognizeTextWithBlocks(bitmap, recognizeChinese)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun recognizeTextWithBlocks(bitmap: Bitmap, recognizeChinese: Boolean = false): List<OcrTextBlock> = withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = if (recognizeChinese) chineseRecognizer else latinRecognizer
            val result = Tasks.await(recognizer.process(image))
            val blocks = mutableListOf<OcrTextBlock>()

            for (block in result.textBlocks) {
                val blockText = block.text
                val blockCornerPoints = block.cornerPoints
                val blockFrame = block.boundingBox
                val blockLines = block.lines.map { line ->
                    OcrTextLine(
                        text = line.text,
                        cornerPoints = line.cornerPoints,
                        boundingBox = line.boundingBox
                    )
                }
                blocks.add(
                    OcrTextBlock(
                        text = blockText,
                        cornerPoints = blockCornerPoints,
                        boundingBox = blockFrame,
                        lines = blockLines
                    )
                )
            }
            blocks
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun close() {
        latinRecognizer.close()
        chineseRecognizer.close()
    }
}

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
