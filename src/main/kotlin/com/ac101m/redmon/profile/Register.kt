package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v1.PersistentRegisterV1
import com.ac101m.redmon.persistence.v1.PersistentRegisterBitV1
import net.minecraft.block.AbstractRedstoneGateBlock
import net.minecraft.block.RepeaterBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World


data class Register(
    val name: String,
    val type: RegisterType,
    val watchPoints: List<Vec3i> = listOf()
) {
    private var state: Long = 0

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

    fun updateState(world: World, offset: Vec3i) {
        watchPoints.forEachIndexed { i, position ->
            val blockState = world.getBlockState(BlockPos(position.add(offset)))
            val mask = 1L shl i
            state = state and mask.inv()
            if (blockState.block is RepeaterBlock) {
                if (blockState.get(AbstractRedstoneGateBlock.POWERED)) {
                    state = state or mask
                }
            }
        }
    }

    fun getState(): Long {
        return state and ((1L shl size) - 1)
    }
}
