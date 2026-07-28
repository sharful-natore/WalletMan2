package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.activity.SystemBarStyle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import com.example.data.AppDatabase
import com.example.data.FinanceRepository
import com.example.ui.screens.FinanceNoteApp
import com.example.ui.theme.FinanceNoteTheme
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.FinanceViewModelFactory

class MainActivity : FragmentActivity() {
    private val actionState = androidx.compose.runtime.mutableStateOf<String?>(null)
    private val targetWorkspaceState = androidx.compose.runtime.mutableStateOf<String?>(null)
    private val targetDraftIdState = androidx.compose.runtime.mutableStateOf<Int?>(null)

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        actionState.value = intent.action
        targetWorkspaceState.value = intent.getStringExtra("EXTRA_TARGET_WORKSPACE_ID")
        val draftId = intent.getIntExtra("EXTRA_TARGET_DRAFT_ID", -1)
        targetDraftIdState.value = if (draftId != -1) draftId else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionState.value = intent?.action
        targetWorkspaceState.value = intent?.getStringExtra("EXTRA_TARGET_WORKSPACE_ID")
        val draftId = intent?.getIntExtra("EXTRA_TARGET_DRAFT_ID", -1) ?: -1
        targetDraftIdState.value = if (draftId != -1) draftId else null
        
        // Register global exception handler for logging crashes
        com.example.data.ErrorLogger.registerUncaughtExceptionHandler(this)

        // Initialize Firebase early
        FinanceApplication.ensureFirebaseInitialized(this)

        try {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
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
            val allGradientsConfig by viewModel.allGradientsConfig.collectAsState(initial = emptyList())
            val isScreenSecurityEnabled by viewModel.isScreenSecurityEnabled.collectAsState()
            val action by actionState
            val targetWorkspaceId by targetWorkspaceState
            val targetDraftId by targetDraftIdState
            
            LaunchedEffect(isScreenSecurityEnabled) {
                if (isScreenSecurityEnabled) {
                    window.setFlags(
                        android.view.WindowManager.LayoutParams.FLAG_SECURE,
                        android.view.WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                }
            }
            
            val activeThemeGradient = remember(allGradientsConfig, themeGradientIndex) {
                if (themeGradientIndex in allGradientsConfig.indices) allGradientsConfig[themeGradientIndex]
                else allGradientsConfig.firstOrNull() ?: com.example.ui.theme.ThemeGradient(com.example.ui.theme.GradientsList[0])
            }
            
            FinanceNoteTheme(darkTheme = isDarkTheme, language = language, themePrimaryColor = activeThemeGradient.colors.firstOrNull() ?: com.example.ui.theme.FintechBlue) {
                androidx.compose.runtime.CompositionLocalProvider(com.example.ui.screens.LocalActiveThemeGradient provides activeThemeGradient) {
                    FinanceNoteApp(
                        viewModel = viewModel, 
                        initialAction = action,
                        targetWorkspaceId = targetWorkspaceId,
                        targetDraftId = targetDraftId
                    )
                }
            }
        }
    }
}
