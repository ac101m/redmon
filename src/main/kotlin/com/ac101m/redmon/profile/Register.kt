package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v1.PersistentRegisterV1
import com.ac101m.redmon.persistence.v1.PersistentRegisterBitV1
import com.ac101m.redmon.utils.red
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.RepeaterBlock
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import java.io.StringWriter

data class Register(
    var name: String,
    val type: RegisterType,
    var invert: Boolean,
    var format: RegisterFormat,
    var watchPoints: List<Vec3i> = listOf()
) {
    // Raw state of the register bits
    private var rawState: ULong = 0UL

    // State taking into account inversion
    private val state: ULong get() {
        val mask = (1UL shl size) - 1UL
        return when (invert) {
            false -> rawState and mask
            true -> rawState.inv() and mask
        }
    }

    val size get() = watchPoints.size
    var missingBits = 0

    fun updateState(world: Level, offset: Vec3i) {
        missingBits = 0
        watchPoints.forEachIndexed { i, position ->
            val blockState = world.getBlockState(BlockPos(position.offset(offset)))
            val mask = 1UL shl i
            rawState = rawState and mask.inv()
            if (blockState.block is RepeaterBlock) {
                if (blockState.getValue(BlockStateProperties.POWERED)) {
                    rawState = rawState or mask
                }
            } else {
                missingBits++
            }
        }
    }

    private fun formatSigned(): String {
        val signBitMask = (1UL shl (size - 1))

        val signExtended = if ((state and signBitMask) != 0UL) {
            state or ((1UL shl size) - 1UL).inv()
        } else {
            state
        }

        return "${signExtended.toLong()}"
    }

    private fun formatHex(): String {
        val digitCount = when (size % 4) {
            0 -> size / 4
            else -> (size / 4) + 1
        }

        val hex = state.toString(16).uppercase()
        val sw = StringWriter()

        for (i in hex.length until digitCount) {
            sw.append("0")
        }

        sw.append(hex)

        return "0x$sw"
    }

    private fun formatBinary(): String {
        val digitCount = size

        val bin = state.toString(2).uppercase()
        val sw = StringWriter()

        for (i in bin.length until digitCount) {
            sw.append("0")
        }

        sw.append(bin)

        return "0b$sw"
    }

    fun getRepresentation(): String {
        if (missingBits == size) {
            return "MISSING ALL".red()
        }

        if (missingBits > 0) {
            return "MISSING $missingBits/$size".red()
        }

        return when (format) {
            RegisterFormat.UNSIGNED -> "$state"
            RegisterFormat.SIGNED -> formatSigned()
            RegisterFormat.HEX -> formatHex()
            RegisterFormat.BINARY -> formatBinary()
        }
    }

    fun invert() {
        invert = !invert
    }

    fun flipBits() {
        watchPoints = watchPoints.reversed()
    }

    fun appendBits(bitPositions: List<Vec3i>) {
        watchPoints = watchPoints.plus(bitPositions)
    }

    fun toPersistentV1(): PersistentRegisterV1 {
        val bitLocations = watchPoints.map { PersistentRegisterBitV1(it.x, it.y, it.z) }
        return PersistentRegisterV1(name, type, invert, format, bitLocations)
    }

    companion object {
        fun fromPersistentV1(data: PersistentRegisterV1): Register {
            val watchPoints = data.bitLocations.map { Vec3i(it.x, it.y, it.z) }
            return Register(data.name, data.type, data.invert, data.format, watchPoints)
        }
    }
}
