package net.astrorbits.lib.scoreboard

import io.papermc.paper.scoreboard.numbers.FixedFormat
import io.papermc.paper.scoreboard.numbers.NumberFormat
import io.papermc.paper.scoreboard.numbers.StyledFormat
import net.astrorbits.lib.text.TextHelper.red
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Score
import org.bukkit.scoreboard.Scoreboard

/**
 * 对所有需要被显示的玩家显示的内容都一样的侧边栏
 */
class SidebarDisplay {
    private val scoreboard: Scoreboard = Bukkit.getScoreboardManager().newScoreboard
    private val uid: Int = getUid()
    private var objective: Objective = scoreboard.registerNewObjective(
        objectiveName(uid),
        Criteria.DUMMY,
        Component.empty()
    )

    var lineCount: Int = 0

    var title: Component
        get() = objective.displayName()
        set(value) { objective.displayName(value) }
    var content: List<ScoreEntry> = emptyList()
        get() {
            val scores = ArrayList<Score>()
            for (i in 0..lineCount) {
                scores.add(objective.getScore(scoreName(i)))
            }
            return scores.map { score ->
                val name = score.customName() ?: Component.empty()
                val numberFormat = score.numberFormat() ?: Component.text(score.score).red()
                val number = when (numberFormat) {
                    is FixedFormat -> numberFormat.component()
                    is StyledFormat -> Component.text(score.score).style(numberFormat.style())
                    else -> Component.empty()
                }
                return@map ScoreEntry(name, number)
            }
        }
        set(value) {
            lineCount = value.size - 1
            for ((i, entry) in value.withIndex()) {
                val (name, number) = entry
                val score = objective.getScore(scoreName(i))
                score.score = value.size - i
                score.customName(name)
                score.numberFormat(NumberFormat.fixed(number))
            }
            field = value
        }

    fun clearDisplay() {
        unregisterDisplay()
        objective = scoreboard.registerNewObjective(
            objectiveName(uid),
            Criteria.DUMMY,
            Component.empty()
        )
    }

    fun hide() {
        objective.displaySlot = null
    }

    fun show() {
        objective.displaySlot = DisplaySlot.SIDEBAR
    }

    fun unregisterDisplay() {
        try {
            objective.unregister()
        } catch (_: Exception) { }
    }

    fun addPlayer(player: Player) {
        player.scoreboard = scoreboard
    }

    fun removePlayer(player: Player) {
        player.scoreboard = emptyScoreboard
    }

    data class ScoreEntry(val name: Component, val number: Component)

    companion object {
        private var globalUid = 0

        private fun getUid(): Int {
            return globalUid++
        }

        private val emptyScoreboard: Scoreboard by lazy { Bukkit.getScoreboardManager().newScoreboard }

        private fun scoreName(index: Int): String = "line.$index"
        private fun objectiveName(uid: Int): String = "sidebar_display.$uid"
    }
}