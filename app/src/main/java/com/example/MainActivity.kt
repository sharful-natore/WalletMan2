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
import com.example.ui.screens.SanchayApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.FinanceViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize database & repository
        val database = AppDatabase.getDatabase(this)
        val repository = FinanceRepository(database.financeDao())
        
        // ViewModel creation
        val factory = FinanceViewModelFactory(repository)
        val viewModel: FinanceViewModel by viewModels { factory }
        
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            
            LaunchedEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(0xFF3F8CFF.toInt()),
                    navigationBarStyle = SystemBarStyle.dark(0xFF3F8CFF.toInt())
                )
                
                // Explicitly configure status bar and navigation bar icon visibility using WindowCompat
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
            
            MyApplicationTheme(darkTheme = isDarkTheme) {
                SanchayApp(viewModel = viewModel)
            }
        }
    }
}
