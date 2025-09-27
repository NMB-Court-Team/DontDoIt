package net.astrorbits.lib.math.vector

import io.papermc.paper.math.Position
import net.astrorbits.lib.codec.Codec
import net.astrorbits.lib.codec.ObjectDecoder
import net.astrorbits.lib.codec.ObjectEncoder
import net.astrorbits.lib.codec.SizedCodec
import org.bukkit.Location
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import org.joml.Vector3L
import org.joml.Vector3d
import org.joml.Vector3f
import org.joml.Vector3i

class Box(val min: Vec3d, val max: Vec3d) {
    init {
        require(min.x <= max.x && min.y <= max.y && min.z <= max.z)
    }

    constructor(min: Vec3i, max: Vec3i) : this(min.toVec3d(), max.add(Vec3i.ONE).toVec3d())
    constructor(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double) : this(Vec3d(minX, minY, minZ), Vec3d(maxX, maxY, maxZ))

    val minX: Double
        get() = min.x
    val minY: Double
        get() = min.y
    val minZ: Double
        get() = min.z
    val maxX: Double
        get() = max.x
    val maxY: Double
        get() = max.y
    val maxZ: Double
        get() = max.z
    val center: Vec3d
        get() = midpoint(min, max)
    val size: Vec3d
        get() = max - min
    val sizeX: Double
        get() = maxX - minX
    val sizeY: Double
        get() = maxY - minY
    val sizeZ: Double
        get() = maxZ - minZ

    /**
     * 将[Box]的大小往六个方向各扩展对应的长度，输入负数则对应收缩
     *
     * **注意: 实际扩展/收缩的长度为对应方向输入值的两倍**
     */
    fun expand(x: Double, y: Double, z: Double): Box {
        return Box(min.subtract(x, y, z), max.add(x, y, z))
    }

    fun offset(x: Double, y: Double, z: Double): Box {
        return Box(min.offset(x, y, z), max.offset(x, y, z))
    }

    fun offset(offset: Vec3d): Box {
        return Box(min.offset(offset), max.offset(offset))
    }

    fun offset(offset: Vec3i): Box {
        return offset(offset.toVec3d())
    }

    /**
     * 检查两个[Box]是否相交
     */
    fun intersects(other: Box): Boolean {
        return minX <= other.maxX && maxX >= other.minX && minY <= other.maxY && maxY >= other.minY && minZ <= other.maxZ && maxZ >= other.minZ
    }

    /**
     * 计算同时被两个[Box]包含的最大[Box]
     */
    fun intersection(other: Box): Box {
        return Box(max(min, other.min), min(max, other.max))
    }

    /**
     * 计算同时包含两个[Box]的最小的[Box]
     */
    fun union(other: Box): Box {
        return Box(min(min, other.min), max(max, other.max))
    }

    /**
     * 计算射线与Box的交点距离射线起点的距离
     * @return 如果射线与Box不相交，则返回-1.0
     */
    fun calcRayIntersectDistance(start: Vec3d, direction: Vec3d): Double {
        // 计算射线方向的倒数，避免除法运算
        val invDirX = if (direction.x == 0.0) Double.MAX_VALUE else 1.0 / direction.x
        val invDirY = if (direction.y == 0.0) Double.MAX_VALUE else 1.0 / direction.y
        val invDirZ = if (direction.z == 0.0) Double.MAX_VALUE else 1.0 / direction.z

        // 计算与各个面的交点参数t
        var t1 = (minX - start.x) * invDirX
        var t2 = (maxX - start.x) * invDirX
        var t3 = (minY - start.y) * invDirY
        var t4 = (maxY - start.y) * invDirY
        var t5 = (minZ - start.z) * invDirZ
        var t6 = (maxZ - start.z) * invDirZ

        // 确保t1 <= t2, t3 <= t4, t5 <= t6
        if (t1 > t2) {
            val temp = t1
            t1 = t2
            t2 = temp
        }
        if (t3 > t4) {
            val temp = t3
            t3 = t4
            t4 = temp
        }
        if (t5 > t6) {
            val temp = t5
            t5 = t6
            t6 = temp
        }

        // 计算进入和离开Box的参数
        val tNear = maxOf(t1, t3, t5)
        val tFar = minOf(t2, t4, t6)

        // 检查是否相交
        // 如果tFar < 0，Box在射线后方
        // 如果tNear > tFar，射线错过了Box
        if (tFar < 0.0 || tNear > tFar) {
            return -1.0
        }

        // 返回第一个交点的距离
        // 如果tNear >= 0，射线起点在Box外部，返回tNear
        // 如果tNear < 0，射线起点在Box内部，返回0（或tFar，取决于需求）
        return if (tNear >= 0.0) tNear else 0.0
    }

    /**
     * 获取射线与Box交点的详细信息
     * @return Pair<距离, 交点坐标>，如果不相交则返回null
     */
    fun getRayIntersectDetails(start: Vec3d, direction: Vec3d): Pair<Double, Vec3d>? {
        val distance = calcRayIntersectDistance(start, direction)

        if (distance < 0.0) {
            return null
        }

        val intersectionPoint = Vec3d(
            start.x + direction.x * distance,
            start.y + direction.y * distance,
            start.z + direction.z * distance
        )

        return Pair(distance, intersectionPoint)
    }

