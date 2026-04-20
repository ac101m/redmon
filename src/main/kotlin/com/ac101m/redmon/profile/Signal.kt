package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v2.PersistentBlockV2
import com.ac101m.redmon.persistence.v2.PersistentSignalV2
import com.ac101m.redmon.utils.Colour
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.level.Level

data class Signal(
    var name: String,
    val type: SignalType,
    var invert: Boolean,
    var format: SignalFormat,
    var blocks: List<BlockPos> = listOf()
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

    private val bitCount get() = blocks.size * type.bitsPerBlock
    var missingBits = 0

    init {
        require(blocks.size <= type.maxBlocks) {
            "Too many blocks. Signals of type $type may contain at most ${type.maxBlocks} blocks"
        }
    }

    fun updateState(level: Level, offset: Vec3i) {
        var state = 0UL
        val mask = computeMask(type.bitsPerBlock)
        var shift = 0
        var missing = 0

        for (location in blocks) {
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
            return "${Colour.GRAY.prefix}EMPTY"
        }

        if (missingBits == bitCount) {
            return "${Colour.RED.prefix}MISSING ALL"
        }

        if (missingBits > 0) {
            return "${Colour.GOLD.prefix}MISSING $missingBits/$bitCount"
        }

        return format.getRepresentation(state, bitCount)
    }

    fun invert() {
        invert = !invert
    }

    fun flipBits() {
        blocks = blocks.reversed()
    }

    fun appendBlocks(newBlocks: List<BlockPos>) {
        require(newBlocks.size + blocks.size <= type.maxBlocks) {
            "Too many blocks. Signals of type $type may contain at most ${type.maxBlocks} blocks"
        }
        blocks = blocks.plus(newBlocks)
    }

    fun toPersistentSignal(): PersistentSignalV2 {
        val b = blocks.map { PersistentBlockV2(it.x, it.y, it.z) }
        return PersistentSignalV2(name, type, invert, format.name, b)
    }

    companion object {
        fun fromPersistentSignal(persistentSignal: PersistentSignalV2): Signal {
            val blockLocations = persistentSignal.blocks.map {
                BlockPos(it.x, it.y, it.z)
            }
            val format = SignalFormat.fromStringOrDefault(persistentSignal.format)
            return Signal(persistentSignal.name, persistentSignal.type, persistentSignal.invert, format, blockLocations)
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
