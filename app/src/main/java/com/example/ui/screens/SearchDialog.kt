package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Person
import com.example.data.SavingsGoal
import com.example.data.Transaction
import com.example.ui.AppLanguage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDialog(
    onDismissRequest: () -> Unit,
    language: AppLanguage,
    isDark: Boolean,
    transactions: List<Transaction>,
    persons: List<Person>,
    savingsGoals: List<SavingsGoal>,
    onNavigateToTransaction: (Int) -> Unit,
    onNavigateToPerson: (Int) -> Unit,
    onNavigateToGoal: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredTransactions = remember(searchQuery, transactions) {
        if (searchQuery.isBlank()) emptyList()
        else transactions.filter { it.category.contains(searchQuery, true) || it.note.contains(searchQuery, true) }
    }
    val filteredPersons = remember(searchQuery, persons) {
        if (searchQuery.isBlank()) emptyList()
        else persons.filter { it.name.contains(searchQuery, true) || it.phone.contains(searchQuery, true) }
    }
    val filteredGoals = remember(searchQuery, savingsGoals) {
        if (searchQuery.isBlank()) emptyList()
        else savingsGoals.filter { it.title.contains(searchQuery, true) }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDark) Color(0xFF0F121F) else Color(0xFFF8FAFC)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top App Bar
                TopAppBar(
                    title = { Text(if (language == AppLanguage.BN) "অনুসন্ধান" else "Search") },
                    navigationIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = if (isDark) Color.White else Color(0xFF1E293B),
                        navigationIconContentColor = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                )

                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (language == AppLanguage.BN) "কিছু খুঁজুন..." else "Search for something...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0xFF1E2235) else Color.White,
                        unfocusedContainerColor = if (isDark) Color(0xFF1E2235) else Color.White,
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = Color.Transparent
                    ),
                    singleLine = true
                )

                // Results
                if (searchQuery.isNotBlank()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (filteredTransactions.isNotEmpty()) {
                            item {
                                Text(
                                    text = if (language == AppLanguage.BN) "ট্রানজ্যাকশন" else "Transactions",
                                    color = if (isDark) Color.Gray else Color.DarkGray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            items(filteredTransactions) { tx ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1A1D2D) else Color.White),
                                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToTransaction(tx.id) }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(tx.category, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                                            Text(
                                                text = "৳ ${tx.amount}",
                                                color = if (tx.type == "INCOME" || tx.type == "REPAY_RECEIVED") Color(0xFF10B981) else Color(0xFFEF4444),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        if (tx.note.isNotBlank()) {
                                            Text(tx.note, fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredPersons.isNotEmpty()) {
                            item {
                                Text(
                                    text = if (language == AppLanguage.BN) "দেনা-পাওনা" else "Debts",
                                    color = if (isDark) Color.Gray else Color.DarkGray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(filteredPersons) { person ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1A1D2D) else Color.White),
                                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToPerson(person.id) }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(person.name, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                                        Text(if (person.phone.isNotBlank()) person.phone else if (language == AppLanguage.BN) "ফোন নম্বর নেই" else "No phone", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        if (filteredGoals.isNotEmpty()) {
                            item {
                                Text(
                                    text = if (language == AppLanguage.BN) "সঞ্চয় লক্ষ্য" else "Savings Goals",
                                    color = if (isDark) Color.Gray else Color.DarkGray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(filteredGoals) { goal ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1A1D2D) else Color.White),
                                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToGoal(goal.id) }
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(goal.title, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
                                        Text(
                                            text = if (language == AppLanguage.BN) "লক্ষ্য: ৳${goal.targetAmount} | জমানো: ৳${goal.savedAmount}" else "Target: ৳${goal.targetAmount} | Saved: ৳${goal.savedAmount}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        if (filteredTransactions.isEmpty() && filteredPersons.isEmpty() && filteredGoals.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (language == AppLanguage.BN) "কোনো ফলাফল পাওয়া যায়নি" else "No results found",
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (language == AppLanguage.BN) "খুঁজতে টাইপ করুন..." else "Type to search...",
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
