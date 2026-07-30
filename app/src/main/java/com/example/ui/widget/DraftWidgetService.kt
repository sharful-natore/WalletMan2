package com.example.ui.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.AppDatabase
import com.example.data.DraftTransaction
import kotlinx.coroutines.runBlocking

class DraftWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return DraftWidgetFactory(this.applicationContext)
    }
}

class DraftWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    private var draftsList: List<DraftTransaction> = emptyList()

    override fun onCreate() { }

    override fun onDataSetChanged() {
        runBlocking {
            try {
                val db = AppDatabase.getDatabase(context.applicationContext)
                val draftsDao = db.financeDao()
                draftsList = draftsDao.getAllDraftTransactionsList()
            } catch (e: Exception) {
                draftsList = emptyList()
            }
        }
    }

    override fun onDestroy() {
        draftsList = emptyList()
    }

    override fun getCount(): Int = if (draftsList.isEmpty()) 0 else draftsList.size + 1

    override fun getViewTypeCount(): Int = 2

    private fun parseDetails(note: String): Pair<Double?, String?> {
        val result = com.example.data.DraftParser.parse(note)
        return Pair(result.amount, result.type)
    }

    private fun toBanglaDigits(str: String): String {
        return str.replace("0", "০").replace("1", "১").replace("2", "২")
            .replace("3", "৩").replace("4", "৪").replace("5", "৫")
            .replace("6", "৬").replace("7", "৭").replace("8", "৮")
            .replace("9", "৯")
    }

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position > draftsList.size) return null
        val config = WidgetConfigManager.loadConfig(context)

        // Summary item
        if (position == draftsList.size) {
            val rv = RemoteViews(context.packageName, R.layout.widget_draft_summary_item)

            var totalIncome = 0.0
            var totalExpense = 0.0
            var totalLend = 0.0
            var totalBorrow = 0.0
            var totalSavings = 0.0
            var totalWithdrawal = 0.0

            for (d in draftsList) {
                val (inferredAmt, inferredType) = parseDetails(d.note)
                val amt = d.amount ?: inferredAmt ?: 0.0
                val t = d.type ?: inferredType ?: "EXPENSE"
                when (t) {
                    "INCOME" -> totalIncome += amt
                    "EXPENSE" -> totalExpense += amt
                    "LEND" -> totalLend += amt
                    "BORROW" -> totalBorrow += amt
                    "SAVINGS" -> totalSavings += amt
                    "WITHDRAWAL" -> totalWithdrawal += amt
                }
            }

            val netIncExp = totalIncome - totalExpense
            val incExpStr = if (netIncExp > 0) {
                "আয় ${toBanglaDigits(netIncExp.toInt().toString())}৳"
            } else if (netIncExp < 0) {
                "ব্যয় ${toBanglaDigits((-netIncExp).toInt().toString())}৳"
            } else if (totalIncome > 0 || totalExpense > 0) {
                "আয়/ব্যয় ০৳"
            } else null

            val netLendBorrow = totalLend - totalBorrow
            val lendBorrowStr = if (netLendBorrow > 0) {
                "পাওনা ${toBanglaDigits(netLendBorrow.toInt().toString())}৳"
            } else if (netLendBorrow < 0) {
                "দেনা ${toBanglaDigits((-netLendBorrow).toInt().toString())}৳"
            } else if (totalLend > 0 || totalBorrow > 0) {
                "দেনা/পাওনা ০৳"
            } else null

            val netSavWith = totalSavings - totalWithdrawal
            val savWithStr = if (netSavWith > 0) {
                "সঞ্চয় ${toBanglaDigits(netSavWith.toInt().toString())}৳"
            } else if (netSavWith < 0) {
                "উত্তোলন ${toBanglaDigits((-netSavWith).toInt().toString())}৳"
            } else if (totalSavings > 0 || totalWithdrawal > 0) {
                "সঞ্চয়/উত্তোলন ০৳"
            } else null

            val summaryParts = listOfNotNull(incExpStr, lendBorrowStr, savWithStr)
            val summaryText = if (summaryParts.isNotEmpty()) {
                summaryParts.joinToString("  |  ")
            } else {
                "মোট ${toBanglaDigits(draftsList.size.toString())} টি"
            }

            rv.setTextViewText(R.id.widget_summary_text, summaryText)
            rv.setTextColor(R.id.widget_summary_title, config.listItemTextColor)
            rv.setTextColor(R.id.widget_summary_text, config.listItemTextColor)
            val summaryAlpha = android.graphics.Color.alpha(config.listItemBg)
            val summaryOpaque = (config.listItemBg and 0x00FFFFFF) or -0x1000000
            rv.setInt(R.id.widget_summary_bg, "setColorFilter", summaryOpaque)
            rv.setInt(R.id.widget_summary_bg, "setImageAlpha", summaryAlpha)

            return rv
        }

        // Regular Draft Item
        val draft = draftsList[position]
        val rv = RemoteViews(context.packageName, R.layout.widget_draft_item)

        val (inferredAmt, inferredType) = parseDetails(draft.note)
        val finalAmt = draft.amount ?: inferredAmt
        val finalType = draft.type ?: inferredType ?: "EXPENSE"

        val amountStr = if (finalAmt != null) {
            " " + toBanglaDigits(finalAmt.toInt().toString()) + "৳"
        } else ""
        
        rv.setTextViewText(R.id.widget_item_text, "${draft.note}$amountStr")
        rv.setTextColor(R.id.widget_item_text, config.listItemTextColor)
        rv.setTextColor(R.id.widget_item_serial, config.listItemTextColor)
        val itemAlpha = android.graphics.Color.alpha(config.listItemBg)
        val itemOpaque = (config.listItemBg and 0x00FFFFFF) or -0x1000000
        rv.setInt(R.id.widget_item_bg, "setColorFilter", itemOpaque)
        rv.setInt(R.id.widget_item_bg, "setImageAlpha", itemAlpha)
        
        val serial = toBanglaDigits((position + 1).toString())
        rv.setTextViewText(R.id.widget_item_serial, "$serial.")

        // Tag text and color
        val (tagText, tagColor) = when (finalType) {
            "INCOME" -> "আয়" to android.graphics.Color.parseColor("#059669")
            "EXPENSE" -> "ব্যয়" to android.graphics.Color.parseColor("#DC2626")
            "LEND" -> "পাওনা" to android.graphics.Color.parseColor("#7C3AED")
            "BORROW" -> "দেনা" to android.graphics.Color.parseColor("#D97706")
            "SAVINGS" -> "সঞ্চয়" to android.graphics.Color.parseColor("#2563EB")
            "WITHDRAWAL" -> "উত্তোলন" to android.graphics.Color.parseColor("#0D9488")
            else -> "ব্যয়" to android.graphics.Color.parseColor("#DC2626")
        }
        rv.setTextViewText(R.id.widget_item_tag, tagText)
        rv.setTextColor(R.id.widget_item_tag, tagColor)

        // Intent for item click (Open Edit Dialog Activity)
        val editIntent = Intent().apply {
            putExtra("draft_id", draft.id)
            putExtra("action_type", "edit")
        }
        rv.setOnClickFillInIntent(R.id.widget_item_container, editIntent)
        
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getItemId(position: Int): Long = if (position < draftsList.size) draftsList[position].id.toLong() else 999999L
    override fun hasStableIds(): Boolean = false
}
