package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FintechGradientCard(
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = gradientColors
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = shape
            )
            .then(clickModifier)
            .padding(20.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    cornerRadius: Dp = 24.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    
    // Frosted glass background opacity depends on dark/light mode
    val backgroundColor = if (isDark) {
        Color(0xFF1C1E2D).copy(alpha = 0.75f)
    } else {
        Color.White.copy(alpha = 0.85f)
    }

    val borderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.03f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.08f),
                Color.Black.copy(alpha = 0.02f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = shape
            )
            .then(clickModifier)
            .padding(18.dp)
    ) {
        Column {
            content()
        }
    }
}
