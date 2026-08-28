package com.spcrk.app.ai

import com.spcrk.app.ai.api.OcrService
import com.spcrk.app.ai.api.OcrTextBlock
import com.spcrk.app.ai.api.OcrTextLine
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

class OcrManager(private val context: Context) : OcrService {
    private val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val chineseRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

    override suspend fun recognizeText(uri: Uri, recognizeChinese: Boolean): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap == null) return@withContext ""
            recognizeText(bitmap, recognizeChinese)
        } catch (e: Exception) { "" }
    }

    override suspend fun recognizeText(bitmap: Bitmap, recognizeChinese: Boolean): String = withContext(Dispatchers.IO) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = if (recognizeChinese) chineseRecognizer else latinRecognizer
            Tasks.await(recognizer.process(image)).text
        } catch (e: Exception) { "" }
    }

    override suspend fun recognizeTextWithBlocks(uri: Uri, recognizeChinese: Boolean): List<OcrTextBlock> =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap == null) return@withContext emptyList()
                recognizeTextWithBlocks(bitmap, recognizeChinese)
            } catch (e: Exception) { emptyList() }
        }

    override suspend fun recognizeTextWithBlocks(bitmap: Bitmap, recognizeChinese: Boolean): List<OcrTextBlock> =
        withContext(Dispatchers.IO) {
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                val recognizer = if (recognizeChinese) chineseRecognizer else latinRecognizer
                val result = Tasks.await(recognizer.process(image))
                result.textBlocks.map { block ->
                    OcrTextBlock(
                        text = block.text,
                        cornerPoints = block.cornerPoints,
                        boundingBox = block.boundingBox,
                        lines = block.lines.map { line ->
                            OcrTextLine(text = line.text, cornerPoints = line.cornerPoints, boundingBox = line.boundingBox)
                        }
                    )
                }
            } catch (e: Exception) { emptyList() }
        }

    override fun close() { latinRecognizer.close(); chineseRecognizer.close() }
}
