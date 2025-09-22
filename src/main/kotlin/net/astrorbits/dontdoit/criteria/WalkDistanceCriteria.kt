package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.system.CriteriaType
import net.astrorbits.dontdoit.team.TeamData
import org.bukkit.Statistic
import java.util.UUID
import kotlin.math.floor
import kotlin.properties.Delegates

class WalkDistanceCriteria : Criteria() {
    override val type: CriteriaType = CriteriaType.WALK_DISTANCE
    var walkDistance by Delegates.notNull<Int>()

    private val playerOnBindWalkDistance: MutableMap<UUID, Int> = mutableMapOf()
    private val playerOnBindWalkOnWaterDistance: MutableMap<UUID, Int> = mutableMapOf()
    private val playerOnBindWalkUnderWaterDistance: MutableMap<UUID, Int> = mutableMapOf()

    override fun onBind(teamData: TeamData) {
        super.onBind(teamData)
        for (player in teamData.members) {
            val uuid = player.uniqueId
            playerOnBindWalkDistance[uuid] = player.getStatistic(Statistic.WALK_ONE_CM)
            playerOnBindWalkOnWaterDistance[uuid] = player.getStatistic(Statistic.WALK_ON_WATER_ONE_CM)
            playerOnBindWalkUnderWaterDistance[uuid] = player.getStatistic(Statistic.WALK_UNDER_WATER_ONE_CM)
        }
    }

    override fun onUnbind(teamData: TeamData) {
        super.onUnbind(teamData)
        for (player in teamData.members) {
            val uuid = player.uniqueId
            playerOnBindWalkDistance.remove(uuid)
            playerOnBindWalkOnWaterDistance.remove(uuid)
            playerOnBindWalkUnderWaterDistance.remove(uuid)
        }
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            val uuid = player.uniqueId
            val walk = player.getStatistic(Statistic.WALK_ONE_CM)
            val walkOnWater = player.getStatistic(Statistic.WALK_ON_WATER_ONE_CM)
            val walkUnderWater = player.getStatistic(Statistic.WALK_UNDER_WATER_ONE_CM)
            if (uuid !in playerOnBindWalkDistance) {
                playerOnBindWalkDistance[uuid] = walk
            }
            if (uuid !in playerOnBindWalkOnWaterDistance) {
                playerOnBindWalkUnderWaterDistance[uuid] = walkOnWater
            }
            if (uuid !in playerOnBindWalkUnderWaterDistance) {
                playerOnBindWalkUnderWaterDistance[uuid] = walkUnderWater
            }
            val totalWalkDistance = (walk - playerOnBindWalkDistance[uuid]!!) +
                (walkOnWater - playerOnBindWalkOnWaterDistance[uuid]!!) +
                (walkUnderWater - playerOnBindWalkUnderWaterDistance[uuid]!!)

            if (totalWalkDistance >= walkDistance) {
                trigger(player)
                break
            }
        }
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        val walkDistanceString = data[WALK_DISTANCE_KEY] ?: throw InvalidCriteriaException(this, "Missing key '$WALK_DISTANCE_KEY'")
        val walkDistanceMeters = try {
            walkDistanceString.toFloat()
        } catch (_: NumberFormatException) {
            throw InvalidCriteriaException(this, "Invalid walk distance: $walkDistanceString")
        }
        walkDistance = floor(walkDistanceMeters * CENTIMETER_PER_METER).toInt()
    }

    companion object {
        const val WALK_DISTANCE_KEY = "distance"
        const val CENTIMETER_PER_METER = 100
    }
}