package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.delay
import com.example.BuildConfig
import com.example.R
import android.content.Context
import android.content.Intent
import android.os.Build
import android.net.Uri
import com.example.ui.components.*
import com.example.ui.theme.*
import androidx.compose.ui.graphics.vector.ImageVector
import java.text.DecimalFormat
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.*
import com.example.ui.viewmodel.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContract
import com.yalantis.ucrop.UCrop
import java.io.File

val FintechBlue: Color
    @Composable
    get() = MaterialTheme.colorScheme.primary

val LocalActiveThemeGradient = androidx.compose.runtime.staticCompositionLocalOf<com.example.ui.theme.ThemeGradient> { error("Not provided") }

val activeThemeGradientConfig: com.example.ui.theme.ThemeGradient
    @Composable
    get() = LocalActiveThemeGradient.current

val activeThemeGradient: List<Color>
    @Composable
    get() = activeThemeGradientConfig.colors

val activeThemeGradientBrush: androidx.compose.ui.graphics.Brush
    @Composable
    get() = activeThemeGradientConfig.toBrush()

class UCropContract : ActivityResultContract<Pair<Uri, Uri>, Uri?>() {
    override fun createIntent(context: Context, input: Pair<Uri, Uri>): Intent {
        val (sourceUri, destinationUri) = input
        return UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(1000, 1000)
            .getIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (resultCode == Activity.RESULT_OK && intent != null) {
            UCrop.getOutput(intent)
        } else {
            null
        }
    }
}



fun formatNumber(number: Int, lang: AppLanguage): String {
    val str = number.toString()
    if (lang == AppLanguage.EN) return str
    return str
        .replace("0", "০")
        .replace("1", "১")
        .replace("2", "২")
        .replace("3", "৩")
        .replace("4", "৪")
        .replace("5", "৫")
        .replace("6", "৬")
        .replace("7", "৭")
        .replace("8", "৮")
        .replace("9", "৯")
}

fun getCustomTimeFilterLabel(timeFilter: String, lang: AppLanguage): String {
    val monthsBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    val monthsEn = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    
    if (timeFilter.startsWith("CUSTOM_MONTH:")) {
        val parts = timeFilter.substringAfter("CUSTOM_MONTH:").split("-")
        if (parts.size == 2) {
            val year = parts[0].toIntOrNull() ?: 2026
            val month = parts[1].toIntOrNull() ?: 1
            val monthStr = if (lang == AppLanguage.BN) monthsBn[month - 1] else monthsEn[month - 1]
            return "$monthStr, ${formatNumber(year, lang)}"
        }
    } else if (timeFilter.startsWith("CUSTOM_DATE:")) {
        val parts = timeFilter.substringAfter("CUSTOM_DATE:").split("-")
        if (parts.size == 3) {
            val year = parts[0].toIntOrNull() ?: 2026
            val month = parts[1].toIntOrNull() ?: 1
            val day = parts[2].toIntOrNull() ?: 1
            val monthStr = if (lang == AppLanguage.BN) monthsBn[month - 1] else monthsEn[month - 1]
            return "${formatNumber(day, lang)} $monthStr, ${formatNumber(year, lang)}"
        }
    }
    return timeFilter
}

fun getBudgetCategoryColors(categoryType: String?): List<Color> {
    val incomeColors = listOf(
        Color(0xFF8BC34A), // Distinct Lime Green
        Color(0xFF4CAF50), // Standard Green
        Color(0xFF9CCC65), // Soft Light Green
        Color(0xFFCDDC39), // Bright Lime Yellow
        Color(0xFFAED581), // Soft Pastel Lime Green
        Color(0xFF7CB342), // Rich Olive Green
        Color(0xFFC5E1A5), // Pale Light Green
        Color(0xFF558B2F), // Dark Olive Green
        Color(0xFFD4E157), // Mid Lime Yellow
        Color(0xFFE6EE9C)  // Very Pale Lime
    )
    val expenseColors = listOf(
        Color(0xFF8BC34A), // Light Green
        Color(0xFFCDDC39), // Lime
        Color(0xFFFFC107), // Amber
        Color(0xFFFF9800), // Orange
        Color(0xFFFF5722), // Deep Orange
        Color(0xFFF44336), // Red
        Color(0xFFE91E63)  // Pink
    )
    val savingsColors = listOf(
        Color(0xFF0D47A1), // Deep Navy Blue
        Color(0xFF2196F3), // Bright Royal Blue
        Color(0xFF03A9F4), // Sky Blue
        Color(0xFF1565C0), // Rich Dark Blue
        Color(0xFF00BCD4), // Teal Blue/Cyan
        Color(0xFF90CAF9), // Pastel Soft Blue
        Color(0xFF1E88E5), // Vivid Dodger Blue
        Color(0xFF80DEEA), // Soft Pastel Cyan
        Color(0xFF0288D1), // Medium Dark Cyan Blue
        Color(0xFFB3E5FC)  // Very Light Ice Blue
    )
    val defaultColors = listOf(
        Color(0xFF4285F4),
        Color(0xFF00BCD4),
        Color(0xFF34A853),
        Color(0xFFEA4335),
        Color(0xFF3F51B5),
        Color(0xFF9C27B0),
        Color(0xFFFF9800),
        Color(0xFFE91E63),
        Color(0xFFFF5722)
    )

    return when (categoryType) {
        "INCOME" -> incomeColors
        "EXPENSE" -> expenseColors
        "SAVINGS" -> savingsColors
        else -> defaultColors
    }
}

@Composable
fun CategorySegmentedDonutChart(
    targetAmount: Double,
    totalFilledAmount: Double,
    segments: List<Pair<String, Double>>,
    isDark: Boolean,
    language: AppLanguage,
    modifier: Modifier = Modifier,
    strokeWidthDp: Dp = 10.dp,
    centerTextSize: TextUnit = 14.sp,
    categoryType: String? = null,
    unfilledColorOverride: Color? = null,
    centerColorOverride: Color? = null,
    customPalette: List<Color>? = null,
    onCenterClick: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    var animationPlayed by remember { mutableStateOf(false) }
    var hoveredSegment by remember { mutableStateOf<Pair<String, Double>?>(null) }

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    LaunchedEffect(hoveredSegment) {
        if (hoveredSegment != null) {
            delay(3000) // Auto-hide tooltip after 3 seconds
            hoveredSegment = null
        }
    }

    val animatedProgressMultiplier by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "donut_chart_animation"
    )

    val progress = if (targetAmount > 0.0) {
        (totalFilledAmount / targetAmount).coerceIn(0.0, 1.0)
    } else {
        0.0
    }

    val actualProgressMultiplier = if (targetAmount > 0.0) {
        totalFilledAmount / targetAmount
    } else {
        0.0
    }

    val percentageText = if (targetAmount > 0.0) {
        "${(actualProgressMultiplier * animatedProgressMultiplier * 100).toInt()}%"
    } else {
        if (language == AppLanguage.BN) "সেট নেই" else "Not Set"
    }

    // Dynamic color logic: green for INCOME, dark orange for EXPENSE, and theme blue for SAVINGS
    val percentageColor = if (targetAmount > 0.0) {
        when (categoryType) {
            "INCOME" -> Color(0xFF10B981) // Green
            "EXPENSE" -> Color(0xFFE65100) // Dark Orange
            "SAVINGS" -> FintechBlue // Theme Blue
            else -> FintechBlue
        }
    } else {
        Color.Gray
    }

    val centerBgColor = centerColorOverride ?: (if (isDark) Color(0xFF333333).copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.3f))
    // Unfilled base color
    val unfilledColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFE2E8F0)

    val colors = if (!customPalette.isNullOrEmpty()) customPalette else getBudgetCategoryColors(categoryType)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier
            .fillMaxSize()
            .pointerInput(segments, targetAmount) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = { offset ->
                    val strokePx = strokeWidthDp.toPx()
                    val chartRadius = (minOf(size.width, size.height) - strokePx) / 2f
                    val innerRadius = chartRadius - strokePx / 2f
                    val outerRadius = chartRadius + strokePx / 2f
                    
                    val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                    
                    // Limit inward touch padding so the center hole remains easily clickable
                    val maxInwardPadding = minOf(12.dp.toPx(), innerRadius * 0.4f)
                    val outwardPadding = 16.dp.toPx()
                    
                    if (distance in (innerRadius - maxInwardPadding)..(outerRadius + outwardPadding)) {
                        var angle = (kotlin.math.atan2(dy, dx) * 180f / kotlin.math.PI).toFloat()
                        if (angle < 0) angle += 360f // 0 is 3 o'clock, clockwise
                        
                        // Map angle so that 0 is top (12 o'clock)
                        var angleFromTop = angle + 90f
                        if (angleFromTop >= 360f) angleFromTop -= 360f
                        
                        var currentAngle = 0f
                        var found: Pair<String, Double>? = null
                        val validSegments = segments.filter { it.second > 0.0 }
                        val activeSegmentsSum = validSegments.sumOf { it.second }
                        val progress = if (targetAmount > 0.0) {
                            (totalFilledAmount / targetAmount).coerceIn(0.0, 1.0)
                        } else {
                            0.0
                        }
                        for (segment in validSegments) {
                            val proportionOfActive = if (activeSegmentsSum > 0.0) segment.second / activeSegmentsSum else 0.0
                            val sweep = (proportionOfActive * progress * 360f).toFloat()
                            if (angleFromTop >= currentAngle && angleFromTop <= (currentAngle + sweep)) {
                                found = segment
                                break
                            }
                            currentAngle += sweep
                        }
                        
                        if (found != null) {
                            hoveredSegment = found
                        } else {
                            hoveredSegment = null
                            onCenterClick()
                        }
                    } else {
                        hoveredSegment = null
                        onCenterClick()
                    }
                }
            )
        }
    ) {
            val sizeMin = size.minDimension
            val strokeWidthPx = strokeWidthDp.toPx()
            // Subtract 16.dp to leave 8.dp of space on the outside for the glowing shadow to fade out nicely
            val radius = (sizeMin - strokeWidthPx - 16.dp.toPx()) / 2f

            // Draw central hollow background
            val innerRadius = radius - strokeWidthPx / 2f
            if (innerRadius > 0f) {
                drawCircle(
                    color = centerBgColor,
                    radius = innerRadius,
                    center = center
                )
            }

            val activeSegments = segments.filter { it.second > 0.0 }
            val activeSegmentsSum = activeSegments.sumOf { it.second }

            val drawSegments = mutableListOf<Pair<Float, Color>>()

            val resolvedUnfilledColor = unfilledColorOverride ?: unfilledColor

            if (targetAmount > 0.0 && activeSegmentsSum > 0.0) {
                // Determine progress of active segments capped at 1.0
                val progress = (totalFilledAmount / targetAmount).coerceIn(0.0, 1.0)
                
                // Add active segments
                activeSegments.forEachIndexed { index, segment ->
                    val segmentAmount = segment.second
                    val proportionOfActive = if (activeSegmentsSum > 0.0) segmentAmount / activeSegmentsSum else 0.0
                    val allocatedSweep = (proportionOfActive * progress * 360f).toFloat() * animatedProgressMultiplier
                    if (allocatedSweep > 0f) {
                        drawSegments.add(Pair(allocatedSweep, colors[index % colors.size]))
                    }
                }
                
                // Add remaining gray segment
                val totalActiveSweep = drawSegments.sumOf { it.first.toDouble() }.toFloat()
                val remainingSweep = (360f - totalActiveSweep).coerceAtLeast(0f)
                if (remainingSweep > 0f) {
                    drawSegments.add(Pair(remainingSweep, resolvedUnfilledColor))
                }
            } else {
                // If there's no budget set or no active spending, show 100% unfilled gray
                drawSegments.add(Pair(360f, resolvedUnfilledColor))
            }

            // Compute concentric paths for the double-layer design (outer solid color, inner lighter color layer)
            val outerStrokeWidth = strokeWidthPx * 0.72f
            val innerStrokeWidth = strokeWidthPx * 0.28f
            val outerRadius = radius + (innerStrokeWidth / 2f)
            val concentricInnerRadius = radius - (outerStrokeWidth / 2f)

            val outerArcTopLeft = androidx.compose.ui.geometry.Offset(center.x - outerRadius, center.y - outerRadius)
            val outerArcSize = androidx.compose.ui.geometry.Size(outerRadius * 2f, outerRadius * 2f)

            val innerArcTopLeft = androidx.compose.ui.geometry.Offset(center.x - concentricInnerRadius, center.y - concentricInnerRadius)
            val innerArcSize = androidx.compose.ui.geometry.Size(concentricInnerRadius * 2f, concentricInnerRadius * 2f)

            // Draw the segments
            if (drawSegments.size == 1) {
                // Single segment, draw perfect continuous circle with no gaps/caps artifacts
                val color = drawSegments[0].second
                val arcTopLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius)
                val arcSize = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)
                
                if (color == resolvedUnfilledColor) {
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx)
                    )
                } else {
                    // 1. Draw background unfilled track first
                    drawArc(
                        color = resolvedUnfilledColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = arcTopLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx)
                    )

                    // 2. Draw outer solid segment
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = outerArcTopLeft,
                        size = outerArcSize,
                        style = Stroke(width = outerStrokeWidth)
                    )

                    // 3. Draw inner concentric lighter layer
                    drawArc(
                        color = color.copy(alpha = 0.38f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = innerArcTopLeft,
                        size = innerArcSize,
                        style = Stroke(width = innerStrokeWidth)
                    )
                }
            } else if (drawSegments.size > 1) {
                var startAngle = -90f
                val arcTopLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius)
                val arcSize = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)

                // 1. Draw the continuous unfilled background channel track first
                drawArc(
                    color = resolvedUnfilledColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx)
                )

                // 2. Draw active segments on top using double-layer concentric arcs
                drawSegments.forEach { segment ->
                    val allocatedSweep = segment.first
                    val color = segment.second

                    if (color != resolvedUnfilledColor) {
                        // Outer vibrant arc segment
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = allocatedSweep,
                            useCenter = false,
                            topLeft = outerArcTopLeft,
                            size = outerArcSize,
                            style = Stroke(width = outerStrokeWidth, cap = StrokeCap.Butt)
                        )

                        // Inner concentric lighter layer
                        drawArc(
                            color = color.copy(alpha = 0.38f),
                            startAngle = startAngle,
                            sweepAngle = allocatedSweep,
                            useCenter = false,
                            topLeft = innerArcTopLeft,
                            size = innerArcSize,
                            style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Butt)
                        )
                    }

                    startAngle += allocatedSweep
                }
            }
        }


    // Center percentage text or hovered segment info
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = formatNumberString(percentageText, language),
                fontSize = if (targetAmount > 0.0) 28.sp else 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (targetAmount > 0.0) percentageColor else (if (isDark) Color.White else Color(0xFF1E293B)),
                textAlign = TextAlign.Center
            )
        }

        // External Tooltip
        if (hoveredSegment != null) {
            val segmentIndex = segments.indexOfFirst { it.first == hoveredSegment!!.first }
            val segmentColor = if (segmentIndex != -1) colors[segmentIndex % colors.size] else FintechBlue
            
            androidx.compose.ui.window.Popup(
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, -50),
                onDismissRequest = { hoveredSegment = null }
            ) {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = segmentColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = hoveredSegment!!.first,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = formatCurrency(hoveredSegment!!.second, language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetControlDonutChart(
    targetAmount: Double,
    totalFilledAmount: Double,
    categoryType: String, // "INCOME", "EXPENSE", "SAVINGS"
    language: AppLanguage,
    modifier: Modifier = Modifier,
    strokeWidthDp: Dp = 14.dp,
    centerTextSize: TextUnit = 14.sp,
    centerColorOverride: Color = Color(0xFFF8F9FA),
    customGradient: List<Color>? = null,
    onCenterClick: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val animatedProgressMultiplier by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "budget_chart_animation"
    )

    val progress = if (targetAmount > 0.0) {
        (totalFilledAmount / targetAmount).coerceIn(0.0, 1.0)
    } else {
        0.0
    }

    val actualProgressMultiplier = if (targetAmount > 0.0) {
        totalFilledAmount / targetAmount
    } else {
        0.0
    }

    val percentageText = if (targetAmount > 0.0) {
        "${(actualProgressMultiplier * animatedProgressMultiplier * 100).toInt()}%"
    } else {
        if (language == AppLanguage.BN) "সেট করুন" else "Set"
    }

    // Colors & Gradients selection
    val gradientColors = customGradient ?: when (categoryType) {
        "INCOME" -> listOf(Color(0xFFFFC107), Color(0xFFCDDC39), Color(0xFF8BC34A), Color(0xFF34A853))  // amber -> lime -> light green -> green
        "EXPENSE" -> listOf(Color(0xFF4CAF50), Color(0xFFCDDC39), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722), Color(0xFFF44336)) // Green -> Lime -> Amber -> Orange -> Deep Orange -> Red
        "SAVINGS" -> listOf(Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4), Color(0xFF4CAF50), Color(0xFF8BC34A)) // blue -> light blue -> cyan -> green -> light green
        else -> listOf(Color(0xFF4285F4), Color(0xFF34A853))
    }

    val trackColor = when (categoryType) {
        "INCOME" -> Color(0xFF34A853).copy(alpha = 0.20f)             // 20% green
        "EXPENSE" -> Color(0xFFFF5722).copy(alpha = 0.20f)            // 20% deep orange
        "SAVINGS" -> Color(0xFF4285F4).copy(alpha = 0.20f)            // 20% tech blue
        else -> Color.LightGray.copy(alpha = 0.20f)
    }

    val percentageColor = when (categoryType) {
        "INCOME" -> Color(0xFF10B981)
        "EXPENSE" -> Color(0xFFFF5722)
        "SAVINGS" -> Color(0xFF4285F4)
        else -> FintechBlue
    }

    Box(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(
                onTap = { onCenterClick() },
                onLongPress = { onLongPress() }
            )
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sizeMin = size.minDimension
            val strokeWidthPx = strokeWidthDp.toPx()
            val radius = (sizeMin - strokeWidthPx) / 2f
            val centerOffset = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)

            // Draw central hollow background
            val innerRadius = radius - strokeWidthPx / 2f
            if (innerRadius > 0f) {
                drawCircle(
                    color = centerColorOverride,
                    radius = innerRadius,
                    center = centerOffset
                )
            }

            // Draw progress track (100% circle background with trackColor)
            val arcTopLeft = androidx.compose.ui.geometry.Offset(centerOffset.x - radius, centerOffset.y - radius)
            val arcSize = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx)
            )

            // Draw active segment (progress arc)
            val sweepAngle = (progress * 360f).toFloat() * animatedProgressMultiplier
            if (sweepAngle > 0f) {
                // Gradient brush for the segment
                val brush = Brush.linearGradient(
                    colors = gradientColors,
                    start = Offset(centerOffset.x, centerOffset.y - radius),
                    end = Offset(centerOffset.x, centerOffset.y + radius)
                )

                // Main Gradient Arc
                drawArc(
                    brush = brush,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }

        // Center percentage text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = formatNumberString(percentageText, language),
                fontSize = if (targetAmount > 0.0) centerTextSize else 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (targetAmount > 0.0) percentageColor else Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SegmentedDonutChart(
    incomeProgress: Double,
    expenseProgress: Double,
    savingsProgress: Double,
    activeCount: Int,
    isDark: Boolean,
    language: AppLanguage,
    modifier: Modifier = Modifier,
    strokeWidthDp: Dp = 16.dp,
    centerTextSize: TextUnit = 24.sp
) {
    val totalProgress = if (activeCount > 0) {
        (incomeProgress + expenseProgress + savingsProgress) / activeCount
    } else {
        0.0
    }

    val percentageText = "${(totalProgress * 100).toInt()}%"

    val incomeColor = Color(0xFF22C55E) // Modern emerald green
    val expenseColor = Color(0xFFEF4444) // Modern coral red
    val savingsColor = Color(0xFF3B82F6) // Modern bright blue
    val unfilledColor = if (isDark) Color.White.copy(alpha = 0.15f) else Color(0xFFE2E8F0)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val sizeMin = size.minDimension
            val strokeWidthPx = strokeWidthDp.toPx()
            // Subtract 24.dp to leave 12.dp space for glowing shadow to avoid clipping
            val radius = (sizeMin - strokeWidthPx - 24.dp.toPx()) / 2f

            // 1. Draw background full circle
            drawCircle(
                color = unfilledColor,
                radius = radius,
                style = Stroke(width = strokeWidthPx)
            )

            // 2. Draw active segments
            if (activeCount > 0 && totalProgress > 0.0) {
                var startAngle = -90f
                val gapAngle = 4.5f

                val activeSegments = mutableListOf<Pair<Double, Color>>()
                if (incomeProgress > 0.0) activeSegments.add(Pair(incomeProgress, incomeColor))
                if (expenseProgress > 0.0) activeSegments.add(Pair(expenseProgress, expenseColor))
                if (savingsProgress > 0.0) activeSegments.add(Pair(savingsProgress, savingsColor))

                activeSegments.forEachIndexed { index, segment ->
                    val progressVal = segment.first
                    val sweepAngle = ((progressVal / activeCount) * 360f).toFloat()

                    if (sweepAngle > 0f) {
                        val isOnlySegment = activeSegments.size == 1 && (progressVal / activeCount) >= 0.99

                        if (isOnlySegment) {
                            // Soft glowing shadow extending ONLY outwards (blurred)
                            // Android 8.1 compatible natural outward glow with smooth gradient fade
                            val glowLayers = 100
                            val glowSize = 6.dp.toPx()
                            for (i in glowLayers downTo 1) {
                                val fraction = i.toFloat() / glowLayers
                                val currentGlowWidth = glowSize * fraction
                                val glowRadius = radius + (strokeWidthPx / 2f) + (currentGlowWidth / 2f)
                                // Linear fade for smoother outer transition and blurrier falloff
                                val alpha = 0.0142f * (1.0f - fraction)
                                drawArc(
                                    color = segment.second.copy(alpha = alpha.coerceAtLeast(0.001f)),
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    topLeft = androidx.compose.ui.geometry.Offset(center.x - glowRadius, center.y - glowRadius),
                                    size = androidx.compose.ui.geometry.Size(glowRadius * 2f, glowRadius * 2f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = currentGlowWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
                                )
                            }

                            drawArc(
                                color = segment.second,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                            )
                        } else {
                            val adjustedSweep = (sweepAngle - gapAngle).coerceAtLeast(1f)
                            val adjustedStart = startAngle + (gapAngle / 2f)

                            // Soft glowing shadow extending ONLY outwards (blurred)
                            // Android 8.1 compatible natural outward glow with smooth gradient fade
                            val glowLayers = 100
                            val glowSize = 6.dp.toPx()
                            for (i in glowLayers downTo 1) {
                                val fraction = i.toFloat() / glowLayers
                                val currentGlowWidth = glowSize * fraction
                                val glowRadius = radius + (strokeWidthPx / 2f) + (currentGlowWidth / 2f)
                                // Linear fade for smoother outer transition and blurrier falloff
                                val alpha = 0.0142f * (1.0f - fraction)
                                drawArc(
                                    color = segment.second.copy(alpha = alpha.coerceAtLeast(0.001f)),
                                    startAngle = adjustedStart,
                                    sweepAngle = adjustedSweep,
                                    useCenter = false,
                                    topLeft = androidx.compose.ui.geometry.Offset(center.x - glowRadius, center.y - glowRadius),
                                    size = androidx.compose.ui.geometry.Size(glowRadius * 2f, glowRadius * 2f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = currentGlowWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
                                )
                            }

                            drawArc(
                                color = segment.second,
                                startAngle = adjustedStart,
                                sweepAngle = adjustedSweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                            )
                        }
                    }
                    startAngle += sweepAngle
                }
            }
        }

        // 3. Center percentage text (no Filled label)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = percentageText,
                fontSize = centerTextSize,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else Color.Black
            )
        }
    }
}

@Composable
fun HighlightedText(
    text: String,
    query: String,
    color: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = TextStyle.Default,
    modifier: Modifier = Modifier
) {
    if (query.isEmpty() || !text.contains(query, ignoreCase = true)) {
        Text(text = text, style = style, modifier = modifier)
        return
    }

    val isDark = isSystemInDarkTheme()
    val highlightBg = if (isDark) Color(0xFFFFD54F).copy(alpha = 0.25f) else Color(0xFFFDE047).copy(alpha = 0.6f)
    val highlightFg = if (isDark) Color(0xFFFFD54F) else Color(0xFFB45309)

    val annotatedString = buildAnnotatedString {
        var startIdx = 0
        val queryLower = query.lowercase()
        val textLower = text.lowercase()

        while (true) {
            val idx = textLower.indexOf(queryLower, startIdx)
            if (idx == -1) {
                append(text.substring(startIdx))
                break
            }

            append(text.substring(startIdx, idx))
            
            withStyle(style = SpanStyle(background = highlightBg, color = highlightFg, fontWeight = FontWeight.ExtraBold)) {
                append(text.substring(idx, idx + query.length))
            }
            startIdx = idx + query.length
        }
    }

    Text(text = annotatedString, style = style, modifier = modifier)
}

fun formatNumberString(str: String, lang: AppLanguage): String {
    if (lang == AppLanguage.EN) return str
    return str
        .replace("0", "০")
        .replace("1", "১")
        .replace("2", "২")
        .replace("3", "৩")
        .replace("4", "৪")
        .replace("5", "৫")
        .replace("6", "৬")
        .replace("7", "৭")
        .replace("8", "৮")
        .replace("9", "৯")
}

fun getFormattedDateLabel(timeFilter: String, lang: AppLanguage): String {
    if (timeFilter.startsWith("CUSTOM_DATE:")) {
        val parts = timeFilter.substringAfter("CUSTOM_DATE:").split("-")
        if (parts.size == 3) {
            val year = parts[0].toIntOrNull() ?: 2026
            val month = parts[1].toIntOrNull() ?: 1
            val day = parts[2].toIntOrNull() ?: 1
            val shortYear = year % 100
            val dayStr = if (day < 10) "0$day" else "$day"
            val monthStr = if (month < 10) "0$month" else "$month"
            val yearStr = if (shortYear < 10) "0$shortYear" else "$shortYear"
            val rawDate = "$dayStr/$monthStr/$yearStr"
            return if (lang == AppLanguage.BN) "${formatNumberString(rawDate, lang)} তারিখের" else "$rawDate"
        }
    }
    return if (lang == AppLanguage.BN) "তারিখ অনুযায়ী" else "By Date"
}

@Composable
fun MonthYearPickerDialog(
    initialYear: Int,
    initialMonth: Int,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int) -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialYear) }
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    
    val monthsBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    val monthsEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val months = if (language == AppLanguage.BN) monthsBn else monthsEn

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (language == AppLanguage.BN) "মাস নির্বাচন করুন" else "Select Month & Year") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(if (language == AppLanguage.BN) "মাস" else "Month", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.height(180.dp)) {
                        items(12) { index ->
                            val m = index + 1
                            val isSelected = selectedMonth == m
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { selectedMonth = m }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(months[index], fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(if (language == AppLanguage.BN) "বছর" else "Year", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val years = (currentYear - 3..currentYear + 2).toList()
                    androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.height(180.dp)) {
                        items(years.size) { index ->
                            val y = years[index]
                            val isSelected = selectedYear == y
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { selectedYear = y }
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(formatNumber(y, language), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedYear, selectedMonth) }) {
                Text(Translation.get("confirm", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translation.get("cancel", language))
            }
        }
    )
}

@Composable
fun SpecificDatePickerDialog(
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int,
    initialHour: Int = 12,
    initialMinute: Int = 0,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int, day: Int, hour: Int, minute: Int) -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialYear) }
    var selectedMonth by remember { mutableStateOf(initialMonth) }
    var selectedDay by remember { mutableStateOf(initialDay) }
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }
    
    val dayState = androidx.compose.foundation.lazy.rememberLazyListState()
    val monthState = androidx.compose.foundation.lazy.rememberLazyListState()
    val yearState = androidx.compose.foundation.lazy.rememberLazyListState()
    val hourState = androidx.compose.foundation.lazy.rememberLazyListState()
    val minuteState = androidx.compose.foundation.lazy.rememberLazyListState()

    val currentYearCal = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val years = (currentYearCal - 3..currentYearCal + 2).toList()

    LaunchedEffect(Unit) {
        // Centering logic: Scroll so selected item is in the middle
        // Assuming 180dp height and ~30dp item height (including padding) -> 6 items visible
        // Let's use index - 2 or - 3
        dayState.scrollToItem(kotlin.math.max(0, initialDay - 1 - 2))
        monthState.scrollToItem(kotlin.math.max(0, initialMonth - 1 - 2))
        val yearIndex = years.indexOf(initialYear)
        if (yearIndex != -1) yearState.scrollToItem(kotlin.math.max(0, yearIndex - 2))
        hourState.scrollToItem(kotlin.math.max(0, initialHour - 2))
        minuteState.scrollToItem(kotlin.math.max(0, initialMinute - 2))
    }
    
    val monthsBn = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    val monthsEn = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val months = if (language == AppLanguage.BN) monthsBn else monthsEn

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (language == AppLanguage.BN) "তারিখ ও সময় নির্বাচন করুন" else "Select Date & Time") },
        text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                Column(modifier = Modifier.weight(0.9f)) {
                    Text(if (language == AppLanguage.BN) "দিন" else "Day", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.height(180.dp),
                        state = dayState
                    ) {
                        items(31) { index ->
                            val d = index + 1
                            val isSelected = selectedDay == d
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { selectedDay = d }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(formatNumber(d, language), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1.3f)) {
                    Text(if (language == AppLanguage.BN) "মাস" else "Month", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.height(180.dp),
                        state = monthState
                    ) {
                        items(12) { index ->
                            val m = index + 1
                            val isSelected = selectedMonth == m
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { selectedMonth = m }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(months[index], fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1.0f)) {
                    Text(if (language == AppLanguage.BN) "বছর" else "Year", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.height(180.dp),
                        state = yearState
                    ) {
                        items(years.size) { index ->
                            val y = years[index]
                            val isSelected = selectedYear == y
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { selectedYear = y }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(formatNumber(y, language), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(0.9f)) {
                    Text(if (language == AppLanguage.BN) "ঘণ্টা" else "Hour", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.height(180.dp),
                        state = hourState
                    ) {
                        items(24) { h ->
                            val isSelected = selectedHour == h
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { selectedHour = h }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(formatNumber(h, language), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(0.9f)) {
                    Text(if (language == AppLanguage.BN) "মিনিট" else "Min", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.height(180.dp),
                        state = minuteState
                    ) {
                        items(60) { m ->
                            val isSelected = selectedMinute == m
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable { selectedMinute = m }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(formatNumber(m, language), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute) }) {
                Text(Translation.get("confirm", language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Translation.get("cancel", language))
            }
        }
    )
}

@Composable
fun WorkspaceManagementDialog(
    language: AppLanguage,
    isDark: Boolean,
    workspaces: List<com.example.data.WorkspaceStats>,
    currentWorkspace: com.example.data.Workspace,
    onSelect: (String) -> Unit,
    onCreate: (String) -> Unit,
    onEdit: (String, String) -> Unit,
    onUpdatePhoto: (String, String?) -> Unit,
    onDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newWorkspaceName by remember { mutableStateOf("") }
    var showAddInput by remember { mutableStateOf(false) }
    var editingWorkspaceId by remember { mutableStateOf<String?>(null) }
    var editWorkspaceName by remember { mutableStateOf("") }
    var deletingWorkspaceId by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var pickingPhotoForWorkspaceId by remember { mutableStateOf<String?>(null) }
    
    val cropLauncher = rememberLauncherForActivityResult(com.example.ui.screens.UCropContract()) { uri ->
        if (uri != null && pickingPhotoForWorkspaceId != null) {
            onUpdatePhoto(pickingPhotoForWorkspaceId!!, uri.toString())
            pickingPhotoForWorkspaceId = null
        } else {
            pickingPhotoForWorkspaceId = null
        }
    }
    
    val workspacePhotoPicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val destinationUri = android.net.Uri.fromFile(java.io.File(context.cacheDir, "workspace_crop_${System.currentTimeMillis()}.jpg"))
            cropLauncher.launch(uri to destinationUri)
        } else {
            pickingPhotoForWorkspaceId = null
        }
    }

    val textColor = if (isDark) Color.White else Color(0xFF0F1724)
    val subtitleColor = if (isDark) Color.LightGray else Color.Gray
    val borderCol = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f)

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.SpaceDashboard,
                    contentDescription = null,
                    tint = FintechBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (language == AppLanguage.BN) "ওয়ার্কস্পেস ও তথ্য বিবরণী" else "Workspaces & Stats",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (language == AppLanguage.BN) "বিদ্যমান ওয়ার্কস্পেসসমূহ:" else "Existing Workspaces:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = subtitleColor
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    items(workspaces, key = { it.workspace.id }) { ws ->
                        val visibleState = remember { androidx.compose.animation.core.MutableTransitionState(false).apply { targetState = true } }
                        androidx.compose.animation.AnimatedVisibility(
                            visibleState = visibleState,
                            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(
                                initialOffsetY = { 40 }
                            ),
                            modifier = Modifier.animateItem()
                        ) {
                        val isSelected = ws.workspace.id == currentWorkspace.id
                        val itemBg = if (isSelected) FintechBlue.copy(alpha = 0.08f) else (if (isDark) Color(0xFF121212) else Color(0xFFF8FAFC))
                        val cardBorderCol = if (isSelected) FintechBlue else borderCol
                        
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = itemBg),
                            border = BorderStroke(1.dp, cardBorderCol),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(ws.workspace.id) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (!ws.profilePhoto.isNullOrBlank()) {
                                        AsyncImage(
                                            model = ws.profilePhoto,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .clickable { pickingPhotoForWorkspaceId = ws.workspace.id; workspacePhotoPicker.launch("image/*") }
                                                .border(1.5.dp, if (isSelected) FintechBlue else Color.Gray, CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        val initials = ws.profileName.take(1).uppercase()
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) FintechBlue.copy(alpha = 0.2f) else (if (isDark) Color(0xFF2E344A) else Color(0xFFE2E8F0)))
                                                .border(1.5.dp, if (isSelected) FintechBlue else borderCol, CircleShape)
                                                .clickable { pickingPhotoForWorkspaceId = ws.workspace.id; workspacePhotoPicker.launch("image/*") },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.AddAPhoto,
                                                contentDescription = "Add photo",
                                                tint = if (isSelected) FintechBlue else textColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = ws.profileName,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(FintechGreen.copy(alpha = 0.15f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = if (language == AppLanguage.BN) "সক্রিয়" else "Active",
                                                        color = FintechGreen,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                        if (ws.workspace.id != "default") {
                                            Text(
                                                text = ws.workspace.name,
                                                fontSize = 11.sp,
                                                color = subtitleColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        } else {
                                            Text(
                                                text = if (language == AppLanguage.BN) "মূল ওয়ার্কস্পেস" else "Main Workspace",
                                                fontSize = 11.sp,
                                                color = subtitleColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                editingWorkspaceId = ws.workspace.id
                                                editWorkspaceName = ws.workspace.name
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Edit,
                                                contentDescription = "Edit name",
                                                tint = FintechBlue,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        
                                        if (ws.workspace.id != "default") {
                                            IconButton(
                                                onClick = {
                                                    deletingWorkspaceId = ws.workspace.id
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Delete,
                                                    contentDescription = "Delete",
                                                    tint = FintechRed.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                HorizontalDivider(color = borderCol.copy(alpha = 0.6f))
                                
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.BN) "আয়: ৳${String.format("%.2f", ws.income)}" else "Income: ৳${String.format("%.2f", ws.income)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = FintechGreen
                                        )
                                        Text(
                                            text = if (language == AppLanguage.BN) "ব্যয়: ৳${String.format("%.2f", ws.expense)}" else "Expense: ৳${String.format("%.2f", ws.expense)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = FintechRed
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.BN) "পাওনা: ৳${String.format("%.2f", ws.netOwedToMe)}" else "Receivables: ৳${String.format("%.2f", ws.netOwedToMe)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = FintechBlue
                                        )
                                        Text(
                                            text = if (language == AppLanguage.BN) "দেনা: ৳${String.format("%.2f", ws.netIOwe)}" else "Payables: ৳${String.format("%.2f", ws.netIOwe)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFFF97316)
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.People,
                                                contentDescription = "Persons",
                                                tint = subtitleColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (language == AppLanguage.BN) "ব্যক্তি: ${ws.personCount} জন" else "Persons: ${ws.personCount}",
                                                fontSize = 11.sp,
                                                color = subtitleColor
                                            )
                                        }
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.AccountBalance,
                                                contentDescription = "Savings Cards",
                                                tint = subtitleColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (language == AppLanguage.BN) "সেভিংস কার্ড: ${ws.cardCount}টি" else "Savings Goals: ${ws.cardCount}",
                                                fontSize = 11.sp,
                                                color = subtitleColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        }
                    }
                }
                
                HorizontalDivider(color = borderCol)
                
                Text(
                    text = if (language == AppLanguage.BN) "নতুন ওয়ার্কস্পেস তৈরি করুন:" else "Create New Workspace:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = subtitleColor
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimatedVisibility(
                        visible = showAddInput,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally(),
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = newWorkspaceName,
                            onValueChange = { newWorkspaceName = it },
                            placeholder = {
                                Text(
                                    text = if (language == AppLanguage.BN) "যেমন: দোকানের হিসাব" else "e.g., Shop Accounts",
                                    fontSize = 13.sp
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxHeight(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FintechBlue,
                                unfocusedBorderColor = borderCol
                            )
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (!showAddInput) {
                                showAddInput = true
                            } else {
                                if (newWorkspaceName.isNotBlank()) {
                                    onCreate(newWorkspaceName.trim())
                                    newWorkspaceName = ""
                                    showAddInput = false
                                } else {
                                    showAddInput = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = if (showAddInput) Modifier.height(56.dp) else Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Icon(
                            imageVector = if (showAddInput) Icons.Rounded.Check else Icons.Rounded.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (!showAddInput) {
                                (if (language == AppLanguage.BN) "নতুন ওয়ার্কস্পেস যোগ করুন" else "Add New Workspace")
                            } else {
                                (if (language == AppLanguage.BN) "যোগ করুন" else "Add")
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (language == AppLanguage.BN) "বন্ধ করুন" else "Close",
                    color = FintechBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )

    if (editingWorkspaceId != null) {
        AlertDialog(
            onDismissRequest = { editingWorkspaceId = null },
            title = {
                Text(
                    text = if (language == AppLanguage.BN) "ওয়ার্কস্পেস নাম পরিবর্তন" else "Rename Workspace",
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            },
            text = {
                OutlinedTextField(
                    value = editWorkspaceName,
                    onValueChange = { editWorkspaceName = it },
                    label = {
                        Text(if (language == AppLanguage.BN) "নাম" else "Name")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editWorkspaceName.isNotBlank()) {
                            onEdit(editingWorkspaceId!!, editWorkspaceName.trim())
                            editingWorkspaceId = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                ) {
                    Text(if (language == AppLanguage.BN) "দাখিল করুন" else "Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingWorkspaceId = null }) {
                    Text(if (language == AppLanguage.BN) "বাতিল" else "Cancel", color = FintechBlue)
                }
            }
        )
    }

    if (deletingWorkspaceId != null) {
        AlertDialog(
            onDismissRequest = { deletingWorkspaceId = null },
            title = {
                Text(
                    text = if (language == AppLanguage.BN) "ওয়ার্কস্পেস মুছে ফেলুন?" else "Delete Workspace?",
                    fontWeight = FontWeight.Bold,
                    color = FintechRed
                )
            },
            text = {
                Text(
                    text = if (language == AppLanguage.BN) 
                        "আপনি কি সত্যিই এই ওয়ার্কস্পেসটি মুছে ফেলতে চান? এর সকল হিসাবনিকাশ ও ব্যক্তি ডেটা স্থায়ীভাবে মুছে যাবে এবং তা আর পুনরুদ্ধার করা যাবে না।" 
                    else 
                        "Are you sure you want to delete this workspace? All associated transactions, accounts, and person data will be permanently removed. This action cannot be undone.",
                    color = textColor
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(deletingWorkspaceId!!)
                        deletingWorkspaceId = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechRed)
                ) {
                    Text(if (language == AppLanguage.BN) "হ্যাঁ, মুছে ফেলুন" else "Yes, Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingWorkspaceId = null }) {
                    Text(if (language == AppLanguage.BN) "বাতিল" else "Cancel", color = FintechBlue)
                }
            }
        )
    }
}

@Composable
fun DeleteVerificationDialog(
    language: AppLanguage,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val verificationCode = remember { (1000..9999).random().toString() }
    var userInput by remember { mutableStateOf("") }
    val isMatched = userInput.trim() == verificationCode
    
    val title = if (language == AppLanguage.BN) "মুছে ফেলার নিশ্চিতকরণ" else "Confirm Deletion"
    val msg = if (language == AppLanguage.BN) {
        "এটি মুছে ফেলতে নিচে দেখানো কোডটি টাইপ করুন:"
    } else {
        "To delete, please type the verification code below:"
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = null, tint = FintechRed)
                Text(title, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(msg, style = MaterialTheme.typography.bodyMedium)
                
                // Big beautiful matched/unmatched verification code display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatNumber(verificationCode.toInt(), language),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.error,
                        letterSpacing = 4.sp
                    )
                }
                
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = {
                        Text(
                            if (language == AppLanguage.BN) "কোডটি এখানে লিখুন" else "Enter code here",
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("delete_verification_input"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isMatched,
                colors = ButtonDefaults.buttonColors(containerColor = FintechRed),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("delete_verification_confirm_btn")
            ) {
                Text(Translation.get("delete", language), color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(Translation.get("cancel", language))
            }
        }
    )
}

// Helper to format currency
fun formatCurrency(amount: Double, lang: AppLanguage): String {
    val formatter = DecimalFormat("#,##,###.##")
    val formatted = formatter.format(amount)
    return if (lang == AppLanguage.BN) {
        val bnDigits = formatted
            .replace("0", "০")
            .replace("1", "১")
            .replace("2", "২")
            .replace("3", "৩")
            .replace("4", "৪")
            .replace("5", "৫")
            .replace("6", "৬")
            .replace("7", "৭")
            .replace("8", "৮")
            .replace("9", "৯")
        "৳ $bnDigits"
    } else {
        "৳ $formatted"
    }
}

// Helper to format date
fun formatDate(timestamp: Long, lang: AppLanguage): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.ENGLISH)
    val enDate = format.format(date)
    if (lang == AppLanguage.EN) return enDate

    return enDate
        .replace("PM", "অপরাহ্ন")
        .replace("AM", "পূর্বাহ্ন")
        .replace("Jan", "জানু")
        .replace("Feb", "ফেব্রু")
        .replace("Mar", "মার্চ")
        .replace("Apr", "এপ্রিল")
        .replace("May", "মে")
        .replace("Jun", "জুন")
        .replace("Jul", "জুলাই")
        .replace("Aug", "আগস্ট")
        .replace("Sep", "সেপ্টে")
        .replace("Oct", "অক্টো")
        .replace("Nov", "নভে")
        .replace("Dec", "ডিসে")
}

fun formatDateToDay(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH)
    return format.format(date)
}

fun formatDateHeader(dateStr: String, lang: AppLanguage): String {
    try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH)
        val date = sdf.parse(dateStr) ?: return dateStr
        val format = java.text.SimpleDateFormat("dd MMMM, yyyy", java.util.Locale.ENGLISH)
        val enDate = format.format(date)
        if (lang == AppLanguage.EN) return enDate
        
        return enDate
            .replace("January", "জানুয়ারি")
            .replace("February", "ফেব্রুয়ারি")
            .replace("March", "মার্চ")
            .replace("April", "এপ্রিল")
            .replace("May", "মে")
            .replace("June", "জুন")
            .replace("July", "জুলাই")
            .replace("August", "আগস্ট")
            .replace("September", "সেপ্টেম্বর")
            .replace("October", "অক্টোবর")
            .replace("November", "নভেম্বর")
            .replace("December", "ডিসেম্বর")
    } catch (e: Exception) {
        return dateStr
    }
}

class NotchedBottomBarShape(
    private val notchRadiusDp: Dp = 34.dp,
    private val cornerRadiusDp: Dp = 12.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { notchRadiusDp.toPx() }
        val cr = with(density) { cornerRadiusDp.toPx() }
        
        val path = Path().apply {
            moveTo(0f, 0f)
            val centerX = size.width / 2f
            
            // Draw line to the start of the left corner
            lineTo(centerX - r - cr, 0f)
            
            // Left corner (smoothly curves inwards)
            quadraticBezierTo(
                centerX - r, 0f, 
                centerX - r, cr
            )
            
            // The notch arc (perfect semicircle curving downwards)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(centerX - r, cr - r, centerX + r, cr + r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = -180f,
                forceMoveTo = false
            )
            
            // Right corner (smoothly curves outwards to top edge)
            quadraticBezierTo(
                centerX + r, 0f, 
                centerX + r + cr, 0f
            )
            
            // Draw line to the end of the bar
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun FinanceNoteApp(
    viewModel: FinanceViewModel, 
    initialAction: String? = null,
    targetWorkspaceId: String? = null
) {
    val language by viewModel.language.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val selectedThemeIndex by viewModel.selectedThemeGradientIndex.collectAsState()
    val allGradientsList by viewModel.allGradientsConfig.collectAsState(initial = emptyList())
    val themeGradient = if (selectedThemeIndex in allGradientsList.indices) allGradientsList[selectedThemeIndex] else activeThemeGradientConfig
    val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsState()
    val isNetworkActive by viewModel.isNetworkActive.collectAsState()
    val firestoreSyncStatus by viewModel.firestoreSyncStatus.collectAsState()
    val customNotifications by viewModel.customNotifications.collectAsState()
    
    val showCloudDataFoundDialog by viewModel.showCloudDataFoundDialog.collectAsState()
    val pendingCloudData by viewModel.pendingCloudData.collectAsState()
    
    val rawProfileName by viewModel.profileName.collectAsState()
    val rawProfileEmail by viewModel.profileEmail.collectAsState()
    val rawProfilePhotoUri by viewModel.profilePhotoUri.collectAsState()

    val googleName by viewModel.googleName.collectAsState()
    val googleEmail by viewModel.googleEmail.collectAsState()
    val googlePhotoUrl by viewModel.googlePhotoUrl.collectAsState()
    val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsState()
    val isAuthenticated by viewModel.isUserSignedInFlow.collectAsStateWithLifecycle()
    
    val currentWorkspace by viewModel.currentWorkspace.collectAsState()
    val workspaceStatsList by viewModel.workspaceStatsList.collectAsState(initial = emptyList())
    
    val profileName = rawProfileName.ifBlank { (if (isGoogleSignedIn) googleName else null) ?: "" }
    val profileEmail = rawProfileEmail.ifBlank { (if (isGoogleSignedIn) googleEmail else null) ?: "" }
    val profilePhotoUri = rawProfilePhotoUri ?: (if (isGoogleSignedIn) googlePhotoUrl else null)

    val context = LocalContext.current
    
    val updateInfo by viewModel.updateManager.updateInfo.collectAsState()
    val isCheckingForUpdate by viewModel.updateManager.isChecking.collectAsState()
    var showUpdatePopup by remember(initialAction) { mutableStateOf(initialAction == "ACTION_SHOW_UPDATE") }

    LaunchedEffect(targetWorkspaceId) {
        targetWorkspaceId?.let { id ->
            if (currentWorkspace.id != id) {
                viewModel.selectWorkspace(id)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.updateManager.checkForUpdates(context) { isAvailable ->
            if (isAvailable) {
                showUpdatePopup = true
                val info = viewModel.updateManager.updateInfo.value
                if (!info.isForceUpdate) {
                    UpdateNotificationHelper.showUpdateNotification(
                        context, 
                        if (language == AppLanguage.BN) "BN" else "EN",
                        info.latestVersion
                    )
                }
            }
        }
    }

    var pendingDifferentAccountLogin by remember { mutableStateOf<com.google.android.gms.auth.api.signin.GoogleSignInAccount?>(null) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val finalClientId = if (BuildConfig.DRIVE_API.isNotEmpty() &&
            BuildConfig.DRIVE_API != "YOUR_DRIVE_API_CLIENT_ID" &&
            BuildConfig.DRIVE_API != "..." &&
            BuildConfig.DRIVE_API.contains(".apps.googleusercontent.com")
        ) {
            BuildConfig.DRIVE_API
        } else if (BuildConfig.GOOGLE_CLIENT_ID.isNotEmpty()) {
            BuildConfig.GOOGLE_CLIENT_ID
        } else {
            BuildConfig.DRIVE_API
        }
        val task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
            if (account != null) {
                val email = account.email
                val prefs = context.getSharedPreferences("financenote_google_prefs", Context.MODE_PRIVATE)
                val lastEmail = prefs.getString("last_google_email", null)
                if (lastEmail != null && email != null && lastEmail != email) {
                    pendingDifferentAccountLogin = account
                } else {
                    viewModel.handleGoogleSignInSuccess(
                        context = context,
                        account = account,
                        onSuccess = {
                            viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "গুগল ড্রাইভ কানেক্ট সফল হয়েছে!" else "Google Drive connected successfully!", isSuccess = true, type = "SUCCESS")
                            viewModel.triggerCustomNotification(
                                if (language == AppLanguage.BN) "আপনার গুগল অ্যাকাউন্ট সফলভাবে সিস্টেমের সাথে কানেক্ট করা হয়েছে।" else "Your Google account has been successfully connected with the system.",
                                isSuccess = true,
                                type = "SIGN_IN"
                            )
                        },
                        onError = { err ->
                            viewModel.triggerCustomNotification("${if (language == AppLanguage.BN) "কানেক্ট ব্যর্থ হয়েছে: " else "Connection failed: "}$err", isSuccess = false, type = "ERROR")
                        }
                    )
                }
            } else {
                viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "সাইন ইন ব্যর্থ হয়েছে।" else "Sign-in failed.", isSuccess = false, type = "ERROR")
            }
        } catch (e: Exception) {
            val errMsg = e.localizedMessage ?: "Unknown error"
            viewModel.triggerCustomNotification("${if (language == AppLanguage.BN) "সাইন ইন ব্যর্থ হয়েছে: " else "Sign-in failed: "}$errMsg", isSuccess = false, type = "ERROR")
        }
    }

    val triggerGoogleSignIn = {
        val finalClientId = if (BuildConfig.DRIVE_API.isNotEmpty() &&
            BuildConfig.DRIVE_API != "YOUR_DRIVE_API_CLIENT_ID" &&
            BuildConfig.DRIVE_API != "..." &&
            BuildConfig.DRIVE_API.contains(".apps.googleusercontent.com")
        ) {
            BuildConfig.DRIVE_API
        } else if (BuildConfig.GOOGLE_CLIENT_ID.isNotEmpty()) {
            BuildConfig.GOOGLE_CLIENT_ID
        } else {
            BuildConfig.DRIVE_API
        }
        val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestEmail()
            .requestProfile()
            .requestIdToken(finalClientId)
            .requestScopes(com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/drive.file"))
            .build()

        val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
        
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    var showSplash by remember { mutableStateOf(true) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadProfile(context)
        kotlinx.coroutines.delay(2000)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen(isDark = isDarkTheme)
        return
    }

    val persons by viewModel.persons.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val personDebts by viewModel.personDebts.collectAsState()
    val savingsTransactions by viewModel.savingsTransactions.collectAsState()

    val totalBalance by viewModel.totalBalance.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val totalOwedToMe by viewModel.totalOwedToMe.collectAsState()
    val totalIOwe by viewModel.totalIOwe.collectAsState()

    var timeFilter by remember { mutableStateOf("MONTH") } // Default to "MONTH" as requested by user
    var showMonthPickerState by remember { mutableStateOf(false) }
    var showDatePickerState by remember { mutableStateOf(false) }

    val handleTimeFilterChange: (String) -> Unit = { filterVal ->
        when (filterVal) {
            "TRIGGER_MONTH_PICKER" -> showMonthPickerState = true
            "TRIGGER_DATE_PICKER" -> showDatePickerState = true
            else -> timeFilter = filterVal
        }
    }

    // Filtered Transactions and metrics for the current time period
    val filteredTransactionsForMetrics = remember(transactions, timeFilter) {
        filterTransactionsByTime(transactions, timeFilter)
    }

    val filteredSavingsTransactions = remember(savingsTransactions, timeFilter) {
        // We can reuse the logic by temporarily mapping or just using a generic filter
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        when {
            timeFilter == "ALL" -> savingsTransactions
            timeFilter == "TODAY" -> {
                calendar.timeInMillis = now
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                savingsTransactions.filter { it.timestamp >= startOfDay }
            }
            timeFilter == "MONTH" -> {
                calendar.timeInMillis = now
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.timeInMillis
                savingsTransactions.filter { it.timestamp >= startOfMonth }
            }
            timeFilter.startsWith("CUSTOM_MONTH:") -> {
                val parts = timeFilter.substringAfter("CUSTOM_MONTH:").split("-")
                if (parts.size == 2) {
                    val year = parts[0].toIntOrNull() ?: 2026
                    val month = parts[1].toIntOrNull() ?: 1
                    calendar.clear()
                    calendar.set(java.util.Calendar.YEAR, year)
                    calendar.set(java.util.Calendar.MONTH, month - 1)
                    calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    calendar.set(java.util.Calendar.MINUTE, 0)
                    calendar.set(java.util.Calendar.SECOND, 0)
                    calendar.set(java.util.Calendar.MILLISECOND, 0)
                    val start = calendar.timeInMillis
                    calendar.add(java.util.Calendar.MONTH, 1)
                    val end = calendar.timeInMillis
                    savingsTransactions.filter { it.timestamp in start until end }
                } else savingsTransactions
            }
            timeFilter.startsWith("CUSTOM_DATE:") -> {
                val parts = timeFilter.substringAfter("CUSTOM_DATE:").split("-")
                if (parts.size == 3) {
                    val year = parts[0].toIntOrNull() ?: 2026
                    val month = parts[1].toIntOrNull() ?: 1
                    val day = parts[2].toIntOrNull() ?: 1
                    calendar.clear()
                    calendar.set(year, month - 1, day, 0, 0, 0)
                    calendar.set(java.util.Calendar.MILLISECOND, 0)
                    val start = calendar.timeInMillis
                    calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                    val end = calendar.timeInMillis
                    savingsTransactions.filter { it.timestamp in start until end }
                } else savingsTransactions
            }
            else -> savingsTransactions
        }
    }

    val currentTotalIncome = remember(filteredTransactionsForMetrics) {
        filteredTransactionsForMetrics.filter { it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT") }.fold(0.0) { acc, tx -> acc + tx.amount }
    }
    val currentTotalExpense = remember(filteredTransactionsForMetrics) {
        filteredTransactionsForMetrics.filter { it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT") }.fold(0.0) { acc, tx -> acc + tx.amount }
    }
    
    val currentTotalBalance = remember(filteredTransactionsForMetrics, filteredSavingsTransactions) {
        val cashIncome = filteredTransactionsForMetrics.filter { it.type == "INCOME" }.fold(0.0) { acc, tx -> acc + tx.amount }
        val cashBorrowed = filteredTransactionsForMetrics.filter { it.type == "BORROW" && it.subType != "CREDIT" }.fold(0.0) { acc, tx -> acc + tx.amount }
        val repaidReceived = filteredTransactionsForMetrics.filter { it.type == "REPAY_RECEIVED" }.fold(0.0) { acc, tx -> acc + tx.amount }
        val savingsWithdrawals = filteredSavingsTransactions.filter { !it.isDeposit }.fold(0.0) { acc, tx -> acc + tx.amount }

        val cashExpense = filteredTransactionsForMetrics.filter { it.type == "EXPENSE" }.fold(0.0) { acc, tx -> acc + tx.amount }
        val cashLent = filteredTransactionsForMetrics.filter { it.type == "LEND" && it.subType != "CREDIT" }.fold(0.0) { acc, tx -> acc + tx.amount }
        val repaidPaid = filteredTransactionsForMetrics.filter { it.type == "REPAY_PAID" }.fold(0.0) { acc, tx -> acc + tx.amount }
        val savingsDeposits = filteredSavingsTransactions.filter { it.isDeposit }.fold(0.0) { acc, tx -> acc + tx.amount }

        (cashIncome + cashBorrowed + repaidReceived + savingsWithdrawals) - 
        (cashExpense + cashLent + repaidPaid + savingsDeposits)
    }

    // Filter personDebts by time locally for UI if needed
    val filteredPersonDebts = remember(personDebts, transactions, timeFilter, persons) {
        if (timeFilter == "ALL") {
            personDebts
        } else {
            val filteredTxs = filterTransactionsByTime(transactions, timeFilter)
            persons.map { person ->
                val personTx = filteredTxs.filter { it.personId == person.id }
                val lent = personTx.filter { it.type == "LEND" }.sumOf { it.amount }
                val borrowed = personTx.filter { it.type == "BORROW" }.sumOf { it.amount }
                val repaidPaid = personTx.filter { it.type == "REPAY_PAID" }.sumOf { it.amount }
                val repaidReceived = personTx.filter { it.type == "REPAY_RECEIVED" }.sumOf { it.amount }
                
                val net = (lent + repaidPaid) - (borrowed + repaidReceived)
                PersonDebt(
                    person = person,
                    netBalance = net,
                    totalLent = lent,
                    totalBorrowed = borrowed,
                    totalRepaidPaid = repaidPaid,
                    totalRepaidReceived = repaidReceived
                )
            }
        }
    }

    val currentTotalOwedToMe = remember(filteredPersonDebts) {
        filteredPersonDebts.filter { it.netBalance > 0 }.sumOf { it.netBalance }
    }
    val currentTotalIOwe = remember(filteredPersonDebts) {
        filteredPersonDebts.filter { it.netBalance < 0 }.sumOf { -it.netBalance }
    }

    var activeTab by remember { mutableStateOf("dashboard") }
    var settingsFilter by remember { mutableStateOf("") }
    var transactionFilter by remember { mutableStateOf("ALL") }
    var debtFilter by remember { mutableStateOf("ALL") }
    var editingBudgetGradientType by remember { mutableStateOf<String?>(null) }
    val budgetGradients by viewModel.budgetGradients.collectAsState(initial = emptyMap())

    // Dialog & overlay states
    val composeCoroutineScope = rememberCoroutineScope()
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var editingPerson by remember { mutableStateOf<Person?>(null) }
    var showAddPersonDialog by remember { mutableStateOf(false) }
    var showAddSavingsGoalDialog by remember { mutableStateOf(false) }
    var pendingDeleteTransactions by remember { mutableStateOf<List<Int>?>(null) }
    var pendingDeletePersons by remember { mutableStateOf<List<Int>?>(null) }
    var pendingDeleteGoals by remember { mutableStateOf<List<Int>?>(null) }
    var showSavingsContributionDialog by remember { mutableStateOf<SavingsGoal?>(null) }
    var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }
    var savingsTxToEdit by remember { mutableStateOf<SavingsTransaction?>(null) }
    var isWithdrawMode by remember { mutableStateOf(false) }
    var selectedPersonDetail by remember { mutableStateOf<PersonDebt?>(null) }
    var showDeletePersonConfirmId by remember { mutableStateOf<Int?>(null) }
    var showDeleteGoalConfirmId by remember { mutableStateOf<Int?>(null) }
    var selectedSavingsGoalDetail by remember { mutableStateOf<SavingsGoal?>(null) }
    var goalToEdit by remember { mutableStateOf<SavingsGoal?>(null) }
    var personToMove by remember { mutableStateOf<Person?>(null) }
    var personsToMoveIds by remember { mutableStateOf<List<Int>?>(null) }
    var goalsToMoveIds by remember { mutableStateOf<List<Int>?>(null) }
    var goalToMove by remember { mutableStateOf<SavingsGoal?>(null) }
    var personActionChoice by remember { mutableStateOf<Person?>(null) }
    var goalActionChoice by remember { mutableStateOf<SavingsGoal?>(null) }

    var showExitConfirm by remember { mutableStateOf(false) }
    var highlightedTxId by remember { mutableStateOf<Int?>(null) }
    var highlightedPersonId by remember { mutableStateOf<Int?>(null) }
    var highlightedGoalId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(highlightedTxId) {
        if (highlightedTxId != null) {
            kotlinx.coroutines.delay(3000)
            highlightedTxId = null
        }
    }
    LaunchedEffect(highlightedPersonId) {
        if (highlightedPersonId != null) {
            kotlinx.coroutines.delay(3000)
            highlightedPersonId = null
        }
    }
    LaunchedEffect(highlightedGoalId) {
        if (highlightedGoalId != null) {
            kotlinx.coroutines.delay(3000)
            highlightedGoalId = null
        }
    }

    var showSearch by remember { mutableStateOf(false) }
    var showThemeCustomizeDialog by remember { mutableStateOf(false) }
    var showCustomGradientDialog by remember { mutableStateOf(false) }
    var editingCustomGradientIndex by remember { mutableStateOf<Int?>(null) }
    var customGradientColors by remember { mutableStateOf(listOf(Color(0xFF6F7BF7), Color(0xFF38BDF8))) }
    var customGradientType by remember { mutableStateOf(com.example.ui.theme.CustomGradientType.LINEAR) }
    var customGradientDirection by remember { mutableStateOf(com.example.ui.theme.CustomGradientDirection.HORIZONTAL) }
    var selectedColorIndexInGradient by remember { mutableStateOf(0) }
    var showLongPressOptions by remember { mutableStateOf(false) }
    var longPressedIndex by remember { mutableStateOf(-1) }
    var showRealtimeSyncDialog by remember { mutableStateOf(false) }
    var showProfileSetup by remember { mutableStateOf(false) }
    var showEnhancedProfileMenu by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showNoInternetDialog by remember { mutableStateOf(false) }
    var onNoInternetRetryAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val executeWithInternetCheck: (() -> Unit) -> Unit = { action ->
        if (!viewModel.isNetworkAvailable(context)) {
            onNoInternetRetryAction = action
            showNoInternetDialog = true
        } else {
            action()
        }
    }

    val driveStatusMessage by viewModel.driveStatusMessage.collectAsState()
    val googleDriveFiles by viewModel.googleDriveFiles.collectAsState()
    val isFetchingFiles by viewModel.isFetchingFiles.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var showSyncSuccessIcon by remember { mutableStateOf(false) }
    var previousFirestoreSyncStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(firestoreSyncStatus) {
        val wasSyncing = previousFirestoreSyncStatus == "Syncing..." || previousFirestoreSyncStatus == "Downloading..."
        if (wasSyncing && firestoreSyncStatus == "Synced") {
            showSyncSuccessIcon = true
            kotlinx.coroutines.delay(3000)
            showSyncSuccessIcon = false
        }
        previousFirestoreSyncStatus = firestoreSyncStatus
    }


    var showBackupConfirm by remember { mutableStateOf(false) }
    var cloudBackupStats by remember { mutableStateOf<com.example.ui.viewmodel.BackupStats?>(null) }
    var showRestoreListDialog by remember { mutableStateOf(false) }

    // Logic for triggering sign-in or backup automatically from intents
    LaunchedEffect(initialAction, isGoogleSignedIn) {
        if (initialAction != null) {
            if (initialAction == "ACTION_REFRESH_WIDGET") {
                com.example.widget.updateAllWidgets(context)
            }
            if (initialAction == "ACTION_GOOGLE_SIGN_IN" && !isGoogleSignedIn) {
                triggerGoogleSignIn()
            }
            if (initialAction == "ACTION_BACKUP") {
                if (!isGoogleSignedIn) {
                    triggerGoogleSignIn()
                } else {
                    val backupData = viewModel.getCurrentDatabaseBackup()
                    val stats = viewModel.calculateBackupStats(backupData)
                    cloudBackupStats = stats
                    showBackupConfirm = true
                }
            }
            
            // Route tab selection and dialog visibility based on intent actions
            when (initialAction) {
                "ACTION_DEBT_CREDIT" -> {
                    activeTab = "debts"
                }
                "ACTION_VIEW_DEBT" -> {
                    activeTab = "debts"
                    debtFilter = "DENA"
                }
                "ACTION_VIEW_CREDIT" -> {
                    activeTab = "debts"
                    debtFilter = "PAWN"
                }
                "ACTION_SAVINGS" -> {
                    activeTab = "savings"
                    showAddSavingsGoalDialog = false
                }
                "ACTION_VIEW_INCOME" -> {
                    activeTab = "transactions"
                    transactionFilter = "INCOME"
                }
                "ACTION_VIEW_EXPENSE" -> {
                    activeTab = "transactions"
                    transactionFilter = "EXPENSE"
                }
                "ACTION_SETTINGS" -> {
                    activeTab = "settings"
                }
                "ACTION_SETTINGS_PROFILE" -> {
                    activeTab = "settings"
                    settingsFilter = "expand_profile"
                }
                "ACTION_GOOGLE_SIGN_IN" -> {
                    activeTab = "settings"
                }
                "ACTION_CHARTS" -> {
                    activeTab = "charts"
                }
                "ACTION_ADD_TRANSACTION" -> {
                    showAddTransactionDialog = true
                }
                "ACTION_SYNC_TRIGGER" -> {
                    showRealtimeSyncDialog = true
                }
            }
        }
    }
    if (personToMove != null) {
        MoveToWorkspaceDialog(
            itemName = personToMove!!.name,
            language = language,
            isDark = isDarkTheme,
            workspaces = workspaceStatsList,
            currentWorkspaceId = currentWorkspace.id,
            onConfirm = { targetWorkspaceId ->
                viewModel.movePerson(personToMove!!.id, targetWorkspaceId)
                personToMove = null
            },
            onDismiss = { personToMove = null }
        )
    }

    if (personsToMoveIds != null) {
        MoveToWorkspaceDialog(
            itemName = if (language == AppLanguage.BN) "${personsToMoveIds!!.size} জন ব্যক্তি" else "${personsToMoveIds!!.size} persons",
            language = language,
            isDark = isDarkTheme,
            workspaces = workspaceStatsList,
            currentWorkspaceId = currentWorkspace.id,
            onConfirm = { targetWorkspaceId ->
                viewModel.movePersons(personsToMoveIds!!, targetWorkspaceId)
                personsToMoveIds = null
            },
            onDismiss = { personsToMoveIds = null }
        )
    }

    if (goalsToMoveIds != null) {
        MoveToWorkspaceDialog(
            itemName = if (language == AppLanguage.BN) "${goalsToMoveIds!!.size}টি সঞ্চয় কার্ড" else "${goalsToMoveIds!!.size} savings cards",
            language = language,
            isDark = isDarkTheme,
            workspaces = workspaceStatsList,
            currentWorkspaceId = currentWorkspace.id,
            onConfirm = { targetWorkspaceId ->
                viewModel.moveSavingsGoals(goalsToMoveIds!!, targetWorkspaceId)
                goalsToMoveIds = null
            },
            onDismiss = { goalsToMoveIds = null }
        )
    }

    if (goalToMove != null) {
        MoveToWorkspaceDialog(
            itemName = goalToMove!!.title,
            language = language,
            isDark = isDarkTheme,
            workspaces = workspaceStatsList,
            currentWorkspaceId = currentWorkspace.id,
            onConfirm = { targetWorkspaceId ->
                viewModel.moveSavingsGoal(goalToMove!!.id, targetWorkspaceId)
                goalToMove = null
            },
            onDismiss = { goalToMove = null }
        )
    }

    if (personActionChoice != null) {
        ItemActionChoiceDialog(
            itemName = personActionChoice!!.name,
            language = language,
            isDark = isDarkTheme,
            onMove = { personToMove = personActionChoice },
            onDelete = { showDeletePersonConfirmId = personActionChoice!!.id },
            onDismiss = { personActionChoice = null }
        )
    }

    if (goalActionChoice != null) {
        ItemActionChoiceDialog(
            itemName = goalActionChoice!!.title,
            language = language,
            isDark = isDarkTheme,
            onMove = { goalToMove = goalActionChoice },
            onDelete = { showDeleteGoalConfirmId = goalActionChoice!!.id },
            onDismiss = { goalActionChoice = null }
        )
    }

    if (showDeleteGoalConfirmId != null) {
        DeleteVerificationDialog(
            language = language,
            onConfirm = {
                viewModel.deleteSavingsGoal(showDeleteGoalConfirmId!!)
                showDeleteGoalConfirmId = null
            },
            onDismiss = { showDeleteGoalConfirmId = null }
        )
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text(if (language == AppLanguage.BN) "অ্যাপ থেকে বের হবেন?" else "Exit App?") },
            text = { Text(if (language == AppLanguage.BN) "আপনি কি নিশ্চিত যে অ্যাপ থেকে বের হতে চান?" else "Are you sure you want to exit the app?") },
            confirmButton = {
                Button(onClick = { (context as? android.app.Activity)?.finish() }) {
                    Text(if (language == AppLanguage.BN) "হ্যাঁ" else "Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) {
                    Text(if (language == AppLanguage.BN) "না" else "No")
                }
            }
        )
    }

    if (showSearch) {
        SearchDialog(
            onDismissRequest = { showSearch = false },
            language = language,
            isDark = isDarkTheme,
            transactions = transactions,
            persons = persons,
            savingsGoals = savingsGoals,
            savingsTransactions = savingsTransactions,
            onNavigateToTransaction = { 
                activeTab = "transactions"
                highlightedTxId = it
                showSearch = false
            },
            onNavigateToPerson = { 
                activeTab = "debts"
                highlightedPersonId = it
                showSearch = false
            },
            onNavigateToGoal = { 
                activeTab = "savings"
                highlightedGoalId = it
                showSearch = false
            },
            onNavigateToSavingsTransaction = {
                activeTab = "savings"
                showSearch = false
            },
            onNavigateToSettings = { settingId ->
                activeTab = "settings"
                settingsFilter = settingId
                showSearch = false
            }
        )
    }

    // Back handling for overlays and settings
    androidx.activity.compose.BackHandler(
        enabled = true
    ) {
        when {
            selectedPersonDetail != null -> selectedPersonDetail = null
            selectedSavingsGoalDetail != null -> selectedSavingsGoalDetail = null
            activeTab != "dashboard" -> activeTab = "dashboard"
            else -> showExitConfirm = true
        }
    }

    var showWorkspaceDialog by remember { mutableStateOf(false) }

    if (showWorkspaceDialog) {
        WorkspaceManagementDialog(
            language = language,
            isDark = isDarkTheme,
            workspaces = workspaceStatsList,
            currentWorkspace = currentWorkspace,
            onSelect = { workspaceId ->
                viewModel.selectWorkspace(workspaceId)
                showWorkspaceDialog = false
            },
            onCreate = { name ->
                viewModel.createWorkspace(name)
            },
            onEdit = { id, name ->
                viewModel.editWorkspace(id, name)
            },
            onUpdatePhoto = { id, photo ->
                viewModel.updateWorkspaceProfilePhoto(id, photo)
            },
            onDelete = { workspaceId ->
                viewModel.deleteWorkspace(workspaceId)
            },
            onDismiss = { showWorkspaceDialog = false }
        )
    }

    if (showProfileSetup) {
        ProfileSetupScreen(
            viewModel = viewModel,
            language = language,
            isDark = isDarkTheme,
            onDismiss = { showProfileSetup = false }
        )
    }

    if (showEnhancedProfileMenu) {
        EnhancedProfileMenu(
            viewModel = viewModel,
            language = language,
            isDark = isDarkTheme,
            onDismiss = { showEnhancedProfileMenu = false },
            onSwitchWorkspace = { 
                showEnhancedProfileMenu = false
                showWorkspaceDialog = true 
            },
            onProfileSettings = {
                showEnhancedProfileMenu = false
                showProfileSetup = true
            },
            onBackupRestore = {
                showEnhancedProfileMenu = false
                settingsFilter = "backup"
                activeTab = "settings"
            },
            onLogin = {
                showEnhancedProfileMenu = false
                triggerGoogleSignIn()
            },
            onLogout = {
                showEnhancedProfileMenu = false
                composeCoroutineScope.launch {
                    kotlinx.coroutines.delay(300)
                    showLogoutConfirm = true
                }
            }
        )
    }

    val isProfileSetupComplete by viewModel.isProfileSetupComplete.collectAsStateWithLifecycle()
    
    LaunchedEffect(isAuthenticated, isProfileSetupComplete) {
        if (isAuthenticated && !isProfileSetupComplete) {
            showProfileSetup = true
        }
        if (isAuthenticated) {
            viewModel.fetchUserProfile()
        }
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme
    ) {
        if (!isAuthenticated && !showSplash) {
            LoginScreen(
                viewModel = viewModel,
                language = language,
                isDark = isDarkTheme,
                onDismiss = { /* mandatory login, no dismiss unless logged in */ },
                onGoogleSignIn = { triggerGoogleSignIn() }
            )
        } else {
            Scaffold(
                containerColor = Color.Transparent,
            topBar = {
                val topBarGradient = if (isDarkTheme) {
                    Brush.linearGradient(listOf(Color(0xFF121212), Color(0xFF121212)))
                } else {
                    activeThemeGradientBrush
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(topBarGradient)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (activeTab == "settings" || activeTab == "charts") {
                                IconButton(
                                    onClick = { activeTab = "dashboard" },
                                    modifier = Modifier.padding(end = 4.dp).size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = Translation.get("back", language),
                                        tint = Color.White
                                    )
                                }
                            }
                            
                            val screenTitle = when {
                                selectedSavingsGoalDetail != null -> if (language == AppLanguage.BN) "সঞ্চয় কার্ড" else "Savings Card"
                                selectedPersonDetail != null -> Translation.get("details", language)
                                activeTab == "dashboard" -> Translation.get("dashboard", language)
                                activeTab == "transactions" -> Translation.get("transactions", language)
                                activeTab == "debts" -> Translation.get("debts", language)
                                activeTab == "savings" -> if (language == AppLanguage.BN) "সঞ্চয় কার্ড" else "Savings Card"
                                activeTab == "settings" -> Translation.get("settings", language)
                                activeTab == "charts" -> if (language == AppLanguage.BN) "রিপোর্ট চার্ট" else "Report Chart"
                                else -> Translation.get("dashboard", language)
                            }
                            
                            AnimatedContent(
                                targetState = screenTitle,
                                modifier = Modifier.weight(1f, fill = false),
                                transitionSpec = {
                                    (slideInVertically { height -> height } + fadeIn())
                                        .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                                },
                                label = "TitleSlideTransition"
                            ) { title ->
                                Text(
                                    text = title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (activeTab != "charts") {
                                IconButton(
                                    onClick = { showSearch = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = "Search",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Box(contentAlignment = Alignment.TopEnd) {
                                IconButton(
                                    onClick = {
                                        showRealtimeSyncDialog = true
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (firestoreSyncStatus == "Syncing..." || firestoreSyncStatus == "Downloading...") {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            androidx.compose.animation.AnimatedVisibility(
                                                visible = showSyncSuccessIcon,
                                                enter = scaleIn() + fadeIn(),
                                                exit = scaleOut() + fadeOut()
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CheckCircle,
                                                    contentDescription = "Sync Success",
                                                    tint = Color.White, // Success White
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }

                                            if (!showSyncSuccessIcon) {
                                                Icon(
                                                    imageVector = if (!isGoogleSignedIn) {
                                                        Icons.Rounded.CloudOff
                                                    } else if (hasUnsavedChanges) {
                                                        Icons.Rounded.Sync
                                                    } else {
                                                        Icons.Rounded.CloudDone
                                                    },
                                                    contentDescription = "Cloud Sync",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                SyncStatusBadge(
                                    isNetworkAvailable = isNetworkActive,
                                    hasUnsavedChanges = hasUnsavedChanges,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 2.dp, end = 2.dp)
                                )
                            }

                            IconButton(
                                onClick = { showThemeCustomizeDialog = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Palette,
                                    contentDescription = "Theme Customization",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            if (activeTab != "settings") {
                                IconButton(
                                    onClick = {
                                        activeTab = "settings"
                                        selectedPersonDetail = null
                                        selectedSavingsGoalDetail = null
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Settings,
                                        contentDescription = "Settings",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            var verticalDragAmount by remember { mutableStateOf(0f) }
                            AnimatedContent(
                                targetState = currentWorkspace.id,
                                modifier = Modifier.requiredSize(32.dp),
                                transitionSpec = {
                                    if (targetState != initialState) {
                                        if (verticalDragAmount > 0) {
                                            (slideInVertically { -it } + fadeIn()).togetherWith(slideOutVertically { it } + fadeOut())
                                        } else {
                                            (slideInVertically { it } + fadeIn()).togetherWith(slideOutVertically { -it } + fadeOut())
                                        }
                                    } else {
                                        fadeIn().togetherWith(fadeOut())
                                    }
                                },
                                label = "workspace_switch_transition"
                            ) { _ ->
                                Box(
                                    modifier = Modifier
                                        .requiredSize(32.dp)
                                        .graphicsLayer { translationY = verticalDragAmount.coerceIn(-40f, 40f) }
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .clickable { showEnhancedProfileMenu = true }
                                        .border(2.dp, Color.White, CircleShape)
                                        .pointerInput(workspaceStatsList) {
                                            detectVerticalDragGestures(
                                                onVerticalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    verticalDragAmount += dragAmount
                                                },
                                                onDragEnd = {
                                                    if (verticalDragAmount > 40) {
                                                        val currentIndex = workspaceStatsList.indexOfFirst { it.workspace.id == currentWorkspace.id }
                                                        if (currentIndex != -1 && workspaceStatsList.size > 1) {
                                                            val nextIndex = (currentIndex + 1) % workspaceStatsList.size
                                                            viewModel.selectWorkspace(workspaceStatsList[nextIndex].workspace.id)
                                                        }
                                                    } else if (verticalDragAmount < -40) {
                                                        val currentIndex = workspaceStatsList.indexOfFirst { it.workspace.id == currentWorkspace.id }
                                                        if (currentIndex != -1 && workspaceStatsList.size > 1) {
                                                            val prevIndex = (currentIndex - 1 + workspaceStatsList.size) % workspaceStatsList.size
                                                            viewModel.selectWorkspace(workspaceStatsList[prevIndex].workspace.id)
                                                        }
                                                    }
                                                    kotlinx.coroutines.MainScope().launch {
                                                        kotlinx.coroutines.delay(300)
                                                        verticalDragAmount = 0f
                                                    }
                                                }
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (profilePhotoUri != null) {
                                        AsyncImage(
                                            model = profilePhotoUri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else if (!isGoogleSignedIn) {
                                        Icon(
                                            imageVector = Icons.Rounded.Cloud,
                                            contentDescription = "Sign in to backup",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        val initials = if (profileName.isNotBlank()) {
                                            profileName.split(" ").take(2).mapNotNull { it.firstOrNull()?.toString() }.joinToString("").uppercase()
                                        } else ""

                                        if (initials.isNotEmpty()) {
                                            Text(
                                                text = initials,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Rounded.Person,
                                                contentDescription = null,
                                                tint = FintechBlue,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                val bottomBarGradient = if (isDarkTheme) {
                    Brush.linearGradient(listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E)))
                } else {
                    activeThemeGradientBrush
                }
                
                val glassBorderColor = if (isDarkTheme) {
                    Color.White.copy(alpha = 0.15f)
                } else {
                    Color.Black.copy(alpha = 0.08f)
                }
                val density = LocalDensity.current
                val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Helper for bottom bar items
                    @Composable
                    fun BottomNavItem(
                        tab: String,
                        icon: Any,
                        testTag: String,
                        iconSize: androidx.compose.ui.unit.Dp = 24.dp
                    ) {
                        val isSelected = activeTab == tab
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { 
                                    activeTab = tab
                                    selectedPersonDetail = null
                                    selectedSavingsGoalDetail = null
                                }
                                .padding(horizontal = 2.dp, vertical = 2.dp)
                                .testTag(testTag),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color.Transparent)
                                    .padding(horizontal = 18.dp, vertical = 8.dp)
                             ) {
                                if (icon is ImageVector) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.70f),
                                        modifier = Modifier.size(iconSize)
                                    )
                                } else if (icon is androidx.compose.ui.graphics.painter.Painter) {
                                    Icon(
                                        painter = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.70f),
                                        modifier = Modifier.size(iconSize)
                                    )
                                }
                            }
                        }
                    }

                    // 1. Clipped and styled navigation bar background with Notch Cut shape
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 0.dp) // Removed 16dp space to avoid "fixed card" look
                            .height(58.dp + navBarPadding)
                            .clip(NotchedBottomBarShape(notchRadiusDp = 38.dp, cornerRadiusDp = 12.dp))
                            .background(bottomBarGradient)
                            .border(
                                width = 1.dp,
                                color = glassBorderColor,
                                shape = NotchedBottomBarShape(notchRadiusDp = 38.dp, cornerRadiusDp = 12.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .align(Alignment.TopCenter),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left-side items
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BottomNavItem(
                                    tab = "dashboard",
                                    icon = Icons.Rounded.SpaceDashboard,
                                    testTag = "nav_dashboard"
                                )
                                BottomNavItem(
                                    tab = "transactions",
                                    icon = painterResource(id = R.drawable.order_approve_24),
                                    testTag = "nav_transactions",
                                    iconSize = 24.dp
                                )
                            }

                            // Spacer in the middle for the notch cutout
                            Spacer(modifier = Modifier.width(72.dp))

                            // Right-side items
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BottomNavItem(
                                    tab = "debts",
                                    icon = painterResource(id = R.drawable.ic_lend_borrow_new),
                                    testTag = "nav_debts",
                                    iconSize = 24.dp
                                )
                                BottomNavItem(
                                    tab = "savings",
                                    icon = Icons.Rounded.AccountBalance,
                                    testTag = "nav_savings",
                                    iconSize = 24.dp
                                )
                            }
                        }
                    }


                    // 2. Floating Add Button sits beautifully centered in the notch cutout
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(bottom = 12.dp + navBarPadding) // Lowered to nestle perfectly in the notch
                            .size(68.dp) // Fix size so the glowing background doesn't shift the button up
                    ) {
                        val fabPrimary = activeThemeGradient.first()
                        val fabSecondary = activeThemeGradient.last()
                        if (isDarkTheme) {
                            Box(
                                modifier = Modifier
                                    .requiredSize(92.dp) // larger than the 68.dp button for a soft glowing edge
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                fabPrimary.copy(alpha = 0.45f),
                                                fabSecondary.copy(alpha = 0.15f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .shadow(elevation = 8.dp, shape = CircleShape, spotColor = fabPrimary, ambientColor = fabSecondary)
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(fabPrimary, fabSecondary)
                                    )
                                )
                                .clickable { showAddTransactionDialog = true }
                                .testTag("fab_add_transaction"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add Transaction",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDarkTheme) Color(0xFF0B0D14) else Color(0xFFF8FAFC))
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = 0.dp,
                        start = innerPadding.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)
                    )
            ) {
                // Background Glows for Premium fintech look (Only on Dark mode for sleek visuals)
                if (isDarkTheme) {
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF3F51B5).copy(alpha = 0.15f), Color.Transparent)
                                )
                            )
                            .align(Alignment.TopStart)
                    )
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0xFF009688).copy(alpha = 0.15f), Color.Transparent)
                                )
                            )
                            .align(Alignment.BottomEnd)
                    )
                }

                // Main screen switches
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(0.dp))

                    // Active Tab Render
                    Box(modifier = Modifier.weight(1f)) {
                        val mainOrSettingsOrCharts = when (activeTab) {
                            "settings" -> "settings"
                            "charts" -> "charts"
                            else -> "main"
                        }
                        AnimatedContent(
                            targetState = mainOrSettingsOrCharts,
                            transitionSpec = {
                                (slideInHorizontally(initialOffsetX = { it }) + fadeIn())
                                    .togetherWith(slideOutHorizontally(targetOffsetX = { -it }) + fadeOut())
                            },
                            label = "MainContentTransition"
                        ) { tabState ->
                            if (tabState == "settings") {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    language = language,
                                    isDark = isDarkTheme,
                                    filter = settingsFilter,
                                    onShowUpdatePopup = { showUpdatePopup = true },
                                    onBack = { activeTab = "dashboard" },
                                    onSignInClick = { triggerGoogleSignIn() },
                                    onBackupClick = {
                                        composeCoroutineScope.launch {
                                            val backupData = viewModel.getCurrentDatabaseBackup()
                                            val stats = viewModel.calculateBackupStats(backupData)
                                            cloudBackupStats = stats
                                            showBackupConfirm = true
                                        }
                                    },
                                    onRestoreClick = {
                                        executeWithInternetCheck {
                                            showRestoreListDialog = true
                                            viewModel.listGoogleDriveFiles(context)
                                        }
                                    },
                                    showLogoutConfirm = showLogoutConfirm,
                                    onLogoutClick = { showLogoutConfirm = true },
                                    onLogoutConfirmDismiss = { showLogoutConfirm = false }
                                )
                            } else if (tabState == "charts") {
                                ChartsScreen(
                                    viewModel = viewModel,
                                    language = language,
                                    isDark = isDarkTheme,
                                    transactions = transactions,
                                    persons = persons,
                                    onBack = { activeTab = "dashboard" },
                                    onEditGradient = { editingBudgetGradientType = it }
                                )
                            } else {
                                val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
                                val coroutineScope = rememberCoroutineScope()

                                // Sync activeTab click to Pager
                                LaunchedEffect(activeTab) {
                                    val page = when (activeTab) {
                                        "dashboard" -> 0
                                        "transactions" -> 1
                                        "debts" -> 2
                                        "savings" -> 3
                                        else -> null
                                    }
                                    if (page != null && pagerState.currentPage != page) {
                                        pagerState.animateScrollToPage(
                                            page = page,
                                            animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing)
                                        )
                                    }
                                }

                                // Sync Pager settled page to activeTab
                                LaunchedEffect(pagerState.settledPage) {
                                    if (!pagerState.isScrollInProgress) {
                                        val tab = when (pagerState.settledPage) {
                                            0 -> "dashboard"
                                            1 -> "transactions"
                                            2 -> "debts"
                                            3 -> "savings"
                                            else -> "dashboard"
                                        }
                                        if (activeTab != tab && (activeTab == "dashboard" || activeTab == "transactions" || activeTab == "debts" || activeTab == "savings")) {
                                            activeTab = tab
                                        }
                                    }
                                }

                                HorizontalPager(
                                    state = pagerState,
                                    beyondViewportPageCount = 1,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    when (page) {
                                        0 -> DashboardScreen(
                                            language = language,
                                            isDark = isDarkTheme,
                                            profileName = profileName,
                                            profileEmail = profileEmail,
                                            profilePhotoUri = profilePhotoUri,
                                            balance = currentTotalBalance,
                                            income = currentTotalIncome,
                                            expense = currentTotalExpense,
                                            owedToMe = currentTotalOwedToMe,
                                            iOwe = currentTotalIOwe,
                                            recentTransactions = transactions.take(10),
                                            persons = persons,
                                            onAddTransactionClick = { showAddTransactionDialog = true },
                                            onAddPersonClick = { showAddPersonDialog = true },
                                            onAddSavingClick = { showAddSavingsGoalDialog = true },
                                            onDeleteTransaction = { viewModel.deleteTransaction(it) },
                                            onDeleteTransactions = { pendingDeleteTransactions = it },
                                            onEditTransaction = { transactionToEdit = it },
                                            onNavigate = { tab, filter ->
                                                activeTab = tab
                                                if (tab == "transactions") transactionFilter = filter
                                                if (tab == "debts") debtFilter = filter
                                                if (tab == "settings") settingsFilter = filter
                                            },
                                            onWorkspaceClick = { showWorkspaceDialog = true },
                                            timeFilter = timeFilter,
                                            onTimeFilterChange = handleTimeFilterChange,
                                            isGoogleSignedIn = isGoogleSignedIn,
                                            onSignInClick = { triggerGoogleSignIn() },
                                            onBackupClick = {
                                                composeCoroutineScope.launch {
                                                    val backupData = viewModel.getCurrentDatabaseBackup()
                                                    val stats = viewModel.calculateBackupStats(backupData)
                                                    cloudBackupStats = stats
                                                    showBackupConfirm = true
                                                }
                                            },
                                            viewModel = viewModel,
                                            onPersonClick = { person ->
                                                val foundDebt = filteredPersonDebts.find { it.person.id == person.id }
                                                if (foundDebt != null) {
                                                    selectedPersonDetail = foundDebt
                                                    activeTab = "debts"
                                                }
                                            },
                                            activeTab = activeTab,
                                            onEditGradient = { editingBudgetGradientType = it }
                                        )
                                        1 -> TransactionsScreen(
                                            language = language,
                                            isDark = isDarkTheme,
                                            transactions = transactions,
                                            persons = persons,
                                            onAddTransactionClick = { showAddTransactionDialog = true },
                                            onDeleteTransaction = { viewModel.deleteTransaction(it) },
                                            onDeleteTransactions = { pendingDeleteTransactions = it },
                                            onEditTransaction = { transactionToEdit = it },
                                            filter = transactionFilter,
                                            onFilterChange = { transactionFilter = it },
                                            timeFilter = timeFilter,
                                            onTimeFilterChange = handleTimeFilterChange,
                                            highlightedTxId = highlightedTxId,
                                            onNavigateToTab = { tab, filter ->
                                                activeTab = tab
                                                if (tab == "transactions") transactionFilter = filter
                                                if (tab == "debts") debtFilter = filter
                                                if (tab == "settings") settingsFilter = filter
                                            },
                                            onPersonClick = { person ->
                                                val foundDebt = filteredPersonDebts.find { it.person.id == person.id }
                                                if (foundDebt != null) {
                                                    selectedPersonDetail = foundDebt
                                                    activeTab = "debts"
                                                }
                                            },
                                            activeTab = activeTab
                                        )
                                        2 -> DebtsScreen(
                                            language = language,
                                            isDark = isDarkTheme,
                                            personDebts = filteredPersonDebts,
                                            onAddPersonClick = { showAddPersonDialog = true },
                                            onPersonClick = { selectedPersonDetail = it },
                                            onDeletePerson = { showDeletePersonConfirmId = it },
                                            onMovePerson = { personActionChoice = it },
                                            onDeletePersons = { pendingDeletePersons = it },
                                            onMovePersons = { personsToMoveIds = it },
                                            filter = debtFilter,
                                            onFilterChange = { debtFilter = it },
                                            timeFilter = timeFilter,
                                            onTimeFilterChange = handleTimeFilterChange,
                                            highlightedPersonId = highlightedPersonId,
                                            activeTab = activeTab
                                        )
                                        3 -> SavingsScreen(
                                            language = language,
                                            isDark = isDarkTheme,
                                            profileName = profileName,
                                            profilePhotoUri = profilePhotoUri,
                                            allGradientsList = allGradientsList,
                                            savingsGoals = savingsGoals,
                                            onAddSavingsGoalClick = { showAddSavingsGoalDialog = true },
                                            onGoalClick = { selectedSavingsGoalDetail = it },
                                            onContributeClick = { goal, isWithdraw -> 
                                                showSavingsContributionDialog = goal
                                                isWithdrawMode = isWithdraw
                                            },
                                            onEditGoal = { goalToEdit = it },
                                            onMoveGoal = { goalActionChoice = it },
                                            onDeleteGoals = { pendingDeleteGoals = it },
                                            onMoveGoals = { goalsToMoveIds = it },
                                            highlightedGoalId = highlightedGoalId,
                                            activeTab = activeTab
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Dynamic Overlays & Dialogs
                if (editingBudgetGradientType != null) {
                    val type = editingBudgetGradientType!!
                    val defaultColors = when (type) {
                        "INCOME" -> listOf(Color(0xFFFFC107), Color(0xFFCDDC39), Color(0xFF8BC34A), Color(0xFF34A853))
                        "EXPENSE" -> listOf(Color(0xFF4CAF50), Color(0xFFCDDC39), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722), Color(0xFFF44336))
                        "SAVINGS" -> listOf(Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4), Color(0xFF4CAF50), Color(0xFF8BC34A))
                        "BUDGET_DETAILS_INCOME" -> listOf(Color(0xFF4285F4), Color(0xFF00BCD4), Color(0xFF34A853), Color(0xFFEA4335), Color(0xFF3F51B5))
                        "BUDGET_DETAILS_EXPENSE" -> listOf(Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFFFF5722), Color(0xFFFF9800), Color(0xFF3F51B5))
                        "BUDGET_DETAILS_SAVINGS" -> listOf(Color(0xFF2196F3), Color(0xFF00BCD4), Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFFFC107))
                        "CHART_INCOME_CAT" -> listOf(Color(0xFF22C55E), Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF3B82F6), Color(0xFF8B5CF6))
                        "CHART_EXPENSE_CAT" -> listOf(Color(0xFFEF4444), Color(0xFFF97316), Color(0xFFFBBF24), Color(0xFFEC4899), Color(0xFFA855F7))
                        "SPLINE_INC_EXP" -> listOf(Color(0xFF3B82F6), Color(0xFFEF4444))
                        "SPLINE_LEND_BORR" -> listOf(Color(0xFF10B981), Color(0xFFF59E0B))
                        else -> listOf(Color(0xFF4285F4), Color(0xFF34A853))
                    }
                    val currentColors = budgetGradients[type] ?: defaultColors
                    var editedColors by remember { mutableStateOf(currentColors) }
                    var selectedUpperIndex by remember { mutableStateOf(0) }

                    var draggedColor by remember { mutableStateOf<Color?>(null) }
                    var dragWindowPos by remember { mutableStateOf(Offset.Zero) }
                    var hoveredUpperIndex by remember { mutableStateOf<Int?>(null) }
                    val upperItemBounds = remember { mutableStateMapOf<Int, androidx.compose.ui.geometry.Rect>() }
                    var dialogTopLeftInWindow by remember { mutableStateOf(Offset.Zero) }

                    val isGradientChart = type in listOf("INCOME", "EXPENSE", "SAVINGS")

                    val presets = listOf(
                        Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFEF4444),
                        Color(0xFFFBBF24), Color(0xFF8B5CF6), Color(0xFFEC4899),
                        Color(0xFF06B6D4), Color(0xFF14B8A6), Color(0xFFF97316),
                        Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFFD946EF),
                        Color(0xFFF43F5E), Color(0xFF84CC16), Color(0xFF22C55E),
                        Color(0xFF0EA5E9), Color(0xFF64748B), Color(0xFF1E293B),
                        Color(0xFF0F172A), Color(0xFF94A3B8)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { coords ->
                                dialogTopLeftInWindow = coords.boundsInWindow().topLeft
                            }
                    ) {
                        AlertDialog(
                            onDismissRequest = { editingBudgetGradientType = null },
                            containerColor = if (isDarkTheme) Color(0xFF1E2235) else Color.White,
                            title = {
                                Text(
                                    text = if (language == AppLanguage.BN) {
                                        if (isGradientChart) "গ্রাডিয়েন্ট কালার পরিবর্তন" else "চার্ট কালার প্যালেট পরিবর্তন"
                                    } else {
                                        if (isGradientChart) "Edit Chart Gradient" else "Edit Chart Colors"
                                    },
                                    color = if (isDarkTheme) Color.White else Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text(
                                        text = if (language == AppLanguage.BN) 
                                            "নিচের কালার প্যালেট থেকে সিলেক্ট করুন অথবা চেপে ধরে (ড্রাগ করে) ওপরের লাইনে বসান (সর্বোচ্চ ১০টি কালার)।" 
                                        else 
                                            "Click to replace or long-press & drag colors to upper line (up to 10 colors).",
                                        color = Color.Gray,
                                        fontSize = 12.sp
                                    )

                                    // Live Bar Preview (Gradient vs Segmented Palette)
                                    if (editedColors.isNotEmpty()) {
                                        if (isGradientChart && editedColors.size >= 2) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(28.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(Brush.horizontalGradient(editedColors))
                                                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                            )
                                        } else {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(28.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                            ) {
                                                editedColors.forEach { col ->
                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .fillMaxHeight()
                                                            .background(col)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Top Line: Active Colors
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.BN) "সক্রিয় কালারসমূহ (উপরের লাইন)" else "Active Colors (Upper Line)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDarkTheme) Color.LightGray else Color.DarkGray
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (editedColors.size < 10) {
                                                IconButton(
                                                    onClick = {
                                                        val newCol = presets.filter { it !in editedColors }.randomOrNull() ?: presets.random()
                                                        editedColors = editedColors + newCol
                                                        selectedUpperIndex = editedColors.size - 1
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Rounded.Add, contentDescription = "Add Color", tint = FintechBlue)
                                                }
                                            }
                                            if (editedColors.size > (if (isGradientChart) 2 else 1)) {
                                                IconButton(
                                                    onClick = {
                                                        val removeIdx = selectedUpperIndex.coerceIn(0, editedColors.size - 1)
                                                        val mutable = editedColors.toMutableList()
                                                        mutable.removeAt(removeIdx)
                                                        editedColors = mutable
                                                        selectedUpperIndex = (removeIdx - 1).coerceAtLeast(0)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Rounded.Delete, contentDescription = "Remove Color", tint = FintechRed)
                                                }
                                            }
                                        }
                                    }

                                    // Upper Row Scrollable List
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        editedColors.forEachIndexed { index, color ->
                                            val isSelected = selectedUpperIndex == index
                                            val isHovered = hoveredUpperIndex == index
                                            Box(
                                                modifier = Modifier
                                                    .onGloballyPositioned { coords ->
                                                        upperItemBounds[index] = coords.boundsInWindow()
                                                    }
                                                    .size(38.dp)
                                                    .clip(CircleShape)
                                                    .background(color)
                                                    .border(
                                                        width = if (isSelected || isHovered) 3.dp else 1.dp,
                                                        color = if (isHovered) Color.Yellow else if (isSelected) FintechBlue else (if (isDarkTheme) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.2f)),
                                                        shape = CircleShape
                                                    )
                                                    .clickable {
                                                        selectedUpperIndex = index
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = "Selected",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = if (isDarkTheme) Color(0xFF2E354F) else Color(0xFFE2E8F0))

                                    // Bottom Line: Palette Presets
                                    Text(
                                        text = if (language == AppLanguage.BN) "কালার প্যালেট (নিচের লাইন)" else "Color Palette (Lower Line)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkTheme) Color.LightGray else Color.DarkGray
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        presets.forEach { preset ->
                                            var itemTopLeftInWindow by remember { mutableStateOf(Offset.Zero) }
                                            Box(
                                                modifier = Modifier
                                                    .onGloballyPositioned { coords ->
                                                        itemTopLeftInWindow = coords.boundsInWindow().topLeft
                                                    }
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(preset)
                                                    .border(1.dp, if (isDarkTheme) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.15f), CircleShape)
                                                    .pointerInput(preset) {
                                                        detectDragGesturesAfterLongPress(
                                                            onDragStart = { startOffset ->
                                                                draggedColor = preset
                                                                dragWindowPos = itemTopLeftInWindow + startOffset
                                                            },
                                                            onDrag = { change, dragAmount ->
                                                                change.consume()
                                                                dragWindowPos += dragAmount
                                                                val match = upperItemBounds.entries.firstOrNull { (_, rect) ->
                                                                    rect.contains(dragWindowPos)
                                                                }
                                                                hoveredUpperIndex = match?.key
                                                            },
                                                            onDragEnd = {
                                                                val targetIdx = hoveredUpperIndex
                                                                if (targetIdx != null && targetIdx in editedColors.indices) {
                                                                    val mutable = editedColors.toMutableList()
                                                                    mutable[targetIdx] = preset
                                                                    editedColors = mutable
                                                                    selectedUpperIndex = targetIdx
                                                                }
                                                                draggedColor = null
                                                                hoveredUpperIndex = null
                                                            },
                                                            onDragCancel = {
                                                                draggedColor = null
                                                                hoveredUpperIndex = null
                                                            }
                                                        )
                                                    }
                                                    .clickable {
                                                        if (selectedUpperIndex in editedColors.indices) {
                                                            val mutable = editedColors.toMutableList()
                                                            mutable[selectedUpperIndex] = preset
                                                            editedColors = mutable
                                                        }
                                                    }
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        viewModel.updateBudgetGradient(type, editedColors)
                                        editingBudgetGradientType = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.BN) "সংরক্ষণ" else "Save",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { editingBudgetGradientType = null }) {
                                    Text(
                                        text = if (language == AppLanguage.BN) "বাতিল" else "Cancel",
                                        color = FintechRed
                                    )
                                }
                            }
                        )

                        // Floating Dragged Color Bubble
                        if (draggedColor != null) {
                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        translationX = dragWindowPos.x - dialogTopLeftInWindow.x - 21.dp.toPx()
                                        translationY = dragWindowPos.y - dialogTopLeftInWindow.y - 21.dp.toPx()
                                    }
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(draggedColor!!)
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }
                    }
                }
                if (pendingDeleteTransactions != null) {
                    DeleteVerificationDialog(
                        language = language,
                        onConfirm = {
                            viewModel.deleteTransactions(pendingDeleteTransactions!!)
                            pendingDeleteTransactions = null
                        },
                        onDismiss = { pendingDeleteTransactions = null }
                    )
                }

                if (pendingDeletePersons != null) {
                    DeleteVerificationDialog(
                        language = language,
                        onConfirm = {
                            viewModel.deletePersons(pendingDeletePersons!!)
                            pendingDeletePersons = null
                        },
                        onDismiss = { pendingDeletePersons = null }
                    )
                }

                if (pendingDeleteGoals != null) {
                    DeleteVerificationDialog(
                        language = language,
                        onConfirm = {
                            viewModel.deleteSavingsGoals(pendingDeleteGoals!!)
                            pendingDeleteGoals = null
                        },
                        onDismiss = { pendingDeleteGoals = null }
                    )
                }

                if (showAddTransactionDialog || transactionToEdit != null) {
                    AddTransactionDialog(viewModel = viewModel, 
                        language = language,
                        persons = persons,
                        isDark = isDarkTheme,
                        editTransaction = transactionToEdit,
                        onDismiss = { 
                            showAddTransactionDialog = false 
                            transactionToEdit = null
                        },
                        onConfirm = { amount, type, category, note, personId, timestamp, subType ->
                            if (transactionToEdit != null) {
                                viewModel.updateTransaction(transactionToEdit!!.copy(
                                    amount = amount,
                                    type = type,
                                    category = category,
                                    note = note,
                                    personId = personId,
                                    timestamp = timestamp,
                                    subType = subType
                                ))
                            } else {
                                viewModel.addTransaction(amount, type, category, note, personId, timestamp, subType)
                            }
                            showAddTransactionDialog = false
                            transactionToEdit = null
                        },
                        onAddPersonClick = {
                            showAddPersonDialog = true
                        }
                    )
                }

                if (showAddPersonDialog) {
                    AddPersonDialog(viewModel = viewModel, 
                        initialPerson = editingPerson,
                        language = language,
                        isDark = isDarkTheme,
                        onDismiss = { 
                            showAddPersonDialog = false
                            editingPerson = null
                        },
                        onConfirm = { name, phone, address, photoUri ->
                            if (editingPerson != null) {
                                viewModel.updatePerson(editingPerson!!.copy(name = name, phone = phone, address = address, photoUri = photoUri))
                            } else {
                                viewModel.addPerson(name, phone, address, photoUri)
                            }
                            showAddPersonDialog = false
                            editingPerson = null
                        }
                    )
                }

                if (showAddSavingsGoalDialog || goalToEdit != null) {
                    AddSavingsGoalDialog(viewModel = viewModel, 
                        language = language,
                        isDark = isDarkTheme,
                        initialGoal = goalToEdit,
                        onDismiss = { 
                            showAddSavingsGoalDialog = false
                            goalToEdit = null
                        },
                        onAddCustomGradient = {
                            showCustomGradientDialog = true
                            editingCustomGradientIndex = null
                            customGradientColors = listOf(Color(0xFF6F7BF7), Color(0xFF38BDF8))
                        },
                        onConfirm = { title, target, sector, colorIdx, cardholder ->
                            if (goalToEdit != null) {
                                viewModel.updateSavingsGoal(goalToEdit!!.copy(
                                    title = title,
                                    targetAmount = target,
                                    category = sector,
                                    colorIndex = colorIdx,
                                    cardholderName = cardholder
                                ))
                            } else {
                                viewModel.addSavingsGoal(title, target, sector, colorIdx, cardholder)
                            }
                            showAddSavingsGoalDialog = false
                            goalToEdit = null
                        }
                    )
                }

                if (showSavingsContributionDialog != null || savingsTxToEdit != null) {
                    val goalId = savingsTxToEdit?.goalId ?: showSavingsContributionDialog?.id ?: 0
                    val goal = savingsGoals.find { it.id == goalId } ?: showSavingsContributionDialog
                    if (goal != null) {
                        SavingsContributionDialog(viewModel = viewModel, 
                            language = language,
                            savingsGoal = goal,
                            isDark = isDarkTheme,
                            initialIsWithdraw = isWithdrawMode,
                            txToEdit = savingsTxToEdit,
                            onDismiss = { 
                                showSavingsContributionDialog = null 
                                savingsTxToEdit = null
                            },
                            onConfirm = { amount, isWithdraw, note, timestamp ->
                                val finalAmount = if (isWithdraw) -amount else amount
                                if (savingsTxToEdit != null) {
                                    val updatedTx = savingsTxToEdit!!.copy(
                                        amount = amount,
                                        isDeposit = !isWithdraw,
                                        note = note,
                                        timestamp = timestamp
                                    )
                                    viewModel.updateSavingsTransaction(savingsTxToEdit!!, updatedTx)
                                    savingsTxToEdit = null
                                } else {
                                    viewModel.addSavingsContribution(goal.id, finalAmount, note, timestamp = timestamp)
                                    showSavingsContributionDialog = null
                                }
                            }
                        )
                    }
                }

                if (showMonthPickerState) {
                    val calendar = java.util.Calendar.getInstance()
                    MonthYearPickerDialog(
                        initialYear = calendar.get(java.util.Calendar.YEAR),
                        initialMonth = calendar.get(java.util.Calendar.MONTH) + 1,
                        language = language,
                        onDismiss = { showMonthPickerState = false },
                        onConfirm = { year, month ->
                            timeFilter = "CUSTOM_MONTH:$year-$month"
                            showMonthPickerState = false
                        }
                    )
                }

                if (showDatePickerState) {
                    val calendar = java.util.Calendar.getInstance()
                    SpecificDatePickerDialog(
                        initialYear = calendar.get(java.util.Calendar.YEAR),
                        initialMonth = calendar.get(java.util.Calendar.MONTH) + 1,
                        initialDay = calendar.get(java.util.Calendar.DAY_OF_MONTH),
                        language = language,
                        onDismiss = { showDatePickerState = false },
                        onConfirm = { year, month, day, _, _ ->
                            timeFilter = "CUSTOM_DATE:$year-$month-$day"
                            showDatePickerState = false
                        }
                    )
                }

                // Detailed view for savings transactions
                AnimatedVisibility(
                    visible = selectedSavingsGoalDetail != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    val goalSnapshot = selectedSavingsGoalDetail
                    if (goalSnapshot != null) {
                        val goal = savingsGoals.find { it.id == goalSnapshot.id } ?: goalSnapshot
                        SavingsGoalDetailOverlay(
                            language = language,
                            isDark = isDarkTheme,
                            profileName = profileName,
                            allGradientsList = allGradientsList,
                            goal = goal,
                            transactionsFlow = viewModel.getSavingsTransactions(goal.id),
                            onDismiss = { selectedSavingsGoalDetail = null },
                            onDeleteGoal = { viewModel.deleteSavingsGoal(it) },
                            onEditGoal = { goalToEdit = it },
                            onContributeClick = { goalObj, isWithdraw ->
                                showSavingsContributionDialog = goalObj
                                isWithdrawMode = isWithdraw
                            },
                            onDeleteTx = { viewModel.deleteSavingsTransaction(it) },
                            onEditTx = { savingsTxToEdit = it },
                            onMoveGoal = { goalActionChoice = it }
                        )
                    }
                }

                // Detailed view for transactions with a specific person
                AnimatedVisibility(
                    visible = selectedPersonDetail != null,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                ) {
                    val debtInfoSnapshot = selectedPersonDetail
                    if (debtInfoSnapshot != null) {
                        val debtInfo = filteredPersonDebts.find { it.person.id == debtInfoSnapshot.person.id } ?: debtInfoSnapshot
                        PersonDetailOverlay(
                            language = language,
                            isDark = isDarkTheme,
                            personDebt = debtInfo,
                            transactionsFlow = viewModel.getTransactionsByPerson(debtInfo.person.id),
                            onDismiss = { selectedPersonDetail = null },
                            onLendClick = { amt, note, ts, subType ->
                                viewModel.addTransaction(amt, "LEND", "Lending", note.ifBlank { "Lent money to ${debtInfo.person.name}" }, debtInfo.person.id, timestamp = ts, subType = subType)
                            },
                            onBorrowClick = { amt, note, ts, subType ->
                                viewModel.addTransaction(amt, "BORROW", "Borrowing", note.ifBlank { "Borrowed money from ${debtInfo.person.name}" }, debtInfo.person.id, timestamp = ts, subType = subType)
                            },
                            onRepayPaidClick = { amt, note, ts ->
                                viewModel.addTransaction(amt, "REPAY_PAID", "Repay Paid", note.ifBlank { "Paid back borrowed loan" }, debtInfo.person.id, timestamp = ts)
                            },
                            onRepayReceivedClick = { amt, note, ts ->
                                viewModel.addTransaction(amt, "REPAY_RECEIVED", "Repay Received", note.ifBlank { "Received back lent loan" }, debtInfo.person.id, timestamp = ts)
                            },
                            onDeleteTx = { viewModel.deleteTransaction(it) },
                            onEditTx = { transactionToEdit = it },
                            onEditPerson = {
                                editingPerson = it
                                showAddPersonDialog = true
                            },
                            onDeletePerson = {
                                showDeletePersonConfirmId = debtInfo.person.id
                            }
                        )
                    }
                }

                if (showDeletePersonConfirmId != null) {
                    DeleteVerificationDialog(
                        language = language,
                        onConfirm = {
                            showDeletePersonConfirmId?.let { id ->
                                viewModel.deletePerson(id)
                            }
                            showDeletePersonConfirmId = null
                            selectedPersonDetail = null
                        },
                        onDismiss = {
                            showDeletePersonConfirmId = null
                        }
                    )
                }



                if (showBackupConfirm && cloudBackupStats != null) {
                    val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                    val defaultName = "finance_note_backup_$timestamp.json"

                    BackupStatsDialog(
                        title = if (language == AppLanguage.BN) "ক্লাউড ব্যাকআপ সামারি" else "Cloud Backup Summary",
                        stats = cloudBackupStats!!,
                        language = language,
                        isDark = isDarkTheme,
                        isRestoreMode = false,
                        initialFileName = defaultName,
                        workspaces = workspaceStatsList,
                        onConfirm = { finalFileName, comment, selectedWorkspaceIds ->
                            val backupAction = {
                                viewModel.backupToGoogleDrive(
                                    context = context,
                                    customFileName = finalFileName,
                                    comment = comment,
                                    workspaceIds = selectedWorkspaceIds,
                                    onSuccess = {
                                        viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "ড্রাইভ ব্যাকআপ সফল হয়েছে!" else "Drive Backup successful!", isSuccess = true, type = "SUCCESS")
                                        viewModel.listGoogleDriveFiles(context)
                                    },
                                    onError = { err ->
                                        viewModel.triggerCustomNotification("${if (language == AppLanguage.BN) "ব্যাকআপ ব্যর্থ হয়েছে: " else "Backup failed: "}$err", isSuccess = false, type = "ERROR")
                                    }
                                )
                            }
                            showBackupConfirm = false
                            if (!viewModel.isNetworkAvailable(context)) {
                                onNoInternetRetryAction = backupAction
                                showNoInternetDialog = true
                            } else {
                                backupAction()
                            }
                        },
                        onDismiss = { showBackupConfirm = false }
                    )
                }

                if (showRestoreListDialog) {
                    GoogleDriveRestoreListDialog(
                        language = language,
                        isDark = isDarkTheme,
                        isFetching = isFetchingFiles,
                        files = googleDriveFiles,
                        viewModel = viewModel,
                        onDismiss = { showRestoreListDialog = false },
                        onRestoreSuccess = {
                            viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "ড্রাইভ থেকে রিস্টোর সফল হয়েছে!" else "Drive Restore successful!", isSuccess = true, type = "SUCCESS")
                            showRestoreListDialog = false
                        },
                        onDelete = { fileId ->
                            executeWithInternetCheck {
                                viewModel.deleteGoogleDriveFile(
                                    context = context,
                                    fileId = fileId,
                                    onSuccess = {
                                        viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "ফাইলটি সফলভাবে ডিলিট করা হয়েছে!" else "File deleted successfully!", isSuccess = true, type = "SUCCESS")
                                    },
                                    onError = { err ->
                                        viewModel.triggerCustomNotification("${if (language == AppLanguage.BN) "ডিলিট ব্যর্থ হয়েছে: " else "Delete failed: "}$err", isSuccess = false, type = "ERROR")
                                    }
                                )
                            }
                        },
                        onRefresh = {
                            executeWithInternetCheck {
                                viewModel.listGoogleDriveFiles(context)
                            }
                        },
                        executeWithInternetCheck = executeWithInternetCheck
                    )
                }
            }
        }
        
        if (isSyncing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(enabled = false) {}, // Intercept clicks
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .background(if (isDarkTheme) Color(0xFF121212) else Color.White, RoundedCornerShape(16.dp))
                        .padding(32.dp)
                ) {
                    CircularProgressIndicator(color = FintechBlue)
                    Text(
                        text = if (language == AppLanguage.BN) "দয়া করে অপেক্ষা করুন..." else "Please wait...",
                        color = if (isDarkTheme) Color.White else Color(0xFF1E293B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    if (showThemeCustomizeDialog) {
        val allGradientsList by viewModel.allGradientsConfig.collectAsState()
        val staticGradientsSize = com.example.ui.theme.GradientsList.size

        AlertDialog(
            onDismissRequest = { showThemeCustomizeDialog = false },
            containerColor = if (isDarkTheme) Color(0xFF1E2235) else Color.White,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.BN) "থিম কাস্টমাইজেশন" else "Theme Customization",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                        )
                    }
                    IconButton(
                        onClick = { showThemeCustomizeDialog = false },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = if (isDarkTheme) Color.Gray else Color.DarkGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Dark Mode Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDarkTheme) Color(0xFF282E47) else Color(0xFFF1F5F9))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                                contentDescription = null,
                                tint = if (isDarkTheme) Color(0xFFFBBF24) else Color(0xFF3B82F6),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (language == AppLanguage.BN) "ডার্ক মোড" else "Dark Mode",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                            )
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { viewModel.toggleTheme(context) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = if (isDarkTheme) Color(0xFF1E2235) else Color(0xFFE2E8F0)
                            )
                        )
                    }

                    // 2. Color Schemes Title and Add button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "পছন্দের কালার স্কীম বেছে নিন" else "Choose Color Scheme",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.LightGray else Color(0xFF475569),
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = {
                                editingCustomGradientIndex = null
                                customGradientColors = listOf(Color(0xFF6F7BF7), Color(0xFF38BDF8))
                                selectedColorIndexInGradient = 0
                                showCustomGradientDialog = true
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add custom gradient",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.BN) "নতুন" else "New",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // 3. Grid of Color Schemes with Chunked Rows to prevent nested scroll issues
                    val chunkedGradients = allGradientsList.chunked(4)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        chunkedGradients.forEachIndexed { rowIndex, rowGradients ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowGradients.forEachIndexed { colIndex, gradient ->
                                    val actualIndex = rowIndex * 4 + colIndex
                                    val isSelected = selectedThemeIndex == actualIndex
                                    val firstColor = gradient.colors.firstOrNull() ?: Color.Gray

                                    @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(gradient.toBrush())
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) {
                                                    if (isDarkTheme) Color.White else Color(0xFF0F172A)
                                                } else {
                                                    Color.White.copy(alpha = 0.3f)
                                                },
                                                shape = CircleShape
                                            )
                                            .combinedClickable(
                                                onClick = {
                                                    viewModel.selectThemeGradientIndex(context, actualIndex)
                                                },
                                                onLongClick = {
                                                    if (actualIndex > 0) {
                                                        longPressedIndex = actualIndex
                                                        showLongPressOptions = true
                                                    }
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.9f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Check,
                                                    contentDescription = "Selected",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                if (rowGradients.size < 4) {
                                    repeat(4 - rowGradients.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showThemeCustomizeDialog = false }
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "সম্পন্ন" else "Done",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }

    if (showCustomGradientDialog) {
        val activeStats = workspaceStatsList.find { it.workspace.id == currentWorkspace.id }
        val currentTotalIncome = activeStats?.income ?: 0.0

        val parseHexColor: (String) -> Color? = { hex ->
            val clean = hex.trim().replace("#", "")
            try {
                if (clean.length == 6) {
                    val r = clean.substring(0, 2).toInt(16)
                    val g = clean.substring(2, 4).toInt(16)
                    val b = clean.substring(4, 6).toInt(16)
                    Color(r, g, b)
                } else if (clean.length == 8) {
                    val a = clean.substring(0, 2).toInt(16)
                    val r = clean.substring(2, 4).toInt(16)
                    val g = clean.substring(4, 6).toInt(16)
                    val b = clean.substring(6, 8).toInt(16)
                    Color(r, g, b, a)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }

        AlertDialog(
            onDismissRequest = { showCustomGradientDialog = false },
            containerColor = if (isDarkTheme) Color(0xFF1E2235) else Color.White,
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingCustomGradientIndex != null) {
                            if (language == AppLanguage.BN) "গ্রাডিয়েন্ট এডিট করুন" else "Edit Gradient"
                        } else {
                            if (language == AppLanguage.BN) "কাস্টম গ্রাডিয়েন্ট তৈরি করুন" else "Create Custom Gradient"
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                    )
                    IconButton(
                        onClick = { showCustomGradientDialog = false },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = if (isDarkTheme) Color.Gray else Color.DarkGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Preview Card Title
                    Text(
                        text = if (language == AppLanguage.BN) "প্রিভিউ (মোট আয় কার্ড)" else "Preview (Total Income Card)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)
                    )

                    // Card Preview
                    FintechGradientCard(
                        gradientColors = customGradientColors,
                        brush = com.example.ui.theme.ThemeGradient(customGradientColors, customGradientType, customGradientDirection).toBrush(),
                        cornerRadius = 24.dp,
                        padding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (language == AppLanguage.BN) "মোট আয়" else "Total Income",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatCurrency(currentTotalIncome, language),
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 20.sp,
                                    maxLines = 1
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = if (isDarkTheme) Color(0xFF2E354F) else Color(0xFFE2E8F0))

                    // Gradient Direction Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (language == AppLanguage.BN) "গ্রাডিয়েন্ট ডিরেকশন" else "Gradient Direction",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            com.example.ui.theme.CustomGradientDirection.values().forEach { dir ->
                                val isSelected = customGradientDirection == dir
                                val label = when (dir) {
                                    com.example.ui.theme.CustomGradientDirection.HORIZONTAL -> if (language == AppLanguage.BN) "হরিজন্টাল" else "Horizontal"
                                    com.example.ui.theme.CustomGradientDirection.VERTICAL -> if (language == AppLanguage.BN) "ভার্টিক্যাল" else "Vertical"
                                    com.example.ui.theme.CustomGradientDirection.DIAGONAL -> if (language == AppLanguage.BN) "ডায়াগনাল" else "Diagonal"
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) FintechBlue else (if (isDarkTheme) Color(0xFF2A2D3D) else Color(0xFFE2E8F0)))
                                        .clickable { customGradientDirection = dir }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else (if (isDarkTheme) Color.LightGray else Color.DarkGray)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = if (isDarkTheme) Color(0xFF2E354F) else Color(0xFFE2E8F0))

                    // Gradient Colors list with selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "গ্রাডিয়েন্ট কালারসমূহ" else "Gradient Colors",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Add Color button
                            if (customGradientColors.size < 5) {
                                IconButton(
                                    onClick = {
                                        customGradientColors = customGradientColors + Color(0xFF8B5CF6)
                                        selectedColorIndexInGradient = customGradientColors.size - 1
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = "Add Color",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            // Delete Color button (visible if > 2 colors)
                            if (customGradientColors.size > 2) {
                                IconButton(
                                    onClick = {
                                        val mutable = customGradientColors.toMutableList()
                                        mutable.removeAt(selectedColorIndexInGradient)
                                        customGradientColors = mutable
                                        selectedColorIndexInGradient = Math.max(0, selectedColorIndexInGradient - 1)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "Delete Color",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Display horizontal list of gradient circles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        customGradientColors.forEachIndexed { idx, color ->
                            val isSelected = selectedColorIndexInGradient == idx
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) {
                                            if (isDarkTheme) Color.White else Color(0xFF0F172A)
                                        } else {
                                            Color.Gray.copy(alpha = 0.5f)
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorIndexInGradient = idx },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected color",
                                        tint = if (color.red * 0.299 + color.green * 0.587 + color.blue * 0.114 > 0.5) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    // RGB Sliders for the selected color
                    val activeColor = customGradientColors.getOrNull(selectedColorIndexInGradient) ?: Color.White
                    var redSliderVal by remember(activeColor) { mutableStateOf((activeColor.red * 255f)) }
                    var greenSliderVal by remember(activeColor) { mutableStateOf((activeColor.green * 255f)) }
                    var blueSliderVal by remember(activeColor) { mutableStateOf((activeColor.blue * 255f)) }
                    var hexText by remember(activeColor) {
                        mutableStateOf(
                            String.format("#%02X%02X%02X", (activeColor.red * 255f).toInt(), (activeColor.green * 255f).toInt(), (activeColor.blue * 255f).toInt())
                        )
                    }

                    // Red Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.BN) "লাল (Red)" else "Red",
                                fontSize = 11.sp,
                                color = if (isDarkTheme) Color.LightGray else Color.Gray
                            )
                            Text(
                                text = "${redSliderVal.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color.LightGray else Color.Gray
                            )
                        }
                        Slider(
                            value = redSliderVal,
                            onValueChange = { newValue ->
                                redSliderVal = newValue
                                val newCol = Color(redSliderVal.toInt(), greenSliderVal.toInt(), blueSliderVal.toInt())
                                val mutable = customGradientColors.toMutableList()
                                if (selectedColorIndexInGradient in mutable.indices) {
                                    mutable[selectedColorIndexInGradient] = newCol
                                    customGradientColors = mutable
                                }
                            },
                            valueRange = 0f..255f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Red,
                                activeTrackColor = Color.Red.copy(alpha = 0.7f),
                                inactiveTrackColor = Color.Red.copy(alpha = 0.2f)
                            )
                        )
                    }

                    // Green Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.BN) "সবুজ (Green)" else "Green",
                                fontSize = 11.sp,
                                color = if (isDarkTheme) Color.LightGray else Color.Gray
                            )
                            Text(
                                text = "${greenSliderVal.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color.LightGray else Color.Gray
                            )
                        }
                        Slider(
                            value = greenSliderVal,
                            onValueChange = { newValue ->
                                greenSliderVal = newValue
                                val newCol = Color(redSliderVal.toInt(), greenSliderVal.toInt(), blueSliderVal.toInt())
                                val mutable = customGradientColors.toMutableList()
                                if (selectedColorIndexInGradient in mutable.indices) {
                                    mutable[selectedColorIndexInGradient] = newCol
                                    customGradientColors = mutable
                                }
                            },
                            valueRange = 0f..255f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF10B981),
                                activeTrackColor = Color(0xFF10B981).copy(alpha = 0.7f),
                                inactiveTrackColor = Color(0xFF10B981).copy(alpha = 0.2f)
                            )
                        )
                    }

                    // Blue Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.BN) "নীল (Blue)" else "Blue",
                                fontSize = 11.sp,
                                color = if (isDarkTheme) Color.LightGray else Color.Gray
                            )
                            Text(
                                text = "${blueSliderVal.toInt()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color.LightGray else Color.Gray
                            )
                        }
                        Slider(
                            value = blueSliderVal,
                            onValueChange = { newValue ->
                                blueSliderVal = newValue
                                val newCol = Color(redSliderVal.toInt(), greenSliderVal.toInt(), blueSliderVal.toInt())
                                val mutable = customGradientColors.toMutableList()
                                if (selectedColorIndexInGradient in mutable.indices) {
                                    mutable[selectedColorIndexInGradient] = newCol
                                    customGradientColors = mutable
                                }
                            },
                            valueRange = 0f..255f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF3B82F6),
                                activeTrackColor = Color(0xFF3B82F6).copy(alpha = 0.7f),
                                inactiveTrackColor = Color(0xFF3B82F6).copy(alpha = 0.2f)
                            )
                        )
                    }

                    // Hex Color input
                    OutlinedTextField(
                        value = hexText,
                        onValueChange = { text ->
                            hexText = text
                            val parsed = parseHexColor(text)
                            if (parsed != null) {
                                val mutable = customGradientColors.toMutableList()
                                if (selectedColorIndexInGradient in mutable.indices) {
                                    mutable[selectedColorIndexInGradient] = parsed
                                    customGradientColors = mutable
                                }
                            }
                        },
                        label = { Text(if (language == AppLanguage.BN) "হেক্স কোড (Hex Code)" else "Hex Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDarkTheme) Color.White else Color(0xFF1E293B),
                            unfocusedTextColor = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                        ),
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(activeColor)
                                    .border(1.dp, Color.Gray.copy(alpha = 0.5f), CircleShape)
                            )
                        }
                    )

                    // Palette Presets
                    Text(
                        text = if (language == AppLanguage.BN) "সহজ কালার প্যালেট" else "Preset Colors",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color.LightGray else Color.Gray
                    )
                    
                    val presets = listOf(
                        Color(0xFF3B82F6), Color(0xFF10B981), Color(0xFFEF4444),
                        Color(0xFFFBBF24), Color(0xFF8B5CF6), Color(0xFFEC4899),
                        Color(0xFF06B6D4), Color(0xFF14B8A6), Color(0xFFF97316),
                        Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFFD946EF),
                        Color(0xFFF43F5E), Color(0xFF84CC16), Color(0xFF22C55E),
                        Color(0xFF10B981), Color(0xFF0EA5E9), Color(0xFF64748B),
                        Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFFFFFFFF), Color(0xFF94A3B8)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presets.forEach { presetColor ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(presetColor)
                                    .border(1.dp, if (isDarkTheme) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.15f), CircleShape)
                                    .clickable {
                                        val mutable = customGradientColors.toMutableList()
                                        if (selectedColorIndexInGradient in mutable.indices) {
                                            mutable[selectedColorIndexInGradient] = presetColor
                                            customGradientColors = mutable
                                        }
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCustomGradientDialog = false
                        val idx = editingCustomGradientIndex
                        if (idx != null) {
                            viewModel.updateGradientAt(context, idx, customGradientColors, type = customGradientType, direction = customGradientDirection)
                        } else {
                            viewModel.addCustomGradient(context, customGradientColors, type = customGradientType, direction = customGradientDirection)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "সংরক্ষণ" else "Save",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCustomGradientDialog = false }
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "বাতিল" else "Cancel",
                        color = if (isDarkTheme) Color.Gray else Color.DarkGray
                    )
                }
            }
        )
    }

    if (showLongPressOptions) {
        val allGradientsList by viewModel.allGradientsConfig.collectAsState()
        AlertDialog(
            onDismissRequest = { showLongPressOptions = false },
            containerColor = if (isDarkTheme) Color(0xFF1E2235) else Color.White,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = if (language == AppLanguage.BN) "গ্রাডিয়েন্ট অপশন" else "Gradient Options",
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                )
            },
            text = {
                Text(
                    text = if (language == AppLanguage.BN) "আপনি কি এই কাস্টম গ্রাডিয়েন্টটি এডিট বা ডিলিট করতে চান?" else "Do you want to edit or delete this custom gradient?",
                    color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLongPressOptions = false
                        editingCustomGradientIndex = longPressedIndex
                        val selectedGradient = allGradientsList.getOrNull(longPressedIndex) ?: ThemeGradient(GradientsList.first())
                        customGradientColors = selectedGradient.colors
                        customGradientType = selectedGradient.type
                        customGradientDirection = selectedGradient.direction
                        selectedColorIndexInGradient = 0
                        showCustomGradientDialog = true
                    }
                ) {
                    Text(if (language == AppLanguage.BN) "এডিট করুন" else "Edit")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showLongPressOptions = false
                            viewModel.deleteGradientAt(context, longPressedIndex)
                        }
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "ডিলিট করুন" else "Delete",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    TextButton(
                        onClick = { showLongPressOptions = false }
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "বাতিল" else "Cancel",
                            color = if (isDarkTheme) Color.Gray else Color.DarkGray
                        )
                    }
                }
            }
        )
    }

    if (showRealtimeSyncDialog) {
        val isNetworkAvailable = viewModel.isNetworkAvailable(context)
        val hasUnsavedChanges by viewModel.hasUnsavedChanges.collectAsState()
        val lastSyncTime by viewModel.lastSyncTime.collectAsState()
        val syncStatus by viewModel.firestoreSyncStatus.collectAsState()
        val lastMutationAction by viewModel.lastMutationAction.collectAsState()
        val lastMutationName by viewModel.lastMutationName.collectAsState()
        val lastMutationCategory by viewModel.lastMutationCategory.collectAsState()
        val lastMutationAmount by viewModel.lastMutationAmount.collectAsState()

        AlertDialog(
            onDismissRequest = { showRealtimeSyncDialog = false },
            containerColor = if (isDarkTheme) Color(0xFF121212) else Color.White,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = if (language == AppLanguage.BN) "রিয়েল-টাইম ক্লাউড সিঙ্ক" else "Real-time Cloud Sync",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sign-in Status Notice
                    if (!isGoogleSignedIn) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) Color(0x1AFF5252) else Color(0xFFFFEBEE)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = null,
                                    tint = Color.Red
                                )
                                Text(
                                    text = if (language == AppLanguage.BN) {
                                        "ক্লাউড সিঙ্ক ব্যবহার করতে প্রথমে সেটিংস থেকে গুগল দিয়ে সাইন-ইন সম্পন্ন করুন।"
                                    } else {
                                        "Please sign in with Google in settings to enable cloud sync."
                                    },
                                    fontSize = 13.sp,
                                    color = if (isDarkTheme) Color(0xFFFF8A80) else Color(0xFFD32F2F),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    } else {
                        // Signed In User Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.BN) "ইউজার অ্যাকাউন্ট:" else "User Account:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)
                            )
                            Text(
                                text = googleEmail ?: "",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color(0xFF90CAF9) else Color(0xFF1E88E5)
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)))

                        // Network Status Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.BN) "নেটওয়ার্ক স্ট্যাটাস:" else "Network Status:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isNetworkAvailable) Color(0xFF4CAF50) else Color(0xFFF44336))
                                )
                                Text(
                                    text = if (isNetworkAvailable) {
                                        if (language == AppLanguage.BN) "অনলাইন" else "Online"
                                    } else {
                                        if (language == AppLanguage.BN) "অফলাইন" else "Offline"
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNetworkAvailable) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)))

                        // Unsaved Changes Row
                        if (hasUnsavedChanges) {
                            val unsyncedItems = viewModel.getUnsyncedItems()
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BN) "অসংরক্ষিত ডাটা:" else "Unsaved Data:",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)
                                )
                                Text(
                                    text = unsyncedItems.joinToString(", "),
                                    fontSize = 13.sp,
                                    color = Color(0xFFFFB300),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BN) "সব সংরক্ষিত:" else "All Saved:",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)
                                )
                                Text(
                                    text = if (language == AppLanguage.BN) "না (সব সংরক্ষিত)" else "No (Fully saved)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)))

                        // Last Sync Time Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.BN) "সর্বশেষ সিঙ্ক:" else "Last Synced:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)
                            )
                            Text(
                                text = formatSyncTime(lastSyncTime, language),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                            )
                        }
                        
                        if (lastMutationAction != null) {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDarkTheme) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.02f))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (hasUnsavedChanges) {
                                        if (language == AppLanguage.BN) "অসংরক্ষিত ডাটা (সিঙ্ক প্রয়োজন):" else "Unsaved Data (Sync Needed):"
                                    } else {
                                        if (language == AppLanguage.BN) "সর্বশেষ সিঙ্কড ডাটা:" else "Last Synced Data:"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasUnsavedChanges) Color(0xFFFFB300) else Color(0xFF4CAF50)
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                val actionText = when (lastMutationAction) {
                                    "ADD" -> if (language == AppLanguage.BN) "যোগ করা হয়েছে" else "Added"
                                    "EDIT" -> if (language == AppLanguage.BN) "আপডেট করা হয়েছে" else "Updated"
                                    "DELETE" -> if (language == AppLanguage.BN) "মুছে ফেলা হয়েছে" else "Deleted"
                                    else -> lastMutationAction ?: ""
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.BN) "কাজ:" else "Action:",
                                        fontSize = 12.sp,
                                        color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)
                                    )
                                    Text(
                                        text = actionText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.BN) "নাম:" else "Name:",
                                        fontSize = 12.sp,
                                        color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)
                                    )
                                    Text(
                                        text = lastMutationName ?: "-",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.BN) "খাত:" else "Category:",
                                        fontSize = 12.sp,
                                        color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)
                                    )
                                    
                                    val categoryDisplay = when (lastMutationCategory) {
                                        "PERSON" -> if (language == AppLanguage.BN) "ব্যক্তি/লেনদেন" else "Person/Debt"
                                        "SAVINGS_GOAL" -> if (language == AppLanguage.BN) "সঞ্চয় লক্ষ্য" else "Savings Goal"
                                        "SAVINGS_CONTRIBUTION" -> if (language == AppLanguage.BN) "সঞ্চয় লেনদেন" else "Savings Transaction"
                                        else -> {
                                            val cat = lastMutationCategory ?: ""
                                            if (language == AppLanguage.BN) {
                                                cat.replace("EXPENSE", "ব্যয়").replace("INCOME", "আয়")
                                            } else {
                                                cat
                                            }
                                        }
                                    }
                                    
                                    Text(
                                        text = categoryDisplay,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                                    )
                                }

                                if (lastMutationAmount != null && lastMutationAmount!! > 0.0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (language == AppLanguage.BN) "পরিমান:" else "Amount:",
                                            fontSize = 12.sp,
                                            color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)
                                        )
                                        Text(
                                            text = if (language == AppLanguage.BN) "৳${String.format("%,.2f", lastMutationAmount)}" else "$${String.format("%,.2f", lastMutationAmount)}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                                        )
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)))
                            
                            Text(
                                text = if (hasUnsavedChanges) {
                                    if (language == AppLanguage.BN) "অসংরক্ষিত ডাটা আছে" else "Unsaved data present"
                                } else {
                                    if (language == AppLanguage.BN) "সব ডেটা সিঙ্ক হয়েছে" else "All data synced"
                                },
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = if (isDarkTheme) Color.Gray else Color.DarkGray,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (isDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)))

                        // Sync status description if syncing
                        if (!syncStatus.isNullOrEmpty()) {
                            Text(
                                text = "${if (language == AppLanguage.BN) "স্ট্যাটাস: " else "Status: "}$syncStatus",
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                                color = if (isDarkTheme) Color.LightGray else Color.Gray,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (isGoogleSignedIn) {
                    Button(
                        onClick = {
                            val syncAction = {
                                viewModel.uploadToFirestore(
                                    onComplete = {
                                        viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "ক্লাউড সিঙ্ক সফলভাবে সম্পন্ন হয়েছে!" else "Cloud sync completed successfully!", isSuccess = true, type = "SUCCESS")
                                    },
                                    onError = { err ->
                                        viewModel.triggerCustomNotification("${if (language == AppLanguage.BN) "সিঙ্ক ব্যর্থ হয়েছে: " else "Sync failed: "}$err", isSuccess = false, type = "ERROR")
                                    }
                                )
                            }
                            if (!viewModel.isNetworkAvailable(context)) {
                                onNoInternetRetryAction = syncAction
                                showNoInternetDialog = true
                            } else {
                                syncAction()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FintechBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Rounded.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (language == AppLanguage.BN) "এখনই সিঙ্ক করুন" else "Sync Now")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRealtimeSyncDialog = false }) {
                    Text(if (language == AppLanguage.BN) "বন্ধ করুন" else "Close")
                }
            }
        )
    }

    if (showNoInternetDialog) {
        AlertDialog(
            onDismissRequest = { showNoInternetDialog = false },
            containerColor = if (isDarkTheme) Color(0xFF121212) else Color.White,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Wifi,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = if (language == AppLanguage.BN) "ইন্টারনেট সংযোগ নেই" else "No Internet Connection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                    )
                }
            },
            text = {
                Text(
                    text = if (language == AppLanguage.BN)
                        "আপনার অনুরোধকৃত কাজটি সম্পন্ন করার জন্য ইন্টারনেট সংযোগ প্রয়োজন। অনুগ্রহ করে ইন্টারনেট কানেকশন চালু করে আবার চেষ্টা করুন।"
                    else
                        "An active internet connection is required to complete this task. Please enable internet and try again.",
                    color = if (isDarkTheme) Color.LightGray else Color.DarkGray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Settings Button
                    Button(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS)
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
                                    context.startActivity(intent)
                                } catch (ex: Exception) {
                                    viewModel.triggerCustomNotification(
                                        if (language == AppLanguage.BN) "সেটিংস খুলতে ব্যর্থ হয়েছে" else "Failed to open settings",
                                        isSuccess = false,
                                        type = "ERROR"
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray
                        )
                    ) {
                        Text(if (language == AppLanguage.BN) "সেটিংস" else "Settings")
                    }

                    // Retry Button
                    Button(
                        onClick = {
                            if (viewModel.isNetworkAvailable(context)) {
                                showNoInternetDialog = false
                                onNoInternetRetryAction?.invoke()
                            } else {
                                viewModel.triggerCustomNotification(
                                    if (language == AppLanguage.BN) "এখনও ইন্টারনেট সংযোগ পাওয়া যায়নি! অনুগ্রহ করে কানেকশন চালু করুন।" else "Still no internet connection! Please check your network connection.",
                                    isSuccess = false,
                                    type = "ERROR"
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FintechBlue
                        )
                    ) {
                        Text(if (language == AppLanguage.BN) "আবার চেষ্টা করুন" else "Retry")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoInternetDialog = false }) {
                    Text(if (language == AppLanguage.BN) "বন্ধ করুন" else "Close")
                }
            }
        )
    }

    var cloudRestoreUserInput by remember { mutableStateOf("") }
    val cloudRestoreCaptcha = remember(showCloudDataFoundDialog) {
        val chars = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"
        (1..4).map { chars.random() }.joinToString("")
    }
    val isCloudRestoreCaptchaCorrect = cloudRestoreUserInput.trim().equals(cloudRestoreCaptcha, ignoreCase = true)

    if (showCloudDataFoundDialog && pendingCloudData != null) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.dismissCloudDataFoundDialog() 
                cloudRestoreUserInput = ""
            },
            containerColor = if (isDarkTheme) Color(0xFF121212) else Color.White,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.CloudDownload, contentDescription = null, tint = FintechBlue, modifier = Modifier.size(28.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "ক্লাউড ডেটা পাওয়া গেছে" else "Cloud Data Found",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (language == AppLanguage.BN)
                            "আপনার এই গুগল অ্যাকাউন্টে পূর্বের ব্যাকআপ ডাটা পাওয়া গেছে। আপনি কি সেই ডাটা রিস্টোর করতে চান? \n\nসতর্কতা: রিস্টোর করলে বর্তমান লোকাল ডাটা মুছে যাবে।"
                        else "Previous backup data was found on this Google account. Do you want to restore that data?\n\nWarning: Restoring will overwrite your current local data.",
                        fontSize = 14.sp,
                        color = if (isDarkTheme) Color.LightGray else Color(0xFF475569)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Text(
                            text = if (language == AppLanguage.BN)
                                "মোট লেনদেন: ${pendingCloudData?.transactions?.size ?: 0}"
                            else "Total Transactions: ${pendingCloudData?.transactions?.size ?: 0}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isDarkTheme) Color(0xFF282E47) else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "রিস্টোর নিশ্চিত করতে ক্যাপচা কোডটি টাইপ করুন" else "To confirm restore, type the captcha code",
                            fontSize = 12.sp,
                            color = if (isDarkTheme) Color.Gray else Color(0xFF64748B),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = cloudRestoreCaptcha,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 6.sp,
                            color = Color(0xFF10B981),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        OutlinedTextField(
                            value = cloudRestoreUserInput,
                            onValueChange = { cloudRestoreUserInput = it },
                            placeholder = { Text(if (language == AppLanguage.BN) "ক্যাপচা কোড" else "Captcha Code") },
                            modifier = Modifier.fillMaxWidth().testTag("cloud_restore_captcha_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isCloudRestoreCaptchaCorrect) Color(0xFF10B981) else Color(0xFF3B82F6),
                                unfocusedBorderColor = if (isDarkTheme) Color(0xFF2E334D) else Color(0xFFCBD5E1),
                                focusedTextColor = if (isDarkTheme) Color.White else Color(0xFF1E293B),
                                unfocusedTextColor = if (isDarkTheme) Color.White else Color(0xFF1E293B)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val userInputToClear = cloudRestoreUserInput
                            cloudRestoreUserInput = ""
                            viewModel.confirmCloudSync(context, backupLocalFirst = true) {
                                viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "ব্যাকআপ ও রিস্টোর সফল হয়েছে!" else "Backup & Restore successful!", isSuccess = true, type = "SUCCESS")
                            }
                        },
                        enabled = isCloudRestoreCaptchaCorrect,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FintechBlue,
                            disabledContainerColor = FintechBlue.copy(alpha = 0.6f)
                        )
                    ) {
                        Text(if (language == AppLanguage.BN) "ব্যাকআপ নিয়ে রিস্টোর করুন" else "Backup & Restore", color = Color.White)
                    }
                    Button(
                        onClick = {
                            val userInputToClear = cloudRestoreUserInput
                            cloudRestoreUserInput = ""
                            viewModel.confirmCloudSync(context, backupLocalFirst = false) {
                                viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "রিস্টোর সফল হয়েছে!" else "Restore successful!", isSuccess = true, type = "SUCCESS")
                            }
                        },
                        enabled = isCloudRestoreCaptchaCorrect,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkTheme) Color.DarkGray else Color.LightGray,
                            disabledContainerColor = (if (isDarkTheme) Color.DarkGray else Color.LightGray).copy(alpha = 0.6f)
                        )
                    ) {
                        Text(if (language == AppLanguage.BN) "সরাসরি রিস্টোর (ব্যাকআপ ছাড়া)" else "Restore (No Backup)", color = if (isDarkTheme) Color.White else Color.Black)
                    }
                }
            },
            dismissButton = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            cloudRestoreUserInput = ""
                            viewModel.skipCloudSyncAndOverwrite(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (language == AppLanguage.BN) "লোকাল ডাটা রাখুন (ক্লাউড আপডেট করুন)" else "Keep Local (Update Cloud)", color = FintechBlue)
                    }
                    TextButton(
                        onClick = { 
                            cloudRestoreUserInput = ""
                            viewModel.dismissCloudDataFoundDialog() 
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (language == AppLanguage.BN) "বাতিল" else "Cancel", color = FintechRed)
                    }
                }
            }
        )
    }

    if (showUpdatePopup) {
        AlertDialog(
            onDismissRequest = {
                if (!updateInfo.isForceUpdate) {
                    showUpdatePopup = false
                }
            },
            properties = if (updateInfo.isForceUpdate) {
                androidx.compose.ui.window.DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            } else {
                androidx.compose.ui.window.DialogProperties()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Update, contentDescription = null, tint = FintechBlue, modifier = Modifier.size(24.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "নতুন আপডেট উপলব্ধ!" else "New Update Available!",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (language == AppLanguage.BN)
                            "অ্যাপের একটি নতুন সংস্করণ (${updateInfo.latestVersion}) পাওয়া যাচ্ছে। আরও উন্নত ফিচার ও সিকিউরিটির জন্য এখনই আপডেট করুন।"
                        else "A new version of the app (${updateInfo.latestVersion}) is available. Please update to get the latest features and security improvements.",
                        fontSize = 14.sp
                    )
                    
                    if (updateInfo.updateDetails.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == AppLanguage.BN) "নতুন কী আছে (Changelog):" else "What's New (Changelog):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isDarkTheme) Color.LightGray else Color.DarkGray
                        )
                        Text(
                            text = updateInfo.updateDetails,
                            fontSize = 13.sp,
                            color = if (isDarkTheme) Color.White else Color.Black
                        )
                    }
                    
                    if (updateInfo.isForceUpdate) {
                        Text(
                            text = if (language == AppLanguage.BN) "এটি একটি আবশ্যক আপডেট। চালিয়ে যেতে আপডেট করুন।" else "This is a required update. Please update to continue.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = FintechRed
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.updateUrl))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                ) {
                    Text(text = if (language == AppLanguage.BN) "আপডেট করুন" else "Update Now", color = Color.White)
                }
            },
            dismissButton = {
                if (!updateInfo.isForceUpdate) {
                    TextButton(onClick = { showUpdatePopup = false }) {
                        Text(if (language == AppLanguage.BN) "পরে করব" else "Later", color = Color.Gray)
                    }
                }
            }
        )
    }

    if (pendingDifferentAccountLogin != null) {
        val account = pendingDifferentAccountLogin!!
        AlertDialog(
            onDismissRequest = { pendingDifferentAccountLogin = null },
            title = {
                Text(
                    text = if (language == AppLanguage.BN) "ভিন্ন অ্যাকাউন্ট সনাক্তকরণ" else "Different Account Detected",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (language == AppLanguage.BN)
                        "আপনি একটি ভিন্ন গুগল অ্যাকাউন্ট (${account.email}) দিয়ে লগইন করার চেষ্টা করছেন। এটি করলে আপনার ফোনে থাকা পূর্ববর্তী অ্যাকাউন্টের সকল লোকাল ডাটা মুছে যাবে এবং নতুন অ্যাকাউন্টের ক্লাউড ডাটা সিঙ্ক হবে। আপনি কি নিশ্চিত যে এগিয়ে যেতে চান?"
                        else "You are trying to log in with a different Google account (${account.email}). Doing this will delete all previous local data on your phone and sync the new account's cloud data. Are you sure you want to proceed?"
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = FintechRed),
                    onClick = {
                        val act = pendingDifferentAccountLogin
                        pendingDifferentAccountLogin = null
                        if (act != null) {
                            viewModel.clearAllDataLocal {
                                viewModel.handleGoogleSignInSuccess(
                                    context = context,
                                    account = act,
                                    onSuccess = {
                                        viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "গুগল ড্রাইভ কানেক্ট সফল হয়েছে!" else "Google Drive connected successfully!", isSuccess = true, type = "SUCCESS")
                                        viewModel.triggerCustomNotification(
                                            if (language == AppLanguage.BN) "আপনার নতুন গুগল অ্যাকাউন্টটি সফলভাবে সিঙ্ক করা হয়েছে।" else "Your new Google account has been successfully synced.",
                                            isSuccess = true,
                                            type = "SIGN_IN"
                                        )
                                    },
                                    onError = { err ->
                                        viewModel.triggerCustomNotification("${if (language == AppLanguage.BN) "কানেক্ট ব্যর্থ হয়েছে: " else "Connection failed: "}$err", isSuccess = false, type = "ERROR")
                                    }
                                )
                            }
                        }
                    }
                ) {
                    Text(if (language == AppLanguage.BN) "মুছে ফেলুন এবং কানেক্ট করুন" else "Clear and Connect", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDifferentAccountLogin = null }) {
                    Text(if (language == AppLanguage.BN) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Custom Notification Overlay Stack
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            customNotifications.reversed().forEach { notif ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value != SwipeToDismissBoxValue.Settled) {
                            viewModel.dismissCustomNotification(notif.id)
                            true
                        } else {
                            false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {},
                    content = {
                        CustomNotificationOverlay(
                            notification = notif,
                            language = language,
                            isDark = isDarkTheme
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
}


@Composable
fun getGreeting(language: AppLanguage): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return if (language == AppLanguage.BN) {
        when (hour) {
            in 5..11 -> "শুভ সকাল,"
            in 12..15 -> "শুভ দুপুর,"
            in 16..17 -> "শুভ বিকাল,"
            in 18..19 -> "শুভ সন্ধ্যা,"
            else -> "শুভ রাত্রি,"
        }
    } else {
        when (hour) {
            in 5..11 -> "Good Morning,"
            in 12..15 -> "Good Afternoon,"
            in 16..17 -> "Good Afternoon,"
            in 18..19 -> "Good Evening,"
            else -> "Good Night,"
        }
    }
}

data class BudgetAlertData(
    val title: String,
    val message: String,
    val isWarning: Boolean
)

private fun showLocalSystemNotification(context: Context, title: String, message: String, notificationId: Int) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
    if (notificationManager != null) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "finance_budget_alerts",
                "Finance Budget Alerts",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for monthly budget progress"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val builder = androidx.core.app.NotificationCompat.Builder(context, "finance_budget_alerts")
            .setSmallIcon(com.example.R.drawable.ic_pie_chart)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun DashboardScreen(
    language: AppLanguage,
    isDark: Boolean,
    profileName: String,
    profileEmail: String,
    profilePhotoUri: String?,
    balance: Double,
    income: Double,
    expense: Double,
    owedToMe: Double,
    iOwe: Double,
    recentTransactions: List<Transaction>,
    persons: List<Person>,
    onAddTransactionClick: () -> Unit,
    onAddPersonClick: () -> Unit,
    onAddSavingClick: () -> Unit,
    onDeleteTransaction: (Int) -> Unit,
    onDeleteTransactions: (List<Int>) -> Unit = {},
    onEditTransaction: (Transaction) -> Unit,
    onNavigate: (String, String) -> Unit,
    onWorkspaceClick: () -> Unit = {},
    timeFilter: String = "ALL",
    onTimeFilterChange: (String) -> Unit = {},
    isGoogleSignedIn: Boolean = false,
    onSignInClick: () -> Unit = {},
    onBackupClick: () -> Unit = {},
    viewModel: FinanceViewModel? = null,
    onPersonClick: ((Person) -> Unit)? = null,
    activeTab: String = "",
    onEditGradient: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val workspaces by viewModel?.workspaces?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val currentWorkspace by viewModel?.currentWorkspace?.collectAsState(initial = com.example.data.Workspace(id = "default", name = "ব্যক্তিগত")) ?: remember { mutableStateOf(com.example.data.Workspace(id = "default", name = "ব্যক্তিগত")) }
    val workspaceStatsList by viewModel?.workspaceStatsList?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    val savingsGoals by viewModel?.savingsGoals?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    var showBudgetDialogType by remember { mutableStateOf<String?>(null) }
    var showBudgetDetailsType by remember { mutableStateOf<String?>(null) }
    var activeAlertPopup by remember { mutableStateOf<BudgetAlertData?>(null) }
    val transactions by viewModel?.transactions?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
    var selectedTxIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedTxIds.isNotEmpty()

    androidx.activity.compose.BackHandler(enabled = isSelectionMode) {
        selectedTxIds = emptySet()
    }
    val incomeByCategory = remember(transactions, timeFilter) {
        val filtered = filterTransactionsByTime(transactions, timeFilter)
        // Include INCOME type AND LEND transactions that are of subtype "CREDIT" (Credit Sales)
        filtered.filter { it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT") }
            .groupBy { it.category }
            .map { Pair(it.key, it.value.sumOf { tx -> tx.amount }) }
            .sortedByDescending { it.second }
    }
    val expenseByCategory = remember(transactions, timeFilter) {
        val filtered = filterTransactionsByTime(transactions, timeFilter)
        // Include EXPENSE type AND BORROW transactions that are of subtype "CREDIT" (Credit Purchases)
        filtered.filter { it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT") }
            .groupBy { it.category }
            .map { Pair(it.key, it.value.sumOf { tx -> tx.amount }) }
            .sortedByDescending { it.second }
    }
    val monthlyBudgets by viewModel?.monthlyBudgets?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    val budgetGradients by viewModel?.budgetGradients?.collectAsState() ?: remember { mutableStateOf(emptyMap()) }

    val budgetIncomeAmount = remember(timeFilter, monthlyBudgets, currentWorkspace) {
        val ym = getYearMonthFromFilter(timeFilter)
        if (ym != null) {
            monthlyBudgets.find { it.year == ym.first && it.month == ym.second }?.income ?: 0.0
        } else if (timeFilter == "ALL") {
            currentWorkspace.budgetIncome
        } else 0.0
    }
    val budgetExpenseAmount = remember(timeFilter, monthlyBudgets, currentWorkspace) {
        val ym = getYearMonthFromFilter(timeFilter)
        if (ym != null) {
            monthlyBudgets.find { it.year == ym.first && it.month == ym.second }?.expense ?: 0.0
        } else if (timeFilter == "ALL") {
            currentWorkspace.budgetExpense
        } else 0.0
    }
    val budgetSavingsAmount = remember(timeFilter, monthlyBudgets, currentWorkspace) {
        val ym = getYearMonthFromFilter(timeFilter)
        if (ym != null) {
            monthlyBudgets.find { it.year == ym.first && it.month == ym.second }?.savings ?: 0.0
        } else if (timeFilter == "ALL") {
            currentWorkspace.budgetSavings
        } else 0.0
    }
    val savingsTransactions by viewModel?.savingsTransactions?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    var showBudgetHistoryDialog by remember { mutableStateOf(false) }
    val totalSavingsAmount = savingsGoals.sumOf { it.savedAmount }

    val localFilteredSavingsTransactions = remember(savingsTransactions, timeFilter) {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        when {
            timeFilter == "ALL" -> savingsTransactions
            timeFilter == "TODAY" -> {
                calendar.timeInMillis = now
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                savingsTransactions.filter { it.timestamp >= startOfDay }
            }
            timeFilter == "MONTH" -> {
                calendar.timeInMillis = now
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.timeInMillis
                savingsTransactions.filter { it.timestamp >= startOfMonth }
            }
            timeFilter.startsWith("CUSTOM_MONTH:") -> {
                val parts = timeFilter.substringAfter("CUSTOM_MONTH:").split("-")
                if (parts.size == 2) {
                    val year = parts[0].toIntOrNull() ?: 2026
                    val month = parts[1].toIntOrNull() ?: 1
                    calendar.clear()
                    calendar.set(java.util.Calendar.YEAR, year)
                    calendar.set(java.util.Calendar.MONTH, month - 1)
                    calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    calendar.set(java.util.Calendar.MINUTE, 0)
                    calendar.set(java.util.Calendar.SECOND, 0)
                    calendar.set(java.util.Calendar.MILLISECOND, 0)
                    val start = calendar.timeInMillis
                    calendar.add(java.util.Calendar.MONTH, 1)
                    val end = calendar.timeInMillis
                    savingsTransactions.filter { it.timestamp in start until end }
                } else savingsTransactions
            }
            timeFilter.startsWith("CUSTOM_DATE:") -> {
                val parts = timeFilter.substringAfter("CUSTOM_DATE:").split("-")
                if (parts.size == 3) {
                    val year = parts[0].toIntOrNull() ?: 2026
                    val month = parts[1].toIntOrNull() ?: 1
                    val day = parts[2].toIntOrNull() ?: 1
                    calendar.clear()
                    calendar.set(year, month - 1, day, 0, 0, 0)
                    calendar.set(java.util.Calendar.MILLISECOND, 0)
                    val start = calendar.timeInMillis
                    calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                    val end = calendar.timeInMillis
                    savingsTransactions.filter { it.timestamp in start until end }
                } else savingsTransactions
            }
            else -> savingsTransactions
        }
    }

    val dynamicSavingsAmount = remember(timeFilter, totalSavingsAmount, localFilteredSavingsTransactions) {
        if (timeFilter == "ALL") {
            totalSavingsAmount
        } else {
            localFilteredSavingsTransactions.filter { it.isDeposit }.sumOf { it.amount } - 
            localFilteredSavingsTransactions.filter { !it.isDeposit }.sumOf { it.amount }
        }
    }

    val savingsByGoal = remember(savingsGoals, localFilteredSavingsTransactions, timeFilter) {
        savingsGoals.map { goal ->
            val savedInPeriod = if (timeFilter == "ALL") {
                goal.savedAmount
            } else {
                val goalTx = localFilteredSavingsTransactions.filter { it.goalId == goal.id }
                goalTx.filter { it.isDeposit }.sumOf { it.amount } - 
                goalTx.filter { !it.isDeposit }.sumOf { it.amount }
            }
            Pair(goal.title, savedInPeriod)
        }
        .filter { it.second > 0.0 }
        .sortedByDescending { it.second }
    }

    val currentNetWorth = remember(balance, dynamicSavingsAmount, owedToMe, iOwe) {
        balance + dynamicSavingsAmount + owedToMe - iOwe
    }

    val effectiveIncome = income
    val effectiveExpense = expense
    val effectiveSavings = dynamicSavingsAmount
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            viewModel?.let { vm ->
                if (!vm.isNotificationEnabled.value) {
                    vm.toggleNotification(context)
                }
            }
        } else {
            // Permission denied, ensure notification is disabled if it was first launch attempt
            viewModel?.let { vm ->
                if (vm.isNotificationEnabled.value) {
                    vm.toggleNotification(context)
                }
            }
        }
    }

    val budgetPrefs = remember { context.getSharedPreferences("budget_alerts_prefs", Context.MODE_PRIVATE) }
    val currentYearMonth = remember(transactions) {
        val sdf = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US)
        sdf.format(java.util.Date())
    }
    val currentFilterKey = remember(timeFilter, currentYearMonth) {
        if (timeFilter == "ALL") "ALL" else currentYearMonth
    }

    var isAlertSystemReady by remember { mutableStateOf(false) }
    LaunchedEffect(currentWorkspace.id, timeFilter) {
        isAlertSystemReady = false
        delay(1000) // Wait 1 second for database flows to emit real values for the new workspace / filter
        
        // Initialize shown flags for already-exceeded thresholds so switching workspace or filter never triggers them
        val editor = budgetPrefs.edit()
        
        // Income Budget
        if (budgetIncomeAmount > 0.0) {
            val ratio = income / budgetIncomeAmount
            val key80 = "${currentWorkspace.id}_${currentFilterKey}_income_80_shown"
            val key100 = "${currentWorkspace.id}_${currentFilterKey}_income_100_shown"
            editor.putBoolean(key80, ratio >= 0.8)
            editor.putBoolean(key100, ratio >= 1.0)
        }
        
        // Expense Budget
        if (budgetExpenseAmount > 0.0) {
            val ratio = expense / budgetExpenseAmount
            val key80 = "${currentWorkspace.id}_${currentFilterKey}_expense_80_shown"
            val key100 = "${currentWorkspace.id}_${currentFilterKey}_expense_100_shown"
            editor.putBoolean(key80, ratio >= 0.8)
            editor.putBoolean(key100, ratio >= 1.0)
        }
        
        // Savings Budget
        if (budgetSavingsAmount > 0.0) {
            val ratio = effectiveSavings / budgetSavingsAmount
            val key80 = "${currentWorkspace.id}_${currentFilterKey}_savings_80_shown"
            val key100 = "${currentWorkspace.id}_${currentFilterKey}_savings_100_shown"
            editor.putBoolean(key80, ratio >= 0.8)
            editor.putBoolean(key100, ratio >= 1.0)
        }
        
        editor.apply()
        isAlertSystemReady = true
    }

    LaunchedEffect(
        income, expense, effectiveSavings,
        budgetIncomeAmount, budgetExpenseAmount, budgetSavingsAmount,
        currentFilterKey, isAlertSystemReady
    ) {
        if (!isAlertSystemReady) return@LaunchedEffect
        
        // Check Income
        if (budgetIncomeAmount > 0.0) {
            val key80 = "${currentWorkspace.id}_${currentFilterKey}_income_80_shown"
            val key100 = "${currentWorkspace.id}_${currentFilterKey}_income_100_shown"
            val shown80 = budgetPrefs.getBoolean(key80, false)
            val shown100 = budgetPrefs.getBoolean(key100, false)
            val ratio = income / budgetIncomeAmount
            val ratioPercent = (ratio * 100).toInt()
            
            if (ratio >= 1.0) {
                if (!shown100) {
                    val formattedPct = formatNumberString("$ratioPercent%", language)
                    val title = if (language == AppLanguage.BN) "লক্ষ্য পূরণ! 🏆" else "Goal Achieved! 🏆"
                    val msg = if (language == AppLanguage.BN) {
                        "অভিনন্দন! আপনি আপনার আয় বাজেটের $formattedPct (${formatCurrency(income, language)}) অর্জন করেছেন!"
                    } else {
                        "Congratulations! You have achieved $ratioPercent% of your Income Budget (${formatCurrency(income, language)})!"
                    }
                    showLocalSystemNotification(context, title, msg, 8001)
                    activeAlertPopup = BudgetAlertData(title, msg, isWarning = false)
                    budgetPrefs.edit().putBoolean(key100, true).putBoolean(key80, true).apply()
                }
            } else if (ratio >= 0.8) {
                if (!shown80) {
                    val formattedPct = formatNumberString("$ratioPercent%", language)
                    val title = if (language == AppLanguage.BN) "অভিনন্দন! 🎉" else "Congratulations! 🎉"
                    val msg = if (language == AppLanguage.BN) {
                        "আপনি আপনার আয় বাজেটের $formattedPct (${formatCurrency(income, language)}) অর্জন করেছেন!"
                    } else {
                        "You have achieved $ratioPercent% of your Income Budget (${formatCurrency(income, language)})!"
                    }
                    showLocalSystemNotification(context, title, msg, 8001)
                    activeAlertPopup = BudgetAlertData(title, msg, isWarning = false)
                    budgetPrefs.edit().putBoolean(key80, true).apply()
                }
                if (shown100) {
                    budgetPrefs.edit().putBoolean(key100, false).apply()
                }
            } else {
                if (shown80 || shown100) {
                    budgetPrefs.edit().putBoolean(key80, false).putBoolean(key100, false).apply()
                }
            }
        }

        // Check Expense
        if (budgetExpenseAmount > 0.0) {
            val key80 = "${currentWorkspace.id}_${currentFilterKey}_expense_80_shown"
            val key100 = "${currentWorkspace.id}_${currentFilterKey}_expense_100_shown"
            val shown80 = budgetPrefs.getBoolean(key80, false)
            val shown100 = budgetPrefs.getBoolean(key100, false)
            val ratio = expense / budgetExpenseAmount
            val ratioPercent = (ratio * 100).toInt()
            
            if (ratio >= 1.0) {
                if (!shown100) {
                    val formattedPct = formatNumberString("$ratioPercent%", language)
                    val title = if (language == AppLanguage.BN) "বাজেট অতিক্রম! 🚨" else "Budget Exceeded! 🚨"
                    val msg = if (language == AppLanguage.BN) {
                        "সতর্কতা! আপনার ব্যয় বাজেট ১০০% অতিক্রম করেছে (${formatCurrency(expense, language)})!"
                    } else {
                        "Warning! Your expense has exceeded 100% of your Expense Budget limit (${formatCurrency(expense, language)})!"
                    }
                    showLocalSystemNotification(context, title, msg, 8002)
                    activeAlertPopup = BudgetAlertData(title, msg, isWarning = true)
                    budgetPrefs.edit().putBoolean(key100, true).putBoolean(key80, true).apply()
                }
            } else if (ratio >= 0.8) {
                if (!shown80) {
                    val formattedPct = formatNumberString("$ratioPercent%", language)
                    val title = if (language == AppLanguage.BN) "সতর্কতা! ⚠️" else "Budget Warning! ⚠️"
                    val msg = if (language == AppLanguage.BN) {
                        "সাবধান! আপনার ব্যয় বাজেটের $formattedPct (${formatCurrency(expense, language)}) খরচ হয়ে গেছে!"
                    } else {
                        "Warning! You have spent $ratioPercent% of your Expense Budget limit (${formatCurrency(expense, language)})!"
                    }
                    showLocalSystemNotification(context, title, msg, 8002)
                    activeAlertPopup = BudgetAlertData(title, msg, isWarning = true)
                    budgetPrefs.edit().putBoolean(key80, true).apply()
                }
                if (shown100) {
                    budgetPrefs.edit().putBoolean(key100, false).apply()
                }
            } else {
                if (shown80 || shown100) {
                    budgetPrefs.edit().putBoolean(key80, false).putBoolean(key100, false).apply()
                }
            }
        }

        // Check Savings
        if (budgetSavingsAmount > 0.0) {
            val key80 = "${currentWorkspace.id}_${currentFilterKey}_savings_80_shown"
            val key100 = "${currentWorkspace.id}_${currentFilterKey}_savings_100_shown"
            val shown80 = budgetPrefs.getBoolean(key80, false)
            val shown100 = budgetPrefs.getBoolean(key100, false)
            val ratio = effectiveSavings / budgetSavingsAmount
            val ratioPercent = (ratio * 100).toInt()
            
            if (ratio >= 1.0) {
                if (!shown100) {
                    val formattedPct = formatNumberString("$ratioPercent%", language)
                    val title = if (language == AppLanguage.BN) "লক্ষ্য পূরণ! 🎯" else "Savings Goal Met! 🎯"
                    val msg = if (language == AppLanguage.BN) {
                        "চমত্কার! আপনি আপনার সঞ্চয় লক্ষ্যের $formattedPct (${formatCurrency(effectiveSavings, language)}) পূরণ করেছেন!"
                    } else {
                        "Fantastic! You have fulfilled $ratioPercent% of your Savings Goal (${formatCurrency(effectiveSavings, language)})!"
                    }
                    showLocalSystemNotification(context, title, msg, 8003)
                    activeAlertPopup = BudgetAlertData(title, msg, isWarning = false)
                    budgetPrefs.edit().putBoolean(key100, true).putBoolean(key80, true).apply()
                }
            } else if (ratio >= 0.8) {
                if (!shown80) {
                    val formattedPct = formatNumberString("$ratioPercent%", language)
                    val title = if (language == AppLanguage.BN) "দুর্দান্ত অর্জন! 🎯" else "Great Achievement! 🎯"
                    val msg = if (language == AppLanguage.BN) {
                        "অসাধারণ! আপনি আপনার সঞ্চয় লক্ষ্যের $formattedPct (${formatCurrency(effectiveSavings, language)}) পূরণ করেছেন!"
                    } else {
                        "Amazing! You have fulfilled $ratioPercent% of your Savings Goal (${formatCurrency(effectiveSavings, language)})!"
                    }
                    showLocalSystemNotification(context, title, msg, 8003)
                    activeAlertPopup = BudgetAlertData(title, msg, isWarning = false)
                    budgetPrefs.edit().putBoolean(key80, true).apply()
                }
                if (shown100) {
                    budgetPrefs.edit().putBoolean(key100, false).apply()
                }
            } else {
                if (shown80 || shown100) {
                    budgetPrefs.edit().putBoolean(key80, false).putBoolean(key100, false).apply()
                }
            }
        }
    }

    if (activeAlertPopup != null) {
        val alert = activeAlertPopup!!
        AlertDialog(
            onDismissRequest = { activeAlertPopup = null },
            icon = {
                Icon(
                    imageVector = if (alert.isWarning) Icons.Rounded.Warning else Icons.Rounded.Savings,
                    contentDescription = null,
                    tint = if (alert.isWarning) Color(0xFFEF4444) else Color(0xFF10B981),
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = alert.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (alert.isWarning) Color(0xFFEF4444) else Color(0xFF10B981)
                )
            },
            text = {
                Text(
                    text = alert.message,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.DarkGray
                )
            },
            confirmButton = {
                Button(
                    onClick = { activeAlertPopup = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (alert.isWarning) Color(0xFFEF4444) else Color(0xFF10B981)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "ঠিক আছে" else "OK",
                        color = Color.White
                    )
                }
            },
            modifier = Modifier.testTag("budget_threshold_alert_dialog")
        )
    }

    if (showBudgetHistoryDialog) {
        BudgetHistoryDialog(
            language = language,
            isDark = isDark,
            transactions = transactions,
            savingsTransactions = savingsTransactions,
            budgetIncome = budgetIncomeAmount,
            budgetExpense = budgetExpenseAmount,
            budgetSavings = budgetSavingsAmount,
            onDismiss = { showBudgetHistoryDialog = false }
        )
    }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
        val hasAskedNotif = prefs.getBoolean("has_asked_notification_permission", false)
        
        if (!hasAskedNotif && Build.VERSION.SDK_INT >= 33) {
            prefs.edit().putBoolean("has_asked_notification_permission", true).apply()
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else if (!hasAskedNotif) {
            // Below Android 13, permission is granted by default, but we should still track that we "asked" or initialized
            prefs.edit().putBoolean("has_asked_notification_permission", true).apply()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Multi-Select Header Toolbar
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelectionMode,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FintechBlue)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedTxIds = emptySet() }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Text(
                            text = "${selectedTxIds.size} ${if (language == AppLanguage.BN) "টি নির্বাচিত" else "selected"}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    
                    TextButton(
                        onClick = {
                            if (selectedTxIds.size == recentTransactions.size) {
                                selectedTxIds = emptySet()
                            } else {
                                selectedTxIds = recentTransactions.map { it.id }.toSet()
                            }
                        }
                    ) {
                        Text(
                            text = if (selectedTxIds.size == recentTransactions.size) 
                                (if (language == AppLanguage.BN) "সব আনমার্ক" else "Deselect All")
                            else (if (language == AppLanguage.BN) "সব মার্ক" else "Select All"),
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
            ) {
        // Balance Card (Fintech Gradient Card with sleek styling and beautifully integrated debts/loans cards)
        item {
            FintechGradientCard(
                gradientColors = if (isDark) listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E)) else activeThemeGradient, // Sleek Indigo-Violet-Fuchsia Gradient
                cornerRadius = 32.dp,
                padding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
                modifier = Modifier.testTag("dashboard_balance_card")
            ) {
                // Total Balance Header Row (Full-width)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Translation.get("total_balance", language),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "(${getFormattedPeriodText(timeFilter, language)})",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Main Balance Row with Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formatCurrency(balance, language),
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            modifier = Modifier
                                .padding(vertical = 1.dp)
                                .basicMarquee()
                        )
                        
                        // Net Worth Display
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (language == AppLanguage.BN) "নীট সম্পদ: " else "Net Worth: ",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatCurrency(currentNetWorth, language),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Bottom-right decorative card chip icon with matching border and pulsing scale applied to both container and image
                    Box(
                        modifier = Modifier
                            .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                            .clickable { onNavigate("charts", "") },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_custom_pie_chart),
                            contentDescription = "Charts",
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 1: Income and Expense Sub-Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Income Sub-Card
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                            .clickable { onNavigate("transactions", "INCOME") }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BN) "আয়" else "Income",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatCurrency(income, language),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }

                    // Expense Sub-Card
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(30.dp))
                            .clickable { onNavigate("transactions", "EXPENSE") }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BN) "ব্যয়" else "Expense",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatCurrency(expense, language),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            modifier = Modifier.basicMarquee()
                        )
                    }
                }
            }
        }

        // Time Filter Row
        item {
            TimeFilterRow(
                timeFilter = timeFilter,
                language = language,
                onTimeFilterChange = onTimeFilterChange,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        // I Owe & Owed to Me Cards (দেনা ও পাওনা)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // I Owe Card (দেনা)
                FintechGradientCard(
                    gradientColors = if (isDark) listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E)) else activeThemeGradient,
                    cornerRadius = 24.dp,
                    padding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(82.dp)
                        .clickable { onNavigate("debts", "DENA") }
                        .testTag("dashboard_i_owe_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                            Text(
                                text = Translation.get("i_owe", language),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                            Text(
                                text = formatCurrency(iOwe, language),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .basicMarquee()
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Owed to Me Card (পাওনা)
                FintechGradientCard(
                    gradientColors = if (isDark) listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E)) else activeThemeGradient,
                    cornerRadius = 24.dp,
                    padding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(82.dp)
                        .clickable { onNavigate("debts", "PAWN") }
                        .testTag("dashboard_owed_to_me_card")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                            Text(
                                text = Translation.get("owed_to_me", language),
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee()
                            )
                            Text(
                                text = formatCurrency(owedToMe, language),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .basicMarquee()
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Monthly Budget Control Card (সাদা রং এর)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1C1C1E) else Color.White),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("budget_control_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                timeFilter == "ALL" -> if (language == AppLanguage.BN) "সর্বমোট বাজেট কন্ট্রোল" else "Total Budget Control"
                                else -> if (language == AppLanguage.BN) "মাসিক বাজেট কন্ট্রোল" else "Monthly Budget Control"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = FintechBlue,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showBudgetHistoryDialog = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Rounded.History,
                                contentDescription = null,
                                tint = FintechBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (language == AppLanguage.BN) "ইতিহাস" else "History",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FintechBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Income Donut Chart
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = { showBudgetDetailsType = "INCOME" },
                                    onLongClick = { onEditGradient?.invoke("INCOME") }
                                )
                                .padding(top = 0.dp, bottom = 4.dp, start = 2.dp, end = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (language == AppLanguage.BN) "আয়" else "Income",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF4CAF50) else Color(0xFF2E7D32), // Green
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            BudgetControlDonutChart(
                                targetAmount = budgetIncomeAmount,
                                totalFilledAmount = effectiveIncome,
                                categoryType = "INCOME",
                                language = language,
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(0.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,
                                centerColorOverride = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF8F9FA),
                                customGradient = budgetGradients["INCOME"],
                                onCenterClick = { showBudgetDetailsType = "INCOME" },
                                onLongPress = { onEditGradient?.invoke("INCOME") }
                            )
                        }

                        // 2. Expense Donut Chart
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = { showBudgetDetailsType = "EXPENSE" },
                                    onLongClick = { onEditGradient?.invoke("EXPENSE") }
                                )
                                .padding(top = 0.dp, bottom = 4.dp, start = 2.dp, end = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (language == AppLanguage.BN) "ব্যয়" else "Expense",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFFFF9800) else Color(0xFFE65100), // Dark Orange
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            BudgetControlDonutChart(
                                targetAmount = budgetExpenseAmount,
                                totalFilledAmount = effectiveExpense,
                                categoryType = "EXPENSE",
                                language = language,
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(0.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,
                                centerColorOverride = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF8F9FA),
                                customGradient = budgetGradients["EXPENSE"],
                                onCenterClick = { showBudgetDetailsType = "EXPENSE" },
                                onLongPress = { onEditGradient?.invoke("EXPENSE") }
                            )
                        }

                        // 3. Savings Donut Chart
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = { showBudgetDetailsType = "SAVINGS" },
                                    onLongClick = { onEditGradient?.invoke("SAVINGS") }
                                )
                                .padding(top = 0.dp, bottom = 4.dp, start = 2.dp, end = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (language == AppLanguage.BN) "সঞ্চয়" else "Savings",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF64B5F6) else Color(0xFF0D47A1), // Deep Blue
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            BudgetControlDonutChart(
                                targetAmount = budgetSavingsAmount,
                                totalFilledAmount = effectiveSavings,
                                categoryType = "SAVINGS",
                                language = language,
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(0.dp),
                                strokeWidthDp = 14.dp,
                                centerTextSize = 14.sp,
                                centerColorOverride = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF8F9FA),
                                customGradient = budgetGradients["SAVINGS"],
                                onCenterClick = { showBudgetDetailsType = "SAVINGS" },
                                onLongPress = { onEditGradient?.invoke("SAVINGS") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "বিস্তারিত দেখতে চার্টের ওপর ক্লিক করুন" else "Click on charts to view details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = FintechBlue,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Recent Transactions Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Translation.get("recent_tx", language),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.DarkGray
                )
                TextButton(
                    onClick = { onNavigate("transactions", "ALL") },
                    modifier = Modifier.testTag("see_all_transactions_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "সব দেখুন" else "See All",
                            color = FintechBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "See All",
                            tint = FintechBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(0.dp))
        }

        if (recentTransactions.isEmpty()) {
            item {
                val bgColor by androidx.compose.animation.animateColorAsState(if (isDark) Color(0xFF121212) else Color.White)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = bgColor),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(64.dp)
                                .padding(bottom = 16.dp)
                        )
                        Text(
                            text = Translation.get("no_tx", language),
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            val grouped = recentTransactions.sortedByDescending { it.timestamp }.groupBy { formatDateToDay(it.timestamp) }
            grouped.forEach { (date, txs) ->
                item {
                    Text(
                        text = formatDateHeader(date, language),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 0.dp, bottom = 4.dp)
                    )
                }
                items(txs, key = { it.id }) { tx ->
                    val isSelected = selectedTxIds.contains(tx.id)
                    TransactionRowItem(
                        tx = tx,
                        language = language,
                        isDark = isDark,
                        persons = persons,
                        onDelete = onDeleteTransaction,
                        onEdit = onEditTransaction,
                        onNavigateToTab = onNavigate,
                        onPersonClick = onPersonClick,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        onLongClick = {
                            selectedTxIds = if (isSelected) selectedTxIds - tx.id else selectedTxIds + tx.id
                        },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        // Buffer space at bottom to stay above navbar
        item {
            Spacer(modifier = Modifier.height(110.dp))
        }
    }
            } // Close Column

            // Bulk Actions Floating Buttons
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelectionMode,
                enter = androidx.compose.animation.scaleIn() + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 113.dp)
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDark) Color(0xFF2C2C2E) else Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .border(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Delete Button
                    Button(
                        onClick = {
                            onDeleteTransactions(selectedTxIds.toList())
                            selectedTxIds = emptySet()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FintechRed),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.BN) "মুছে ফেলুন" else "Delete",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } // Close Box



    if (showBudgetDetailsType != null) {
        val categoryType = showBudgetDetailsType!!
        val targetAmount = when (categoryType) {
            "INCOME" -> budgetIncomeAmount
            "EXPENSE" -> budgetExpenseAmount
            "SAVINGS" -> budgetSavingsAmount
            else -> 0.0
        }
        val totalFilledAmount = when (categoryType) {
            "INCOME" -> effectiveIncome
            "EXPENSE" -> effectiveExpense
            "SAVINGS" -> effectiveSavings
            else -> 0.0
        }
        val segments = when (categoryType) {
            "INCOME" -> incomeByCategory
            "EXPENSE" -> expenseByCategory
            "SAVINGS" -> savingsByGoal
            else -> emptyList()
        }
        val titleText = when (categoryType) {
            "INCOME" -> if (language == AppLanguage.BN) "আয় বাজেটের বিস্তারিত" else "Income Budget Details"
            "EXPENSE" -> if (language == AppLanguage.BN) "ব্যয় বাজেটের বিস্তারিত" else "Expense Budget Details"
            "SAVINGS" -> if (language == AppLanguage.BN) "সঞ্চয় লক্ষ্যের বিস্তারিত" else "Savings Goal Details"
            else -> ""
        }
        
        val subtitleText = when (categoryType) {
            "INCOME" -> if (language == AppLanguage.BN) "আয় খাতের অবদান ও অর্জনের অগ্রগতির চিত্র" else "Contribution breakdown and achieved income budget progress."
            "EXPENSE" -> if (language == AppLanguage.BN) "ব্যয় খাতের অবদান ও বাজেটের অগ্রগতির চিত্র" else "Contribution breakdown and spent expense budget progress."
            "SAVINGS" -> if (language == AppLanguage.BN) "সঞ্চয় লক্ষ্যভিত্তিক জমার অগ্রগতির চিত্র" else "Contribution breakdown and saved savings goal progress."
            else -> ""
        }

        var isEditingBudget by remember(targetAmount) { mutableStateOf(targetAmount == 0.0) }
        var localBudgetInput by remember(targetAmount) { mutableStateOf(if (targetAmount > 0.0) targetAmount.toInt().toString() else "") }
        var showInplaceBudgetCalculator by remember { mutableStateOf(false) }

        val colors = budgetGradients["BUDGET_DETAILS_$categoryType"] ?: getBudgetCategoryColors(categoryType)

        AlertDialog(
            onDismissRequest = { showBudgetDetailsType = null },
            modifier = Modifier.testTag("budget_progress_details_dialog"),
            title = {
                Text(
                    text = titleText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FintechBlue
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = subtitleText,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Large Segmented Donut Chart in a card with matching styling
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1C1C1E) else Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CategorySegmentedDonutChart(
                                targetAmount = targetAmount,
                                totalFilledAmount = totalFilledAmount,
                                segments = segments,
                                isDark = isDark,
                                language = language,
                                modifier = Modifier.size(160.dp),
                                strokeWidthDp = 22.dp, // Match budget section with increased thickness
                                centerTextSize = 22.sp,
                                categoryType = categoryType,
                                centerColorOverride = if (isDark) Color(0xFF2C2C2E) else Color(0xFFF8F9FA), // Match budget section
                                customPalette = budgetGradients["BUDGET_DETAILS_$categoryType"],
                                onCenterClick = {
                                    if (targetAmount == 0.0) {
                                        isEditingBudget = true
                                    }
                                },
                                onLongPress = {
                                    onEditGradient?.invoke("BUDGET_DETAILS_$categoryType")
                                }
                            )
                            if (targetAmount > 0.0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "${if (language == AppLanguage.BN) "মোট: " else "Total: "}${formatCurrency(totalFilledAmount, language)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (categoryType) {
                                        "INCOME" -> Color(0xFF10B981)
                                        "EXPENSE" -> Color(0xFFE65100)
                                        "SAVINGS" -> FintechBlue
                                        else -> FintechBlue
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // In-place Budget Edit card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color.White.copy(alpha = 0.04f) else Color(0xFFF1F5F9)
                        ),
                        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.06f) else Color.LightGray.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val isSumAllTime = timeFilter == "ALL" && when(categoryType) {
                                "INCOME" -> currentWorkspace.budgetIncome == 0.0
                                "EXPENSE" -> currentWorkspace.budgetExpense == 0.0
                                "SAVINGS" -> currentWorkspace.budgetSavings == 0.0
                                else -> false
                            }
                            val ym = getYearMonthFromFilter(timeFilter)

                            if (!isEditingBudget) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (targetAmount == 0.0) {
                                                isEditingBudget = true
                                            }
                                        },
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (language == AppLanguage.BN) "বাজেট সীমা নির্ধারণ করুন" else "Set Budget Limit",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            text = if (targetAmount > 0.0) formatCurrency(targetAmount, language) else (if (language == AppLanguage.BN) "সেট করুন" else "Set Budget"),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (targetAmount > 0.0) FintechBlue else Color.Gray
                                        )
                                    }
                                    IconButton(
                                        onClick = { isEditingBudget = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Edit,
                                            contentDescription = "Edit Budget",
                                            tint = FintechBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            } else {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = localBudgetInput,
                                        onValueChange = { input -> 
                                            if (input.all { it.isDigit() || it == '.' }) localBudgetInput = input
                                        },
                                        label = {
                                            Text(
                                                text = if (language == AppLanguage.BN) "বাজেটের পরিমাণ (টাকা)" else "Budget Amount (BDT)",
                                                fontSize = 12.sp
                                            )
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        singleLine = true,
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                        ),
                                        trailingIcon = {
                                            IconButton(onClick = { showInplaceBudgetCalculator = true }) {
                                                Icon(
                                                    imageVector = androidx.compose.material.icons.Icons.Rounded.Calculate,
                                                    contentDescription = "Calculator",
                                                    tint = FintechBlue
                                                )
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = FintechBlue,
                                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.4f),
                                            focusedLabelColor = FintechBlue
                                        ),
                                        modifier = Modifier.fillMaxWidth().testTag("inplace_budget_input")
                                    )
                                    if (showInplaceBudgetCalculator) {
                                        CalculatorDialog(
                                            language = language,
                                            isDark = isDark,
                                            onDismiss = { showInplaceBudgetCalculator = false },
                                            onInsert = { localBudgetInput = it }
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { isEditingBudget = false }
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.BN) "বাতিল" else "Cancel",
                                                color = Color.Gray
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = {
                                                val newAmount = localBudgetInput.toDoubleOrNull() ?: 0.0
                                                if (ym != null) {
                                                    when (categoryType) {
                                                        "INCOME" -> viewModel?.setMonthlyBudget(ym.first, ym.second, newAmount, null, null)
                                                        "EXPENSE" -> viewModel?.setMonthlyBudget(ym.first, ym.second, null, newAmount, null)
                                                        "SAVINGS" -> viewModel?.setMonthlyBudget(ym.first, ym.second, null, null, newAmount)
                                                    }
                                                } else if (timeFilter == "ALL") {
                                                    when (categoryType) {
                                                        "INCOME" -> viewModel?.setBudgetIncome(newAmount)
                                                        "EXPENSE" -> viewModel?.setBudgetExpense(newAmount)
                                                        "SAVINGS" -> viewModel?.setBudgetSavings(newAmount)
                                                    }
                                                }
                                                isEditingBudget = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.BN) "সংরক্ষণ" else "Save",
                                                color = Color.White,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Detail category progress percentages
                    if (segments.isEmpty()) {
                        Text(
                            text = if (language == AppLanguage.BN) "কোন তথ্য নেই" else "No data available",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            segments.filter { it.second > 0.0 }.forEach { segment ->
                                val originalIndex = segments.indexOf(segment)
                                val name = segment.first
                                val amount = segment.second
                                val color = colors[originalIndex % colors.size]
                                
                                val percentOfTotal = if (totalFilledAmount > 0.0) {
                                    (amount / totalFilledAmount) * 100
                                } else {
                                    0.0
                                }

                                val percentOfBudget = if (targetAmount > 0.0) {
                                    (amount / targetAmount) * 100
                                } else {
                                    0.0
                                }

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDark) Color.White.copy(alpha = 0.04f) else Color(0xFFF8FAFC)
                                    ),
                                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.06f) else Color.LightGray.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .background(color, CircleShape)
                                                        .border(1.5.dp, Color.White, CircleShape)
                                                )
                                                Text(
                                                    text = name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDark) Color.White else Color.Black
                                                )
                                            }
                                            Text(
                                                text = formatCurrency(amount, language),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FintechBlue
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.BN) {
                                                    "মোট অর্জনের: ${formatNumberString("%.1f".format(percentOfTotal), language)}%"
                                                } else {
                                                    "Of total: %.1f%%".format(percentOfTotal)
                                                },
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                            if (targetAmount > 0.0) {
                                                Text(
                                                    text = if (language == AppLanguage.BN) {
                                                        "বাজেট সীমার: ${formatNumberString("%.1f".format(percentOfBudget), language)}%"
                                                    } else {
                                                        "Of budget: %.1f%%".format(percentOfBudget)
                                                    },
                                                    fontSize = 12.sp,
                                                    color = Color.Gray,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showBudgetDetailsType = null },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "ঠিক আছে" else "Close",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    if (showBudgetDialogType != null) {
        val category = showBudgetDialogType!!
        val currentLimit = when (category) {
            "INCOME" -> budgetIncomeAmount
            "EXPENSE" -> budgetExpenseAmount
            "SAVINGS" -> budgetSavingsAmount
            else -> 0.0
        }
        val currentValue = when (category) {
            "INCOME" -> income
            "EXPENSE" -> expense
            "SAVINGS" -> effectiveSavings
            else -> 0.0
        }
        val title = when (category) {
            "INCOME" -> if (language == AppLanguage.BN) "আয় বাজেট" else "Income Budget"
            "EXPENSE" -> if (language == AppLanguage.BN) "ব্যয় বাজেট" else "Expense Budget"
            "SAVINGS" -> if (language == AppLanguage.BN) "সঞ্চয় লক্ষ্য" else "Savings Goal"
            else -> ""
        }

        var tempAmount by remember { mutableStateOf(if (currentLimit > 0) currentLimit.toInt().toString() else "") }
        var showTempAmountCalculator by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showBudgetDialogType = null },
            modifier = Modifier.testTag("budget_setting_dialog"),
            title = {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FintechBlue
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) 
                            "বর্তমান খাতের বিস্তারিত ও বাজেট সেট করুন। বাজেট সেট করা থাকলে তা প্রগ্রেস ট্র্যাক করতে সাহায্য করবে।"
                            else "Set your monthly budget for this category. Setting a budget helps track your financial goals.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF1F5F9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (language == AppLanguage.BN) "বর্তমান অর্জন: " else "Current Status: ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray
                            )
                            Text(
                                text = formatCurrency(currentValue, language),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FintechBlue,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            if (currentLimit > 0) {
                                Text(
                                    text = if (language == AppLanguage.BN) 
                                        "বাজেট লক্ষ্য: ${formatCurrency(currentLimit, language)}" 
                                        else "Budget Limit: ${formatCurrency(currentLimit, language)}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = tempAmount,
                        onValueChange = { input -> 
                            if (input.all { it.isDigit() }) tempAmount = input
                        },
                        label = {
                            Text(
                                text = if (language == AppLanguage.BN) "বাজেটের পরিমাণ (টাকা)" else "Budget Amount (BDT)",
                                fontSize = 13.sp
                            )
                        },
                        placeholder = {
                            Text(
                                text = "e.g. 5000",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showTempAmountCalculator = true }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Rounded.Calculate,
                                    contentDescription = "Calculator",
                                    tint = FintechBlue
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FintechBlue,
                            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.4f),
                            focusedLabelColor = FintechBlue
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("budget_input_field")
                    )
                    if (showTempAmountCalculator) {
                        CalculatorDialog(
                            language = language,
                            isDark = isDark,
                            onDismiss = { showTempAmountCalculator = false },
                            onInsert = { tempAmount = it }
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentLimit > 0) {
                        TextButton(
                            onClick = {
                                when (category) {
                                    "INCOME" -> viewModel?.setBudgetIncome(0.0)
                                    "EXPENSE" -> viewModel?.setBudgetExpense(0.0)
                                    "SAVINGS" -> viewModel?.setBudgetSavings(0.0)
                                }
                                showBudgetDialogType = null
                            }
                        ) {
                            Text(
                                text = if (language == AppLanguage.BN) "মুছে ফেলুন" else "Clear",
                                color = FintechRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val finalVal = tempAmount.toDoubleOrNull() ?: 0.0
                            when (category) {
                                "INCOME" -> viewModel?.setBudgetIncome(finalVal)
                                "EXPENSE" -> viewModel?.setBudgetExpense(finalVal)
                                "SAVINGS" -> viewModel?.setBudgetSavings(finalVal)
                            }
                            showBudgetDialogType = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "সংরক্ষণ" else "Save",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetDialogType = null }) {
                    Text(
                        text = if (language == AppLanguage.BN) "বাতিল" else "Cancel",
                        color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray
                    )
                }
            }
        )
    }
}

// ---------------- TRANSACTION ROW COMPONENT ----------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionRowItem(
    tx: Transaction,
    language: AppLanguage,
    isDark: Boolean,
    persons: List<Person>,
    onDelete: (Int) -> Unit,
    onEdit: (Transaction) -> Unit,
    isHighlighted: Boolean = false,
    onNavigateToTab: ((String, String) -> Unit)? = null,
    onPersonClick: ((Person) -> Unit)? = null,
    searchQuery: String = "",
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDetails by remember { mutableStateOf(false) }
    val linkedPerson = persons.find { it.id == tx.personId }
    val context = LocalContext.current

    if (showDeleteConfirm) {
        DeleteVerificationDialog(
            language = language,
            onConfirm = {
                onDelete(tx.id)
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    if (showDetails) {
        TransactionDetailsDialog(
            tx = tx,
            language = language,
            isDark = isDark,
            linkedPerson = linkedPerson,
            onDismiss = { showDetails = false },
            onDelete = {
                showDetails = false
                showDeleteConfirm = true
            },
            onEdit = {
                showDetails = false
                onEdit(tx)
            },
            onShareText = {
                val shareText = buildString {
                    append(if (language == AppLanguage.BN) "মানি রসিদ\n" else "Money Receipt\n")
                    append("-------------------\n")
                    append(if (language == AppLanguage.BN) "ধরণ: " else "Type: ").append(tx.type).append("\n")
                    append(if (language == AppLanguage.BN) "পরিমাণ: " else "Amount: ").append(formatCurrency(tx.amount, language)).append("\n")
                    append(if (language == AppLanguage.BN) "ক্যাটাগরি: " else "Category: ").append(tx.category).append("\n")
                    if (linkedPerson != null) {
                        append(if (language == AppLanguage.BN) "ব্যক্তি: " else "Person: ").append(linkedPerson.name).append("\n")
                    }
                    if (tx.note.isNotEmpty()) {
                        append(if (language == AppLanguage.BN) "নোট: " else "Note: ").append(tx.note).append("\n")
                    }
                    append(if (language == AppLanguage.BN) "তারিখ: " else "Date: ").append(formatDate(tx.timestamp, language)).append("\n")
                    append("-------------------\n")
                    append(if (language == AppLanguage.BN) "ফাইন্যান্স নোট থেকে শেয়ার করা হয়েছে" else "Shared via Finance Note")
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, if (language == AppLanguage.BN) "শেয়ার করুন" else "Share via"))
            },
            onShareImage = { bitmap ->
                shareBitmap(context, bitmap, if (language == AppLanguage.BN) "ফাইন্যান্স নোট থেকে ট্রানজ্যাকশন মেমো" else "Transaction Memo from Finance Note")
            },
            onNavigateToTab = onNavigateToTab,
            onPersonClick = onPersonClick
        )
    }

    val bgColor by androidx.compose.animation.animateColorAsState(if (isHighlighted) (if (isDark) Color(0xFF453A1E) else Color(0xFFFEF3C7)) else (if (isDark) Color(0xFF121212) else Color.White))
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(if (isHighlighted || isSelected) 2.dp else 1.dp, if (isHighlighted) Color(0xFFFBBF24) else if (isSelected) FintechBlue else (if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onLongClick()
                    } else {
                        showDetails = true
                    }
                },
                onLongClick = onLongClick
            )
            .testTag("tx_item_${tx.id}")
    ) {
        // Category Icon and Color as requested:
        // INCOME: green (FintechGreen)
        // EXPENSE: red (FintechRed)
        // BORROW: orange (0xFFF97316)
        // LEND: yellow (0xFFF59E0B)
        // REPAY_PAID: purple (0xFF8B5CF6)
        // REPAY_RECEIVED: blue (0xFF3B82F6)
        val iconColor = when (tx.type) {
            "INCOME" -> Color(0xFF0D9488) // Teal
            "EXPENSE" -> FintechRed
            "LEND" -> Color(0xFF0D9488) // Teal
            "BORROW" -> Color(0xFFF97316)
            "REPAY_RECEIVED" -> Color(0xFF0D9488) // Teal
            "REPAY_PAID" -> Color(0xFF8B5CF6)
            else -> Color.Gray
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) FintechBlue else Color.Gray.copy(alpha = 0.2f))
                            .border(2.dp, if (isSelected) FintechBlue else Color.Gray.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    when (tx.type) {
                        "INCOME" -> Icon(Icons.AutoMirrored.Rounded.TrendingUp, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                        "EXPENSE" -> Icon(Icons.AutoMirrored.Rounded.TrendingDown, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                        "LEND", "BORROW" -> Icon(painterResource(id = R.drawable.ic_lend_borrow_new), contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                        "REPAY_RECEIVED" -> Icon(painterResource(id = R.drawable.ic_repay_received), contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                        "REPAY_PAID" -> Icon(painterResource(id = R.drawable.ic_repay_paid), contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                        else -> Icon(Icons.AutoMirrored.Rounded.CompareArrows, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    val formattedCategory = if (language == AppLanguage.BN) {
                        when (tx.category) {
                            "Salary" -> "বেতন"
                            "Business" -> "ব্যবসা"
                            "Agriculture" -> "কৃষি"
                            "Gift" -> "উপহার"
                            "Sales" -> "বিক্রয়"
                            "Honorarium" -> "সম্মানী"
                            "Food" -> "খাবার"
                            "Housing" -> "বাসস্থান"
                            "Transport" -> "যাতায়াত"
                            "Shopping" -> "কেনাকাটা"
                            "Medical" -> "চিকিৎসা"
                            "Education" -> "শিক্ষা"
                            "Clothing" -> "পোশাক"
                            "Others" -> "অন্যান্য"
                            "Lending" -> "ধার দেওয়া"
                            "Borrowing" -> "ধার নেওয়া"
                            "Repay Paid" -> Translation.get("debt_repaid", language)
                            "Repay Received" -> Translation.get("pawn_repaid", language)
                            else -> tx.category
                        }
                    } else {
                        tx.category
                    }

                    val titleText = run {
                        val typeSuffix = when (tx.type) {
                            "LEND" -> if (language == AppLanguage.BN) "পাওনা" else "Receivable"
                            "BORROW" -> if (language == AppLanguage.BN) "দেনা" else "Payable"
                            "REPAY_PAID" -> if (language == AppLanguage.BN) "দেনা পরিশোধ" else "Debt Repaid"
                            "REPAY_RECEIVED" -> if (language == AppLanguage.BN) "পাওনা পরিশোধ" else "Loan Repaid"
                            "INCOME" -> if (language == AppLanguage.BN) "আয়" else "Income"
                            "EXPENSE" -> if (language == AppLanguage.BN) "ব্যয়" else "Expense"
                            else -> ""
                        }
                        val baseName = if (tx.type in listOf("LEND", "BORROW", "REPAY_PAID", "REPAY_RECEIVED") && linkedPerson != null) {
                            linkedPerson.name
                        } else {
                            if (linkedPerson != null) "$formattedCategory (${linkedPerson.name})" else formattedCategory
                        }
                        if (typeSuffix.isNotEmpty()) {
                            "$baseName ($typeSuffix)"
                        } else {
                            baseName
                        }
                    }

                    HighlightedText(
                        text = titleText,
                        query = searchQuery,
                        color = MaterialTheme.colorScheme.primary,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E222F)
                        )
                    )
                    val creditLabel = if (tx.subType == "CREDIT") {
                        if (tx.type == "LEND") (if (language == AppLanguage.BN) "(বাকি বিক্রয়)" else "(Credit Sale)")
                        else if (tx.type == "BORROW") (if (language == AppLanguage.BN) "(বাকি ক্রয়)" else "(Credit Purchase)")
                        else ""
                    } else ""
                    
                    val descriptionText = if (tx.note.isNotEmpty()) {
                        if (creditLabel.isNotEmpty()) "${tx.note} $creditLabel" else tx.note
                    } else creditLabel

                    if (descriptionText.isNotEmpty()) {
                        HighlightedText(
                            text = descriptionText,
                            query = searchQuery,
                            color = MaterialTheme.colorScheme.primary,
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        )
                    }
                    Text(
                        text = formatDate(tx.timestamp, language),
                        fontSize = 10.sp,
                        color = Color.Gray.copy(alpha = 0.8f)
                    )
                }
            }

            // Amount
            val amountPrefix = when (tx.type) {
                "INCOME", "BORROW", "REPAY_RECEIVED" -> "+"
                else -> "-"
            }
            val amountColor = iconColor

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$amountPrefix${formatCurrency(tx.amount, language)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = Color.Gray.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
            }
        }

// ---------------- TRANSACTION DETAILS DIALOG ----------------
@Composable
fun TransactionDetailsDialog(
    tx: Transaction,
    language: AppLanguage,
    isDark: Boolean,
    linkedPerson: Person?,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onShareText: () -> Unit,
    onShareImage: (android.graphics.Bitmap) -> Unit,
    onNavigateToTab: ((String, String) -> Unit)? = null,
    onPersonClick: ((Person) -> Unit)? = null
) {
    val bgColor = if (isDark) Color(0xFF121212) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subtitleColor = if (isDark) Color.LightGray else Color(0xFF64748B)

    var showShareOptions by remember { mutableStateOf(false) }
    val screenshotState = rememberScreenshotState()

    if (showShareOptions) {
        AlertDialog(
            onDismissRequest = { showShareOptions = false },
            title = { Text(if (language == AppLanguage.BN) "কিভাবে শেয়ার করবেন?" else "How to share?") },
            text = {
                Column {
                    TextButton(onClick = { 
                        showShareOptions = false
                        onShareText()
                    }) {
                        Text(if (language == AppLanguage.BN) "টেক্সট হিসেবে" else "As Text", color = FintechBlue)
                    }
                    TextButton(onClick = {
                        showShareOptions = false
                        screenshotState.createBitmap()?.let { onShareImage(it) }
                    }) {
                        Text(if (language == AppLanguage.BN) "ছবি হিসেবে (স্ক্রিনশট)" else "As Image (Screenshot)", color = FintechBlue)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showShareOptions = false }) {
                    Text(if (language == AppLanguage.BN) "বাতিল" else "Cancel", color = FintechRed)
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (language == AppLanguage.BN) "ট্রানজ্যাকশন মেমো" else "Transaction Memo",
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .captureToPicture(screenshotState)
                    .background(if (isDark) Color(0xFF121212) else Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Detail Row Helper
                val DetailRow = @Composable { label: String, value: String, onClick: (() -> Unit)? ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .then(
                                if (onClick != null) {
                                    Modifier
                                        .clickable { onClick() }
                                        .background(if (isDark) Color(0xFF1C1C1E) else Color(0xFFEDF2F7))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                } else {
                                    Modifier.padding(vertical = 4.dp)
                                }
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = label, color = subtitleColor, fontSize = 13.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = value,
                                color = if (onClick != null) FintechBlue else textColor,
                                fontSize = 13.sp,
                                fontWeight = if (onClick != null) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.testTag("clickable_memo_${label.replace(":", "").lowercase().trim()}")
                            )
                            if (onClick != null) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Rounded.ChevronRight,
                                    contentDescription = "Navigate",
                                    tint = FintechBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                val typeStr = when (tx.type) {
                    "INCOME" -> if (language == AppLanguage.BN) "আয়" else "Income"
                    "EXPENSE" -> if (language == AppLanguage.BN) "ব্যয়" else "Expense"
                    "LEND" -> if (language == AppLanguage.BN) "ধার দেওয়া" else "Lend"
                    "BORROW" -> if (language == AppLanguage.BN) "ধার নেওয়া" else "Borrow"
                    "REPAY_PAID" -> if (language == AppLanguage.BN) "পরিশোধ" else "Repay Paid"
                    "REPAY_RECEIVED" -> if (language == AppLanguage.BN) "প্রাপ্তি" else "Repay Received"
                    else -> tx.type
                }
                
                val (targetTab, targetFilter) = when (tx.type) {
                    "INCOME" -> Pair("transactions", "INCOME")
                    "EXPENSE" -> Pair("transactions", "EXPENSE")
                    "LEND", "REPAY_RECEIVED" -> Pair("debts", "PAWN")
                    "BORROW", "REPAY_PAID" -> Pair("debts", "DENA")
                    else -> Pair("transactions", "ALL")
                }

                DetailRow(
                    if (language == AppLanguage.BN) "ধরণ:" else "Type:",
                    typeStr,
                    if (onNavigateToTab != null) {
                        {
                            onDismiss()
                            onNavigateToTab(targetTab, targetFilter)
                        }
                    } else null
                )

                DetailRow(
                    if (language == AppLanguage.BN) "পরিমাণ:" else "Amount:",
                    formatCurrency(tx.amount, language),
                    null
                )

                DetailRow(
                    if (language == AppLanguage.BN) "ক্যাটাগরি:" else "Category:",
                    tx.category,
                    if (onNavigateToTab != null) {
                        {
                            onDismiss()
                            onNavigateToTab(targetTab, targetFilter)
                        }
                    } else null
                )

                if (linkedPerson != null) {
                    DetailRow(
                        if (language == AppLanguage.BN) "ব্যক্তি:" else "Person:",
                        linkedPerson.name,
                        if (onPersonClick != null) {
                            {
                                onDismiss()
                                onPersonClick(linkedPerson)
                            }
                        } else null
                    )
                }

                DetailRow(
                    if (language == AppLanguage.BN) "তারিখ:" else "Date:",
                    formatDate(tx.timestamp, language),
                    null
                )
                
                if (tx.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = subtitleColor.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = if (language == AppLanguage.BN) "নোট:" else "Note:", color = subtitleColor, fontSize = 13.sp)
                    Text(text = tx.note, color = textColor, fontSize = 14.sp)
                }
            }
        },
        containerColor = bgColor,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = FintechBlue)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = FintechRed)
                    }
                    IconButton(onClick = { showShareOptions = true }) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share", tint = FintechBlue)
                    }
                }
                
                TextButton(onClick = onDismiss) {
                    Text(if (language == AppLanguage.BN) "বন্ধ করুন" else "Close", color = FintechBlue)
                }
            }
        }
    )
}

// ---------------- TRANSACTIONS TAB ----------------
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun TransactionsScreen(
    language: AppLanguage,
    isDark: Boolean,
    transactions: List<Transaction>,
    persons: List<Person>,
    onAddTransactionClick: () -> Unit,
    onDeleteTransaction: (Int) -> Unit,
    onDeleteTransactions: (List<Int>) -> Unit = {},
    onEditTransaction: (Transaction) -> Unit,
    filter: String = "ALL",
    onFilterChange: (String) -> Unit = {},
    timeFilter: String = "ALL",
    onTimeFilterChange: (String) -> Unit = {},
    highlightedTxId: Int? = null,
    onNavigateToTab: ((String, String) -> Unit)? = null,
    onPersonClick: ((Person) -> Unit)? = null,
    activeTab: String = ""
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentSortBy by remember { mutableStateOf("DATE_DESC") }
    var showSortMenu by remember { mutableStateOf(false) }

    var selectedTxIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedTxIds.isNotEmpty()

    androidx.activity.compose.BackHandler(enabled = isSelectionMode) {
        selectedTxIds = emptySet()
    }
    val timeFilteredTransactions = remember(transactions, timeFilter) {
        filterTransactionsByTime(transactions, timeFilter)
    }

    val filteredTransactions = remember(timeFilteredTransactions, filter) {
        val list = timeFilteredTransactions
        // Type filter
        when (filter) {
            "INCOME" -> list.filter { it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT") }
            "EXPENSE" -> list.filter { it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT") }
            "DENA" -> list.filter { it.type == "BORROW" || it.type == "REPAY_PAID" }
            "PAWN" -> list.filter { it.type == "LEND" || it.type == "REPAY_RECEIVED" }
            else -> list
        }
    }

    val searchQueryLower = searchQuery.lowercase().trim()
    val searchedTransactions = remember(filteredTransactions, searchQuery, persons) {
        if (searchQueryLower.isEmpty()) {
            filteredTransactions
        } else {
            filteredTransactions.filter { tx ->
                val personName = persons.find { it.id == tx.personId }?.name?.lowercase() ?: ""
                tx.note.lowercase().contains(searchQueryLower) || 
                tx.amount.toString().contains(searchQueryLower) ||
                personName.contains(searchQueryLower)
            }
        }
    }

    val sortedTransactions = remember(searchedTransactions, currentSortBy, persons) {
        val list = searchedTransactions.toMutableList()
        when (currentSortBy) {
            "DATE_DESC" -> list.sortByDescending { it.timestamp }
            "DATE_ASC" -> list.sortBy { it.timestamp }
            "AMOUNT_DESC" -> list.sortByDescending { it.amount }
            "AMOUNT_ASC" -> list.sortBy { it.amount }
            "NAME_ASC" -> list.sortBy { tx ->
                persons.find { it.id == tx.personId }?.name?.lowercase() ?: tx.note.lowercase()
            }
            "NAME_DESC" -> list.sortByDescending { tx ->
                persons.find { it.id == tx.personId }?.name?.lowercase() ?: tx.note.lowercase()
            }
        }
        list
    }

    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    LaunchedEffect(highlightedTxId) {
        if (highlightedTxId != null) {
            var targetIndex = -1
            if (currentSortBy.startsWith("DATE")) {
                val grouped = sortedTransactions.groupBy { formatDateToDay(it.timestamp) }
                var currentIndex = 0
                outer@ for ((_, txs) in grouped) {
                    currentIndex++ // for the date header item
                    for (tx in txs) {
                        if (tx.id == highlightedTxId) {
                            targetIndex = currentIndex
                            break@outer
                        }
                        currentIndex++
                    }
                }
            } else {
                targetIndex = sortedTransactions.indexOfFirst { it.id == highlightedTxId }
            }
            
            if (targetIndex != -1) {
                try {
                    lazyListState.animateScrollToItem(targetIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val totalIncome = timeFilteredTransactions.filter { it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT") }.sumOf { it.amount }
    val totalExpense = timeFilteredTransactions.filter { it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT") }.sumOf { it.amount }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelectionMode,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FintechBlue)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedTxIds = emptySet() }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Text(
                            text = "${selectedTxIds.size} ${if (language == AppLanguage.BN) "টি নির্বাচিত" else "selected"}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    
                    TextButton(
                        onClick = {
                            if (selectedTxIds.size == sortedTransactions.size) {
                                selectedTxIds = emptySet()
                            } else {
                                selectedTxIds = sortedTransactions.map { it.id }.toSet()
                            }
                        }
                    ) {
                        Text(
                            text = if (selectedTxIds.size == sortedTransactions.size) 
                                (if (language == AppLanguage.BN) "সব আনমার্ক" else "Deselect All")
                            else (if (language == AppLanguage.BN) "সব মার্ক" else "Select All"),
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Modern Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        text = if (language == AppLanguage.BN) "এখানে খুঁজুন..." else "Search here...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = null,
                                tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f),
                    focusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF1F5F9),
                    unfocusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF1F5F9)
                )
            )

            // Summary Cards (arranged horizontally in a Row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FintechGradientCard(
                    gradientColors = if (isDark) listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E)) else activeThemeGradient,
                    cornerRadius = 24.dp,
                    padding = PaddingValues(10.dp),
                    modifier = Modifier.weight(1f).height(80.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(Translation.get("total_income", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(formatCurrency(totalIncome, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                FintechGradientCard(
                    gradientColors = if (isDark) listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E)) else activeThemeGradient,
                    cornerRadius = 24.dp,
                    padding = PaddingValues(10.dp),
                    modifier = Modifier.weight(1f).height(80.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(Translation.get("total_expense", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(formatCurrency(totalExpense, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.TrendingDown,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Time Filter Row
            TimeFilterRow(
                timeFilter = timeFilter,
                language = language,
                onTimeFilterChange = onTimeFilterChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Type Filters chip row & Sort button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        Pair("ALL", "all"),
                        Pair("INCOME", "income"),
                        Pair("EXPENSE", "expense"),
                        Pair("DENA", "dena"),
                        Pair("PAWN", "pawn")
                    )

                    filters.forEach { (type, labelKey) ->
                        val isSelected = filter == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else if (isDark) Color(0xFF1C1C1E) else Color.White
                                )
                                .clickable { onFilterChange(type) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = Translation.get(labelKey, language),
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isDark) Color(0xFF1C1C1E) else Color.White,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sort,
                            contentDescription = "Sort",
                            tint = FintechBlue
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(if (isDark) Color(0xFF1C1C1E) else Color.White)
                    ) {
                        val sortOptions = listOf(
                            Triple("DATE_DESC", if (language == AppLanguage.BN) "নতুন সময় আগে" else "Newest First", Icons.Rounded.ArrowDownward),
                            Triple("DATE_ASC", if (language == AppLanguage.BN) "পুরানো সময় আগে" else "Oldest First", Icons.Rounded.ArrowUpward),
                            Triple("AMOUNT_DESC", if (language == AppLanguage.BN) "বেশি পরিমাণ আগে" else "Highest Amount First", Icons.Rounded.TrendingUp),
                            Triple("AMOUNT_ASC", if (language == AppLanguage.BN) "কম পরিমাণ আগে" else "Lowest Amount First", Icons.Rounded.TrendingDown),
                            Triple("NAME_ASC", if (language == AppLanguage.BN) "নাম অনুযায়ী (ক-অ)" else "Name (A-Z)", Icons.Rounded.SortByAlpha),
                            Triple("NAME_DESC", if (language == AppLanguage.BN) "নাম অনুযায়ী (অ-ক)" else "Name (Z-A)", Icons.Rounded.SortByAlpha)
                        )

                        sortOptions.forEach { (option, label, icon) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (currentSortBy == option) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = label,
                                            color = if (currentSortBy == option) MaterialTheme.colorScheme.primary else (if (isDark) Color.White else Color.Black),
                                            fontWeight = if (currentSortBy == option) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    }
                                },
                                onClick = {
                                    currentSortBy = option
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Transactions List
            if (sortedTransactions.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(64.dp)
                            .padding(bottom = 16.dp)
                    )
                    Text(Translation.get("no_tx", language), color = Color.Gray)
                }
            } else {
                if (currentSortBy.startsWith("DATE")) {
                    val grouped = sortedTransactions.groupBy { formatDateToDay(it.timestamp) }
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        grouped.forEach { (date, txs) ->
                            item {
                                Text(
                                    text = formatDateHeader(date, language),
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
                                )
                            }
                            items(txs, key = { it.id }) { tx ->
                                val isSelected = selectedTxIds.contains(tx.id)
                                Box(modifier = Modifier.padding(horizontal = 4.dp).animateItem()) {
                                    TransactionRowItem(
                                        tx = tx,
                                        language = language,
                                        isDark = isDark,
                                        persons = persons,
                                        onDelete = onDeleteTransaction,
                                        onEdit = onEditTransaction,
                                        isHighlighted = (tx.id == highlightedTxId),
                                        onNavigateToTab = onNavigateToTab,
                                        onPersonClick = onPersonClick,
                                        searchQuery = searchQuery,
                                        isSelected = isSelected,
                                        isSelectionMode = isSelectionMode,
                                        onLongClick = {
                                            selectedTxIds = if (isSelected) selectedTxIds - tx.id else selectedTxIds + tx.id
                                        }
                                    )
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(110.dp)) // Floating button padding
                        }
                    }
                } else {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(sortedTransactions, key = { it.id }) { tx ->
                            val isSelected = selectedTxIds.contains(tx.id)
                            Box(modifier = Modifier.padding(horizontal = 4.dp).animateItem()) {
                                TransactionRowItem(
                                    tx = tx,
                                    language = language,
                                    isDark = isDark,
                                    persons = persons,
                                    onDelete = onDeleteTransaction,
                                    onEdit = onEditTransaction,
                                    isHighlighted = (tx.id == highlightedTxId),
                                    onNavigateToTab = onNavigateToTab,
                                    onPersonClick = onPersonClick,
                                    searchQuery = searchQuery,
                                    isSelected = isSelected,
                                    isSelectionMode = isSelectionMode,
                                    onLongClick = {
                                        selectedTxIds = if (isSelected) selectedTxIds - tx.id else selectedTxIds + tx.id
                                    }
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(110.dp)) // Floating button padding
                        }
                    }
                }
            }
        }

        if (isSelectionMode) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 110.dp)
                    .background(if (isDark) Color(0xFF121212) else Color.White, RoundedCornerShape(28.dp))
                    .border(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete Button (মুছে ফেলুন) - Only delete button for transactions!
                Button(
                    onClick = {
                        onDeleteTransactions(selectedTxIds.toList())
                        selectedTxIds = emptySet()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechRed),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "মুছে ফেলুন" else "Delete",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ---------------- DEBTS & CREDITS TAB ----------------
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun DebtsScreen(
    language: AppLanguage,
    isDark: Boolean,
    personDebts: List<PersonDebt>,
    onAddPersonClick: () -> Unit,
    onPersonClick: (PersonDebt) -> Unit,
    onDeletePerson: (Int) -> Unit,
    onMovePerson: (Person) -> Unit = {},
    onDeletePersons: (List<Int>) -> Unit = {},
    onMovePersons: (List<Int>) -> Unit = {},
    filter: String = "ALL",
    onFilterChange: (String) -> Unit = {},
    timeFilter: String = "ALL",
    onTimeFilterChange: (String) -> Unit = {},
    highlightedPersonId: Int? = null,
    activeTab: String = ""
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentSortBy by remember { mutableStateOf("NAME_ASC") }
    var showSortMenu by remember { mutableStateOf(false) }

    var selectedPersonIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedPersonIds.isNotEmpty()

    androidx.activity.compose.BackHandler(enabled = isSelectionMode) {
        selectedPersonIds = emptySet()
    }

    val filteredDebts = remember(personDebts, filter) {
        when (filter) {
            "DENA" -> personDebts.filter { it.netBalance < 0 }
            "PAWN" -> personDebts.filter { it.netBalance > 0 }
            else -> personDebts
        }
    }

    val searchQueryLower = searchQuery.lowercase().trim()
    val searchedDebts = remember(filteredDebts, searchQuery) {
        if (searchQueryLower.isEmpty()) {
            filteredDebts
        } else {
            filteredDebts.filter { item ->
                item.person.name.lowercase().contains(searchQueryLower) ||
                item.person.phone.lowercase().contains(searchQueryLower) ||
                item.person.address.lowercase().contains(searchQueryLower) ||
                item.netBalance.toString().contains(searchQueryLower)
            }
        }
    }

    val sortedDebts = remember(searchedDebts, currentSortBy) {
        val list = searchedDebts.toMutableList()
        when (currentSortBy) {
            "NAME_ASC" -> list.sortBy { it.person.name.lowercase() }
            "NAME_DESC" -> list.sortByDescending { it.person.name.lowercase() }
            "AMOUNT_DESC" -> list.sortByDescending { kotlin.math.abs(it.netBalance) }
            "AMOUNT_ASC" -> list.sortBy { kotlin.math.abs(it.netBalance) }
        }
        list
    }

    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    LaunchedEffect(highlightedPersonId) {
        if (highlightedPersonId != null) {
            val targetIndex = sortedDebts.indexOfFirst { it.person.id == highlightedPersonId }
            if (targetIndex != -1) {
                try {
                    lazyListState.animateScrollToItem(targetIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val totalDena = personDebts.filter { it.netBalance < 0 }.sumOf { -it.netBalance }
    val totalPawn = personDebts.filter { it.netBalance > 0 }.sumOf { it.netBalance }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Multi-Select Header Toolbar
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelectionMode,
                enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FintechBlue)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedPersonIds = emptySet() }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Text(
                            text = "${selectedPersonIds.size} ${if (language == AppLanguage.BN) "জন নির্বাচিত" else "selected"}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    
                    TextButton(
                        onClick = {
                            if (selectedPersonIds.size == sortedDebts.size) {
                                selectedPersonIds = emptySet()
                            } else {
                                selectedPersonIds = sortedDebts.map { it.person.id }.toSet()
                            }
                        }
                    ) {
                        Text(
                            text = if (selectedPersonIds.size == sortedDebts.size) 
                                (if (language == AppLanguage.BN) "সব আনমার্ক" else "Deselect All")
                            else (if (language == AppLanguage.BN) "সব মার্ক" else "Select All"),
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Modern Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        text = if (language == AppLanguage.BN) "এখানে খুঁজুন..." else "Search here...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = null,
                                tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f),
                    focusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF1F5F9),
                    unfocusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF1F5F9)
                )
            )

            // Summary Cards (arranged horizontally in a Row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FintechGradientCard(
                    gradientColors = if (isDark) listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E)) else activeThemeGradient,
                    cornerRadius = 24.dp,
                    padding = PaddingValues(10.dp),
                    modifier = Modifier.weight(1f).height(80.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(Translation.get("total_dena", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(formatCurrency(totalDena, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
                        }
                        Icon(
                            imageVector = Icons.Rounded.ArrowDownward,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                FintechGradientCard(
                    gradientColors = if (isDark) listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E)) else activeThemeGradient,
                    cornerRadius = 24.dp,
                    padding = PaddingValues(10.dp),
                    modifier = Modifier.weight(1f).height(80.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(Translation.get("total_pawn", language), color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 1)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(formatCurrency(totalPawn, language), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
                        }
                        Icon(
                            imageVector = Icons.Rounded.ArrowUpward,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Time Filter Row
            TimeFilterRow(
                timeFilter = timeFilter,
                language = language,
                onTimeFilterChange = onTimeFilterChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Filter Tabs & Sort Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        Pair("ALL", "all"),
                        Pair("DENA", "dena"),
                        Pair("PAWN", "pawn")
                    )

                    filters.forEach { (type, labelKey) ->
                        val isSelected = filter == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else if (isDark) Color(0xFF1C1C1E) else Color.White
                                )
                                .clickable { onFilterChange(type) }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = Translation.get(labelKey, language),
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isDark) Color(0xFF1C1C1E) else Color.White,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sort,
                            contentDescription = "Sort",
                            tint = FintechBlue
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(if (isDark) Color(0xFF1C1C1E) else Color.White)
                    ) {
                        val sortOptions = listOf(
                            Triple("NAME_ASC", if (language == AppLanguage.BN) "নাম অনুযায়ী (ক-অ)" else "Name (A-Z)", Icons.Rounded.SortByAlpha),
                            Triple("NAME_DESC", if (language == AppLanguage.BN) "নাম অনুযায়ী (অ-ক)" else "Name (Z-A)", Icons.Rounded.SortByAlpha),
                            Triple("AMOUNT_DESC", if (language == AppLanguage.BN) "বেশি পরিমাণ আগে" else "Highest Amount First", Icons.Rounded.TrendingUp),
                            Triple("AMOUNT_ASC", if (language == AppLanguage.BN) "কম পরিমাণ আগে" else "Lowest Amount First", Icons.Rounded.TrendingDown)
                        )

                        sortOptions.forEach { (option, label, icon) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (currentSortBy == option) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = label,
                                            color = if (currentSortBy == option) MaterialTheme.colorScheme.primary else (if (isDark) Color.White else Color.Black),
                                            fontWeight = if (currentSortBy == option) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    }
                                },
                                onClick = {
                                    currentSortBy = option
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            if (sortedDebts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.6f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(Translation.get("no_persons", language), color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(sortedDebts, key = { it.person.id }) { item ->
                        val isSelected = selectedPersonIds.contains(item.person.id)
                        Box(modifier = Modifier.padding(horizontal = 4.dp).animateItem()) {
                            PersonDebtRowItem(
                                item = item,
                                language = language,
                                isDark = isDark,
                                onClick = { debt ->
                                    if (isSelectionMode) {
                                        selectedPersonIds = if (isSelected) {
                                            selectedPersonIds - debt.person.id
                                        } else {
                                            selectedPersonIds + debt.person.id
                                        }
                                    } else {
                                        onPersonClick(debt)
                                    }
                                },
                                onDelete = onDeletePerson,
                                onMove = onMovePerson,
                                isHighlighted = (item.person.id == highlightedPersonId),
                                searchQuery = searchQuery,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onLongClick = {
                                    selectedPersonIds = selectedPersonIds + item.person.id
                                }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(110.dp))
                    }
                }
            }
        }

        // Bulk Actions Floating Buttons
        androidx.compose.animation.AnimatedVisibility(
            visible = isSelectionMode,
            enter = androidx.compose.animation.scaleIn() + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp)
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDark) Color(0xFF121212) else Color.White)
                    .border(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bulk Move Button
                Button(
                    onClick = { 
                        onMovePersons(selectedPersonIds.toList())
                        selectedPersonIds = emptySet()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Rounded.SwapHoriz, contentDescription = null, tint = FintechBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (language == AppLanguage.BN) "মুভ" else "Move", color = FintechBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                
                // Bulk Delete Button
                var showBulkDeleteConfirm by remember { mutableStateOf(false) }
                Button(
                    onClick = { showBulkDeleteConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = null, tint = FintechRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (language == AppLanguage.BN) "ডিলিট" else "Delete", color = FintechRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                
                if (showBulkDeleteConfirm) {
                    DeleteVerificationDialog(
                        language = language,
                        onConfirm = {
                            onDeletePersons(selectedPersonIds.toList())
                            selectedPersonIds = emptySet()
                            showBulkDeleteConfirm = false
                        },
                        onDismiss = { showBulkDeleteConfirm = false }
                    )
                }
            }
        }

        // Floating Action Button to Add Person
        androidx.compose.animation.AnimatedVisibility(
            visible = !isSelectionMode,
            enter = androidx.compose.animation.scaleIn() + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 113.dp, end = 16.dp)
        ) {
            FloatingActionButton(
                onClick = onAddPersonClick,
                containerColor = FintechBlue,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(60.dp)
                    .testTag("fab_add_person")
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_debt_credit), 
                    contentDescription = "Add Person", 
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
fun TimeFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: ImageVector
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TimeFilterRow(
    timeFilter: String,
    language: AppLanguage,
    onTimeFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isMonthActive = timeFilter == "MONTH" || timeFilter.startsWith("CUSTOM_MONTH:")
        val isDateActive = timeFilter.startsWith("CUSTOM_DATE:")
        
        // 1. All Time Chip
        TimeFilterChip(
            selected = timeFilter == "ALL",
            onClick = { onTimeFilterChange("ALL") },
            label = if (language == AppLanguage.BN) "সব সময়ের" else "All Time",
            icon = Icons.Rounded.DateRange
        )
        
        // 2. Month Chip
        val calendar = java.util.Calendar.getInstance()
        val curYear = calendar.get(java.util.Calendar.YEAR)
        val curMonth = calendar.get(java.util.Calendar.MONTH) + 1
        val monthLabel = if (timeFilter.startsWith("CUSTOM_MONTH:")) {
            getCustomTimeFilterLabel(timeFilter, language) + (if (language == AppLanguage.BN) " মাসের" else " Month")
        } else {
            getCustomTimeFilterLabel("CUSTOM_MONTH:$curYear-$curMonth", language) + (if (language == AppLanguage.BN) " মাসের" else " Month")
        }
        
        TimeFilterChip(
            selected = isMonthActive,
            onClick = { onTimeFilterChange("TRIGGER_MONTH_PICKER") },
            label = monthLabel,
            icon = Icons.Rounded.DateRange
        )
        
        // 3. Specific Date Chip
        val dateLabel = if (timeFilter.startsWith("CUSTOM_DATE:")) {
            getFormattedDateLabel(timeFilter, language)
        } else {
            if (language == AppLanguage.BN) "তারিখ অনুযায়ী" else "Select Date"
        }
        
        TimeFilterChip(
            selected = isDateActive,
            onClick = { onTimeFilterChange("TRIGGER_DATE_PICKER") },
            label = dateLabel,
            icon = Icons.Rounded.DateRange
        )
    }
}

fun filterTransactionsByTime(transactions: List<Transaction>, timeFilter: String): List<Transaction> {
    val now = System.currentTimeMillis()
    val calendar = java.util.Calendar.getInstance()
    return when {
        timeFilter == "TODAY" -> {
            calendar.timeInMillis = now
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            transactions.filter { it.timestamp >= startOfDay }
        }
        timeFilter == "MONTH" -> {
            calendar.timeInMillis = now
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val startOfMonth = calendar.timeInMillis
            transactions.filter { it.timestamp >= startOfMonth }
        }
        timeFilter.startsWith("CUSTOM_MONTH:") -> {
            val parts = timeFilter.substringAfter("CUSTOM_MONTH:").split("-")
            if (parts.size == 2) {
                val year = parts[0].toIntOrNull() ?: 2026
                val month = parts[1].toIntOrNull() ?: 1
                calendar.clear()
                calendar.set(java.util.Calendar.YEAR, year)
                calendar.set(java.util.Calendar.MONTH, month - 1)
                calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.add(java.util.Calendar.MONTH, 1)
                val end = calendar.timeInMillis
                transactions.filter { it.timestamp in start until end }
            } else transactions
        }
        timeFilter.startsWith("CUSTOM_DATE:") -> {
            val parts = timeFilter.substringAfter("CUSTOM_DATE:").split("-")
            if (parts.size == 3) {
                val year = parts[0].toIntOrNull() ?: 2026
                val month = parts[1].toIntOrNull() ?: 1
                val day = parts[2].toIntOrNull() ?: 1
                calendar.clear()
                calendar.set(java.util.Calendar.YEAR, year)
                calendar.set(java.util.Calendar.MONTH, month - 1)
                calendar.set(java.util.Calendar.DAY_OF_MONTH, day)
                calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                calendar.set(java.util.Calendar.MINUTE, 0)
                calendar.set(java.util.Calendar.SECOND, 0)
                calendar.set(java.util.Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.add(java.util.Calendar.DAY_OF_MONTH, 1)
                val end = calendar.timeInMillis
                transactions.filter { it.timestamp in start until end }
            } else transactions
        }
        else -> transactions
    }
}

fun getYearMonthFromFilter(filter: String): Pair<Int, Int>? {
    val calendar = java.util.Calendar.getInstance()
    return when {
        filter == "MONTH" -> {
            Pair(calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH) + 1)
        }
        filter.startsWith("CUSTOM_MONTH:") -> {
            val parts = filter.substringAfter("CUSTOM_MONTH:").split("-")
            if (parts.size == 2) {
                Pair(parts[0].toIntOrNull() ?: 2026, parts[1].toIntOrNull() ?: 1)
            } else null
        }
        else -> null
    }
}

fun getFormattedPeriodText(timeFilter: String, language: AppLanguage): String {
    if (timeFilter == "ALL") {
        return if (language == AppLanguage.BN) "সব সময়ের তথ্য দেখানো হচ্ছে" else "All-time data is shown"
    }
    if (timeFilter == "TODAY") {
        return if (language == AppLanguage.BN) "আজকের তথ্য দেখানো হচ্ছে" else "Today's data is shown"
    }
    
    val now = System.currentTimeMillis()
    val calendar = java.util.Calendar.getInstance()
    var year = calendar.get(java.util.Calendar.YEAR)
    var month = calendar.get(java.util.Calendar.MONTH) + 1
    
    if (timeFilter.startsWith("CUSTOM_MONTH:")) {
        val parts = timeFilter.substringAfter("CUSTOM_MONTH:").split("-")
        if (parts.size == 2) {
            year = parts[0].toIntOrNull() ?: year
            month = parts[1].toIntOrNull() ?: month
        }
    } else if (timeFilter.startsWith("CUSTOM_DATE:")) {
        val parts = timeFilter.substringAfter("CUSTOM_DATE:").split("-")
        if (parts.size == 3) {
            year = parts[0].toIntOrNull() ?: year
            month = parts[1].toIntOrNull() ?: month
        }
    }
    
    val monthNameBn = when (month) {
        1 -> "জানুয়ারি"
        2 -> "ফেব্রুয়ারি"
        3 -> "মার্চ"
        4 -> "এপ্রিল"
        5 -> "মে"
        6 -> "জুন"
        7 -> "জুলাই"
        8 -> "আগস্ট"
        9 -> "সেপ্টেম্বর"
        10 -> "অক্টোবর"
        11 -> "নভেম্বর"
        12 -> "ডিসেম্বর"
        else -> "জুলাই"
    }
    val monthNameEn = when (month) {
        1 -> "January"
        2 -> "February"
        3 -> "March"
        4 -> "April"
        5 -> "May"
        6 -> "June"
        7 -> "July"
        8 -> "August"
        9 -> "September"
        10 -> "October"
        11 -> "November"
        12 -> "December"
        else -> "July"
    }
    
    val yearStr = year.toString()
    val yearFormatted = if (language == AppLanguage.BN) {
        yearStr.map { char ->
            when (char) {
                '0' -> '০'
                '1' -> '১'
                '2' -> '২'
                '3' -> '৩'
                '4' -> '৪'
                '5' -> '৫'
                '6' -> '৬'
                '7' -> '৭'
                '8' -> '৮'
                '9' -> '৯'
                else -> char
            }
        }.joinToString("")
    } else {
        yearStr
    }
    
    return if (language == AppLanguage.BN) {
        "$monthNameBn $yearFormatted এর তথ্য দেখানো হচ্ছে"
    } else {
        "$monthNameEn $yearFormatted's data is shown"
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PersonDebtRowItem(
    item: PersonDebt,
    language: AppLanguage,
    isDark: Boolean,
    onClick: (PersonDebt) -> Unit,
    onDelete: (Int) -> Unit,
    onMove: (Person) -> Unit = {},
    isHighlighted: Boolean = false,
    searchQuery: String = "",
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        DeleteVerificationDialog(
            language = language,
            onConfirm = {
                onDelete(item.person.id)
                showDeleteConfirm = false
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    val bgColor by androidx.compose.animation.animateColorAsState(
        if (isSelected) {
            if (isDark) FintechBlue.copy(alpha = 0.2f) else FintechBlue.copy(alpha = 0.1f)
        } else if (isHighlighted) {
            if (isDark) Color(0xFF453A1E) else Color(0xFFFEF3C7)
        } else {
            if (isDark) Color(0xFF121212) else Color.White
        }
    )
    
    val borderColor = if (isHighlighted) {
        Color(0xFFFBBF24) // Beautiful glowing amber color
    } else if (isSelected) {
        FintechBlue
    } else if (isDark) {
        Color.White.copy(alpha = 0.05f)
    } else {
        Color.Black.copy(alpha = 0.05f)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(if (isHighlighted || isSelected) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onLongClick()
                    } else {
                        onClick(item)
                    }
                },
                onLongClick = onLongClick
            )
            .testTag("person_item_${item.person.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (isSelectionMode) {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) FintechBlue else Color.Gray.copy(alpha = 0.2f))
                            .border(2.dp, if (isSelected) FintechBlue else Color.Gray.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                // Avatar representation
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(FintechBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.person.photoUri.isNotEmpty()) {
                        AsyncImage(
                            model = item.person.photoUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null,
                            tint = FintechBlue,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    HighlightedText(
                        text = item.person.name,
                        query = searchQuery,
                        color = MaterialTheme.colorScheme.primary,
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E222F)
                        )
                    )
                    if (item.person.phone.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            HighlightedText(
                                text = item.person.phone,
                                query = searchQuery,
                                color = MaterialTheme.colorScheme.primary,
                                style = TextStyle(fontSize = 11.sp, color = Color.Gray)
                            )
                        }
                    }
                    if (item.person.address.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 1.dp)) {
                            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            HighlightedText(
                                text = item.person.address,
                                query = searchQuery,
                                color = MaterialTheme.colorScheme.primary,
                                style = TextStyle(fontSize = 11.sp, color = Color.Gray)
                            )
                        }
                    }
                }
            }

            // Net balance representation
            Column(horizontalAlignment = Alignment.End) {
                val (statusText, statusColor, absoluteAmount) = when {
                    item.netBalance > 0 -> Triple(Translation.get("you_get", language), FintechGreen, item.netBalance)
                    item.netBalance < 0 -> Triple(Translation.get("you_owe", language), FintechRed, -item.netBalance)
                    else -> Triple(Translation.get("settled", language), Color.Gray, 0.0)
                }

                Text(
                    text = statusText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
                Text(
                    text = formatCurrency(absoluteAmount, language),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = statusColor
                )
            }
        }
            }
        }

// ---------------- SAVINGS TAB ----------------
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun SavingsScreen(
    language: AppLanguage,
    isDark: Boolean,
    profileName: String,
    profilePhotoUri: String?,
    allGradientsList: List<com.example.ui.theme.ThemeGradient>,
    savingsGoals: List<SavingsGoal>,
    onAddSavingsGoalClick: () -> Unit,
    onGoalClick: (SavingsGoal) -> Unit,
    onContributeClick: (SavingsGoal, Boolean) -> Unit,
    onEditGoal: (SavingsGoal) -> Unit,
    onMoveGoal: (SavingsGoal) -> Unit = {},
    onDeleteGoals: (List<Int>) -> Unit = {},
    onMoveGoals: (List<Int>) -> Unit = {},
    highlightedGoalId: Int? = null,
    activeTab: String = ""
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentSortBy by remember { mutableStateOf("TITLE_ASC") }
    var showSortMenu by remember { mutableStateOf(false) }

    var selectedGoalIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedGoalIds.isNotEmpty()

    androidx.activity.compose.BackHandler(enabled = isSelectionMode) {
        selectedGoalIds = emptySet()
    }

    val searchQueryLower = searchQuery.lowercase().trim()
    val searchedGoals = remember(savingsGoals, searchQuery) {
        if (searchQueryLower.isEmpty()) {
            savingsGoals
        } else {
            savingsGoals.filter { goal ->
                goal.title.lowercase().contains(searchQueryLower) ||
                goal.targetAmount.toString().contains(searchQueryLower) ||
                goal.savedAmount.toString().contains(searchQueryLower)
            }
        }
    }

    val sortedGoals = remember(searchedGoals, currentSortBy) {
        val list = searchedGoals.toMutableList()
        when (currentSortBy) {
            "TITLE_ASC" -> list.sortBy { it.title.lowercase() }
            "TITLE_DESC" -> list.sortByDescending { it.title.lowercase() }
            "TARGET_DESC" -> list.sortByDescending { it.targetAmount }
            "TARGET_ASC" -> list.sortBy { it.targetAmount }
            "SAVED_DESC" -> list.sortByDescending { it.savedAmount }
            "SAVED_ASC" -> list.sortBy { it.savedAmount }
        }
        list
    }

    val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    LaunchedEffect(highlightedGoalId) {
        if (highlightedGoalId != null) {
            val targetIndex = sortedGoals.indexOfFirst { it.id == highlightedGoalId }
            if (targetIndex != -1) {
                try {
                    lazyListState.animateScrollToItem(targetIndex)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            androidx.compose.animation.AnimatedVisibility(
                visible = !isSelectionMode,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "আপনার সঞ্চয় কার্ডসমূহ" else "Your Savings Cards",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FintechBlue
                    )
                    
                    IconButton(
                        onClick = onAddSavingsGoalClick,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(FintechBlue.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_savings),
                            contentDescription = "Add",
                            tint = FintechBlue
                        )
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = isSelectionMode,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FintechBlue)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedGoalIds = emptySet() }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Text(
                            text = "${selectedGoalIds.size} ${if (language == AppLanguage.BN) "টি নির্বাচিত" else "selected"}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    
                    TextButton(
                        onClick = {
                            if (selectedGoalIds.size == sortedGoals.size) {
                                selectedGoalIds = emptySet()
                            } else {
                                selectedGoalIds = sortedGoals.map { it.id }.toSet()
                            }
                        }
                    ) {
                        Text(
                            text = if (selectedGoalIds.size == sortedGoals.size) 
                                (if (language == AppLanguage.BN) "সব আনমার্ক" else "Deselect All")
                            else (if (language == AppLanguage.BN) "সব মার্ক" else "Select All"),
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Modern Search Bar & Sort Button Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f),
                    placeholder = {
                        Text(
                            text = if (language == AppLanguage.BN) "এখানে খুঁজুন..." else "Search here...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null,
                                    tint = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.12f),
                        focusedContainerColor = if (isDark) Color(0xFF121212) else Color(0xFFF1F5F9),
                        unfocusedContainerColor = if (isDark) Color(0xFF121212) else Color(0xFFF1F5F9)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (isDark) Color(0xFF121212) else Color.White,
                                RoundedCornerShape(16.dp)
                            )
                            .border(
                                1.dp,
                                if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f),
                                RoundedCornerShape(16.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sort,
                            contentDescription = "Sort",
                            tint = FintechBlue
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(if (isDark) Color(0xFF121212) else Color.White)
                    ) {
                        val sortOptions = listOf(
                            Triple("TITLE_ASC", if (language == AppLanguage.BN) "নাম অনুযায়ী (ক-অ)" else "Title (A-Z)", Icons.Rounded.SortByAlpha),
                            Triple("TITLE_DESC", if (language == AppLanguage.BN) "নাম অনুযায়ী (অ-ক)" else "Title (Z-A)", Icons.Rounded.SortByAlpha),
                            Triple("TARGET_DESC", if (language == AppLanguage.BN) "বেশি লক্ষ্য আগে" else "Highest Target First", Icons.Rounded.TrendingUp),
                            Triple("TARGET_ASC", if (language == AppLanguage.BN) "কম লক্ষ্য আগে" else "Lowest Target First", Icons.Rounded.TrendingDown),
                            Triple("SAVED_DESC", if (language == AppLanguage.BN) "বেশি সঞ্চিত আগে" else "Highest Saved First", Icons.Rounded.ArrowUpward),
                            Triple("SAVED_ASC", if (language == AppLanguage.BN) "কম সঞ্চিত আগে" else "Lowest Saved First", Icons.Rounded.ArrowDownward)
                        )

                        sortOptions.forEach { (option, label, icon) ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (currentSortBy == option) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = label,
                                            color = if (currentSortBy == option) MaterialTheme.colorScheme.primary else (if (isDark) Color.White else Color.Black),
                                            fontWeight = if (currentSortBy == option) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    }
                                },
                                onClick = {
                                    currentSortBy = option
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (sortedGoals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalance,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.6f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(Translation.get("no_savings", language), color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(sortedGoals, key = { it.id }) { goal ->
                        val isSelected = selectedGoalIds.contains(goal.id)
                        Box(modifier = Modifier.animateItem()) {
                            SavingsGoalCardItem(
                                goal = goal,
                                language = language,
                                isDark = isDark,
                                profileName = profileName,
                                allGradientsList = allGradientsList,
                                onGoalClick = { item ->
                                    if (isSelectionMode) {
                                        selectedGoalIds = if (isSelected) selectedGoalIds - item.id else selectedGoalIds + item.id
                                    } else {
                                        onGoalClick(item)
                                    }
                                },
                                onContributeClick = onContributeClick,
                                onEditGoal = onEditGoal,
                                onMove = onMoveGoal,
                                isHighlighted = (goal.id == highlightedGoalId),
                                searchQuery = searchQuery,
                                isSelected = isSelected,
                                isSelectionMode = isSelectionMode,
                                onLongClick = {
                                    selectedGoalIds = if (isSelected) selectedGoalIds - goal.id else selectedGoalIds + goal.id
                                }
                            )
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
        }

        if (isSelectionMode) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 110.dp)
                    .background(if (isDark) Color(0xFF121212) else Color.White, RoundedCornerShape(28.dp))
                    .border(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Move Button (মুভ)
                Button(
                    onClick = {
                        onMoveGoals(selectedGoalIds.toList())
                        selectedGoalIds = emptySet()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoveToInbox,
                        contentDescription = "Move",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "মুভ করুন" else "Move",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Delete Button (মুছে ফেলুন)
                Button(
                    onClick = {
                        onDeleteGoals(selectedGoalIds.toList())
                        selectedGoalIds = emptySet()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechRed),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "মুছে ফেলুন" else "Delete",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Floating Action Button to Add Savings Goal
            FloatingActionButton(
                onClick = onAddSavingsGoalClick,
                containerColor = FintechBlue,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 113.dp, end = 16.dp)
                    .size(60.dp)
                    .testTag("fab_add_savings_goal")
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_savings), 
                    contentDescription = "Add Goal", 
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavingsGoalCardItem(
    goal: SavingsGoal,
    language: AppLanguage,
    isDark: Boolean,
    profileName: String,
    allGradientsList: List<com.example.ui.theme.ThemeGradient>,
    onGoalClick: (SavingsGoal) -> Unit,
    onContributeClick: (SavingsGoal, Boolean) -> Unit,
    onEditGoal: (SavingsGoal) -> Unit,
    onMove: (SavingsGoal) -> Unit = {},
    isHighlighted: Boolean = false,
    maskBalance: Boolean = true,
    searchQuery: String = "",
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onLongClick: () -> Unit = {}
) {
    val rawGradient = if (goal.colorIndex in allGradientsList.indices) {
        allGradientsList[goal.colorIndex].colors
    } else {
        com.example.ui.theme.GradientsList[0]
    }
    val gradient = if (isDark) {
        listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E))
    } else {
        rawGradient
    }

    FintechGradientCard(
        gradientColors = gradient,
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .then(
                if (isHighlighted) Modifier.border(4.dp, Color(0xFFFBBF24), RoundedCornerShape(24.dp))
                else if (isSelected) Modifier.border(3.dp, Color.White, RoundedCornerShape(24.dp))
                else if (isDark) Modifier.border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                else Modifier
            )
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onLongClick()
                    } else {
                        onGoalClick(goal)
                    }
                },
                onLongClick = onLongClick
            )
            .testTag("savings_item_${goal.id}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Mastercard Chip and Contactless Icon
            Column(modifier = Modifier.align(Alignment.TopStart).padding(start = 12.dp, top = 12.dp)) {
                // Chip Icon Simulation
                Box(
                    modifier = Modifier
                        .size(38.dp, 28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(3) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Icon(
                    imageVector = Icons.Rounded.Wifi,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp).rotate(90f)
                )
            }


            val formattedCategory = if (language == AppLanguage.BN) {
                when (goal.category.lowercase()) {
                    "emergency", "জরুরি ফান্ড" -> "জরুরি ফান্ড"
                    "laptop", "ল্যাপটপ" -> "ল্যাপটপ"
                    "travel", "ভ্রমণ" -> "ভ্রমণ"
                    "marriage", "বিয়ে" -> "বিয়ে"
                    "investment", "বিনিয়োগ" -> "বিনিয়োগ"
                    else -> goal.category.uppercase()
                }
            } else {
                goal.category.uppercase()
            }

            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp)
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.2f))
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(Icons.Rounded.Check, contentDescription = null, tint = FintechBlue, modifier = Modifier.size(16.dp))
                    }
                }
            } else {
                HighlightedText(
                    text = formattedCategory,
                    query = searchQuery,
                    color = Color(0xFFFBBF24),
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 12.dp)
                )
            }

            // Goal Title and Balance in the bottom-left
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 12.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                HighlightedText(
                    text = goal.title.uppercase(),
                    query = searchQuery,
                    color = Color(0xFFFBBF24),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                )
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = if (maskBalance) "৳ ●●●●" else formatCurrency(goal.savedAmount, language),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (goal.targetAmount > 0) {
                        Text(
                            text = " / " + formatCurrency(goal.targetAmount, language),
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 2.dp, start = 2.dp)
                        )
                    }
                }
                
                Text(
                    text = goal.cardholderName.ifBlank { profileName }.uppercase(),
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            }

            // Bottom row: Mastercard-like logo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp, bottom = 8.dp)
                    .align(Alignment.BottomEnd),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {

                // Mastercard Circles Logo Simulation
                Box(contentAlignment = Alignment.Center) {
                    Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEB001B).copy(alpha = 0.8f))
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF79E1B).copy(alpha = 0.8f))
                        )
                    }
                }
            }
        }
            }
        }

// ---------------- DIALOGS ----------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonSelectorDialog(
    language: AppLanguage,
    persons: List<Person>,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSelect: (Person) -> Unit,
    onAddPersonClick: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredPersons = remember(searchQuery, persons) {
        persons.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.phone.contains(searchQuery, ignoreCase = true) ||
            it.address.contains(searchQuery, ignoreCase = true)
        }
    }

    val dialogBg = if (isDark) Color(0xFF121212) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F1724)
    val secondaryTextColor = if (isDark) Color.LightGray else Color(0xFF475569)
    val cardBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF8FAFC)
    val dividerColor = if (isDark) Color(0xFF262626) else Color(0xFFE2E8F0)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "ব্যক্তি সিলেক্ট করুন" else "Select Person",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = textColor
                    )
                    
                    IconButton(
                        onClick = onAddPersonClick,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PersonAdd,
                            contentDescription = "Add Person",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { 
                        Text(
                            text = if (language == AppLanguage.BN) "নাম, ফোন বা ঠিকানা দিয়ে খুঁজুন..." else "Search by name, phone...",
                            color = secondaryTextColor.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = secondaryTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = dividerColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // List of Persons
                if (filteredPersons.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "কোনো ব্যক্তি পাওয়া যায়নি" else "No person found",
                            color = secondaryTextColor,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredPersons, key = { it.id }) { person ->
                            val visibleState = remember(searchQuery) { androidx.compose.animation.core.MutableTransitionState(false).apply { targetState = true } }
                            androidx.compose.animation.AnimatedVisibility(
                                visibleState = visibleState,
                                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(
                                    initialOffsetY = { 40 }
                                ),
                                modifier = Modifier.animateItem()
                            ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelect(person)
                                        onDismiss()
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar / Photo
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (person.photoUri.isNotEmpty()) {
                                            AsyncImage(
                                                model = person.photoUri,
                                                contentDescription = person.name,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                text = person.name.take(1).uppercase(),
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Info
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = person.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = textColor
                                        )

                                        if (person.phone.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Phone,
                                                    contentDescription = "Phone",
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = person.phone,
                                                    fontSize = 11.sp,
                                                    color = secondaryTextColor
                                                )
                                            }
                                        }

                                        if (person.address.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Rounded.LocationOn,
                                                    contentDescription = "Address",
                                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = person.address,
                                                    fontSize = 11.sp,
                                                    color = secondaryTextColor,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cancel Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "বাতিল" else "Cancel",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(viewModel: com.example.ui.viewmodel.FinanceViewModel, 
    language: AppLanguage,
    persons: List<Person>,
    isDark: Boolean,
    editTransaction: Transaction? = null,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String, String, Int?, Long, String?) -> Unit,
    onAddPersonClick: () -> Unit = {}
) {
    val personDebts by viewModel.personDebts.collectAsState()
    var note by remember { mutableStateOf(editTransaction?.note ?: "") }
    var customTimestamp by remember { mutableStateOf<Long?>(editTransaction?.timestamp) }

    // Dropdowns / Option selectors
    var type by remember { mutableStateOf(editTransaction?.type ?: "EXPENSE") } // INCOME, EXPENSE, LEND, BORROW, REPAY_PAID, REPAY_RECEIVED
    var subType by remember { mutableStateOf(editTransaction?.subType ?: "CASH") } // CASH, CREDIT
    var selectedPersonId by remember { mutableStateOf<Int?>(editTransaction?.personId) }
    var category by remember { mutableStateOf(editTransaction?.category ?: "Food") }

    var previousPersonIds by remember { mutableStateOf(persons.map { it.id }.toSet()) }

    LaunchedEffect(persons) {
        val currentIds = persons.map { it.id }.toSet()
        val addedIds = currentIds - previousPersonIds
        if (addedIds.isNotEmpty()) {
            val newPerson = persons.find { it.id in addedIds }
            if (newPerson != null) {
                selectedPersonId = newPerson.id
            }
        }
        previousPersonIds = currentIds
    }

    val categoriesIncome = listOf("Salary", "Business", "Agriculture", "Gift", "Sales", "Honorarium", "Others")
    val categoriesExpense = listOf("Food", "Housing", "Transport", "Shopping", "Medical", "Education", "Clothing", "Others")

    val types = listOf(
        Pair("INCOME", "tx_type_income"),
        Pair("EXPENSE", "tx_type_expense"),
        Pair("LEND", "tx_type_lend"),
        Pair("BORROW", "tx_type_borrow"),
        Pair("REPAY_PAID", "tx_type_repay_paid"),
        Pair("REPAY_RECEIVED", "tx_type_repay_received")
    )

    var personDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var showPersonSelector by remember { mutableStateOf(false) }
    val isPersonRequired = type != "INCOME" && type != "EXPENSE"

    val dialogBg = if (isDark) Color(0xFF121212) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F1724)
    val labelColor = if (isDark) Color.Gray else Color(0xFF64748B)
    val chipBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF1F5F9)

    var amountInputState by remember { mutableStateOf(editTransaction?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var showTxCalculator by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        Translation.get(if (editTransaction != null) "edit_tx" else "add_tx", language),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = textColor,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Type Chips Grid
                item {
                    Text(Translation.get("type", language), color = labelColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            types.take(3).forEach { (tValue, tLabelKey) ->
                                val chipColor = when(tValue) {
                                    "INCOME", "LEND", "REPAY_RECEIVED" -> FintechGreen
                                    "EXPENSE", "BORROW", "REPAY_PAID" -> FintechRed
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                val isSelected = type == tValue
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) chipColor else chipColor.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .background(if (isSelected) chipColor else Color.Transparent)
                                        .clickable {
                                            type = tValue
                                            // auto set categories
                                            if (tValue == "INCOME") category = "Salary"
                                            else if (tValue == "EXPENSE") category = "Food"
                                            else category = "Loan"
                                            
                                            // Default subtype to CASH for all
                                            subType = "CASH"
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = Translation.get(tLabelKey, language),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else chipColor
                                    )
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            types.drop(3).forEach { (tValue, tLabelKey) ->
                                val chipColor = when(tValue) {
                                    "INCOME", "LEND", "REPAY_RECEIVED" -> FintechGreen
                                    "EXPENSE", "BORROW", "REPAY_PAID" -> FintechRed
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                val isSelected = type == tValue

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) chipColor else chipColor.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .background(if (isSelected) chipColor else Color.Transparent)
                                        .clickable {
                                            type = tValue
                                            category = "Loan"
                                            subType = "CASH"
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = Translation.get(tLabelKey, language),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else chipColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Sub-type selector (LEND/BORROW specific)
                if (type == "LEND" || type == "BORROW") {
                    item {
                        Text(
                            if (language == AppLanguage.BN) "লেনদেনের ধরন" else "Transaction Detail",
                            color = labelColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val options = if (type == "LEND") {
                                listOf(
                                    Triple("CASH", if (language == AppLanguage.BN) "নগদ ধার" else "Cash Loan", FintechBlue),
                                    Triple("CREDIT", if (language == AppLanguage.BN) "বাকি বিক্রয়" else "Credit Sale", FintechRed)
                                )
                            } else {
                                listOf(
                                    Triple("CASH", if (language == AppLanguage.BN) "নগদ ধার" else "Cash Loan", FintechBlue),
                                    Triple("CREDIT", if (language == AppLanguage.BN) "বাকি ক্রয়" else "Credit Purchase", FintechRed)
                                )
                            }

                            options.forEach { (sValue, sLabel, sColor) ->
                                val isSelected = subType == sValue
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) sColor else chipBg)
                                        .clickable { subType = sValue }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sLabel,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else textColor
                                    )
                                }
                            }
                        }
                    }
                }

                // Amount Text Field
                item {
                    OutlinedTextField(
                        value = amountInputState,
                        onValueChange = { amountInputState = it },
                        label = { Text(Translation.get("amount", language)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            IconButton(onClick = { showTxCalculator = true }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Rounded.Calculate,
                                    contentDescription = "Calculator",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = labelColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = labelColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tx_amount")
                    )
                    if (showTxCalculator) {
                        CalculatorDialog(
                            language = language,
                            isDark = isDark,
                            onDismiss = { showTxCalculator = false },
                            onInsert = { amountInputState = it }
                        )
                    }
                }

                // Link with Person (Needed for Lending, Borrowing, Repayments)
                if (isPersonRequired) {
                    item {
                        Text(Translation.get("person", language) + (if (isPersonRequired) " *" else ""), color = labelColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                val selectedPersonName = persons.find { it.id == selectedPersonId }?.name ?: Translation.get("select_person", language)
                                OutlinedTextField(
                                    readOnly = true,
                                    value = selectedPersonName,
                                    onValueChange = {},
                                    trailingIcon = { 
                                        IconButton(onClick = { showPersonSelector = true }) {
                                            Icon(
                                                imageVector = Icons.Rounded.ArrowDropDown,
                                                contentDescription = "Select Person",
                                                tint = labelColor
                                            )
                                        }
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textColor,
                                        unfocusedTextColor = textColor,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = labelColor
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showPersonSelector = true }
                                )
                            }
                            IconButton(
                                onClick = onAddPersonClick,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            ) {
                                Icon(Icons.Rounded.PersonAdd, contentDescription = "Add Person", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        if (selectedPersonId != null) {
                            val debtInfo = personDebts.find { it.person.id == selectedPersonId }
                            if (debtInfo != null) {
                                val netBalance = debtInfo.netBalance
                                val infoText = if (language == AppLanguage.BN) {
                                    if (netBalance > 0) {
                                        "পাওনা: ${formatCurrency(netBalance, language)}"
                                    } else if (netBalance < 0) {
                                        "দেনা: ${formatCurrency(-netBalance, language)}"
                                    } else {
                                        "কোনো দেনা-পাওনা নেই"
                                    }
                                } else {
                                    if (netBalance > 0) {
                                        "Receivable: ${formatCurrency(netBalance, language)}"
                                    } else if (netBalance < 0) {
                                        "Payable: ${formatCurrency(-netBalance, language)}"
                                    } else {
                                        "No outstanding balance"
                                    }
                                }
                                val infoColor = if (netBalance > 0) {
                                    FintechGreen
                                } else if (netBalance < 0) {
                                    FintechRed
                                } else {
                                    labelColor
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp, start = 4.dp, end = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = infoText,
                                        color = infoColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    val enteredAmount = amountInputState.toDoubleOrNull() ?: 0.0
                                    if (enteredAmount > 0.0 && netBalance != 0.0) {
                                        val netBalanceAbs = kotlin.math.abs(netBalance)
                                        val isIncreasing = if (netBalance > 0) {
                                            type == "LEND" || type == "REPAY_PAID"
                                        } else {
                                            type == "BORROW" || type == "REPAY_RECEIVED"
                                        }
                                        
                                        val finalVal = if (isIncreasing) {
                                            netBalanceAbs + enteredAmount
                                        } else {
                                            netBalanceAbs - enteredAmount
                                        }

                                        val remainingText = if (language == AppLanguage.BN) {
                                            if (isIncreasing) {
                                                "পরিবর্তিত: ${formatCurrency(finalVal, language)}"
                                            } else {
                                                if (finalVal >= 0.0) {
                                                    "অবশিষ্ট: ${formatCurrency(finalVal, language)}"
                                                } else {
                                                    "অতিরিক্ত: ${formatCurrency(-finalVal, language)}"
                                                }
                                            }
                                        } else {
                                            if (isIncreasing) {
                                                "Changed: ${formatCurrency(finalVal, language)}"
                                            } else {
                                                if (finalVal >= 0.0) {
                                                    "Remaining: ${formatCurrency(finalVal, language)}"
                                                } else {
                                                    "Surplus: ${formatCurrency(-finalVal, language)}"
                                                }
                                            }
                                        }
                                        Text(
                                            text = remainingText,
                                            color = if (isIncreasing) FintechBlue else (if (finalVal >= 0.0) labelColor else FintechBlue),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Category Selector (Hidden for debts/credits which is preset as Loan)
                if (type == "INCOME" || type == "EXPENSE") {
                    item {
                        Text(Translation.get("category", language), color = labelColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                            ExposedDropdownMenuBox(
                                expanded = categoryDropdownExpanded,
                                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                            ) {
                                val formattedCategory = if (language == AppLanguage.BN) {
                                    when (category) {
                                        "Salary" -> "বেতন"
                                        "Business" -> "ব্যবসা"
                                        "Agriculture" -> "কৃষি"
                                        "Gift" -> "উপহার"
                                        "Sales" -> "বিক্রয়"
                                        "Honorarium" -> "সম্মানী"
                                        "Food" -> "খাবার"
                                        "Housing" -> "বাসস্থান"
                                        "Transport" -> "যাতায়াত"
                                        "Shopping" -> "কেনাকাটা"
                                        "Medical" -> "চিকিৎসা"
                                        "Education" -> "শিক্ষা"
                                        "Clothing" -> "পোশাক"
                                        "Others" -> "অন্যান্য"
                                        else -> category
                                    }
                                } else {
                                    category
                                }

                                OutlinedTextField(
                                    readOnly = true,
                                    value = formattedCategory,
                                    onValueChange = {},
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = textColor,
                                        unfocusedTextColor = textColor,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = labelColor
                                    ),
                                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                                )
                                ExposedDropdownMenu(
                                    expanded = categoryDropdownExpanded,
                                    onDismissRequest = { categoryDropdownExpanded = false }
                                ) {
                                    val cats = if (type == "INCOME") categoriesIncome else categoriesExpense
                                    cats.forEach { cat ->
                                        val catLabel = if (language == AppLanguage.BN) {
                                            when (cat) {
                                                "Salary" -> "বেতন"
                                                "Business" -> "ব্যবসা"
                                                "Agriculture" -> "কৃষি"
                                                "Gift" -> "উপহার"
                                                "Sales" -> "বিক্রয়"
                                                "Honorarium" -> "সম্মানী"
                                                "Others" -> "অন্যান্য"
                                                "Food" -> "খাবার"
                                                "Housing" -> "বাসস্থান"
                                                "Transport" -> "যাতায়াত"
                                                "Shopping" -> "কেনাকাটা"
                                                "Education" -> "শিক্ষা"
                                                "Medical" -> "চিকিৎসা"
                                                "Clothing" -> "পোশাক"
                                                else -> cat
                                            }
                                        } else cat

                                        DropdownMenuItem(
                                            text = { Text(catLabel) },
                                            onClick = {
                                                category = cat
                                                categoryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Note/Description field
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(Translation.get("note", language)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = labelColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = labelColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_tx_note")
                    )
                }

                // Manual Date/Time Selection
                item {
                    Text(
                        if (language == AppLanguage.BN) "তারিখ ও সময় (ঐচ্ছিক)" else "Date & Time (Optional)",
                        color = labelColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    var showManualDatePicker by remember { mutableStateOf(false) }
                    
                    val dateLabel = if (customTimestamp != null) {
                        formatDate(customTimestamp!!, language)
                    } else {
                        if (language == AppLanguage.BN) "বর্তমান সময় (স্বয়ংক্রিয়)" else "Current Time (Automatic)"
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBg)
                            .clickable { showManualDatePicker = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text(dateLabel, color = textColor, fontSize = 14.sp)
                        }
                        if (customTimestamp != null) {
                            IconButton(onClick = { customTimestamp = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = FintechRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    
                    if (showManualDatePicker) {
                        val calendar = java.util.Calendar.getInstance()
                        if (customTimestamp != null) {
                            calendar.timeInMillis = customTimestamp!!
                        }
                        val curYear = calendar.get(java.util.Calendar.YEAR)
                        val curMonth = calendar.get(java.util.Calendar.MONTH) + 1
                        val curDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        val curHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                        val curMinute = calendar.get(java.util.Calendar.MINUTE)
                        SpecificDatePickerDialog(
                            initialYear = curYear,
                            initialMonth = curMonth,
                            initialDay = curDay,
                            initialHour = curHour,
                            initialMinute = curMinute,
                            language = language,
                            onDismiss = { showManualDatePicker = false },
                            onConfirm = { year, month, day, hour, minute ->
                                calendar.clear()
                                calendar.set(year, month - 1, day, hour, minute)
                                customTimestamp = calendar.timeInMillis
                                showManualDatePicker = false
                            }
                        )
                    }
                }

                // Confirm and Cancel buttons
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(Translation.get("cancel", language), color = labelColor)
                        }

                        Button(
                            onClick = {
                                val amount = amountInputState.toDoubleOrNull() ?: 0.0
                                if (amount <= 0) {
                                    viewModel.triggerCustomNotification(Translation.get("error_empty_amount", language), isSuccess = false, type = "ERROR")
                                } else if (isPersonRequired && selectedPersonId == null) {
                                    viewModel.triggerCustomNotification(Translation.get("error_empty_person", language), isSuccess = false, type = "ERROR")
                                } else {
                                    onConfirm(amount, type, category, note, selectedPersonId, customTimestamp ?: System.currentTimeMillis(), subType)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("btn_confirm_tx")
                        ) {
                            Text(Translation.get("confirm", language), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showPersonSelector && isPersonRequired) {
        PersonSelectorDialog(
            language = language,
            persons = persons,
            isDark = isDark,
            onDismiss = { showPersonSelector = false },
            onSelect = { person ->
                selectedPersonId = person.id
            },
            onAddPersonClick = onAddPersonClick
        )
    }
}

@Composable
fun AddPersonDialog(viewModel: com.example.ui.viewmodel.FinanceViewModel, 
    initialPerson: Person? = null,
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialPerson?.name ?: "") }
    var phone by remember { mutableStateOf(initialPerson?.phone ?: "") }
    var address by remember { mutableStateOf(initialPerson?.address ?: "") }
    var photoUri by remember { mutableStateOf(initialPerson?.photoUri ?: "") }
    val context = LocalContext.current

    val dialogBg = if (isDark) Color(0xFF121212) else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    val labelColor = if (isDark) Color.Gray else Color(0xFF64748B)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    Translation.get("add_person", language),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = textColor
                )

                val cropLauncher = rememberLauncherForActivityResult(UCropContract()) { uri ->
                    uri?.let { photoUri = it.toString() }
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri ->
                    if (uri != null) {
                        val destinationUri = Uri.fromFile(File(context.cacheDir, "person_crop_${System.currentTimeMillis()}.jpg"))
                        cropLauncher.launch(uri to destinationUri)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                        .clickable { launcher.launch("image/*") }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUri.isNotEmpty()) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = FintechBlue,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Text(
                        text = if (photoUri.isEmpty()) Translation.get("select_image", language) else Translation.get("change", language) ?: "Change",
                        color = textColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(Translation.get("name", language)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = labelColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = labelColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_person_name")
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(Translation.get("phone", language)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = labelColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = labelColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_person_phone")
                )
                
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(Translation.get("address", language)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = labelColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = labelColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_person_address")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(Translation.get("cancel", language), color = labelColor)
                    }

                    Button(
                        onClick = {
                            val trimmedPhone = phone.trim()
                            if (name.trim().isEmpty()) {
                                viewModel.triggerCustomNotification(Translation.get("enter_name", language), isSuccess = true, type = "INFO")
                            } else if (trimmedPhone.isNotEmpty() && (!trimmedPhone.all { it.isDigit() } || trimmedPhone.length != 11)) {
                                viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "সঠিক ১১ ডিজিটের ফোন নম্বর লিখুন" else "Enter a valid 11-digit phone number", isSuccess = true, type = "INFO")
                            } else {
                                onConfirm(name.trim().uppercase(), trimmedPhone, address.trim(), photoUri.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_confirm_person")
                    ) {
                        Text(Translation.get("confirm", language), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
            }
        }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsGoalDialog(viewModel: com.example.ui.viewmodel.FinanceViewModel, 
    language: AppLanguage,
    isDark: Boolean,
    initialGoal: SavingsGoal? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, Int, String) -> Unit,
    onAddCustomGradient: () -> Unit = {}
) {
    var title by remember { mutableStateOf(initialGoal?.title ?: "") }
    var cardholderName by remember { mutableStateOf(initialGoal?.cardholderName ?: "") }
    var targetStr by remember { mutableStateOf(if (initialGoal != null) initialGoal.targetAmount.toString() else "") }
    var showTargetCalculator by remember { mutableStateOf(false) }
    var sector by remember { mutableStateOf(initialGoal?.category ?: "Emergency") }
    var colorIndex by remember { mutableStateOf(initialGoal?.colorIndex ?: 0) }

    val sectors = listOf("Emergency", "Laptop", "Travel", "Marriage", "Investment", "Other")
    var sectorDropdownExpanded by remember { mutableStateOf(false) }
    var customSectorName by remember { mutableStateOf("") }
    val context = LocalContext.current

    val dialogBg = if (isDark) Color(0xFF121212) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F1724)
    val labelColor = if (isDark) Color.Gray else Color(0xFF64748B)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        if (language == AppLanguage.BN) "নতুন সঞ্চয় কার্ড" else "New Savings Card",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = textColor
                    )
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (language == AppLanguage.BN) "কার্ডের নাম" else "Card Name") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = labelColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = labelColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_saving_title")
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = cardholderName,
                        onValueChange = { cardholderName = it },
                        label = { Text(if (language == AppLanguage.BN) "কার্ডধারীর নাম" else "Cardholder Name") },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = labelColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = labelColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_saving_cardholder")
                    )
                }

                item {
                    OutlinedTextField(
                        value = targetStr,
                        onValueChange = { targetStr = it },
                        label = { Text(Translation.get("target", language)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        trailingIcon = {
                            IconButton(onClick = { showTargetCalculator = true }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Rounded.Calculate,
                                    contentDescription = "Calculator",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = labelColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = labelColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_saving_target")
                    )
                    if (showTargetCalculator) {
                        CalculatorDialog(
                            language = language,
                            isDark = isDark,
                            onDismiss = { showTargetCalculator = false },
                            onInsert = { targetStr = it }
                        )
                    }
                }

                item {
                    Text(Translation.get("category", language), color = labelColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = sectorDropdownExpanded,
                            onExpandedChange = { sectorDropdownExpanded = !sectorDropdownExpanded }
                        ) {
                            val formattedSector = if (language == AppLanguage.BN) {
                                when (sector) {
                                    "Emergency" -> "জরুরি ফান্ড"
                                    "Laptop" -> "ল্যাপটপ"
                                    "Travel" -> "ভ্রমণ"
                                    "Marriage" -> "বিয়ে"
                                    "Investment" -> "বিনিয়োগ"
                                    "Other" -> "অন্যান্য"
                                    else -> sector
                                }
                            } else {
                                sector
                            }

                            OutlinedTextField(
                                readOnly = true,
                                value = formattedSector,
                                onValueChange = {},
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectorDropdownExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = labelColor
                                ),
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                            )
                            ExposedDropdownMenu(
                                expanded = sectorDropdownExpanded,
                                onDismissRequest = { sectorDropdownExpanded = false }
                            ) {
                                sectors.forEach { sec ->
                                    val secLabel = if (language == AppLanguage.BN) {
                                        when (sec) {
                                            "Emergency" -> "জরুরি ফান্ড"
                                            "Laptop" -> "ল্যাপটপ"
                                            "Travel" -> "ভ্রমণ"
                                            "Marriage" -> "বিয়ে"
                                            "Investment" -> "বিনিয়োগ"
                                            "Other" -> "অন্যান্য"
                                            else -> sec
                                        }
                                    } else sec

                                    DropdownMenuItem(
                                        text = { Text(secLabel) },
                                        onClick = {
                                            sector = sec
                                            sectorDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (sector == "Other") {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = customSectorName,
                            onValueChange = { customSectorName = it.uppercase() },
                            label = { Text(if (language == AppLanguage.BN) "খাতের নাম লিখুন" else "Enter Sector Name") },
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = labelColor,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = labelColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // Color Picker Grid
                item {
                    val allGradientsList by viewModel.allGradientsConfig.collectAsState(initial = emptyList())
                    Text(if (language == AppLanguage.BN) "কার্ডের রঙ" else "Card Color", color = labelColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        allGradientsList.forEachIndexed { idx, grad ->
                            val isSelected = colorIndex == idx
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(grad.toBrush())
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) textColor else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { colorIndex = idx }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF2E354F) else Color(0xFFE2E8F0))
                                .clickable { onAddCustomGradient() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(androidx.compose.material.icons.Icons.Rounded.Add, contentDescription = "Add Custom Gradient", tint = textColor)
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text(Translation.get("cancel", language), color = labelColor)
                        }

                        Button(
                            onClick = {
                                val target = targetStr.toDoubleOrNull() ?: 0.0
                                val finalSector = if (sector == "Other" && customSectorName.isNotBlank()) customSectorName else sector
                                if (title.trim().isNotEmpty()) {
                                    onConfirm(title.trim().uppercase(), target, finalSector, colorIndex, cardholderName.trim().uppercase())
                                } else {
                                    viewModel.triggerCustomNotification(Translation.get("error_empty_title", language), isSuccess = false, type = "ERROR")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("btn_confirm_saving")
                        ) {
                            Text(Translation.get("confirm", language), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
            }
        }

@Composable
fun SavingsContributionDialog(viewModel: com.example.ui.viewmodel.FinanceViewModel, 
    language: AppLanguage,
    savingsGoal: SavingsGoal,
    isDark: Boolean,
    initialIsWithdraw: Boolean = false,
    txToEdit: SavingsTransaction? = null,
    onDismiss: () -> Unit,
    onConfirm: (Double, Boolean, String, Long) -> Unit
) {
    var amountStr by remember { mutableStateOf(if (txToEdit != null) txToEdit.amount.toString() else "") }
    var showSavTxCalculator by remember { mutableStateOf(false) }
    var noteStr by remember { mutableStateOf(txToEdit?.note ?: "") }
    var isWithdraw by remember { mutableStateOf(txToEdit?.let { !it.isDeposit } ?: initialIsWithdraw) }
    var customTimestamp by remember { mutableStateOf<Long?>(txToEdit?.timestamp) }
    val context = LocalContext.current

    val dialogBg = if (isDark) Color(0xFF121212) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF0F1724)
    val labelColor = if (isDark) Color.Gray else Color(0xFF64748B)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${Translation.get("add_money", language)} (${savingsGoal.title})",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = textColor
                )

                // Deposit / Withdraw Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isWithdraw) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .border(1.dp, if (!isWithdraw) Color.Transparent else labelColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable { isWithdraw = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(Translation.get("deposit", language), color = if (!isWithdraw) Color.White else textColor)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isWithdraw) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .border(1.dp, if (isWithdraw) Color.Transparent else labelColor.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable { isWithdraw = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(Translation.get("withdraw", language), color = if (isWithdraw) Color.White else textColor)
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text(Translation.get("amount", language)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = {
                        IconButton(onClick = { showSavTxCalculator = true }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Rounded.Calculate,
                                contentDescription = "Calculator",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = labelColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = labelColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_contribution_amount")
                )
                if (showSavTxCalculator) {
                    CalculatorDialog(
                        language = language,
                        isDark = isDark,
                        onDismiss = { showSavTxCalculator = false },
                        onInsert = { amountStr = it }
                    )
                }

                OutlinedTextField(
                    value = noteStr,
                    onValueChange = { noteStr = it },
                    label = { Text(if (language == AppLanguage.BN) "মন্তব্য যোগ করুন" else "Add Comment") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = labelColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = labelColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                )

                // Date & Time Selector (Custom time) for savings contribution
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        if (language == AppLanguage.BN) "তারিখ ও সময় (ঐচ্ছিক)" else "Date & Time (Optional)",
                        color = labelColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    var showManualDatePicker by remember { mutableStateOf(false) }
                    
                    val dateLabel = if (customTimestamp != null) {
                        formatDate(customTimestamp!!, language)
                    } else {
                        if (language == AppLanguage.BN) "বর্তমান সময় (স্বয়ংক্রিয়)" else "Current Time (Automatic)"
                    }
                    
                    val chipBg = if (isDark) Color(0xFF2D2D30) else Color(0xFFF3F4F6)
                    val inputTextColor = if (isDark) Color.White else Color.Black
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(chipBg)
                            .clickable { showManualDatePicker = true }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text(dateLabel, color = inputTextColor, fontSize = 14.sp)
                        }
                        if (customTimestamp != null) {
                            IconButton(onClick = { customTimestamp = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = FintechRed, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    
                    if (showManualDatePicker) {
                        val calendar = java.util.Calendar.getInstance()
                        if (customTimestamp != null) {
                            calendar.timeInMillis = customTimestamp!!
                        }
                        val curYear = calendar.get(java.util.Calendar.YEAR)
                        val curMonth = calendar.get(java.util.Calendar.MONTH) + 1
                        val curDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                        val curHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                        val curMinute = calendar.get(java.util.Calendar.MINUTE)
                        SpecificDatePickerDialog(
                            initialYear = curYear,
                            initialMonth = curMonth,
                            initialDay = curDay,
                            initialHour = curHour,
                            initialMinute = curMinute,
                            language = language,
                            onDismiss = { showManualDatePicker = false },
                            onConfirm = { year, month, day, hour, minute ->
                                calendar.clear()
                                calendar.set(year, month - 1, day, hour, minute)
                                customTimestamp = calendar.timeInMillis
                                showManualDatePicker = false
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(Translation.get("cancel", language), color = labelColor)
                    }

                    Button(
                        onClick = {
                            val amount = amountStr.toDoubleOrNull() ?: 0.0
                            if (amount > 0) {
                                if (isWithdraw && amount > savingsGoal.savedAmount) {
                                    viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "পর্যাপ্ত ব্যালেন্স নেই!" else "Insufficient balance!", isSuccess = false, type = "ERROR")
                                } else {
                                    onConfirm(amount, isWithdraw, noteStr, customTimestamp ?: System.currentTimeMillis())
                                }
                            } else {
                                viewModel.triggerCustomNotification(Translation.get("error_empty_amount", language), isSuccess = false, type = "ERROR")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_confirm_contribution")
                    ) {
                        Text(Translation.get("confirm", language), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
            }
        }

// ---------------- SAVINGS GOAL DETAIL OVERLAY ----------------
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SavingsGoalDetailOverlay(
    language: AppLanguage,
    isDark: Boolean,
    profileName: String,
    allGradientsList: List<com.example.ui.theme.ThemeGradient>,
    goal: SavingsGoal,
    transactionsFlow: kotlinx.coroutines.flow.Flow<List<SavingsTransaction>>,
    onDismiss: () -> Unit,
    onDeleteGoal: (Int) -> Unit,
    onEditGoal: (SavingsGoal) -> Unit,
    onContributeClick: (SavingsGoal, Boolean) -> Unit,
    onDeleteTx: (SavingsTransaction) -> Unit,
    onEditTx: (SavingsTransaction) -> Unit,
    onMoveGoal: (SavingsGoal) -> Unit = {}
) {
    val context = LocalContext.current
    val txList by transactionsFlow.collectAsState(initial = emptyList())
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var selectedSavingsTx by remember { mutableStateOf<SavingsTransaction?>(null) }
    var txToDelete by remember { mutableStateOf<SavingsTransaction?>(null) }
    var selectedSavingsTxIds by remember { mutableStateOf(setOf<Int>()) }
    val isSelectionMode = selectedSavingsTxIds.isNotEmpty()

    androidx.activity.compose.BackHandler(enabled = isSelectionMode) {
        selectedSavingsTxIds = emptySet()
    }
    var showMultiDeleteConfirm by remember { mutableStateOf(false) }
    
    if (txToDelete != null) {
        DeleteVerificationDialog(
            language = language,
            onConfirm = {
                onDeleteTx(txToDelete!!)
                txToDelete = null
            },
            onDismiss = { txToDelete = null }
        )
    }

    if (showMultiDeleteConfirm) {
        DeleteVerificationDialog(
            language = language,
            onConfirm = {
                selectedSavingsTxIds.forEach { id ->
                    val tx = txList.find { it.id == id }
                    if (tx != null) {
                        onDeleteTx(tx)
                    }
                }
                selectedSavingsTxIds = emptySet()
                showMultiDeleteConfirm = false
            },
            onDismiss = { showMultiDeleteConfirm = false }
        )
    }

    if (showDeleteConfirm) {
        DeleteVerificationDialog(
            language = language,
            onConfirm = {
                onDeleteGoal(goal.id)
                showDeleteConfirm = false
                onDismiss()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("savings_detail_overlay"),
        color = if (isDark) Color(0xFF000000) else Color(0xFFF3F4F6)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSelectionMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "${selectedSavingsTxIds.size}টি নির্বাচিত" else "${selectedSavingsTxIds.size} Selected",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FintechBlue
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showMultiDeleteConfirm = true }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "Delete Selected", tint = FintechRed)
                            }
                            IconButton(onClick = { selectedSavingsTxIds = emptySet() }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear Selection", tint = if (isDark) Color.White else Color.Black)
                            }
                        }
                    }
                } else {
                    Text(
                        text = if (language == AppLanguage.BN) "সঞ্চয় কার্ডের বিস্তারিত" else "Savings Card Details",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FintechBlue,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    Row(
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        IconButton(onClick = { onEditGoal(goal) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = Color.Gray)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = FintechRed)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = if (isDark) Color.White else Color.Black)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            // Show Card at top
            SavingsGoalCardItem(
                goal = goal,
                language = language,
                isDark = isDark,
                profileName = profileName,
                allGradientsList = allGradientsList,
                onGoalClick = {}, // No click action here
                onContributeClick = { _, _ -> },
                onEditGoal = {},
                maskBalance = false
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Deposit/Withdraw Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onContributeClick(goal, false) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechGreen)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (language == AppLanguage.BN) "জমা করুন" else "Deposit", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onContributeClick(goal, true) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechRed)
                ) {
                    Icon(Icons.Rounded.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (language == AppLanguage.BN) "উত্তোলন" else "Withdraw", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // History Label
            Text(
                text = Translation.get("history", language),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF0F1724),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Transactions List
            if (txList.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.6f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(if (language == AppLanguage.BN) "কোনো লেনদেন নেই" else "No transactions yet", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(txList, key = { it.id }) { tx ->
                        val visibleState = remember { androidx.compose.animation.core.MutableTransitionState(false).apply { targetState = true } }
                        androidx.compose.animation.AnimatedVisibility(
                            visibleState = visibleState,
                            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(
                                initialOffsetY = { 40 }
                            ),
                            modifier = Modifier.animateItem()
                        ) {
                            val isDeposit = tx.isDeposit
                        val amountColor = if (isDeposit) FintechGreen else FintechRed
                        val txIcon = if (isDeposit) Icons.AutoMirrored.Rounded.TrendingUp else Icons.AutoMirrored.Rounded.TrendingDown
                        val txLabel = if (isDeposit) Translation.get("deposit", language) else Translation.get("withdraw", language)

                        val isSelected = selectedSavingsTxIds.contains(tx.id)
                        val bgColor by androidx.compose.animation.animateColorAsState(if (isDark) Color(0xFF121212) else Color.White)
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) FintechBlue else (if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            selectedSavingsTxIds = if (isSelected) {
                                                selectedSavingsTxIds - tx.id
                                            } else {
                                                selectedSavingsTxIds + tx.id
                                            }
                                        } else {
                                            selectedSavingsTx = tx
                                        }
                                    },
                                    onLongClick = {
                                        selectedSavingsTxIds = if (isSelected) {
                                            selectedSavingsTxIds - tx.id
                                        } else {
                                            selectedSavingsTxIds + tx.id
                                        }
                                    }
                                )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(amountColor.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = txIcon,
                                            contentDescription = null,
                                            tint = amountColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = txLabel,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White else Color(0xFF0F1724),
                                            fontSize = 14.sp
                                        )
                                        val d = java.util.Date(tx.timestamp)
                                        val f = java.text.SimpleDateFormat("dd MMM, yyyy", java.util.Locale.getDefault())
                                        Text(
                                            text = f.format(d),
                                            color = Color.Gray,
                                            fontSize = 12.sp
                                        )
                                        if (tx.note.isNotEmpty()) {
                                            Text(
                                                text = tx.note,
                                                color = Color.Gray,
                                                fontSize = 12.sp,
                                                fontStyle = FontStyle.Italic
                                            )
                                        }
                                    }
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = (if (isDeposit) "+" else "-") + formatCurrency(tx.amount, language),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = amountColor,
                                        fontSize = 15.sp
                                    )
                                    
                                    if (!isSelectionMode) {
                                        IconButton(
                                            onClick = { txToDelete = tx },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Delete,
                                                contentDescription = "Delete",
                                                tint = FintechRed.copy(alpha = 0.8f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    } else {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                selectedSavingsTxIds = if (checked == true) {
                                                    selectedSavingsTxIds + tx.id
                                                } else {
                                                    selectedSavingsTxIds - tx.id
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = FintechBlue)
                                        )
                                    }
                                }
                            }
                        }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(110.dp)) }
                }
            }
        }
    }

    if (selectedSavingsTx != null) {
        val tx = selectedSavingsTx!!
        SavingsTransactionDetailsDialog(
            tx = tx,
            goalName = goal.title,
            language = language,
            isDark = isDark,
            onDismiss = { selectedSavingsTx = null },
            onDelete = {
                txToDelete = tx
                selectedSavingsTx = null
            },
            onEdit = {
                onEditTx(tx)
                selectedSavingsTx = null
            },
            onShareText = {
                val shareText = buildString {
                    append(if (language == AppLanguage.BN) "সঞ্চয় ট্রানজ্যাকশন মেমো\n" else "Savings Transaction Memo\n")
                    append("-------------------\n")
                    val typeStr = if (tx.isDeposit) (if (language == AppLanguage.BN) "জমা" else "Deposit") else (if (language == AppLanguage.BN) "উত্তোলন" else "Withdraw")
                    append(if (language == AppLanguage.BN) "ধরণ: " else "Type: ").append(typeStr).append("\n")
                    append(if (language == AppLanguage.BN) "পরিমাণ: " else "Amount: ").append(formatCurrency(tx.amount, language)).append("\n")
                    append(if (language == AppLanguage.BN) "লক্ষ্য: " else "Goal: ").append(goal.title).append("\n")
                    if (tx.note.isNotBlank()) {
                        append(if (language == AppLanguage.BN) "নোট: " else "Note: ").append(tx.note).append("\n")
                    }
                    append(if (language == AppLanguage.BN) "তারিখ: " else "Date: ").append(formatDate(tx.timestamp, language)).append("\n")
                    append("-------------------\n")
                    append(if (language == AppLanguage.BN) "ফাইন্যান্স নোট থেকে শেয়ার করা হয়েছে" else "Shared via Finance Note")
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, if (language == AppLanguage.BN) "শেয়ার করুন" else "Share via"))
            },
            onShareImage = { bitmap ->
                shareBitmap(context, bitmap, if (language == AppLanguage.BN) "ফাইন্যান্স নোট থেকে সঞ্চয় মেমো" else "Savings Memo from Finance Note")
            }
        )
    }
}

@Composable
fun SavingsTransactionDetailsDialog(
    tx: SavingsTransaction,
    goalName: String,
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onShareText: () -> Unit,
    onShareImage: (android.graphics.Bitmap) -> Unit
) {
    val bgColor = if (isDark) Color(0xFF121212) else Color.White
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val subtitleColor = if (isDark) Color.LightGray else Color(0xFF64748B)

    var showShareOptions by remember { mutableStateOf(false) }
    val screenshotState = rememberScreenshotState()

    if (showShareOptions) {
        AlertDialog(
            onDismissRequest = { showShareOptions = false },
            title = { Text(if (language == AppLanguage.BN) "কিভাবে শেয়ার করবেন?" else "How to share?") },
            text = {
                Column {
                    TextButton(onClick = { 
                        showShareOptions = false
                        onShareText()
                    }) {
                        Text(if (language == AppLanguage.BN) "টেক্সট হিসেবে" else "As Text", color = FintechBlue)
                    }
                    TextButton(onClick = {
                        showShareOptions = false
                        screenshotState.createBitmap()?.let { onShareImage(it) }
                    }) {
                        Text(if (language == AppLanguage.BN) "ছবি হিসেবে (স্ক্রিনশট)" else "As Image (Screenshot)", color = FintechBlue)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showShareOptions = false }) {
                    Text(if (language == AppLanguage.BN) "বাতিল" else "Cancel", color = FintechRed)
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (language == AppLanguage.BN) "ট্রানজ্যাকশন মেমো" else "Transaction Memo",
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .captureToPicture(screenshotState)
                    .background(if (isDark) Color(0xFF121212) else Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Detail Row Helper
                val DetailRow = @Composable { label: String, value: String ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, color = subtitleColor, fontSize = 13.sp)
                        Text(text = value, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }

                val typeStr = if (tx.isDeposit) {
                    if (language == AppLanguage.BN) "জমা" else "Deposit"
                } else {
                    if (language == AppLanguage.BN) "উত্তোলন" else "Withdraw"
                }
                
                DetailRow(if (language == AppLanguage.BN) "ধরণ:" else "Type:", typeStr)
                DetailRow(if (language == AppLanguage.BN) "পরিমাণ:" else "Amount:", formatCurrency(tx.amount, language))
                DetailRow(if (language == AppLanguage.BN) "লক্ষ্য:" else "Goal:", goalName)
                DetailRow(if (language == AppLanguage.BN) "তারিখ:" else "Date:", formatDate(tx.timestamp, language))
                
                if (tx.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = subtitleColor.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = if (language == AppLanguage.BN) "নোট:" else "Note:", color = subtitleColor, fontSize = 13.sp)
                    Text(text = tx.note, color = textColor, fontSize = 14.sp)
                }
            }
        },
        containerColor = bgColor,
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = FintechBlue)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = FintechRed)
                    }
                    IconButton(onClick = { showShareOptions = true }) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share", tint = FintechBlue)
                    }
                }
                
                TextButton(onClick = onDismiss) {
                    Text(if (language == AppLanguage.BN) "বন্ধ করুন" else "Close", color = FintechBlue)
                }
            }
        }
    )
}

// ---------------- PERSON DETAIL FULL OVERLAY ----------------
@Composable
fun PersonDetailOverlay(
    language: AppLanguage,
    isDark: Boolean,
    personDebt: PersonDebt,
    transactionsFlow: kotlinx.coroutines.flow.Flow<List<Transaction>>,
    onDismiss: () -> Unit,
    onLendClick: (Double, String, Long, String) -> Unit,
    onBorrowClick: (Double, String, Long, String) -> Unit,
    onRepayPaidClick: (Double, String, Long) -> Unit,
    onRepayReceivedClick: (Double, String, Long) -> Unit,
    onDeleteTx: (Int) -> Unit,
    onEditTx: (Transaction) -> Unit,
    onEditPerson: (Person) -> Unit,
    onDeletePerson: () -> Unit
) {
    val txList by transactionsFlow.collectAsState(initial = emptyList())
    var showActionSheet by remember { mutableStateOf<String?>(null) } // "LEND", "BORROW", "REPAY_PAID", "REPAY_RECEIVED"
    var actionAmountStr by remember { mutableStateOf("") }
    var showActionCalculator by remember { mutableStateOf(false) }
    var actionNoteStr by remember { mutableStateOf("") }
    var actionSubType by remember(showActionSheet) { mutableStateOf("CASH") }
    var customActionTimestamp by remember(showActionSheet) { mutableStateOf<Long?>(null) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("person_detail_overlay"),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Header - Simplified (removed Close button to reduce empty space as requested by user)
            Spacer(modifier = Modifier.height(4.dp))

            // Person Bio Card (Frosted Card)
            FrostedGlassCard(
                isDark = isDark,
                padding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (personDebt.person.photoUri.isNotEmpty()) {
                            AsyncImage(
                                model = personDebt.person.photoUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = personDebt.person.name.take(1).uppercase(),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = personDebt.person.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color.White else Color(0xFF1E222F)
                        )
                        if (personDebt.person.phone.isNotEmpty()) {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${personDebt.person.phone}"))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Rounded.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = personDebt.person.phone,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                                )
                            }
                        }
                        if (personDebt.person.address.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                                Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = personDebt.person.address,
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                    
                    Row {
                        IconButton(onClick = { onEditPerson(personDebt.person) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit", tint = Color.Gray)
                        }
                        IconButton(onClick = { onDeletePerson() }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = FintechRed)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Net Balance Summary Box inside Card
                val (statusText, statusColor, absoluteAmount) = when {
                    personDebt.netBalance > 0 -> Triple(Translation.get("you_get", language), FintechGreen, personDebt.netBalance)
                    personDebt.netBalance < 0 -> Triple(Translation.get("you_owe", language), FintechRed, -personDebt.netBalance)
                    else -> Triple(Translation.get("settled", language), Color.Gray, 0.0)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDark) Color(0xFF121212) else Color(0xFFF3F4F6))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Translation.get("net_status", language),
                        color = Color.Gray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = statusText,
                            fontSize = 10.sp,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatCurrency(absoluteAmount, language),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Grid (Lend / Borrow / Repay)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showActionSheet = "LEND" },
                        colors = ButtonDefaults.buttonColors(containerColor = FintechGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(Translation.get("tx_type_lend", language).split(" ").take(2).joinToString(" "), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showActionSheet = "BORROW" },
                        colors = ButtonDefaults.buttonColors(containerColor = FintechRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(Translation.get("tx_type_borrow", language).split(" ").take(2).joinToString(" "), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showActionSheet = "REPAY_RECEIVED" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(if (language == AppLanguage.BN) "ফেরত পেলাম" else Translation.get("tx_type_repay_received", language).split(" ").take(2).joinToString(" "), color = if (isDark) Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showActionSheet = "REPAY_PAID" },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Text(if (language == AppLanguage.BN) "ফেরত দিলাম" else Translation.get("tx_type_repay_paid", language).split(" ").take(2).joinToString(" "), color = if (isDark) Color.White else Color.DarkGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // History Label
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${personDebt.person.name} ${Translation.get("history_with", language)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.DarkGray
                )
                
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = if (isDark) Color.White else Color.DarkGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Dynamic Action sheet overlay / Dialog
            if (showActionSheet != null) {
                val actType = showActionSheet!!
                val labelKey = when (actType) {
                    "LEND" -> "tx_type_lend"
                    "BORROW" -> "tx_type_borrow"
                    "REPAY_PAID" -> "tx_type_repay_paid"
                    else -> "tx_type_repay_received"
                }

                val overlayTextColor = if (isDark) Color.White else Color.Black
                val overlayLabelColor = if (isDark) Color.Gray else Color(0xFF64748B)

                Dialog(onDismissRequest = { showActionSheet = null }) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = Translation.get(labelKey, language),
                                fontWeight = FontWeight.ExtraBold,
                                color = overlayTextColor,
                                fontSize = 16.sp
                            )

                            OutlinedTextField(
                                value = actionAmountStr,
                                onValueChange = { actionAmountStr = it },
                                label = { Text(Translation.get("amount", language)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                trailingIcon = {
                                    IconButton(onClick = { showActionCalculator = true }) {
                                        Icon(
                                            imageVector = androidx.compose.material.icons.Icons.Rounded.Calculate,
                                            contentDescription = "Calculator",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = overlayLabelColor,
                                    focusedTextColor = overlayTextColor,
                                    unfocusedTextColor = overlayTextColor,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = overlayLabelColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (showActionCalculator) {
                                CalculatorDialog(
                                    language = language,
                                    isDark = isDark,
                                    onDismiss = { showActionCalculator = false },
                                    onInsert = { actionAmountStr = it }
                                )
                            }

                            OutlinedTextField(
                                value = actionNoteStr,
                                onValueChange = { actionNoteStr = it },
                                label = { Text(if (language == AppLanguage.BN) "মন্তব্য যোগ করুন" else "Add Comment") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = overlayLabelColor,
                                    focusedTextColor = overlayTextColor,
                                    unfocusedTextColor = overlayTextColor,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = overlayLabelColor
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Sub-type selector (LEND/BORROW specific)
                            if (actType == "LEND" || actType == "BORROW") {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        if (language == AppLanguage.BN) "লেনদেনের ধরন" else "Transaction Detail",
                                        color = overlayLabelColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.align(Alignment.Start)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val options = if (actType == "LEND") {
                                            listOf(
                                                Triple("CASH", if (language == AppLanguage.BN) "নগদ ধার" else "Cash Loan", FintechBlue),
                                                Triple("CREDIT", if (language == AppLanguage.BN) "বাকি বিক্রয়" else "Credit Sale", FintechRed)
                                            )
                                        } else {
                                            listOf(
                                                Triple("CASH", if (language == AppLanguage.BN) "নগদ ধার" else "Cash Loan", FintechBlue),
                                                Triple("CREDIT", if (language == AppLanguage.BN) "বাকি ক্রয়" else "Credit Purchase", FintechRed)
                                            )
                                        }

                                        options.forEach { (sValue, sLabel, sColor) ->
                                            val isSelected = actionSubType == sValue
                                            val chipBg = if (isDark) Color(0xFF2D2D30) else Color(0xFFF3F4F6)
                                            val textColor = if (isDark) Color.White else Color.Black
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) sColor else chipBg)
                                                    .clickable { actionSubType = sValue }
                                                    .padding(vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = sLabel,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else textColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Date & Time Selector (Custom time)
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    if (language == AppLanguage.BN) "তারিখ ও সময় (ঐচ্ছিক)" else "Date & Time (Optional)",
                                    color = overlayLabelColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Start)
                                )
                                
                                var showManualDatePicker by remember { mutableStateOf(false) }
                                
                                val dateLabel = if (customActionTimestamp != null) {
                                    formatDate(customActionTimestamp!!, language)
                                } else {
                                    if (language == AppLanguage.BN) "বর্তমান সময় (স্বয়ংক্রিয়)" else "Current Time (Automatic)"
                                }
                                
                                val chipBg = if (isDark) Color(0xFF2D2D30) else Color(0xFFF3F4F6)
                                val textColor = if (isDark) Color.White else Color.Black
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(chipBg)
                                        .clickable { showManualDatePicker = true }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Rounded.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        Text(dateLabel, color = textColor, fontSize = 14.sp)
                                    }
                                    if (customActionTimestamp != null) {
                                        IconButton(onClick = { customActionTimestamp = null }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = FintechRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                
                                if (showManualDatePicker) {
                                    val calendar = java.util.Calendar.getInstance()
                                    if (customActionTimestamp != null) {
                                        calendar.timeInMillis = customActionTimestamp!!
                                    }
                                    val curYear = calendar.get(java.util.Calendar.YEAR)
                                    val curMonth = calendar.get(java.util.Calendar.MONTH) + 1
                                    val curDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                    val curHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                                    val curMinute = calendar.get(java.util.Calendar.MINUTE)
                                    SpecificDatePickerDialog(
                                        initialYear = curYear,
                                        initialMonth = curMonth,
                                        initialDay = curDay,
                                        initialHour = curHour,
                                        initialMinute = curMinute,
                                        language = language,
                                        onDismiss = { showManualDatePicker = false },
                                        onConfirm = { year, month, day, hour, minute ->
                                            calendar.clear()
                                            calendar.set(year, month - 1, day, hour, minute)
                                            customActionTimestamp = calendar.timeInMillis
                                            showManualDatePicker = false
                                        }
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                TextButton(onClick = { 
                                    showActionSheet = null
                                    actionNoteStr = ""
                                }, modifier = Modifier.weight(1f)) {
                                    Text(Translation.get("cancel", language), color = Color.Gray)
                                }
                                Button(
                                    onClick = {
                                        val amt = actionAmountStr.toDoubleOrNull() ?: 0.0
                                        if (amt > 0) {
                                            val finalTimestamp = customActionTimestamp ?: System.currentTimeMillis()
                                            when (actType) {
                                                "LEND" -> onLendClick(amt, actionNoteStr, finalTimestamp, actionSubType)
                                                "BORROW" -> onBorrowClick(amt, actionNoteStr, finalTimestamp, actionSubType)
                                                "REPAY_PAID" -> onRepayPaidClick(amt, actionNoteStr, finalTimestamp)
                                                "REPAY_RECEIVED" -> onRepayReceivedClick(amt, actionNoteStr, finalTimestamp)
                                            }
                                            actionAmountStr = ""
                                            actionNoteStr = ""
                                            showActionSheet = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.weight(1.5f)
                                ) {
                                    Text(Translation.get("confirm", language), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Transaction history list for this person
            if (txList.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(64.dp)
                            .padding(bottom = 16.dp)
                    )
                    Text(Translation.get("no_tx", language), color = Color.Gray)
                }
            } else {
                val grouped = txList.sortedByDescending { it.timestamp }.groupBy { formatDateToDay(it.timestamp) }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    grouped.forEach { (date, txs) ->
                        item {
                            Text(
                                text = formatDateHeader(date, language),
                                color = Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                            )
                        }
                        items(txs, key = { it.id }) { tx ->
                            val visibleState = remember { androidx.compose.animation.core.MutableTransitionState(false).apply { targetState = true } }
                            androidx.compose.animation.AnimatedVisibility(
                                visibleState = visibleState,
                                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically(
                                    initialOffsetY = { 40 }
                                ),
                                modifier = Modifier.animateItem()
                            ) {
                                TransactionRowItem(tx, language, isDark, listOf(personDebt.person), onDeleteTx, onEditTx)
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(110.dp)) }
                }
            }
        }
            }
        }

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FintechGradientCard(
        gradientColors = if (isDark) listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E)) else activeThemeGradient,
        cornerRadius = 24.dp,
        onClick = onClick,
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
            }
        }

@Composable
fun SettingItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    isDark: Boolean,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconColor.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) Color.White else Color(0xFF1E293B)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = if (isDark) Color.Gray else Color(0xFF64748B)
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun SettingCategory(
    title: String,
    isDark: Boolean,
    icon: ImageVector? = null,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = FintechBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF1E293B)
                )
            }
            Icon(
                imageVector = if (isExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = if (isDark) Color.LightGray else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDark) Color(0xFF121212) else Color.White)
                    .border(
                        width = 1.dp,
                        color = if (isDark) Color(0xFF262626) else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column {
                    content()
                }
            }
        }
            }
        }

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    language: AppLanguage,
    isDark: Boolean,
    filter: String = "",
    onShowUpdatePopup: () -> Unit,
    onBack: () -> Unit,
    onSignInClick: () -> Unit,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    showLogoutConfirm: Boolean,
    onLogoutClick: () -> Unit,
    onLogoutConfirmDismiss: () -> Unit
) {
    val context = LocalContext.current
    val localCoroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.verifyNotificationState(context)
    }

    val rawProfileName by viewModel.profileName.collectAsState()
    val rawProfileEmail by viewModel.profileEmail.collectAsState()
    val rawProfilePhotoUri by viewModel.profilePhotoUri.collectAsState()
    val profilePhone by viewModel.profilePhone.collectAsState()
    val profileSocial by viewModel.profileSocial.collectAsState()
    val profileAddress by viewModel.profileAddress.collectAsState()

    val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsState()
    val googleName by viewModel.googleName.collectAsState()
    val googleEmail by viewModel.googleEmail.collectAsState()
    val googlePhotoUrl by viewModel.googlePhotoUrl.collectAsState()
    val driveStatusMessage by viewModel.driveStatusMessage.collectAsState()
    val lastGDriveBackupTime by viewModel.lastGDriveBackupTime.collectAsState()
    val autoBackupIntervalDays by viewModel.autoBackupIntervalDays.collectAsState()

    val profileName = rawProfileName.ifBlank { (if (isGoogleSignedIn) googleName else null) ?: "" }
    val profileEmail = rawProfileEmail.ifBlank { (if (isGoogleSignedIn) googleEmail else null) ?: "" }
    val profilePhotoUri = rawProfilePhotoUri ?: (if (isGoogleSignedIn) googlePhotoUrl else null)

    var isSignoutBackupActive by remember { mutableStateOf(false) }
    var pendingLocalBackupComment by remember { mutableStateOf("") }
    var pendingLocalWorkspaceIds by remember { mutableStateOf<List<String>?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.let { outputStream ->
                viewModel.exportBackupToUri(context, outputStream, pendingLocalBackupComment, pendingLocalWorkspaceIds, {
                    viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "ব্যাকআপ সফলভাবে সেভ হয়েছে!" else "Backup successfully saved!", isSuccess = true, type = "SUCCESS")
                    pendingLocalBackupComment = ""
                    pendingLocalWorkspaceIds = null
                    if (isSignoutBackupActive) {
                        viewModel.performAutoBackupAndSignOut(context, profileName) {
                            isSignoutBackupActive = false
                            viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "গুগল ড্রাইভ থেকে লগআউট সফল হয়েছে!" else "Logged out from Google Drive successfully!", isSuccess = true, type = "SUCCESS")
                            viewModel.triggerCustomNotification(
                                if (language == AppLanguage.BN) "আপনার অ্যাকাউন্ট থেকে সফলভাবে লগআউট করা হয়েছে।" else "Successfully logged out of your account.",
                                isSuccess = true,
                                type = "SIGN_OUT"
                            )
                        }
                    }
                }, { error ->
                    viewModel.triggerCustomNotification("Error: $error", isSuccess = false, type = "ERROR")
                    if (isSignoutBackupActive) {
                        viewModel.performAutoBackupAndSignOut(context, profileName) {
                            isSignoutBackupActive = false
                            viewModel.triggerCustomNotification(
                                if (language == AppLanguage.BN) "আপনার অ্যাকাউন্ট থেকে সফলভাবে লগআউট করা হয়েছে।" else "Successfully logged out of your account.",
                                isSuccess = true,
                                type = "SIGN_OUT"
                            )
                        }
                    }
                })
            }
        } else {
            if (isSignoutBackupActive) {
                viewModel.performAutoBackupAndSignOut(context, profileName) {
                    isSignoutBackupActive = false
                    viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "গুগল ড্রাইভ থেকে লগআউট সফল হয়েছে!" else "Logged out from Google Drive successfully!", isSuccess = true, type = "SUCCESS")
                    viewModel.triggerCustomNotification(
                        if (language == AppLanguage.BN) "আপনার অ্যাকাউন্ট থেকে সফলভাবে লগআউট করা হয়েছে।" else "Successfully logged out of your account.",
                        isSuccess = true,
                        type = "SIGN_OUT"
                    )
                }
            }
        }
    }

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showErrorLogDialog by remember { mutableStateOf(false) }
    var currentLogsText by remember { mutableStateOf("") }
    var autoBackupDropdownExpanded by remember { mutableStateOf(false) }
    var showTrashDialog by remember { mutableStateOf(false) }

    if (showTrashDialog) {
        TrashDialog(
            viewModel = viewModel,
            language = language,
            isDarkTheme = isDark,
            onDismiss = { showTrashDialog = false }
        )
    }
    
    val updateInfo by viewModel.updateManager.updateInfo.collectAsState()
    val isCheckingForUpdate by viewModel.updateManager.isChecking.collectAsState()

    val currentWorkspace by viewModel.currentWorkspace.collectAsState(initial = com.example.data.Workspace(id = "default", name = "ব্যক্তিগত"))
    var showWorkspaceDialog by remember { mutableStateOf(false) }
    val workspaceStatsList by viewModel.workspaceStatsList.collectAsState(initial = emptyList())

    if (showWorkspaceDialog) {
        WorkspaceManagementDialog(
            language = language,
            isDark = isDark,
            workspaces = workspaceStatsList,
            currentWorkspace = currentWorkspace,
            onSelect = { workspaceId ->
                viewModel.selectWorkspace(workspaceId)
                showWorkspaceDialog = false
            },
            onCreate = { name ->
                viewModel.createWorkspace(name)
            },
            onEdit = { id, name ->
                viewModel.editWorkspace(id, name)
            },
            onUpdatePhoto = { id, photo ->
                viewModel.updateWorkspaceProfilePhoto(id, photo)
            },
            onDelete = { workspaceId ->
                viewModel.deleteWorkspace(workspaceId)
            },
            onDismiss = { showWorkspaceDialog = false }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.initGoogleDrive(context)
    }

    var nameInput by remember(profileName) { mutableStateOf(profileName) }
    var emailInput by remember(profileEmail) { mutableStateOf(profileEmail) }
    var photoUriInput by remember(profilePhotoUri) { mutableStateOf(profilePhotoUri) }
    var phoneInput by remember(profilePhone) { mutableStateOf(profilePhone) }
    var socialInput by remember(profileSocial) { mutableStateOf(profileSocial) }
    var addressInput by remember(profileAddress) { mutableStateOf(profileAddress) }

    val cropLauncher = rememberLauncherForActivityResult(UCropContract()) { uri ->
        uri?.let {
            photoUriInput = it.toString()
        }
    }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val destinationUri = Uri.fromFile(File(context.cacheDir, "profile_crop_${System.currentTimeMillis()}.jpg"))
            cropLauncher.launch(it to destinationUri)
        }
    }

    var pasteJsonInput by remember { mutableStateOf("") }
    var showPasteArea by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF000000) else Color(0xFFF1F5F9))
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 0. WORKSPACE SELECTOR CARD ---
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF121212) else Color.White
            ),
            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showWorkspaceDialog = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FintechBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SpaceDashboard,
                            contentDescription = null,
                            tint = FintechBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (language == AppLanguage.BN) "অ্যাক্টিভ ওয়ার্কস্পেস" else "Active Workspace",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = currentWorkspace.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E222F)
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "পরিবর্তন করুন" else "Change",
                        fontSize = 12.sp,
                        color = FintechBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = FintechBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // --- 2. DARK MODE CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "অ্যাপ থিম কাস্টমাইজেশন" else "App Theme Customization",
            isDark = isDark,
            icon = Icons.Rounded.Palette,
            initiallyExpanded = false
        ) {
            val selectedThemeIndex by viewModel.selectedThemeGradientIndex.collectAsState()
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFEAB308).copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WbSunny,
                            contentDescription = null,
                            tint = Color(0xFFEAB308),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.BN) "ডার্ক মোড" else "Dark Mode",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        Text(
                            text = if (language == AppLanguage.BN) "চোখের সুরক্ষায় ডার্ক থিম সক্রিয় করুন" else "Activate dark theme for eye protection",
                            fontSize = 12.sp,
                            color = if (isDark) Color.Gray else Color(0xFF64748B)
                        )
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleTheme(context) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = FintechBlue,
                            uncheckedThumbColor = if (isDark) Color.Gray else Color.White,
                            uncheckedTrackColor = if (isDark) Color(0xFF2A2E42) else Color(0xFFE2E8F0)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))
                
                Text(
                    text = if (language == AppLanguage.BN) "থিম কালার স্কীম" else "Theme Color Scheme",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else Color(0xFF1E293B)
                )
                Text(
                    text = if (language == AppLanguage.BN) "অ্যাপের ড্যাশবোর্ড, টাইটেল বার, বাটনসহ সম্পূর্ণ থিম কালার কাস্টমাইজ করুন" else "Customize the primary color theme of dashboard, cards, headers and buttons",
                    fontSize = 11.sp,
                    color = if (isDark) Color.Gray else Color(0xFF64748B)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GradientsList.forEachIndexed { idx, grad ->
                        val isSelected = selectedThemeIndex == idx
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(grad))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) (if (isDark) Color.White else Color(0xFF1E293B)) else (if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)),
                                    shape = CircleShape
                                )
                                .clickable {
                                    viewModel.selectThemeGradientIndex(context, idx)
                                }
                        )
                    }
                }
            }
        }

        // --- 3. LANGUAGE CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "ভাষা" else "Language",
            isDark = isDark,
            icon = Icons.Rounded.Translate,
            initiallyExpanded = false
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(FintechBlue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Translate,
                        contentDescription = null,
                        tint = FintechBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.BN) "ভাষা পরিবর্তন করুন" else "Change Language",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Text(
                        text = if (language == AppLanguage.BN) "বাংলা এবং ইংরেজি মধ্যে পরিবর্তন করুন" else "Switch between Bengali and English",
                        fontSize = 12.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )
                }
                Row(
                    modifier = Modifier
                        .background(
                            if (isDark) Color(0xFF1C1C1E) else Color(0xFFF1F5F9),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // English Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (language == AppLanguage.EN) FintechBlue else Color.Transparent
                            )
                            .clickable {
                                if (language != AppLanguage.EN) {
                                    viewModel.toggleLanguage(context)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "English",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (language == AppLanguage.EN) Color.White else (if (isDark) Color.LightGray else Color.DarkGray)
                        )
                    }
                    // Bangla Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (language == AppLanguage.BN) FintechBlue else Color.Transparent
                            )
                            .clickable {
                                if (language != AppLanguage.BN) {
                                    viewModel.toggleLanguage(context)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "বাংলা",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (language == AppLanguage.BN) Color.White else (if (isDark) Color.LightGray else Color.DarkGray)
                        )
                    }
                }
            }
        }

        // --- 5. DATA BACKUP & RESTORE CARD ---
        var pendingLocalRestoreData by remember { mutableStateOf<com.example.data.FinanceBackup?>(null) }
        var pendingLocalRestoreStats by remember { mutableStateOf<com.example.ui.viewmodel.BackupStats?>(null) }
        var pendingLocalRestoreFileName by remember { mutableStateOf("") }
        var pendingLocalRestoreJson by remember { mutableStateOf("") }
        var showLocalBackupStatsDialog by remember { mutableStateOf(false) }
        var currentDbStats by remember { mutableStateOf<com.example.ui.viewmodel.BackupStats?>(null) }

        fun getFileNameFromUri(context: android.content.Context, uri: android.net.Uri): String? {
            var result: String? = null
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        if (index != -1) {
                            result = cursor.getString(index)
                        }
                    }
                } finally {
                    cursor?.close()
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result?.lastIndexOf('/')
                if (cut != null && cut != -1) {
                    result = result.substring(cut + 1)
                }
            }
            return result
        }

        val openDocumentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.let { inputStream ->
                    try {
                        val jsonContent = inputStream.use { it.bufferedReader().readText() }
                        val parsed = viewModel.parseBackupJson(jsonContent)
                        if (parsed != null) {
                            pendingLocalRestoreData = parsed
                            pendingLocalRestoreStats = viewModel.calculateBackupStats(parsed)
                            pendingLocalRestoreFileName = getFileNameFromUri(context, uri) ?: "Local Backup"
                            pendingLocalRestoreJson = jsonContent
                        } else {
                            viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "ভুল ব্যাকআপ ফরম্যাট" else "Invalid backup format", isSuccess = false, type = "ERROR")
                        }
                    } catch (e: Exception) {
                        viewModel.triggerCustomNotification("Error: ${e.localizedMessage}", isSuccess = false, type = "ERROR")
                    }
                }
            }
        }

        // Render Local Backup/Restore summary dialogs if active
        if (showLocalBackupStatsDialog && currentDbStats != null) {
            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
            val defaultName = "financenote_backup_$timestamp.json"

            BackupStatsDialog(
                title = if (language == AppLanguage.BN) "লোকাল ব্যাকআপ সামারি" else "Local Backup Summary",
                stats = currentDbStats!!,
                language = language,
                isDark = isDark,
                isRestoreMode = false,
                initialFileName = defaultName,
                workspaces = workspaceStatsList,
                onConfirm = { finalFileName, comment, selectedWorkspaceIds ->
                    showLocalBackupStatsDialog = false
                    pendingLocalBackupComment = comment
                    // We also need to pass workspaceIds for local backup
                    // Create a flow that captures selectedWorkspaceIds and then uses it in the export
                    // But exportBackupToUri is called later in createDocumentLauncher.
                    // Wait, createDocumentLauncher is an ActivityResultLauncher!
                    pendingLocalWorkspaceIds = selectedWorkspaceIds
                    createDocumentLauncher.launch(finalFileName)
                },
                onDismiss = { showLocalBackupStatsDialog = false }
            )
        }

        if (pendingLocalRestoreStats != null && pendingLocalRestoreData != null) {
            BackupStatsDialog(
                title = if (language == AppLanguage.BN) "লোকাল রিস্টোর সামারি" else "Local Restore Summary",
                stats = pendingLocalRestoreStats!!,
                language = language,
                isDark = isDark,
                isRestoreMode = true,
                initialFileName = pendingLocalRestoreFileName,
                workspaces = workspaceStatsList,
                onBackupRequested = {
                    localCoroutineScope.launch {
                        val backupData = viewModel.getCurrentDatabaseBackup()
                        val stats = viewModel.calculateBackupStats(backupData)
                        currentDbStats = stats
                        showLocalBackupStatsDialog = true
                    }
                },
                onConfirm = { _, _, selectedWorkspaceIds ->
                    viewModel.importBackup(
                        context = context,
                        json = pendingLocalRestoreJson,
                        fromLocalFile = false,
                        workspaceIds = selectedWorkspaceIds,
                        onSuccess = {
                            pendingLocalRestoreStats = null
                            pendingLocalRestoreData = null
                            pendingLocalRestoreJson = ""
                            viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "ব্যাকআপ সফলভাবে রিস্টোর হয়েছে!" else "Backup successfully restored!", isSuccess = true, type = "SUCCESS")
                        },
                        onError = { error ->
                            viewModel.triggerCustomNotification("Error: $error", isSuccess = false, type = "ERROR")
                        }
                    )
                },
                onDismiss = {
                    pendingLocalRestoreStats = null
                    pendingLocalRestoreData = null
                    pendingLocalRestoreJson = ""
                }
            )
        }

        SettingCategory(
            title = if (language == AppLanguage.BN) "ডাটা ব্যাকআপ ও রিস্টোর" else "Data Backup & Restore",
            isDark = isDark,
            icon = Icons.Rounded.Backup,
            initiallyExpanded = filter == "backup"
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Local Backup & Restore Sub-header
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (language == AppLanguage.BN) "লোকাল ব্যাকআপ ও রিস্টোর" else "Local Backup & Restore",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Text(
                        text = if (language == AppLanguage.BN) 
                            "আপনার সমস্ত ডাটা ফোনের মেমোরিতে ফাইল আকারে ব্যাকআপ রাখুন এবং যেকোনো সময় রিস্টোর করুন।" 
                            else "Keep a backup of all your data as a file in phone memory and restore anytime.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                localCoroutineScope.launch {
                                    val backupData = viewModel.getCurrentDatabaseBackup()
                                    val stats = viewModel.calculateBackupStats(backupData)
                                    currentDbStats = stats
                                    showLocalBackupStatsDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                        ) {
                            Icon(Icons.Rounded.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (language == AppLanguage.BN) "ব্যাকআপ তৈরি" else "Create Backup", fontSize = 12.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                openDocumentLauncher.launch(arrayOf("application/json", "application/octet-stream"))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFE2E8F0)),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF262626) else Color(0xFFCBD5E1))
                        ) {
                            Icon(Icons.Rounded.Restore, contentDescription = null, tint = FintechBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (language == AppLanguage.BN) "রিস্টোর করুন" else "Restore", fontSize = 12.sp, color = FintechBlue)
                        }
                    }
                }

                // Expandable paste backup area
                AnimatedVisibility(visible = showPasteArea) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Option 1: Quick Restore from File
                        Button(
                            onClick = {
                                try {
                                    val backupFile = java.io.File(context.filesDir, "financenote_backup.json")
                                    if (backupFile.exists()) {
                                        val jsonContent = backupFile.readText()
                                        val parsed = viewModel.parseBackupJson(jsonContent)
                                        if (parsed != null) {
                                            pendingLocalRestoreData = parsed
                                            pendingLocalRestoreStats = viewModel.calculateBackupStats(parsed)
                                            pendingLocalRestoreFileName = "financenote_backup.json"
                                            pendingLocalRestoreJson = jsonContent
                                            showPasteArea = false
                                        } else {
                                            viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "ভুল ব্যাকআপ ফরম্যাট" else "Invalid backup format", isSuccess = false, type = "ERROR")
                                        }
                                    } else {
                                        viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "কোনো সেভ করা ব্যাকআপ পাওয়া যায়নি!" else "No saved backup found!", isSuccess = true, type = "INFO")
                                    }
                                } catch (e: Exception) {
                                    viewModel.triggerCustomNotification("Error: ${e.localizedMessage}", isSuccess = false, type = "ERROR")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Icon(Icons.Rounded.RestorePage, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (language == AppLanguage.BN) "মেমোরি থেকে রিস্টোর" else "Restore from Memory", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        // Option 2: Paste Code
                        OutlinedTextField(
                            value = pasteJsonInput,
                            onValueChange = { pasteJsonInput = it },
                            label = { Text(if (language == AppLanguage.BN) "ব্যাকআপ কোড পেস্ট করুন" else "Paste Backup Code") },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FintechBlue,
                                unfocusedBorderColor = if (isDark) Color(0xFF262626) else Color(0xFFCBD5E1),
                                focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                                unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                        )
                        Button(
                            onClick = {
                                if (pasteJsonInput.isNotBlank()) {
                                    try {
                                        val parsed = viewModel.parseBackupJson(pasteJsonInput)
                                        if (parsed != null) {
                                            pendingLocalRestoreData = parsed
                                            pendingLocalRestoreStats = viewModel.calculateBackupStats(parsed)
                                            pendingLocalRestoreFileName = if (language == AppLanguage.BN) "পেস্ট করা ব্যাকআপ কোড" else "Pasted Backup Code"
                                            pendingLocalRestoreJson = pasteJsonInput
                                            pasteJsonInput = ""
                                            showPasteArea = false
                                        } else {
                                            viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "ভুল ব্যাকআপ ফরম্যাট" else "Invalid backup format", isSuccess = false, type = "ERROR")
                                        }
                                    } catch (e: Exception) {
                                        viewModel.triggerCustomNotification("Error: ${e.localizedMessage}", isSuccess = false, type = "ERROR")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                        ) {
                            Text(if (language == AppLanguage.BN) "নিশ্চিত করুন" else "Confirm", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                HorizontalDivider(color = if (isDark) Color(0xFF262626) else Color(0xFFE2E8F0))

                // Online Backup (Google Drive) Sub-header
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (language == AppLanguage.BN) "অনলাইন ব্যাকআপ (গুগল ড্রাইভ)" else "Online Backup (Google Drive)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    // User status row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDark) Color(0xFF1C1C1E) else Color(0xFFF1F5F9))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isGoogleSignedIn && !profilePhotoUri.isNullOrEmpty()) {
                                AsyncImage(
                                    model = profilePhotoUri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle, 
                                    contentDescription = null, 
                                    tint = FintechBlue, 
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isGoogleSignedIn) (googleName.orEmpty().ifBlank { if (language == AppLanguage.BN) "গুগল ইউজার" else "Google User" }) else (if (language == AppLanguage.BN) "লগইন করা নেই" else "Not Signed In"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                            Text(
                                text = if (isGoogleSignedIn) (googleEmail.orEmpty().ifBlank { "drive.user@gmail.com" }) else (if (language == AppLanguage.BN) "ব্যাকআপ রাখতে অনুগ্রহ করে সাইন-ইন করুন" else "Please sign-in to backup your data"),
                                fontSize = 11.sp,
                                color = if (isDark) Color.Gray else Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (isGoogleSignedIn) {
                                    onLogoutClick()
                                } else {
                                    onSignInClick()
                                }
                            },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = if (isGoogleSignedIn) FintechRed else FintechBlue),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                            modifier = Modifier
                                .width(90.dp)
                                .height(36.dp)
                                .align(Alignment.CenterVertically)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isGoogleSignedIn) (if (language == AppLanguage.BN) "লগআউট" else "Logout") else (if (language == AppLanguage.BN) "লগইন" else "Login"),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (!driveStatusMessage.isNullOrEmpty()) {
                        Text(
                            text = driveStatusMessage ?: "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = FintechBlue,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    Text(
                        text = if (language == AppLanguage.BN) 
                            "আপনার সমস্ত লেনদেন, সঞ্চয় এবং হিসাবের তথ্য সম্পূর্ণ নিরাপদ রাখতে সরাসরি গুগল ড্রাইভে ব্যাকআপ রাখুন।" 
                            else "To keep all your transaction, savings, and account data safe, backup directly to Google Drive.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )

                    // Last Backup Time Info Row & Auto Backup Setting
                    if (isGoogleSignedIn) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            HorizontalDivider(color = if (isDark) Color(0xFF262626) else Color(0xFFE2E8F0))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BN) "সর্বশেষ ক্লাউড ব্যাকআপ:" else "Last Cloud Backup:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.LightGray else Color(0xFF475569)
                                )
                                Text(
                                    text = formatSyncTime(lastGDriveBackupTime, language),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FintechBlue
                                )
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BN) "স্বয়ংক্রিয় ব্যাকআপের সময়কাল:" else "Auto Backup Interval:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.LightGray else Color(0xFF475569)
                                )
                                
                                val intervalOptions = listOf(-1, 1, 2, 5, 7, 15, 30)
                                val selectedText = when (autoBackupIntervalDays) {
                                    -1 -> if (language == AppLanguage.BN) "বন্ধ (Never)" else "Never"
                                    1 -> if (language == AppLanguage.BN) "১ দিন পর পর" else "Every 1 Day"
                                    2 -> if (language == AppLanguage.BN) "২ দিন পর পর" else "Every 2 Days"
                                    5 -> if (language == AppLanguage.BN) "৫ দিন পর পর" else "Every 5 Days"
                                    7 -> if (language == AppLanguage.BN) "৭ দিন পর পর" else "Every 7 Days"
                                    15 -> if (language == AppLanguage.BN) "১৫ দিন পর পর" else "Every 15 Days"
                                    30 -> if (language == AppLanguage.BN) "৩০ দিন পর পর" else "Every 30 Days"
                                    else -> if (language == AppLanguage.BN) "বন্ধ (Never)" else "Never"
                                }

                                ExposedDropdownMenuBox(
                                    expanded = autoBackupDropdownExpanded,
                                    onExpandedChange = { autoBackupDropdownExpanded = !autoBackupDropdownExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = selectedText,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = if (isDark) Color(0xFF262626) else Color(0xFFCBD5E1),
                                            focusedBorderColor = FintechBlue,
                                            unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                                            focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                                            unfocusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF8FAFC),
                                            focusedContainerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF8FAFC)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = autoBackupDropdownExpanded) }
                                    )
                                    ExposedDropdownMenu(
                                        expanded = autoBackupDropdownExpanded,
                                        onDismissRequest = { autoBackupDropdownExpanded = false }
                                    ) {
                                        intervalOptions.forEach { days ->
                                            val itemText = when (days) {
                                                -1 -> if (language == AppLanguage.BN) "বন্ধ (Never)" else "Never"
                                                1 -> if (language == AppLanguage.BN) "১ দিন পর পর" else "Every 1 Day"
                                                2 -> if (language == AppLanguage.BN) "২ দিন পর পর" else "Every 2 Days"
                                                5 -> if (language == AppLanguage.BN) "৫ দিন পর পর" else "Every 5 Days"
                                                7 -> if (language == AppLanguage.BN) "৭ দিন পর পর" else "Every 7 Days"
                                                15 -> if (language == AppLanguage.BN) "১৫ দিন পর পর" else "Every 15 Days"
                                                30 -> if (language == AppLanguage.BN) "৩০ দিন পর পর" else "Every 30 Days"
                                                else -> ""
                                            }
                                            DropdownMenuItem(
                                                text = { Text(itemText, fontSize = 13.sp) },
                                                onClick = {
                                                    viewModel.setAutoBackupIntervalDays(context, days)
                                                    autoBackupDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(color = if (isDark) Color(0xFF262626) else Color(0xFFE2E8F0))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (!isGoogleSignedIn) {
                                    onSignInClick()
                                } else {
                                    onBackupClick()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                        ) {
                            Icon(Icons.Rounded.CloudUpload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (language == AppLanguage.BN) "ক্লাউড ব্যাকআপ" else "Cloud Backup", fontSize = 11.sp, color = Color.White)
                        }
                        Button(
                            onClick = {
                                if (!isGoogleSignedIn) {
                                    onSignInClick()
                                } else {
                                    onRestoreClick()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF1C1C1E) else Color(0xFFE2E8F0)),
                            border = BorderStroke(1.dp, if (isDark) Color(0xFF262626) else Color(0xFFCBD5E1))
                        ) {
                            Icon(Icons.Rounded.CloudDownload, contentDescription = null, tint = FintechBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (language == AppLanguage.BN) "ক্লাউড রিস্টোর" else "Cloud Restore", fontSize = 11.sp, color = FintechBlue)
                        }
                    }
                }
            }
        }
        // --- TRASH / RECYCLE BIN ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "ট্র্যাশ (রিসাইকেল বিন)" else "Trash (Recycle Bin)",
            isDark = isDark,
            icon = Icons.Rounded.DeleteOutline,
            initiallyExpanded = false
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (language == AppLanguage.BN) 
                        "যেকোনো ডিলেটেড এন্ট্রি ৩০ দিন পর্যন্ত ট্র্যাশে থাকবে। আপনি চাইলে রিস্টোর বা পার্মানেন্ট ডিলেট করতে পারবেন।" 
                        else "Deleted entries stay here for 30 days. Restore or permanently delete them.",
                    fontSize = 12.sp,
                    color = if (isDark) Color.Gray else Color(0xFF64748B)
                )
                
                Button(
                    onClick = { showTrashDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                        contentColor = if (isDark) Color.White else Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = if (isDark) Color.White else FintechBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (language == AppLanguage.BN) "ট্র্যাশ ওপেন করুন" else "Open Trash", fontSize = 12.sp)
                }
            }
        }
        // --- 3. NOTIFICATION WIDGET CARD ---
        val notificationEnabled by viewModel.isNotificationEnabled.collectAsState()
        val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.toggleNotification(context)
            } else {
                viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "নটিফিকেশন পারমিশন প্রয়োজন" else "Notification permission required", isSuccess = true, type = "INFO")
            }
        }

        SettingCategory(
            title = if (language == AppLanguage.BN) "কুইক একশন উইজেট" else "Quick Action Widget",
            isDark = isDark,
            icon = Icons.Rounded.NotificationsActive,
            initiallyExpanded = false
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(FintechBlue.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SmartButton,
                        contentDescription = null,
                        tint = FintechBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.BN) "নটিফিকেশন বার উইজেট" else "Notification Bar Widget",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Text(
                        text = if (language == AppLanguage.BN) "দ্রুত একশন বাটনগুলো নটিফিকেশন বারে দেখান" else "Show quick action buttons in notification bar",
                        fontSize = 12.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )
                }
                Switch(
                    checked = notificationEnabled,
                    onCheckedChange = { 
                        if (!notificationEnabled && android.os.Build.VERSION.SDK_INT >= 33) {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.toggleNotification(context)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = FintechBlue,
                        uncheckedThumbColor = if (isDark) Color.Gray else Color.White,
                        uncheckedTrackColor = if (isDark) Color(0xFF2A2E42) else Color(0xFFE2E8F0)
                    )
                )
            }
        }



        // --- 6. APP ERROR LOG (SUPPORT) CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "অ্যাপ এর লগ (সাপোর্ট)" else "App Error Log (Support)",
            isDark = isDark,
            icon = Icons.Rounded.BugReport
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.BN) 
                        "অ্যাপ ব্যবহারে কোনো কারিগরি সমস্যা হলে নিচের বাটনে ক্লিক করে এরর লগগুলো আমাদের ইমেইল বা হোয়াটসঅ্যাপে পাঠাতে পারেন। এটি আমাদের সমস্যা সমাধানে সাহায্য করবে।" 
                        else "If you encounter any technical issues while using the app, you can check the error logs and send them to our email or WhatsApp. This helps us solve the problem.",
                    fontSize = 12.sp,
                    color = if (isDark) Color.Gray else Color(0xFF64748B),
                    lineHeight = 18.sp
                )

                Button(
                    onClick = {
                        showErrorLogDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechRed)
                ) {
                    Icon(Icons.Rounded.Restore, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "এরর রিপোর্ট চেক করুন" else "Check Error Report",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        
        // --- 7. ABOUT DEVELOPER CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "ডেভেলপার প্রোফাইল" else "Developer Profile",
            isDark = isDark,
            icon = Icons.Rounded.Code,
            initiallyExpanded = false
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.6f) else Color(0xFFF1F5F9)
                ),
                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = FintechBlue,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Shariful Islam",
                                color = FintechBlue,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (language == AppLanguage.BN) "ইউজার এক্সপেরিয়েন্স ও অ্যাপ ডেভেলপার" else "User Experience & App Developer",
                                color = if (isDark) Color.Gray else Color(0xFF64748B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val contactItems = listOf(
                            Triple(Icons.Rounded.Link, "facebook.com/shariful.uxd", "https://facebook.com/shariful.uxd"),
                            Triple(Icons.Rounded.Phone, "01768899599", "tel:01768899599"),
                            Triple(Icons.Rounded.Email, "connect.shariful@gmail.com", "mailto:connect.shariful@gmail.com")
                        )

                        contactItems.forEach { (icon, text, uri) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        try {
                                            val intent = if (uri.startsWith("mailto:")) {
                                                android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                                    data = android.net.Uri.parse(uri)
                                                }
                                            } else {
                                                android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uri))
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {}
                                    }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = FintechBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = text,
                                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF334155),
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = FintechBlue.copy(alpha = 0.6f),
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/8801768899599"))
                                context.startActivity(intent)
                            } catch (e: Exception) {}
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = FintechBlue.copy(alpha = 0.1f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Campaign,
                            contentDescription = null,
                            tint = FintechBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.BN) "হোয়াটসঅ্যাপে যোগাযোগ করুন" else "Contact on WhatsApp",
                            color = FintechBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }


        // --- 8. APP INFO CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "অ্যাপ ইনফো" else "App Info",
            isDark = isDark,
            icon = Icons.Rounded.Info,
            initiallyExpanded = true
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "অ্যাপের নাম" else "App Name",
                        fontSize = 14.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )
                    Text(
                        text = if (language == AppLanguage.BN) "ফাইন্যান্স নোট (Finance Note)" else "Finance Note",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                }
                
                HorizontalDivider(color = if (isDark) Color(0xFF262626) else Color(0xFFE2E8F0))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "সংস্করণ" else "Version",
                        fontSize = 14.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )
                    Text(
                        text = viewModel.updateManager.getAppVersionName(context),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                }
                
                HorizontalDivider(color = if (isDark) Color(0xFF262626) else Color(0xFFE2E8F0))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isCheckingForUpdate) {
                            viewModel.updateManager.checkForUpdates(context) { isAvailable ->
                                if (isAvailable) {
                                    onShowUpdatePopup()
                                } else {
                                    viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "আপনি সর্বশেষ সংস্করণ ব্যবহার করছেন" else "You are using the latest version", isSuccess = true, type = "INFO")
                                }
                            }
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "আপডেট চেক করুন" else "Check for Updates",
                        fontSize = 14.sp,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    if (isCheckingForUpdate) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = FintechBlue,
                            strokeWidth = 2.dp
                        )
                    } else if (updateInfo.isUpdateAvailable) {
                        Text(
                            text = if (language == AppLanguage.BN) "আপডেট উপলব্ধ" else "Update Available",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981) // Green
                        )
                    } else {
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = if (isDark) Color.Gray else Color.DarkGray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // --- 4.5 SHARE APP CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "অ্যাপটি শেয়ার করুন" else "Share App",
            isDark = isDark,
            icon = Icons.Rounded.Share,
            initiallyExpanded = true
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.BN) "বন্ধু ও পরিজনদের সাথে অ্যাপটি শেয়ার করুন" else "Share the app with friends and family",
                    fontSize = 13.sp,
                    color = if (isDark) Color.Gray else Color(0xFF64748B)
                )
                Button(
                    onClick = {
                        val shareText = buildString {
                            append(if (language == AppLanguage.BN) "দৈনন্দিন লেনদেন, দেনাপাওনা ও সঞ্চয়ের হিসাব রাখতে ডাউনলোড করুন ফাইন্যান্স নোট অ্যাপ: " else "Download Finance Note app to keep track of your daily transactions, debts, and savings: ")
                            append("\n")
                            val updateInfoStr = context.getSharedPreferences("FinancePrefs", android.content.Context.MODE_PRIVATE).getString("update_info", null)
                            var link = "https://sites.google.com/view/financenote/home?authuser=0"
                            if (updateInfoStr != null) {
                                try {
                                    val obj = org.json.JSONObject(updateInfoStr)
                                    val url = obj.optString("update_url", "")
                                    if (url.isNotBlank()) link = url
                                } catch (e: Exception) {}
                            }
                            append(link)
                        }
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, if (language == AppLanguage.BN) "শেয়ার করুন" else "Share via"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Share, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "শেয়ার করুন" else "Share Now",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }

        // --- 9. PRIVACY & TERMS CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "প্রাইভেসি ও শর্তাবলী" else "Privacy & Terms",
            isDark = isDark,
            icon = Icons.Rounded.Security,
            initiallyExpanded = false
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Item 1: Privacy Policy
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showPrivacyDialog = true
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Rounded.Info, contentDescription = null, tint = FintechBlue, modifier = Modifier.size(18.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "প্রাইভেসি পলিসি" else "Privacy Policy",
                        fontSize = 14.sp,
                        color = if (isDark) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = if (isDark) Color.Gray else Color.DarkGray, modifier = Modifier.size(16.dp))
                }

                HorizontalDivider(color = if (isDark) Color(0xFF262626) else Color(0xFFE2E8F0))

                // Item 2: Terms of Use
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showTermsDialog = true
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Rounded.Security, contentDescription = null, tint = FintechBlue, modifier = Modifier.size(18.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "ব্যবহারের শর্তাবলী" else "Terms of Use",
                        fontSize = 14.sp,
                        color = if (isDark) Color.White else Color(0xFF1E293B),
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = if (isDark) Color.Gray else Color.DarkGray, modifier = Modifier.size(16.dp))
                }
            }
        }

        // --- 10. VISIT WEBSITE CARD ---
        SettingCategory(
            title = if (language == AppLanguage.BN) "আমাদের ওয়েবসাইট ভিজিট করুন" else "Visit Our Website",
            isDark = isDark,
            icon = Icons.Rounded.Public,
            initiallyExpanded = true
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.BN) "অ্যাপের আপডেট ও তথ্য জানতে আমাদের ওয়েবসাইট ভিজিট করুন" else "Visit our website for app updates and information",
                    fontSize = 13.sp,
                    color = if (isDark) Color.Gray else Color(0xFF64748B)
                )
                Button(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://sites.google.com/view/financenote/home?authuser=0"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Icon(imageVector = Icons.Rounded.Public, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.BN) "ওয়েবসাইট ভিজিট করুন" else "Visit Website",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(200.dp))
    }

    if (showTrashDialog) {
        TrashDialog(
            viewModel = viewModel,
            language = language,
            isDarkTheme = isDark,
            onDismiss = { showTrashDialog = false }
        )
    }

    var logoutUserInput by remember { mutableStateOf("") }
    val logoutCaptcha = remember(showLogoutConfirm) {
        val chars = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"
        (1..4).map { chars.random() }.joinToString("")
    }
    val isLogoutCaptchaCorrect = logoutUserInput.trim().equals(logoutCaptcha, ignoreCase = true)

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { 
                onLogoutConfirmDismiss() 
                logoutUserInput = ""
            },
            title = { Text(if (language == AppLanguage.BN) "লগআউট ও ব্যাকআপ নিশ্চিতকরণ" else "Logout & Backup Confirmation") },
            text = { 
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (language == AppLanguage.BN) 
                            "লগআউট করার পূর্বে আপনি কি একটি ম্যানুয়াল ব্যাকআপ সংরক্ষণ করতে চান? আপনি ম্যানুয়াল ব্যাকআপ না নিলেও গুগল ড্রাইভ এবং লোকাল স্টোরেজে একটি অটো ব্যাকআপ সংরক্ষণ করা হবে।"
                        else 
                            "Would you like to save a manual backup before logging out? Even if you don't take a manual backup, an automatic backup will be saved in Google Drive and local storage for your safety."
                    ) 
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isDark) Color(0xFF282E47) else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "লগআউট নিশ্চিত করতে ক্যাপচা কোডটি টাইপ করুন" else "To confirm logout, type the captcha code",
                            fontSize = 12.sp,
                            color = if (isDark) Color.Gray else Color(0xFF64748B),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = logoutCaptcha,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 6.sp,
                            color = Color(0xFFEF4444),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                        OutlinedTextField(
                            value = logoutUserInput,
                            onValueChange = { logoutUserInput = it },
                            placeholder = { Text(if (language == AppLanguage.BN) "ক্যাপচা কোড" else "Captcha Code") },
                            modifier = Modifier.fillMaxWidth().testTag("logout_captcha_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isLogoutCaptchaCorrect) Color(0xFF10B981) else Color(0xFF3B82F6),
                                unfocusedBorderColor = if (isDark) Color(0xFF2E334D) else Color(0xFFCBD5E1),
                                focusedTextColor = if (isDark) Color.White else Color(0xFF1E293B),
                                unfocusedTextColor = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FintechBlue,
                            disabledContainerColor = FintechBlue.copy(alpha = 0.6f)
                        ),
                        enabled = isLogoutCaptchaCorrect,
                        onClick = {
                            onLogoutConfirmDismiss()
                            logoutUserInput = ""
                            isSignoutBackupActive = true
                            val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                            pendingLocalBackupComment = "Backup before logout"
                            createDocumentLauncher.launch("financenote_backup_before_logout_$timestamp.json")
                        },
                        modifier = Modifier.fillMaxWidth(0.95f)
                    ) {
                        Text(if (language == AppLanguage.BN) "ব্যাকআপ নিন ও লগআউট করুন" else "Backup & Logout", color = Color.White)
                    }
                    
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FintechRed,
                            disabledContainerColor = FintechRed.copy(alpha = 0.6f)
                        ),
                        enabled = isLogoutCaptchaCorrect,
                        onClick = {
                            onLogoutConfirmDismiss()
                            logoutUserInput = ""
                            viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "অটো ব্যাকআপ তৈরি হচ্ছে এবং লগআউট করা হচ্ছে..." else "Creating auto backup and logging out...", isSuccess = true, type = "INFO")
                            viewModel.performAutoBackupAndSignOut(context, profileName) {
                                viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "গুগল ড্রাইভ থেকে লগআউট সফল হয়েছে!" else "Logged out from Google Drive successfully!", isSuccess = true, type = "SUCCESS")
                                viewModel.triggerCustomNotification(
                                    if (language == AppLanguage.BN) "আপনার অ্যাকাউন্ট থেকে সফলভাবে লগআউট করা হয়েছে।" else "Successfully logged out of your account.",
                                    isSuccess = true,
                                    type = "SIGN_OUT"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.95f)
                    ) {
                        Text(if (language == AppLanguage.BN) "শুধু লগআউট (অটো ব্যাকআপ সহ)" else "Just Logout (with Auto Backup)", color = Color.White)
                    }
                    
                    TextButton(
                        onClick = { 
                            onLogoutConfirmDismiss() 
                            logoutUserInput = ""
                        },
                        modifier = Modifier.fillMaxWidth(0.95f)
                    ) {
                        Text(if (language == AppLanguage.BN) "বাতিল" else "Cancel")
                    }
                }
            }
        )
    }


    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    text = if (language == AppLanguage.BN) "প্রাইভেসি পলিসি" else "Privacy Policy",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) 
                            "১. তথ্য সংগ্রহ ও ব্যবহার:\nFinance Note একটি আধুনিক ও নিরাপদ পার্সোনাল ফাইন্যান্স ম্যানেজার। আপনার এন্ট্রি করা সমস্ত ডাটা ডিফল্টভাবে আপনার ডিভাইসের লোকাল SQLite (Room) ডাটাবেজে অত্যন্ত সুরক্ষিতভাবে সংরক্ষিত থাকে। আপনি যদি স্বেচ্ছায় 'ক্লাউড সিঙ্ক' অপশনটি চালু করেন তবেই তা আমাদের এনক্রিপ্টেড ফায়ারবেস ক্লাউড সার্ভারে সিঙ্ক হবে।"
                            else "1. Data Collection & Usage:\nFinance Note is a modern and secure personal finance manager. By default, all your data is stored locally on your device in a secure SQLite (Room) database. Data is only synchronized with our encrypted Firebase Cloud server if you explicitly enable the 'Cloud Sync' feature.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color(0xFF334155)
                    )
                    Text(
                        text = if (language == AppLanguage.BN)
                            "২. এন্ড-টু-এন্ড AES-256 এনক্রিপশন:\nআপনার সর্বোচ্চ নিরাপত্তার কথা মাথায় রেখে, গুগল ড্রাইভ ব্যাকআপ, ক্লাউড সিঙ্ক, এবং লোকাল ব্যাকআপ ফাইলসহ সকল প্রকার তথ্য মিলিটারী-গ্রেড AES-256 এনক্রিপশন দ্বারা এনক্রিপ্ট করা হয়। যার ফলে আপনার পাসওয়ার্ড বা ব্যাকআপ কোড ছাড়া কেউ আপনার তথ্য ডিক্রিপ্ট করতে পারবে না।"
                            else "2. End-to-End AES-256 Encryption:\nFor your maximum security, all backup formats—including Google Drive backups, cloud sync records, and local files—are encrypted using military-grade AES-256 encryption. This ensures that only you, with your unique backup code, can access your data.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color(0xFF334155)
                    )
                    Text(
                        text = if (language == AppLanguage.BN)
                            "৩. নিরাপদ ব্যাকআপ পলিসি:\n Finance Note আপনার কোনো ব্যক্তিগত তথ্য বা পাসওয়ার্ড আমাদের সার্ভারে জমা রাখে না। আপনার গুগল ড্রাইভ ব্যাকআপ আপনার নিজস্ব ড্রাইভ ফোল্ডারে সংরক্ষিত থাকে যা সম্পূর্ণ আপনার নিয়ন্ত্রণাধীন।"
                            else "3. Secure Backup Policy:\nFinance Note does not store any of your private credentials or passwords on our servers. Your Google Drive backups are stored in your own personal drive folder, remaining under your full control at all times.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color(0xFF334155)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                ) {
                    Text(text = if (language == AppLanguage.BN) "ঠিক আছে" else "Close")
                }
            }
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(
                    text = if (language == AppLanguage.BN) "ব্যবহারের শর্তাবলী" else "Terms of Use",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) 
                            "১. অ্যাপের ব্যবহার:\nFinance Note অ্যাপটি শুধুমাত্র ব্যক্তিগত ব্যবহারের জন্য তৈরি। ভুল এন্ট্রি, ডিভাইসের হার্ডওয়্যার ত্রুটি বা পাসওয়ার্ড ভুলে যাওয়ার কারণে ডাটা নষ্ট হলে কর্তৃপক্ষ দায়ী থাকবে না। তবে আমরা সর্বদা আপনার ডাটা সুরক্ষিত রাখতে সচেষ্ট।"
                            else "1. App Usage:\nFinance Note is strictly for personal financial management. The developers are not liable for data loss caused by incorrect manual entries, hardware failure, or forgotten backup passwords.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color(0xFF334155)
                    )
                    Text(
                        text = if (language == AppLanguage.BN)
                            "২. ক্লাউড ও সিকিউরিটি:\nক্লাউড সিঙ্ক এবং গুগল ড্রাইভ ব্যাকআপ ফিচার ব্যবহারের জন্য ইন্টারনেট সংযোগ আবশ্যক। আপনার সিকিউরিটির স্বার্থে সমস্ত ব্যাকআপ ফাইল ডিভাইস থেকেই এনক্রিপ্ট হয়ে আপলোড হয়।"
                            else "2. Cloud & Security:\nAn active internet connection is required for Cloud Sync and Google Drive backup features. For your security, all backup files are encrypted on-device before being uploaded.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color(0xFF334155)
                    )
                    Text(
                        text = if (language == AppLanguage.BN)
                            "৩. পলিসি পরিবর্তন:\nইউজার এক্সপেরিয়েন্স এবং সিকিউরিটি আপডেট বজায় রাখতে Finance Note যেকোনো সময় অ্যাপের শর্তাবলী ও ফিচার পরিবর্তন করার অধিকার সংরক্ষণ করে।"
                            else "3. Policy Changes:\nFinance Note reserves the right to update these terms and features at any time to ensure the highest standards of security and user experience.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.LightGray else Color(0xFF334155)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTermsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                ) {
                    Text(text = if (language == AppLanguage.BN) "সম্মত আছি" else "I Agree")
                }
            }
        )
    }

    if (showErrorLogDialog) {
        // Load logs text from ErrorLogger
        LaunchedEffect(Unit) {
            currentLogsText = com.example.data.ErrorLogger.getErrorLogs(context)
        }

        AlertDialog(
            onDismissRequest = { showErrorLogDialog = false },
            title = {
                Text(
                    text = if (language == AppLanguage.BN) "সিস্টেম এরর রিপোর্ট" else "System Error Report",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) 
                            "নিচে আপনার অ্যাপের সাম্প্রতিক এরর লগসমূহ দেওয়া হলো। আপনি এগুলো ডেভেলপারকে ইমেইল বা হোয়াটসঅ্যাপের মাধ্যমে পাঠাতে পারেন।" 
                            else "Below are the recent system error logs from your app. You can send them to the developer via Email or WhatsApp.",
                        fontSize = 12.sp,
                        color = if (isDark) Color.Gray else Color(0xFF64748B)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(if (isDark) Color(0xFF1C1C1E) else Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .border(1.5.dp, if (isDark) Color(0xFF262626) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        if (currentLogsText.isBlank()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (language == AppLanguage.BN) "কোনো এরর লগ পাওয়া যায়নি!" else "No error logs found!",
                                    fontSize = 13.sp,
                                    color = if (isDark) Color.Gray else Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = currentLogsText,
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = if (isDark) Color.LightGray else Color(0xFF334155)
                                )
                            }
                        }
                    }
                    
                    if (currentLogsText.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Send Email Button
                            Button(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("connect.shariful@gmail.com"))
                                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Finance Note Error Log Report")
                                            putExtra(android.content.Intent.EXTRA_TEXT, currentLogsText)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Send Email"))
                                    } catch (e: Exception) {
                                        viewModel.triggerCustomNotification("Error: ${e.localizedMessage}", isSuccess = false, type = "ERROR")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FintechBlue),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (language == AppLanguage.BN) "ইমেইল" else "Email", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Send WhatsApp Button
                            Button(
                                onClick = {
                                    try {
                                        val truncatedLogs = if (currentLogsText.length > 2000) {
                                            currentLogsText.takeLast(2000) + "\n(Trimming logs for WhatsApp...)"
                                        } else {
                                            currentLogsText
                                        }
                                        val message = "Hi Developer, here is my Finance Note Error Report:\n\n$truncatedLogs"
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://wa.me/8801768899599?text=${android.net.Uri.encode(message)}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        viewModel.triggerCustomNotification("Error: ${e.localizedMessage}", isSuccess = false, type = "ERROR")
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Rounded.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentLogsText.isNotBlank()) {
                        TextButton(
                            onClick = {
                                com.example.data.ErrorLogger.clearErrorLogs(context)
                                currentLogsText = ""
                                viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "এরর লগসমূহ মুছে ফেলা হয়েছে!" else "Error logs cleared!", isSuccess = false, type = "ERROR")
                            }
                        ) {
                            Text(text = if (language == AppLanguage.BN) "লগ মুছুন" else "Clear Logs", color = FintechRed, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = { showErrorLogDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF2E334D) else Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "ঠিক আছে" else "Close",
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    }
                }
            }
        )
    }
}



fun getMonthlySumsForYear(
    transactions: List<Transaction>,
    year: Int,
    type: String
): List<Double> {
    val cal = java.util.Calendar.getInstance()
    val sums = DoubleArray(12) { 0.0 }
    for (tx in transactions) {
        if (tx.type == type) {
            cal.timeInMillis = tx.timestamp
            if (cal.get(java.util.Calendar.YEAR) == year) {
                val month = cal.get(java.util.Calendar.MONTH)
                if (month in 0..11) {
                    sums[month] += tx.amount
                }
            }
        }
    }
    return sums.toList()
}

fun formatShortCurrency(value: Double, language: AppLanguage): String {
    val formatted = when {
        value == 0.0 -> "0"
        value >= 100000.0 -> {
            val lakhs = value / 100000.0
            if (language == AppLanguage.BN) {
                "${String.format("%.1f", lakhs)} লাখ"
            } else {
                "${String.format("%.1f", lakhs)}L"
            }
        }
        value >= 1000.0 -> {
            val thousands = value / 1000.0
            if (language == AppLanguage.BN) {
                "${String.format("%.1f", thousands)}কে"
            } else {
                "${String.format("%.1f", thousands)}K"
            }
        }
        else -> "${value.toInt()}"
    }
    val withPrefix = "৳$formatted"
    return if (language == AppLanguage.BN) {
        withPrefix
            .replace("0", "০")
            .replace("1", "১")
            .replace("2", "২")
            .replace("3", "৩")
            .replace("4", "৪")
            .replace("5", "৫")
            .replace("6", "৬")
            .replace("7", "৭")
            .replace("8", "৮")
            .replace("9", "৯")
    } else {
        withPrefix
    }
}

data class ChartDataset(
    val label: String,
    val data: List<Double>,
    val color: Color
)

@Composable
fun TimelineSplineChart(
    title: String,
    datasets: List<ChartDataset>,
    monthsLabels: List<String>,
    language: AppLanguage,
    isDark: Boolean,
    targetIndex: Int = -1,
    onLongPress: () -> Unit = {}
) {
    val bgColor = if (isDark) listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E)) else listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC))
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val gridColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        FintechGradientCard(
            gradientColors = bgColor,
            cornerRadius = 24.dp,
            padding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { onLongPress() })
                }
        ) {
        Column {
            // Title & Legends Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                
                // Legends in a Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    datasets.forEach { dataset ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(dataset.color, CircleShape)
                                    .border(1.2.dp, Color.White, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = dataset.label,
                                color = textColor.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Graph Area using Row: Fixed Left Y-Axis, Scrollable Right Canvas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Fixed Y-axis labels on the Left
                Canvas(
                    modifier = Modifier
                        .width(30.dp)
                        .fillMaxHeight()
                ) {
                    val canvasHeight = size.height
                    val paddingTop = 15.dp.toPx()
                    val paddingBottom = 25.dp.toPx()
                    val usableHeight = canvasHeight - paddingTop - paddingBottom

                    val maxVal = datasets.flatMap { it.data }.maxOrNull() ?: 0.0
                    val yMax = if (maxVal <= 0.0) 1000.0 else maxVal * 1.15

                    val gridLinesCount = 4
                    val textPaint = android.graphics.Paint().apply {
                        color = (if (isDark) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY)
                        textSize = 9.dp.toPx()
                        textAlign = android.graphics.Paint.Align.RIGHT
                        isAntiAlias = true
                        alpha = 130
                    }

                    for (i in 0 until gridLinesCount) {
                        val fraction = i.toFloat() / (gridLinesCount - 1)
                        val y = paddingTop + fraction * usableHeight
                        val labelVal = yMax * (1f - fraction)
                        val labelStr = formatShortCurrency(labelVal, language)
                        drawContext.canvas.nativeCanvas.drawText(
                            labelStr,
                            size.width - 3.dp.toPx(),
                            y + 4.dp.toPx(),
                            textPaint
                        )
                    }
                }

                // 2. Scrollable Plot Area on the Right
                val scrollState = rememberScrollState()

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    val availableWidth = maxWidth
                    val minItemWidth = 25.2.dp
                    val dynamicWidth = maxOf(availableWidth, (monthsLabels.size * minItemWidth.value).dp)
                    val density = androidx.compose.ui.platform.LocalDensity.current

                    LaunchedEffect(monthsLabels, dynamicWidth, maxWidth, targetIndex, scrollState.maxValue) {
                        kotlinx.coroutines.delay(100)
                        val maxScroll = scrollState.maxValue
                        if (maxScroll > 0 && targetIndex >= 0 && targetIndex < monthsLabels.size) {
                            val paddingLeft = with(density) { 10.dp.toPx() }
                            val paddingRight = with(density) { 15.dp.toPx() }
                            val availableWidthPx = with(density) { maxWidth.toPx() }
                            val dynamicWidthPx = with(density) { dynamicWidth.toPx() }
                            val usableWidth = dynamicWidthPx - paddingLeft - paddingRight
                            val numLabels = monthsLabels.size
                            val maxIdx = if (numLabels > 1) numLabels - 1 else 1
                            val targetX = paddingLeft + targetIndex * (usableWidth / maxIdx.toFloat())
                            val scrollPosition = targetX - (availableWidthPx / 2f)
                            scrollState.scrollTo(scrollPosition.coerceIn(0f, maxScroll.toFloat()).toInt())
                        } else {
                            scrollState.scrollTo(0)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(scrollState)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .width(dynamicWidth)
                                .fillMaxHeight()
                        ) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            val paddingLeft = 10.dp.toPx()
                            val paddingRight = 15.dp.toPx()
                            val paddingTop = 15.dp.toPx()
                            val paddingBottom = 25.dp.toPx()

                            val usableWidth = canvasWidth - paddingLeft - paddingRight
                            val usableHeight = canvasHeight - paddingTop - paddingBottom
                            val bottomY = canvasHeight - paddingBottom

                            val maxVal = datasets.flatMap { it.data }.maxOrNull() ?: 0.0
                            val yMax = if (maxVal <= 0.0) 1000.0 else maxVal * 1.15

                            // Draw Horizontal Grid Lines across full dynamic width
                            val gridLinesCount = 4
                            for (i in 0 until gridLinesCount) {
                                val fraction = i.toFloat() / (gridLinesCount - 1)
                                val y = paddingTop + fraction * usableHeight
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, y),
                                    end = Offset(canvasWidth, y),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }

                            // Draw Splines & Fills
                            val numLabels = monthsLabels.size
                            val maxIdx = if (numLabels > 1) numLabels - 1 else 1

                            datasets.forEach { dataset ->
                                val points = mutableListOf<Offset>()
                                for (idx in 0 until numLabels) {
                                    val v = if (idx < dataset.data.size) dataset.data[idx] else 0.0
                                    val x = paddingLeft + idx * (usableWidth / maxIdx.toFloat())
                                    val y = paddingTop + (1f - (v / yMax).toFloat()) * usableHeight
                                    points.add(Offset(x, y))
                                }

                                if (points.isNotEmpty()) {
                                    val strokePath = Path()
                                    val fillPath = Path()

                                    strokePath.moveTo(points[0].x, points[0].y)
                                    fillPath.moveTo(points[0].x, points[0].y)

                                    for (i in 0 until points.size - 1) {
                                        val p0 = points[i]
                                        val p1 = points[i + 1]

                                        val controlX1 = p0.x + (p1.x - p0.x) / 2f
                                        val controlY1 = p0.y
                                        val controlX2 = p1.x - (p1.x - p0.x) / 2f
                                        val controlY2 = p1.y

                                        strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                                        fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                                    }

                                    fillPath.lineTo(points.last().x, bottomY)
                                    fillPath.lineTo(points.first().x, bottomY)
                                    fillPath.close()

                                    // Draw gradient fill under curve
                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                dataset.color.copy(alpha = 0.25f),
                                                dataset.color.copy(alpha = 0.01f)
                                            ),
                                            startY = paddingTop,
                                            endY = bottomY
                                        )
                                    )

                                    // Draw smooth line stroke
                                    drawPath(
                                        path = strokePath,
                                        color = dataset.color,
                                        style = Stroke(
                                            width = 2.5.dp.toPx(),
                                            join = StrokeJoin.Round,
                                            cap = StrokeCap.Round
                                        )
                                    )

                                    // Draw dots on data points
                                    points.forEach { pt ->
                                        drawCircle(
                                            color = dataset.color,
                                            radius = 3.5.dp.toPx(),
                                            center = pt
                                        )
                                        drawCircle(
                                            color = if (isDark) Color(0xFF121212) else Color.White,
                                            radius = 1.5.dp.toPx(),
                                            center = pt
                                        )
                                    }
                                }
                            }

                            // Draw X-Axis Labels (Months / Days)
                            val xLabelPaint = android.graphics.Paint().apply {
                                color = (if (isDark) android.graphics.Color.WHITE else android.graphics.Color.DKGRAY)
                                textSize = 8.dp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                alpha = 150
                            }

                            for (idx in 0 until numLabels) {
                                val x = paddingLeft + idx * (usableWidth / maxIdx.toFloat())
                                val monthLabel = monthsLabels[idx]
                                drawContext.canvas.nativeCanvas.drawText(
                                    monthLabel,
                                    x,
                                    canvasHeight - 6.dp.toPx(),
                                    xLabelPaint
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

enum class ChartFilterMode {
    MONTH, YEAR, MONTH_TO_MONTH, YEAR_TO_YEAR, DAY
}

fun isTxInMonth(txTimestamp: Long, year: Int, month: Int): Boolean {
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = txTimestamp
    return cal.get(java.util.Calendar.YEAR) == year && cal.get(java.util.Calendar.MONTH) == month
}

fun isTxInDay(txTimestamp: Long, year: Int, month: Int, day: Int): Boolean {
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = txTimestamp
    return cal.get(java.util.Calendar.YEAR) == year &&
            cal.get(java.util.Calendar.MONTH) == month &&
            cal.get(java.util.Calendar.DAY_OF_MONTH) == day
}

fun isTxInYear(txTimestamp: Long, year: Int): Boolean {
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = txTimestamp
    return cal.get(java.util.Calendar.YEAR) == year
}

fun isTxInMonthRange(txTimestamp: Long, startYear: Int, startMonth: Int, endYear: Int, endMonth: Int): Boolean {
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = txTimestamp
    val txYear = cal.get(java.util.Calendar.YEAR)
    val txMonth = cal.get(java.util.Calendar.MONTH)
    
    val startVal = startYear * 12 + startMonth
    val endVal = endYear * 12 + endMonth
    val txVal = txYear * 12 + txMonth
    
    return txVal in startVal..endVal
}

fun isTxInYearRange(txTimestamp: Long, startYear: Int, endYear: Int): Boolean {
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = txTimestamp
    val txYear = cal.get(java.util.Calendar.YEAR)
    return txYear in startYear..endYear
}

data class SplineChartData(
    val labels: List<String>,
    val incomeSums: List<Double>,
    val expenseSums: List<Double>,
    val lendSums: List<Double>,
    val borrowSums: List<Double>
)

fun calculateSplineChartData(
    transactions: List<Transaction>,
    filterMode: ChartFilterMode,
    selectedYear: Int,
    selectedMonth: Int,
    startYear: Int,
    startMonth: Int,
    endYear: Int,
    endMonth: Int,
    startYearY2Y: Int,
    endYearY2Y: Int,
    language: AppLanguage
): SplineChartData {
    val cal = java.util.Calendar.getInstance()
    val banglaMonths = listOf("জানু", "ফেব্রু", "মার্চ", "এপ্রি", "মে", "জুন", "জুলা", "আগস্ট", "সেপ্টে", "অক্টো", "নভে", "ডিসে")
    val englishMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val monthsLabels = if (language == AppLanguage.BN) banglaMonths else englishMonths

    when (filterMode) {
        ChartFilterMode.YEAR -> {
            val endYear = selectedYear
            val startYear = selectedYear - 4
            val yearsRange = (startYear..endYear).toList()
            
            val labelsList = yearsRange.map { formatNumber(it, language) }
            val incomesList = DoubleArray(5) { 0.0 }
            val expensesList = DoubleArray(5) { 0.0 }
            val lendsList = DoubleArray(5) { 0.0 }
            val borrowsList = DoubleArray(5) { 0.0 }
            
            for (tx in transactions) {
                cal.timeInMillis = tx.timestamp
                val txYear = cal.get(java.util.Calendar.YEAR)
                if (txYear in startYear..endYear) {
                    val index = txYear - startYear
                    if (index in 0..4) {
                        when (tx.type) {
                            "INCOME" -> incomesList[index] += tx.amount
                            "EXPENSE" -> expensesList[index] += tx.amount
                            "LEND" -> {
                                lendsList[index] += tx.amount
                                if (tx.subType == "CREDIT") incomesList[index] += tx.amount
                            }
                            "BORROW" -> {
                                borrowsList[index] += tx.amount
                                if (tx.subType == "CREDIT") expensesList[index] += tx.amount
                            }
                            "REPAY_RECEIVED" -> lendsList[index] -= tx.amount // Repay reduces active lend
                            "REPAY_PAID" -> borrowsList[index] -= tx.amount // Repay reduces active borrow
                        }
                    }
                }
            }
            return SplineChartData(
                labels = labelsList,
                incomeSums = incomesList.toList(),
                expenseSums = expensesList.toList(),
                lendSums = lendsList.toList(),
                borrowSums = borrowsList.toList()
            )
        }
        ChartFilterMode.MONTH -> {
            val yearToUse = selectedYear
            val incomes = DoubleArray(12) { 0.0 }
            val expenses = DoubleArray(12) { 0.0 }
            val lends = DoubleArray(12) { 0.0 }
            val borrows = DoubleArray(12) { 0.0 }
            
            for (tx in transactions) {
                cal.timeInMillis = tx.timestamp
                if (cal.get(java.util.Calendar.YEAR) == yearToUse) {
                    val month = cal.get(java.util.Calendar.MONTH)
                    if (month in 0..11) {
                        when (tx.type) {
                            "INCOME" -> incomes[month] += tx.amount
                            "EXPENSE" -> expenses[month] += tx.amount
                            "LEND" -> {
                                lends[month] += tx.amount
                                if (tx.subType == "CREDIT") incomes[month] += tx.amount
                            }
                            "BORROW" -> {
                                borrows[month] += tx.amount
                                if (tx.subType == "CREDIT") expenses[month] += tx.amount
                            }
                            "REPAY_RECEIVED" -> lends[month] -= tx.amount
                            "REPAY_PAID" -> borrows[month] -= tx.amount
                        }
                    }
                }
            }
            return SplineChartData(
                labels = monthsLabels,
                incomeSums = incomes.toList(),
                expenseSums = expenses.toList(),
                lendSums = lends.toList(),
                borrowSums = borrows.toList()
            )
        }
        ChartFilterMode.MONTH_TO_MONTH -> {
            val labelsList = mutableListOf<String>()
            val incomesList = mutableListOf<Double>()
            val expensesList = mutableListOf<Double>()
            val lendsList = mutableListOf<Double>()
            val borrowsList = mutableListOf<Double>()

            var curYear = startYear
            var curMonth = startMonth
            val endTotalMonths = endYear * 12 + endMonth
            
            var count = 0
            while ((curYear * 12 + curMonth <= endTotalMonths) && count < 24) {
                val monthName = monthsLabels[curMonth]
                val yearShort = curYear % 100
                val displayYr = if (language == AppLanguage.BN) {
                    val yrStr = yearShort.toString()
                    val yrFormatted = yrStr
                        .replace("0", "০")
                        .replace("1", "১")
                        .replace("2", "২")
                        .replace("3", "৩")
                        .replace("4", "৪")
                        .replace("5", "৫")
                        .replace("6", "৬")
                        .replace("7", "৭")
                        .replace("8", "৮")
                        .replace("9", "৯")
                    "'$yrFormatted"
                } else {
                    "'$yearShort"
                }
                labelsList.add("$monthName $displayYr")
                
                var monthlyIncome = 0.0
                var monthlyExpense = 0.0
                var monthlyLend = 0.0
                var monthlyBorrow = 0.0
                
                for (tx in transactions) {
                    cal.timeInMillis = tx.timestamp
                    if (cal.get(java.util.Calendar.YEAR) == curYear && cal.get(java.util.Calendar.MONTH) == curMonth) {
                        when (tx.type) {
                            "INCOME" -> monthlyIncome += tx.amount
                            "EXPENSE" -> monthlyExpense += tx.amount
                            "LEND" -> {
                                monthlyLend += tx.amount
                                if (tx.subType == "CREDIT") monthlyIncome += tx.amount
                            }
                            "BORROW" -> {
                                monthlyBorrow += tx.amount
                                if (tx.subType == "CREDIT") monthlyExpense += tx.amount
                            }
                            "REPAY_RECEIVED" -> monthlyLend -= tx.amount
                            "REPAY_PAID" -> monthlyBorrow -= tx.amount
                        }
                    }
                }
                
                incomesList.add(monthlyIncome)
                expensesList.add(monthlyExpense)
                lendsList.add(monthlyLend)
                borrowsList.add(monthlyBorrow)
                
                curMonth++
                if (curMonth > 11) {
                    curMonth = 0
                    curYear++
                }
                count++
            }
            return SplineChartData(
                labels = if (labelsList.isEmpty()) listOf("") else labelsList,
                incomeSums = if (incomesList.isEmpty()) listOf(0.0) else incomesList,
                expenseSums = if (expensesList.isEmpty()) listOf(0.0) else expensesList,
                lendSums = if (lendsList.isEmpty()) listOf(0.0) else lendsList,
                borrowSums = if (borrowsList.isEmpty()) listOf(0.0) else borrowsList
            )
        }
        ChartFilterMode.YEAR_TO_YEAR -> {
            val labelsList = mutableListOf<String>()
            val incomesList = mutableListOf<Double>()
            val expensesList = mutableListOf<Double>()
            val lendsList = mutableListOf<Double>()
            val borrowsList = mutableListOf<Double>()
            
            val sYr = startYearY2Y
            val eYr = if (endYearY2Y < startYearY2Y) startYearY2Y else endYearY2Y
            val yearsRange = (sYr..eYr).take(10)
            
            for (year in yearsRange) {
                val displayYr = if (language == AppLanguage.BN) {
                    year.toString()
                        .replace("0", "০")
                        .replace("1", "১")
                        .replace("2", "২")
                        .replace("3", "৩")
                        .replace("4", "৪")
                        .replace("5", "৫")
                        .replace("6", "৬")
                        .replace("7", "৭")
                        .replace("8", "৮")
                        .replace("9", "৯")
                } else {
                    year.toString()
                }
                labelsList.add(displayYr)
                
                var yearlyIncome = 0.0
                var yearlyExpense = 0.0
                var yearlyLend = 0.0
                var yearlyBorrow = 0.0
                
                for (tx in transactions) {
                    cal.timeInMillis = tx.timestamp
                    if (cal.get(java.util.Calendar.YEAR) == year) {
                        when (tx.type) {
                            "INCOME" -> yearlyIncome += tx.amount
                            "EXPENSE" -> yearlyExpense += tx.amount
                            "LEND" -> {
                                yearlyLend += tx.amount
                                if (tx.subType == "CREDIT") yearlyIncome += tx.amount
                            }
                            "BORROW" -> {
                                yearlyBorrow += tx.amount
                                if (tx.subType == "CREDIT") yearlyExpense += tx.amount
                            }
                            "REPAY_RECEIVED" -> yearlyLend -= tx.amount
                            "REPAY_PAID" -> yearlyBorrow -= tx.amount
                        }
                    }
                }
                
                incomesList.add(yearlyIncome)
                expensesList.add(yearlyExpense)
                lendsList.add(yearlyLend)
                borrowsList.add(yearlyBorrow)
            }
            return SplineChartData(
                labels = if (labelsList.isEmpty()) listOf("") else labelsList,
                incomeSums = if (incomesList.isEmpty()) listOf(0.0) else incomesList,
                expenseSums = if (expensesList.isEmpty()) listOf(0.0) else expensesList,
                lendSums = if (lendsList.isEmpty()) listOf(0.0) else lendsList,
                borrowSums = if (borrowsList.isEmpty()) listOf(0.0) else borrowsList
            )
        }
        ChartFilterMode.DAY -> {
            cal.set(java.util.Calendar.YEAR, selectedYear)
            cal.set(java.util.Calendar.MONTH, selectedMonth)
            val daysInMonth = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            
            val labelsList = (1..daysInMonth).map { formatNumber(it, language) }
            val incomesList = DoubleArray(daysInMonth) { 0.0 }
            val expensesList = DoubleArray(daysInMonth) { 0.0 }
            val lendsList = DoubleArray(daysInMonth) { 0.0 }
            val borrowsList = DoubleArray(daysInMonth) { 0.0 }
            
            for (tx in transactions) {
                cal.timeInMillis = tx.timestamp
                if (cal.get(java.util.Calendar.YEAR) == selectedYear && cal.get(java.util.Calendar.MONTH) == selectedMonth) {
                    val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
                    if (day in 1..daysInMonth) {
                        when (tx.type) {
                            "INCOME" -> incomesList[day - 1] += tx.amount
                            "EXPENSE" -> expensesList[day - 1] += tx.amount
                            "LEND" -> {
                                lendsList[day - 1] += tx.amount
                                if (tx.subType == "CREDIT") incomesList[day - 1] += tx.amount
                            }
                            "BORROW" -> {
                                borrowsList[day - 1] += tx.amount
                                if (tx.subType == "CREDIT") expensesList[day - 1] += tx.amount
                            }
                            "REPAY_RECEIVED" -> lendsList[day - 1] -= tx.amount
                            "REPAY_PAID" -> borrowsList[day - 1] -= tx.amount
                        }
                    }
                }
            }
            return SplineChartData(
                labels = labelsList,
                incomeSums = incomesList.toList(),
                expenseSums = expensesList.toList(),
                lendSums = lendsList.toList(),
                borrowSums = borrowsList.toList()
            )
        }
    }
}

@Composable
fun CustomChartFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val containerColor = if (selected) {
        if (isDark) Color(0xFF3B82F6) else Color(0xFF2563EB)
    } else {
        if (isDark) Color(0xFF121212) else Color(0xFFF1F5F9)
    }
    
    val contentColor = if (selected) {
        Color.White
    } else {
        if (isDark) Color.White.copy(alpha = 0.7f) else Color(0xFF1E293B).copy(alpha = 0.7f)
    }
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SegmentedFilterPicker(
    label: String,
    valueDisplay: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    isDark: Boolean,
    onClickCenter: (() -> Unit)? = null
) {
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    val buttonBg = if (isDark) Color(0xFF121212) else Color(0xFFF1F5F9)
    val containerBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFE2E8F0)
    
    Row(
        modifier = Modifier
            .background(containerBg, RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                color = textColor.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 6.dp, end = 8.dp)
            )
        }
        
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.iconButtonColors(containerColor = buttonBg)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                contentDescription = "Previous",
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Text(
            text = valueDisplay,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 14.dp)
                .then(
                    if (onClickCenter != null) Modifier.clickable { onClickCenter() }
                    else Modifier
                )
        )
        
        IconButton(
            onClick = onNext,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.iconButtonColors(containerColor = buttonBg)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Next",
                tint = textColor,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ChartsScreen(
    viewModel: com.example.ui.viewmodel.FinanceViewModel? = null,
    language: AppLanguage,
    isDark: Boolean,
    transactions: List<Transaction>,
    persons: List<Person>,
    onBack: () -> Unit,
    onEditGradient: ((String) -> Unit)? = null
) {
    val budgetGradients by viewModel?.budgetGradients?.collectAsState() ?: remember { mutableStateOf(emptyMap()) }
    val budgetIncomeAmount by viewModel?.budgetIncome?.collectAsState() ?: remember { mutableStateOf(0.0) }
    val budgetExpenseAmount by viewModel?.budgetExpense?.collectAsState() ?: remember { mutableStateOf(0.0) }

    var pieChartFilterMode by remember { mutableStateOf(ChartFilterMode.MONTH) }
    var timelineChartFilterMode by remember { mutableStateOf(ChartFilterMode.MONTH) }

    // Dialog state variables for manual picking
    var showYearPickerForState by remember { mutableStateOf<((Int) -> Unit)?>(null) }
    var showMonthPickerForState by remember { mutableStateOf<Pair<Int, (Int, Int) -> Unit>?>(null) }
    var showDayPickerForState by remember { mutableStateOf<Triple<Int, Int, (Int, Int, Int) -> Unit>?>(null) }
    var showMonthRangePickerState by remember { mutableStateOf<Pair<Pair<Int, Int>, (Int, Int, Int, Int) -> Unit>?>(null) }
    var showYearRangePickerState by remember { mutableStateOf<Pair<Pair<Int, Int>, (Int, Int) -> Unit>?>(null) }

    // Calendar & Defaults
    val currentCal = java.util.Calendar.getInstance()
    val currentYear = currentCal.get(java.util.Calendar.YEAR)
    val currentMonth = currentCal.get(java.util.Calendar.MONTH) // 0-indexed

    // Pie Chart Specific States:
    var pieSelectedMonth by remember { mutableStateOf(currentMonth) }
    var pieSelectedYear by remember { mutableStateOf(currentYear) }
    var pieSelectedDay by remember { mutableStateOf(currentCal.get(java.util.Calendar.DAY_OF_MONTH)) }

    // Timeline Chart Specific States:
    var timelineSelectedMonth by remember { mutableStateOf(currentMonth) }
    var timelineSelectedYear by remember { mutableStateOf(currentYear) }
    var timelineSelectedDay by remember { mutableStateOf(currentCal.get(java.util.Calendar.DAY_OF_MONTH)) }

    // 2. MONTH_TO_MONTH mode (default: Jan of current year to current month)
    var startMonth by remember { mutableStateOf(0) } // Jan
    var startYear by remember { mutableStateOf(currentYear) }
    var endMonth by remember { mutableStateOf(currentMonth) }
    var endYear by remember { mutableStateOf(currentYear) }

    // 3. YEAR_TO_YEAR mode (default: last year to current year)
    var startYearY2Y by remember { mutableStateOf(currentYear - 1) }
    var endYearY2Y by remember { mutableStateOf(currentYear) }

    // Month Names
    val bnMonthNamesFull = listOf("জানুয়ারি", "ফেব্রুয়ারি", "মার্চ", "এপ্রিল", "মে", "জুন", "জুলাই", "আগস্ট", "সেপ্টেম্বর", "অক্টোবর", "নভেম্বর", "ডিসেম্বর")
    val enMonthNamesFull = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    fun formatMonthYearDisplay(month: Int, year: Int, language: AppLanguage): String {
        val monthStr = if (language == AppLanguage.BN) bnMonthNamesFull[month] else enMonthNamesFull[month]
        val yearStr = formatNumber(year, language)
        return "$monthStr, $yearStr"
    }

    fun formatYearDisplay(year: Int, language: AppLanguage): String {
        return formatNumber(year, language)
    }

    // Filter transactions for Pie Charts based on selection
    val filteredTxsForPie = remember(transactions, pieChartFilterMode, pieSelectedYear, pieSelectedMonth, pieSelectedDay, startYear, startMonth, endYear, endMonth, startYearY2Y, endYearY2Y) {
        transactions.filter { tx ->
            when (pieChartFilterMode) {
                ChartFilterMode.MONTH -> {
                    isTxInMonth(tx.timestamp, pieSelectedYear, pieSelectedMonth)
                }
                ChartFilterMode.YEAR -> {
                    isTxInYear(tx.timestamp, pieSelectedYear)
                }
                ChartFilterMode.MONTH_TO_MONTH -> {
                    isTxInMonthRange(tx.timestamp, startYear, startMonth, endYear, endMonth)
                }
                ChartFilterMode.YEAR_TO_YEAR -> {
                    isTxInYearRange(tx.timestamp, startYearY2Y, endYearY2Y)
                }
                ChartFilterMode.DAY -> {
                    isTxInDay(tx.timestamp, pieSelectedYear, pieSelectedMonth, pieSelectedDay)
                }
            }
        }
    }

    val incomeTransactions = filteredTxsForPie.filter { it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT") }
    val expenseTransactions = filteredTxsForPie.filter { it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT") }
    
    val incomesByCategory = incomeTransactions.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }
    val expensesByCategory = expenseTransactions.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }
    
    val totalIncome = incomeTransactions.sumOf { it.amount }
    val totalExpense = expenseTransactions.sumOf { it.amount }

    val incomePalette = listOf(
        Color(0xFF10B981), // Emerald 500
        Color(0xFF3B82F6), // Blue 500
        Color(0xFF8B5CF6), // Violet 500
        Color(0xFFF59E0B), // Amber 500
        Color(0xFFEC4899), // Pink 500
        Color(0xFF06B6D4), // Cyan 500
        Color(0xFFF97316), // Orange 500
    )
    
    val expensePalette = listOf(
        Color(0xFFEF4444), // Red 500
        Color(0xFFF97316), // Orange 500
        Color(0xFFF59E0B), // Amber 500
        Color(0xFF10B981), // Emerald 500
        Color(0xFF3B82F6), // Blue 500
        Color(0xFF8B5CF6), // Violet 500
        Color(0xFFEC4899), // Pink 500
    )

    // Calculate Spline Chart Data dynamically based on filter selection
    val splineChartData = remember(transactions, timelineChartFilterMode, timelineSelectedYear, timelineSelectedMonth, startYear, startMonth, endYear, endMonth, startYearY2Y, endYearY2Y, language) {
        calculateSplineChartData(
            transactions = transactions,
            filterMode = timelineChartFilterMode,
            selectedYear = timelineSelectedYear,
            selectedMonth = timelineSelectedMonth,
            startYear = startYear,
            startMonth = startMonth,
            endYear = endYear,
            endMonth = endMonth,
            startYearY2Y = startYearY2Y,
            endYearY2Y = endYearY2Y,
            language = language
        )
    }

    val splineIncExpGradients = budgetGradients["SPLINE_INC_EXP"]
    val dataset1 = listOf(
        ChartDataset(
            label = if (language == AppLanguage.BN) "আয়" else "Income",
            data = splineChartData.incomeSums,
            color = splineIncExpGradients?.getOrNull(0) ?: Color(0xFF10B981)
        ),
        ChartDataset(
            label = if (language == AppLanguage.BN) "ব্যয়" else "Expense",
            data = splineChartData.expenseSums,
            color = splineIncExpGradients?.getOrNull(1) ?: Color(0xFFEF4444)
        )
    )

    val splineLendBorrGradients = budgetGradients["SPLINE_LEND_BORR"]
    val dataset2 = listOf(
        ChartDataset(
            label = if (language == AppLanguage.BN) "পাওনা" else "Lend",
            data = splineChartData.lendSums,
            color = splineLendBorrGradients?.getOrNull(0) ?: Color(0xFF3B82F6)
        ),
        ChartDataset(
            label = if (language == AppLanguage.BN) "দেনা" else "Borrow",
            data = splineChartData.borrowSums,
            color = splineLendBorrGradients?.getOrNull(1) ?: Color(0xFFFBBF24)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 4.dp, bottom = 200.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        androidx.activity.compose.BackHandler(onBack = onBack)

        // --- CONTROL CARD ---
        val cardBg = if (isDark) listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E)) else listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC))
        val textColor = if (isDark) Color.White else Color(0xFF1E293B)
        
        FintechGradientCard(
            gradientColors = cardBg,
            cornerRadius = 24.dp,
            padding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "ফিল্টার ও সময়কাল (পাইচার্ট)" else "Filters & Time Period (Pie Chart)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
                    )
                    
                    Icon(
                        imageVector = Icons.Rounded.FilterList,
                        contentDescription = "Filter",
                        tint = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                }
                

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomChartFilterChip(
                        selected = pieChartFilterMode == ChartFilterMode.DAY,
                        label = if (language == AppLanguage.BN) "দিন" else "Day",
                        onClick = { pieChartFilterMode = ChartFilterMode.DAY },
                        isDark = isDark
                    )
                    CustomChartFilterChip(
                        selected = pieChartFilterMode == ChartFilterMode.MONTH,
                        label = if (language == AppLanguage.BN) "মাস" else "Month",
                        onClick = { pieChartFilterMode = ChartFilterMode.MONTH },
                        isDark = isDark
                    )
                    CustomChartFilterChip(
                        selected = pieChartFilterMode == ChartFilterMode.YEAR,
                        label = if (language == AppLanguage.BN) "বছর" else "Year",
                        onClick = { pieChartFilterMode = ChartFilterMode.YEAR },
                        isDark = isDark
                    )
                    CustomChartFilterChip(
                        selected = pieChartFilterMode == ChartFilterMode.MONTH_TO_MONTH,
                        label = if (language == AppLanguage.BN) "মাস টু মাস" else "Month-to-Month",
                        onClick = { pieChartFilterMode = ChartFilterMode.MONTH_TO_MONTH },
                        isDark = isDark
                    )
                    CustomChartFilterChip(
                        selected = pieChartFilterMode == ChartFilterMode.YEAR_TO_YEAR,
                        label = if (language == AppLanguage.BN) "বছর টু বছর" else "Year-to-Year",
                        onClick = { pieChartFilterMode = ChartFilterMode.YEAR_TO_YEAR },
                        isDark = isDark
                    )
                }


                when (pieChartFilterMode) {
                    ChartFilterMode.MONTH -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SegmentedFilterPicker(
                                label = if (language == AppLanguage.BN) "নির্বাচিত মাস:" else "Selected Month:",
                                valueDisplay = formatMonthYearDisplay(pieSelectedMonth, pieSelectedYear, language),
                                onPrevious = {
                                    if (pieSelectedMonth == 0) {
                                        pieSelectedMonth = 11
                                        pieSelectedYear--
                                    } else {
                                        pieSelectedMonth--
                                    }
                                },
                                onNext = {
                                    if (pieSelectedMonth == 11) {
                                        pieSelectedMonth = 0
                                        pieSelectedYear++
                                    } else {
                                        pieSelectedMonth++
                                    }
                                },
                                isDark = isDark,
                                onClickCenter = {
                                    showMonthPickerForState = Pair(pieSelectedYear) { m, y ->
                                        pieSelectedMonth = m
                                        pieSelectedYear = y
                                    }
                                }
                            )
                        }
                    }
                    ChartFilterMode.YEAR -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SegmentedFilterPicker(
                                label = if (language == AppLanguage.BN) "নির্বাচিত বছর:" else "Selected Year:",
                                valueDisplay = formatYearDisplay(pieSelectedYear, language),
                                onPrevious = { pieSelectedYear-- },
                                onNext = { pieSelectedYear++ },
                                isDark = isDark,
                                onClickCenter = {
                                    showYearPickerForState = { y ->
                                        pieSelectedYear = y
                                    }
                                }
                            )
                        }
                    }
                    ChartFilterMode.DAY -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SegmentedFilterPicker(
                                label = if (language == AppLanguage.BN) "নির্বাচিত দিন:" else "Selected Day:",
                                valueDisplay = "${formatNumber(pieSelectedDay, language)} ${formatMonthYearDisplay(pieSelectedMonth, pieSelectedYear, language)}",
                                onPrevious = {
                                    val cal = java.util.Calendar.getInstance()
                                    cal.set(java.util.Calendar.YEAR, pieSelectedYear)
                                    cal.set(java.util.Calendar.MONTH, pieSelectedMonth)
                                    cal.set(java.util.Calendar.DAY_OF_MONTH, pieSelectedDay)
                                    cal.add(java.util.Calendar.DAY_OF_MONTH, -1)
                                    pieSelectedYear = cal.get(java.util.Calendar.YEAR)
                                    pieSelectedMonth = cal.get(java.util.Calendar.MONTH)
                                    pieSelectedDay = cal.get(java.util.Calendar.DAY_OF_MONTH)
                                },
                                onNext = {
                                    val cal = java.util.Calendar.getInstance()
                                    cal.set(java.util.Calendar.YEAR, pieSelectedYear)
                                    cal.set(java.util.Calendar.MONTH, pieSelectedMonth)
                                    cal.set(java.util.Calendar.DAY_OF_MONTH, pieSelectedDay)
                                    cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
                                    pieSelectedYear = cal.get(java.util.Calendar.YEAR)
                                    pieSelectedMonth = cal.get(java.util.Calendar.MONTH)
                                    pieSelectedDay = cal.get(java.util.Calendar.DAY_OF_MONTH)
                                },
                                isDark = isDark,
                                onClickCenter = {
                                    showDayPickerForState = Triple(pieSelectedMonth, pieSelectedYear) { d, m, y ->
                                        pieSelectedDay = d
                                        pieSelectedMonth = m
                                        pieSelectedYear = y
                                    }
                                }
                            )
                        }
                    }
                    ChartFilterMode.MONTH_TO_MONTH -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SegmentedFilterPicker(
                                label = if (language == AppLanguage.BN) "শুরুর মাস:" else "Start Month:",
                                valueDisplay = formatMonthYearDisplay(startMonth, startYear, language),
                                onPrevious = {
                                    if (startMonth == 0) {
                                        startMonth = 11
                                        startYear--
                                    } else {
                                        startMonth--
                                    }
                                },
                                onNext = {
                                    if (startMonth == 11) {
                                        startMonth = 0
                                        startYear++
                                    } else {
                                        startMonth++
                                    }
                                },
                                isDark = isDark,
                                onClickCenter = {
                                    showMonthPickerForState = Pair(startYear) { m, y ->
                                        startMonth = m
                                        startYear = y
                                    }
                                }
                            )
                            SegmentedFilterPicker(
                                label = if (language == AppLanguage.BN) "শেষের মাস:" else "End Month:",
                                valueDisplay = formatMonthYearDisplay(endMonth, endYear, language),
                                onPrevious = {
                                    if (endMonth == 0) {
                                        endMonth = 11
                                        endYear--
                                    } else {
                                        endMonth--
                                    }
                                },
                                onNext = {
                                    if (endMonth == 11) {
                                        endMonth = 0
                                        endYear++
                                    } else {
                                        endMonth++
                                    }
                                },
                                isDark = isDark,
                                onClickCenter = {
                                    showMonthPickerForState = Pair(endYear) { m, y ->
                                        endMonth = m
                                        endYear = y
                                    }
                                }
                            )
                        }
                    }
                    ChartFilterMode.YEAR_TO_YEAR -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SegmentedFilterPicker(
                                label = if (language == AppLanguage.BN) "শুরুর বছর:" else "Start Year:",
                                valueDisplay = formatYearDisplay(startYearY2Y, language),
                                onPrevious = { startYearY2Y-- },
                                onNext = { startYearY2Y++ },
                                isDark = isDark,
                                onClickCenter = {
                                    showYearPickerForState = { y ->
                                        startYearY2Y = y
                                    }
                                }
                            )
                            SegmentedFilterPicker(
                                label = if (language == AppLanguage.BN) "শেষের বছর:" else "End Year:",
                                valueDisplay = formatYearDisplay(endYearY2Y, language),
                                onPrevious = { endYearY2Y-- },
                                onNext = { endYearY2Y++ },
                                isDark = isDark,
                                onClickCenter = {
                                    showYearPickerForState = { y ->
                                        endYearY2Y = y
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // --- INCOME CHART ---
        ChartSection(
            title = if (language == AppLanguage.BN) "খাত অনুযায়ী আয়" else "Income by Category",
            data = incomesByCategory,
            total = totalIncome,
            palette = budgetGradients["CHART_INCOME_CAT"] ?: incomePalette,
            language = language,
            isDark = isDark,
            onLongPress = { onEditGradient?.invoke("CHART_INCOME_CAT") }
        )

        // --- EXPENSE CHART ---
        ChartSection(
            title = if (language == AppLanguage.BN) "খাত অনুযায়ী ব্যয়" else "Expense by Category",
            data = expensesByCategory,
            total = totalExpense,
            palette = budgetGradients["CHART_EXPENSE_CAT"] ?: expensePalette,
            language = language,
            isDark = isDark,
            onLongPress = { onEditGradient?.invoke("CHART_EXPENSE_CAT") }
        )


        // --- TIMELINE FILTER ---
        FintechGradientCard(
            gradientColors = cardBg,
            cornerRadius = 24.dp,
            padding = PaddingValues(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "ফিল্টার ও সময়কাল (টাইমলাইন চার্ট)" else "Filters & Time Period (Timeline Chart)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
                    )
                    
                    Icon(
                        imageVector = Icons.Rounded.FilterList,
                        contentDescription = "Filter",
                        tint = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CustomChartFilterChip(
                        selected = timelineChartFilterMode == ChartFilterMode.DAY,
                        label = if (language == AppLanguage.BN) "দিন" else "Day",
                        onClick = { timelineChartFilterMode = ChartFilterMode.DAY },
                        isDark = isDark
                    )
                    CustomChartFilterChip(
                        selected = timelineChartFilterMode == ChartFilterMode.MONTH,
                        label = if (language == AppLanguage.BN) "মাস" else "Month",
                        onClick = { timelineChartFilterMode = ChartFilterMode.MONTH },
                        isDark = isDark
                    )
                    CustomChartFilterChip(
                        selected = timelineChartFilterMode == ChartFilterMode.YEAR,
                        label = if (language == AppLanguage.BN) "বছর" else "Year",
                        onClick = { timelineChartFilterMode = ChartFilterMode.YEAR },
                        isDark = isDark
                    )
                    CustomChartFilterChip(
                        selected = timelineChartFilterMode == ChartFilterMode.MONTH_TO_MONTH,
                        label = if (language == AppLanguage.BN) "মাস টু মাস" else "Month-to-Month",
                        onClick = { timelineChartFilterMode = ChartFilterMode.MONTH_TO_MONTH },
                        isDark = isDark
                    )
                    CustomChartFilterChip(
                        selected = timelineChartFilterMode == ChartFilterMode.YEAR_TO_YEAR,
                        label = if (language == AppLanguage.BN) "বছর টু বছর" else "Year-to-Year",
                        onClick = { timelineChartFilterMode = ChartFilterMode.YEAR_TO_YEAR },
                        isDark = isDark
                    )
                }

                when (timelineChartFilterMode) {
                    ChartFilterMode.DAY -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SegmentedFilterPicker(
                                label = if (language == AppLanguage.BN) "নির্বাচিত মাস:" else "Selected Month:",
                                valueDisplay = formatMonthYearDisplay(timelineSelectedMonth, timelineSelectedYear, language),
                                onPrevious = {
                                    if (timelineSelectedMonth == 0) {
                                        timelineSelectedMonth = 11
                                        timelineSelectedYear--
                                    } else {
                                        timelineSelectedMonth--
                                    }
                                },
                                onNext = {
                                    if (timelineSelectedMonth == 11) {
                                        timelineSelectedMonth = 0
                                        timelineSelectedYear++
                                    } else {
                                        timelineSelectedMonth++
                                    }
                                },
                                isDark = isDark,
                                onClickCenter = {
                                    showMonthPickerForState = Pair(timelineSelectedYear) { m, y ->
                                        timelineSelectedMonth = m
                                        timelineSelectedYear = y
                                    }
                                }
                            )
                        }
                    }
                    ChartFilterMode.MONTH -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SegmentedFilterPicker(
                                label = if (language == AppLanguage.BN) "নির্বাচিত বছর:" else "Selected Year:",
                                valueDisplay = formatYearDisplay(timelineSelectedYear, language),
                                onPrevious = { timelineSelectedYear-- },
                                onNext = { timelineSelectedYear++ },
                                isDark = isDark,
                                onClickCenter = {
                                    showYearPickerForState = { y ->
                                        timelineSelectedYear = y
                                    }
                                }
                            )
                        }
                    }
                    ChartFilterMode.YEAR -> {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SegmentedFilterPicker(
                                label = if (language == AppLanguage.BN) "নির্বাচিত বছর:" else "Selected Year:",
                                valueDisplay = formatYearDisplay(timelineSelectedYear, language),
                                onPrevious = { timelineSelectedYear-- },
                                onNext = { timelineSelectedYear++ },
                                isDark = isDark,
                                onClickCenter = {
                                    showYearPickerForState = { y ->
                                        timelineSelectedYear = y
                                    }
                                }
                            )
                        }
                    }
                    ChartFilterMode.MONTH_TO_MONTH -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SegmentedFilterPicker(
                                label = if (language == AppLanguage.BN) "শুরুর মাস:" else "Start Month:",
                                valueDisplay = formatMonthYearDisplay(startMonth, startYear, language),
                                onPrevious = {
                                    if (startMonth == 0) {
                                        startMonth = 11
                                        startYear--
                                    } else {
                                        startMonth--
                                    }
                                },
                                onNext = {
                                    if (startMonth == 11) {
                                        startMonth = 0
                                        startYear++
                                    } else {
                                        startMonth++
                                    }
                                },
                                isDark = isDark,
                                onClickCenter = {
                                    showMonthPickerForState = Pair(startYear) { m, y ->
                                        startMonth = m
                                        startYear = y
                                    }
                                }
                            )
                            SegmentedFilterPicker(
                                label = if (language == AppLanguage.BN) "শেষের মাস:" else "End Month:",
                                valueDisplay = formatMonthYearDisplay(endMonth, endYear, language),
                                onPrevious = {
                                    if (endMonth == 0) {
                                        endMonth = 11
                                        endYear--
                                    } else {
                                        endMonth--
                                    }
                                },
                                onNext = {
                                    if (endMonth == 11) {
                                        endMonth = 0
                                        endYear++
                                    } else {
                                        endMonth++
                                    }
                                },
                                isDark = isDark,
                                onClickCenter = {
                                    showMonthPickerForState = Pair(endYear) { m, y ->
                                        endMonth = m
                                        endYear = y
                                    }
                                }
                            )
                        }
                    }
                    ChartFilterMode.YEAR_TO_YEAR -> {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SegmentedFilterPicker(
                                label = if (language == AppLanguage.BN) "শুরুর বছর:" else "Start Year:",
                                valueDisplay = formatYearDisplay(startYearY2Y, language),
                                onPrevious = { startYearY2Y-- },
                                onNext = { startYearY2Y++ },
                                isDark = isDark,
                                onClickCenter = {
                                    showYearPickerForState = { y ->
                                        startYearY2Y = y
                                    }
                                }
                            )
                            SegmentedFilterPicker(
                                label = if (language == AppLanguage.BN) "শেষের বছর:" else "End Year:",
                                valueDisplay = formatYearDisplay(endYearY2Y, language),
                                onPrevious = { endYearY2Y-- },
                                onNext = { endYearY2Y++ },
                                isDark = isDark,
                                onClickCenter = {
                                    showYearPickerForState = { y ->
                                        endYearY2Y = y
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Display Header Label based on current state
        val displayPeriodLabel = when (timelineChartFilterMode) {
            ChartFilterMode.MONTH -> formatYearDisplay(timelineSelectedYear, language)
            ChartFilterMode.YEAR -> "${formatYearDisplay(timelineSelectedYear - 4, language)} - ${formatYearDisplay(timelineSelectedYear, language)}"
            ChartFilterMode.MONTH_TO_MONTH -> "${formatMonthYearDisplay(startMonth, startYear, language)} - ${formatMonthYearDisplay(endMonth, endYear, language)}"
            ChartFilterMode.YEAR_TO_YEAR -> "${formatYearDisplay(startYearY2Y, language)} - ${formatYearDisplay(endYearY2Y, language)}"
            ChartFilterMode.DAY -> formatMonthYearDisplay(timelineSelectedMonth, timelineSelectedYear, language)
        }

        val nowCal = java.util.Calendar.getInstance()
        val cYear = nowCal.get(java.util.Calendar.YEAR)
        val cMonth = nowCal.get(java.util.Calendar.MONTH)
        val cDay = nowCal.get(java.util.Calendar.DAY_OF_MONTH)

        val targetScrollIndex = remember(
            timelineChartFilterMode,
            timelineSelectedYear,
            timelineSelectedMonth,
            startYear,
            startMonth,
            endYear,
            endMonth,
            startYearY2Y,
            endYearY2Y,
            splineChartData.labels
        ) {
            when (timelineChartFilterMode) {
                ChartFilterMode.DAY -> {
                    if (timelineSelectedYear == cYear && timelineSelectedMonth == cMonth) {
                        (cDay - 1).coerceIn(0, splineChartData.labels.size - 1)
                    } else if (timelineSelectedYear < cYear || (timelineSelectedYear == cYear && timelineSelectedMonth < cMonth)) {
                        splineChartData.labels.size - 1
                    } else {
                        0
                    }
                }
                ChartFilterMode.MONTH -> {
                    if (timelineSelectedYear == cYear) {
                        cMonth.coerceIn(0, splineChartData.labels.size - 1)
                    } else if (timelineSelectedYear < cYear) {
                        splineChartData.labels.size - 1
                    } else {
                        0
                    }
                }
                ChartFilterMode.YEAR -> {
                    val startYr = timelineSelectedYear - 4
                    if (cYear in startYr..timelineSelectedYear) {
                        (cYear - startYr).coerceIn(0, splineChartData.labels.size - 1)
                    } else if (cYear > timelineSelectedYear) {
                        splineChartData.labels.size - 1
                    } else {
                        0
                    }
                }
                ChartFilterMode.MONTH_TO_MONTH -> {
                    var targetIdx = -1
                    var curYr = startYear
                    var curMn = startMonth
                    val endTotalMonths = endYear * 12 + endMonth
                    val currentTotalMonths = cYear * 12 + cMonth
                    
                    var count = 0
                    while ((curYr * 12 + curMn <= endTotalMonths) && count < 24) {
                        if (curYr == cYear && curMn == cMonth) {
                            targetIdx = count
                            break
                        }
                        curMn++
                        if (curMn > 11) {
                            curMn = 0
                            curYr++
                        }
                        count++
                    }
                    if (targetIdx != -1) {
                        targetIdx.coerceIn(0, splineChartData.labels.size - 1)
                    } else {
                        if (currentTotalMonths > endTotalMonths) {
                            splineChartData.labels.size - 1
                        } else {
                            0
                        }
                    }
                }
                ChartFilterMode.YEAR_TO_YEAR -> {
                    val sYr = startYearY2Y
                    val eYr = if (endYearY2Y < startYearY2Y) startYearY2Y else endYearY2Y
                    val yearsRange = (sYr..eYr).take(10)
                    val foundIdx = yearsRange.indexOf(cYear)
                    if (foundIdx != -1) {
                        foundIdx.coerceIn(0, splineChartData.labels.size - 1)
                    } else {
                        if (cYear > eYr) {
                            splineChartData.labels.size - 1
                        } else {
                            0
                        }
                    }
                }
            }
        }

        // --- TIMELINE SPLINE CHART 1: INCOME & EXPENSE ---
        TimelineSplineChart(
            title = if (language == AppLanguage.BN) "আয় ও ব্যয় ট্রেন্ড ($displayPeriodLabel)" else "Income & Expense Trend ($displayPeriodLabel)",
            datasets = dataset1,
            monthsLabels = splineChartData.labels,
            language = language,
            isDark = isDark,
            targetIndex = targetScrollIndex,
            onLongPress = { onEditGradient?.invoke("SPLINE_INC_EXP") }
        )

        // --- TIMELINE SPLINE CHART 2: LEND & BORROW ---
        TimelineSplineChart(
            title = if (language == AppLanguage.BN) "দেনা ও পাওনা ট্রেন্ড ($displayPeriodLabel)" else "Lend & Borrow Trend ($displayPeriodLabel)",
            datasets = dataset2,
            monthsLabels = splineChartData.labels,
            language = language,
            isDark = isDark,
            targetIndex = targetScrollIndex,
            onLongPress = { onEditGradient?.invoke("SPLINE_LEND_BORR") }
        )
        
        Spacer(modifier = Modifier.height(90.dp))
    }

    // --- Manual selection Pickers ---
    if (showYearPickerForState != null) {
        val onYearSelected = showYearPickerForState!!
        AlertDialog(
            onDismissRequest = { showYearPickerForState = null },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showYearPickerForState = null }) {
                    Text(if (language == AppLanguage.BN) "বাতিল" else "Cancel")
                }
            },
            title = {
                Text(
                    text = if (language == AppLanguage.BN) "বছর নির্বাচন করুন" else "Select Year",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (isDark) Color.White else Color(0xFF1E293B)
                )
            },
            text = {
                val years = (currentYear - 10..currentYear + 5).toList()
                                    LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(years) { year ->
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onYearSelected(year)
                                    showYearPickerForState = null
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = formatYearDisplay(year, language),
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                        }
                    }
                }
            },
            containerColor = if (isDark) Color(0xFF1C1C1E) else Color.White
        )
    }

    if (showMonthPickerForState != null) {
        val initialYear = showMonthPickerForState!!.first
        val onMonthSelected = showMonthPickerForState!!.second
        var tempYear by remember { mutableStateOf(initialYear) }

        AlertDialog(
            onDismissRequest = { showMonthPickerForState = null },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMonthPickerForState = null }) {
                    Text(if (language == AppLanguage.BN) "বাতিল" else "Cancel")
                }
            },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "মাস নির্বাচন করুন" else "Select Month",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { tempYear-- }) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Prev Year")
                        }
                        Text(
                            text = formatYearDisplay(tempYear, language),
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        IconButton(onClick = { tempYear++ }) {
                            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next Year")
                        }
                    }
                }
            },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.height(220.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(12) { mIndex ->
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onMonthSelected(mIndex, tempYear)
                                    showMonthPickerForState = null
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (language == AppLanguage.BN) bnMonthNamesFull[mIndex] else enMonthNamesFull[mIndex],
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E293B),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            },
            containerColor = if (isDark) Color(0xFF1C1C1E) else Color.White
        )
    }

    if (showDayPickerForState != null) {
        val initialMonth = showDayPickerForState!!.first
        val initialYear = showDayPickerForState!!.second
        val onDaySelected = showDayPickerForState!!.third
        
        var tempMonth by remember { mutableStateOf(initialMonth) }
        var tempYear by remember { mutableStateOf(initialYear) }
        
        val daysInMonth = remember(tempMonth, tempYear) {
            java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.YEAR, tempYear)
                set(java.util.Calendar.MONTH, tempMonth)
            }.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        }

        AlertDialog(
            onDismissRequest = { showDayPickerForState = null },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDayPickerForState = null }) {
                    Text(if (language == AppLanguage.BN) "বাতিল" else "Cancel")
                }
            },
            title = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (language == AppLanguage.BN) "দিন নির্বাচন করুন" else "Select Day",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                if (tempMonth == 0) {
                                    tempMonth = 11
                                    tempYear--
                                } else {
                                    tempMonth--
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Prev Month")
                            }
                            Text(
                                text = if (language == AppLanguage.BN) bnMonthNamesFull[tempMonth] else enMonthNamesFull[tempMonth],
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                            IconButton(onClick = {
                                if (tempMonth == 11) {
                                    tempMonth = 0
                                    tempYear++
                                } else {
                                    tempMonth++
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next Month")
                            }
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { tempYear-- }) {
                                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Prev Year")
                            }
                            Text(
                                text = formatYearDisplay(tempYear, language),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                            IconButton(onClick = { tempYear++ }) {
                                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next Year")
                            }
                        }
                    }
                }
            },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.height(220.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(daysInMonth) { dayIdx ->
                        val dayNum = dayIdx + 1
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    onDaySelected(dayNum, tempMonth, tempYear)
                                    showDayPickerForState = null
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = formatNumber(dayNum, language),
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color(0xFF1E293B)
                            )
                        }
                    }
                }
            },
            containerColor = if (isDark) Color(0xFF1C1C1E) else Color.White
        )
    }
}

@Composable
fun ChartSection(
    title: String,
    data: Map<String, Double>,
    total: Double,
    palette: List<Color>,
    language: AppLanguage,
    isDark: Boolean = true,
    onLongPress: () -> Unit = {}
) {
    val bgColor = if (isDark) listOf(Color(0xFF1C1C1E), Color(0xFF1C1C1E)) else listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
    val textColor = if (isDark) Color.White else Color(0xFF1E293B)
    
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationPlayed = true
    }
    val animatedProgressMultiplier by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "chart_donut_animation"
    )

    val centerBgColor = FintechBlue.copy(alpha = 0.1f)

    FintechGradientCard(
        gradientColors = bgColor,
        cornerRadius = 24.dp,
        padding = PaddingValues(20.dp),
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongPress() })
            }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = Color(0xFF3B82F6),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (total == 0.0) {
                Box(modifier = Modifier.height(150.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (language == AppLanguage.BN) "কোন তথ্য নেই" else "No Data Available",
                        color = textColor.copy(alpha = 0.6f)
                    )
                }
            } else {
                val values = data.values.map { it.toFloat() }
                val labels = data.keys.toList()
                val totalFloat = total.toFloat()

                Row(
                     modifier = Modifier.fillMaxWidth(),
                     verticalAlignment = Alignment.CenterVertically,
                     horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val sizeMin = size.minDimension
                                val strokeWidthPx = 16.dp.toPx()
                                // Subtract 16.dp to leave 8.dp space for glowing shadow
                                val radius = (sizeMin - strokeWidthPx - 16.dp.toPx()) / 2f

                                // Draw central hollow background
                                val innerRadius = radius - strokeWidthPx / 2f
                                if (innerRadius > 0f) {
                                    drawCircle(
                                        color = centerBgColor,
                                        radius = innerRadius,
                                        center = center
                                    )
                                }

                                // Draw active segments with flat caps and gap-less design
                                if (totalFloat > 0f) {
                                    var startAngle = -90f
                                    val arcTopLeft = androidx.compose.ui.geometry.Offset(center.x - radius, center.y - radius)
                                    val arcSize = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f)

                                    val validValues = values.filter { it > 0f }
                                    validValues.forEachIndexed { index, value ->
                                        val sweepAngle = ((value / totalFloat) * 360f) * animatedProgressMultiplier
                                        val color = palette[index % palette.size]

                                        // Soft glowing shadow extending ONLY outwards (blurred)
                                        // Android 8.1 compatible natural outward glow with smooth gradient fade
                                        val glowLayers = 100
                                        val glowSize = 6.dp.toPx()
                                        for (i in glowLayers downTo 1) {
                                            val fraction = i.toFloat() / glowLayers
                                            val currentGlowWidth = glowSize * fraction
                                            val glowRadius = radius + (strokeWidthPx / 2f) + (currentGlowWidth / 2f)
                                            // Linear fade for smoother outer transition and blurrier falloff
                                            val alpha = 0.019f * (1.0f - fraction)
                                            drawArc(
                                                color = color.copy(alpha = alpha.coerceAtLeast(0.001f)),
                                                startAngle = startAngle,
                                                sweepAngle = sweepAngle + 0.8f,
                                                useCenter = false,
                                                topLeft = androidx.compose.ui.geometry.Offset(center.x - glowRadius, center.y - glowRadius),
                                                size = androidx.compose.ui.geometry.Size(glowRadius * 2f, glowRadius * 2f),
                                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = currentGlowWidth, cap = androidx.compose.ui.graphics.StrokeCap.Butt)
                                            )
                                        }

                                        drawArc(
                                            color = color,
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle + 0.8f,
                                            useCenter = false,
                                            topLeft = arcTopLeft,
                                            size = arcSize,
                                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
                                        )
                                        
                                        startAngle += sweepAngle
                                    }
                                }
                            }
                            
                            // Center Amount Text
                            Text(
                                text = formatCurrency(total, language),
                                color = if (isDark) Color.White else Color(0xFF1E293B),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        labels.forEachIndexed { index, label ->
                            val value = data[label] ?: 0.0
                            val percent = ((value / total) * 100).toInt()
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(palette[index % palette.size])
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = label,
                                        color = textColor,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${formatCurrency(value, language)} ($percent%)",
                                        color = textColor.copy(alpha = 0.7f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }    
    }
}

@Composable
fun SplashScreen(isDark: Boolean) {
    // Force white background as requested
    val bgColor = Color.White
    
    // Pulse animation for the logo, optimized within bounds (0.85f to 1.0f) to prevent clipping
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.00f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoScale"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        val screenWidth = maxWidth
        // App Logo with pulsing animation, positioned slightly above center
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-40).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.app_logo_new),
                contentDescription = "App Logo",
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                modifier = Modifier
                    .size(screenWidth * 0.45f) // Optimized size with safe padding to ensure no clipping
                    .padding(12.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale
                    )
            )
        }

        // Modern loading circle at the bottom
        CircularProgressIndicator(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
                .size(44.dp),
            color = FintechBlue,
            strokeWidth = 4.dp,
            trackColor = Color.Black.copy(alpha = 0.05f)
        )
    }
}



@Composable
fun GoogleDriveRestoreListDialog(
    language: AppLanguage,
    isDark: Boolean,
    isFetching: Boolean,
    files: List<com.example.data.GoogleDriveFile>,
    viewModel: com.example.ui.viewmodel.FinanceViewModel,
    onDismiss: () -> Unit,
    onRestoreSuccess: () -> Unit,
    onDelete: (String) -> Unit,
    onRefresh: () -> Unit,
    executeWithInternetCheck: (((() -> Unit) -> Unit))? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var fileToDelete by remember { mutableStateOf<com.example.data.GoogleDriveFile?>(null) }

    var pendingCloudRestoreData by remember { mutableStateOf<com.example.data.FinanceBackup?>(null) }
    var pendingCloudRestoreStats by remember { mutableStateOf<com.example.ui.viewmodel.BackupStats?>(null) }
    var pendingCloudRestoreFileName by remember { mutableStateOf("") }
    var pendingCloudRestoreJson by remember { mutableStateOf("") }
    val workspaceStatsList by viewModel.workspaceStatsList.collectAsState(initial = emptyList())
    var isDownloadingByFileId by remember { mutableStateOf<String?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.let { outputStream ->
                viewModel.exportBackupToUri(context, outputStream, "Auto Backup before Cloud Restore", null, {
                    viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "ব্যাকআপ সফলভাবে সেভ হয়েছে!" else "Backup successfully saved!", isSuccess = true, type = "SUCCESS")
                }, { error ->
                    viewModel.triggerCustomNotification("Error: $error", isSuccess = false, type = "ERROR")
                })
            }
        }
    }

    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(16.dp)),
            color = if (isDark) Color(0xFF121212) else Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.BN) "ক্লাউড ব্যাকআপ ফাইলসমূহ" else "Cloud Backup Files",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = FintechBlue)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = if (isDark) Color.White else Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isFetching) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FintechBlue)
                    }
                } else if (files.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.CloudOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = if (isDark) Color.DarkGray else Color.LightGray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (language == AppLanguage.BN) "কোনো ব্যাকআপ ফাইল পাওয়া যায়নি" else "No backup files found",
                                color = if (isDark) Color.Gray else Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(files) { file ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {
                                        val downloadAction = {
                                            isDownloadingByFileId = file.id
                                            viewModel.downloadGoogleDriveFile(
                                                context = context,
                                                fileId = file.id,
                                                onSuccess = { jsonContent ->
                                                    isDownloadingByFileId = null
                                                    val parsed = viewModel.parseBackupJson(jsonContent)
                                                    if (parsed != null) {
                                                        pendingCloudRestoreData = parsed
                                                        pendingCloudRestoreStats = viewModel.calculateBackupStats(parsed)
                                                        pendingCloudRestoreFileName = file.name
                                                        pendingCloudRestoreJson = jsonContent
                                                    } else {
                                                        viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "ভুল ব্যাকআপ ফরম্যাট" else "Invalid backup format", isSuccess = false, type = "ERROR")
                                                    }
                                                },
                                                onError = { err ->
                                                    isDownloadingByFileId = null
                                                    viewModel.triggerCustomNotification("${if (language == AppLanguage.BN) "ডাউনলোড ব্যর্থ হয়েছে: " else "Download failed: "}$err", isSuccess = false, type = "ERROR")
                                                }
                                            )
                                        }
                                        if (executeWithInternetCheck != null) {
                                            executeWithInternetCheck(downloadAction)
                                        } else {
                                            downloadAction()
                                        }
                                    },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF121212) else Color(0xFFF1F5F9)
                                ),
                                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.InsertDriveFile,
                                        contentDescription = null,
                                        tint = FintechBlue,
                                        modifier = Modifier.size(28.dp)
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        // Nicely formatted name
                                        val displayName = if (file.name.startsWith("finance_note_backup_")) {
                                            val parts = file.name.removePrefix("finance_note_backup_").removeSuffix(".json")
                                            if (parts.length >= 15) { // yyyyMMdd_HHmmss
                                                val dateStr = parts.substring(0, 8)
                                                val timeStr = parts.substring(9)
                                                val yr = dateStr.substring(0, 4)
                                                val mn = dateStr.substring(4, 6)
                                                val dy = dateStr.substring(6, 8)
                                                val hh = timeStr.substring(0, 2)
                                                val mm = timeStr.substring(2, 4)
                                                val ss = timeStr.substring(4, 6)
                                                if (language == AppLanguage.BN) {
                                                    "ব্যাকআপ: $dy-$mn-$yr $hh:$mm:$ss"
                                                } else {
                                                    "Backup: $dy/$mn/$yr $hh:$mm:$ss"
                                                }
                                            } else {
                                                file.name
                                            }
                                        } else {
                                            file.name
                                        }

                                        Text(
                                            text = displayName,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp,
                                            color = if (isDark) Color.White else Color(0xFF1E293B)
                                        )

                                        // Size and Date Info
                                        val sizeStr = if (!file.size.isNullOrEmpty()) {
                                            val kb = file.size.toLongOrNull()?.let { it / 1024 } ?: 0
                                            "$kb KB"
                                        } else ""

                                        Text(
                                            text = sizeStr,
                                            fontSize = 11.sp,
                                            color = if (isDark) Color.Gray else Color(0xFF64748B)
                                        )
                                    }

                                    // Action buttons
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = {
                                                val downloadAction = {
                                                    isDownloadingByFileId = file.id
                                                    viewModel.downloadGoogleDriveFile(
                                                        context = context,
                                                        fileId = file.id,
                                                        onSuccess = { jsonContent ->
                                                            isDownloadingByFileId = null
                                                            val parsed = viewModel.parseBackupJson(jsonContent)
                                                            if (parsed != null) {
                                                                pendingCloudRestoreData = parsed
                                                                pendingCloudRestoreStats = viewModel.calculateBackupStats(parsed)
                                                                pendingCloudRestoreFileName = file.name
                                                                pendingCloudRestoreJson = jsonContent
                                                            } else {
                                                                viewModel.triggerCustomNotification(if (language == AppLanguage.BN) "ভুল ব্যাকআপ ফরম্যাট" else "Invalid backup format", isSuccess = false, type = "ERROR")
                                                            }
                                                        },
                                                        onError = { err ->
                                                            isDownloadingByFileId = null
                                                            viewModel.triggerCustomNotification("Download failed: $err", isSuccess = false, type = "ERROR")
                                                        }
                                                    )
                                                }
                                                if (executeWithInternetCheck != null) {
                                                    executeWithInternetCheck(downloadAction)
                                                } else {
                                                    downloadAction()
                                                }
                                            },
                                            enabled = isDownloadingByFileId == null
                                        ) {
                                            if (isDownloadingByFileId == file.id) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = FintechBlue)
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Rounded.Restore,
                                                    contentDescription = "Restore",
                                                    tint = FintechBlue,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { fileToDelete = file }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Delete,
                                                contentDescription = "Delete",
                                                tint = FintechRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Backup stats dialog for cloud restore
    if (pendingCloudRestoreStats != null && pendingCloudRestoreData != null) {
        BackupStatsDialog(
            title = if (language == AppLanguage.BN) "ক্লাউড রিস্টোর সামারি" else "Cloud Restore Summary",
            stats = pendingCloudRestoreStats!!,
            language = language,
            isDark = isDark,
            isRestoreMode = true,
            initialFileName = pendingCloudRestoreFileName,
            workspaces = workspaceStatsList,
            onBackupRequested = {
                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                createDocumentLauncher.launch("financenote_backup_before_restore_$timestamp.json")
            },
            onConfirm = { _, _, selectedWorkspaceIds ->
                viewModel.importBackup(
                    context = context,
                    json = pendingCloudRestoreJson,
                    fromLocalFile = false,
                    workspaceIds = selectedWorkspaceIds,
                    onSuccess = {
                        pendingCloudRestoreStats = null
                        pendingCloudRestoreData = null
                        pendingCloudRestoreJson = ""
                        onRestoreSuccess()
                    },
                    onError = { err ->
                        viewModel.triggerCustomNotification("Error: $err", isSuccess = false, type = "ERROR")
                    }
                )
            },
            onDismiss = {
                pendingCloudRestoreStats = null
                pendingCloudRestoreData = null
                pendingCloudRestoreJson = ""
            }
        )
    }

    // Captcha delete confirmation dialog
    fileToDelete?.let { file ->
        CaptchaDeleteDialog(
            fileName = file.name,
            language = language,
            isDark = isDark,
            onConfirm = {
                onDelete(file.id)
                fileToDelete = null
            },
            onDismiss = { fileToDelete = null }
        )
        Spacer(modifier = Modifier.height(90.dp))
    }
}

fun formatSyncTime(timestamp: Long?, language: AppLanguage): String {
    if (timestamp == null || timestamp == 0L) {
        return if (language == AppLanguage.BN) "কখনো সিঙ্ক হয়নি" else "Never synced"
    }
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
    val formatted = sdf.format(java.util.Date(timestamp))
    if (language == AppLanguage.BN) {
        return formatted
            .replace("AM", "পূর্বাহ্ণ")
            .replace("PM", "অপরাহ্ণ")
            .replace("Jan", "জানুয়ারি")
            .replace("Feb", "ফেব্রুয়ারি")
            .replace("Mar", "মার্চ")
            .replace("Apr", "এপ্রিল")
            .replace("May", "মে")
            .replace("Jun", "জুন")
            .replace("Jul", "জুলাই")
            .replace("Aug", "আগস্ট")
            .replace("Sep", "সেপ্টেম্বর")
            .replace("Oct", "অক্টোবর")
            .replace("Nov", "নভেম্বর")
            .replace("Dec", "ডিসেম্বর")
    }
    return formatted
}

@Composable
fun CustomNotificationOverlay(
    notification: com.example.ui.viewmodel.CustomNotification,
    language: AppLanguage,
    isDark: Boolean
) {
    val bgColor = if (notification.isSuccess) {
        if (isDark) Color(0xFF065F46) else Color(0xFFD1FAE5)
    } else {
        if (isDark) Color(0xFF991B1B) else Color(0xFFFEE2E2)
    }
    
    val contentColor = if (notification.isSuccess) {
        if (isDark) Color(0xFF34D399) else Color(0xFF065F46)
    } else {
        if (isDark) Color(0xFFF87171) else Color(0xFF991B1B)
    }

    val icon = when (notification.type) {
        "SUCCESS" -> Icons.Rounded.CheckCircle
        "ERROR" -> Icons.Rounded.Error
        "SIGN_IN" -> Icons.AutoMirrored.Rounded.Login
        "SIGN_OUT" -> Icons.AutoMirrored.Rounded.Logout
        "SYNC" -> Icons.Rounded.Sync
        "BACKUP" -> Icons.Rounded.CloudUpload
        "RESTORE" -> Icons.Rounded.CloudDownload
        else -> Icons.Rounded.Info
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = notification.message,
                color = contentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun BudgetHistoryDialog(
    language: AppLanguage,
    isDark: Boolean,
    transactions: List<Transaction>,
    savingsTransactions: List<com.example.data.SavingsTransaction>,
    budgetIncome: Double,
    budgetExpense: Double,
    budgetSavings: Double,
    onDismiss: () -> Unit
) {
    // Grouping helper
    val getYearMonthString: (Long) -> String = { timestamp ->
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timestamp
        val year = cal.get(java.util.Calendar.YEAR)
        val month = cal.get(java.util.Calendar.MONTH) + 1
        String.format("%04d-%02d", year, month)
    }

    val allMonthKeys = remember(transactions, savingsTransactions) {
        val keys = mutableSetOf<String>()
        transactions.forEach { keys.add(getYearMonthString(it.timestamp)) }
        savingsTransactions.forEach { keys.add(getYearMonthString(it.timestamp)) }
        keys.add(getYearMonthString(System.currentTimeMillis()))
        keys.toList().sortedDescending()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = if (language == AppLanguage.BN) "বন্ধ করুন" else "Close",
                    color = FintechBlue,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Rounded.History,
                    contentDescription = null,
                    tint = FintechBlue,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = if (language == AppLanguage.BN) "বাজেট পূরণের ইতিহাস" else "Budget Fulfillment History",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                if (allMonthKeys.isEmpty()) {
                    Text(
                        text = if (language == AppLanguage.BN) "কোন তথ্য পাওয়া যায়নি" else "No data available",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allMonthKeys, key = { it }) { monthKey ->
                            Box(modifier = Modifier.animateItem()) {
                                val displayMonth = getCustomTimeFilterLabel("CUSTOM_MONTH:$monthKey", language)
                            
                            val monthlyIncome = remember(transactions, monthKey) {
                                transactions.filter { (it.type == "INCOME" || (it.type == "LEND" && it.subType == "CREDIT")) && getYearMonthString(it.timestamp) == monthKey }
                                    .sumOf { it.amount }
                            }
                            val monthlyExpense = remember(transactions, monthKey) {
                                transactions.filter { (it.type == "EXPENSE" || (it.type == "BORROW" && it.subType == "CREDIT")) && getYearMonthString(it.timestamp) == monthKey }
                                    .sumOf { it.amount }
                            }
                            val monthlySavings = remember(savingsTransactions, monthKey) {
                                savingsTransactions.filter { getYearMonthString(it.timestamp) == monthKey }
                                    .sumOf { if (it.isDeposit) it.amount else -it.amount }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF121212) else Color(0xFFF8FAFC)
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Month Header
                                    Text(
                                        text = displayMonth,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FintechBlue
                                    )

                                    // 1. Income Progress
                                    val incProgress = if (budgetIncome > 0) (monthlyIncome / budgetIncome).coerceIn(0.0, 1.0).toFloat() else 0f
                                    val incPercentText = if (budgetIncome > 0) "${(monthlyIncome / budgetIncome * 100).toInt()}%" else if (language == AppLanguage.BN) "সেট করুন" else "Set"
                                    
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.BN) "আয়" else "Income",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                text = "${formatNumber(monthlyIncome.toInt(), language)} ৳ / ${if (budgetIncome > 0) formatNumber(budgetIncome.toInt(), language) + " ৳" else ""} ($incPercentText)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (budgetIncome > 0 && monthlyIncome >= budgetIncome * 0.8) Color(0xFF10B981) else if (isDark) Color.White else Color.Black
                                            )
                                        }
                                        if (budgetIncome > 0) {
                                            LinearProgressIndicator(
                                                progress = { incProgress },
                                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                                color = Color(0xFF10B981),
                                                trackColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE2E8F0)
                                            )
                                        }
                                    }

                                    // 2. Expense Progress
                                    val expProgress = if (budgetExpense > 0) (monthlyExpense / budgetExpense).coerceIn(0.0, 1.0).toFloat() else 0f
                                    val expPercentText = if (budgetExpense > 0) "${(monthlyExpense / budgetExpense * 100).toInt()}%" else if (language == AppLanguage.BN) "সেট করুন" else "Set"
                                    val isExpOverLimit = budgetExpense > 0 && monthlyExpense >= budgetExpense * 0.8

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.BN) "ব্যয়" else "Expense",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                text = "${formatNumber(monthlyExpense.toInt(), language)} ৳ / ${if (budgetExpense > 0) formatNumber(budgetExpense.toInt(), language) + " ৳" else ""} ($expPercentText)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isExpOverLimit) Color(0xFFEF4444) else if (isDark) Color.White else Color.Black
                                            )
                                        }
                                        if (budgetExpense > 0) {
                                            LinearProgressIndicator(
                                                progress = { expProgress },
                                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                                color = if (isExpOverLimit) Color(0xFFEF4444) else FintechBlue,
                                                trackColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE2E8F0)
                                            )
                                        }
                                    }

                                    // 3. Savings Progress
                                    val savProgress = if (budgetSavings > 0) (monthlySavings / budgetSavings).coerceIn(0.0, 1.0).toFloat() else 0f
                                    val savPercentText = if (budgetSavings > 0) "${(monthlySavings / budgetSavings * 100).toInt()}%" else if (language == AppLanguage.BN) "সেট করুন" else "Set"

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.BN) "সঞ্চয়" else "Savings",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                text = "${formatNumber(monthlySavings.toInt(), language)} ৳ / ${if (budgetSavings > 0) formatNumber(budgetSavings.toInt(), language) + " ৳" else ""} ($savPercentText)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (budgetSavings > 0 && monthlySavings >= budgetSavings * 0.8) Color(0xFF10B981) else if (isDark) Color.White else Color.Black
                                            )
                                        }
                                        if (budgetSavings > 0) {
                                            LinearProgressIndicator(
                                                progress = { savProgress },
                                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                                color = Color(0xFF3B82F6),
                                                trackColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFE2E8F0)
                                            )
                                        }
                                    }
                                }
                            }
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = if (isDark) Color(0xFF121212) else Color.White,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = true)
    )
}

@Composable
fun SyncStatusBadge(
    isNetworkAvailable: Boolean,
    hasUnsavedChanges: Boolean,
    modifier: Modifier = Modifier
) {
    val badgeColor = when {
        !isNetworkAvailable -> Color(0xFFFF3B30) // Vibrant Red if offline
        hasUnsavedChanges -> Color(0xFFFFD600)   // Vibrant Yellow if unsaved changes
        else -> Color(0xFF00FF66)               // Extremely bright neon green if everything is synced
    }

    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier.size(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glowing pulse circle
        Box(
            modifier = Modifier
                .size(7.dp)
                .graphicsLayer(
                    scaleX = pulseScale,
                    scaleY = pulseScale,
                    alpha = pulseAlpha
                )
                .background(badgeColor, shape = CircleShape)
        )
        // Solid center circle with crisp white border
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(badgeColor, shape = CircleShape)
                .border(1.2.dp, Color.White, CircleShape)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: FinanceViewModel,
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    var selectedTab by remember { mutableStateOf(0) } // 0: Email, 1: Phone
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var verificationIdState by remember { mutableStateOf<String?>(null) }
    var codeSent by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotEmailInput by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDark) Color(0xFF121212) else Color(0xFFF8FAFC)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = FintechBlue.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = FintechBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (language == AppLanguage.BN) "Finance Note অ্যাপটি ব্যবহার করতে আপনার একাউন্ট লগিন করুন" else "Please login to your account to use Finance Note app",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Google Sign-In Button (Modernized)
                Button(
                    onClick = {
                        onGoogleSignIn()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDark) Color(0xFF2C2C2E) else Color.White),
                    border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else Color.LightGray),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Custom Google-like icon or official icon if available
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Cloud, // Placeholder for Google Icon
                                contentDescription = null,
                                tint = FintechBlue,
                                modifier = Modifier.size(18.dp).padding(2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (language == AppLanguage.BN) "গুগল একাউন্ট দিয়ে লগিন করুন" else "Sign in with Google Account",
                            color = if (isDark) Color.White else Color(0xFF1E293B),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
                    Text(
                        text = if (language == AppLanguage.BN) " অথবা " else " OR ",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Divider(modifier = Modifier.weight(1f), color = Color.Gray.copy(alpha = 0.3f))
                }

                // Tabs: Email vs Phone
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDark) Color(0xFF1C1C1E) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) FintechBlue else Color.Transparent
                        )
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "ইমেইল ও পাসওয়ার্ড" else "Email & Password",
                            color = if (selectedTab == 0) Color.White else (if (isDark) Color.LightGray else Color.DarkGray),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) FintechBlue else Color.Transparent
                        )
                    ) {
                        Text(
                            text = if (language == AppLanguage.BN) "ফোন নম্বর (OTP)" else "Phone Number (OTP)",
                            color = if (selectedTab == 1) Color.White else (if (isDark) Color.LightGray else Color.DarkGray),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEF4444).copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFEF4444),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (successMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = successMessage ?: "",
                            color = Color(0xFF10B981),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Tab 0: Email / Password
                if (selectedTab == 0) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text(if (language == AppLanguage.BN) "ইমেইল এড্রেস" else "Email Address") },
                            leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = FintechBlue) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FintechBlue,
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.LightGray,
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black
                            )
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text(if (language == AppLanguage.BN) "পাসওয়ার্ড" else "Password") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = FintechBlue) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                        tint = FintechBlue
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FintechBlue,
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.LightGray,
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showForgotPasswordDialog = true }) {
                                Text(
                                    text = if (language == AppLanguage.BN) "পাসওয়ার্ড ভুলে গেছেন?" else "Forgot Password?",
                                    color = FintechBlue,
                                    fontSize = 13.sp
                                )
                            }
                            TextButton(onClick = { isRegisterMode = !isRegisterMode }) {
                                Text(
                                    text = if (isRegisterMode) (if (language == AppLanguage.BN) "লগইন করুন" else "Have account? Login") else (if (language == AppLanguage.BN) "নতুন অ্যাকাউন্ট তৈরি করুন" else "Register new account"),
                                    color = FintechBlue,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                successMessage = null
                                if (isRegisterMode) {
                                    viewModel.registerWithEmail(emailInput, passwordInput,
                                        onSuccess = {
                                            isLoading = false
                                            successMessage = if (language == AppLanguage.BN) "রেজিস্ট্রেশন সফল হয়েছে! এখন লগইন করুন।" else "Registration successful! Now please login."
                                            isRegisterMode = false // Switch to login mode
                                            passwordInput = "" // Clear password for security
                                        },
                                        onError = { err ->
                                            isLoading = false
                                            errorMessage = err
                                        }
                                    )
                                } else {
                                    viewModel.loginWithEmail(emailInput, passwordInput,
                                        onSuccess = {
                                            isLoading = false
                                            successMessage = if (language == AppLanguage.BN) "লগইন সফল হয়েছে!" else "Login successful!"
                                            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                kotlinx.coroutines.delay(1000)
                                                onDismiss()
                                            }
                                        },
                                        onError = { err ->
                                            isLoading = false
                                            errorMessage = err
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text(
                                    text = if (isRegisterMode) (if (language == AppLanguage.BN) "রেজিস্ট্রেশন করুন" else "Sign Up") else (if (language == AppLanguage.BN) "লগইন করুন" else "Login"),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                } else {
                    // Tab 1: Phone OTP Login
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text(if (language == AppLanguage.BN) "ফোন নম্বর (যেমন: +8801...)" else "Phone Number (e.g. +8801...)") },
                            leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null, tint = FintechBlue) },
                            singleLine = true,
                            enabled = !codeSent,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FintechBlue,
                                unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.LightGray,
                                focusedTextColor = if (isDark) Color.White else Color.Black,
                                unfocusedTextColor = if (isDark) Color.White else Color.Black
                            )
                        )

                        if (codeSent) {
                            OutlinedTextField(
                                value = otpInput,
                                onValueChange = { otpInput = it },
                                label = { Text(if (language == AppLanguage.BN) "ওটিপি (OTP) কোড" else "Verification Code (OTP)") },
                                leadingIcon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = FintechBlue) },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FintechBlue,
                                    unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.LightGray,
                                    focusedTextColor = if (isDark) Color.White else Color.Black,
                                    unfocusedTextColor = if (isDark) Color.White else Color.Black
                                )
                            )
                        }

                        Button(
                            onClick = {
                                if (!codeSent) {
                                    if (activity == null) {
                                        errorMessage = "Activity not available for phone auth"
                                        return@Button
                                    }
                                    isLoading = true
                                    errorMessage = null
                                    viewModel.sendPhoneVerificationCode(activity, phoneInput,
                                        onCodeSent = { vid ->
                                            isLoading = false
                                            if (vid == "AUTO_VERIFIED") {
                                                successMessage = if (language == AppLanguage.BN) "স্বয়ংক্রিয়ভাবে ভেরিফাইড!" else "Automatically verified!"
                                                kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                    kotlinx.coroutines.delay(1000)
                                                    onDismiss()
                                                }
                                            } else {
                                                verificationIdState = vid
                                                codeSent = true
                                                successMessage = if (language == AppLanguage.BN) "ওটিপি কোড পাঠানো হয়েছে!" else "OTP code sent successfully!"
                                            }
                                        },
                                        onError = { err ->
                                            isLoading = false
                                            errorMessage = err
                                        }
                                    )
                                } else {
                                    if (verificationIdState == null) return@Button
                                    isLoading = true
                                    errorMessage = null
                                    viewModel.verifyPhoneCode(verificationIdState!!, otpInput, phoneInput,
                                        onSuccess = {
                                            isLoading = false
                                            successMessage = if (language == AppLanguage.BN) "লগইন সফল হয়েছে!" else "Login successful!"
                                            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                                kotlinx.coroutines.delay(1000)
                                                onDismiss()
                                            }
                                        },
                                        onError = { err ->
                                            isLoading = false
                                            errorMessage = err
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text(
                                    text = if (!codeSent) (if (language == AppLanguage.BN) "ওটিপি কোড পাঠান" else "Send OTP") else (if (language == AppLanguage.BN) "ভেরিফাই ও লগইন" else "Verify & Login"),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text(if (language == AppLanguage.BN) "পাসওয়ার্ড রিস্টেট করুন" else "Reset Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = if (language == AppLanguage.BN) "আপনার অ্যাকাউন্ট ইমেইল দিন:" else "Enter your account email:")
                    OutlinedTextField(
                        value = forgotEmailInput,
                        onValueChange = { forgotEmailInput = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendPasswordReset(forgotEmailInput,
                            onSuccess = {
                                showForgotPasswordDialog = false
                                successMessage = if (language == AppLanguage.BN) "পাসওয়ার্ড রিস্টেট লিংক পাঠানো হয়েছে!" else "Password reset email sent!"
                            },
                            onError = { err ->
                                errorMessage = err
                                showForgotPasswordDialog = false
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                ) {
                    Text("Send")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    viewModel: FinanceViewModel,
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit
) {
    val googleName by viewModel.googleName.collectAsStateWithLifecycle()
    val userAddress by viewModel.userAddress.collectAsStateWithLifecycle()
    val userPhone by viewModel.userPhone.collectAsStateWithLifecycle()
    val userDOB by viewModel.userDOB.collectAsStateWithLifecycle()
    val googlePhotoUrl by viewModel.googlePhotoUrl.collectAsStateWithLifecycle()
    
    var name by remember { mutableStateOf(googleName ?: "") }
    var address by remember { mutableStateOf(userAddress ?: "") }
    var phone by remember { mutableStateOf(userPhone ?: "") }
    var dob by remember { mutableStateOf(userDOB ?: "") }
    var photoUri by remember { mutableStateOf(googlePhotoUrl ?: "") }
    
    val context = LocalContext.current
    val cropLauncher = rememberLauncherForActivityResult(UCropContract()) { uri ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val destinationUri = Uri.fromFile(java.io.File(context.cacheDir, "profile_setup_crop_${System.currentTimeMillis()}.jpg"))
            cropLauncher.launch(uri to destinationUri)
        }
    }
    
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDark) Color(0xFF121212) else Color(0xFFF8FAFC)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.BN) "প্রোফাইল সেটআপ" else "Profile Setup",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))
                        .clickable { photoLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri.isNotEmpty()) {
                        AsyncImage(
                            model = photoUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.AddAPhoto,
                            contentDescription = null,
                            tint = if (isDark) Color.LightGray else Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (language == AppLanguage.BN) "আপনার নাম" else "Your Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedLabelColor = FintechBlue,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text(if (language == AppLanguage.BN) "ফোন নম্বর" else "Phone Number") },
                    leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null, tint = FintechBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedLabelColor = FintechBlue,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                OutlinedTextField(
                    value = dob,
                    onValueChange = { dob = it },
                    label = { Text(if (language == AppLanguage.BN) "জন্ম তারিখ" else "Date of Birth") },
                    leadingIcon = { Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = FintechBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("DD/MM/YYYY", color = Color.Gray.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedLabelColor = FintechBlue,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text(if (language == AppLanguage.BN) "ঠিকানা" else "Address") },
                    leadingIcon = { Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = FintechBlue) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = if (isDark) Color.White else Color.Black,
                        unfocusedTextColor = if (isDark) Color.White else Color.Black,
                        focusedLabelColor = FintechBlue,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                if (error != null) {
                    Text(text = error!!, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        isLoading = true
                        viewModel.updateUserProfile(name, address, phone, dob, photoUri,
                            onSuccess = {
                                isLoading = false
                                onDismiss()
                            },
                            onError = { err ->
                                isLoading = false
                                error = err
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (language == AppLanguage.BN) "সংরক্ষণ করুন" else "Save Profile", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                
                TextButton(onClick = onDismiss) {
                    Text(if (language == AppLanguage.BN) "পরে করব" else "Setup Later")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedProfileMenu(
    viewModel: FinanceViewModel,
    language: AppLanguage,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSwitchWorkspace: () -> Unit,
    onProfileSettings: () -> Unit,
    onBackupRestore: () -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit
) {
    val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsStateWithLifecycle()
    val googleName by viewModel.googleName.collectAsStateWithLifecycle()
    val profileName by viewModel.profileName.collectAsStateWithLifecycle()
    val googleEmail by viewModel.googleEmail.collectAsStateWithLifecycle()
    val googlePhotoUrl by viewModel.googlePhotoUrl.collectAsStateWithLifecycle()

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header User Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGoogleSignedIn && !googlePhotoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = googlePhotoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(32.dp), tint = if (isDark) Color.LightGray else Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isGoogleSignedIn) (googleName ?: (if (language == AppLanguage.BN) "ব্যবহারকারী" else "User")) else (if (language == AppLanguage.BN) "অতিথি ইউজার" else "Guest User"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (isDark) Color.White else Color.Black
                        )
                        if (isGoogleSignedIn) {
                            Text(
                                text = googleEmail ?: "",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                HorizontalDivider(color = if (isDark) Color.Gray.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.5f))

                // Menu Items
                ProfileMenuItem(
                    icon = Icons.Rounded.Workspaces,
                    label = if (language == AppLanguage.BN) "ওয়ার্কস্পেস সুইচ করুন" else "Switch Workspace",
                    isDark = isDark,
                    onClick = onSwitchWorkspace
                )
                
                ProfileMenuItem(
                    icon = Icons.Rounded.ManageAccounts,
                    label = if (language == AppLanguage.BN) "প্রোফাইল সেটিংস" else "Profile Settings",
                    isDark = isDark,
                    onClick = onProfileSettings
                )

                ProfileMenuItem(
                    icon = Icons.Rounded.CloudSync,
                    label = if (language == AppLanguage.BN) "ব্যাকআপ ও রিস্টোর" else "Backup & Restore",
                    isDark = isDark,
                    onClick = onBackupRestore
                )

                HorizontalDivider(color = if (isDark) Color.Gray.copy(alpha = 0.2f) else Color.LightGray.copy(alpha = 0.5f))

                if (isGoogleSignedIn) {
                    ProfileMenuItem(
                        icon = Icons.Rounded.Logout,
                        label = if (language == AppLanguage.BN) "লগআউট" else "Logout",
                        isDark = isDark,
                        textColor = Color(0xFFEF4444),
                        onClick = onLogout
                    )
                } else {
                    Button(
                        onClick = onLogin,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FintechBlue)
                    ) {
                        Text(if (language == AppLanguage.BN) "লগইন করুন" else "Login Now", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    isDark: Boolean,
    textColor: Color? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = textColor ?: (if (isDark) Color.LightGray else Color(0xFF475569)),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = textColor ?: (if (isDark) Color.White else Color(0xFF1E293B))
        )
    }
}
