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
    lateinit var blockTypes: Set<Material>
    var isWildcard: Boolean = false
    var reversed: Boolean = false

    override fun readData(data: Map<String, String>) {
        super.readData(data)
        val entries = data.getCsvEntries(BreakBlockCriteria.BLOCK_TYPES_KEY, AbsentBehavior.WILDCARD)

        val result = HashSet<Material>()
        for ((block, isTag, isReversed) in entries) {
            if (isReversed && result.isEmpty()) {
                result.addAll(Material.entries.filter { it.isBlock })
            }
            val blocks = HashSet<Material>()
            if (isTag) {
                val tagKey = Identifier.of(block).toKey()
                val tag = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK).tags.firstOrNull { it.tagKey().key() == tagKey }
                    ?: throw InvalidCriteriaException(this, "Invalid block tag: $block")
                blocks.addAll(tag.values().map { Material.matchMaterial(it.asString())!! })
            } else {
                val material = Material.matchMaterial(block)
                if (material == null || !material.isBlock) throw InvalidCriteriaException(this, "Invalid block: $block")
                blocks.add(material)
            }
            if (isReversed) {
                result.removeAll(blocks)
            } else {
                result.addAll(blocks)
            }
        }
        blockTypes = result

        isWildcard = entries.isWildcard

        data.setBoolField(REVERSED_KEY, true) { reversed = it }
    }

    override fun tick(teamData: TeamData) {
        for (player in teamData.members) {
            if (isWildcard && reversed && !player.isOnGround) {
                trigger(player)
                break
            }
            if (player.isOnGround) {
                val world = player.world
                val groundPos = player.location.clone().add(0.0, -0.01, 0.0)
                val block = world.getBlockAt(groundPos)
                val blockPos = Vec3d.fromLocation(groundPos).floor()
                if (block.isEmpty ||
                    !block.isCollidable ||
                    (!reversed && !isWildcard && block.type !in blockTypes) ||
                    (reversed && block.type in blockTypes)
                ) continue

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
        const val REVERSED_KEY = "reversed"
    }
}