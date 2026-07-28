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
            // For API < 29 (Android 8.1/8.0/7.0), use FingerprintManagerCompat directly.
            // This is 100% safe and avoids loading any BiometricManager class references that cause crashes on custom OEM ROMs like Oppo ColorOS.
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
            // Use 255 (BIOMETRIC_STRONG constant value) safely without referencing the Authenticator class if it fails class verification on some devices
            val result = if (sdkVersion >= 30) {
                biometricManager.canAuthenticate(255) // 255 is BiometricManager.Authenticators.BIOMETRIC_STRONG
            } else {
                biometricManager.canAuthenticate()
            }
            
            when (result) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
                else -> {
                    // Fallback to FingerprintManagerCompat as a safety measure
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
            // Final fail-safe fallback
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
     * Show Android's standard BiometricPrompt with custom Title, Subtitle, Negative Button ("Use PIN")
     * and fallback handling.
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Unlock Finance Note",
        subtitle: String = "Use your fingerprint or face to access your account",
        negativeButtonText: String = "Use PIN",
        onSuccess: () -> Unit,
        onUsePinFallback: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val sdkVersion = android.os.Build.VERSION.SDK_INT
        if (sdkVersion < 29) {
            // Bypass BiometricPrompt on older Android versions entirely to prevent OEM crashes
            showFingerprintPromptFallback(
                activity = activity,
                title = title,
                subtitle = subtitle,
                negativeButtonText = negativeButtonText,
                onSuccess = onSuccess,
                onUsePinFallback = onUsePinFallback,
                onError = onError
            )
            return
        }

        // On Android 10+ (SDK >= 29), use standard BiometricPrompt, with safety fallbacks
        try {
            val status = getBiometricStatus(activity)
            if (status != BiometricStatus.AVAILABLE) {
                val errorMsg = when (status) {
                    BiometricStatus.NO_HARDWARE -> "No biometric hardware detected."
                    BiometricStatus.NOT_ENROLLED -> "No biometrics enrolled on this device."
                    else -> "Biometric security is currently unavailable."
                }
                onError(errorMsg)
                onUsePinFallback()
                return
            }

            val executor = ContextCompat.getMainExecutor(activity)
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    try {
                        onSuccess()
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        onError(t.localizedMessage ?: "Success handler exception")
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    try {
                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || 
                            errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                            onUsePinFallback()
                        } else {
                            onError(errString.toString())
                        }
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            }

            val biometricPrompt = try {
                BiometricPrompt(activity, executor, callback)
            } catch (e: Throwable) {
                e.printStackTrace()
                try { onError("Native prompt fail: ${e.localizedMessage}") } catch (t: Throwable) {}
                // Instantiate failed -> use fingerprint dialog fallback
                showFingerprintPromptFallback(
                    activity = activity,
                    title = title,
                    subtitle = subtitle,
                    negativeButtonText = negativeButtonText,
                    onSuccess = onSuccess,
                    onUsePinFallback = onUsePinFallback,
                    onError = onError
                )
                return
            }

            val promptInfo = try {
                val builder = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setNegativeButtonText(negativeButtonText)
                if (sdkVersion >= 30) {
                    builder.setAllowedAuthenticators(255) // BIOMETRIC_STRONG
                }
                builder.build()
            } catch (e: Throwable) {
                e.printStackTrace()
                try {
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle(title)
                        .setSubtitle(subtitle)
                        .setNegativeButtonText(negativeButtonText)
                        .build()
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                    try { onError("Prompt builder fail: ${ex.localizedMessage}") } catch (t: Throwable) {}
                    showFingerprintPromptFallback(
                        activity = activity,
                        title = title,
                        subtitle = subtitle,
                        negativeButtonText = negativeButtonText,
                        onSuccess = onSuccess,
                        onUsePinFallback = onUsePinFallback,
                        onError = onError
                    )
                    return
                }
            }

            try {
                biometricPrompt.authenticate(promptInfo)
            } catch (e: Throwable) {
                e.printStackTrace()
                try { onError("Authenticate call fail: ${e.localizedMessage}") } catch (t: Throwable) {}
                // Authentication execution failed -> use fingerprint dialog fallback
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
        } catch (e: Throwable) {
            e.printStackTrace()
            try { onError("System crash fallback: ${e.localizedMessage}") } catch (t: Throwable) {}
            try {
                showFingerprintPromptFallback(
                    activity = activity,
                    title = title,
                    subtitle = subtitle,
                    negativeButtonText = negativeButtonText,
                    onSuccess = onSuccess,
                    onUsePinFallback = onUsePinFallback,
                    onError = onError
                )
            } catch (t: Throwable) {
                t.printStackTrace()
                onUsePinFallback()
            }
        }
    }

    /**
     * Custom AlertDialog-based Fingerprint authentication fallback for Android 9 and below.
     * This avoids any Fragment or BiometricPrompt class loading issues on custom OEM skins.
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
            val paddingLarge = (24 * density).toInt()
            val paddingSmall = (12 * density).toInt()

            val rootLayout = android.widget.LinearLayout(activity).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setPadding(paddingLarge, paddingLarge, paddingLarge, paddingSmall)
                gravity = android.view.Gravity.CENTER_HORIZONTAL
            }

            val subtitleTextView = android.widget.TextView(activity).apply {
                text = subtitle
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.parseColor("#475569"))
                setPadding(0, 0, 0, (20 * density).toInt())
            }
            rootLayout.addView(subtitleTextView)

            val iconView = android.widget.ImageView(activity).apply {
                // Safe system icon to avoid layout or reference errors
                setImageResource(android.R.drawable.ic_dialog_info)
                val iconSize = (64 * density).toInt()
                layoutParams = android.widget.LinearLayout.LayoutParams(iconSize, iconSize).apply {
                    gravity = android.view.Gravity.CENTER_HORIZONTAL
                }
                setColorFilter(android.graphics.Color.parseColor("#0284C7"))
            }
            rootLayout.addView(iconView)

            val statusTextView = android.widget.TextView(activity).apply {
                text = "Touch Fingerprint Sensor"
                textSize = 15f
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.parseColor("#0284C7"))
                setPadding(0, (16 * density).toInt(), 0, 0)
            }
            rootLayout.addView(statusTextView)

            // Use android.app.AlertDialog to prevent any theme dependency crashes on OEM ROMs
            val dialogBuilder = android.app.AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(rootLayout)
                .setCancelable(false)
                .setNegativeButton(negativeButtonText) { dialog, _ ->
                    try {
                        cancellationSignal.cancel()
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
                    onUsePinFallback()
                    dialog.dismiss()
                }

            val alertDialog = dialogBuilder.create()

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
                        statusTextView.text = "Success!"
                        statusTextView.setTextColor(android.graphics.Color.parseColor("#22C55E"))
                        iconView.setColorFilter(android.graphics.Color.parseColor("#22C55E"))

                        rootLayout.postDelayed({
                            try {
                                alertDialog.dismiss()
                                onSuccess()
                            } catch (t: Throwable) {
                                t.printStackTrace()
                                onSuccess()
                            }
                        }, 400)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                        onSuccess()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    try {
                        statusTextView.text = "Fingerprint not recognized. Try again."
                        statusTextView.setTextColor(android.graphics.Color.parseColor("#EF4444"))
                        iconView.setColorFilter(android.graphics.Color.parseColor("#EF4444"))

                        statusTextView.postDelayed({
                            try {
                                statusTextView.text = "Touch Fingerprint Sensor"
                                statusTextView.setTextColor(android.graphics.Color.parseColor("#0284C7"))
                                iconView.setColorFilter(android.graphics.Color.parseColor("#0284C7"))
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
                        // 5 = FINGERPRINT_ERROR_CANCELED, 10 = FINGERPRINT_ERROR_USER_CANCELED
                        if (errMsgId == 5 || errMsgId == 10) {
                            return
                        }

                        statusTextView.text = errorMsg
                        statusTextView.setTextColor(android.graphics.Color.parseColor("#EF4444"))
                        iconView.setColorFilter(android.graphics.Color.parseColor("#EF4444"))

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
                        statusTextView.setTextColor(android.graphics.Color.parseColor("#F59E0B"))
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

    /**
     * Reusable callback helper function to trigger re-authentication for sensitive actions
     * (e.g., restoring Drive backup, permanent trash deletion).
     */
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
