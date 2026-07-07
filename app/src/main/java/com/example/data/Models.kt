package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "persons")
@JsonClass(generateAdapter = true)
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val photoUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
@JsonClass(generateAdapter = true)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double,
    val type: String, // "INCOME", "EXPENSE", "LEND", "BORROW", "REPAY_PAID", "REPAY_RECEIVED"
    val category: String, // e.g. "Salary", "Food", "Shopping", "Lending", "Borrowing", "Repayment"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val personId: Int? = null // Linked person if it's a debt/credit related transaction
)

@Entity(tableName = "savings_goals")
@JsonClass(generateAdapter = true)
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val category: String = "General",
    val colorIndex: Int = 0, // Index for choosing distinct fintech gradient card themes
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "savings_transactions")
@JsonClass(generateAdapter = true)
data class SavingsTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val amount: Double,
    val isDeposit: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class FinanceBackup(
    val persons: List<Person>,
    val transactions: List<Transaction>,
    val savingsGoals: List<SavingsGoal>,
    val savingsTransactions: List<SavingsTransaction> = emptyList()
)
