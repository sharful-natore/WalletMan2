package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.FinanceRepository
import com.example.data.Transaction
import kotlinx.coroutines.flow.firstOrNull

object SmsSyncHelper {

    fun scanSmsInbox(context: Context, daysBack: Int = 30): List<ParsedFinancialSms> {
        val parsedList = mutableListOf<ParsedFinancialSms>()
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_SMS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return parsedList
        }

        val minTimestamp = System.currentTimeMillis() - (daysBack.toLong() * 24 * 60 * 60 * 1000L)
        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("address", "body", "date")
        val selection = "date >= ?"
        val selectionArgs = arrayOf(minTimestamp.toString())
        val sortOrder = "date DESC"

        try {
            val cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
            cursor?.use {
                val addressIdx = it.getColumnIndex("address")
                val bodyIdx = it.getColumnIndex("body")
                val dateIdx = it.getColumnIndex("date")

                while (it.moveToNext()) {
                    val address = if (addressIdx != -1) it.getString(addressIdx) else null
                    val body = if (bodyIdx != -1) it.getString(bodyIdx) else null
                    val date = if (dateIdx != -1) it.getLong(dateIdx) else System.currentTimeMillis()

                    val parsed = SmsParser.parse(address, body, date)
                    if (parsed != null) {
                        parsedList.add(parsed)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return parsedList
    }

    suspend fun syncSmsInboxToDatabase(
        context: Context,
        repository: FinanceRepository,
        workspaceId: String,
        daysBack: Int = 30
    ): Int {
        val parsedSmsList = scanSmsInbox(context, daysBack)
        if (parsedSmsList.isEmpty()) return 0

        val existingList = repository.allTransactions.firstOrNull() ?: emptyList()
        val existingNotes = existingList.map { it.note }.toSet()
        val existingTrxIds = existingList.mapNotNull { tx ->
            val regex = Regex("TrxID:\\s*([A-Za-z0-9]+)")
            regex.find(tx.note)?.groupValues?.get(1)
        }.toSet()

        var insertedCount = 0
        for (parsed in parsedSmsList) {
            if (existingNotes.contains(parsed.summaryNote)) continue
            if (parsed.trxId != null && existingTrxIds.contains(parsed.trxId)) continue

            val transaction = Transaction(
                amount = parsed.amount,
                type = if (parsed.isIncome) "INCOME" else "EXPENSE",
                category = parsed.category,
                timestamp = parsed.timestamp,
                note = parsed.summaryNote,
                workspaceId = workspaceId,
                subType = if (parsed.provider.contains("Bank", ignoreCase = true)) "BANK" else "MOBILE_MONEY"
            )

            repository.insertTransaction(transaction)
            insertedCount++
        }

        return insertedCount
    }
}
