package com.ac101m.redmon.utils

import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

enum class CardinalDirection(val vector: Vec3i) {
    POSITIVE_X(Vec3i(1, 0, 0)),
    NEGATIVE_X(Vec3i(-1, 0, 0)),
    POSITIVE_Y(Vec3i(0, 1, 0)),
    NEGATIVE_Y(Vec3i(0, -1, 0)),
    POSITIVE_Z(Vec3i(0, 0, 1)),
    NEGATIVE_Z(Vec3i(0, 0, -1));

    companion object {
        fun fromLook(look: Vec3): CardinalDirection {
            val xAbs = abs(look.x)
            val yAbs = abs(look.y)
            val zAbs = abs(look.z)
            return if (xAbs >= yAbs && xAbs >= zAbs) {
                if (look.x < 0) NEGATIVE_X else POSITIVE_X
            } else if (yAbs >= xAbs && yAbs >= zAbs) {
                if (look.y < 0) NEGATIVE_Y else POSITIVE_Y
            } else {
                if (look.z < 0) NEGATIVE_Z else POSITIVE_Z
            }
        }
    }
}
