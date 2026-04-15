package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v1.PersistentSignalV1
import com.ac101m.redmon.persistence.v1.PersistentSignalBitV1
import com.ac101m.redmon.utils.red
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.RepeaterBlock
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import java.io.StringWriter

data class Signal(
    val name: String,
    val type: SignalType,
    var invert: Boolean,
    var format: SignalFormat,
    var blockLocations: List<BlockPos> = listOf()
) {
    // Raw state of the signal bits
    private var rawState: ULong = 0UL

    // State taking into account inversion
    private val state: ULong get() {
        val mask = (1UL shl size) - 1UL
        return when (invert) {
            false -> rawState and mask
            true -> rawState.inv() and mask
        }
    }

    val size get() = blockLocations.size
    var missingBits = 0

    fun updateState(world: Level, offset: Vec3i) {
        missingBits = 0
        blockLocations.forEachIndexed { i, position ->
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
            SignalFormat.UNSIGNED -> "$state"
            SignalFormat.SIGNED -> formatSigned()
            SignalFormat.HEX -> formatHex()
            SignalFormat.BINARY -> formatBinary()
        }
    }

    fun invert() {
        invert = !invert
    }

    fun flipBits() {
        blockLocations = blockLocations.reversed()
    }

    fun appendBlocks(newBlockLocations: List<BlockPos>) {
        blockLocations = blockLocations.plus(newBlockLocations)
    }

    fun toPersistentV1(): PersistentSignalV1 {
        val b = blockLocations.map { PersistentSignalBitV1(it.x, it.y, it.z) }
        return PersistentSignalV1(name, type, invert, format, b)
    }

    companion object {
        fun fromPersistentV1(data: PersistentSignalV1): Signal {
            val blockLocations = data.blockLocations.map { BlockPos(it.x, it.y, it.z) }
            return Signal(data.name, data.type, data.invert, data.format, blockLocations)
        }
    }
}
