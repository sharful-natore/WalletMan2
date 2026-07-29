import re

filepath = 'app/src/main/java/com/example/util/BiometricHelper.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

pattern = r'class FingerprintView\(context: Context\) : android\.view\.View\(context\) \{.*?(?=\n\s*/\*\*\n\s*\* Beautiful custom rounded card)'
replacement = """class FingerprintView(context: Context) : android.view.View(context) {
        private val path = android.graphics.Path()
        
        private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.FILL
        }
        
        private val bgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.FILL
        }

        var iconColor: Int = android.graphics.Color.parseColor("#38BDF8")
            set(value) {
                field = value
                invalidate()
            }

        private var gradientPhase = 0f
        private var gradientAnimator: android.animation.ValueAnimator? = null
        
        private var currentState: Int = 0 // 0 = scanning, 1 = success, 2 = failure
            
        init {
            paint.color = iconColor
            // Path data for Icons.Rounded.Fingerprint (Material Symbols Rounded)
            val pathData = "M481-781q106 0 200 45.5T838-604q7 9 4.5 16t-8.5 12q-6 5-14 4.5t-14-8.5q-55-78-141.5-119.5T481-741q-97 0-182 41.5T158-580q-6 9-14 10t-14-4q-7-5-8.5-12.5T126-602q62-85 155.5-132T481-781Zm0 94q135 0 232 90t97 223q0 50-35.5 83.5T688-257q-51 0-87.5-33.5T564-374q0-33-24.5-55.5T481-452q-34 0-58.5 22.5T398-374q0 97 57.5 162T604-121q9 3 12 10t1 15q-2 7-8 12t-15 3q-104-26-170-103.5T358-374q0-50 36-84t87-34q51 0 87 34t36 84q0 33 25 55.5t59 22.5q34 0 58-22.5t24-55.5q0-116-85-195t-203-79q-118 0-203 79t-85 194q0 24 4.5 60t21.5 84q3 9-.5 16T208-205q-8 3-15.5-.5T182-217q-15-39-21.5-77.5T154-374q0-133 96.5-223T481-687Zm0-192q64 0 125 15.5T724-819q9 5 10.5 12t-1.5 14q-3 7-10 11t-17-1q-53-27-109.5-41.5T481-839q-58 0-114 13.5T260-783q-8 5-16 2.5T232-791q-4-8-2-14.5t10-11.5q56-30 117-46t124-16Zm0 289q93 0 160 62.5T708-374q0 9-5.5 14.5T688-354q-8 0-14-5.5t-6-14.5q0-75-55.5-125.5T481-550q-76 0-130.5 50.5T296-374q0 81 28 137.5T406-123q6 6 6 14t-6 14q-6 6-14 6t-14-6q-59-62-90.5-126.5T256-374q0-91 66-153.5T481-590Zm-1 196q9 0 14.5 6t5.5 14q0 75 54 123t126 48q6 0 17-1t23-3q9-2 15.5 2.5T744-191q2 8-3 14t-13 8q-18 5-31.5 5.5t-16.5.5q-89 0-154.5-60T460-374q0-8 5.5-14t14.5-6Z"
            try {
                val parsedPath = androidx.core.graphics.PathParser.createPathFromPathData(pathData)
                path.addPath(parsedPath)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun startScanning() {
            gradientAnimator?.cancel()
            currentState = 0
            iconColor = android.graphics.Color.parseColor("#38BDF8")
            
            gradientAnimator = android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 2000
                repeatMode = android.animation.ValueAnimator.RESTART
                repeatCount = android.animation.ValueAnimator.INFINITE
                interpolator = android.view.animation.LinearInterpolator()
                addUpdateListener { animation ->
                    gradientPhase = animation.animatedValue as Float
                    invalidate()
                }
            }
            gradientAnimator?.start()
        }

        fun animateSuccess() {
            gradientAnimator?.cancel()
            currentState = 1
            iconColor = android.graphics.Color.parseColor("#10B981") // Green
            invalidate()
        }

        fun animateFailure() {
            gradientAnimator?.cancel()
            currentState = 2
            iconColor = android.graphics.Color.parseColor("#EF4444") // Red
            invalidate()
            postDelayed({
                if (currentState == 2) startScanning()
            }, 1200)
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            startScanning()
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            gradientAnimator?.cancel()
        }

        private fun adjustAlpha(color: Int, factor: Float): Int {
            val alpha = Math.round(android.graphics.Color.alpha(color) * factor)
            val red = android.graphics.Color.red(color)
            val green = android.graphics.Color.green(color)
            val blue = android.graphics.Color.blue(color)
            return android.graphics.Color.argb(alpha, red, green, blue)
        }

        override fun onDraw(canvas: android.graphics.Canvas) {
            super.onDraw(canvas)
            val cx = width / 2f
            val cy = height / 2f
            val density = resources.displayMetrics.density
            
            val radius = 44f * density
            bgPaint.color = adjustAlpha(iconColor, 0.12f)
            canvas.drawCircle(cx, cy, radius, bgPaint)

            if (currentState == 0) {
                // Glowing directional gradient for scanning
                val gradient = android.graphics.LinearGradient(
                    cx - radius + (gradientPhase * radius * 2f),
                    cy - radius,
                    cx + radius,
                    cy + radius,
                    intArrayOf(
                        android.graphics.Color.parseColor("#38BDF8"),
                        android.graphics.Color.parseColor("#10B981"),
                        android.graphics.Color.parseColor("#8B5CF6"),
                        android.graphics.Color.parseColor("#38BDF8")
                    ),
                    null,
                    android.graphics.Shader.TileMode.MIRROR
                )
                paint.shader = gradient
            } else {
                paint.shader = null
                paint.color = iconColor
            }
            
            canvas.save()
            // The path coordinates are from 0 to 960 (viewport size), and centered around 480
            // but notice it's viewBox="0 -960 960 960", which means y goes from -960 to 0.
            // Let's compute a scale factor to fit the icon into 50dp
            val targetSize = 56f * density
            val scale = targetSize / 960f
            
            canvas.translate(cx - targetSize / 2f, cy - targetSize / 2f)
            canvas.scale(scale, scale)
            // Path data uses Y from -960 to 0. So we need to translate Y by +960 to fit 0 to 960 bounds.
            canvas.translate(0f, 960f)
            
            canvas.drawPath(path, paint)
            canvas.restore()
        }
    }"""

modified, count = re.subn(pattern, replacement, content, flags=re.DOTALL)
print(f"Replacements: {count}")
with open(filepath, 'w', encoding='utf-8') as f:
    f.write(modified)
