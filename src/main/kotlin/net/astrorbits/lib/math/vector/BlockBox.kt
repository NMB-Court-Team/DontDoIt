package net.astrorbits.lib.math.vector

import io.papermc.paper.math.Position
import net.astrorbits.lib.codec.Codec
import net.astrorbits.lib.codec.ObjectDecoder
import net.astrorbits.lib.codec.ObjectEncoder
import net.astrorbits.lib.codec.SizedCodec
import org.bukkit.Location
import org.bukkit.util.Vector
import org.joml.Vector3L
import org.joml.Vector3d
import org.joml.Vector3f
import org.joml.Vector3i

class BlockBox(val min: Vec3i, val max: Vec3i): Iterable<Vec3i> {
    init {
        require(min.x <= max.x && min.y <= max.y && min.z <= max.z)
    }

    constructor(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int) : this(Vec3i(minX, minY, minZ), Vec3i(maxX, maxY, maxZ))

    val minX: Int
        get() = min.x
    val minY: Int
        get() = min.y
    val minZ: Int
        get() = min.z
    val maxX: Int
        get() = max.x
    val maxY: Int
        get() = max.y
    val maxZ: Int
        get() = max.z
    val center: Vec3d
        get() = midpoint(min, max)
    val size: Vec3i
        get() = max - min + Vec3i.ONE
    val sizeX: Int
        get() = maxX - minX + 1
    val sizeY: Int
        get() = maxY - minY + 1
    val sizeZ: Int
        get() = maxZ - minZ + 1

    /**
     * 将[BlockBox]的大小往六个方向各扩展对应的长度，输入负数则对应收缩
     *
     * **注意: 实际扩展/收缩的长度为对应方向输入值的两倍**
     */
    fun expand(x: Int, y: Int, z: Int): BlockBox {
        return BlockBox(min.subtract(x, y, z), max.add(x, y, z))
    }

    fun offset(x: Int, y: Int, z: Int): BlockBox {
        return BlockBox(min.offset(x, y, z), max.offset(x, y, z))
    }

    fun offset(offset: Vec3i): BlockBox {
        return BlockBox(min.offset(offset), max.offset(offset))
    }

    /**
     * 检查两个[BlockBox]是否相交
     */
    fun intersects(other: BlockBox): Boolean {
        return minX <= other.maxX && maxX >= other.minX && minY <= other.maxY && maxY >= other.minY && minZ <= other.maxZ && maxZ >= other.minZ
    }

    /**
     * 计算同时被两个[BlockBox]包含的最大[BlockBox]
     */
    fun intersection(other: BlockBox): BlockBox {
        return BlockBox(max(min, other.min), min(max, other.max))
    }

    /**
     * 计算同时包含两个[BlockBox]的最小的[BlockBox]
     */
    fun union(other: BlockBox): BlockBox {
        return BlockBox(min(min, other.min), max(max, other.max))
    }

    fun toBox(): Box = Box(min.toVec3d(), max.add(1, 1, 1).toVec3d())

    override fun iterator(): Iterator<Vec3i> = object : Iterator<Vec3i> {
        private var x = min.x
        private var y = min.y
        private var z = min.z

        override fun hasNext(): Boolean = z <= max.z && y <= max.y && x <= max.x

        override fun next(): Vec3i {
            if (!hasNext()) throw NoSuchElementException()
            val pos = Vec3i(x, y, z)

            if (z < max.z) {
                z++
            } else if (y < max.y) {
                z = min.z
                y++
            } else {
                z = min.z
                y = min.y
                x++
            }

            return pos
        }
    }

    operator fun contains(box: Box): Boolean {
        return box.minX >= minX && box.minY >= minY && box.minZ >= minZ
            && box.maxX <= maxX + 1 && box.maxY <= maxY + 1 && box.maxZ <= maxZ + 1
    }

    operator fun contains(box: BlockBox): Boolean {
        return box.minX >= minX && box.minY >= minY && box.minZ >= minZ
            && box.maxX <= maxX && box.maxY <= maxY && box.maxZ <= maxZ
    }

    operator fun contains(pos: Vec3i): Boolean {
        return pos.x >= min.x && pos.y >= min.y && pos.z >= min.z
            && pos.x <= max.x && pos.y <= max.y && pos.z <= max.z
    }

    operator fun contains(pos: Vec3d): Boolean {
        return pos.x >= min.x && pos.y >= min.y && pos.z >= min.z
            && pos.x <= max.x + 1 && pos.y <= max.y + 1 && pos.z <= max.z + 1
    }

    operator fun contains(pos: Vector): Boolean {
        return pos.x >= min.x && pos.y >= min.y && pos.z >= min.z
            && pos.x <= max.x + 1 && pos.y <= max.y + 1 && pos.z <= max.z + 1
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
            && pos.x <= max.x + 1 && pos.y <= max.y + 1 && pos.z <= max.z + 1
    }

    operator fun contains(pos: Vector3d): Boolean {
        return pos.x >= min.x && pos.y >= min.y && pos.z >= min.z
            && pos.x <= max.x + 1 && pos.y <= max.y + 1 && pos.z <= max.z + 1
    }

    operator fun contains(pos: Location): Boolean {
        return pos.x >= min.x && pos.y >= min.y && pos.z >= min.z
            && pos.x <= max.x + 1 && pos.y <= max.y + 1 && pos.z <= max.z + 1
    }

    operator fun contains(pos: Position): Boolean {
        return pos.x() >= min.x && pos.y() >= min.y && pos.z() >= min.z
            && pos.x() <= max.x + 1 && pos.y() <= max.y + 1 && pos.z() <= max.z + 1
    }

    operator fun component1(): Vec3i = min
    operator fun component2(): Vec3i = max

    fun toList(): List<Vec3i> {
        val posList = ArrayList<Vec3i>(sizeX * sizeY * sizeZ)
        for (x in min.x..max.x) {
            for (y in min.y..max.y) {
                for (z in min.z..max.z) {
                    posList.add(Vec3i(x, y, z))
                }
            }
        }
        return posList
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlockBox) return false

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
        return "BlockBox{(${min.toShortString()}) -> (${max.toShortString()})}"
    }

    companion object {
        val CODEC: Codec<BlockBox> = object : SizedCodec<BlockBox>(24) {
            override fun encodeSized(obj: BlockBox): ByteArray = ObjectEncoder(size)
                .putInt(obj.minX).putInt(obj.minY).putInt(obj.minZ)
                .putInt(obj.maxX).putInt(obj.maxY).putInt(obj.maxZ)
                .getData()

            override fun decodeSized(data: ByteArray): BlockBox = ObjectDecoder(data).let { BlockBox(
                it.getInt(), it.getInt(), it.getInt(),
                it.getInt(), it.getInt(), it.getInt()
            ) }
        }

        fun fromString(str: String): BlockBox {
            val coords = str.split(",").map { it.trim().toInt() }
            return BlockBox(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5])
        }

        fun of(corner1: Vec3i, corner2: Vec3i): BlockBox = BlockBox(min(corner1, corner2), max(corner1, corner2))

        fun ofSize(min: Vec3i, size: Vec3i): BlockBox = BlockBox(min, min + size)
        fun ofSize(minX: Int, minY: Int, minZ: Int, sizeX: Int, sizeY: Int, sizeZ: Int): BlockBox = ofSize(Vec3i(minX, minY, minZ), Vec3i(sizeX, sizeY, sizeZ))

        fun unit(pos: Vec3i): BlockBox = BlockBox(pos, pos.offset(Vec3i.ONE))
    }
}