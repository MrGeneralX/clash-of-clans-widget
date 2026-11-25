package com.clashtimers.widget.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.clashtimers.widget.data.VillageRepository
import com.clashtimers.widget.widget.ClashWidgetProvider

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            val repository = VillageRepository(context)
            repository.loadVillageState()?.let { savedState ->
                UpgradeAlarmScheduler.scheduleAllUpgradeAlarms(context, savedState)
            }
            ClashWidgetProvider.updateAllWidgets(context)
        }
    }
}
