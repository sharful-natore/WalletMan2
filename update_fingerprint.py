import re

filepath = 'app/src/main/java/com/example/util/BiometricHelper.kt'
with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Replace FingerprintView class entirely
pattern = r'class FingerprintView\(context: Context\) : android\.view\.View\(context\) \{.*?(?=\n\s*/\*\*\n\s*\* Beautiful custom rounded card)'
replacement = """class FingerprintView(context: Context) : android.view.View(context) {
        private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeWidth = 3.5f * resources.displayMetrics.density
        }
        
        private val bgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.FILL
        }

        var iconColor: Int = android.graphics.Color.parseColor("#38BDF8")
            set(value) {
                field = value
                invalidate()
            }

        private var progress: Float = 0.15f
        private var gradientPhase = 0f
        private var animator: android.animation.ValueAnimator? = null
        private var gradientAnimator: android.animation.ValueAnimator? = null
        
        private val inactiveColor = android.graphics.Color.parseColor("#E2E8F0")
        
        private var currentState: Int = 0 // 0 = scanning, 1 = success, 2 = failure
            
        init {
            paint.color = iconColor
        }

        fun startScanning() {
            animator?.cancel()
            gradientAnimator?.cancel()
            currentState = 0
            iconColor = android.graphics.Color.parseColor("#38BDF8")
            
            animator = android.animation.ValueAnimator.ofFloat(0.15f, 0.65f).apply {
                duration = 1600
                repeatMode = android.animation.ValueAnimator.REVERSE
                repeatCount = android.animation.ValueAnimator.INFINITE
                interpolator = android.view.animation.AccelerateDecelerateInterpolator()
                addUpdateListener { animation ->
                    progress = animation.animatedValue as Float
                    invalidate()
                }
            }
            animator?.start()
            
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
            animator?.cancel()
            gradientAnimator?.cancel()
            currentState = 1
            iconColor = android.graphics.Color.parseColor("#10B981") // Green
            
            animator = android.animation.ValueAnimator.ofFloat(progress, 1.0f).apply {
                duration = 350
                interpolator = android.view.animation.DecelerateInterpolator()
                addUpdateListener { animation ->
                    progress = animation.animatedValue as Float
                    invalidate()
                }
            }
            animator?.start()
        }

        fun animateFailure() {
            animator?.cancel()
            gradientAnimator?.cancel()
            currentState = 2
            iconColor = android.graphics.Color.parseColor("#EF4444") // Red
            
            animator = android.animation.ValueAnimator.ofFloat(progress, 0.1f).apply {
                duration = 200
                interpolator = android.view.animation.AccelerateInterpolator()
                addUpdateListener { animation ->
                    progress = animation.animatedValue as Float
                    invalidate()
                }
                addListener(object : android.animation.AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: android.animation.Animator) {
                        postDelayed({
                            startScanning()
                        }, 1200)
                    }
                })
            }
            animator?.start()
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            startScanning()
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            animator?.cancel()
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

            paint.shader = null
            paint.color = inactiveColor
            drawFingerprint(canvas, cx, cy, density)

            if (currentState == 0) {
                // Glowing directional gradient for scanning
                val gradient = android.graphics.LinearGradient(
                    cx - radius + (gradientPhase * radius * 2f),
                    cy - radius,
                    cx + radius,
                    cy + radius,
                    intArrayOf(
                        android.graphics.Color.parseColor("#3B82F6"),
                        android.graphics.Color.parseColor("#10B981"),
                        android.graphics.Color.parseColor("#8B5CF6"),
                        android.graphics.Color.parseColor("#3B82F6")
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
            val totalHeight = 50f * density
            val clipTop = (cy + 15f * density) - (progress * totalHeight)
            canvas.clipRect(0f, clipTop, width.toFloat(), height.toFloat() + 50f)
            drawFingerprint(canvas, cx, cy, density)
            canvas.restore()
        }

        private fun drawFingerprint(canvas: android.graphics.Canvas, cx: Float, cy: Float, density: Float) {
            val rectF = android.graphics.RectF()
            
            rectF.set(cx - 7 * density, cy - 11 * density, cx + 7 * density, cy + 11 * density)
            canvas.drawArc(rectF, 180f, 180f, false, paint)
            canvas.drawLine(cx - 7 * density, cy, cx - 7 * density, cy + 8 * density, paint)
            canvas.drawLine(cx + 7 * density, cy, cx + 7 * density, cy + 8 * density, paint)
            
            rectF.set(cx - 14 * density, cy - 18 * density, cx + 14 * density, cy + 18 * density)
            canvas.drawArc(rectF, 195f, 150f, false, paint)
            canvas.drawLine(cx - 14 * density, cy + 4 * density, cx - 14 * density, cy + 12 * density, paint)
            canvas.drawLine(cx + 14 * density, cy + 4 * density, cx + 14 * density, cy + 12 * density, paint)
            
            rectF.set(cx - 21 * density, cy - 25 * density, cx + 21 * density, cy + 25 * density)
            canvas.drawArc(rectF, 185f, 170f, false, paint)
            
            rectF.set(cx - 28 * density, cy - 32 * density, cx + 28 * density, cy + 32 * density)
            canvas.drawArc(rectF, 205f, 130f, false, paint)
        }
    }"""

modified, count = re.subn(pattern, replacement, content, flags=re.DOTALL)
print(f"Replacements: {count}")
with open(filepath, 'w', encoding='utf-8') as f:
    f.write(modified)
