package com.example.util

import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.unit.sp

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

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
        private val ridgePaths = ArrayList<android.graphics.Path>()

        private val basePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.parseColor("#40000000") // 25% Black
        }

        private val ridgePaints = arrayOf(
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.FILL
                color = android.graphics.Color.parseColor("#38BDF8") // Light Blue
            },
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.FILL
                color = android.graphics.Color.parseColor("#84CC16") // Lime
            },
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.FILL
                color = android.graphics.Color.parseColor("#A855F7") // Purple
            },
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.FILL
                color = android.graphics.Color.parseColor("#06B6D4") // Cyan
            },
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                style = android.graphics.Paint.Style.FILL
                color = android.graphics.Color.parseColor("#EA580C") // Dark Orange
            }
        )

        private val successPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.parseColor("#059669") // Emerald green
        }

        private val failurePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.parseColor("#DC2626") // Crimson red
        }

        private val bgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.FILL
            color = android.graphics.Color.WHITE // Solid WHITE background for fingerprint icon
        }

        private var animProgress = 0f
        private var waveAnimator: android.animation.ValueAnimator? = null
        private var successScale = 0f
        private var successAnimator: android.animation.ValueAnimator? = null
        private var currentState: Int = 0 // 0 = scanning, 1 = success, 2 = failure

        init {
            // Path data for the 5 fingerprint ridges sorted strictly from bottom to top
            val ridgeDataArray = arrayOf(
                // Ridge 0: Bottom-most short arc/line
                "M480 -394q9 0 14.5 6t5.5 14q0 75 54 123t126 48q6 0 17-1t23-3q9-2 15.5 2.5T744-191q2 8-3 14t-13 8q-18 5-31.5 5.5t-16.5.5q-89 0-154.5-60T460-374q0-8 5.5-14t14.5-6Z",
                // Ridge 1: 2nd line up
                "M481 -590q93 0 160 62.5T708-374q0 9-5.5 14.5T688-354q-8 0-14-5.5t-6-14.5q0-75-55.5-125.5T481-550q-76 0-130.5 50.5T296-374q0 81 28 137.5T406-123q6 6 6 14t-6 14q-6 6-14 6t-14-6q-59-62-90.5-126.5T256-374q0-91 66-153.5T481-590Z",
                // Ridge 2: 3rd line up
                "M481 -687q135 0 232 90t97 223q0 50-35.5 83.5T688-257q-51 0-87.5-33.5T564-374q0-33-24.5-55.5T481-452q-34 0-58.5 22.5T398-374q0 97 57.5 162T604-121q9 3 12 10t1 15q-2 7-8 12t-15 3q-104-26-170-103.5T358-374q0-50 36-84t87-34q51 0 87 34t36 84q0 33 25 55.5t59 22.5q34 0 58-22.5t24-55.5q0-116-85-195t-203-79q-118 0-203 79t-85 194q0 24 4.5 60t21.5 84q3 9-.5 16T208-205q-8 3-15.5-.5T182-217q-15-39-21.5-77.5T154-374q0-133 96.5-223T481-687Z",
                // Ridge 3: 4th line up
                "M481-781q106 0 200 45.5T838-604q7 9 4.5 16t-8.5 12q-6 5-14 4.5t-14-8.5q-55-78-141.5-119.5T481-741q-97 0-182 41.5T158-580q-6 9-14 10t-14-4q-7-5-8.5-12.5T126-602q62-85 155.5-132T481-781Z",
                // Ridge 4: Top-most arc
                "M481 -879q64 0 125 15.5T724-819q9 5 10.5 12t-1.5 14q-3 7-10 11t-17-1q-53-27-109.5-41.5T481-839q-58 0-114 13.5T260-783q-8 5-16 2.5T232-791q-4-8-2-14.5t10-11.5q56-30 117-46t124-16Z"
            )

            for (d in ridgeDataArray) {
                try {
                    val p = androidx.core.graphics.PathParser.createPathFromPathData(d)
                    ridgePaths.add(p)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            loadCustomColors()
        }

        fun loadCustomColors() {
            val prefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
            val defaultColors = arrayOf("#38BDF8", "#84CC16", "#A855F7", "#06B6D4", "#EA580C")
            for (i in 0 until 5) {
                val hex = prefs.getString("fingerprint_color_$i", defaultColors[i]) ?: defaultColors[i]
                try {
                    ridgePaints[i].color = android.graphics.Color.parseColor(hex)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            invalidate()
        }

        fun startScanning() {
            waveAnimator?.cancel()
            currentState = 0
            
            waveAnimator = android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 2400
                repeatMode = android.animation.ValueAnimator.RESTART
                repeatCount = android.animation.ValueAnimator.INFINITE
                interpolator = android.view.animation.LinearInterpolator()
                addUpdateListener { animation ->
                    animProgress = animation.animatedValue as Float
                    invalidate()
                }
            }
            waveAnimator?.start()
        }

        fun animateSuccess() {
            waveAnimator?.cancel()
            successAnimator?.cancel()
            currentState = 1
            successScale = 0f
            
            successAnimator = android.animation.ValueAnimator.ofFloat(0f, 1.2f, 1.0f).apply {
                duration = 500
                interpolator = android.view.animation.OvershootInterpolator(1.5f)
                addUpdateListener { animation ->
                    successScale = animation.animatedValue as Float
                    invalidate()
                }
            }
            successAnimator?.start()
        }

        fun animateFailure() {
            waveAnimator?.cancel()
            currentState = 2
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
            waveAnimator?.cancel()
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
            
            val radius = 38f * density
            bgPaint.color = android.graphics.Color.WHITE
            canvas.drawCircle(cx, cy, radius, bgPaint)

            if (currentState == 1) {
                // Success state: draw pulsing tickmark check
                canvas.save()
                canvas.scale(successScale, successScale, cx, cy)
                
                val checkPath = android.graphics.Path().apply {
                    moveTo(cx - 12f * density, cy + 2f * density)
                    lineTo(cx - 3f * density, cy + 11f * density)
                    lineTo(cx + 13f * density, cy - 7f * density)
                }
                
                val checkPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 6f * density
                    strokeCap = android.graphics.Paint.Cap.ROUND
                    strokeJoin = android.graphics.Paint.Join.ROUND
                    color = android.graphics.Color.parseColor("#10B981") // Success Green
                }
                
                canvas.drawPath(checkPath, checkPaint)
                canvas.restore()
                return
            }

            canvas.save()
            val targetSize = 56f * density
            val scale = targetSize / 960f
            
            canvas.translate(cx - targetSize / 2f, cy - targetSize / 2f)
            canvas.scale(scale, scale)
            canvas.translate(0f, 960f)

            when (currentState) {
                2 -> {
                    // Failure state: all ridges red
                    for (path in ridgePaths) {
                        canvas.drawPath(path, failurePaint)
                    }
                }
                else -> {
                    // Scanning state: Line by line fill animation
                    // 1) Bottom to Top: line by line turns purple (0.0 -> 0.5)
                    // 2) Top to Bottom: line by line turns back to 25% black (0.5 -> 1.0)
                    for (i in ridgePaths.indices) {
                        val path = ridgePaths[i]
                        
                        // Always draw base 25% black line first
                        canvas.drawPath(path, basePaint)

                        // Calculate purple fraction (0.0 = 25% black, 1.0 = full purple)
                        var purpleFraction = 0f

                        if (animProgress < 0.5f) {
                            // Phase 1: Bottom (i=0) to Top (i=4) turning purple
                            val startT = i * 0.09f
                            val endT = startT + 0.08f
                            if (animProgress >= startT) {
                                purpleFraction = if (animProgress >= endT) 1f else (animProgress - startT) / 0.08f
                            }
                        } else {
                            // Phase 2: Top (i=4) to Bottom (i=0) turning back to 25% black
                            val j = (ridgePaths.size - 1) - i // 0 for top (i=4), 4 for bottom (i=0)
                            val startT = 0.50f + j * 0.09f
                            val endT = startT + 0.08f
                            if (animProgress < startT) {
                                purpleFraction = 1f // still purple
                            } else if (animProgress >= endT) {
                                purpleFraction = 0f // turned black
                            } else {
                                purpleFraction = 1f - ((animProgress - startT) / 0.08f)
                            }
                        }

                        if (purpleFraction > 0f) {
                            val activePaint = ridgePaints[i]
                            val prevAlpha = activePaint.alpha
                            activePaint.alpha = (255 * purpleFraction).toInt().coerceIn(0, 255)
                            canvas.drawPath(path, activePaint)
                            activePaint.alpha = prevAlpha
                        }
                    }
                }
            }
            
            canvas.restore()
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
            
            var alertDialog: android.app.AlertDialog? = null

            // Parent FrameLayout to hold the card and overlay the close button
            val parentLayout = android.widget.FrameLayout(activity).apply {
                val backgroundDrawable = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.WHITE)
                    cornerRadius = 24 * density
                }
                background = backgroundDrawable
            }

            // Create root linear layout (no background anymore, background is on parent)
            val rootLayout = android.widget.LinearLayout(activity).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding((24 * density).toInt(), (28 * density).toInt(), (24 * density).toInt(), (24 * density).toInt())
                gravity = android.view.Gravity.CENTER_HORIZONTAL
            }
            parentLayout.addView(rootLayout)

            // Top-right Close/Cross Button
            val closeButton = android.widget.TextView(activity).apply {
                text = "✕"
                textSize = 18f
                setTextColor(android.graphics.Color.parseColor("#94A3B8")) // Slate 400
                setPadding((16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt(), (16 * density).toInt())
                
                setOnClickListener {
                    try {
                        cancellationSignal.cancel()
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                    onUsePinFallback()
                    alertDialog?.dismiss()
                }

                val outValue = android.util.TypedValue()
                activity.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
                setBackgroundResource(outValue.resourceId)
                isClickable = true
                isFocusable = true
            }
            val closeParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.TOP or android.view.Gravity.END
                topMargin = (4 * density).toInt()
                rightMargin = (4 * density).toInt()
            }
            parentLayout.addView(closeButton, closeParams)

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

            // Custom Fingerprint View with Animated Glowing Portal Background
            val fingerprintView = FingerprintView(activity).apply {
                val viewSize = (85 * density).toInt()
                layoutParams = android.view.ViewGroup.LayoutParams(viewSize, viewSize)
            }

            val composeView = androidx.compose.ui.platform.ComposeView(activity).apply {
                setViewTreeLifecycleOwner(activity)
                setViewTreeSavedStateRegistryOwner(activity)
                setViewCompositionStrategy(
                    androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
                )
                val portalSize = (120 * density).toInt()
                layoutParams = android.widget.LinearLayout.LayoutParams(portalSize, portalSize).apply {
                    gravity = android.view.Gravity.CENTER_HORIZONTAL
                    bottomMargin = (20 * density).toInt()
                }
                setContent {
                    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                    var showColorPickerDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                    var colorUpdateTrigger by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }

                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        com.example.ui.components.GlowingPortalBackground(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { fingerprintView },
                                modifier = Modifier
                                    .size((80 * density).dp)
                                    .pointerInput(colorUpdateTrigger) {
                                        detectTapGestures(
                                            onLongPress = {
                                                showColorPickerDialog = true
                                            }
                                        )
                                    }
                            )
                        }

                        if (showColorPickerDialog) {
                            FingerprintColorPickerDialog(
                                context = activity,
                                isDarkTheme = isDark,
                                onDismiss = { showColorPickerDialog = false },
                                onSave = {
                                    fingerprintView.loadCustomColors()
                                    colorUpdateTrigger++
                                    showColorPickerDialog = false
                                }
                            )
                        }
                    }
                }
            }
            rootLayout.addView(composeView)

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
                .setView(parentLayout)
                .setCancelable(false)

            alertDialog = dialogBuilder.create()

            alertDialog.window?.let { window ->
                window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                window.decorView.setViewTreeLifecycleOwner(activity)
                window.decorView.setViewTreeSavedStateRegistryOwner(activity)
            }

            // Dismiss dialog and cancel fingerprint scanning when screen turns off or app is stopped
            val screenOffReceiver = object : android.content.BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: android.content.Intent?) {
                    if (intent?.action == android.content.Intent.ACTION_SCREEN_OFF) {
                        try { cancellationSignal.cancel() } catch (_: Throwable) {}
                        try {
                            if (alertDialog.isShowing) {
                                alertDialog.dismiss()
                            }
                        } catch (_: Throwable) {}
                    }
                }
            }

            val lifecycleObserver = object : androidx.lifecycle.DefaultLifecycleObserver {
                override fun onStop(owner: androidx.lifecycle.LifecycleOwner) {
                    try { cancellationSignal.cancel() } catch (_: Throwable) {}
                    try {
                        if (alertDialog.isShowing) {
                            alertDialog.dismiss()
                        }
                    } catch (_: Throwable) {}
                }
            }

            try {
                activity.registerReceiver(
                    screenOffReceiver,
                    android.content.IntentFilter(android.content.Intent.ACTION_SCREEN_OFF)
                )
            } catch (_: Throwable) {}

            try {
                activity.lifecycle.addObserver(lifecycleObserver)
            } catch (_: Throwable) {}

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
                try {
                    activity.unregisterReceiver(screenOffReceiver)
                } catch (_: Throwable) {}
                try {
                    activity.lifecycle.removeObserver(lifecycleObserver)
                } catch (_: Throwable) {}
            }

            val callback = object : androidx.core.hardware.fingerprint.FingerprintManagerCompat.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.core.hardware.fingerprint.FingerprintManagerCompat.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    try {
                        BiometricFeedback.triggerScanSuccess(activity)
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
                        BiometricFeedback.triggerScanFailure(activity)
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
                                statusTextView.text = if (subtitle.contains("আপনার")) "ফিঙ্গারপ্রিন্ট সেন্সর স্পর্শ করুন" else "Touch Fingerprint Sensor"
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
            BiometricFeedback.triggerScanInitiated(activity)

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

object BiometricFeedback {
    fun triggerScanInitiated(context: Context) {
        try {
            val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 20)
            toneGen.startTone(android.media.ToneGenerator.TONE_PROP_BEEP, 35)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try { toneGen.release() } catch (_: Throwable) {}
            }, 100)
        } catch (_: Throwable) {}
    }

    fun triggerScanSuccess(context: Context) {
        try {
            val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 30)
            toneGen.startTone(android.media.ToneGenerator.TONE_PROP_ACK, 75)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try { toneGen.release() } catch (_: Throwable) {}
            }, 150)
        } catch (_: Throwable) {}
    }

    fun triggerScanFailure(context: Context) {
        try {
            val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 25)
            toneGen.startTone(android.media.ToneGenerator.TONE_PROP_NACK, 90)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try { toneGen.release() } catch (_: Throwable) {}
            }, 180)
        } catch (_: Throwable) {}
    }
}

