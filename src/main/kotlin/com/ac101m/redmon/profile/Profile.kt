package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v1.PersistentProfileV1
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource
import net.minecraft.util.math.Vec3i


data class Profile(
    val name: String,
    val registers: HashMap<String, Register> = HashMap()
) {
    companion object {
        fun fromPersistent(data: PersistentProfileV1): Profile {
            val registers = HashMap<String, Register>()
            data.registers.forEach {
                registers[it.name] = Register.fromPersistent(it)
            }
            return Profile(
                data.name,
                registers
            )
        }
    }

    fun toPersistent(): PersistentProfileV1 {
        val registers = registers.values.map { it.toPersistent() }
        return PersistentProfileV1(
            name,
            registers
        )
    }

    fun updateState(context: CommandContext<FabricClientCommandSource>, offset: Vec3i) {
        registers.values.forEach { register ->
            register.updateState(context, offset)
        }
    }
}
