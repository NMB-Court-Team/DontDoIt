package net.astrorbits.doNotDoIt.criteria

import com.google.gson.JsonObject
import net.astrorbits.doNotDoIt.team.TeamManager
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.function.Supplier

object CriteriaManager {
    private val allCriteriaListenerTypes: MutableMap<String, Supplier<CriteriaListener>> = mutableMapOf()
    private val allCriteriaListener: MutableList<Pair<CriteriaListener, CriteriaListener.SubCriteria>> = mutableListOf()
    private val allCriteria: MutableMap<String, String> = mutableMapOf()
    fun trigger(criteria: CriteriaListener, uuid: UUID) {
        val team = TeamManager.getTeamOf(uuid)?:return
        val teamCriteria = team.criteriaData?:return
        //val displayName = teamCriteria.displayName
        criteria.trigger(team, teamCriteria.first)
    }

    fun registerAll() {
        register("break_block", ::BreakBlockCriteria)
        register("place_block", ::EmptyCriteria)
        register("step_on", ::EmptyCriteria)
        register("used_item", ::ConsumeItemCriteria)
        register("drop_item", ::EmptyCriteria)
        register("pick_up", ::PickUpCriteria)
        register("craft", ::EmptyCriteria)
        register("damage", ::EmptyCriteria)
        register("death", ::EmptyCriteria)
        register("receive_damage", ::EmptyCriteria)
        register("walk", ::EmptyCriteria)
        register("kill", ::EmptyCriteria)
        register("fall", ::EmptyCriteria)
        register("jump", ::EmptyCriteria)
        register("entity_distance", ::EmptyCriteria)
        register("angle_pitch", ::EmptyCriteria)
        register("mainhand", ::EmptyCriteria)
        register("offhand", ::EmptyCriteria)
        register("health", ::EmptyCriteria)
        register("hunger", ::EmptyCriteria)
        register("surround_by", ::EmptyCriteria)
        register("inventory_item", ::EmptyCriteria)
        register("positioned_on", ::EmptyCriteria)

        register("move_time_idle", ::EmptyCriteria)
        register("sprint_time_idle", ::EmptyCriteria)
        register("sneak_time_duration", ::EmptyCriteria)
        register("revive_time_idle", ::EmptyCriteria)
        register("jump_time_idle", ::EmptyCriteria)
        register("angle_change_time_idle", ::EmptyCriteria)

        //EmptyCriteria

    }

    fun register(name: String, initializer: Supplier<CriteriaListener>) {
        if (name in allCriteriaListenerTypes) throw IllegalStateException("Already registered criteria '$name'")
        allCriteriaListenerTypes[name] = initializer
    }

    fun loadFromJson(json: JsonObject, plugin: JavaPlugin) {
        val tempAllCriteria = mutableListOf<CriteriaListener>()
        val allCriteriaJson = json.getAsJsonArray("criteria") ?: return

        for (element in allCriteriaJson) {
            if (!element.isJsonObject) continue
            val jsonObject = element.asJsonObject
            val type = jsonObject.get("type").asString ?: continue
            val criteriaInitializer = allCriteriaListenerTypes[type] ?: continue
            val criteria = criteriaInitializer.get()
            criteria.readFromJson(jsonObject)

            if (criteria is Listener) {
                plugin.server.pluginManager.registerEvents(criteria, plugin)
            }

            tempAllCriteria.add(criteria)
        }
    }

    fun formatName(rawName: String, index: Int): String {
        return rawName.replace(BLOCK_NAME_PATTERN, "<lang:${types[index].translationKey()}>")
    }

    fun removePlayer(){

    }

    fun getAllCriteria(): List<Pair<CriteriaListener, CriteriaListener.SubCriteria>> = allCriteriaListener

    fun getRandomCriteria(): Pair<CriteriaListener, CriteriaListener.SubCriteria> {
        return allCriteriaListener.random() // 等概率抽取
    }
}

