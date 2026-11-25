package com.clashtimers.widget.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.Notification
import com.clashtimers.widget.R
import com.clashtimers.widget.widget.ClashWidgetProvider

class UpgradeAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra(EXTRA_UPGRADE_NAME) ?: "Upgrade"
        val level = intent.getIntExtra(EXTRA_TARGET_LEVEL, 1)

        // 1. Refresh widget immediately so completed upgrade disappears from active list
        ClashWidgetProvider.updateAllWidgets(context)

        // 2. Post completion notification that opens Clash of Clans when tapped
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Upgrade Completion Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifies you the moment a Builder, Lab, or Pet upgrade finishes."
        }
        notificationManager.createNotificationChannel(channel)

        val launchGameIntent = context.packageManager
            .getLaunchIntentForPackage("com.supercell.clashofclans")
            ?: Intent()

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            name.hashCode(),
            launchGameIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("$name → Level $level Complete!")
            .setContentText("A builder or researcher is now free in your village.")
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        notificationManager.notify(name.hashCode(), notification)
    }

    companion object {
        const val CHANNEL_ID = "clash_upgrade_complete_channel"
        const val EXTRA_UPGRADE_NAME = "extra_upgrade_name"
        const val EXTRA_TARGET_LEVEL = "extra_target_level"
        const val EXTRA_CATEGORY = "extra_category"
    }
}
