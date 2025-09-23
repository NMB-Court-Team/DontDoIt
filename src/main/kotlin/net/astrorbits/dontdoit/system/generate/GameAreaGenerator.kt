package net.astrorbits.dontdoit.system.generate

import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.GlobalSettings
import net.astrorbits.lib.NMSWarning
import net.astrorbits.lib.math.vector.Vec3i
import net.astrorbits.lib.math.vector.Vec3i.Companion.getBlock
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import kotlin.math.floor

object GameAreaGenerator : Listener {
    var center: Location? = null
    var groundYLevel: Int? = null

    val STONE_LIKE_BLOCKS: Set<Material> = setOf(
        Material.STONE, Material.ANDESITE, Material.GRANITE, Material.DIORITE, Material.INFESTED_STONE, Material.INFESTED_COBBLESTONE,
        Material.COAL_ORE, Material.IRON_ORE, Material.COPPER_ORE, Material.GOLD_ORE, Material.REDSTONE_ORE,
        Material.EMERALD_ORE, Material.LAPIS_ORE, Material.DIAMOND_ORE,
        Material.DEEPSLATE, Material.TUFF, Material.INFESTED_DEEPSLATE,
        Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_GOLD_ORE,
        Material.DEEPSLATE_REDSTONE_ORE, Material.DEEPSLATE_EMERALD_ORE, Material.DEEPSLATE_LAPIS_ORE, Material.DEEPSLATE_DIAMOND_ORE
    )

    @NMSWarning
    fun generate(generateCenter: Vec3i, world: World) {
        val level = (world as? CraftWorld)?.handle ?: throw IllegalArgumentException("Cannot convert org.bukkit.World to net.minecraft.server.level.ServerLevel")

        val border = world.worldBorder
        val centerLoc = generateCenter.center().toLocation(world)
        border.center = centerLoc
        border.warningDistance = 0
        val size = GlobalSettings.gameAreaSize + 1 - GlobalSettings.gameAreaSize % 2 // 取比gameAreaSize大的最小奇数
        border.size = size.toDouble()
        for (player in Bukkit.getOnlinePlayers()) {
            player.respawnLocation = centerLoc
        }

        center = centerLoc
        groundYLevel = generateCenter.y

        val radius = floor(size / 2.0).toInt()
        val minX = generateCenter.x - radius
        val minZ = generateCenter.z - radius
        val maxX = generateCenter.x + radius
        val maxZ = generateCenter.z + radius

        val bedrockDepth = Configs.BEDROCK_DEPTH.get()
        val bedrockY = generateCenter.y - bedrockDepth

        for (pos in Vec3i(minX, bedrockY, minZ)..Vec3i(maxX, bedrockY, maxZ)) {
            level.setBlock(BlockPos(pos.x, pos.y, pos.z), (Material.BEDROCK.createBlockData() as CraftBlockData).state, Block.UPDATE_CLIENTS)
        }

        val andesiteGeneration = Configs.ANDESITE_GENERATION.get()
        val coalOreGeneration = Configs.COAL_ORE_GENERATION.get()
        val ironOreGeneration = Configs.IRON_ORE_GENERATION.get()
        val diamondOreGeneration = Configs.DIAMOND_ORE_GENERATION.get()

        for (depth in 1 until bedrockDepth) {
            val y = generateCenter.y - depth
            val stones: MutableSet<Vec3i> = mutableSetOf()
            for (pos in Vec3i(minX, y, minZ)..Vec3i(maxX, y, maxZ)) {
                if (world.getBlock(pos).type in STONE_LIKE_BLOCKS) {
                    stones.add(pos)
                }
            }
            val count = stones.size
            andesiteGeneration.generate(depth, count, stones, level)
            coalOreGeneration.generate(depth, count, stones, level)
            ironOreGeneration.generate(depth, count, stones, level)
            diamondOreGeneration.generate(depth, count, stones, level)
        }
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.respawnLocation = center ?: event.player.world.spawnLocation
    }
}