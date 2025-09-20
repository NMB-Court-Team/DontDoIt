package net.astrorbits.lib.math.vector

import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max

fun min(v1: Vec3d, v2: Vec3d): Vec3d {
    return Vec3d(min(v1.x, v2.x), min(v1.y, v2.y), min(v1.z, v2.z))
}

fun min(v1: Vec3i, v2: Vec3i): Vec3i {
    return Vec3i(min(v1.x, v2.x), min(v1.y, v2.y), min(v1.z, v2.z))
}

fun minOf(vararg vectors: Vec3d): Vec3d {
    return Vec3d(vectors.minOf(Vec3d::x), vectors.minOf(Vec3d::y), vectors.minOf(Vec3d::z))
}

fun minOf(vararg vectors: Vec3i): Vec3i {
    return Vec3i(vectors.minOf(Vec3i::x), vectors.minOf(Vec3i::y), vectors.minOf(Vec3i::z))
}

fun max(v1: Vec3d, v2: Vec3d): Vec3d {
    return Vec3d(max(v1.x, v2.x), max(v1.y, v2.y), max(v1.z, v2.z))
}

fun max(v1: Vec3i, v2: Vec3i): Vec3i {
    return Vec3i(max(v1.x, v2.x), max(v1.y, v2.y), max(v1.z, v2.z))
}

fun maxOf(vararg vectors: Vec3d): Vec3d {
    return Vec3d(vectors.maxOf(Vec3d::x), vectors.maxOf(Vec3d::y), vectors.maxOf(Vec3d::z))
}

fun maxOf(vararg vectors: Vec3i): Vec3i {
    return Vec3i(vectors.maxOf(Vec3i::x), vectors.maxOf(Vec3i::y), vectors.maxOf(Vec3i::z))
}

fun abs(v: Vec3d): Vec3d {
    return Vec3d(abs(v.x), abs(v.y), abs(v.z))
}

fun abs(v: Vec3i): Vec3i {
    return Vec3i(abs(v.x), abs(v.y), abs(v.z))
}

fun midpoint(v1: Vec3d, v2: Vec3d): Vec3d {
    return (v1 + v2) / 2.0
}

fun midpoint(v1: Vec3i, v2: Vec3i): Vec3d = midpoint(v1.toVec3d(), v2.toVec3d())