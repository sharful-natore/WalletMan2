package com.example.util

import java.util.regex.Pattern

data class ParsedFinancialSms(
    val amount: Double,
    val isIncome: Boolean,
    val provider: String,
    val category: String,
    val sender: String,
    val summaryNote: String,
    val timestamp: Long,
    val rawBody: String,
    val trxId: String? = null
)

object SmsParser {

    fun parse(sender: String?, body: String?, timestamp: Long = System.currentTimeMillis()): ParsedFinancialSms? {
        if (body.isNullOrBlank()) return null

        val text = body.trim()
        val senderClean = sender?.trim()?.uppercase() ?: ""

        val isFinancialSender = senderClean.contains("BKASH") ||
                senderClean.contains("NAGAD") ||
                senderClean.contains("ROCKET") ||
                senderClean.contains("UPAY") ||
                senderClean.contains("CELLFIN") ||
                senderClean.contains("TAP") ||
                senderClean.contains("CITYBANK") ||
                senderClean.contains("CITY") ||
                senderClean.contains("DBBL") ||
                senderClean.contains("EBL") ||
                senderClean.contains("BRAC") ||
                senderClean.contains("ISLAMI") ||
                senderClean.contains("PUBALI") ||
                senderClean.contains("SCB") ||
                senderClean.contains("HSBC") ||
                senderClean.contains("BANK") ||
                senderClean.contains("FINANCE")

        val lowerText = text.lowercase()
        val containsMoneyKeywords = lowerText.contains("tk") ||
                lowerText.contains("bdt") ||
                lowerText.contains("received") ||
                lowerText.contains("cash out") ||
                lowerText.contains("cash in") ||
                lowerText.contains("send money") ||
                lowerText.contains("recharge") ||
                lowerText.contains("credited") ||
                lowerText.contains("debited") ||
                lowerText.contains("paid") ||
                lowerText.contains("payment") ||
                lowerText.contains("fee") ||
                lowerText.contains("trxid") ||
                lowerText.contains("txnid")

        if (!isFinancialSender && !containsMoneyKeywords) {
            return null
        }

        if (senderClean.contains("BKASH") || lowerText.contains("bkash")) {
            return parseBkash(text, senderClean, timestamp)
        }

        if (senderClean.contains("NAGAD") || lowerText.contains("nagad")) {
            return parseNagad(text, senderClean, timestamp)
        }

        if (senderClean.contains("ROCKET") || senderClean.contains("UPAY") || senderClean.contains("CELLFIN") || senderClean.contains("TAP") || lowerText.contains("rocket") || lowerText.contains("upay")) {
            return parseMfs(text, senderClean, timestamp)
        }

        if (isFinancialSender || lowerText.contains("a/c") || lowerText.contains("account") || lowerText.contains("card")) {
            val bankResult = parseBankSms(text, senderClean, timestamp)
            if (bankResult != null) return bankResult
        }

        return parseGenericFinancialSms(text, senderClean, timestamp)
    }

    private fun parseBkash(text: String, sender: String, timestamp: Long): ParsedFinancialSms? {
        val amount = extractAmount(text) ?: return null
        val lower = text.lowercase()
        val trxId = extractTrxId(text)

        val isIncome = lower.contains("received") || lower.contains("cash in") || lower.contains("credited") || lower.contains("add money")
        val category = when {
            lower.contains("received") -> "bKash Received"
            lower.contains("cash out") -> "bKash Cash Out"
            lower.contains("payment") -> "bKash Payment"
            lower.contains("send money") -> "bKash Send Money"
            lower.contains("recharge") -> "Mobile Recharge"
            lower.contains("fee") -> "Bank Fee"
            isIncome -> "bKash Income"
            else -> "bKash Expense"
        }

        val actionDesc = when {
            lower.contains("received") -> "Received"
            lower.contains("cash out") -> "Cash Out"
            lower.contains("payment") -> "Payment"
            lower.contains("send money") -> "Send Money"
            lower.contains("recharge") -> "Mobile Recharge"
            isIncome -> "Received"
            else -> "Paid"
        }

        val note = "[SMS Auto] bKash $actionDesc Tk ${String.format("%.2f", amount)}${if (trxId != null) " (TrxID: $trxId)" else ""}"

        return ParsedFinancialSms(
            amount = amount,
            isIncome = isIncome,
            provider = "bKash",
            category = category,
            sender = if (sender.isNotBlank()) sender else "bKash",
            summaryNote = note,
            timestamp = timestamp,
            rawBody = text,
            trxId = trxId
        )
    }

