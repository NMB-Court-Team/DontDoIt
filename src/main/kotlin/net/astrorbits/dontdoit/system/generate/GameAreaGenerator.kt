package net.astrorbits.dontdoit.system.generate

import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.DynamicSettings
import net.astrorbits.lib.NMSWarning
import net.astrorbits.lib.math.vector.Vec3i
import net.astrorbits.lib.task.TaskBuilder
import net.astrorbits.lib.task.TaskHelper
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import org.bukkit.*
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import kotlin.math.floor

object GameAreaGenerator : Listener {
    var world: World? = null
    var center: Location? = null
    var groundYLevel: Int? = null
    var materialsInArea: Set<Material> = setOf()

    val STONE_LIKE_BLOCKS: Set<Material> = setOf(
        Material.STONE, Material.ANDESITE, Material.GRANITE, Material.DIORITE, Material.INFESTED_STONE, Material.INFESTED_COBBLESTONE,
        Material.COAL_ORE, Material.IRON_ORE, Material.COPPER_ORE, Material.GOLD_ORE, Material.REDSTONE_ORE,
        Material.EMERALD_ORE, Material.LAPIS_ORE, Material.DIAMOND_ORE,
        Material.DEEPSLATE, Material.TUFF, Material.INFESTED_DEEPSLATE,
        Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_GOLD_ORE,
        Material.DEEPSLATE_REDSTONE_ORE, Material.DEEPSLATE_EMERALD_ORE, Material.DEEPSLATE_LAPIS_ORE, Material.DEEPSLATE_DIAMOND_ORE
    )
    private const val AIR_THRESHOLD = 10
    private const val COLLECT_Y_RANGE = 64

    @NMSWarning
    fun generate(generateCenter: Vec3i, world: World) {
        val level = (world as? CraftWorld)?.handle ?: throw IllegalArgumentException("Cannot convert org.bukkit.World to net.minecraft.server.level.ServerLevel")

        val border = world.worldBorder
        val centerLoc = generateCenter.center().toLocation(world)
        border.center = centerLoc
        border.warningDistance = 0
        val size = DynamicSettings.gameAreaSize + 1 - DynamicSettings.gameAreaSize % 2 // 取比gameAreaSize大的最小奇数
        border.size = size.toDouble()
        for (player in Bukkit.getOnlinePlayers()) {
            player.respawnLocation = centerLoc
        }

        this.world = world
        world.difficulty = Difficulty.NORMAL
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
                if (world.getType(pos.x, pos.y, pos.z) in STONE_LIKE_BLOCKS) {
                    stones.add(pos)
                }
            }
            val count = stones.size
            andesiteGeneration.generate(depth, count, stones, level)
            coalOreGeneration.generate(depth, count, stones, level)
            ironOreGeneration.generate(depth, count, stones, level)
            diamondOreGeneration.generate(depth, count, stones, level)
        }

        // 统计方块
        collectAllBlockMaterialsAsync(world, Vec3i(minX, bedrockY, minZ), Vec3i(maxX, bedrockY + COLLECT_Y_RANGE, maxZ))
    }

    // GPT给出的在异步线程遍历世界上的方块的方案
    private fun collectAllBlockMaterialsAsync(world: World, from: Vec3i, to: Vec3i) {
        val chunkMinX = from.x shr 4
        val chunkMaxX = to.x shr 4
        val chunkMinZ = from.z shr 4
        val chunkMaxZ = to.z shr 4

        val snapshots = mutableMapOf<Pair<Int, Int>, ChunkSnapshot>()

        for (cx in chunkMinX..chunkMaxX) {
            for (cz in chunkMinZ..chunkMaxZ) {
                val chunk = world.getChunkAt(cx, cz)
                snapshots[cx to cz] = chunk.getChunkSnapshot(true, false, false)
            }
        }

        TaskBuilder(DontDoIt.instance).setAsync()
            .setTask {
                val materials = mutableSetOf<Material>()
                val allPossible = Material.entries.size

                outer@for (x in from.x..to.x) {
                    for (z in from.z..to.z) {
                        val snapshot = snapshots[(x shr 4) to (z shr 4)] ?: continue
                        var airCount = 0
                        for (y in from.y..to.y) {
                            val type = snapshot.getBlockType(x and 15, y, z and 15)

                            if (type.isAir) {
                                airCount++
                                if (airCount >= AIR_THRESHOLD) break
                            } else {
                                airCount = 0
                                materials.add(type)
                                if (materials.size == allPossible) {
                                    break@outer
                                }
                            }
                        }
                    }
                }

                TaskHelper.runForceSync(DontDoIt.instance) {
                    materialsInArea = materials
                }
            }.runTask()
    }

    fun onEnterPreparation() {
        world?.difficulty = Difficulty.PEACEFUL
        world?.worldBorder?.reset()
        world = null
        center = null
        groundYLevel = null
        materialsInArea = setOf()
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.respawnLocation = center ?: event.player.world.spawnLocation
    }
}