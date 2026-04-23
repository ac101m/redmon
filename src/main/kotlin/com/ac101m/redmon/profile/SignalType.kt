package com.ac101m.redmon.profile

import com.ac101m.redmon.utils.RedmonException
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

/**
 * Represents different types of signal.
 *
 * @property maxBlocks The maximum number of blocks a signal of this type can contain.
 * @property bitsPerBlock The number of bits carried by each block.
 */
enum class SignalType(val maxBlocks: Int, val bitsPerBlock: Int) {
    REPEATER(maxBlocks = 64, bitsPerBlock = 1),
    DUST_BINARY(maxBlocks = 64, bitsPerBlock = 1),
    DUST_SS(maxBlocks = 16, bitsPerBlock = 4),
    COMPARATOR_BINARY(maxBlocks = 64, bitsPerBlock = 1),
    TORCH(maxBlocks = 64, bitsPerBlock = 1),
    REDSTONE_LAMP(maxBlocks = 64, bitsPerBlock = 1);

    fun getValidBlocks(): Set<Block> {
        return when (this) {
            REPEATER -> setOf(Blocks.REPEATER)
            DUST_BINARY -> setOf(Blocks.REDSTONE_WIRE)
            DUST_SS -> setOf(Blocks.REDSTONE_WIRE)
            COMPARATOR_BINARY -> setOf(Blocks.COMPARATOR)
            TORCH -> setOf(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH)
            REDSTONE_LAMP -> setOf(Blocks.REDSTONE_LAMP)
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
        return when (this) {
            REPEATER -> getBitsRepeater(blockState)
            DUST_BINARY -> getBitsDustBinary(blockState)
            DUST_SS -> getBitsDustSignalStrength(blockState)
            COMPARATOR_BINARY -> getBitsComparatorBinary(blockState)
            TORCH -> getBitsTorchBinary(blockState)
            REDSTONE_LAMP -> getBitsRedstoneLampBinary(blockState)
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

        private fun getBitsRepeater(blockState: BlockState): ULong? {
            if (blockState.block != Blocks.REPEATER) return null
            return if (blockState.getValue(BlockStateProperties.POWERED)) 1UL else 0UL
        }

        private fun getBitsDustBinary(blockState: BlockState): ULong? {
            if (blockState.block != Blocks.REDSTONE_WIRE) return null
            return if (blockState.getValue(BlockStateProperties.POWER) > 0) 1UL else 0UL
        }

        private fun getBitsDustSignalStrength(blockState: BlockState): ULong? {
            if (blockState.block != Blocks.REDSTONE_WIRE) return null
            return blockState.getValue(BlockStateProperties.POWER).toULong()
        }

        private fun getBitsComparatorBinary(blockState: BlockState): ULong? {
            if (blockState.block != Blocks.COMPARATOR) return null
            return if (blockState.getValue(BlockStateProperties.POWERED)) 1UL else 0UL
        }

        private fun getBitsTorchBinary(blockState: BlockState): ULong? {
            if (blockState.block != Blocks.REDSTONE_TORCH && blockState.block != Blocks.REDSTONE_WALL_TORCH) return null
            return if (blockState.getValue(BlockStateProperties.LIT)) 1UL else 0UL
        }

        private fun getBitsRedstoneLampBinary(blockState: BlockState): ULong? {
            if (blockState.block != Blocks.REDSTONE_LAMP) return null
            return if (blockState.getValue(BlockStateProperties.LIT)) 1UL else 0UL
        }
    }
}
