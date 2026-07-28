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
        return try {
            val biometricManager = BiometricManager.from(context)
            val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
            when (biometricManager.canAuthenticate(authenticators)) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
                else -> BiometricStatus.UNAVAILABLE
            }
        } catch (e: Throwable) {
            BiometricStatus.UNAVAILABLE
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
                // Hardware missing, not enrolled, or unavailable -> Fallback to PIN
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
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    try {
                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || 
                            errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                            onUsePinFallback()
                        } else if (errorCode == BiometricPrompt.ERROR_LOCKOUT || 
                                   errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT ||
                                   errorCode == BiometricPrompt.ERROR_NO_BIOMETRICS ||
                                   errorCode == BiometricPrompt.ERROR_HW_UNAVAILABLE) {
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

            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(negativeButtonText)
                .build()

            biometricPrompt.authenticate(promptInfo)
        } catch (e: Throwable) {
            e.printStackTrace()
            try {
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
