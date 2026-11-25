package com.clashtimers.widget.widget

import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.clashtimers.widget.R
import com.clashtimers.widget.data.VillageRepository
import com.clashtimers.widget.model.ActiveUpgrade
import com.clashtimers.widget.model.UpgradeCategory
import com.clashtimers.widget.util.TimeFormatUtils

class ClashWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ClashRemoteViewsFactory(applicationContext)
    }
}

class ClashRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private sealed class WidgetRowItem {
        data class SectionHeader(
            val title: String,
            val category: UpgradeCategory,
            val activeBoostRemainingSec: Long
        ) : WidgetRowItem()

        data class UpgradeRow(val upgrade: ActiveUpgrade) : WidgetRowItem()
    }

    private val rows = mutableListOf<WidgetRowItem>()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        rows.clear()
        val repository = VillageRepository(context)
        val state = repository.loadVillageState() ?: return
        val nowMillis = System.currentTimeMillis()

        val homeUpgrades = state.upgrades.filter {
            it.category == UpgradeCategory.BUILDER_HOME && it.remainingSecondsAt(nowMillis) > 0L
        }
        val builderBoostRemaining = ((state.builderBoostEndMillis - nowMillis) / 1000L).coerceAtLeast(0L)
        rows.add(
            WidgetRowItem.SectionHeader(
                title = repository.formatBuilderHeader(state, nowMillis),
                category = UpgradeCategory.BUILDER_HOME,
                activeBoostRemainingSec = builderBoostRemaining
            )
        )
        homeUpgrades.forEach { rows.add(WidgetRowItem.UpgradeRow(it)) }

        val labUpgrades = state.upgrades.filter {
            it.category == UpgradeCategory.LAB && it.remainingSecondsAt(nowMillis) > 0L
        }
        val labBoostRemaining = ((state.labBoostEndMillis - nowMillis) / 1000L).coerceAtLeast(0L)
        rows.add(
            WidgetRowItem.SectionHeader(
                title = "LAB",
                category = UpgradeCategory.LAB,
                activeBoostRemainingSec = labBoostRemaining
            )
        )
        labUpgrades.forEach { rows.add(WidgetRowItem.UpgradeRow(it)) }

        val petUpgrades = state.upgrades.filter {
            it.category == UpgradeCategory.PETS && it.remainingSecondsAt(nowMillis) > 0L
        }
        val petBoostRemaining = ((state.petBoostEndMillis - nowMillis) / 1000L).coerceAtLeast(0L)
        rows.add(
            WidgetRowItem.SectionHeader(
                title = "PETS",
                category = UpgradeCategory.PETS,
                activeBoostRemainingSec = petBoostRemaining
            )
        )
        petUpgrades.forEach { rows.add(WidgetRowItem.UpgradeRow(it)) }

        val bbUpgrades = state.upgrades.filter {
            (it.category == UpgradeCategory.BUILDER_BASE || it.category == UpgradeCategory.STAR_LAB) &&
                it.remainingSecondsAt(nowMillis) > 0L
        }
        if (bbUpgrades.isNotEmpty()) {
            rows.add(
                WidgetRowItem.SectionHeader(
                    title = "BUILDER BASE (${bbUpgrades.size} active)",
                    category = UpgradeCategory.BUILDER_BASE,
                    activeBoostRemainingSec = 0L
                )
            )
            bbUpgrades.forEach { rows.add(WidgetRowItem.UpgradeRow(it)) }
        }
    }

    override fun onDestroy() {
        rows.clear()
    }

    override fun getCount(): Int = rows.size

    override fun getViewTypeCount(): Int = 2

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = false

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewAt(position: Int): RemoteViews? {
        if (position !in rows.indices) return null
        val nowMillis = System.currentTimeMillis()

        return when (val item = rows[position]) {
            is WidgetRowItem.SectionHeader -> bindSectionHeader(item)
            is WidgetRowItem.UpgradeRow -> bindUpgradeRow(item.upgrade, nowMillis)
        }
    }

    private fun bindSectionHeader(header: WidgetRowItem.SectionHeader): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_item_section_header)
        rv.setTextViewText(R.id.tv_section_title, header.title)

        if (header.category == UpgradeCategory.BUILDER_HOME) {
            rv.setViewVisibility(R.id.btn_bite, View.VISIBLE)
            val biteIntent = Intent().apply {
                putExtra(ClashWidgetProvider.EXTRA_BOOST_ACTION, ClashWidgetProvider.ACTION_BUILDER_BITE)
            }
            rv.setOnClickFillInIntent(R.id.btn_bite, biteIntent)
        } else {
            rv.setViewVisibility(R.id.btn_bite, View.GONE)
        }

        if (header.category == UpgradeCategory.BUILDER_BASE) {
            rv.setViewVisibility(R.id.btn_potion_plus, View.GONE)
            rv.setViewVisibility(R.id.btn_potion_minus, View.GONE)
        } else {
            rv.setViewVisibility(R.id.btn_potion_plus, View.VISIBLE)
            val boostLabel = if (header.activeBoostRemainingSec > 0L) {
                "🧪 ${TimeFormatUtils.formatCompactDuration(header.activeBoostRemainingSec)}"
            } else {
                "🧪 +1"
            }
            rv.setTextViewText(R.id.btn_potion_plus, boostLabel)

            val boostAction = when (header.category) {
                UpgradeCategory.BUILDER_HOME -> ClashWidgetProvider.ACTION_BUILDER_POTION
                UpgradeCategory.LAB -> ClashWidgetProvider.ACTION_LAB_POTION
                UpgradeCategory.PETS -> ClashWidgetProvider.ACTION_PET_POTION
                else -> ""
            }
            val plusIntent = Intent().apply {
                putExtra(ClashWidgetProvider.EXTRA_BOOST_ACTION, boostAction)
            }
            rv.setOnClickFillInIntent(R.id.btn_potion_plus, plusIntent)

            if (header.category == UpgradeCategory.BUILDER_HOME && header.activeBoostRemainingSec > 0L) {
                rv.setViewVisibility(R.id.btn_potion_minus, View.VISIBLE)
                val minusIntent = Intent().apply {
                    putExtra(ClashWidgetProvider.EXTRA_BOOST_ACTION, ClashWidgetProvider.ACTION_UNDO_BUILDER_POTION)
                }
                rv.setOnClickFillInIntent(R.id.btn_potion_minus, minusIntent)
            } else {
                rv.setViewVisibility(R.id.btn_potion_minus, View.GONE)
            }
        }

        return rv
    }

    private fun bindUpgradeRow(upgrade: ActiveUpgrade, nowMillis: Long): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_item_upgrade)

        val goblinPrefix = if (upgrade.isGoblin) "👺 " else ""
        rv.setTextViewText(R.id.tv_upgrade_name, "$goblinPrefix${upgrade.name}")

        val levelLabel = if (upgrade.isSupercharge) {
            "⚡ ${upgrade.targetLevel}"
        } else {
            "→ ${upgrade.targetLevel}"
        }
        rv.setTextViewText(R.id.tv_upgrade_level, levelLabel)

        val progress = upgrade.progressPercentAt(nowMillis)
        rv.setProgressBar(R.id.pb_upgrade_progress, 100, progress, false)

        val remainingSec = upgrade.remainingSecondsAt(nowMillis)
        val days = remainingSec / 86_400L

        if (days > 0L) {
            rv.setViewVisibility(R.id.tv_days_prefix, View.VISIBLE)
            rv.setTextViewText(R.id.tv_days_prefix, "${days}d ")
            val remainderMillis = (remainingSec % 86_400L) * 1000L
            val baseElapsed = SystemClock.elapsedRealtime() + remainderMillis
            rv.setChronometer(R.id.chronometer_upgrade, baseElapsed, null, true)
        } else {
            rv.setViewVisibility(R.id.tv_days_prefix, View.GONE)
            val baseElapsed = TimeFormatUtils.toElapsedRealtimeBase(upgrade.finishEpochMillis, nowMillis)
            rv.setChronometer(R.id.chronometer_upgrade, baseElapsed, null, true)
        }
        rv.setChronometerCountDown(R.id.chronometer_upgrade, true)

        return rv
    }
}
