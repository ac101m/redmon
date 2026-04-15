package com.ac101m.redmon.profile

import com.fasterxml.jackson.annotation.JsonProperty
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

/**
 * Represents different types of signal.
 *
 * @property maxBlocks The maximum number of blocks a signal of this type can contain.
 * @property block The Minecraft block type associated with this signal type.
 */
enum class SignalType(val maxBlocks: Int, val bitsPerBlock: Int) {
    @JsonProperty("REPEATER")
    REPEATER(maxBlocks = 64, bitsPerBlock = 1);

    val block: Block get() {
        return when (this) {
            REPEATER -> Blocks.REPEATER
        }
    }
}
