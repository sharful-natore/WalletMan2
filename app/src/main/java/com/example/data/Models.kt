package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "workspaces")
@JsonClass(generateAdapter = true)
data class Workspace(
    @PrimaryKey val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val budgetIncome: Double = 0.0,
    val budgetExpense: Double = 0.0,
    val budgetSavings: Double = 0.0,
    val profileName: String = "",
    val profileEmail: String = "",
    val profilePhone: String = "",
    val profileSocial: String = "",
    val profileAddress: String = "",
    val profilePhotoUri: String? = null
)

@Entity(tableName = "persons")
@JsonClass(generateAdapter = true)
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val photoUri: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val workspaceId: String = "default"
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
    val personId: Int? = null, // Linked person if it's a debt/credit related transaction
    val workspaceId: String = "default",
    val subType: String? = "CASH" // "CASH" or "CREDIT"
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
    val cardholderName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val workspaceId: String = "default"
)

@Entity(tableName = "savings_transactions")
@JsonClass(generateAdapter = true)
data class SavingsTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val amount: Double,
    val isDeposit: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val workspaceId: String = "default"
)

@JsonClass(generateAdapter = true)
data class FinanceBackup(
    val persons: List<Person>,
    val transactions: List<Transaction>,
    val savingsGoals: List<SavingsGoal>,
    val savingsTransactions: List<SavingsTransaction> = emptyList(),
    val workspaces: List<Workspace> = emptyList(),
    val trashItems: List<TrashItem> = emptyList(),
    val monthlyBudgets: List<MonthlyBudget> = emptyList(),
    val budgetIncome: Double? = 0.0,
    val budgetExpense: Double? = 0.0,
    val budgetSavings: Double? = 0.0,
    val comment: String? = "",
    val createdAt: Long? = null,
    val profileName: String = "",
    val profileEmail: String = "",
    val profilePhone: String = "",
    val profileSocial: String = "",
    val profileAddress: String = "",
    val profilePhotoUri: String? = null,
    val customGradientsConfigSerialized: String? = null,
    val staticGradientOverridesSerialized: String? = null,
    val chartGradientsSerialized: String? = null,
    val selectedThemeGradientIndex: Int? = null
)

@JsonClass(generateAdapter = true)
data class GoogleTokenResponse(
    val access_token: String,
    val refresh_token: String? = null,
    val expires_in: Long? = null,
    val token_type: String? = null
)

@JsonClass(generateAdapter = true)
data class GoogleUserInfoResponse(
    val name: String? = null,
    val email: String? = null,
    val picture: String? = null
)

@JsonClass(generateAdapter = true)
data class GoogleDriveFile(
    val id: String,
    val name: String,
    val mimeType: String? = null,
    val createdTime: String? = null,
    val size: String? = null
)

@JsonClass(generateAdapter = true)
data class GoogleDriveFilesResponse(
    val files: List<GoogleDriveFile>
)

@Entity(tableName = "trash_items")
@JsonClass(generateAdapter = true)
data class TrashItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val originalId: Int,
    val itemType: String, // "TRANSACTION", "PERSON", "SAVINGS_GOAL", "SAVINGS_TRANSACTION"
    val itemJson: String,
    val deletedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "debt_notification_logs")
data class DebtNotificationLog(
    @PrimaryKey val personId: Int,
    val lastNotifiedAt: Long
)

@Entity(tableName = "monthly_budgets")
@JsonClass(generateAdapter = true)
data class MonthlyBudget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val year: Int,
    val month: Int,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val savings: Double = 0.0,
    val workspaceId: String = "default"
)

@JsonClass(generateAdapter = true)
data class PersonWithTransactions(
    val person: Person,
    val transactions: List<Transaction>
)

@JsonClass(generateAdapter = true)
data class GoalWithTransactions(
    val goal: SavingsGoal,
    val transactions: List<SavingsTransaction>
)

@JsonClass(generateAdapter = true)
data class DeletedGDriveBackup(
    val fileId: String,
    val fileName: String,
    val backupJson: String
)

data class WorkspaceStats(
    val workspace: Workspace,
    val profileName: String,
    val profilePhoto: String?,
    val income: Double,
    val expense: Double,
    val netOwedToMe: Double,
    val netIOwe: Double,
    val personCount: Int,
    val cardCount: Int
)

