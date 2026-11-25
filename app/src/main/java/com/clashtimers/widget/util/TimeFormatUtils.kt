package com.clashtimers.widget.util

import android.os.SystemClock
import kotlin.math.max

object TimeFormatUtils {

    fun toElapsedRealtimeBase(
        finishEpochMillis: Long,
        nowWallMillis: Long = System.currentTimeMillis(),
        nowElapsedRealtime: Long = SystemClock.elapsedRealtime()
    ): Long {
        val deltaMillis = finishEpochMillis - nowWallMillis
        return nowElapsedRealtime + deltaMillis
    }

    fun formatDetailedCountdown(remainingSeconds: Long): String {
        if (remainingSeconds <= 0L) return "Done!"

        val days = remainingSeconds / 86_400L
        val hours = (remainingSeconds % 86_400L) / 3_600L
        val minutes = (remainingSeconds % 3_600L) / 60L
        val seconds = remainingSeconds % 60L

        return when {
            days > 0L -> String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds)
            hours > 0L -> String.format("%d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun formatCompactDuration(remainingSeconds: Long): String {
        if (remainingSeconds <= 0L) return "Done"

        val days = remainingSeconds / 86_400L
        val hours = (remainingSeconds % 86_400L) / 3_600L
        val minutes = max(1L, (remainingSeconds % 3_600L) / 60L)

        return when {
            days > 0L && hours > 0L -> "${days}d ${hours}h"
            days > 0L -> "${days}d"
            hours > 0L && minutes > 0L -> "${hours}h ${minutes}m"
            hours > 0L -> "${hours}h"
            else -> "${minutes}m"
        }
    }

    fun formatSyncedAgo(
        syncedAtMillis: Long,
        nowMillis: Long = System.currentTimeMillis()
    ): String {
        if (syncedAtMillis <= 0L) return "never synced"
        val diffSeconds = ((nowMillis - syncedAtMillis) / 1000L).coerceAtLeast(0L)

        return when {
            diffSeconds < 60L -> "synced just now"
            diffSeconds < 3_600L -> "synced ${diffSeconds / 60L}m ago"
            diffSeconds < 86_400L -> "synced ${diffSeconds / 3_600L}h ago"
            else -> "synced ${diffSeconds / 86_400L}d ago"
        }
    }
}
