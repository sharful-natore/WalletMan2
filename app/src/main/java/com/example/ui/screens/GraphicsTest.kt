package com.example.ui.screens

import android.graphics.Picture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas

@Composable
fun TestPicture() {
    val picture = remember { Picture() }
    Modifier.drawWithCache {
        val width = this.size.width.toInt()
        val height = this.size.height.toInt()
        onDrawWithContent {
            val pictureCanvas = picture.beginRecording(width, height)
            draw(this, this.layoutDirection, androidx.compose.ui.graphics.Canvas(pictureCanvas), this.size) {
                this@onDrawWithContent.drawContent()
            }
            picture.endRecording()
            
            drawIntoCanvas { canvas -> canvas.nativeCanvas.drawPicture(picture) }
        }
    }
}
