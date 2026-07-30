sed -i '14679d' app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt
sed -i '14682i\        val isBiometricActionEnabled by viewModel.isBiometricActionEnabled.collectAsState()' app/src/main/java/com/example/ui/screens/FinanceNoteApp.kt
