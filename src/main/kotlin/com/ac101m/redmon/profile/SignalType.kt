package com.ac101m.redmon.profile

import com.ac101m.redmon.utils.RedmonException
import com.fasterxml.jackson.annotation.JsonProperty
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.ComparatorBlock
import net.minecraft.world.level.block.entity.ComparatorBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

/**
 * Represents different types of signal.
 *
 * @property maxBlocks The maximum number of blocks a signal of this type can contain.
 * @property bitsPerBlock The number of bits carried by each block.
 * @property block The Minecraft block type associated with this signal type.
 */
enum class SignalType(val maxBlocks: Int, val bitsPerBlock: Int) {
    @JsonProperty("REPEATER")
    REPEATER(maxBlocks = 64, bitsPerBlock = 1),

    @JsonProperty("DUST_BINARY")
    DUST_BINARY(maxBlocks = 64, bitsPerBlock = 1),

    @JsonProperty("DUST_SS")
    DUST_SS(maxBlocks = 16, bitsPerBlock = 4),

    @JsonProperty("COMPARATOR_BINARY")
    COMPARATOR_BINARY(maxBlocks = 64, bitsPerBlock = 1);

    val block: Block get() {
        return when (this) {
            REPEATER -> Blocks.REPEATER
            DUST_BINARY -> Blocks.REDSTONE_WIRE
            DUST_SS -> Blocks.REDSTONE_WIRE
            COMPARATOR_BINARY -> Blocks.COMPARATOR
        }
    }

    /**
     * Gets the bits from a given block state.
     * Returns NULL if the appropriate block cannot be found.
     *
     * @param level The level for the query.
     * @param blockPos The position of the block to get the bits from.
     */
    fun getBitsFromBlockLocation(level: Level, blockPos: BlockPos): ULong? {
        val blockState = level.getBlockState(blockPos)
        if (blockState.block != this.block) return null
        return when (this) {
            REPEATER -> getBitsRepeater(blockState)
            DUST_BINARY -> getBitsDustBinary(blockState)
            DUST_SS -> getBitsDustSignalStrength(blockState)
            COMPARATOR_BINARY -> getBitsComparatorBinary(blockState)
        }
    }

    companion object {
        fun fromCommandString(str: String): SignalType {
            return try {
                SignalType.valueOf(str.uppercase())
            } catch (e: IllegalArgumentException) {
                val validTypesString = SignalType.entries.joinToString(", ") { it.name.lowercase() }
                throw RedmonException("Invalid signal type. Valid types are: $validTypesString", e)
            }
        }

        private fun getBitsRepeater(blockState: BlockState): ULong {
            return if (blockState.getValue(BlockStateProperties.POWERED)) 1UL else 0UL
        }

        private fun getBitsDustBinary(blockState: BlockState): ULong {
            return if (blockState.getValue(BlockStateProperties.POWER) > 0) 1UL else 0UL
        }

        private fun getBitsDustSignalStrength(blockState: BlockState): ULong {
            return blockState.getValue(BlockStateProperties.POWER).toULong()
        }

        private fun getBitsComparatorBinary(blockState: BlockState): ULong {
            return if (blockState.getValue(BlockStateProperties.POWERED)) 1UL else 0UL
        }
    }
}
