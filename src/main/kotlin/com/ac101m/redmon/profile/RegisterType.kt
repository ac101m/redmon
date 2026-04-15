package com.ac101m.redmon.profile

import com.fasterxml.jackson.annotation.JsonProperty
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

enum class RegisterType {
    @JsonProperty("REPEATER")
    REPEATER;

    fun getBlock(): Block {
        return when (this) {
            REPEATER -> Blocks.REPEATER
        }
    }
}
