package com.example.ui.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.example.data.Person
import com.example.data.SavingsGoal
import com.example.data.SavingsTransaction
import com.example.data.Transaction
import com.example.ui.AppLanguage

fun normalizeBn(str: String): String {
    return str.lowercase()
        .replace("\u09AF\u09BC", "\u09DF") // convert য় (Ja + Nukta) to য় (Ya)
        .replace("বাকী", "বাকি")
}

fun formatNumberByLanguage(num: Int, language: AppLanguage): String {
    if (language != AppLanguage.BN) return num.toString()
    val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    return num.toString().map { if (it in '0'..'9') bnDigits[it - '0'] else it }.joinToString("")
}

fun highlightMatch(text: String, query: String, highlightColor: Color): AnnotatedString {
    if (query.isBlank() || text.isBlank()) return AnnotatedString(text)

    val normalizedQuery = normalizeBn(query)
    val queryTokens = normalizedQuery.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (queryTokens.isEmpty()) return AnnotatedString(text)

    val normalizedText = normalizeBn(text)
    val textLower = text.lowercase()
    val ranges = mutableListOf<IntRange>()

    for (token in queryTokens) {
        var startIdx = 0
        while (startIdx < text.length) {
            var idx = normalizedText.indexOf(token, startIdx)
            if (idx == -1) {
                idx = textLower.indexOf(token, startIdx)
            }
            if (idx == -1) break
            ranges.add(idx until (idx + token.length))
            startIdx = idx + token.length
        }
    }

    if (ranges.isEmpty()) return AnnotatedString(text)

    ranges.sortBy { it.first }
    val mergedRanges = mutableListOf<IntRange>()
    for (r in ranges) {
        if (mergedRanges.isEmpty()) {
            mergedRanges.add(r)
        } else {
            val last = mergedRanges.last()
            if (r.first <= last.last + 1) {
                mergedRanges[mergedRanges.size - 1] = last.first..maxOf(last.last, r.last)
            } else {
                mergedRanges.add(r)
            }
        }
    }

    val highlightBg = highlightColor.copy(alpha = 0.25f)
    return buildAnnotatedString {
        var lastIdx = 0
        for (range in mergedRanges) {
            if (range.first > lastIdx) {
                append(text.substring(lastIdx, range.first))
            }
            pushStyle(SpanStyle(background = highlightBg, color = highlightColor, fontWeight = FontWeight.ExtraBold))
            append(text.substring(range.first, range.last + 1))
            pop()
            lastIdx = range.last + 1
        }
        if (lastIdx < text.length) {
            append(text.substring(lastIdx))
        }
    }
}

fun matchTransactionSearch(tx: Transaction, searchQuery: String, persons: List<Person>): Boolean {
    if (searchQuery.isBlank()) return true
    val normalizedQuery = normalizeBn(searchQuery)
    val queryTokens = normalizedQuery.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (queryTokens.isEmpty()) return true

    val person = persons.find { it.id == tx.personId }
    val personName = person?.name ?: ""
    val personPhone = person?.phone ?: ""
    val personAddress = person?.address ?: ""

    val catEn = tx.category.lowercase()
    val catBn = when (tx.category) {
        "Salary" -> "বেতন আয় আয়উৎস salary"
        "Business" -> "ব্যবসা বাণিজ্য বিজনেস business"
        "Agriculture" -> "কৃষি চাষাবাদ agriculture"
        "Gift" -> "উপহার হাদিয়া gift"
        "Sales" -> "বিক্রয় বিক্রি সেলস sale sales"
        "Honorarium" -> "সম্মানী honorarium"
        "Freelance" -> "ফ্রিল্যান্সিং freelance"
        "Rental" -> "ভাড়া বাসাভাড়া rental rent"
        "Investment" -> "বিনিয়োগ সঞ্চয় investment"
        "Food" -> "খাবার খাদ্য নাস্তা বাজার food"
        "Housing" -> "বাসস্থান বাসাভাড়া housing"
        "Bills" -> "বিল ইউটিলিটি bills bill"
        "Transport" -> "যাতায়াত পরিবহন travel transport"
        "Shopping" -> "কেনাকাটা বাজার ক্রয় কেনা শপিং shopping buy purchase"
        "Medical" -> "চিকিৎসা ওষুধ ডাক্তার medical"
        "Education" -> "শিক্ষা পড়াশোনা education"
        "Clothing" -> "পোশাক কাপড় clothing"
        "Entertainment" -> "বিনোদন entertainment"
        "Others" -> "অন্যান্য others"
        else -> tx.category
    }

    val subTypeUpper = tx.subType?.uppercase() ?: ""
    val typeUpper = tx.type.uppercase()

    val badgeAndSemanticText = buildString {
        // Credit terms
        if (subTypeUpper == "CREDIT") {
            append(" বাকি বাকী বাকীতে ধার credit baki ")
            if (typeUpper == "LEND" || tx.category.equals("Sales", ignoreCase = true)) {
                append(" বাকি বিক্রয় বাকী বিক্রয় বাকি বিক্রি বাকীতে বিক্রয় credit sale credit sales ")
            }
            if (typeUpper == "BORROW" || tx.category.equals("Shopping", ignoreCase = true) || typeUpper == "EXPENSE") {
                append(" বাকি ক্রয় বাকী ক্রয় বাকি কেনাকাটা বাকীতে ক্রয় credit purchase credit buy ")
            }
        } else if (subTypeUpper == "CASH") {
            append(" ক্যাশ নগদ cash ")
        } else if (subTypeUpper == "BANK") {
            append(" ব্যাংক bank ")
        } else if (subTypeUpper == "MOBILE_BANK") {
            append(" মোবাইল ব্যাংকিং বিকাশ নগদ রকেট mobile bank bkash nagad ")
        }

        // Type terms
        when {
            typeUpper == "BORROW" -> {
                append(" দেনা ধার ঋণ borrow debt payable ")
                if (subTypeUpper == "CREDIT") {
                    append(" বাকি ক্রয় বাকী ক্রয় ")
                }
            }
            typeUpper == "LEND" -> {
                append(" পাওনা lend receivable ")
                if (subTypeUpper == "CREDIT") {
                    append(" বাকি বিক্রয় বাকী বিক্রয় ")
                }
            }
            typeUpper == "REPAY_PAID" || tx.category.contains("দেনা পরিশোধ") || tx.category.equals("Repay Paid", ignoreCase = true) -> {
                append(" দেনা পরিশোধ দেনা ফেরত ঋণ পরিশোধ ধার শোধ repay paid debt repaid ")
            }
            typeUpper == "REPAY_RECEIVED" || tx.category.contains("পাওনা পরিশোধ") || tx.category.equals("Repay Received", ignoreCase = true) -> {
                append(" পাওনা পরিশোধ পাওনা ফেরত পাওনা আদায় loan repaid repay received ")
            }
            typeUpper == "EXPENSE" -> {
                append(" ব্যয় খরচ expense ")
            }
            typeUpper == "INCOME" -> {
                append(" আয় ইনকাম income ")
            }
        }
    }

    val noteStr = tx.note ?: ""
    val amountStr = tx.amount.toString()

    val rawCombined = "$personName $personPhone $personAddress $catEn $catBn $badgeAndSemanticText $noteStr $amountStr"
    val normalizedCombined = normalizeBn(rawCombined)

    return queryTokens.all { token ->
        normalizedCombined.contains(token) || rawCombined.lowercase().contains(token)
    }
}

