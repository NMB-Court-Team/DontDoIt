package net.astrorbits.dontdoit.criteria

import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.astrorbits.dontdoit.team.TeamData
import net.astrorbits.lib.range.DoubleRange
import net.astrorbits.lib.range.FloatRange
import net.astrorbits.lib.range.IntRange
import net.astrorbits.lib.text.TextHelper
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.ArrayList
import java.util.UUID

abstract class Criteria {
    abstract val type: CriteriaType
    var easyToTrigger: Boolean = false
    lateinit var displayName: Component
    val holders: ReferenceArrayList<TeamData> = ReferenceArrayList()

    /**
     * 当队伍绑定了该词条时调用
     *
     * 绑定操作包括：
     * 1. 游戏开始时获得此词条
     * 2. 词条自动更换或触发后，替换到此词条
     * @param teamData 绑定了此词条的队伍
     */
    open fun onBind(teamData: TeamData) {
        holders.add(teamData)
    }

    /**
     * 当队伍解除绑定该词条时调用
     *
     * 解除绑定操作包括：
     * 1. 此词条自动更换成了另一个词条
     * 2. 此词条被触发
     * 3. 游戏结束时取消所有玩家的词条，包括此词条
     * @param teamData 解除绑定了此词条的队伍
     */
    open fun onUnbind(teamData: TeamData) {
        holders.remove(teamData)
    }

    /**
     * 游戏进行期间每刻调用
     * @param teamData 持有此词条的队伍
     */
    open fun tick(teamData: TeamData) { }

    fun trigger(player: Player) {
        if (holders.any { player in it }) {
            CriteriaManager.trigger(this, player)
        }
    }

    fun trigger(playerUuid: UUID) {
        val player = Bukkit.getPlayer(playerUuid) ?: return
        trigger(player)
    }

    fun trigger(teamData: TeamData) {
        if (teamData in holders) {
            CriteriaManager.trigger(this, teamData)
        }
    }

    /**
     * 读取数据，用于初始化词条
     */
    open fun readData(data: Map<String, String>) {
        data.setBoolField(EASY_TO_TRIGGER, true) { easyToTrigger = it }
        data.setField(NAME_KEY) { displayName = TextHelper.parseMiniMessage(it) }
    }

    protected fun Map<String, String>.setField(key: String, ignoreIfAbsent: Boolean = false, fieldSetter: (String) -> Unit) {
        val content = this[key]
        if (!ignoreIfAbsent && content == null) {
            throw InvalidCriteriaException(this@Criteria, "Missing key '$key'")
        } else if (content == null) { // && ignoreIfAbsent
            return
        }
        fieldSetter(content)
    }

    protected fun Map<String, String>.setBoolField(key: String, ignoreIfAbsent: Boolean = false, fieldSetter: (Boolean) -> Unit) {
        setField(key, ignoreIfAbsent) { content ->
            val value = try {
                content.lowercase().toBooleanStrict()
            } catch (e: IllegalArgumentException) {
                throw InvalidCriteriaException(this@Criteria, "Invalid boolean value: $content")
            }
            fieldSetter(value)
        }
    }

    protected fun Map<String, String>.setIntField(key: String, ignoreIfAbsent: Boolean = false, fieldSetter: (Int) -> Unit) {
        setField(key, ignoreIfAbsent) { content ->
            val value = try {
                content.toInt()
            } catch (e: NumberFormatException) {
                throw InvalidCriteriaException(this@Criteria, "Invalid int value: $content")
            }
            fieldSetter(value)
        }
    }

    protected fun Map<String, String>.setFloatField(key: String, ignoreIfAbsent: Boolean = false, fieldSetter: (Float) -> Unit) {
        setField(key, ignoreIfAbsent) { content ->
            val value = try {
                content.toFloat()
            } catch (e: NumberFormatException) {
                throw InvalidCriteriaException(this@Criteria, "Invalid float value: $content")
            }
            fieldSetter(value)
        }
    }

    protected fun Map<String, String>.setDoubleField(key: String, ignoreIfAbsent: Boolean = false, fieldSetter: (Double) -> Unit) {
        setField(key, ignoreIfAbsent) { content ->
            val value = try {
                content.toDouble()
            } catch (e: NumberFormatException) {
                throw InvalidCriteriaException(this@Criteria, "Invalid double value: $content")
            }
            fieldSetter(value)
        }
    }

    protected fun Map<String, String>.setIntRangeField(key: String, ignoreIfAbsent: Boolean = false, fieldSetter: (IntRange) -> Unit) {
        setField(key, ignoreIfAbsent) { content ->
            val value = try {
                IntRange.parse(content)
            } catch (e: Exception) {
                throw InvalidCriteriaException(this@Criteria, "Invalid int range: $content")
            }
            fieldSetter(value)
        }
    }

    protected fun Map<String, String>.setFloatRangeField(key: String, ignoreIfAbsent: Boolean = false, fieldSetter: (FloatRange) -> Unit) {
        setField(key, ignoreIfAbsent) { content ->
            val value = try {
                FloatRange.parse(content)
            } catch (e: Exception) {
                throw InvalidCriteriaException(this@Criteria, "Invalid float range: $content")
            }
            fieldSetter(value)
        }
    }

    protected fun Map<String, String>.setDoubleRangeField(key: String, ignoreIfAbsent: Boolean = false, fieldSetter: (DoubleRange) -> Unit) {
        setField(key, ignoreIfAbsent) { content ->
            val value = try {
                DoubleRange.parse(content)
            } catch (e: Exception) {
                throw InvalidCriteriaException(this@Criteria, "Invalid double range: $content")
            }
            fieldSetter(value)
        }
    }

    protected enum class AbsentBehavior {
        EMPTY, WILDCARD, EXCEPTION
    }

    protected fun Map<String, String>.getCsvEntries(key: String, absentBehavior: AbsentBehavior = AbsentBehavior.EXCEPTION): CsvEntryList {
        val content = this[key] ?: if (absentBehavior == AbsentBehavior.EXCEPTION) {
            throw InvalidCriteriaException(this@Criteria, "Missing key '$key")
        } else {
            return CsvEntryList(emptyList(), absentBehavior == AbsentBehavior.WILDCARD)
        }
        val stringEntries = content.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val entries = ArrayList<CsvEntry>()
        var isWildcard = false
        for (stringEntry in stringEntries) {
            if (stringEntry == "*") {
                isWildcard = true
            }
            var isTag = false
            var isReversed = false
            var csvContent: String = stringEntry
            if (stringEntry.startsWith("#")) {
                isTag = true
                csvContent = stringEntry.removePrefix("#")
            }
            if (stringEntry.startsWith("!")) {
                isReversed = true
                csvContent = stringEntry.removePrefix("!")
            }
            if (stringEntry.startsWith("!#")) {
                isTag = true
                isReversed = true
                csvContent = stringEntry.removePrefix("!#")
            }
            if (stringEntry.startsWith("#!")) {
                throw InvalidCriteriaException(this@Criteria, "Illegal token order: '#!'")
            }
            entries.add(CsvEntry(csvContent, isTag, isReversed))
        }
        return CsvEntryList(entries, isWildcard)
    }

    protected class CsvEntryList(private val entries: List<CsvEntry>, val isWildcard: Boolean) : List<CsvEntry> by entries

    protected data class CsvEntry(val content: String, val isTag: Boolean, val isReversed: Boolean)

    companion object {
        const val NAME_KEY = "name"
        const val EASY_TO_TRIGGER = "easy_to_trigger"
    }
}
