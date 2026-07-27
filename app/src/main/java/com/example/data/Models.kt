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
    val draftTransactions: List<DraftTransaction> = emptyList(),
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

@Entity(tableName = "draft_transactions")
@JsonClass(generateAdapter = true)
data class DraftTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Double? = null,
    val type: String? = null, // "INCOME", "EXPENSE", "LEND", "BORROW" etc. or null
    val category: String? = null,
    val note: String,
    val timestamp: Long = System.currentTimeMillis(),
    val workspaceId: String = "default"
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

data class DraftParseResult(
    val amount: Double?,
    val type: String?,
    val category: String?,
    val cleanedNote: String
)

object DraftParser {
    fun parse(note: String): DraftParseResult {
        if (note.isBlank()) {
            return DraftParseResult(null, null, null, "")
        }

        var amount: Double? = null
        var rawAmountStr = ""
        val digitRegex = Regex("[0-9০-৯]+")
        val matches = digitRegex.findAll(note)
        for (match in matches) {
            val numStr = match.value
            val engNumStr = numStr.map { c ->
                if (c in '০'..'৯') (c - '০' + 48).toChar().toString() else c.toString()
            }.joinToString("")
            val parsed = engNumStr.toDoubleOrNull()
            if (parsed != null && parsed > 0) {
                amount = parsed
                rawAmountStr = numStr
                break
            }
        }

        val noteLower = note.lowercase()
        var type: String? = null
        var category: String? = null

        val isExpenseExplicit = noteLower.contains("ব্যয়") || noteLower.contains("খরচ") || noteLower.contains("expense") || noteLower.contains("cost")
        val isIncomeExplicit = noteLower.contains("আয়") || noteLower.contains("ইনকাম") || noteLower.contains("income") || noteLower.contains("earning")
        val isBorrowExplicit = noteLower.contains("দেনা") || noteLower.contains("ধার নেওয়া") || noteLower.contains("ধার নিলাম") || noteLower.contains("কর্জ") || noteLower.contains("কর্য") || noteLower.contains("borrow") || noteLower.contains("debt")
        val isLendExplicit = noteLower.contains("পাওনা") || noteLower.contains("ধার দেওয়া") || noteLower.contains("ধার দিলাম") || noteLower.contains("ধারে দিলাম") || noteLower.contains("lend") || noteLower.contains("receivable")
        val isSavingsExplicit = noteLower.contains("সঞ্চয়") || noteLower.contains("savings") || noteLower.contains("ডিপোজিট") || noteLower.contains("সংরক্ষণ")
        val isWithdrawalExplicit = noteLower.contains("উত্তোলন") || noteLower.contains("withdraw") || noteLower.contains("উঠালাম") || noteLower.contains("ক্যাশ আউট")

        val hasSalary = noteLower.contains("বেতন") || noteLower.contains("salary")
        val hasBusiness = noteLower.contains("ব্যবসা") || noteLower.contains("business")
        val hasGift = noteLower.contains("উপহার") || noteLower.contains("gift")
        val hasHonorarium = noteLower.contains("সম্মানী") || noteLower.contains("honorarium")

        val hasFood = noteLower.contains("খাবার") || noteLower.contains("চা") || noteLower.contains("ভাত") || noteLower.contains("নাস্তা") || noteLower.contains("কফি") || noteLower.contains("breakfast") || noteLower.contains("lunch") || noteLower.contains("dinner") || noteLower.contains("food")
        val hasGrocery = noteLower.contains("বাজার") || noteLower.contains("গ্রোসারী") || noteLower.contains("grocery")
        val hasTransport = noteLower.contains("গাড়ি") || noteLower.contains("রিকশা") || noteLower.contains("বাস") || noteLower.contains("ভাড়া") || noteLower.contains("ট্যাক্সি") || noteLower.contains("সিএনজি") || noteLower.contains("rent") || noteLower.contains("travel") || noteLower.contains("fare") || noteLower.contains("transport")
        val hasShopping = noteLower.contains("জামা") || noteLower.contains("কাপড়") || noteLower.contains("shopping") || noteLower.contains("কেনাকাটা")
        val hasMedical = noteLower.contains("চিকিৎসা") || noteLower.contains("ঔষধ") || noteLower.contains("ডাক্তার") || noteLower.contains("মেডিকেল") || noteLower.contains("medicine") || noteLower.contains("medical")
        val hasEducation = noteLower.contains("বই") || noteLower.contains("স্কুল") || noteLower.contains("কলেজ") || noteLower.contains("টিউশন") || noteLower.contains("শিক্ষা") || noteLower.contains("education")

        if (isSavingsExplicit) {
            type = "SAVINGS"
            category = "Savings"
        } else if (isWithdrawalExplicit) {
            type = "WITHDRAWAL"
            category = "Withdrawal"
        } else if (isIncomeExplicit || hasSalary || hasBusiness || hasGift || hasHonorarium || noteLower.contains("পেলাম")) {
            type = "INCOME"
            category = when {
                hasSalary -> "Salary"
                hasBusiness -> "Business"
                hasGift -> "Gift"
                hasHonorarium -> "Honorarium"
                else -> "Income"
            }
        } else if (isLendExplicit) {
            type = "LEND"
            category = "Lending"
        } else if (isBorrowExplicit) {
            type = "BORROW"
            category = "Borrowing"
        } else if (isExpenseExplicit || hasFood || hasGrocery || hasTransport || hasShopping || hasMedical || hasEducation) {
            type = "EXPENSE"
            category = when {
                hasFood -> "Food"
                hasGrocery -> "Grocery"
                hasTransport -> "Transportation"
                hasShopping -> "Shopping"
                hasMedical -> "Medical"
                hasEducation -> "Education"
                else -> "Expense"
            }
        } else {
            type = "EXPENSE"
            category = "Other"
        }

        var cleaned = note
        if (rawAmountStr.isNotEmpty()) {
            cleaned = cleaned.replaceFirst(rawAmountStr, "")
        }

        val wordsToStrip = listOf(
            "টাকা", "টাকায়", "টাকা.", "টাকা,", "টি", "টা", "tk", "taka",
            "ব্যয়", "খরচ", "আয়", "ইনকাম", "দেনা", "পাওনা", "সঞ্চয়", "উত্তোলন",
            "expense", "cost", "income", "borrow", "lend", "savings", "withdraw"
        )

        for (w in wordsToStrip) {
            val regex = Regex("(?i)\\b" + Regex.escape(w) + "\\b")
            cleaned = cleaned.replace(regex, "")
        }

        cleaned = cleaned.replace(Regex("\\s+"), " ").trim()

        if (cleaned.isBlank()) {
            val noteNoNum = if (rawAmountStr.isNotEmpty()) note.replaceFirst(rawAmountStr, "").trim().replace(Regex("\\s+"), " ") else note
            cleaned = if (noteNoNum.isNotBlank()) noteNoNum else (category ?: note)
        }

        return DraftParseResult(amount, type, category, cleaned)
    }
}

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

