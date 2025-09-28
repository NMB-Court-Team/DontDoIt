package net.astrorbits.dontdoit.criteria.system

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.criteria.*
import net.astrorbits.dontdoit.criteria.builtin.UserDefinedCriteria
import net.astrorbits.dontdoit.criteria.builtin.YLevelCriteria
import net.astrorbits.dontdoit.criteria.helper.BuiltinCriteria
import net.astrorbits.dontdoit.criteria.helper.MoveType
import net.astrorbits.dontdoit.criteria.helper.YLevelType
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectContext
import net.astrorbits.dontdoit.criteria.inspect.InventoryInspectable
import net.astrorbits.dontdoit.system.generate.GameAreaGenerator
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.dontdoit.system.team.TeamManager
import net.astrorbits.lib.collection.CollectionHelper
import net.astrorbits.lib.config.Config
import net.astrorbits.lib.math.Duration
import net.astrorbits.lib.task.TaskBuilder
import net.astrorbits.lib.task.TaskType
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import java.util.Random
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Supplier

object CriteriaManager {
    private val LOGGER = DontDoIt.LOGGER

    const val CRITERIA_FILE_NAME = "criteria.yml"

    lateinit var criteriaConfig: Config
    lateinit var allCriteriaConfigData: CriteriaConfigData

    private val _allCriteriaTypes: Object2ReferenceOpenHashMap<String, Supplier<Criteria>> = Object2ReferenceOpenHashMap()
    private val _allCriteria: ReferenceOpenHashSet<Criteria> = ReferenceOpenHashSet()

    val allCriteriaTypes: Map<String, Supplier<Criteria>>
        get() = _allCriteriaTypes
    val allCriteria: Set<Criteria>
        get() = _allCriteria.filter { if (it is BuiltinCriteria) it.shouldUse() else true }.toSet()
    val triggerCountStat: MutableMap<UUID, Int> = mutableMapOf()

    fun trigger(criteria: Criteria, player: Player) {
        val teamData = TeamManager.getTeam(player) ?: return
        if (criteria === teamData.criteria) {
            teamData.trigger(player)
            val triggerCount = triggerCountStat.computeIfAbsent(player.uniqueId) { 0 }
            triggerCountStat[player.uniqueId] = triggerCount + 1
        }
    }

    fun trigger(criteria: Criteria, teamData: TeamData) {
        val teamCriteria = teamData.criteria ?: return
        if (teamCriteria === criteria) {
            teamData.trigger()
            for (player in teamData.members) {
                val triggerCount = triggerCountStat.computeIfAbsent(player.uniqueId) { 0 }
                triggerCountStat[player.uniqueId] = triggerCount + 1
            }
        }
    }

    fun init(plugin: JavaPlugin) {
        MoveType.registerListener(plugin)

        registerAll()

        criteriaConfig = Config(
            "criteria",
            plugin.dataPath.resolve(CRITERIA_FILE_NAME),
            CRITERIA_FILE_NAME,
            plugin.slF4JLogger
        )
        allCriteriaConfigData = criteriaConfig.defineConfig(CriteriaConfigData(
            "criteria",
            _allCriteriaTypes.keys.associateWith { emptyList() }
        ))
        criteriaConfig.load()
        loadFromConfig(plugin)
    }

    fun registerAll() {
        register("eat_item", ::EatItemCriteria)
        register("drop_item", ::DropItemCriteria)
        register("craft_item", ::CraftItemCriteria)
        register("pick_up_item", ::PickUpItemCriteria)
        register("holding_item", ::HoldingItemCriteria)
        register("inventory_containing_item", ::InventoryContainingItemCriteria)

        register("break_block", ::BreakBlockCriteria)
        register("place_block", ::PlaceBlockCriteria)
        register("physical_action", ::PhysicalActionCriteria)
        register("interact_block_with_item", ::InteractBlockWithItemCriteria)
        register("surrounded_by_block", ::SurroundedByBlockCriteria)
        register("in_block", ::InBlockCriteria)
        register("standing_on_block", ::StandingOnBlockCriteria)
        register("bucket_operation", ::BucketOperationCriteria)

        register("effect", ::EffectCriteria)
        register("hurt_entity", ::HurtEntityCriteria)
        register("kill_entity", ::KillEntityCriteria)
        register("hurt_by_entity", ::HurtByEntityCriteria)
        register("killed_by_entity", ::KilledByEntityCriteria)
        register("distance_to_entity", ::DistanceToEntityCriteria)

        register("respawn_time", ::RespawnTimeCriteria)
        register("move_time", ::MoveTimeCriteria)
        register("criteria_hold_time", ::CriteriaHoldTimeCriteria)
        register("after_death_time", ::AfterDeathTimeCriteria)

        register("health", ::HealthCriteria)
        register("rotation", ::RotationCriteria)
        register("food_level", ::FoodLevelCriteria)
        register("heightmap_matching_pos", ::HeightmapMatchingPosCriteria)
        register("life_count", ::LifeCountCriteria)
        register("walk_distance", ::WalkDistanceCriteria)
        register("jump", ::JumpCriteria)
        register("fall_distance", ::FallDistanceCriteria)
        register("level_up", ::LevelUpCriteria)
        register("immediately_trigger", ::ImmediatelyTriggerCriteria)
    }

