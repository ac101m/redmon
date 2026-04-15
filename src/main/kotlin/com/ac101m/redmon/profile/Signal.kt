package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v1.PersistentSignalV1
import com.ac101m.redmon.persistence.v1.PersistentSignalBitV1
import com.ac101m.redmon.utils.gray
import com.ac101m.redmon.utils.red
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.level.Level

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
        val mask = computeMask(bitCount)
        return when (invert) {
            false -> rawState and mask
            true -> rawState.inv() and mask
        }
    }

    private val bitCount get() = blockLocations.size * type.bitsPerBlock
    var missingBits = 0

    init {
        require(blockLocations.size <= type.maxBlocks) {
            "Too many blocks. Signals of type $type may contain at most ${type.maxBlocks} blocks"
        }
    }

    fun updateState(level: Level, offset: Vec3i) {
        var state = 0UL
        val mask = computeMask(type.bitsPerBlock)
        var shift = 0
        var missing = 0

        for (location in blockLocations) {
            val position = location.offset(offset)
            val blockBits = type.getBitsFromBlockLocation(level, position)

            if (blockBits != null) {
                state = state and (mask shl shift).inv()
                state = state or ((blockBits and mask) shl shift)
            } else {
                missing += type.bitsPerBlock
            }

            shift += type.bitsPerBlock
        }

        rawState = state
        missingBits = missing
    }

    fun getRepresentation(): String {
        if (bitCount == 0) {
            return "EMPTY".gray()
        }

        if (missingBits == bitCount) {
            return "MISSING ALL".red()
        }

        if (missingBits > 0) {
            return "MISSING $missingBits/$bitCount".red()
        }

        return format.getRepresentation(state, bitCount)
    }

    fun invert() {
        invert = !invert
    }

    fun flipBits() {
        blockLocations = blockLocations.reversed()
    }

    fun appendBlocks(newBlockLocations: List<BlockPos>) {
        require(newBlockLocations.size + blockLocations.size <= type.maxBlocks) {
            "Too many blocks. Signals of type $type may contain at most ${type.maxBlocks} blocks"
        }
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

        fun computeMask(bitCount: Int): ULong {
            return if (bitCount < 64) {
                (1UL shl bitCount) - 1UL
            } else {
                ULong.MAX_VALUE
            }
        }
    }
}
