package net.astrorbits.dontdoit.criteria

import net.astrorbits.dontdoit.criteria.helper.BucketOperationType
import net.astrorbits.dontdoit.criteria.helper.CriteriaType
import net.astrorbits.dontdoit.criteria.inspect.BlockInspectCandidate
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent

class BucketOperationCriteria : Criteria(), Listener, BlockInspectCandidate {
    override val type: CriteriaType = CriteriaType.BUCKET_OPERATION
    lateinit var fluid: Material
    lateinit var operationType: BucketOperationType

    override fun getCandidateBlockTypes(): Set<Material> {
        return setOf(fluid)
    }

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        data.setField(FLUID_KEY) {
            val material = Material.matchMaterial(it)
            if (material !in AVAILABLE_FLUIDS) {
                throw InvalidCriteriaException(this, "Invalid fluid: $it")
            }
            fluid = material!!
        }
        data.setField(OPERATION_TYPE_KEY) { operationType = BucketOperationType.valueOf(it.uppercase()) }
    }

    @EventHandler
    fun onBucketFilled(event: PlayerBucketFillEvent) {
        if (operationType == BucketOperationType.FILL && event.block.type == fluid) {
            trigger(event.player)
        }
    }

    @EventHandler
    fun onBucketPoured(event: PlayerBucketEmptyEvent) {
        if (operationType == BucketOperationType.POUR && event.bucket in BUCKETS[fluid]!!) {
            trigger(event.player)
        }
    }

    companion object {
        const val FLUID_KEY = "fluid"
        const val OPERATION_TYPE_KEY = "type"

        val AVAILABLE_FLUIDS: Set<Material> = setOf(Material.WATER, Material.LAVA, Material.POWDER_SNOW)
        val BUCKETS: Map<Material, Set<Material>> = mapOf(
            Material.WATER to setOf(
                Material.WATER_BUCKET,
                Material.COD_BUCKET,
                Material.SALMON_BUCKET,
                Material.TROPICAL_FISH_BUCKET,
                Material.PUFFERFISH_BUCKET,
                Material.AXOLOTL_BUCKET,
                Material.TADPOLE_BUCKET
            ),
            Material.LAVA to setOf(Material.LAVA),
            Material.POWDER_SNOW to setOf(Material.POWDER_SNOW_BUCKET)
        )
    }
}