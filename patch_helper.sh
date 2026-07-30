sed -i '118i\
fun performActionWithSecurity(\
    context: android.content.Context,\
    viewModel: com.example.ui.viewmodel.FinanceViewModel,\
    actionTitle: String = "Action Confirmation",\
    actionSubtitle: String = "Verify your identity to proceed",\
    onSuccess: () -> Unit,\
    onFallback: () -> Unit\
) {\
    if (viewModel.isBiometricEnabled.value && viewModel.isBiometricActionEnabled.value && com.example.util.BiometricHelper.isBiometricAvailable(context)) {\
        com.example.util.BiometricHelper.showBiometricPrompt(\
            activity = context as androidx.fragment.app.FragmentActivity,\
            title = actionTitle,\
            subtitle = actionSubtitle,\
            negativeButtonText = if (viewModel.language.value == com.example.ui.AppLanguage.BN) "ক্যাপচা ব্যবহার করুন" else "Use CAPTCHA",\
            onSuccess = onSuccess,\
            onUsePinFallback = onFallback,\
            onError = { onFallback() }\
        )\
    } else {\
        onFallback()\
    }\
}\
' app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt
