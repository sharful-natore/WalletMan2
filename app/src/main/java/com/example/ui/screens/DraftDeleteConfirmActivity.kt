package com.example.ui.screens

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.data.AppDatabase
import com.example.data.FinanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DraftDeleteConfirmActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val draftId = intent.getIntExtra("draft_id", -1)
        if (draftId == -1) {
            finish()
            return
        }

        setContent {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f))) {
                AlertDialog(
                    onDismissRequest = { finish() },
                    title = { Text("Delete Draft") },
                    text = { Text("Are you sure you want to delete this draft?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                val db = com.example.data.AppDatabase.getDatabase(applicationContext)
                                val repo = com.example.data.FinanceRepository(db.financeDao())
                                CoroutineScope(Dispatchers.IO).launch {
                                    repo.deleteDraftTransaction(draftId)
                                }
                                finish()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                        ) {
                            Text("Delete", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { finish() }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
