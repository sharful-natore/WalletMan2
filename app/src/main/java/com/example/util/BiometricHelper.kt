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
                onError("Initialization failed: ${e.localizedMessage}")
                onUsePinFallback()
                return
            }

            val promptInfo = try {
                val builder = BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle)
                    .setNegativeButtonText(negativeButtonText)
                if (android.os.Build.VERSION.SDK_INT >= 30) {
                    builder.setAllowedAuthenticators(255) // 255 is BiometricManager.Authenticators.BIOMETRIC_STRONG
                }
                builder.build()
            } catch (e: Throwable) {
                e.printStackTrace()
                // Fallback to standard builder without explicit strong requirement if API doesn't support setAllowedAuthenticators on older SDK
                try {
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle(title)
                        .setSubtitle(subtitle)
                        .setNegativeButtonText(negativeButtonText)
                        .build()
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                    onError("Prompt builder failed: ${ex.localizedMessage}")
                    onUsePinFallback()
                    return
                }
            }

            try {
                biometricPrompt.authenticate(promptInfo)
            } catch (e: Throwable) {
                e.printStackTrace()
                onError("Failed to launch authenticating prompt: ${e.localizedMessage}")
                onUsePinFallback()
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            try {
                onError("System error: ${e.localizedMessage}")
                onUsePinFallback()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
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
