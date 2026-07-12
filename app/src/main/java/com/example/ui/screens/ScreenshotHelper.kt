package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Picture
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class ScreenshotState {
    val picture = Picture()
    var width: Int = 0
    var height: Int = 0

    fun createBitmap(): Bitmap? {
        if (width == 0 || height == 0) return null
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawPicture(picture)
        return bitmap
    }
}

@Composable
fun rememberScreenshotState() = remember { ScreenshotState() }

fun Modifier.captureToPicture(state: ScreenshotState): Modifier = this.drawWithCache {
    state.width = this.size.width.toInt()
    state.height = this.size.height.toInt()
    onDrawWithContent {
        val pictureCanvas = state.picture.beginRecording(state.width, state.height)
        draw(this, this.layoutDirection, androidx.compose.ui.graphics.Canvas(pictureCanvas), this.size) {
            this@onDrawWithContent.drawContent()
        }
        state.picture.endRecording()
        
        drawIntoCanvas { canvas -> canvas.nativeCanvas.drawPicture(state.picture) }
    }
}

fun shareBitmap(context: Context, bitmap: Bitmap, shareText: String) {
    try {
        val cachePath = File(context.cacheDir, "shared_images")
        cachePath.mkdirs()
        val file = File(cachePath, "share_receipt.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()
        
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Share via"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
