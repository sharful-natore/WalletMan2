import re

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "r") as f:
    content = f.read()

# We will match the entire ChartSection from its @Composable annotation down to just before SplashScreen
p = r"@Composable\nfun ChartSection\([\s\S]*?\}\n\n@Composable\nfun SplashScreen"

def replace_chart_section(match):
    return """@Composable
fun ChartSection(
    title: String,
    data: Map<String, Double>,
    total: Double,
    palette: List<Color>,
    language: AppLanguage,
    isDark: Boolean = true
) {
    val bgColor = if (isDark) listOf(Color(0xFF1E222F), Color(0xFF2A2E3D)) else listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
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
                                        drawContext.canvas.save()
                                        val innerClipRadius = radius - strokeWidthPx / 2f
                                        val innerClipPath = androidx.compose.ui.graphics.Path().apply {
                                            addOval(androidx.compose.ui.geometry.Rect(
                                                center.x - innerClipRadius, center.y - innerClipRadius,
                                                center.x + innerClipRadius, center.y + innerClipRadius
                                            ))
                                        }
                                        drawContext.canvas.clipPath(innerClipPath, androidx.compose.ui.graphics.ClipOp.Difference)
                                        
                                        val shadowPaint = androidx.compose.ui.graphics.Paint().apply {
                                            this.color = color.copy(alpha = 0.65f)
                                            this.style = androidx.compose.ui.graphics.PaintingStyle.Stroke
                                            this.strokeWidth = strokeWidthPx
                                            this.strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt
                                            asFrameworkPaint().maskFilter = android.graphics.BlurMaskFilter(12.dp.toPx(), android.graphics.BlurMaskFilter.Blur.NORMAL)
                                        }
                                        drawContext.canvas.drawArc(
                                            rect = androidx.compose.ui.geometry.Rect(
                                                center.x - radius, center.y - radius,
                                                center.x + radius, center.y + radius
                                            ),
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle + 0.8f,
                                            useCenter = false,
                                            paint = shadowPaint
                                        )
                                        drawContext.canvas.restore()

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
fun SplashScreen"""

new_content = re.sub(p, replace_chart_section, content)

with open("app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt", "w") as f:
    f.write(new_content)
