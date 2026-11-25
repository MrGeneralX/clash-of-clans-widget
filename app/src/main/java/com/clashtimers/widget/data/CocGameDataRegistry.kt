package com.clashtimers.widget.data

import com.clashtimers.widget.model.UpgradeCategory
import kotlin.math.max

object CocGameDataRegistry {

    data class ItemMetadata(
        val name: String,
        val defaultCategory: UpgradeCategory,
        val maxRegularLevel: Int = 99
    )

    private val registry: Map<Int, ItemMetadata> = mapOf(
        1000001 to ItemMetadata("Town Hall", UpgradeCategory.BUILDER_HOME, 18),
        1000000 to ItemMetadata("Army Camp", UpgradeCategory.BUILDER_HOME, 14),
        1000002 to ItemMetadata("Elixir Collector", UpgradeCategory.BUILDER_HOME, 17),
        1000003 to ItemMetadata("Elixir Storage", UpgradeCategory.BUILDER_HOME, 18),
        1000004 to ItemMetadata("Gold Mine", UpgradeCategory.BUILDER_HOME, 17),
        1000005 to ItemMetadata("Gold Storage", UpgradeCategory.BUILDER_HOME, 18),
        1000006 to ItemMetadata("Barracks", UpgradeCategory.BUILDER_HOME, 18),
        1000007 to ItemMetadata("Laboratory", UpgradeCategory.BUILDER_HOME, 15),
        1000008 to ItemMetadata("Cannon", UpgradeCategory.BUILDER_HOME, 21),
        1000009 to ItemMetadata("Archer Tower", UpgradeCategory.BUILDER_HOME, 21),
        1000010 to ItemMetadata("Wall", UpgradeCategory.BUILDER_HOME, 18),
        1000011 to ItemMetadata("Wizard Tower", UpgradeCategory.BUILDER_HOME, 17),
        1000012 to ItemMetadata("Air Defense", UpgradeCategory.BUILDER_HOME, 15),
        1000013 to ItemMetadata("Mortar", UpgradeCategory.BUILDER_HOME, 17),
        1000014 to ItemMetadata("Clan Castle", UpgradeCategory.BUILDER_HOME, 13),
        1000015 to ItemMetadata("Builder's Hut", UpgradeCategory.BUILDER_HOME, 7),
        1000019 to ItemMetadata("Hidden Tesla", UpgradeCategory.BUILDER_HOME, 16),
        1000020 to ItemMetadata("Spell Factory", UpgradeCategory.BUILDER_HOME, 8),
        1000021 to ItemMetadata("X-Bow", UpgradeCategory.BUILDER_HOME, 12),
        1000023 to ItemMetadata("Dark Elixir Drill", UpgradeCategory.BUILDER_HOME, 11),
        1000024 to ItemMetadata("Dark Elixir Storage", UpgradeCategory.BUILDER_HOME, 12),
        1000026 to ItemMetadata("Dark Barracks", UpgradeCategory.BUILDER_HOME, 12),
        1000027 to ItemMetadata("Inferno Tower", UpgradeCategory.BUILDER_HOME, 11),
        1000028 to ItemMetadata("Air Sweeper", UpgradeCategory.BUILDER_HOME, 7),
        1000029 to ItemMetadata("Dark Spell Factory", UpgradeCategory.BUILDER_HOME, 7),
        1000031 to ItemMetadata("Eagle Artillery", UpgradeCategory.BUILDER_HOME, 7),
        1000032 to ItemMetadata("Bomb Tower", UpgradeCategory.BUILDER_HOME, 12),
        1000059 to ItemMetadata("Workshop", UpgradeCategory.BUILDER_HOME, 8),
        1000067 to ItemMetadata("Scattershot", UpgradeCategory.BUILDER_HOME, 6),
        1000068 to ItemMetadata("Pet House", UpgradeCategory.BUILDER_HOME, 11),
        1000070 to ItemMetadata("Blacksmith", UpgradeCategory.BUILDER_HOME, 9),
        1000071 to ItemMetadata("Hero Hall", UpgradeCategory.BUILDER_HOME, 11),
        1000072 to ItemMetadata("Spell Tower", UpgradeCategory.BUILDER_HOME, 4),
        1000077 to ItemMetadata("Monolith", UpgradeCategory.BUILDER_HOME, 4),
        1000079 to ItemMetadata("Multi-Gear Tower", UpgradeCategory.BUILDER_HOME, 3),
        1000084 to ItemMetadata("Multi-Archer Tower", UpgradeCategory.BUILDER_HOME, 4),
        1000085 to ItemMetadata("Ricochet Cannon", UpgradeCategory.BUILDER_HOME, 4),
        1000089 to ItemMetadata("Firespitter", UpgradeCategory.BUILDER_HOME, 3),
        1000093 to ItemMetadata("Revenge Tower", UpgradeCategory.BUILDER_HOME, 3),

        1000034 to ItemMetadata("Builder Hall", UpgradeCategory.BUILDER_BASE, 10),
        1000033 to ItemMetadata("Star Laboratory", UpgradeCategory.BUILDER_BASE, 10),
        1000035 to ItemMetadata("Elixir Collector (BB)", UpgradeCategory.BUILDER_BASE, 10),
        1000036 to ItemMetadata("Elixir Storage (BB)", UpgradeCategory.BUILDER_BASE, 10),
        1000037 to ItemMetadata("Gold Mine (BB)", UpgradeCategory.BUILDER_BASE, 10),
        1000038 to ItemMetadata("Gold Storage (BB)", UpgradeCategory.BUILDER_BASE, 10),
        1000039 to ItemMetadata("Clock Tower", UpgradeCategory.BUILDER_BASE, 10),
        1000040 to ItemMetadata("Cannon (BB)", UpgradeCategory.BUILDER_BASE, 10),
        1000041 to ItemMetadata("Double Cannon", UpgradeCategory.BUILDER_BASE, 10),
        1000044 to ItemMetadata("Archer Tower (BB)", UpgradeCategory.BUILDER_BASE, 10),
        1000045 to ItemMetadata("Firecrackers", UpgradeCategory.BUILDER_BASE, 10),
        1000046 to ItemMetadata("Crusher", UpgradeCategory.BUILDER_BASE, 10),
        1000048 to ItemMetadata("Guard Post", UpgradeCategory.BUILDER_BASE, 10),
        1000050 to ItemMetadata("Multi Mortar", UpgradeCategory.BUILDER_BASE, 10),
        1000051 to ItemMetadata("Roaster", UpgradeCategory.BUILDER_BASE, 10),
        1000052 to ItemMetadata("Giant Cannon", UpgradeCategory.BUILDER_BASE, 10),
        1000053 to ItemMetadata("Mega Tesla", UpgradeCategory.BUILDER_BASE, 10),
        1000058 to ItemMetadata("Lava Launcher", UpgradeCategory.BUILDER_BASE, 10),
        1000078 to ItemMetadata("X-Bow (BB)", UpgradeCategory.BUILDER_BASE, 10),

        12000000 to ItemMetadata("Bomb", UpgradeCategory.BUILDER_HOME, 13),
        12000001 to ItemMetadata("Spring Trap", UpgradeCategory.BUILDER_HOME, 12),
        12000002 to ItemMetadata("Giant Bomb", UpgradeCategory.BUILDER_HOME, 11),
        12000005 to ItemMetadata("Air Bomb", UpgradeCategory.BUILDER_HOME, 12),
        12000006 to ItemMetadata("Seeking Air Mine", UpgradeCategory.BUILDER_HOME, 7),
        12000008 to ItemMetadata("Skeleton Trap", UpgradeCategory.BUILDER_HOME, 5),
        12000016 to ItemMetadata("Tornado Trap", UpgradeCategory.BUILDER_HOME, 4),
        12000020 to ItemMetadata("Giga Bomb", UpgradeCategory.BUILDER_HOME, 4),

        28000000 to ItemMetadata("Barbarian King", UpgradeCategory.BUILDER_HOME, 105),
        28000001 to ItemMetadata("Archer Queen", UpgradeCategory.BUILDER_HOME, 105),
        28000002 to ItemMetadata("Grand Warden", UpgradeCategory.BUILDER_HOME, 80),
        28000003 to ItemMetadata("Battle Machine", UpgradeCategory.BUILDER_BASE, 35),
        28000004 to ItemMetadata("Royal Champion", UpgradeCategory.BUILDER_HOME, 55),
        28000005 to ItemMetadata("Battle Copter", UpgradeCategory.BUILDER_BASE, 35),
        28000006 to ItemMetadata("Minion Prince", UpgradeCategory.BUILDER_HOME, 95),
        28000007 to ItemMetadata("Dragon Duke", UpgradeCategory.BUILDER_HOME, 30),

        4000000 to ItemMetadata("Barbarian", UpgradeCategory.LAB),
        4000001 to ItemMetadata("Archer", UpgradeCategory.LAB),
        4000002 to ItemMetadata("Goblin", UpgradeCategory.LAB),
        4000003 to ItemMetadata("Giant", UpgradeCategory.LAB),
        4000004 to ItemMetadata("Wall Breaker", UpgradeCategory.LAB),
        4000005 to ItemMetadata("Balloon", UpgradeCategory.LAB),
        4000006 to ItemMetadata("Wizard", UpgradeCategory.LAB),
        4000007 to ItemMetadata("Healer", UpgradeCategory.LAB),
        4000008 to ItemMetadata("Dragon", UpgradeCategory.LAB),
        4000009 to ItemMetadata("P.E.K.K.A", UpgradeCategory.LAB),
        4000010 to ItemMetadata("Minion", UpgradeCategory.LAB),
        4000011 to ItemMetadata("Hog Rider", UpgradeCategory.LAB),
        4000012 to ItemMetadata("Valkyrie", UpgradeCategory.LAB),
        4000013 to ItemMetadata("Golem", UpgradeCategory.LAB),
        4000015 to ItemMetadata("Witch", UpgradeCategory.LAB),
        4000017 to ItemMetadata("Lava Hound", UpgradeCategory.LAB),
        4000022 to ItemMetadata("Bowler", UpgradeCategory.LAB),
        4000023 to ItemMetadata("Baby Dragon", UpgradeCategory.LAB),
        4000024 to ItemMetadata("Miner", UpgradeCategory.LAB),
        4000051 to ItemMetadata("Wall Wrecker", UpgradeCategory.LAB),
        4000052 to ItemMetadata("Battle Blimp", UpgradeCategory.LAB),
        4000053 to ItemMetadata("Yeti", UpgradeCategory.LAB),
        4000058 to ItemMetadata("Ice Golem", UpgradeCategory.LAB),
        4000059 to ItemMetadata("Electro Dragon", UpgradeCategory.LAB),
        4000062 to ItemMetadata("Stone Slammer", UpgradeCategory.LAB),
        4000065 to ItemMetadata("Dragon Rider", UpgradeCategory.LAB),
        4000075 to ItemMetadata("Siege Barracks", UpgradeCategory.LAB),
        4000082 to ItemMetadata("Headhunter", UpgradeCategory.LAB),
        4000087 to ItemMetadata("Log Launcher", UpgradeCategory.LAB),
        4000091 to ItemMetadata("Flame Flinger", UpgradeCategory.LAB),
        4000092 to ItemMetadata("Battle Drill", UpgradeCategory.LAB),
        4000095 to ItemMetadata("Electro Titan", UpgradeCategory.LAB),
        4000097 to ItemMetadata("Apprentice Warden", UpgradeCategory.LAB),
        4000110 to ItemMetadata("Root Rider", UpgradeCategory.LAB),
        4000123 to ItemMetadata("Thrower", UpgradeCategory.LAB),
        4000135 to ItemMetadata("Troop Launcher", UpgradeCategory.LAB),
        4000150 to ItemMetadata("Ruin Witch", UpgradeCategory.LAB),

        26000000 to ItemMetadata("Lightning Spell", UpgradeCategory.LAB),
        26000001 to ItemMetadata("Healing Spell", UpgradeCategory.LAB),
        26000002 to ItemMetadata("Rage Spell", UpgradeCategory.LAB),
        26000003 to ItemMetadata("Jump Spell", UpgradeCategory.LAB),
        26000005 to ItemMetadata("Freeze Spell", UpgradeCategory.LAB),
        26000009 to ItemMetadata("Poison Spell", UpgradeCategory.LAB),
        26000010 to ItemMetadata("Earthquake Spell", UpgradeCategory.LAB),
        26000011 to ItemMetadata("Haste Spell", UpgradeCategory.LAB),
        26000016 to ItemMetadata("Clone Spell", UpgradeCategory.LAB),
        26000017 to ItemMetadata("Skeleton Spell", UpgradeCategory.LAB),
        26000028 to ItemMetadata("Bat Spell", UpgradeCategory.LAB),
        26000035 to ItemMetadata("Invisibility Spell", UpgradeCategory.LAB),
        26000053 to ItemMetadata("Recall Spell", UpgradeCategory.LAB),
        26000070 to ItemMetadata("Overgrowth Spell", UpgradeCategory.LAB),
        26000098 to ItemMetadata("Revive Spell", UpgradeCategory.LAB),

        73000000 to ItemMetadata("L.A.S.S.I", UpgradeCategory.PETS, 15),
        73000001 to ItemMetadata("Electro Owl", UpgradeCategory.PETS, 15),
        73000002 to ItemMetadata("Mighty Yak", UpgradeCategory.PETS, 15),
        73000003 to ItemMetadata("Unicorn", UpgradeCategory.PETS, 15),
        73000004 to ItemMetadata("Phoenix", UpgradeCategory.PETS, 10),
        73000007 to ItemMetadata("Poison Lizard", UpgradeCategory.PETS, 10),
        73000008 to ItemMetadata("Diggy", UpgradeCategory.PETS, 10),
        73000009 to ItemMetadata("Frosty", UpgradeCategory.PETS, 15),
        73000010 to ItemMetadata("Spirit Fox", UpgradeCategory.PETS, 10),
        73000011 to ItemMetadata("Angry Jelly", UpgradeCategory.PETS, 10),
        73000016 to ItemMetadata("Sneezy", UpgradeCategory.PETS, 10)
    )

