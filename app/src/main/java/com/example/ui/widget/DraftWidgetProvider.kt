package com.example.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.R
import com.example.MainActivity
import com.example.ui.screens.DraftInputActivity

class DraftWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            "com.example.UPDATE_DRAFT_WIDGET" -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(
                    android.content.ComponentName(context, DraftWidgetProvider::class.java)
                )
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view)
            }
            "com.example.WIDGET_ITEM_ACTION" -> {
                val draftId = intent.getIntExtra("draft_id", -1)
                val actionType = intent.getStringExtra("action_type") ?: "edit"
                
                if (draftId != -1) {
                    when (actionType) {
                        "edit" -> {
                            val editIntent = Intent(context, DraftInputActivity::class.java).apply {
                                putExtra("draft_id", draftId)
                                putExtra("isEdit", true)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(editIntent)
                        }
                        "post" -> {
                            val postIntent = Intent(context, MainActivity::class.java).apply {
                                action = "ACTION_POST_DRAFT"
                                putExtra("EXTRA_TARGET_DRAFT_ID", draftId)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(postIntent)
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_draft)
            val config = WidgetConfigManager.loadConfig(context)

            // Apply configurations
            views.setInt(R.id.widget_header_bg, "setColorFilter", config.titleSectionBg)
            views.setInt(R.id.widget_list_bg, "setColorFilter", config.listSectionBg)
            
            views.setTextColor(R.id.widget_title, config.titleTextColor)
            views.setViewVisibility(R.id.widget_title, if (config.isTitleVisible) android.view.View.VISIBLE else android.view.View.GONE)
            
            views.setTextColor(R.id.widget_subtitle, config.subtitleTextColor)
            views.setViewVisibility(R.id.widget_subtitle, if (config.isSubtitleVisible) android.view.View.VISIBLE else android.view.View.GONE)
            
            views.setViewVisibility(R.id.widget_list_container, if (config.isListVisible) android.view.View.VISIBLE else android.view.View.GONE)

            // Button configs
            val buttonConfigs = listOf(
                R.id.widget_add_button to (R.id.widget_add_button_bg to R.id.widget_add_button_icon),
                R.id.widget_voice_button to (R.id.widget_voice_button_bg to R.id.widget_voice_button_icon),
                R.id.widget_customize_button to (R.id.widget_customize_button_bg to R.id.widget_customize_button_icon)
            )
            for ((containerId, pair) in buttonConfigs) {
                val (bgId, iconId) = pair
                views.setInt(bgId, "setColorFilter", config.buttonBgColor)
                views.setInt(iconId, "setColorFilter", config.buttonTintColor)
            }

            // Intent for add button (manual)
            val addIntent = Intent(context, DraftInputActivity::class.java).apply {
                putExtra("isVoice", false)
            }
            val addPendingIntent = PendingIntent.getActivity(
                context,
                0,
                addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_add_button, addPendingIntent)
            
            // Intent for voice button
            val voiceIntent = Intent(context, DraftInputActivity::class.java).apply {
                putExtra("isVoice", true)
            }
            val voicePendingIntent = PendingIntent.getActivity(
                context,
                10,
                voiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_voice_button, voicePendingIntent)

            // Intent for customize button
            val customizeIntent = Intent(context, WidgetCustomizationActivity::class.java)
            val customizePendingIntent = PendingIntent.getActivity(
                context,
                20,
                customizeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_customize_button, customizePendingIntent)

            // Intent for info button
            val infoIntent = Intent(context, DraftInputActivity::class.java).apply {
                putExtra("showInfoDialog", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val infoPendingIntent = PendingIntent.getActivity(
                context,
                30,
                infoIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_info_button, infoPendingIntent)
            
            // Service intent for ListView
            val serviceIntent = Intent(context, DraftWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_list_view, serviceIntent)
            
            // Template for ListView items - use Broadcast instead of direct Activity
            val templateIntent = Intent(context, DraftWidgetProvider::class.java).apply {
                action = "com.example.WIDGET_ITEM_ACTION"
            }
            val templatePendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                templateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_list_view, templatePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
