package com.example.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    enum class BiometricStatus {
        AVAILABLE,
        NO_HARDWARE,
        NOT_ENROLLED,
        UNAVAILABLE
    }

    fun getBiometricStatus(context: Context): BiometricStatus {
        val sdkVersion = android.os.Build.VERSION.SDK_INT
        if (sdkVersion < 29) {
            return try {
                val fm = androidx.core.hardware.fingerprint.FingerprintManagerCompat.from(context)
                if (!fm.isHardwareDetected) {
                    BiometricStatus.NO_HARDWARE
                } else if (!fm.hasEnrolledFingerprints()) {
                    BiometricStatus.NOT_ENROLLED
                } else {
                    BiometricStatus.AVAILABLE
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                BiometricStatus.UNAVAILABLE
            }
        }

        return try {
            val biometricManager = BiometricManager.from(context)
            val result = if (sdkVersion >= 30) {
                biometricManager.canAuthenticate(255) // 255 is BIOMETRIC_STRONG
            } else {
                biometricManager.canAuthenticate()
            }
            
            when (result) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
                else -> {
                    try {
                        val fm = androidx.core.hardware.fingerprint.FingerprintManagerCompat.from(context)
                        if (fm.isHardwareDetected) {
                            if (fm.hasEnrolledFingerprints()) BiometricStatus.AVAILABLE else BiometricStatus.NOT_ENROLLED
                        } else {
                            BiometricStatus.UNAVAILABLE
                        }
                    } catch (e: Throwable) {
                        BiometricStatus.UNAVAILABLE
                    }
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            try {
                val fm = androidx.core.hardware.fingerprint.FingerprintManagerCompat.from(context)
                if (fm.isHardwareDetected) {
                    if (fm.hasEnrolledFingerprints()) BiometricStatus.AVAILABLE else BiometricStatus.NOT_ENROLLED
                } else {
                    BiometricStatus.UNAVAILABLE
                }
            } catch (ex: Throwable) {
                BiometricStatus.UNAVAILABLE
            }
        }
    }

    fun isBiometricAvailable(context: Context): Boolean {
        return try {
            val status = getBiometricStatus(context)
            status == BiometricStatus.AVAILABLE
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Show custom fingerprint dialog or native prompt.
     * We default to our custom designed fingerprint dialog on ALL devices to ensure absolute compatibility,
     * visual perfection, and complete bypass of standard OEM BiometricPrompt bugs on devices like Oppo, Vivo, Xiaomi.
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Unlock Finance Note",
        subtitle: String = "Use your fingerprint to access your account",
        negativeButtonText: String = "Use PIN",
        onSuccess: () -> Unit,
        onUsePinFallback: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        // Use our beautiful custom fingerprint dialog fallback on all versions.
        // This is 100% safe, reliable, highly customizable, and prevents any native dialog crashes on custom OEM ROMs like Oppo ColorOS.
        showFingerprintPromptFallback(
            activity = activity,
            title = title,
            subtitle = subtitle,
            negativeButtonText = negativeButtonText,
            onSuccess = onSuccess,
            onUsePinFallback = onUsePinFallback,
            onError = onError
        )
    }

    /**
     * Custom programmatic Fingerprint icon view drawing concentric arcs and a dynamic pulsing background circle.
     * Supports beautiful enrollment-style filling animation (progress 0.0 to 1.0) and transitions.
     */
    class FingerprintView(context: Context) : android.view.View(context) {
        private val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.STROKE
            strokeCap = android.graphics.Paint.Cap.ROUND
            strokeWidth = 3.5f * resources.displayMetrics.density
        }
        
        private val bgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.FILL
        }

        var iconColor: Int = android.graphics.Color.parseColor("#38BDF8") // Light blue default
            set(value) {
                field = value
                paint.color = value
                invalidate()
            }

        private var progress: Float = 0.15f
        private var animator: android.animation.ValueAnimator? = null
        private val inactiveColor = android.graphics.Color.parseColor("#E2E8F0") // Slate 200
            
        init {
            paint.color = iconColor
        }

        fun startScanning() {
            animator?.cancel()
            val defaultColor = android.graphics.Color.parseColor("#38BDF8")
            iconColor = defaultColor
            
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
        }

        fun animateSuccess() {
            animator?.cancel()
            val startProgress = progress
            val startColor = iconColor
            val endColor = android.graphics.Color.parseColor("#22C55E")
            
            animator = android.animation.ValueAnimator.ofFloat(startProgress, 1.0f).apply {
                duration = 350
                interpolator = android.view.animation.DecelerateInterpolator()
                addUpdateListener { animation ->
                    val fraction = animation.animatedFraction
                    progress = animation.animatedValue as Float
                    
                    val evaluator = android.animation.ArgbEvaluator()
                    iconColor = evaluator.evaluate(fraction, startColor, endColor) as Int
                    invalidate()
                }
            }
            animator?.start()
        }

        fun animateFailure() {
            animator?.cancel()
            val startProgress = progress
            val startColor = iconColor
            val errorColor = android.graphics.Color.parseColor("#EF4444")
            
            animator = android.animation.ValueAnimator.ofFloat(startProgress, 0.1f).apply {
                duration = 200
                interpolator = android.view.animation.AccelerateInterpolator()
                addUpdateListener { animation ->
                    val fraction = animation.animatedFraction
                    progress = animation.animatedValue as Float
                    
                    val evaluator = android.animation.ArgbEvaluator()
                    iconColor = evaluator.evaluate(fraction, startColor, errorColor) as Int
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
            
            // Draw a beautiful soft background circle matching current color (12% opacity)
            val radius = 44f * density
            bgPaint.color = adjustAlpha(iconColor, 0.12f)
            canvas.drawCircle(cx, cy, radius, bgPaint)

            // Step 1: Draw the inactive, faint blueprint fingerprint background lines (Slate-200)
            paint.color = inactiveColor
            drawFingerprint(canvas, cx, cy, density)

            // Step 2: Draw the active colored lines on top, clipped by progress from bottom to top
            paint.color = iconColor
            canvas.save()
            
            // Total fingerprint height bounds are roughly cy - 35 * density to cy + 15 * density.
            // When progress is 0f, clipTop is cy + 15 * density (completely hidden).
            // When progress is 1f, clipTop is cy - 35 * density (fully shown).
            val totalHeight = 50f * density
            val clipTop = (cy + 15f * density) - (progress * totalHeight)
            canvas.clipRect(0f, clipTop, width.toFloat(), height.toFloat() + 50f)
            
            drawFingerprint(canvas, cx, cy, density)
            canvas.restore()
        }

        private fun drawFingerprint(canvas: android.graphics.Canvas, cx: Float, cy: Float, density: Float) {
            val rectF = android.graphics.RectF()
            
            // Arc 1 (inner loop)
            rectF.set(cx - 7 * density, cy - 11 * density, cx + 7 * density, cy + 11 * density)
            canvas.drawArc(rectF, 180f, 180f, false, paint)
            canvas.drawLine(cx - 7 * density, cy, cx - 7 * density, cy + 8 * density, paint)
            canvas.drawLine(cx + 7 * density, cy, cx + 7 * density, cy + 8 * density, paint)
            
            // Arc 2
            rectF.set(cx - 14 * density, cy - 18 * density, cx + 14 * density, cy + 18 * density)
            canvas.drawArc(rectF, 195f, 150f, false, paint)
            canvas.drawLine(cx - 14 * density, cy + 4 * density, cx - 14 * density, cy + 12 * density, paint)
            canvas.drawLine(cx + 14 * density, cy + 4 * density, cx + 14 * density, cy + 12 * density, paint)
            
            // Arc 3
            rectF.set(cx - 21 * density, cy - 25 * density, cx + 21 * density, cy + 25 * density)
            canvas.drawArc(rectF, 185f, 170f, false, paint)
            
            // Arc 4 (outer)
            rectF.set(cx - 28 * density, cy - 32 * density, cx + 28 * density, cy + 32 * density)
            canvas.drawArc(rectF, 205f, 130f, false, paint)
        }
    }

    /**
     * Beautiful custom rounded card style Fingerprint authentication dialog.
     */
    private fun showFingerprintPromptFallback(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onUsePinFallback: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val fm = androidx.core.hardware.fingerprint.FingerprintManagerCompat.from(activity)
            if (!fm.isHardwareDetected || !fm.hasEnrolledFingerprints()) {
                onUsePinFallback()
                return
            }

            val cancellationSignal = androidx.core.os.CancellationSignal()
            val density = activity.resources.displayMetrics.density
            
            // Create root linear layout
            val rootLayout = android.widget.LinearLayout(activity).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding((24 * density).toInt(), (28 * density).toInt(), (24 * density).toInt(), (24 * density).toInt())
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                
                // Solid white card with beautifully rounded corners
                val backgroundDrawable = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.WHITE)
                    cornerRadius = 24 * density
                }
                background = backgroundDrawable
            }

            // Title TextView
            val titleTextView = android.widget.TextView(activity).apply {
                text = title
                textSize = 19f
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.parseColor("#0F172A")) // Slate 900
                paint.isFakeBoldText = true
                setPadding(0, 0, 0, (8 * density).toInt())
            }
            rootLayout.addView(titleTextView)

            // Subtitle TextView
            val subtitleTextView = android.widget.TextView(activity).apply {
                text = subtitle
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.parseColor("#475569")) // Slate 600
                setPadding(0, 0, 0, (28 * density).toInt())
            }
            rootLayout.addView(subtitleTextView)

            // Custom Fingerprint View
            val fingerprintView = FingerprintView(activity).apply {
                val viewSize = (110 * density).toInt()
                layoutParams = android.widget.LinearLayout.LayoutParams(viewSize, viewSize).apply {
                    gravity = android.view.Gravity.CENTER_HORIZONTAL
                    bottomMargin = (24 * density).toInt()
                }
            }
            rootLayout.addView(fingerprintView)

            // Dynamic Status/Instruction TextView
            val statusTextView = android.widget.TextView(activity).apply {
                text = if (subtitle.contains("আপনার")) "ফিঙ্গারপ্রিন্ট সেন্সর স্পর্শ করুন" else "Touch Fingerprint Sensor"
                textSize = 15f
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.parseColor("#38BDF8")) // Light Blue
                paint.isFakeBoldText = true
                setPadding(0, 0, 0, (28 * density).toInt())
            }
            rootLayout.addView(statusTextView)

            // Custom Styled Negative Button (Centered Text Button)
            val negativeButton = android.widget.Button(activity).apply {
                text = negativeButtonText
                textSize = 15f
                setTextColor(android.graphics.Color.parseColor("#4F46E5")) // Indigo 600
                paint.isFakeBoldText = true
                setBackground(android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.TRANSPARENT)
                })
                setAllCaps(false)
                
                val paddingHoriz = (24 * density).toInt()
                val paddingVert = (12 * density).toInt()
                setPadding(paddingHoriz, paddingVert, paddingHoriz, paddingVert)
                
                // Native ripple effect
                val outValue = android.util.TypedValue()
                activity.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                setBackgroundResource(outValue.resourceId)
            }
            rootLayout.addView(negativeButton)

            // Dialog configuration
            val dialogBuilder = android.app.AlertDialog.Builder(activity)
                .setView(rootLayout)
                .setCancelable(false)

            val alertDialog = dialogBuilder.create()

            // Remove native dialog background/borders to show the beautiful card round corners
            alertDialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

            negativeButton.setOnClickListener {
                try {
                    cancellationSignal.cancel()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
                onUsePinFallback()
                alertDialog.dismiss()
            }

            alertDialog.setOnDismissListener {
                try {
                    cancellationSignal.cancel()
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }

            val callback = object : androidx.core.hardware.fingerprint.FingerprintManagerCompat.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.core.hardware.fingerprint.FingerprintManagerCompat.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    try {
                        statusTextView.text = if (subtitle.contains("আপনার")) "সফল হয়েছে!" else "Success!"
                        statusTextView.setTextColor(android.graphics.Color.parseColor("#22C55E")) // Green
                        fingerprintView.animateSuccess()

                        rootLayout.postDelayed({
                            try {
                                alertDialog.dismiss()
                                onSuccess()
                            } catch (t: Throwable) {
                                t.printStackTrace()
                                onSuccess()
                            }
                        }, 450)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        onSuccess()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    try {
                        statusTextView.text = if (subtitle.contains("আপনার")) "ফিঙ্গারপ্রিন্ট মেলেনি, আবার চেষ্টা করুন।" else "Fingerprint not recognized. Try again."
                        statusTextView.setTextColor(android.graphics.Color.parseColor("#EF4444")) // Red
                        fingerprintView.animateFailure()

                        // Standard modern tactile vibration on failure
                        try {
                            val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                            if (android.os.Build.VERSION.SDK_INT >= 26) {
                                vibrator.vibrate(android.os.VibrationEffect.createOneShot(200, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                            } else {
                                vibrator.vibrate(200)
                            }
                        } catch (t: Throwable) {}

                        statusTextView.postDelayed({
                            try {
                                statusTextView.text = if (subtitle.contains("আপনার")) "ফিンダーপ্রিন্ট সেন্সর স্পর্শ করুন" else "Touch Fingerprint Sensor"
                                statusTextView.setTextColor(android.graphics.Color.parseColor("#38BDF8"))
                            } catch (t: Throwable) {
                                t.printStackTrace()
                            }
                        }, 1500)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }

                override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
                    super.onAuthenticationError(errMsgId, errString)
                    try {
                        val errorMsg = errString.toString()
                        if (errMsgId == 5 || errMsgId == 10) {
                            return
                        }

                        statusTextView.text = errorMsg
                        statusTextView.setTextColor(android.graphics.Color.parseColor("#EF4444"))
                        fingerprintView.animateFailure()

                        statusTextView.postDelayed({
                            try {
                                alertDialog.dismiss()
                                onError(errorMsg)
                                onUsePinFallback()
                            } catch (t: Throwable) {
                                t.printStackTrace()
                            }
                        }, 1000)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }

                override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
                    super.onAuthenticationHelp(helpMsgId, helpString)
                    try {
                        statusTextView.text = helpString.toString()
                        statusTextView.setTextColor(android.graphics.Color.parseColor("#F59E0B")) // Orange/Yellow
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }
            }

            alertDialog.show()

            try {
                fm.authenticate(null, 0, cancellationSignal, callback, null)
            } catch (e: Throwable) {
                e.printStackTrace()
                alertDialog.dismiss()
                onError("Failed to start fingerprint scan: ${e.localizedMessage}")
                onUsePinFallback()
            }

        } catch (e: Throwable) {
            e.printStackTrace()
            onError("Fingerprint system error: ${e.localizedMessage}")
            onUsePinFallback()
        }
    }

    fun requireReauthentication(
        activity: FragmentActivity,
        title: String = "Confirm Security Verification",
        subtitle: String = "Verify your identity before performing this sensitive action",
        onGranted: () -> Unit,
        onUsePinFallback: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        showBiometricPrompt(
            activity = activity,
            title = title,
            subtitle = subtitle,
            negativeButtonText = "Use PIN",
            onSuccess = onGranted,
            onUsePinFallback = onUsePinFallback,
            onError = onError
        )
    }
}
