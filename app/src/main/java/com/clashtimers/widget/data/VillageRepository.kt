package com.clashtimers.widget.data

import android.content.Context
import android.content.SharedPreferences
import com.clashtimers.widget.model.ActiveUpgrade
import com.clashtimers.widget.model.HelperStatus
import com.clashtimers.widget.model.ParsedVillageState
import com.clashtimers.widget.model.UpgradeCategory
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max

class VillageRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    enum class BoostType(
        val multiplier: Int,
        val durationSeconds: Long,
        val targetCategory: UpgradeCategory
    ) {
        BUILDER_POTION(multiplier = 10, durationSeconds = 3_600L, targetCategory = UpgradeCategory.BUILDER_HOME),
        BUILDER_BITE(multiplier = 2, durationSeconds = 3_600L, targetCategory = UpgradeCategory.BUILDER_HOME),
        RESEARCH_POTION(multiplier = 24, durationSeconds = 3_600L, targetCategory = UpgradeCategory.LAB),
        STUDY_SOUP(multiplier = 4, durationSeconds = 3_600L, targetCategory = UpgradeCategory.LAB),
        PET_POTION(multiplier = 24, durationSeconds = 3_600L, targetCategory = UpgradeCategory.PETS)
    }

    fun saveVillageState(state: ParsedVillageState) {
        val root = JSONObject().apply {
            put("playerTag", state.playerTag)
            put("playerName", state.playerName)
            put("townHallLevel", state.townHallLevel)
            put("builderHallLevel", state.builderHallLevel)
            put("exportTimestampSeconds", state.exportTimestampSeconds)
            put("syncedAtMillis", state.syncedAtMillis)
            put("totalHomeBuilders", state.totalHomeBuilders)
            put("totalBuilderBaseBuilders", state.totalBuilderBaseBuilders)
            put("builderBoostEndMillis", state.builderBoostEndMillis)
            put("labBoostEndMillis", state.labBoostEndMillis)
            put("petBoostEndMillis", state.petBoostEndMillis)

            val upgradesArray = JSONArray()
            for (u in state.upgrades) {
                upgradesArray.put(
                    JSONObject().apply {
                        put("id", u.id)
                        put("dataId", u.dataId)
                        put("name", u.name)
                        put("category", u.category.name)
                        put("currentLevel", u.currentLevel)
                        put("targetLevel", u.targetLevel)
                        put("isSupercharge", u.isSupercharge)
                        put("isGoblin", u.isGoblin)
                        put("initialRemainingSeconds", u.initialRemainingSeconds)
                        put("totalDurationSeconds", u.totalDurationSeconds)
                        put("finishEpochMillis", u.finishEpochMillis)
                        put("helperAssigned", u.helperAssigned)
                    }
                )
            }
            put("upgrades", upgradesArray)

            val helpersArray = JSONArray()
            for (h in state.helpers) {
                helpersArray.put(
                    JSONObject().apply {
                        put("dataId", h.dataId)
                        put("name", h.name)
                        put("level", h.level)
                        put("cooldownFinishMillis", h.cooldownFinishMillis)
                    }
                )
            }
            put("helpers", helpersArray)
        }

        prefs.edit()
            .putString(KEY_VILLAGE_STATE_JSON, root.toString())
            .apply()
    }

    fun loadVillageState(): ParsedVillageState? {
        val raw = prefs.getString(KEY_VILLAGE_STATE_JSON, null) ?: return null
        return try {
            val root = JSONObject(raw)
            val upgradesArray = root.optJSONArray("upgrades") ?: JSONArray()
            val upgrades = mutableListOf<ActiveUpgrade>()

            for (i in 0 until upgradesArray.length()) {
                val obj = upgradesArray.optJSONObject(i) ?: continue
                upgrades.add(
                    ActiveUpgrade(
                        id = obj.getString("id"),
                        dataId = obj.getInt("dataId"),
                        name = obj.getString("name"),
                        category = UpgradeCategory.valueOf(obj.getString("category")),
                        currentLevel = obj.getInt("currentLevel"),
                        targetLevel = obj.getInt("targetLevel"),
                        isSupercharge = obj.optBoolean("isSupercharge", false),
                        isGoblin = obj.optBoolean("isGoblin", false),
                        initialRemainingSeconds = obj.getLong("initialRemainingSeconds"),
                        totalDurationSeconds = obj.getLong("totalDurationSeconds"),
                        finishEpochMillis = obj.getLong("finishEpochMillis"),
                        helperAssigned = obj.optBoolean("helperAssigned", false)
                    )
                )
            }

            val helpersArray = root.optJSONArray("helpers") ?: JSONArray()
            val helpers = mutableListOf<HelperStatus>()
            for (i in 0 until helpersArray.length()) {
                val obj = helpersArray.optJSONObject(i) ?: continue
                helpers.add(
                    HelperStatus(
                        dataId = obj.getInt("dataId"),
                        name = obj.getString("name"),
                        level = obj.getInt("level"),
                        cooldownFinishMillis = obj.getLong("cooldownFinishMillis")
                    )
                )
            }

            ParsedVillageState(
                playerTag = root.optString("playerTag", "#UNKNOWN"),
                playerName = root.optString("playerName", getCustomPlayerName()),
                townHallLevel = root.optInt("townHallLevel", 1),
                builderHallLevel = root.optInt("builderHallLevel", 1),
                exportTimestampSeconds = root.optLong("exportTimestampSeconds", 0L),
                syncedAtMillis = root.optLong("syncedAtMillis", 0L),
                totalHomeBuilders = root.optInt("totalHomeBuilders", 6),
                totalBuilderBaseBuilders = root.optInt("totalBuilderBaseBuilders", 2),
                upgrades = upgrades.sortedBy { it.finishEpochMillis },
                helpers = helpers,
                builderBoostEndMillis = root.optLong("builderBoostEndMillis", 0L),
                labBoostEndMillis = root.optLong("labBoostEndMillis", 0L),
                petBoostEndMillis = root.optLong("petBoostEndMillis", 0L)
            )
        } catch (e: Exception) {
            null
        }
    }

    fun applyBoost(boostType: BoostType, nowMillis: Long = System.currentTimeMillis()): ParsedVillageState? {
        val currentState = loadVillageState() ?: return null
        val boostDurationMillis = boostType.durationSeconds * 1000L
        val maxWorkCoveredSeconds = boostType.multiplier * boostType.durationSeconds

        val updatedUpgrades = currentState.upgrades.map { upgrade ->
            if (upgrade.category != boostType.targetCategory) {
                upgrade
            } else {
                val remainingSec = upgrade.remainingSecondsAt(nowMillis)
                if (remainingSec <= 0L) {
                    upgrade
                } else {
                    val savedSeconds = if (remainingSec >= maxWorkCoveredSeconds) {
                        (boostType.multiplier - 1L) * boostType.durationSeconds
                    } else {
                        (remainingSec * (boostType.multiplier - 1L)) / boostType.multiplier
                    }
                    upgrade.copy(
                        finishEpochMillis = upgrade.finishEpochMillis - (savedSeconds * 1000L)
                    )
                }
            }
        }

        val newBuilderBoostEnd = if (boostType.targetCategory == UpgradeCategory.BUILDER_HOME) {
            max(currentState.builderBoostEndMillis, nowMillis) + boostDurationMillis
        } else {
            currentState.builderBoostEndMillis
        }

        val newLabBoostEnd = if (boostType.targetCategory == UpgradeCategory.LAB) {
            max(currentState.labBoostEndMillis, nowMillis) + boostDurationMillis
        } else {
            currentState.labBoostEndMillis
        }

        val newPetBoostEnd = if (boostType.targetCategory == UpgradeCategory.PETS) {
            max(currentState.petBoostEndMillis, nowMillis) + boostDurationMillis
        } else {
            currentState.petBoostEndMillis
        }

        val updatedState = currentState.copy(
            upgrades = updatedUpgrades.sortedBy { it.finishEpochMillis },
            builderBoostEndMillis = newBuilderBoostEnd,
            labBoostEndMillis = newLabBoostEnd,
            petBoostEndMillis = newPetBoostEnd
        )
        saveVillageState(updatedState)
        return updatedState
    }

    fun undoBuilderBoostHour(nowMillis: Long = System.currentTimeMillis()): ParsedVillageState? {
        val currentState = loadVillageState() ?: return null
        if (currentState.builderBoostEndMillis <= nowMillis) return currentState

        val nineHoursMillis = 9L * 3_600_000L
        val oneHourMillis = 3_600_000L

        val revertedUpgrades = currentState.upgrades.map { upgrade ->
            if (upgrade.category == UpgradeCategory.BUILDER_HOME && upgrade.remainingSecondsAt(nowMillis) > 0L) {
                upgrade.copy(finishEpochMillis = upgrade.finishEpochMillis + nineHoursMillis)
            } else {
                upgrade
            }
        }

        val updatedState = currentState.copy(
            upgrades = revertedUpgrades.sortedBy { it.finishEpochMillis },
            builderBoostEndMillis = (currentState.builderBoostEndMillis - oneHourMillis).coerceAtLeast(nowMillis)
        )
        saveVillageState(updatedState)
        return updatedState
    }

    fun formatBuilderHeader(state: ParsedVillageState, nowMillis: Long = System.currentTimeMillis()): String {
        val activeHomeUpgrades = state.upgrades.filter {
            it.category == UpgradeCategory.BUILDER_HOME && it.remainingSecondsAt(nowMillis) > 0L
        }
        val hasGoblin = activeHomeUpgrades.any { it.isGoblin }
        val regularBusy = activeHomeUpgrades.count { !it.isGoblin }.coerceAtMost(state.totalHomeBuilders)

        return if (hasGoblin) {
            "BUILDERS $regularBusy/${state.totalHomeBuilders} + Goblin"
        } else {
            "BUILDERS $regularBusy/${state.totalHomeBuilders}"
        }
    }

    fun formatHelpersFooter(state: ParsedVillageState, nowMillis: Long = System.currentTimeMillis()): String {
        val apprentice = state.helpers.find { it.dataId == 93000000 }
        val assistant = state.helpers.find { it.dataId == 93000001 }

        val parts = mutableListOf<String>()
        if (apprentice != null) {
            val status = if (apprentice.isIdleAt(nowMillis)) "Idle" else "Cooldown"
            parts.add("Apprentice: $status")
        }
        if (assistant != null) {
            val activeLabName = state.upgrades.firstOrNull {
                it.category == UpgradeCategory.LAB && it.remainingSecondsAt(nowMillis) > 0L
            }?.name
            val status = when {
                assistant.isIdleAt(nowMillis) -> "Idle"
                activeLabName != null -> activeLabName
                else -> "Active"
            }
            parts.add("Assistant: $status")
        }
        return if (parts.isEmpty()) "Helpers: Ready" else parts.joinToString(" • ")
    }

    fun saveCustomPlayerName(name: String) {
        prefs.edit().putString(KEY_PLAYER_NAME, name.ifBlank { "Chief" }).apply()
        loadVillageState()?.let { current ->
            saveVillageState(current.copy(playerName = name.ifBlank { "Chief" }))
        }
    }

    fun getCustomPlayerName(): String {
        return prefs.getString(KEY_PLAYER_NAME, "Kuswa") ?: "Kuswa"
    }

    companion object {
        private const val PREFS_NAME = "clash_timers_widget_prefs"
        private const val KEY_VILLAGE_STATE_JSON = "village_state_json"
        private const val KEY_PLAYER_NAME = "custom_player_name"
    }
}
