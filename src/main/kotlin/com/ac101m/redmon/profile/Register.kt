package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v1.PersistentRegisterV1
import com.ac101m.redmon.persistence.v1.PersistentRegisterBitV1
import com.ac101m.redmon.utils.red
import net.minecraft.block.AbstractRedstoneGateBlock
import net.minecraft.block.RepeaterBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import java.io.StringWriter


data class Register(
    var name: String,
    val type: RegisterType,
    var invert: Boolean,
    var format: RegisterFormat,
    var watchPoints: List<Vec3i> = listOf()
) {
    private var state: ULong = 0UL

    val size get() = watchPoints.size

    var missingBits = 0


    companion object {
        fun fromPersistent(data: PersistentRegisterV1): Register {
            return Register(
                data.name,
                data.type,
                data.invert,
                data.format,
                data.bitLocations.map { Vec3i(it.x, it.y, it.z) }
            )
        }
    }


    fun toPersistent(): PersistentRegisterV1 {
        return PersistentRegisterV1(
            name,
            type,
            invert,
            format,
            watchPoints.map { PersistentRegisterBitV1(it.x, it.y, it.z) }
        )
    }


    fun updateState(world: World, offset: Vec3i) {
        missingBits = 0
        watchPoints.forEachIndexed { i, position ->
            val blockState = world.getBlockState(BlockPos(position.add(offset)))
            val mask = 1UL shl i
            state = state and mask.inv()
            if (blockState.block is RepeaterBlock) {
                if (blockState.get(AbstractRedstoneGateBlock.POWERED)) {
                    state = state or mask
                }
            } else {
                missingBits++
            }
        }
    }


    fun getState(): ULong {
        val mask = (1UL shl size) - 1UL
        return when (invert) {
            false -> state and mask
            true -> state.inv() and mask
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
        watchPoints = List(watchPoints.size) { i ->
            watchPoints[(size - 1) - i]
        }
    }


    fun appendBits(positionsToAppend: List<Vec3i>) {
        watchPoints = watchPoints.plus(positionsToAppend)
    }
}
