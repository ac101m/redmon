package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v1.PersistentRegisterV1
import com.ac101m.redmon.persistence.v1.PersistentRegisterBitV1
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i


data class Register(
    val name: String,
    val type: RegisterType,
    val watchPoints: List<Vec3i> = listOf()
) {
    companion object {
        fun fromPersistent(data: PersistentRegisterV1): Register {
            return Register(
                data.name,
                RegisterType.valueOf(data.type),
                data.bitLocations.map { Vec3i(it.x, it.y, it.z) }
            )
        }
    }

    fun toPersistent(): PersistentRegisterV1 {
        return PersistentRegisterV1(
            name,
            type.name,
            watchPoints.map { PersistentRegisterBitV1(it.x, it.y, it.z) }
        )
    }

    val size get() = watchPoints.size

    fun updateState(context: CommandContext<FabricClientCommandSource>, offset: Vec3i) {
        for (position in watchPoints) {
            val blockState = context.source.player.world.getBlockState(BlockPos(position.add(offset)))
        }
    }
}
