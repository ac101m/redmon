package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v1.PersistentRegisterV1
import com.ac101m.redmon.persistence.v1.PersistentRegisterBitV1
import net.minecraft.block.AbstractRedstoneGateBlock
import net.minecraft.block.RepeaterBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World


data class Register(
    var name: String,
    val type: RegisterType,
    var invert: Boolean,
    var watchPoints: List<Vec3i> = listOf()
) {
    private var state: Long = 0

    val size get() = watchPoints.size


    companion object {
        fun fromPersistent(data: PersistentRegisterV1): Register {
            return Register(
                data.name,
                RegisterType.valueOf(data.type),
                data.invert,
                data.bitLocations.map { Vec3i(it.x, it.y, it.z) }
            )
        }
    }


    fun toPersistent(): PersistentRegisterV1 {
        return PersistentRegisterV1(
            name,
            type.name,
            invert,
            watchPoints.map { PersistentRegisterBitV1(it.x, it.y, it.z) }
        )
    }


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
        val mask = (1L shl size) - 1
        return when (invert) {
            false -> state and mask
            true -> state.inv() and mask
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