@androidx.compose.runtime.Composable
fun FingerprintColorPickerDialog(
    context: Context,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val prefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
    val defaultColors = arrayOf("#38BDF8", "#84CC16", "#A855F7", "#06B6D4", "#EA580C")
    
    val customColors = androidx.compose.runtime.remember {
        val list = mutableListOf<androidx.compose.ui.graphics.Color>()
        for (i in 0 until 5) {
            val hex = prefs.getString("fingerprint_color_$i", defaultColors[i]) ?: defaultColors[i]
            val colorVal = try {
                androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex))
            } catch (e: Exception) {
                androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(defaultColors[i]))
            }
            list.add(colorVal)
        }
        val stateList = androidx.compose.runtime.mutableStateListOf<androidx.compose.ui.graphics.Color>()
        stateList.addAll(list)
        stateList
    }

    val languageIsBn = androidx.compose.runtime.remember {
        val lPrefs = context.getSharedPreferences("financenote_prefs", Context.MODE_PRIVATE)
        lPrefs.getString("app_language", "BN") == "BN"
    }

    var selectedIdx by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
    
    val activeColor = customColors.getOrNull(selectedIdx) ?: androidx.compose.ui.graphics.Color.White
    var redVal by androidx.compose.runtime.remember(selectedIdx) { androidx.compose.runtime.mutableStateOf((activeColor.red * 255f)) }
    var greenVal by androidx.compose.runtime.remember(selectedIdx) { androidx.compose.runtime.mutableStateOf((activeColor.green * 255f)) }
    var blueVal by androidx.compose.runtime.remember(selectedIdx) { androidx.compose.runtime.mutableStateOf((activeColor.blue * 255f)) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFF1E293B) else androidx.compose.ui.graphics.Color.White,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        tonalElevation = 8.dp,
        title = {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                androidx.compose.material3.Text(
                    text = if (languageIsBn) "ফিঙ্গারপ্রিন্ট কালার কাস্টমাইজ" else "Fingerprint Color",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (isDarkTheme) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color(0xFF1E293B)
                )
                androidx.compose.material3.IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = if (isDarkTheme) androidx.compose.ui.graphics.Color.Gray else androidx.compose.ui.graphics.Color.DarkGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        text = {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                androidx.compose.material3.Text(
                    text = if (languageIsBn) "যে কোনো একটি অংশ সিলেক্ট করে কালার কাস্টমাইজ করুন:" else "Select a ridge to customize its color:",
                    fontSize = 12.sp,
                    color = if (isDarkTheme) androidx.compose.ui.graphics.Color.LightGray else androidx.compose.ui.graphics.Color.DarkGray
                )

                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    customColors.forEachIndexed { idx, color ->
                        val isSelected = selectedIdx == idx
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) {
                                        if (isDarkTheme) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color(0xFF0F172A)
                                    } else {
                                        androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.5f)
                                    },
                                    shape = CircleShape
                                )
                                .clickable { selectedIdx = idx },
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            if (isSelected) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = if (color.red * 0.299 + color.green * 0.587 + color.blue * 0.114 > 0.5) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                androidx.compose.material3.Text(
                                    text = "${idx + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = if (color.red * 0.299 + color.green * 0.587 + color.blue * 0.114 > 0.5) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White
                                )
                            }
                        }
                    }
                }

                androidx.compose.material3.HorizontalDivider(color = if (isDarkTheme) androidx.compose.ui.graphics.Color(0xFF2E354F) else androidx.compose.ui.graphics.Color(0xFFE2E8F0))

                androidx.compose.foundation.layout.Column {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                    ) {
                        androidx.compose.material3.Text(
                            text = if (languageIsBn) "লাল (Red)" else "Red",
                            fontSize = 11.sp,
                            color = if (isDarkTheme) androidx.compose.ui.graphics.Color.LightGray else androidx.compose.ui.graphics.Color.Gray
                        )
                        androidx.compose.material3.Text(
                            text = "${redVal.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = if (isDarkTheme) androidx.compose.ui.graphics.Color.LightGray else androidx.compose.ui.graphics.Color.Gray
                        )
                    }
                    androidx.compose.material3.Slider(
                        value = redVal,
                        onValueChange = { newValue ->
                            redVal = newValue
                            val newCol = androidx.compose.ui.graphics.Color(redVal.toInt(), greenVal.toInt(), blueVal.toInt())
                            customColors[selectedIdx] = newCol
                        },
                        valueRange = 0f..255f,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = androidx.compose.ui.graphics.Color.Red,
                            activeTrackColor = androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.7f),
                            inactiveTrackColor = androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.2f)
                        )
                    )
                }

                androidx.compose.foundation.layout.Column {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                    ) {
                        androidx.compose.material3.Text(
                            text = if (languageIsBn) "সবুজ (Green)" else "Green",
                            fontSize = 11.sp,
                            color = if (isDarkTheme) androidx.compose.ui.graphics.Color.LightGray else androidx.compose.ui.graphics.Color.Gray
                        )
                        androidx.compose.material3.Text(
                            text = "${greenVal.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = if (isDarkTheme) androidx.compose.ui.graphics.Color.LightGray else androidx.compose.ui.graphics.Color.Gray
                        )
                    }
                    androidx.compose.material3.Slider(
                        value = greenVal,
                        onValueChange = { newValue ->
                            greenVal = newValue
                            val newCol = androidx.compose.ui.graphics.Color(redVal.toInt(), greenVal.toInt(), blueVal.toInt())
                            customColors[selectedIdx] = newCol
                        },
                        valueRange = 0f..255f,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = androidx.compose.ui.graphics.Color(0xFF10B981),
                            activeTrackColor = androidx.compose.ui.graphics.Color(0xFF10B981).copy(alpha = 0.7f),
                            inactiveTrackColor = androidx.compose.ui.graphics.Color(0xFF10B981).copy(alpha = 0.2f)
                        )
                    )
                }

                androidx.compose.foundation.layout.Column {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                    ) {
                        androidx.compose.material3.Text(
                            text = if (languageIsBn) "নীল (Blue)" else "Blue",
                            fontSize = 11.sp,
                            color = if (isDarkTheme) androidx.compose.ui.graphics.Color.LightGray else androidx.compose.ui.graphics.Color.Gray
                        )
                        androidx.compose.material3.Text(
                            text = "${blueVal.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = if (isDarkTheme) androidx.compose.ui.graphics.Color.LightGray else androidx.compose.ui.graphics.Color.Gray
                        )
                    }
                    androidx.compose.material3.Slider(
                        value = blueVal,
                        onValueChange = { newValue ->
                            blueVal = newValue
                            val newCol = androidx.compose.ui.graphics.Color(redVal.toInt(), greenVal.toInt(), blueVal.toInt())
                            customColors[selectedIdx] = newCol
                        },
                        valueRange = 0f..255f,
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = androidx.compose.ui.graphics.Color(0xFF3B82F6),
                            activeTrackColor = androidx.compose.ui.graphics.Color(0xFF3B82F6).copy(alpha = 0.7f),
                            inactiveTrackColor = androidx.compose.ui.graphics.Color(0xFF3B82F6).copy(alpha = 0.2f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = {
                    val edit = prefs.edit()
                    customColors.forEachIndexed { i, col ->
                        val hex = String.format("#%02X%02X%02X", (col.red * 255f).toInt(), (col.green * 255f).toInt(), (col.blue * 255f).toInt())
                        edit.putString("fingerprint_color_$i", hex)
                    }
                    edit.apply()
                    onSave()
                },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF4F46E5)
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                androidx.compose.material3.Text(
                    text = if (languageIsBn) "সংরক্ষণ" else "Save",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(
                onClick = onDismiss
            ) {
                androidx.compose.material3.Text(
                    text = if (languageIsBn) "বাতিল" else "Cancel",
                    color = if (isDarkTheme) androidx.compose.ui.graphics.Color.LightGray else androidx.compose.ui.graphics.Color.Gray,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }
        }
    )
}
