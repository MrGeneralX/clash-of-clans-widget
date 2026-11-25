package com.clashtimers.widget.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.clashtimers.widget.model.ParsedVillageState

object UpgradeAlarmScheduler {

    fun scheduleAllUpgradeAlarms(context: Context, state: ParsedVillageState) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val nowMillis = System.currentTimeMillis()

        for (upgrade in state.upgrades) {
            if (upgrade.finishEpochMillis <= nowMillis) continue

            val intent = Intent(context, UpgradeAlarmReceiver::class.java).apply {
                putExtra(UpgradeAlarmReceiver.EXTRA_UPGRADE_NAME, upgrade.name)
                putExtra(UpgradeAlarmReceiver.EXTRA_TARGET_LEVEL, upgrade.targetLevel)
                putExtra(UpgradeAlarmReceiver.EXTRA_CATEGORY, upgrade.category.name)
            }

            val requestCode = upgrade.id.hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        upgrade.finishEpochMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        upgrade.finishEpochMillis,
                        pendingIntent
                    )
                }
            } catch (se: SecurityException) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, upgrade.finishEpochMillis, pendingIntent)
            }
        }
    }
}
