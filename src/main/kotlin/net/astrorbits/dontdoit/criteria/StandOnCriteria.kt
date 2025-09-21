package net.astrorbits.dontdoit.criteria

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.astrorbits.dontdoit.team.TeamData
import net.astrorbits.lib.Identifier
import net.astrorbits.lib.math.vector.Box
import net.astrorbits.lib.math.vector.Vec3d
import org.bukkit.Material
import org.bukkit.event.Listener

class StandOnCriteria : Criteria(), Listener {
    override val type: CriteriaType = CriteriaType.STAND_ON
    lateinit var blockTypes: List<Material>
    var isWildcard: Boolean = false

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        val blockContent = data[BLOCK_TYPES_KEY] ?: throw InvalidCriteriaException(this, "Missing key '$BLOCK_TYPES_KEY'")
        val blocks = blockContent.replace("\n", "").split(",").map { it.trim() }
        val result = ArrayList<Material>()
        for (block in blocks) {
            if (block.startsWith("#")) {
                val tagKey = Identifier.of(block.removePrefix("#")).toKey()
                val tag = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK).tags.firstOrNull { it.tagKey().key() == tagKey }
                    ?: throw InvalidCriteriaException(this, "Invalid block tag: $block")
                result.addAll(tag.values().map { Material.matchMaterial(it.asString())!! })
            } else if (block == "*") {
                isWildcard = true
            } else {
                val material = Material.matchMaterial(block)
                if (material == null || !material.isBlock) throw InvalidCriteriaException(this, "Invalid block: $block")
                result.add(material)
            }
        }
        blockTypes = result
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if (player.isOnGround) {
                val world = player.world
                val groundPos = player.location.clone().add(0.0, -0.01, 0.0)
                val block = world.getBlockAt(groundPos)
                val blockPos = Vec3d.fromLocation(groundPos).floor()
                if (block.isEmpty || !block.isCollidable || (!isWildcard && block.type !in blockTypes)) continue
                val isPosInCollisionShape = block.collisionShape.boundingBoxes.any { boundingBox ->
                    val box = Box.fromBoundingBox(boundingBox).offset(blockPos)
                    return@any groundPos in box
                }
                if (isPosInCollisionShape) {
                    trigger(player)
                    break
                }
            }
        }
    }

    companion object {
        const val BLOCK_TYPES_KEY = "block"
    }
}