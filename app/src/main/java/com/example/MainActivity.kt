package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.SystemBarStyle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.example.data.AppDatabase
import com.example.data.FinanceRepository
import com.example.ui.screens.FinanceNoteApp
import com.example.ui.theme.FinanceNoteTheme
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.FinanceViewModelFactory

class MainActivity : ComponentActivity() {
    private val actionState = androidx.compose.runtime.mutableStateOf<String?>(null)
    private val targetWorkspaceState = androidx.compose.runtime.mutableStateOf<String?>(null)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        actionState.value = intent.action
        targetWorkspaceState.value = intent.getStringExtra("EXTRA_TARGET_WORKSPACE_ID")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionState.value = intent?.action
        targetWorkspaceState.value = intent?.getStringExtra("EXTRA_TARGET_WORKSPACE_ID")
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isStatusBarContrastEnforced = false
            window.isNavigationBarContrastEnforced = false
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        
        // Register global exception handler for logging crashes
        com.example.data.ErrorLogger.registerUncaughtExceptionHandler(this)
        
        // Initialize database & repository
        val database = AppDatabase.getDatabase(this)
        val repository = FinanceRepository(database.financeDao())
        
        // ViewModel creation
        val factory = FinanceViewModelFactory(repository, application)
        val viewModel: FinanceViewModel by viewModels { factory }
        
        // Load persistent settings
        viewModel.loadProfile(this)
        
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val language by viewModel.language.collectAsState()
            val themeGradientIndex by viewModel.selectedThemeGradientIndex.collectAsState()
            val action by actionState
            val targetWorkspaceId by targetWorkspaceState
            
            FinanceNoteTheme(darkTheme = isDarkTheme, language = language, themeGradientIndex = themeGradientIndex) {
                FinanceNoteApp(
                    viewModel = viewModel, 
                    initialAction = action,
                    targetWorkspaceId = targetWorkspaceId
                )
            }
        }
    }
}
