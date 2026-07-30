sed -i '1772,1788c\
@Composable\
fun DeleteVerificationDialog(\
    language: AppLanguage,\
    onConfirm: () -> Unit,\
    onDismiss: () -> Unit\
) {\
    val context = androidx.compose.ui.platform.LocalContext.current\
    var showCaptchaDialog by remember { mutableStateOf(false) }\
\
    LaunchedEffect(Unit) {\
        val prefs = context.getSharedPreferences("financenote_prefs", android.content.Context.MODE_PRIVATE)\
        val bioEnabled = prefs.getBoolean("biometric_lock_enabled", false)\
        val actionEnabled = prefs.getBoolean("biometric_action_confirmation_enabled", true)\
        \
        if (bioEnabled && actionEnabled && com.example.util.BiometricHelper.isBiometricAvailable(context)) {\
            com.example.util.BiometricHelper.showBiometricPrompt(\
                activity = context as androidx.fragment.app.FragmentActivity,\
                title = if (language == AppLanguage.BN) "মুছে ফেলার নিশ্চিতকরণ" else "Confirm Deletion",\
                subtitle = if (language == AppLanguage.BN) "মুছে ফেলার আগে আপনার পরিচয় নিশ্চিত করুন" else "Verify your identity before deleting",\
                negativeButtonText = if (language == AppLanguage.BN) "ক্যাপচা ব্যবহার করুন" else "Use CAPTCHA",\
                onSuccess = { onConfirm() },\
                onUsePinFallback = { showCaptchaDialog = true },\
                onError = { showCaptchaDialog = true }\
            )\
        } else {\
            showCaptchaDialog = true\
        }\
    }\
\
    if (showCaptchaDialog) {\
        val verificationCode = remember { (1000..9999).random().toString() }\
        var userInput by remember { mutableStateOf("") }\
        val isMatched = userInput.trim() == verificationCode\
        val title = if (language == AppLanguage.BN) "মুছে ফেলার নিশ্চিতকরণ" else "Confirm Deletion"\
        val msg = if (language == AppLanguage.BN) {\
            "এটি মুছে ফেলতে নিচে দেখানো কোডটি টাইপ করুন:"\
        } else {\
            "To delete, please type the verification code below:"\
        }\
' app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt
