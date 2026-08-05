package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FintechGradientCard(
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    padding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    brush: Brush? = null,
    shadowElevation: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isRawPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed || isRawPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "cardPressScale"
    )

    val clickModifier = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = { onClick?.invoke() },
            onLongClick = { onLongClick?.invoke() }
        )
    } else {
        Modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isRawPressed = true
                    try {
                        awaitRelease()
                    } finally {
                        isRawPressed = false
                    }
                }
            )
        }
    }

    val shadowModifier = if (shadowElevation > 0.dp) {
        Modifier.shadow(
            elevation = shadowElevation,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.22f),
            spotColor = Color.Black.copy(alpha = 0.35f)
        )
    } else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(shadowModifier)
            .clip(shape)
            .background(
                brush = brush ?: Brush.linearGradient(
                    colors = if (gradientColors.size >= 2) gradientColors else listOf(gradientColors.firstOrNull() ?: Color.Gray, gradientColors.lastOrNull() ?: Color.Gray)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.45f),
                        Color.White.copy(alpha = 0.12f)
                    )
                ),
                shape = shape
            )
            .then(clickModifier)
            .padding(padding)
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
    padding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
    shadowElevation: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isRawPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed || isRawPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "frostedPressScale"
    )

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isRawPressed = true
                    try {
                        awaitRelease()
                    } finally {
                        isRawPressed = false
                    }
                }
            )
        }
    }
    
    val shadowModifier = if (shadowElevation > 0.dp) {
        Modifier.shadow(
            elevation = shadowElevation,
            shape = shape,
            clip = false,
            ambientColor = if (isDark) Color.Black.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.12f),
            spotColor = if (isDark) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.18f)
        )
    } else Modifier

    // Frosted glass background opacity depends on dark/light mode
    val backgroundColor = if (isDark) {
        Color(0xFF1C1E2D).copy(alpha = 0.85f)
    } else {
        Color.White.copy(alpha = 0.92f)
    }

    val borderBrush = if (isDark) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.04f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.12f),
                Color.Black.copy(alpha = 0.03f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(shadowModifier)
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp,
                brush = borderBrush,
                shape = shape
            )
            .then(clickModifier)
            .padding(padding)
    ) {
        Column {
            content()
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
fun Modifier.touchScaleDown(
    scaleDownFactor: Float = 0.96f,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isRawPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed || isRawPressed) scaleDownFactor else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "touchScaleDown"
    )

    val clickMod = if (onClick != null || onLongClick != null) {
        Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = { onClick?.invoke() },
            onLongClick = { onLongClick?.invoke() }
        )
    } else {
        Modifier.pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isRawPressed = true
                    try {
                        awaitRelease()
                    } finally {
                        isRawPressed = false
                    }
                }
            )
        }
    }

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(clickMod)
}

fun Modifier.bouncyOverscroll(): Modifier = composed {
    var overscrollAmount by remember { mutableStateOf(0f) }

    val animatedOverscroll by animateFloatAsState(
        targetValue = overscrollAmount,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bouncyOverscroll"
    )

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: androidx.compose.ui.geometry.Offset,
                available: androidx.compose.ui.geometry.Offset,
                source: NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                if (source == NestedScrollSource.UserInput) {
                    val newOverscroll = overscrollAmount + available.y * 0.35f
                    overscrollAmount = newOverscroll.coerceIn(-100f, 100f)
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                overscrollAmount = 0f
                return Velocity.Zero
            }
        }
    }

    androidx.compose.runtime.LaunchedEffect(overscrollAmount) {
        if (overscrollAmount != 0f) {
            kotlinx.coroutines.delay(80)
            overscrollAmount = 0f
        }
    }

    this
        .nestedScroll(nestedScrollConnection)
        .graphicsLayer {
            translationY = animatedOverscroll
            val scaleFactor = 1f + (kotlin.math.abs(animatedOverscroll) / 1200f)
            scaleY = scaleFactor
        }
}