    private fun parseNagad(text: String, sender: String, timestamp: Long): ParsedFinancialSms? {
        val amount = extractAmount(text) ?: return null
        val lower = text.lowercase()
        val trxId = extractTrxId(text)

        val isIncome = lower.contains("received") || lower.contains("cash in") || lower.contains("credited")
        val category = when {
            lower.contains("cash in") || lower.contains("received") -> "Nagad Income"
            lower.contains("cash out") -> "Nagad Cash Out"
            lower.contains("payment") -> "Nagad Payment"
            lower.contains("send money") -> "Nagad Send Money"
            lower.contains("recharge") -> "Mobile Recharge"
            isIncome -> "Nagad Income"
            else -> "Nagad Expense"
        }

        val actionDesc = when {
            lower.contains("cash in") || lower.contains("received") -> "Cash In/Received"
            lower.contains("cash out") -> "Cash Out"
            lower.contains("payment") -> "Payment"
            lower.contains("send money") -> "Send Money"
            lower.contains("recharge") -> "Mobile Recharge"
            isIncome -> "Received"
            else -> "Paid"
        }

        val note = "[SMS Auto] Nagad $actionDesc Tk ${String.format("%.2f", amount)}${if (trxId != null) " (TrxID: $trxId)" else ""}"

        return ParsedFinancialSms(
            amount = amount,
            isIncome = isIncome,
            provider = "Nagad",
            category = category,
            sender = if (sender.isNotBlank()) sender else "Nagad",
            summaryNote = note,
            timestamp = timestamp,
            rawBody = text,
            trxId = trxId
        )
    }

    private fun parseMfs(text: String, sender: String, timestamp: Long): ParsedFinancialSms? {
        val amount = extractAmount(text) ?: return null
        val lower = text.lowercase()
        val trxId = extractTrxId(text)

        val providerName = when {
            sender.contains("ROCKET") || lower.contains("rocket") -> "Rocket"
            sender.contains("UPAY") || lower.contains("upay") -> "Upay"
            sender.contains("CELLFIN") || lower.contains("cellfin") -> "CellFin"
            else -> if (sender.isNotBlank()) sender else "Mobile Banking"
        }

        val isIncome = lower.contains("received") || lower.contains("cash in") || lower.contains("credited") || lower.contains("added")
        val actionDesc = if (isIncome) "Received" else "Spent/Paid"
        val note = "[SMS Auto] $providerName $actionDesc Tk ${String.format("%.2f", amount)}${if (trxId != null) " (TrxID: $trxId)" else ""}"

        return ParsedFinancialSms(
            amount = amount,
            isIncome = isIncome,
            provider = providerName,
            category = "$providerName $actionDesc",
            sender = providerName,
            summaryNote = note,
            timestamp = timestamp,
            rawBody = text,
            trxId = trxId
        )
    }

