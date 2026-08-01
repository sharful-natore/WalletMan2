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

private fun normalizeBn(str: String): String {
    return str.lowercase()
        .replace("য়", "য়")
        .replace("বাকী", "বাকি")
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
    val personName = normalizeBn(person?.name ?: "")
    val personPhone = normalizeBn(person?.phone ?: "")
    val personAddress = normalizeBn(person?.address ?: "")

    val catEn = tx.category.lowercase()
    val catBn = normalizeBn(when (tx.category) {
        "Salary" -> "বেতন আয় আয়উৎস বেতনভাতা salary"
        "Business" -> "ব্যবসা বাণিজ্য বিজনেস ব্যবসায়ী business"
        "Agriculture" -> "কৃষি চাষাবাদ কৃষিআয় কৃষিচাষ agriculture"
        "Gift" -> "উপহার হাদিয়া গিফট gift"
        "Sales" -> "বিক্রয় বিক্রি সেলস বিক্রয়লব্ধ পন্যবিক্রি sale sales"
        "Honorarium" -> "সম্মানী সম্মানীভাতা honorarium"
        "Freelance" -> "ফ্রিল্যান্সিং আউটসোর্সিং freelance"
        "Rental" -> "ভাড়া বাসাভাড়া ভাড়াআদায় rental rent"
        "Investment" -> "বিনিয়োগ ইনভেস্টমেন্ট সঞ্চয় investment"
        "Food" -> "খাবার খাদ্য রেস্তোরাঁ নাস্তা বাজার খাদ্যদ্রব্য রেস্তোরা খাবারখরচ food"
        "Housing" -> "বাসস্থান বাসাভাড়া ঘরভাড়া housing house"
        "Bills" -> "বিল ইউটিলিটি বিদ্যুৎ পানি গ্যাস বিল bills bill"
        "Transport" -> "যাতায়াত পরিবহন গাড়িভাড়া ভাড়া travel transport"
        "Shopping" -> "কেনাকাটা বাজার ক্রয় কেনা কেনাবেচা শপিং শপিংখরচ পোশাক কেনাকাটা shopping buy purchase"
        "Medical" -> "চিকিৎসা ওষুধ ডাক্তার হাসপাতাল চিকিৎসাখরচ medical doctor"
        "Education" -> "শিক্ষা পড়াশোনা স্কুল কলেজ টিউশন শিক্ষাখরচ education tuition"
        "Clothing" -> "পোশাক জামাকাপড় কাপড় পোশাকপরিচ্ছদ clothing clothes"
        "Entertainment" -> "বিনোদন বিনোদনখরচ সিনেমা ভ্রমণ entertainment"
        "Others" -> "অন্যান্য অন্যান্যখরচ others"
        else -> tx.category
    })

    val subTypeRaw = (tx.subType ?: "").lowercase()
    val subTypeTranslation = normalizeBn(when (tx.subType?.uppercase()) {
        "CREDIT" -> "ক্রেডিট বাকি ধার বাকীতে বাকীতেক্রয় বাকীতেবিক্রয় বাকীকিনে বাকীধার বাকিআদায় credit buy purchase sale"
        "CASH" -> "ক্যাশ নগদ ক্যাশটাকা cash"
        "BANK" -> "ব্যাংক ব্যাংকিং ব্যাংকট্রান্সফার bank transfer"
        "MOBILE_BANK" -> "মোবাইলব্যাংকিং বিকাশ নগদ রকেট উপায় mobile bank bkash nagad"
        else -> subTypeRaw
    })

    val typeRaw = tx.type.lowercase()
    val typeTranslation = normalizeBn(when (tx.type.uppercase()) {
        "INCOME" -> "আয় জমা ইনকাম পারিশ্রমিক আয়উৎস income deposit"
        "EXPENSE" -> "ব্যয় খরচ আউটগোয়িং কেনারখরচ কেনাকাটা খরচেরখাত expense expenditure buy purchase"
        "LEND" -> "পাওনা লেন্ড ধারদেওয়া ধারদিয়া পাওনাটাকা lend lent"
        "BORROW" -> "দেনা ধার ধারনেওয়া ধারনিওয়া ঋণ দেনাটাকা borrow borrowed debt"
        "REPAY_PAID" -> "দেনা পরিশোধ ঋণপরিশোধ ধারশোধ শোধ repay paid"
        "REPAY_RECEIVED" -> "পাওনা পরিশোধ পাওনাআদায় পাওনাফেরত পাওনাশোধ repay received"
        else -> typeRaw
    })

    val subTypeUpper = tx.subType?.uppercase() ?: ""
    val typeUpper = tx.type.uppercase()

    val isCreditPurchase = (subTypeUpper == "CREDIT" && (typeUpper in listOf("BORROW", "EXPENSE") || tx.category == "Shopping")) ||
            (typeUpper == "BORROW")
    val isCreditSale = (subTypeUpper == "CREDIT" && (typeUpper in listOf("LEND", "INCOME") || tx.category == "Sales")) ||
            (typeUpper == "LEND")

    val semanticPhrase = normalizeBn(buildString {
        if (isCreditPurchase) {
            append(" বাকি ক্রয় বাকি ক্রয় বাকি কেনাকাটা বাকি ক্রয়কৃত বাকীতে ক্রয় বাকীতে ক্রয় বাকীকেনাকাটা বাকীকিনে বাকীতেকেনাকাটা credit purchase credit buy credit shopping baki kroy baki kroy ")
        }
        if (isCreditSale) {
            append(" বাকি বিক্রয় বাকি বিক্রয় বাকীতে বিক্রয় বাকীতে বিক্রয় বাকীবিক্রি credit sale credit sales baki bikroy baki bikroy ")
        }
        if (subTypeUpper == "CREDIT") {
            append(" বাকি ধার বাকীতে credit baki ")
        }
        if (typeUpper == "EXPENSE" || tx.category == "Shopping") {
            append(" ক্রয় কেনাকাটা কেনা purchase buy kroy ")
        }
        if (typeUpper == "INCOME" || tx.category == "Sales") {
            append(" বিক্রয় বিক্রি sale sales bikroy ")
        }
    })

    val noteStr = normalizeBn(tx.note)
    val amountStr = tx.amount.toString()

    val combinedText = "$personName $personPhone $personAddress $catEn $catBn $subTypeRaw $subTypeTranslation $typeRaw $typeTranslation $semanticPhrase $noteStr $amountStr"

    return queryTokens.all { token -> combinedText.contains(token) }
}

