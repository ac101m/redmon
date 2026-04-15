package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v1.PersistentProfileV1
import net.minecraft.core.Vec3i
import net.minecraft.world.level.Level

class Profile(
    internal var name: String,
    initRegisters: List<Register>
) {
    val registers = initRegisters.toMutableList()

    private fun getRegisterIndex(name: String): Int? {
        registers.forEachIndexed { i, register ->
            if (register.name == name) {
                return i
            }
        }
        return null
    }

    private fun requireRegisterExists(name: String): Int {
        return requireNotNull(getRegisterIndex(name)) {
            "Profile '${this.name}' does not contain a register with name '$name'"
        }
    }

    private fun requireRegisterDoesNotExist(name: String) {
        require(getRegisterIndex(name) == null) {
            "Profile '${this.name}' already contains a register with name '$name'"
        }
    }

    fun addRegister(register: Register) {
        requireRegisterDoesNotExist(register.name)
        registers.add(register)
    }

    fun getRegister(name: String): Register {
        return registers[requireRegisterExists(name)]
    }

    fun renameRegister(name: String, newName: String) {
        val index = requireRegisterExists(name)
        val renamedRegister = registers[index].copy(name = newName)
        registers[index] = renamedRegister
    }

    fun deleteRegister(name: String) {
        val index = requireRegisterExists(name)
        registers.removeAt(index)
    }

    fun updateState(world: Level, offset: Vec3i) {
        registers.forEach { register ->
            register.updateState(world, offset)
        }
    }

    fun toPersistentV1(): PersistentProfileV1 {
        return PersistentProfileV1(name, registers.map { it.toPersistentV1() })
    }

    fun moveRegister(name: String, n: Int): Int {
        var index = requireRegisterExists(name)
        var moved = 0
        if (n < 0) {        // Up
            while (index > 0 && moved > n) {
                swapRegisters(index, index - 1)
                moved--
                index--
            }
        } else if (n > 0) { // Down
            while (index < registers.size - 1 && moved < n) {
                swapRegisters(index, index + 1)
                moved++
                index++
            }
        }
        return moved
    }

    private fun swapRegisters(a: Int, b: Int) {
        check(a in registers.indices)
        check(b in registers.indices)
        val tmp = registers[a]
        registers[a] = registers[b]
        registers[b] = tmp
    }

    companion object {
        fun fromPersistentV1(data: PersistentProfileV1): Profile {
            val registers = data.registers.map { Register.fromPersistentV1(it) }
            return Profile(data.name, registers)
        }
    }
}
