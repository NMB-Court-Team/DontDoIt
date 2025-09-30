package net.astrorbits.dontdoit.system.generate

import net.astrorbits.dontdoit.Configs
import net.astrorbits.dontdoit.DontDoIt
import net.astrorbits.dontdoit.DynamicSettings
import net.astrorbits.lib.NMSWarning
import net.astrorbits.lib.math.vector.BlockBox
import net.astrorbits.lib.math.vector.Vec3i
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
    private val LOGGER = DontDoIt.LOGGER

    var mainWorld: World? = null
    var centerVec3i: Vec3i? = null
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
    private const val COLLECT_Y_RANGE = 64
    private const val STONE_PERCENTAGE = 0.25
    private const val STONE_SEARCH_MAX_DISTANCE = 30

    @NMSWarning
    fun generate(generateCenter: Vec3i, world: World): Boolean {
        val level = (world as? CraftWorld)?.handle ?: throw IllegalArgumentException("Cannot convert org.bukkit.World to net.minecraft.server.level.ServerLevel")
        if (world.environment != World.Environment.NORMAL) throw IllegalStateException("Generating game area not in overworld is not supported")

        val centerLoc = generateCenter.center().toLocation(world)
        val centerSafeLoc = centerLoc.clone().apply { y = world.getHighestBlockYAt(centerLoc, HeightMap.MOTION_BLOCKING) + 1.0 }
        val radius = calcRadius()
        val minX = generateCenter.x - radius
        val minZ = generateCenter.z - radius
        val maxX = generateCenter.x + radius
        val maxZ = generateCenter.z + radius

        var stoneY: Int? = null

        for (offset in 0 until STONE_SEARCH_MAX_DISTANCE) {
            var stoneCount = 0
            for (pos in Vec3i(minX, generateCenter.y - offset, minZ)..Vec3i(maxX, generateCenter.y - offset, maxZ)) {
                if (world.getType(pos.x, pos.y, pos.z) in STONE_LIKE_BLOCKS) {
                    stoneCount += 1
                }
            }
            if (stoneCount.toDouble() / (DynamicSettings.gameAreaSize * DynamicSettings.gameAreaSize).toDouble() >= STONE_PERCENTAGE) {
                stoneY = generateCenter.y - offset
                break
            }
        }
        if (stoneY == null) return false
        LOGGER.info("Generating game area: stoneY = $stoneY")

        val border = world.worldBorder
        border.center = centerLoc
        border.warningDistance = 0
        val size = calcSize()
        border.size = size.toDouble()

        for (player in Bukkit.getOnlinePlayers()) {
            val playerLoc = player.location.clone()
            player.respawnLocation = centerSafeLoc
            val playerNewLoc = playerLoc.clone().apply {
                x = x.coerceIn(minX + 0.5, maxX - 0.5)
                z = z.coerceIn(minZ + 0.5, maxZ - 0.5)
                y = world.getHighestBlockYAt(Location(world, x, y, z), HeightMap.MOTION_BLOCKING) + 1.0
            }
            player.teleport(playerNewLoc)
        }

        mainWorld = world
        world.difficulty = DynamicSettings.ingameDifficulty
        centerVec3i = generateCenter
        center = centerLoc
        groundYLevel = generateCenter.y

        for (w in Bukkit.getWorlds()) {
            val wBorder = w.worldBorder
            if (world.environment == World.Environment.NORMAL && w.environment == World.Environment.NETHER) {
                wBorder.center = Location(w, floor(centerLoc.x / 8) + 0.5, floor(centerLoc.y / 8) + 0.5, floor(centerLoc.z / 8) + 0.5)
            } else if (world.environment == World.Environment.NETHER && w.environment == World.Environment.NORMAL) {
                wBorder.center = Location(w, floor(centerLoc.x * 8) + 0.5, floor(centerLoc.y * 8) + 0.5, floor(centerLoc.z * 8) + 0.5)
            } else {
                wBorder.center = centerLoc
            }
            wBorder.warningDistance = 0
            wBorder.size = size.toDouble()
            w.difficulty = DynamicSettings.ingameDifficulty
        }

        val bedrockDepth = Configs.BEDROCK_DEPTH.get()
        val bedrockY = stoneY - bedrockDepth

        for (pos in Vec3i(minX, bedrockY, minZ)..Vec3i(maxX, bedrockY, maxZ)) {
            level.setBlock(BlockPos(pos.x, pos.y, pos.z), (Material.BEDROCK.createBlockData() as CraftBlockData).state, Block.UPDATE_CLIENTS)
        }

        val andesiteGeneration = Configs.ANDESITE_GENERATION.get()
        val coalOreGeneration = Configs.COAL_ORE_GENERATION.get()
        val ironOreGeneration = Configs.IRON_ORE_GENERATION.get()
        val diamondOreGeneration = Configs.DIAMOND_ORE_GENERATION.get()

        for (depth in 1 until bedrockDepth) {
            val y = stoneY - depth + 1
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
            for (pos in stones) {
                level.setBlock(BlockPos(pos.x, pos.y, pos.z), (Material.STONE.createBlockData() as CraftBlockData).state, Block.UPDATE_CLIENTS)
            }
        }
        LOGGER.info("Andesites and ores generated")
        return true
    }

    fun calcSize(): Int = DynamicSettings.gameAreaSize + 1 - DynamicSettings.gameAreaSize % 2  // 取比gameAreaSize大的最小奇数
    fun calcRadius(): Int = floor(calcSize() / 2.0).toInt()
    fun getPosRange(): BlockBox? {
        if (centerVec3i == null) return null
        val center = centerVec3i!!
        val radius = calcRadius()
        val minX = center.x - radius
        val minZ = center.z - radius
        val maxX = center.x + radius
        val maxZ = center.z + radius
        val minY = center.y - Configs.BEDROCK_DEPTH.get()
        val maxY = center.y + COLLECT_Y_RANGE
        return BlockBox(minX, minY, minZ, maxX, maxY, maxZ)
    }

    fun onEnterPreparation() {
        for (world in Bukkit.getWorlds()) {
            world.difficulty = Difficulty.PEACEFUL
            world.worldBorder.reset()
        }
        mainWorld = null
        center = null
        groundYLevel = null
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.respawnLocation = center ?: event.player.world.spawnLocation
    }
}