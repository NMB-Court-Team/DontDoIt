package net.astrorbits.dontdoit.criteria.inspect

import io.papermc.paper.datacomponent.DataComponentTypes
import net.astrorbits.dontdoit.system.GameStateManager
import net.astrorbits.dontdoit.system.generate.GameAreaGenerator
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.math.vector.Box
import net.astrorbits.lib.math.vector.Vec3d
import org.bukkit.ChunkSnapshot
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.World
import org.bukkit.damage.DamageType
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture
import kotlin.math.floor

/**
 * 仓检上下文
 * @param allBlocks 游戏区域内所有方块的类型
 * @param surroundingBlocks 附近所有方块的类型
 * @param selfInventoryItems 自己队伍的成员背包里的物品类型
 * @param otherInventoryItems 其他队伍的成员背包里的物品类型
 * @param allGroundItems 地面上所有物品实体的物品类型
 * @param surroundingGroundItems 附近的物品实体的物品类型
 * @param allEntities 区域内所有实体的类型
 * @param surroundingEntities 附近所有实体的类型
 * @param availableDamageTypes 区域内所有可用的伤害类型
 */
class InventoryInspectContext(
    val allBlocks: Set<Material>,
    val surroundingBlocks: Set<Material>,
    val selfInventoryItems: Set<Material>,
    val otherInventoryItems: Set<Material>,
    val allGroundItems: Set<Material>,
    val surroundingGroundItems: Set<Material>,
    val allEntities: Set<EntityType>,
    val surroundingEntities: Set<EntityType>,
    val availableDamageTypes: Set<DamageType>
) {
    private class EntitySnapshot(
        val type: EntityType,
        val pos: Vec3d,
        val holdingItems: List<ItemStack>
    )

    companion object {
        val EMPTY: InventoryInspectContext = InventoryInspectContext(emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet(), emptySet())

        const val SURROUNDING_DISTANCE = 8.0   // 切比雪夫距离

        private fun createEntitySnapshot(entity: Entity): EntitySnapshot {
            val items = mutableListOf<ItemStack>()
            if (entity is InventoryHolder) {
                items.addAll(collectItems(entity.inventory.contents))
            }
            if (entity is LivingEntity) {
                val armorContents = entity.equipment?.armorContents
                if (armorContents != null) {
                    items.addAll(collectItems(armorContents))
                }
            }
            if (entity is Item) {
                items.add(entity.itemStack.clone())
            }
            if (entity is ItemFrame) {
                items.add(entity.item.clone())
            }
            return EntitySnapshot(
                entity.type,
                Vec3d.fromLocation(entity.location),
                items
            )
        }

        private fun collectItems(items: Array<ItemStack?>): List<ItemStack> {
            return items.filterNotNull().filter { !it.isEmpty }.map { it.clone() }
        }

        fun calcContextAsync(world: World, teamData: TeamData, otherTeams: List<TeamData>): CompletableFuture<InventoryInspectContext> {
            if (!GameStateManager.isRunning()) throw IllegalStateException("Cannot create context when game is not started")
            // 主线程收集数据
            val selfMemberSnapshots = teamData.members.map { createEntitySnapshot(it) }
            val otherMemberSnapshots = otherTeams.flatMap { it.members }.map { createEntitySnapshot(it) }

            val area = GameAreaGenerator.getPosRange() ?: throw IllegalStateException("Cannot create context when game is not started")
            val (minPos, maxPos) = area
            val minChunkX = minPos.x shr 4
            val minChunkZ = minPos.z shr 4
            val maxChunkX = maxPos.x shr 4
            val maxChunkZ = maxPos.z shr 4
            val mutableChunkSnapshots: MutableList<ChunkSnapshot> = mutableListOf()
            for (chunkX in minChunkX..maxChunkX) {
                for (chunkZ in minChunkZ..maxChunkZ) {
                    mutableChunkSnapshots.add(world.getChunkAt(chunkX, chunkZ).chunkSnapshot)
                }
            }
            val chunkSnapshots = mutableChunkSnapshots.toList()
            mutableChunkSnapshots.clear()
            val areaMinY = minPos.y
            val areaMaxY = maxPos.y

            val itemEntitySnapshots = world.getEntitiesByClass(Item::class.java).filter { it.location in area }.map { createEntitySnapshot(it) }
            val allEntitySnapshots = world.entities.filter { it.location in area }.map { createEntitySnapshot(it) }

            val isThunderWeather = world.isThundering

            return CompletableFuture.supplyAsync async@{
                val allBlocks: MutableSet<Material> = mutableSetOf()
                for (chunk in chunkSnapshots) {
                    for (x in 0..15) {
                        for (z in 0..15) {
                            for (y in areaMinY..areaMaxY) {
                                allBlocks.add(chunk.getBlockType(x, y, z))
                            }
                        }
                    }
                }

                val surroundingBlocks: MutableSet<Material> = mutableSetOf()
                val surroundingAreas: MutableSet<Box> = mutableSetOf()
                for (member in selfMemberSnapshots) {
                    val pos = member.pos
                    val minX = floor(pos.x - SURROUNDING_DISTANCE).toInt()
                    val maxX = floor(pos.x + SURROUNDING_DISTANCE).toInt()
                    val minY = floor(pos.y - SURROUNDING_DISTANCE).toInt()
                    val maxY = floor(pos.y + SURROUNDING_DISTANCE).toInt()
                    val minZ = floor(pos.z - SURROUNDING_DISTANCE).toInt()
                    val maxZ = floor(pos.z + SURROUNDING_DISTANCE).toInt()
                    for (x in minX..maxX) {
                        for (z in minZ..maxZ) {
                            val chunk = chunkSnapshots.firstOrNull { x shr 4 == it.x && z shr 4 == it.z } ?: continue
                            val inChunkX = x and 15
                            val inChunkZ = z and 15
                            for (y in minY..maxY) {
                                surroundingBlocks.add(chunk.getBlockType(inChunkX, y, inChunkZ))
                            }
                        }
                    }
                    surroundingAreas.add(Box(
                        pos.offset(-SURROUNDING_DISTANCE, -SURROUNDING_DISTANCE, -SURROUNDING_DISTANCE),
                        pos.offset(SURROUNDING_DISTANCE, SURROUNDING_DISTANCE, SURROUNDING_DISTANCE)
                    ))
                }

                val selfInventoryItems: MutableSet<Material> = mutableSetOf()
                for (member in selfMemberSnapshots) {
                    selfInventoryItems.addAll(member.holdingItems.flatMap { collectMaterials(it) })
                }

                val otherInventoryItems: MutableSet<Material> = mutableSetOf()
                for (member in otherMemberSnapshots) {
                    otherInventoryItems.addAll(member.holdingItems.flatMap { collectMaterials(it) })
                }

                val allGroundItems: MutableSet<Material> = mutableSetOf()
                val surroundingGroundItems: MutableSet<Material> = mutableSetOf()
                for (itemEntity in itemEntitySnapshots) {
                    val items = itemEntity.holdingItems.map { it.type }.filter { !it.isAir }
                    allGroundItems.addAll(items)
                    if (surroundingAreas.any { itemEntity.pos in it }) {
                        surroundingGroundItems.addAll(items)
                    }
                }

                val allItems = selfInventoryItems + otherInventoryItems + allGroundItems + surroundingGroundItems

                val allEntities = allEntitySnapshots.map { it.type }.toSet()
                val surroundingEntities = allEntitySnapshots.filter { entity -> surroundingAreas.any { entity.pos in it } }.map { it.type }.toSet()

                val availableDamageTypes: MutableSet<DamageType> = mutableSetOf()
                // arrow
                if (allEntities.any { it in Tag.ENTITY_TYPES_SKELETONS.values } ||
                    ((allItems.contains(Material.BOW) || allItems.contains(Material.CROSSBOW)) &&
                        (allItems.contains(Material.ARROW) || allItems.contains(Material.SPECTRAL_ARROW)))
                ) availableDamageTypes.add(DamageType.ARROW)
                // bad respawn point
                if (allBlocks.contains(Material.NETHER_PORTAL) &&
                    (allBlocks.any { it in Tag.WOOL.values || it in Tag.BEDS.values || it == Material.CRYING_OBSIDIAN || it == Material.RESPAWN_ANCHOR } ||
                        allItems.any { it in Tag.WOOL.values || it in Tag.ITEMS_BEDS.values || it == Material.CRYING_OBSIDIAN || it == Material.RESPAWN_ANCHOR })
                ) availableDamageTypes.add(DamageType.BAD_RESPAWN_POINT)
                // cactus
                if (allBlocks.contains(Material.CACTUS) || allItems.contains(Material.CACTUS)) {
                    availableDamageTypes.add(DamageType.CACTUS)
                }
                // campfire
                if (allBlocks.any { it in Tag.CAMPFIRES.values } || allItems.any { it in Tag.CAMPFIRES.values }) {
                    availableDamageTypes.add(DamageType.CAMPFIRE)
                }
                // cramming, dragon_breath 不触发
                // drown
                if (allBlocks.contains(Material.WATER)) {
                    availableDamageTypes.add(DamageType.DROWN)
                }
                // dry_out 不触发
                // ender_pearl
                if (allItems.contains(Material.ENDER_PEARL) || allEntities.contains(EntityType.ENDERMAN)) {
                    availableDamageTypes.add(DamageType.ENDER_PEARL)
                }
                // explosion
                if (allBlocks.contains(Material.TNT) ||
                    allItems.any { it == Material.TNT || it == Material.TNT_MINECART } ||
                    allEntities.any { it == EntityType.TNT || it == EntityType.TNT_MINECART }
                ) availableDamageTypes.add(DamageType.EXPLOSION)
                // fall
                availableDamageTypes.add(DamageType.FALL)
                // falling_anvil
                if (allBlocks.any { it in Tag.ANVIL.values } || allItems.any { it in Tag.ITEMS_ANVIL.values }) {
                    availableDamageTypes.add(DamageType.FALLING_ANVIL)
                }
                // falling_block 不触发
                // falling_stalactite, stalagmite
                if (allBlocks.contains(Material.POINTED_DRIPSTONE) || allItems.contains(Material.POINTED_DRIPSTONE)) {
                    availableDamageTypes.add(DamageType.FALLING_STALACTITE)
                    availableDamageTypes.add(DamageType.STALAGMITE)
                }
                // fireball
                if (allEntities.any { it == EntityType.GHAST || it == EntityType.BLAZE }) {
                    availableDamageTypes.add(DamageType.FIREBALL)
                }
                // fireworks
                if (allItems.contains(Material.FIREWORK_ROCKET)) {
                    availableDamageTypes.add(DamageType.FIREWORKS)
                }
                // fly_into_wall
                if (allItems.contains(Material.ELYTRA)) {
                    availableDamageTypes.add(DamageType.FLY_INTO_WALL)
                }
                // freeze
                if (allBlocks.any { it == Material.POWDER_SNOW || it == Material.POWDER_SNOW_CAULDRON } || allItems.contains(Material.POWDER_SNOW_BUCKET)) {
                    availableDamageTypes.add(DamageType.FREEZE)
                }
                // generic, generic_kill 不触发
                // hot_floor
                if (allBlocks.contains(Material.MAGMA_BLOCK) || allItems.contains(Material.MAGMA_BLOCK)) {
                    availableDamageTypes.add(DamageType.HOT_FLOOR)
                }
                // in_fire
                if (allBlocks.any { it == Material.FIRE || it == Material.GRAVEL } ||
                    allItems.any { it == Material.FLINT_AND_STEEL || it == Material.FLINT }
                ) availableDamageTypes.add(DamageType.IN_FIRE)
                // in_wall
                availableDamageTypes.add(DamageType.IN_WALL)
                // indirect_magic 不触发
                // lava
                if (allBlocks.any { it == Material.LAVA || it == Material.LAVA_CAULDRON } || allItems.contains(Material.LAVA_BUCKET)) {
                    availableDamageTypes.add(DamageType.LAVA)
                }
                // lightning_bolt
                if (isThunderWeather) {
                    availableDamageTypes.add(DamageType.LIGHTNING_BOLT)
                }
                // magic 不触发
                // mace_smash
                if (allItems.contains(Material.MACE)) {
                    availableDamageTypes.add(DamageType.MACE_SMASH)
                }
                // mob_attack
                availableDamageTypes.add(DamageType.MOB_ATTACK)
                // mob_attack_no_aggro
                if (allEntities.contains(EntityType.GOAT)) {
                    availableDamageTypes.add(DamageType.MOB_ATTACK_NO_AGGRO)
                }
                // mob_projectile
                if (allEntities.any { it == EntityType.SHULKER || it == EntityType.SHULKER_BULLET }) {
                    availableDamageTypes.add(DamageType.MOB_PROJECTILE)
                }
                // on_fire
                if (availableDamageTypes.any { it == DamageType.FIREBALL || it == DamageType.IN_FIRE || it == DamageType.LAVA }) {
                    availableDamageTypes.add(DamageType.ON_FIRE)
                }
                // out_of_world 不触发
                // player_attack
                availableDamageTypes.add(DamageType.PLAYER_ATTACK)
                // player_explosion
                if (availableDamageTypes.contains(DamageType.EXPLOSION) || allEntities.contains(EntityType.CREEPER)) {
                    availableDamageTypes.add(DamageType.PLAYER_EXPLOSION)
                }
                // sonic_boom
                if (allEntities.contains(EntityType.WARDEN)) {
                    availableDamageTypes.add(DamageType.SONIC_BOOM)
                }
                // spit
                if (allEntities.any { it == EntityType.LLAMA || it == EntityType.TRADER_LLAMA }) {
                    availableDamageTypes.add(DamageType.SPIT)
                }
                // stalagmite 前面添加过了
                // starve
                availableDamageTypes.add(DamageType.STARVE)
                // sting
                if (allEntities.contains(EntityType.BEE) || allBlocks.contains(Material.BEEHIVE)) {
                    availableDamageTypes.add(DamageType.STING)
                }
                // sweet_berry_bush
                if (allBlocks.contains(Material.SWEET_BERRY_BUSH) || allItems.contains(Material.SWEET_BERRIES)) {
                    availableDamageTypes.add(DamageType.SWEET_BERRY_BUSH)
                }
                // thorns
                if (allEntities.any { it == EntityType.GUARDIAN || it == EntityType.ELDER_GUARDIAN }) {
                    availableDamageTypes.add(DamageType.THORNS)
                }
                // thrown
                if (availableDamageTypes.contains(DamageType.ENDER_PEARL) ||
                    allBlocks.any { it == Material.SNOW_BLOCK || it == Material.SNOW } ||
                    allItems.any { it == Material.SNOW || it == Material.SNOW_BLOCK || it == Material.SNOWBALL || it == Material.EGG } ||
                    allEntities.contains(EntityType.CHICKEN)
                ) availableDamageTypes.add(DamageType.THROWN)
                // trident
                if (allItems.contains(Material.TRIDENT) || allEntities.any { it == EntityType.DROWNED }) {
                    availableDamageTypes.add(DamageType.TRIDENT)
                }
                // unattributed_fireball 不触发
                // wind_charge
                if (allItems.contains(Material.WIND_CHARGE) || allEntities.contains(EntityType.BREEZE)) {
                    availableDamageTypes.add(DamageType.WIND_CHARGE)
                }
                // wither
                if (allBlocks.contains(Material.WITHER_ROSE) ||
                    allItems.contains(Material.WITHER_ROSE) ||
                    allEntities.any { it == EntityType.WITHER_SKELETON || it == EntityType.WITHER }
                ) availableDamageTypes.add(DamageType.WITHER)
                // wither_skull
                if (allEntities.contains(EntityType.WITHER)) {
                    availableDamageTypes.add(DamageType.WITHER_SKULL)
                }

                return@async InventoryInspectContext(
                    allBlocks,
                    surroundingBlocks,
                    selfInventoryItems,
                    otherInventoryItems,
                    allGroundItems,
                    surroundingGroundItems,
                    allEntities,
                    surroundingEntities,
                    availableDamageTypes
                )
            }
        }

        @Suppress("UnstableApiUsage")
        private fun collectMaterials(item: ItemStack): Set<Material> {
            val materials: MutableSet<Material> = mutableSetOf()
            materials.add(item.type)
            val container = item.getData(DataComponentTypes.CONTAINER)
            if (container != null) {
                materials.addAll(container.contents().flatMap { collectMaterials(it) })
            }
            val bundleContents = item.getData(DataComponentTypes.BUNDLE_CONTENTS)
            if (bundleContents != null) {
                materials.addAll(bundleContents.contents().flatMap { collectMaterials(it) })
            }
            return materials
        }
    }
}