    private val helperNames = mapOf(
        93000000 to "Builder's Apprentice",
        93000001 to "Lab Assistant",
        93000002 to "Alchemist"
    )

    fun resolveName(dataId: Int): String {
        return registry[dataId]?.name ?: helperNames[dataId] ?: "Upgrade ($dataId)"
    }

    fun estimateTotalDurationSeconds(
        dataId: Int,
        targetLevel: Int,
        remainingSeconds: Long,
        category: UpgradeCategory
    ): Long {
        val estimatedDays = when (category) {
            UpgradeCategory.PETS -> when {
                targetLevel <= 5 -> 3L
                targetLevel <= 10 -> 5L
                else -> 7L
            }
            UpgradeCategory.LAB -> when {
                targetLevel <= 4 -> 2L
                targetLevel <= 8 -> 6L
                targetLevel <= 11 -> 10L
                else -> 14L
            }
            UpgradeCategory.BUILDER_HOME -> {
                if (dataId in 28000000..28999999) {
                    when {
                        targetLevel <= 20 -> 1L
                        targetLevel <= 50 -> 4L
                        targetLevel <= 75 -> 6L
                        else -> 8L
                    }
                } else {
                    when {
                        targetLevel <= 5 -> 1L
                        targetLevel <= 12 -> 4L
                        targetLevel <= 18 -> 8L
                        else -> 13L
                    }
                }
            }
            UpgradeCategory.BUILDER_BASE, UpgradeCategory.STAR_LAB -> 5L
        }
        val standardTotal = estimatedDays * 86_400L
        return max(standardTotal, (remainingSeconds * 1.15).toLong())
    }
}
