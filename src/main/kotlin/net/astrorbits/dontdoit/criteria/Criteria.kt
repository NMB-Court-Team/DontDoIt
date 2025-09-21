package net.astrorbits.dontdoit.criteria

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.astrorbits.dontdoit.team.TeamData
import net.astrorbits.lib.Identifier
import net.astrorbits.lib.range.DoubleRange
import net.astrorbits.lib.range.FloatRange
import net.astrorbits.lib.range.IntRange
import net.astrorbits.lib.text.TextHelper
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.damage.DamageType
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.*

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
        /**
         * 指定键的项不存在时，返回空的[CsvEntryList]
         */
        EMPTY,
        /**
         * 指定键的项不存在时，返回空的[CsvEntryList]，且[CsvEntryList.isWildcard]为`true`
         */
        WILDCARD,
        /**
         * 指定键的项不存在时，抛出异常
         */
        EXCEPTION
    }

    /**
     * 获取逗号分隔的项
     * @param absentBehavior 当指定键的项不存在时的行为
     */
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

    protected fun Map<String, String>.setBlockTypes(
        key: String,
        absentBehavior: AbsentBehavior = AbsentBehavior.WILDCARD,
        fieldSetter: (blockTypes: Set<Material>, isWildcard: Boolean) -> Unit
    ) {
        val entries = getCsvEntries(key, absentBehavior)

        val result = HashSet<Material>()
        for ((block, isTag, isReversed) in entries) {
            if (isReversed && result.isEmpty()) {
                result.addAll(Material.entries.filter { it.isBlock })
            }
            val blocks = HashSet<Material>()
            if (isTag) {
                val tagKey = Identifier.of(block).toKey()
                val tag = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK).tags.firstOrNull { it.tagKey().key() == tagKey }
                    ?: throw InvalidCriteriaException(this@Criteria, "Invalid block tag: $block")
                blocks.addAll(tag.values().map { Material.matchMaterial(it.asString())!! })
            } else {
                val material = Material.matchMaterial(block)
                if (material == null || !material.isBlock) throw InvalidCriteriaException(this@Criteria, "Invalid block: $block")
                blocks.add(material)
            }
            if (isReversed) {
                result.removeAll(blocks)
            } else {
                result.addAll(blocks)
            }
        }
        fieldSetter(result, entries.isWildcard)
    }

    protected fun Map<String, String>.setItemTypes(
        key: String,
        absentBehavior: AbsentBehavior = AbsentBehavior.WILDCARD,
        fieldSetter: (itemTypes: Set<Material>, isWildcard: Boolean) -> Unit
    ) {
        val entries = getCsvEntries(key, absentBehavior)

        val result = HashSet<Material>()
        for ((item, isTag, isReversed) in entries) {
            if (isReversed && result.isEmpty()) {
                result.addAll(Material.entries.filter { it.isBlock })
            }
            val items = HashSet<Material>()
            if (isTag) {
                val tagKey = Identifier.of(item).toKey()
                val tag = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).tags.firstOrNull { it.tagKey().key() == tagKey }
                    ?: throw InvalidCriteriaException(this@Criteria, "Invalid item tag: $item")
                items.addAll(tag.values().map { Material.matchMaterial(it.asString())!! })
            } else {
                val material = Material.matchMaterial(item)
                if (material == null || !material.isItem) throw InvalidCriteriaException(this@Criteria, "Invalid item: $item")
                items.add(material)
            }
            if (isReversed) {
                result.removeAll(items)
            } else {
                result.addAll(items)
            }
        }
        fieldSetter(result, entries.isWildcard)
    }

    protected fun Map<String, String>.setEntityTypes(
        key: String,
        absentBehavior: AbsentBehavior = AbsentBehavior.WILDCARD,
        fieldSetter: (entityTypes: Set<EntityType>, isWildcard: Boolean) -> Unit
    ) {
        val entries = getCsvEntries(key, absentBehavior)

        val result = HashSet<EntityType>()
        for ((entity, isTag, isReversed) in entries) {
            if (isReversed && result.isEmpty()) {
                result.addAll(EntityType.entries)
            }
            val entityId = Identifier.of(entity)
            if (!entityId.isVanilla()) throw InvalidCriteriaException(this@Criteria, "Non-vanilla entities are not supported")
            val entities = HashSet<EntityType>()
            if (isTag) {
                val tagKey = entityId.toKey()
                val tag = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE).tags.firstOrNull { it.tagKey().key() == tagKey }
                    ?: throw InvalidCriteriaException(this@Criteria, "Invalid entity tag: $entity")
                entities.addAll(tag.values().map { EntityType.fromName(it.value())!! })
            } else {
                val entityType = EntityType.fromName(entityId.path) ?: throw InvalidCriteriaException(this@Criteria, "Invalid entity: $entity")
                entities.add(entityType)
            }
            if (isReversed) {
                result.removeAll(entities)
            } else {
                result.addAll(entities)
            }
        }
        fieldSetter(result, entries.isWildcard)
    }

    protected fun Map<String, String>.setDamageTypes(
        key: String,
        absentBehavior: AbsentBehavior = AbsentBehavior.WILDCARD,
        fieldSetter: (entityTypes: Set<DamageType>, isWildcard: Boolean) -> Unit
    ) {
        val entries = getCsvEntries(key, absentBehavior)

        val result = HashSet<DamageType>()
        for ((damageType, isTag, isReversed) in entries) {
            if (isReversed && result.isEmpty()) {
                result.addAll(ALL_DAMAGE_TYPES)
            }
            val damageTypeId = Identifier.of(damageType)
            if (!damageTypeId.isVanilla()) throw InvalidCriteriaException(this@Criteria, "Non-vanilla damage types are not supported")
            val damageTypes = HashSet<DamageType>()
            val damageTypeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE)
            if (isTag) {
                val tagKey = damageTypeId.toKey()
                val tag = damageTypeRegistry.tags.firstOrNull { it.tagKey().key() == tagKey }
                    ?: throw InvalidCriteriaException(this@Criteria, "Invalid damage type tag: $damageType")
                damageTypes.addAll(tag.values().map { RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE).getOrThrow(it) })
            } else {
                val loadedDamageType = damageTypeRegistry.get(damageTypeId.toKey())
                    ?: throw InvalidCriteriaException(this@Criteria, "Invalid damage type: $damageType")
                damageTypes.add(loadedDamageType)
            }
            if (isReversed) {
                result.removeAll(damageTypes)
            } else {
                result.addAll(damageTypes)
            }
        }
        fieldSetter(result, entries.isWildcard)
    }

    companion object {
        const val NAME_KEY = "name"
        const val EASY_TO_TRIGGER = "easy_to_trigger"

        val ALL_DAMAGE_TYPES: Set<DamageType> = setOf(
            DamageType.ARROW, DamageType.BAD_RESPAWN_POINT, DamageType.CACTUS, DamageType.CAMPFIRE, DamageType.CRAMMING, DamageType.DRAGON_BREATH,
            DamageType.DROWN, DamageType.DRY_OUT, DamageType.ENDER_PEARL, DamageType.EXPLOSION, DamageType.FALL, DamageType.FALLING_ANVIL,
            DamageType.FALLING_BLOCK, DamageType.FALLING_STALACTITE, DamageType.FIREBALL, DamageType.FIREWORKS, DamageType.FLY_INTO_WALL, DamageType.FREEZE,
            DamageType.GENERIC, DamageType.GENERIC_KILL, DamageType.HOT_FLOOR, DamageType.IN_FIRE, DamageType.IN_WALL, DamageType.INDIRECT_MAGIC,
            DamageType.LAVA, DamageType.LIGHTNING_BOLT, DamageType.MACE_SMASH, DamageType.MAGIC, DamageType.MOB_ATTACK, DamageType.MOB_ATTACK_NO_AGGRO,
            DamageType.MOB_PROJECTILE, DamageType.ON_FIRE, DamageType.OUT_OF_WORLD, DamageType.OUTSIDE_BORDER, DamageType.PLAYER_ATTACK,
            DamageType.PLAYER_EXPLOSION, DamageType.SONIC_BOOM, DamageType.SPIT, DamageType.STALAGMITE, DamageType.STARVE, DamageType.STING,
            DamageType.SWEET_BERRY_BUSH, DamageType.THORNS, DamageType.THROWN, DamageType.TRIDENT, DamageType.UNATTRIBUTED_FIREBALL, DamageType.WIND_CHARGE,
            DamageType.WITHER, DamageType.WITHER_SKULL
        )
    }
}
