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

    override fun getCount(): Int = draftsList.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position >= draftsList.size) return null
        
        val draft = draftsList[position]
        val rv = RemoteViews(context.packageName, R.layout.widget_draft_item)
        val config = WidgetConfigManager.loadConfig(context)

        rv.setTextViewText(R.id.widget_item_text, draft.note)
        rv.setTextColor(R.id.widget_item_text, config.listItemTextColor)
        rv.setTextColor(R.id.widget_item_serial, config.listItemTextColor)
        rv.setInt(R.id.widget_item_bg, "setColorFilter", config.listItemBg)
        
        val serial = (position + 1).toString()
            .replace("0", "০").replace("1", "১").replace("2", "২")
            .replace("3", "৩").replace("4", "৪").replace("5", "৫")
            .replace("6", "৬").replace("7", "৭").replace("8", "৮")
            .replace("9", "৯")
        rv.setTextViewText(R.id.widget_item_serial, "$serial.")

        // Intent for item click (Open Edit Dialog Activity)
        val editIntent = Intent().apply {
            putExtra("draft_id", draft.id)
            putExtra("action_type", "edit")
        }
        rv.setOnClickFillInIntent(R.id.widget_item_container, editIntent)
        
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = draftsList[position].id.toLong()
    override fun hasStableIds(): Boolean = true
}