fun matchPersonSearch(person: Person, searchQuery: String, netBalance: Double, transactions: List<Transaction>): Boolean {
    if (searchQuery.isBlank()) return true
    val normalizedQuery = normalizeBn(searchQuery)
    val queryTokens = normalizedQuery.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (queryTokens.isEmpty()) return true

    val name = person.name
    val phone = person.phone
    val address = person.address ?: ""
    val balanceStr = netBalance.toString()

    val balanceTerms = buildString {
        if (netBalance > 0) append(" পাওনা পাওনাটাকা পাওনাআদায় receivable ")
        else if (netBalance < 0) append(" দেনা দেনাটাকা ঋণ ধার payable debt ")
        else append(" হিসাবসমান settled ")
    }

    val personCombinedText = normalizeBn("$name $phone $address $balanceStr $balanceTerms")
    if (queryTokens.all { token -> personCombinedText.contains(token) }) {
        return true
    }

    // Check if person has any transaction matching the search query
    val personTxList = transactions.filter { it.personId == person.id }
    return personTxList.any { tx -> matchTransactionSearch(tx, searchQuery, listOf(person)) }
}

fun matchSavingsGoalSearch(goal: SavingsGoal, searchQuery: String): Boolean {
    if (searchQuery.isBlank()) return true
    val normalizedQuery = normalizeBn(searchQuery)
    val queryTokens = normalizedQuery.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (queryTokens.isEmpty()) return true

    val title = goal.title
    val target = goal.targetAmount.toString()
    val saved = goal.savedAmount.toString()
    val extraTerms = "সঞ্চয় সেভিংস ডিপিএস সঞ্চয় গোল কার্ড savings goal card deposit dps"

    val combined = normalizeBn("$title $target $saved $extraTerms")
    return queryTokens.all { token -> combined.contains(token) }
}

fun matchSavingsTransactionSearch(st: SavingsTransaction, goalTitle: String, searchQuery: String): Boolean {
    if (searchQuery.isBlank()) return true
    val normalizedQuery = normalizeBn(searchQuery)
    val queryTokens = normalizedQuery.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (queryTokens.isEmpty()) return true

    val note = st.note ?: ""
    val amount = st.amount.toString()
    val goal = goalTitle
    val typeTerm = if (st.isDeposit) "জমা জমাকৃত সঞ্চয় সেভিংস deposit" else "উত্তোলন তোলা উইথড্র withdraw withdrawal"
    val extraTerms = "সঞ্চয় সেভিংস ডিপিএস savings"

    val combined = normalizeBn("$note $amount $goal $typeTerm $extraTerms")
    return queryTokens.all { token -> combined.contains(token) }
}
