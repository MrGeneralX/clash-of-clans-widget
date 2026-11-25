package com.clashtimers.widget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.clashtimers.widget.R
import com.clashtimers.widget.alarm.UpgradeAlarmScheduler
import com.clashtimers.widget.data.VillageRepository
import com.clashtimers.widget.sync.ClipboardSyncActivity
import com.clashtimers.widget.ui.MainActivity
import com.clashtimers.widget.util.TimeFormatUtils

class ClashWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateSingleWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_WIDGET_ITEM_CLICK) {
            val repository = VillageRepository(context)
            val updatedState = when (intent.getStringExtra(EXTRA_BOOST_ACTION)) {
                ACTION_BUILDER_POTION ->
                    repository.applyBoost(VillageRepository.BoostType.BUILDER_POTION)
                ACTION_BUILDER_BITE ->
                    repository.applyBoost(VillageRepository.BoostType.BUILDER_BITE)
                ACTION_UNDO_BUILDER_POTION ->
                    repository.undoBuilderBoostHour()
                ACTION_LAB_POTION ->
                    repository.applyBoost(VillageRepository.BoostType.RESEARCH_POTION)
                ACTION_PET_POTION ->
                    repository.applyBoost(VillageRepository.BoostType.PET_POTION)
                else -> null
            }

            if (updatedState != null) {
                UpgradeAlarmScheduler.scheduleAllUpgradeAlarms(context, updatedState)
                updateAllWidgets(context)
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_ITEM_CLICK = "com.clashtimers.widget.ACTION_WIDGET_ITEM_CLICK"
        const val EXTRA_BOOST_ACTION = "extra_boost_action"

        const val ACTION_BUILDER_POTION = "builder_potion"
        const val ACTION_BUILDER_BITE = "builder_bite"
        const val ACTION_UNDO_BUILDER_POTION = "undo_builder_potion"
        const val ACTION_LAB_POTION = "lab_potion"
        const val ACTION_PET_POTION = "pet_potion"

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, ClashWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_list_view)
            for (id in ids) {
                updateSingleWidget(context, manager, id)
            }
        }

        private fun updateSingleWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val repository = VillageRepository(context)
            val state = repository.loadVillageState()
            val views = RemoteViews(context.packageName, R.layout.widget_clash_timers)

            val playerName = state?.playerName ?: repository.getCustomPlayerName()
            val thText = if (state != null) "TH${state.townHallLevel}" else "TH--"
            views.setTextViewText(R.id.tv_player_name, playerName)
            views.setTextViewText(R.id.tv_th_badge, thText)

            if (state != null) {
                views.setTextViewText(R.id.tv_helpers_footer, repository.formatHelpersFooter(state))
                views.setTextViewText(R.id.tv_synced_ago, TimeFormatUtils.formatSyncedAgo(state.syncedAtMillis))
            } else {
                views.setTextViewText(R.id.tv_helpers_footer, "Tap '↻ Sync' after copying JSON")
                views.setTextViewText(R.id.tv_synced_ago, "never synced")
            }

            val syncIntent = Intent(context, ClipboardSyncActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val syncPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                syncIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.btn_sync, syncPendingIntent)

            val configIntent = Intent(context, MainActivity::class.java)
            val configPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId + 1000,
                configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tv_player_name, configPendingIntent)

            val serviceIntent = Intent(context, ClashWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_list_view, serviceIntent)
            views.setEmptyView(R.id.widget_list_view, R.id.empty_view)

            val clickIntent = Intent(context, ClashWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_ITEM_CLICK
            }
            val clickPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_list_view, clickPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
