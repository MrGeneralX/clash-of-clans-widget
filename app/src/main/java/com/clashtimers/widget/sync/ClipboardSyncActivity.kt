package com.clashtimers.widget.sync

import android.app.Activity
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.clashtimers.widget.alarm.UpgradeAlarmScheduler
import com.clashtimers.widget.data.VillageExportParser
import com.clashtimers.widget.data.VillageRepository
import com.clashtimers.widget.widget.ClashWidgetProvider

class ClipboardSyncActivity : Activity() {

    private var hasProcessedClipboard = false
    private val timeoutHandler = Handler(Looper.getMainLooper())

    private val safetyTimeoutRunnable = Runnable {
        if (!hasProcessedClipboard) {
            hasProcessedClipboard = true
            closeSilently()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
        timeoutHandler.postDelayed(safetyTimeoutRunnable, 1500L)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !hasProcessedClipboard) {
            hasProcessedClipboard = true
            timeoutHandler.removeCallbacks(safetyTimeoutRunnable)
            window.decorView.post {
                processClipboardAndSync()
            }
        }
    }

    private fun processClipboardAndSync() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        if (clipboard == null || !clipboard.hasPrimaryClip()) {
            Toast.makeText(
                applicationContext,
                "Clipboard is empty. Copy your village data in Clash of Clans first!",
                Toast.LENGTH_SHORT
            ).show()
            closeSilently()
            return
        }

        val clipData = clipboard.primaryClip
        val description = clipData?.description
        val isTextClip = description?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true ||
                description?.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML) == true ||
                (clipData != null && clipData.itemCount > 0)

        if (!isTextClip || clipData == null || clipData.itemCount == 0) {
            Toast.makeText(
                applicationContext,
                "No text found in clipboard.",
                Toast.LENGTH_SHORT
            ).show()
            closeSilently()
            return
        }

        val rawClipboardText = clipData.getItemAt(0)
            ?.coerceToText(applicationContext)
            ?.toString()
            .orEmpty()

        val repository = VillageRepository(applicationContext)
        val playerName = repository.getCustomPlayerName()

        when (val result = VillageExportParser.parse(rawClipboardText, playerName)) {
            is VillageExportParser.ParseResult.Success -> {
                val villageState = result.state
                repository.saveVillageState(villageState)
                UpgradeAlarmScheduler.scheduleAllUpgradeAlarms(applicationContext, villageState)
                ClashWidgetProvider.updateAllWidgets(applicationContext)

                val activeCount = villageState.upgrades.size
                Toast.makeText(
                    applicationContext,
                    "Synced TH${villageState.townHallLevel} ($activeCount active upgrades)",
                    Toast.LENGTH_SHORT
                ).show()
            }

            is VillageExportParser.ParseResult.Error -> {
                Toast.makeText(
                    applicationContext,
                    result.message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        closeSilently()
    }

    private fun closeSilently() {
        finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    override fun onDestroy() {
        timeoutHandler.removeCallbacks(safetyTimeoutRunnable)
        super.onDestroy()
    }
}