    operator fun contains(box: Box): Boolean {
        return box.minX >= minX && box.minY >= minY && box.minZ >= minZ
            && box.maxX <= maxX && box.maxY <= maxY && box.maxZ <= maxZ
    }

    operator fun contains(box: BlockBox): Boolean {
        return box.minX >= minX && box.minY >= minY && box.minZ >= minZ
            && box.maxX + 1 <= maxX && box.maxY + 1 <= maxY && box.maxZ + 1 <= maxZ
    }

    operator fun contains(pos: Vec3d): Boolean {
        return pos.x >= min.x && pos.y >= min.y && pos.z >= min.z
            && pos.x <= max.x && pos.y <= max.y && pos.z <= max.z
    }

    operator fun contains(pos: Vec3i): Boolean {
        return pos.x >= min.x && pos.y >= min.y && pos.z >= min.z
            && pos.x <= max.x && pos.y <= max.y && pos.z <= max.z
    }

    operator fun contains(pos: Vector): Boolean {
        return pos.x >= min.x && pos.y >= min.y && pos.z >= min.z
            && pos.x <= max.x && pos.y <= max.y && pos.z <= max.z
    }

    operator fun contains(pos: Vector3i): Boolean {
        return pos.x >= min.x && pos.y >= min.y && pos.z >= min.z
            && pos.x <= max.x && pos.y <= max.y && pos.z <= max.z
    }

    operator fun contains(pos: Vector3L): Boolean {
        return pos.x >= min.x && pos.y >= min.y && pos.z >= min.z
            && pos.x <= max.x && pos.y <= max.y && pos.z <= max.z
    }

    operator fun contains(pos: Vector3f): Boolean {
        return pos.x >= min.x && pos.y >= min.y && pos.z >= min.z
            && pos.x <= max.x && pos.y <= max.y && pos.z <= max.z
    }

    operator fun contains(pos: Vector3d): Boolean {
        return pos.x >= min.x && pos.y >= min.y && pos.z >= min.z
            && pos.x <= max.x && pos.y <= max.y && pos.z <= max.z
    }

    operator fun contains(pos: Location): Boolean {
        return pos.x >= min.x && pos.y >= min.y && pos.z >= min.z
            && pos.x <= max.x && pos.y <= max.y && pos.z <= max.z
    }

    operator fun contains(pos: Position): Boolean {
        return pos.x() >= min.x && pos.y() >= min.y && pos.z() >= min.z
            && pos.x() <= max.x && pos.y() <= max.y && pos.z() <= max.z
    }

    operator fun component1(): Vec3d = min
    operator fun component2(): Vec3d = max

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Box) return false

        if (min != other.min) return false
        if (max != other.max) return false

        return true
    }

    override fun hashCode(): Int {
        var result = min.hashCode()
        result = 31 * result + max.hashCode()
        return result
    }

    override fun toString(): String {
        return "Box{(${min.toShortString()}) -> (${max.toShortString()})}"
    }

    companion object {
        val CODEC: Codec<Box> = object : SizedCodec<Box>(48) {
            override fun encodeSized(obj: Box): ByteArray = ObjectEncoder(size)
                .putDouble(obj.minX).putDouble(obj.minY).putDouble(obj.minZ)
                .putDouble(obj.maxX).putDouble(obj.maxY).putDouble(obj.maxZ)
                .getData()

            override fun decodeSized(data: ByteArray): Box = ObjectDecoder(data).let { Box(
                it.getDouble(), it.getDouble(), it.getDouble(),
                it.getDouble(), it.getDouble(), it.getDouble()
            ) }
        }

        fun fromString(str: String): Box {
            val coords = str.split(",").map { it.trim().toDouble() }
            return Box(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5])
        }

        fun of(corner1: Vec3d, corner2: Vec3d): Box = Box(min(corner1, corner2), max(corner1, corner2))

        fun ofSize(min: Vec3d, size: Vec3d): Box = Box(min, min + size)
        fun ofSize(minX: Double, minY: Double, minZ: Double, sizeX: Double, sizeY: Double, sizeZ: Double): Box = ofSize(Vec3d(minX, minY, minZ), Vec3d(sizeX, sizeY, sizeZ))

        fun unit(pos: Vec3i): Box = Box(pos.toVec3d(), pos.offset(Vec3i.ONE).toVec3d())
        fun unit(pos: Vec3d): Box = Box(pos, pos.offset(Vec3d.ONE))

        /**
         * 计算射线与Box列表的第一个交点
         * @return 第一个相交的Box的索引，如果没有相交则返回-1
         */
        fun findFirstIntersectingBox(boxes: List<Box>, start: Vec3d, direction: Vec3d): Int {
            var closestIndex = -1
            var closestDistance = Double.MAX_VALUE

            for (i in boxes.indices) {
                val box = boxes[i]
                val distance = box.calcRayIntersectDistance(start, direction)

                // 如果有交点且距离更近
                if (distance >= 0.0 && distance < closestDistance) {
                    closestDistance = distance
                    closestIndex = i
                }
            }

            return closestIndex
        }

        fun fromBoundingBox(box: BoundingBox): Box {
            return Box(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)
        }
    }
}