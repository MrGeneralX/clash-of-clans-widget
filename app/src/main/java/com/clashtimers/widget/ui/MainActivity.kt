package com.clashtimers.widget.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.clashtimers.widget.R
import com.clashtimers.widget.alarm.UpgradeAlarmScheduler
import com.clashtimers.widget.data.VillageExportParser
import com.clashtimers.widget.data.VillageRepository
import com.clashtimers.widget.sync.ClipboardSyncActivity
import com.clashtimers.widget.widget.ClashWidgetProvider

class MainActivity : Activity() {

    private lateinit var repository: VillageRepository
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = VillageRepository(this)
        requestNotificationPermissionIfNeeded()

        val etPlayerName = findViewById<EditText>(R.id.et_player_name)
        val btnSaveName = findViewById<Button>(R.id.btn_save_name)
        val btnSyncClipboard = findViewById<Button>(R.id.btn_sync_clipboard_now)
        val btnLoadDemo = findViewById<Button>(R.id.btn_load_demo_data)
        val etManualJson = findViewById<EditText>(R.id.et_manual_json)
        val btnParseManual = findViewById<Button>(R.id.btn_parse_manual_json)
        tvStatus = findViewById(R.id.tv_status_summary)

        etPlayerName.setText(repository.getCustomPlayerName())
        refreshStatusSummary()

        btnSaveName.setOnClickListener {
            val newName = etPlayerName.text.toString().trim().ifBlank { "Chief" }
            repository.saveCustomPlayerName(newName)
            ClashWidgetProvider.updateAllWidgets(this)
            refreshStatusSummary()
            Toast.makeText(this, "Display name saved: $newName", Toast.LENGTH_SHORT).show()
        }

        btnSyncClipboard.setOnClickListener {
            startActivity(Intent(this, ClipboardSyncActivity::class.java))
        }

        btnLoadDemo.setOnClickListener {
            loadSampleTh18Village()
        }

        btnParseManual.setOnClickListener {
            val raw = etManualJson.text.toString()
            parseAndSaveJson(raw)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatusSummary()
    }

    private fun parseAndSaveJson(rawJson: String) {
        val playerName = repository.getCustomPlayerName()
        when (val res = VillageExportParser.parse(rawJson, playerName)) {
            is VillageExportParser.ParseResult.Success -> {
                repository.saveVillageState(res.state)
                UpgradeAlarmScheduler.scheduleAllUpgradeAlarms(this, res.state)
                ClashWidgetProvider.updateAllWidgets(this)
                refreshStatusSummary()
                Toast.makeText(
                    this,
                    "Synced TH${res.state.townHallLevel} (${res.state.upgrades.size} upgrades)!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            is VillageExportParser.ParseResult.Error -> {
                Toast.makeText(this, res.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadSampleTh18Village() {
        val nowSec = System.currentTimeMillis() / 1000L
        val sampleJson = """
        {
          "tag": "#2PP99LQ0",
          "timestamp": $nowSec,
          "buildings": [
            {"data": 1000001, "lvl": 18},
            {"data": 1000015, "lvl": 7, "cnt": 5},
            {"data": 1000065, "lvl": 1},
            {"data": 1000009, "lvl": 20, "timer": 7698, "extra": true},
            {"data": 1000067, "lvl": 5, "timer": 8517, "supercharge": 1},
            {"data": 1000012, "lvl": 13, "timer": 355967},
            {"data": 1000012, "lvl": 13, "timer": 410266},
            {"data": 1000012, "lvl": 13, "timer": 472465}
          ],
          "heroes": [
            {"data": 28000004, "lvl": 50, "timer": 85964},
            {"data": 28000000, "lvl": 101, "timer": 194087}
          ],
          "units": [
            {"data": 4000023, "lvl": 10, "timer": 282845}
          ],
          "pets": [
            {"data": 73000009, "lvl": 11, "timer": 307530}
          ],
          "helpers": [
            {"data": 93000000, "lvl": 8, "helper_cooldown": 0},
            {"data": 93000001, "lvl": 4, "helper_cooldown": 42000}
          ]
        }
        """.trimIndent()
        parseAndSaveJson(sampleJson)
    }

    private fun refreshStatusSummary() {
        val state = repository.loadVillageState()
        tvStatus.text = if (state == null) {
            "Status: No village synced yet. Tap 'Load Sample TH18 Village' above to preview the widget immediately!"
        } else {
            "Status: Synced ${state.playerName} (TH${state.townHallLevel}, ${state.playerTag}) • ${state.upgrades.size} active upgrades."
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}