    private fun parseBankSms(text: String, sender: String, timestamp: Long): ParsedFinancialSms? {
        val amount = extractAmount(text) ?: return null
        val lower = text.lowercase()
        val trxId = extractTrxId(text)

        val isIncome = lower.contains("credited") || lower.contains("deposited") || lower.contains("received")
        val isExpense = lower.contains("debited") || lower.contains("withdrawn") || lower.contains("paid") || lower.contains("purchased") || lower.contains("spent") || lower.contains("txn of")

        if (!isIncome && !isExpense) return null

        val bankName = when {
            sender.contains("CITY") -> "City Bank"
            sender.contains("DBBL") -> "Dutch-Bangla Bank"
            sender.contains("EBL") -> "Eastern Bank"
            sender.contains("BRAC") -> "BRAC Bank"
            sender.contains("ISLAMI") || sender.contains("IBBL") -> "Islami Bank"
            sender.contains("PUBALI") -> "Pubali Bank"
            sender.contains("SCB") -> "Standard Chartered"
            sender.contains("HSBC") -> "HSBC"
            sender.isNotBlank() -> sender
            else -> "Bank"
        }

        val actionDesc = if (isIncome) "Credited" else "Debited"
        val note = "[SMS Auto] $bankName $actionDesc BDT ${String.format("%.2f", amount)}${if (trxId != null) " (TrxID: $trxId)" else ""}"

        return ParsedFinancialSms(
            amount = amount,
            isIncome = isIncome,
            provider = bankName,
            category = if (isIncome) "Bank Credit" else "Bank Debit",
            sender = bankName,
            summaryNote = note,
            timestamp = timestamp,
            rawBody = text,
            trxId = trxId
        )
    }

    private fun parseGenericFinancialSms(text: String, sender: String, timestamp: Long): ParsedFinancialSms? {
        val amount = extractAmount(text) ?: return null
        val lower = text.lowercase()
        val trxId = extractTrxId(text)

        val isIncome = lower.contains("received") || lower.contains("credited") || lower.contains("cash in") || lower.contains("deposited")
        val isExpense = lower.contains("sent") || lower.contains("paid") || lower.contains("debited") || lower.contains("cash out") || lower.contains("spent") || lower.contains("recharge")

        if (!isIncome && !isExpense) return null

        val provider = if (sender.isNotBlank()) sender else "Financial SMS"
        val actionDesc = if (isIncome) "Received" else "Paid"
        val note = "[SMS Auto] $provider $actionDesc ৳${String.format("%.2f", amount)}${if (trxId != null) " (Ref: $trxId)" else ""}"

        return ParsedFinancialSms(
            amount = amount,
            isIncome = isIncome,
            provider = provider,
            category = if (isIncome) "SMS Income" else "SMS Expense",
            sender = provider,
            summaryNote = note,
            timestamp = timestamp,
            rawBody = text,
            trxId = trxId
        )
    }

    private fun extractAmount(text: String): Double? {
        val patterns = listOf(
            "(?i)(?:Tk|BDT|USD|Rs|\\$)\\.?\\s*([0-9,]+(?:\\.[0-9]{1,2})?)",
            "(?i)([0-9,]+(?:\\.[0-9]{1,2})?)\\s*(?:Tk|BDT|USD|Rs|\\$)"
        )

        for (patternStr in patterns) {
            val matcher = Pattern.compile(patternStr).matcher(text)
            if (matcher.find()) {
                val numStr = matcher.group(1)?.replace(",", "")
                val valDouble = numStr?.toDoubleOrNull()
                if (valDouble != null && valDouble > 0.0) {
                    return valDouble
                }
            }
        }
        return null
    }

    private fun extractTrxId(text: String): String? {
        val patterns = listOf(
            "(?i)(?:TrxID|TxnID|Txn ID|Trx ID|Ref|Reference)\\s*:?\\s*([A-Za-z0-9]+)",
            "(?i)ID\\s*:?\\s*([A-Za-z0-9]{8,12})"
        )
        for (pStr in patterns) {
            val matcher = Pattern.compile(pStr).matcher(text)
            if (matcher.find()) {
                val id = matcher.group(1)?.trim()
                if (!id.isNullOrBlank() && id.length >= 4) {
                    return id
                }
            }
        }
        return null
    }
}
