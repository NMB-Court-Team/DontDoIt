package net.astrorbits.dontdoit.system.generate

import net.astrorbits.lib.math.vector.Vec3i
import org.bukkit.Material
import org.bukkit.World

class BlockGeneration(val blockType: Material, val generationEntries: List<GenerationEntry>) {
    fun generate(depth: Int, totalCount: Int, positions: MutableSet<Vec3i>, world: World) {
        val entry = generationEntries.firstOrNull { depth in it } ?: return
        val selectedPositions = entry.selectAndRemove(totalCount, positions)
        for (pos in selectedPositions) {
            world.getBlockAt(pos.x, pos.y, pos.z).setType(blockType, false)
        }
    }
}