    fun register(id: String, initializer: Supplier<Criteria>) {
        _allCriteriaTypes[id] = initializer
    }

    private fun loadFromConfig(plugin: JavaPlugin) {
        val pluginManager = plugin.server.pluginManager

        val allCriteriaTypes = allCriteriaTypes
        val configData = allCriteriaConfigData.get()
        for ((id, allData) in configData) {
            val initializer = allCriteriaTypes[id] ?: continue
            for (data in allData) {
                val criteria = initializer.get()
                try {
                    criteria.readData(data)
                } catch (e: InvalidCriteriaException) {
                    LOGGER.error("Error when loading criteria '$id': ", e)
                    continue
                }
                _allCriteria.add(criteria)
                if (criteria is Listener) {
                    pluginManager.registerEvents(criteria, plugin)
                }
            }
        }
    }

    fun onServerLoadInit() {
        addUserDefinedCriteria()
        addYLevelCriteria()
    }

    lateinit var userDefinedCriteria: Object2ReferenceMap<NamedTextColor, UserDefinedCriteria>
    lateinit var yLevelCriteria: Object2ReferenceMap<YLevelType, YLevelCriteria>

    fun addUserDefinedCriteria() {
        val criteria = Object2ReferenceOpenHashMap<NamedTextColor, UserDefinedCriteria>()
        for ((_, color) in TeamManager.TEAM_COLORS) {
            criteria[color] = UserDefinedCriteria(color)
        }
        _allCriteria.addAll(criteria.values)
        userDefinedCriteria = criteria
    }

    fun addYLevelCriteria() {
        val criteria = Object2ReferenceOpenHashMap<YLevelType, YLevelCriteria>()
        for (type in YLevelType.entries) {
            val c = YLevelCriteria()
            c.setBorder(Int.MIN_VALUE, type)
            criteria[type] = c
        }
        _allCriteria.addAll(criteria.values)
        yLevelCriteria = criteria
    }

    fun updateUserDefinedCriteria(names: Map<NamedTextColor, String>) {
        for ((color, name) in names) {
            userDefinedCriteria[color]?.setName(name)
        }
    }

    fun updateYLevelCriteria(groundYLevel: Int) {
        for ((type, criteria) in yLevelCriteria) {
            criteria.setBorder(groundYLevel, type)
        }
    }

    fun onDisable() {
        criteriaConfig.close()
    }

    fun onEnterPreparation() {
        triggerCountStat.clear()
    }

    fun onGameStart() {
        criteriaContextCalcTask = TaskBuilder(DontDoIt.instance, TaskType.Repeat(CONTEXT_CALC_INTERVAL))
            .setTask {
                val world = GameAreaGenerator.world ?: return@setTask
                val teams = TeamManager.getInUseTeams().values
                for (teamData in teams) {
                    val otherTeams = teams.filter { it !== teamData }
                    val teamColor = teamData.color
                    InventoryInspectContext.calcContextAsync(world, teamData, otherTeams)
                        .handle { ctx, e ->
                            if (e != null) {
                                LOGGER.error("Error when calculating inventory inspect context: ", e)
                                null
                            } else ctx
                        }.thenAccept { ctx ->
                            if (ctx != null) {
                                criteriaContext[teamColor] = ctx
                            }
                        }
                }
            }.runTask()
    }

    fun onGameEnd() {
        criteriaContextCalcTask?.cancel()
        criteriaContextCalcTask = null
    }

    private val random = Random()
    val CONTEXT_CALC_INTERVAL = Duration.seconds(15.0)
    var criteriaContextCalcTask: BukkitTask? = null
    const val INITIAL_WEIGHT = 1.0

    val criteriaContext: ConcurrentHashMap<NamedTextColor, InventoryInspectContext> = ConcurrentHashMap()

    fun getRandomCriteria(teamData: TeamData, oldCriteria: Criteria? = null): Criteria {
        val context = criteriaContext[teamData.color] ?: InventoryInspectContext.EMPTY
        val adjustedWeightMap = allCriteria.associateWith { criteria ->
            var adjustedWeight = criteria.initiallyModifyWeight(INITIAL_WEIGHT, teamData, oldCriteria)
            if (criteria is InventoryInspectable) {
                adjustedWeight = criteria.modifyWeight(adjustedWeight, teamData, context)
            }
            return@associateWith adjustedWeight
        }
        return CollectionHelper.selectByDoubleWeight(adjustedWeightMap, random)
    }
}
