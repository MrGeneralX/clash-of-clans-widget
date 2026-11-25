package com.clashtimers.widget.model

enum class UpgradeCategory {
    BUILDER_HOME,
    BUILDER_BASE,
    LAB,
    PETS,
    STAR_LAB
}

data class ActiveUpgrade(
    val id: String,
    val dataId: Int,
    val name: String,
    val category: UpgradeCategory,
    val currentLevel: Int,
    val targetLevel: Int,
    val isSupercharge: Boolean = false,
    val isGoblin: Boolean = false,
    val initialRemainingSeconds: Long,
    val totalDurationSeconds: Long,
    val finishEpochMillis: Long,
    val helperAssigned: Boolean = false
) {
    fun remainingSecondsAt(nowMillis: Long = System.currentTimeMillis()): Long {
        return ((finishEpochMillis - nowMillis) / 1000L).coerceAtLeast(0L)
    }

    fun progressPercentAt(nowMillis: Long = System.currentTimeMillis()): Int {
        if (totalDurationSeconds <= 0L) return 50
        val remaining = remainingSecondsAt(nowMillis)
        val elapsed = (totalDurationSeconds - remaining).coerceIn(0L, totalDurationSeconds)
        return ((elapsed * 100L) / totalDurationSeconds).toInt().coerceIn(0, 100)
    }
}

data class HelperStatus(
    val dataId: Int,
    val name: String,
    val level: Int,
    val cooldownFinishMillis: Long
) {
    fun isIdleAt(nowMillis: Long = System.currentTimeMillis()): Boolean {
        return nowMillis >= cooldownFinishMillis
    }
}

data class ParsedVillageState(
    val playerTag: String,
    val playerName: String = "Chief",
    val townHallLevel: Int,
    val builderHallLevel: Int,
    val exportTimestampSeconds: Long,
    val syncedAtMillis: Long,
    val totalHomeBuilders: Int,
    val totalBuilderBaseBuilders: Int,
    val upgrades: List<ActiveUpgrade>,
    val helpers: List<HelperStatus>,
    val builderBoostEndMillis: Long = 0L,
    val labBoostEndMillis: Long = 0L,
    val petBoostEndMillis: Long = 0L
)