fun matchPersonSearch(person: Person, searchQuery: String, netBalance: Double, transactions: List<Transaction>): Boolean {
    if (searchQuery.isBlank()) return true
    val normalizedQuery = normalizeBn(searchQuery)
    val queryTokens = normalizedQuery.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (queryTokens.isEmpty()) return true

    val name = normalizeBn(person.name)
    val phone = normalizeBn(person.phone)
    val address = normalizeBn(person.address ?: "")
    val balanceStr = netBalance.toString()

    val balanceTerms = normalizeBn(buildString {
        if (netBalance > 0) append(" পাওনা পাওনাটাকা পাওনাআদায় receivable ")
        else if (netBalance < 0) append(" দেনা দেনাটাকা ঋণ ধার payable debt ")
        else append(" হিসাবসমান settled ")
    })

    val personCombinedText = "$name $phone $address $balanceStr $balanceTerms"
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

    val title = normalizeBn(goal.title)
    val target = goal.targetAmount.toString()
    val saved = goal.savedAmount.toString()
    val extraTerms = normalizeBn("সঞ্চয় সেভিংস ডিপিএস সঞ্চয় গোল কার্ড savings goal card deposit dps")

    val combined = "$title $target $saved $extraTerms"
    return queryTokens.all { token -> combined.contains(token) }
}

fun matchSavingsTransactionSearch(st: SavingsTransaction, goalTitle: String, searchQuery: String): Boolean {
    if (searchQuery.isBlank()) return true
    val normalizedQuery = normalizeBn(searchQuery)
    val queryTokens = normalizedQuery.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (queryTokens.isEmpty()) return true

    val note = normalizeBn(st.note)
    val amount = st.amount.toString()
    val goal = normalizeBn(goalTitle)
    val typeTerm = normalizeBn(if (st.isDeposit) "জমা জমাকৃত সঞ্চয় সেভিংস deposit" else "উত্তোলন তোলা উইথড্র withdraw withdrawal")
    val extraTerms = normalizeBn("সঞ্চয় সেভিংস ডিপিএস savings")

    val combined = "$note $amount $goal $typeTerm $extraTerms"
    return queryTokens.all { token -> combined.contains(token) }
}
