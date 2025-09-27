package net.astrorbits.dontdoit.criteria.inspect

import io.papermc.paper.datacomponent.DataComponentTypes
import net.astrorbits.dontdoit.system.GameStateManager
import net.astrorbits.dontdoit.system.generate.GameAreaGenerator
import net.astrorbits.dontdoit.system.team.TeamData
import net.astrorbits.lib.math.vector.Vec3d
import net.astrorbits.lib.math.vector.Vec3i
import net.minecraft.world.level.ClipContext
import org.bukkit.ChunkSnapshot
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.damage.DamageType
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Item
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.math.floor

class InventoryInspectContext(
    val allBlocks: Set<Material>,
    val surroundingBlocks: Set<Material>,
    val selfInventoryItems: Set<Material>,
    val otherInventoryItems: Set<Material>,
    val allGroundItems: Set<Material>,
    val surroundingGroundItems: Set<Material>,
    val allEntities: Set<EntityType>,          // 不包括玩家实体和物品实体
    val surroundingEntities: Set<EntityType>,  // 不包括玩家实体和物品实体
    val availableDamageTypes: Set<DamageType>
) {
    class EntitySnapshot(
        val uuid: UUID,
        val type: EntityType,
        val pos: Vec3d,
        val holdingItems: List<ItemStack>
    )

    companion object {
        const val SURROUNDING_DISTANCE = 8.0   // 切比雪夫距离

        fun createEntitySnapshot(entity: Entity): EntitySnapshot {
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
                entity.uniqueId,
                entity.type,
                Vec3d.fromLocation(entity.location),
                items
            )
        }

        private fun collectItems(items: Array<ItemStack?>): List<ItemStack> {
            return items.filterNotNull().filter { !it.isEmpty }.map { it.clone() }
        }

        fun createContext(world: World, teamData: TeamData, otherTeams: List<TeamData>): CompletableFuture<InventoryInspectContext> {
            if (!GameStateManager.isRunning()) throw IllegalStateException("Cannot create context when game is not started")
            // 主线程收集数据
            val selfMembers = teamData.members.map { createEntitySnapshot(it) }
            val otherMembers = otherTeams.flatMap { it.members }.map { createEntitySnapshot(it) }
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
            val itemEntities = world.getEntitiesByClass(Item::class.java).filter { it.location in area }.map { createEntitySnapshot(it) }
            val allEntities = world.entities.filter { it !is Player && it !is Item && it.location in area }.map { createEntitySnapshot(it) }
            return CompletableFuture.supplyAsync async@{
                for (chunk in chunkSnapshots) {

                }




                return@async
            }
        }

        private fun ChunkSnapshot.isSurrounding(center: Vec3d): Boolean {

        }

        private operator fun ChunkSnapshot.contains(pos: Vec3i): Boolean {
            return pos.x shr 4 == x && pos.z shr 4 == z
        }

        private operator fun ChunkSnapshot.contains(pos: Vec3d): Boolean {
            return contains(pos.floor())
        }

        private operator fun ChunkSnapshot.contains(loc: Location): Boolean {
            return contains(Vec3d.fromLocation(loc))
        }
    }
}
