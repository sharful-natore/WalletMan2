package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class CustomGradientType {
    LINEAR, RADIAL, SWEEP
}

enum class CustomGradientDirection {
    HORIZONTAL, VERTICAL, DIAGONAL
}

data class ThemeGradient(
    val colors: List<Color>,
    val type: CustomGradientType = CustomGradientType.LINEAR,
    val direction: CustomGradientDirection = CustomGradientDirection.HORIZONTAL
) {
    fun toBrush(): Brush {
        if (colors.isEmpty()) return Brush.linearGradient(listOf(Color.Gray, Color.Gray))
        val safeColors = if (colors.size >= 2) colors else listOf(colors.first(), colors.first())
        return when (type) {
            CustomGradientType.LINEAR -> {
                when (direction) {
                    CustomGradientDirection.HORIZONTAL -> Brush.horizontalGradient(safeColors)
                    CustomGradientDirection.VERTICAL -> Brush.verticalGradient(safeColors)
                    CustomGradientDirection.DIAGONAL -> Brush.linearGradient(safeColors)
                }
            }
            CustomGradientType.RADIAL -> Brush.radialGradient(safeColors)
            CustomGradientType.SWEEP -> Brush.sweepGradient(safeColors)
        }
    }
}
