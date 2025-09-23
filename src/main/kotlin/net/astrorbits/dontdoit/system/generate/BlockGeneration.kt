package net.astrorbits.dontdoit.system.generate

import net.astrorbits.lib.NMSWarning
import net.astrorbits.lib.math.vector.Vec3i
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Block
import org.bukkit.Material
import org.bukkit.craftbukkit.block.data.CraftBlockData

class BlockGeneration(val blockType: Material, val generationEntries: List<GenerationEntry>) {
    @NMSWarning
    fun generate(depth: Int, totalCount: Int, positions: MutableSet<Vec3i>, level: ServerLevel) {
        val entry = generationEntries.firstOrNull { depth in it } ?: return
        val selectedPositions = entry.selectAndRemove(totalCount, positions)
        for (pos in selectedPositions) {
            level.setBlock(BlockPos(pos.x, pos.y, pos.z), (blockType.createBlockData() as CraftBlockData).state, Block.UPDATE_CLIENTS)
        }
    }
}