package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v1.PersistentProfileV1
import net.minecraft.core.Vec3i
import net.minecraft.world.level.Level

class Profile(
    internal var name: String,
    initRegisters: List<Register>
) {
    private val registerIndex = LinkedHashMap<String, Register>()

    val registers get () = registerIndex.values.toList()

    init {
        initRegisters.forEach { register ->
            registerIndex[register.name] = register
        }
    }

    private fun requireRegisterExists(name: String): Register {
        return requireNotNull(registerIndex[name]) {
            "Profile '${this.name}' does not contain a register with name '$name'"
        }
    }

    private fun requireRegisterDoesNotExist(name: String) {
        require(registerIndex[name] == null) {
            "Profile '${this.name}' already contains a register with name '$name'"
        }
    }

    fun addRegister(register: Register) {
        requireRegisterDoesNotExist(register.name)
        registerIndex[register.name] = register
    }

    fun getRegister(name: String): Register {
        return requireRegisterExists(name)
    }

    fun renameRegister(name: String, newName: String) {
        val register = getRegister(name)
        register.name = newName
        registerIndex.remove(name)
        registerIndex[newName] = register
    }

    fun deleteRegister(name: String) {
        requireRegisterExists(name)
        registerIndex.remove(name)
    }

    fun updateState(world: Level, offset: Vec3i) {
        registerIndex.values.forEach { register ->
            register.updateState(world, offset)
        }
    }

    fun toPersistentV1(): PersistentProfileV1 {
        return PersistentProfileV1(name, registerIndex.keys.map { registerIndex[it]!!.toPersistentV1() })
    }

    companion object {
        fun fromPersistentV1(data: PersistentProfileV1): Profile {
            val registers = data.registers.map { Register.fromPersistentV1(it) }
            return Profile(data.name, registers)
        }
    }
}
