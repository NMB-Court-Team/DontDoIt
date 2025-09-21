package net.astrorbits.dontdoit.criteria

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.team.TeamData
import net.astrorbits.dontdoit.team.TeamManager
import net.astrorbits.lib.config.Config
import org.bukkit.entity.Player
import java.util.function.Supplier

object CriteriaManager {
    private val LOGGER = DontDoIt.LOGGER

    const val CRITERIA_FILE_NAME = "criteria.yml"

    lateinit var criteriaConfig: Config
    lateinit var allCriteriaConfigData: CriteriaConfigData

    private val _allCriteriaTypes: Object2ReferenceOpenHashMap<String, Supplier<Criteria>> = Object2ReferenceOpenHashMap()
    private val _allCriteria: ReferenceArrayList<Criteria> = ReferenceArrayList()

    val allCriteriaTypes: Map<String, Supplier<Criteria>>
        get() = _allCriteriaTypes
    val allCriteria: List<Criteria>
        get() = _allCriteria

    fun trigger(criteria: Criteria, player: Player) {
        val teamData = TeamManager.getTeamOf(player) ?: return
        trigger(criteria, teamData)
    }

    fun trigger(criteria: Criteria, teamData: TeamData) {
        val teamCriteria = teamData.criteria ?: return
        if (teamCriteria === criteria) {
            criteria.onUnbind(teamData)
        }
    }

    fun init() {
        registerAll()

        criteriaConfig = Config(
            "criteria",
            DontDoIt.instance.dataPath.resolve(CRITERIA_FILE_NAME),
            CRITERIA_FILE_NAME,
            DontDoIt.LOGGER
        )
        allCriteriaConfigData = criteriaConfig.defineConfig(CriteriaConfigData(
            "criteria",
            _allCriteriaTypes.keys.associateWith { emptyList() }
        ))
        loadFromConfig()
    }

    fun registerAll() {
        register("consumed_item", ::EatenItemCriteria)

//        register("break_block", ::BreakBlockCriteria)
//        register("place_block", ::EmptyCriteria)
//        register("step_on", ::EmptyCriteria)
//        register("used_item", ::ConsumeItemCriteria)
//        register("drop_item", ::EmptyCriteria)
//        register("pick_up", ::PickUpCriteria)
//        register("craft", ::EmptyCriteria)
//        register("damage", ::EmptyCriteria)
//        register("death", ::EmptyCriteria)
//        register("receive_damage", ::EmptyCriteria)
//        register("walk", ::EmptyCriteria)
//        register("kill", ::EmptyCriteria)
//        register("fall", ::EmptyCriteria)
//        register("jump", ::EmptyCriteria)
//        register("entity_distance", ::EmptyCriteria)
//        register("angle_pitch", ::EmptyCriteria)
//        register("mainhand", ::EmptyCriteria)
//        register("offhand", ::EmptyCriteria)
//        register("health", ::EmptyCriteria)
//        register("hunger", ::EmptyCriteria)
//        register("surround_by", ::EmptyCriteria)
//        register("inventory_item", ::EmptyCriteria)
//        register("positioned_on", ::EmptyCriteria)
//        register("move_time_idle", ::EmptyCriteria)
//        register("sprint_time_idle", ::EmptyCriteria)
//        register("sneak_time_duration", ::EmptyCriteria)
//        register("revive_time_idle", ::EmptyCriteria)
//        register("jump_time_idle", ::EmptyCriteria)
//        register("angle_change_time_idle", ::EmptyCriteria)

    }

    fun register(id: String, initializer: Supplier<Criteria>) {
        _allCriteriaTypes[id] = initializer
    }

    private fun loadFromConfig() {
        val allCriteriaTypes = this.allCriteriaTypes
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
            }
        }
    }

    fun getRandomCriteria(): Criteria {
        //TODO 要加上仓检机制
        return allCriteria.random() // 等概率抽取
    }
}

