package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v1.PersistentProfileV1
import net.minecraft.core.Vec3i
import net.minecraft.world.level.Level

data class Profile(
    var name: String,
    val registers: HashMap<String, Register> = LinkedHashMap()
) {
    companion object {
        fun fromPersistent(data: PersistentProfileV1): Profile {
            val registers = LinkedHashMap<String, Register>()
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
        return PersistentProfileV1(
            name,
            registers.keys.map { registers[it]!!.toPersistent() }
        )
    }

    private fun requireRegisterDoesNotExist(name: String) {
        require(registers[name] == null) {
            "A register already exists with name '$name' in profile '${this.name}'."
        }
    }

    fun addRegister(register: Register) {
        requireRegisterDoesNotExist(register.name)
        registers[register.name] = register
    }

    fun getRegister(name: String): Register {
        return requireNotNull(registers[name]) {
            "No register with name '$name' in profile '${this.name}."
        }
    }

    fun renameRegister(name: String, newName: String) {
        val register = getRegister(name)
        register.name = newName
        registers.remove(name)
        registers[newName] = register
    }

    fun removeRegister(name: String) {
        registers.remove(name)
    }

    fun updateState(world: Level, offset: Vec3i) {
        registers.values.forEach { register ->
            register.updateState(world, offset)
        }
    }
}
