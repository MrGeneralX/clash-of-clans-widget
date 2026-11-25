package com.clashtimers.widget.data

import com.clashtimers.widget.model.ActiveUpgrade
import com.clashtimers.widget.model.HelperStatus
import com.clashtimers.widget.model.ParsedVillageState
import com.clashtimers.widget.model.UpgradeCategory
import org.json.JSONArray
import org.json.JSONObject

object VillageExportParser {

    sealed class ParseResult {
        data class Success(val state: ParsedVillageState) : ParseResult()
        data class Error(val message: String) : ParseResult()
    }

    fun parse(
        rawJson: String,
        playerName: String = "Chief",
        nowMillis: Long = System.currentTimeMillis()
    ): ParseResult {
        val trimmed = rawJson.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return ParseResult.Error("Clipboard does not contain valid Clash of Clans JSON.")
        }

        return try {
            val root = JSONObject(trimmed)
            if (!root.has("tag") || !root.has("timestamp")) {
                return ParseResult.Error("Missing 'tag' or 'timestamp'. Copy from Settings -> Data Export.")
            }

            val tag = root.optString("tag", "#UNKNOWN")
            val exportTimestampSeconds = root.optLong("timestamp", nowMillis / 1000L)
            val exportEpochMillis = exportTimestampSeconds * 1000L

            val buildings = root.optJSONArray("buildings")
            val townHallLevel = findBuildingLevel(buildings, 1000001, defaultLevel = 1)
            val builderHallLevel = findBuildingLevel(root.optJSONArray("buildings2"), 1000034, defaultLevel = 1)

            val homeBuilders = countBuilders(buildings)
            val upgrades = mutableListOf<ActiveUpgrade>()

            collectUpgrades(buildings, UpgradeCategory.BUILDER_HOME, exportEpochMillis, upgrades)
            collectUpgrades(root.optJSONArray("traps"), UpgradeCategory.BUILDER_HOME, exportEpochMillis, upgrades)
            collectUpgrades(root.optJSONArray("heroes"), UpgradeCategory.BUILDER_HOME, exportEpochMillis, upgrades)

            collectUpgrades(root.optJSONArray("units"), UpgradeCategory.LAB, exportEpochMillis, upgrades)
            collectUpgrades(root.optJSONArray("spells"), UpgradeCategory.LAB, exportEpochMillis, upgrades)
            collectUpgrades(root.optJSONArray("siege_machines"), UpgradeCategory.LAB, exportEpochMillis, upgrades)

            collectUpgrades(root.optJSONArray("pets"), UpgradeCategory.PETS, exportEpochMillis, upgrades)

            collectUpgrades(root.optJSONArray("buildings2"), UpgradeCategory.BUILDER_BASE, exportEpochMillis, upgrades)
            collectUpgrades(root.optJSONArray("traps2"), UpgradeCategory.BUILDER_BASE, exportEpochMillis, upgrades)
            collectUpgrades(root.optJSONArray("heroes2"), UpgradeCategory.BUILDER_BASE, exportEpochMillis, upgrades)
            collectUpgrades(root.optJSONArray("units2"), UpgradeCategory.STAR_LAB, exportEpochMillis, upgrades)

            val sortedUpgrades = upgrades.sortedBy { it.finishEpochMillis }
            val helpers = parseHelpers(root.optJSONArray("helpers"), exportEpochMillis)

            ParseResult.Success(
                ParsedVillageState(
                    playerTag = tag,
                    playerName = playerName,
                    townHallLevel = townHallLevel,
                    builderHallLevel = builderHallLevel,
                    exportTimestampSeconds = exportTimestampSeconds,
                    syncedAtMillis = nowMillis,
                    totalHomeBuilders = homeBuilders,
                    totalBuilderBaseBuilders = 2,
                    upgrades = sortedUpgrades,
                    helpers = helpers
                )
            )
        } catch (e: Exception) {
            ParseResult.Error("Failed to parse Village JSON: ${e.localizedMessage}")
        }
    }

    private fun collectUpgrades(
        array: JSONArray?,
        category: UpgradeCategory,
        exportEpochMillis: Long,
        outList: MutableList<ActiveUpgrade>
    ) {
        if (array == null) return
        for (i in 0 until array.length()) {
            val item = array.optJSONObject(i) ?: continue
            val timerSeconds = item.optLong("timer", 0L)
            if (timerSeconds <= 0L) continue

            val dataId = item.optInt("data", 0)
            val currentLvl = item.optInt("lvl", 0)
            val hasSupercharge = item.has("supercharge")
            val superchargeTier = item.optInt("supercharge", 0)

            val targetLvl = if (hasSupercharge && currentLvl > 0) {
                superchargeTier + 1
            } else {
                currentLvl + 1
            }

            val isGoblin = item.optBoolean("extra", false)
            val finishMillis = exportEpochMillis + (timerSeconds * 1000L)
            val name = CocGameDataRegistry.resolveName(dataId)
            val totalSeconds = CocGameDataRegistry.estimateTotalDurationSeconds(
                dataId = dataId,
                targetLevel = targetLvl,
                remainingSeconds = timerSeconds,
                category = category
            )

            outList.add(
                ActiveUpgrade(
                    id = "${category.name}_${dataId}_${outList.size}",
                    dataId = dataId,
                    name = name,
                    category = category,
                    currentLevel = currentLvl,
                    targetLevel = targetLvl,
                    isSupercharge = hasSupercharge,
                    isGoblin = isGoblin,
                    initialRemainingSeconds = timerSeconds,
                    totalDurationSeconds = totalSeconds,
                    finishEpochMillis = finishMillis
                )
            )
        }
    }

    private fun parseHelpers(array: JSONArray?, exportEpochMillis: Long): List<HelperStatus> {
        if (array == null) return emptyList()
        val result = mutableListOf<HelperStatus>()
        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val dataId = obj.optInt("data", 0)
            val lvl = obj.optInt("lvl", 0)
            val cooldownSeconds = obj.optLong("helper_cooldown", 0L)
            val cooldownFinish = if (cooldownSeconds > 0L) {
                exportEpochMillis + (cooldownSeconds * 1000L)
            } else {
                0L
            }
            result.add(
                HelperStatus(
                    dataId = dataId,
                    name = CocGameDataRegistry.resolveName(dataId),
                    level = lvl,
                    cooldownFinishMillis = cooldownFinish
                )
            )
        }
        return result
    }

    private fun findBuildingLevel(buildings: JSONArray?, targetDataId: Int, defaultLevel: Int): Int {
        if (buildings == null) return defaultLevel
        for (i in 0 until buildings.length()) {
            val item = buildings.optJSONObject(i) ?: continue
            if (item.optInt("data") == targetDataId) {
                return item.optInt("lvl", defaultLevel)
            }
        }
        return defaultLevel
    }

    private fun countBuilders(buildings: JSONArray?): Int {
        if (buildings == null) return 6
        var huts = 0
        var hasBob = false
        for (i in 0 until buildings.length()) {
            val item = buildings.optJSONObject(i) ?: continue
            when (item.optInt("data")) {
                1000015 -> huts += item.optInt("cnt", 1)
                1000065 -> hasBob = true
            }
        }
        val total = huts + if (hasBob) 1 else 0
        return if (total in 1..6) total else 6
    }
}
