package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v1.PersistentProfileV1
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World


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

    fun addRegister(register: Register) {
        registers[register.name] = register
    }

    fun removeRegister(name: String) {
        registers.remove(name)
    }

    fun getRegister(name: String): Register? {
        return registers[name]
    }

    fun updateState(world: World, offset: Vec3i) {
        registers.values.forEach { register ->
            register.updateState(world, offset)
        }
    }
}