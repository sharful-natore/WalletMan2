package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.AppLanguage

@Composable
fun SearchDialog(
    onDismissRequest: () -> Unit,
    language: AppLanguage
) {
    var searchQuery by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(if (language == AppLanguage.BN) "সার্চ" else "Search", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(if (language == AppLanguage.BN) "কিছু খুঁজুন..." else "Search for something...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Search results placeholder
                Text(if (language == AppLanguage.BN) "এখানে রেজাল্ট আসবে" else "Results will appear here", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
