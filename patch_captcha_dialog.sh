sed -i '550,565c\
@Composable\
fun CaptchaDeleteDialog(\
    fileName: String,\
    language: AppLanguage,\
    isDark: Boolean,\
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
        val captchaCode = remember {\
            val chars = "23456789ABCDEFGHJKMNPQRSTUVWXYZ"\
            (1..4).map { chars.random() }.joinToString("")\
        }\
        var userInput by remember { mutableStateOf("") }\
        val isCorrect = userInput.trim().equals(captchaCode, ignoreCase = true)\
        Dialog(onDismissRequest = onDismiss) {\
' app/src/main/java/com/example/ui/screens/BackupRestoreDialogs.kt
