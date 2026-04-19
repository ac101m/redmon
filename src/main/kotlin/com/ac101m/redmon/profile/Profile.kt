package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v2.PersistentColumnV2
import com.ac101m.redmon.persistence.v2.PersistentPageV2
import com.ac101m.redmon.persistence.v2.PersistentProfileV2
import net.minecraft.core.Vec3i
import net.minecraft.world.level.Level

class Profile(
    internal var name: String,
    initSignals: List<Signal>
) {
    val signals = initSignals.toMutableList()

    private fun getSignalIndex(name: String): Int? {
        signals.forEachIndexed { i, signal ->
            if (signal.name == name) {
                return i
            }
        }
        return null
    }

    private fun requireSignalExists(name: String): Int {
        return requireNotNull(getSignalIndex(name)) {
            "Profile '${this.name}' does not contain a signal with name '$name'"
        }
    }

    private fun requireSignalDoesNotExist(name: String) {
        require(getSignalIndex(name) == null) {
            "Profile '${this.name}' already contains a signal with name '$name'"
        }
    }

    fun addSignal(signal: Signal) {
        requireSignalDoesNotExist(signal.name)
        signals.add(signal)
    }

    fun getSignal(name: String): Signal {
        return signals[requireSignalExists(name)]
    }

    fun renameSignal(name: String, newName: String) {
        val index = requireSignalExists(name)
        val renamedSignal = signals[index].copy(name = newName)
        signals[index] = renamedSignal
    }

    fun deleteSignal(name: String) {
        val index = requireSignalExists(name)
        signals.removeAt(index)
    }

    fun updateState(world: Level, offset: Vec3i) {
        signals.forEach { signal ->
            signal.updateState(world, offset)
        }
    }

    fun moveSignal(name: String, n: Int): Int {
        var index = requireSignalExists(name)
        var moved = 0
        if (n < 0) {        // Up
            while (index > 0 && moved > n) {
                swapSignals(index, index - 1)
                moved--
                index--
            }
        } else if (n > 0) { // Down
            while (index < signals.size - 1 && moved < n) {
                swapSignals(index, index + 1)
                moved++
                index++
            }
        }
        return moved
    }

    private fun swapSignals(a: Int, b: Int) {
        check(a in signals.indices)
        check(b in signals.indices)
        val tmp = signals[a]
        signals[a] = signals[b]
        signals[b] = tmp
    }

    fun toPersistentProfile(): PersistentProfileV2 {
        // TODO: Implement pages and columns
        val persistentSignals = signals.map { persistentSignal ->
            persistentSignal.toPersistentSignal()
        }
        val persistentColumns = listOf(PersistentColumnV2(persistentSignals))
        val persistentPages = listOf(PersistentPageV2("Page 1", persistentColumns))
        return PersistentProfileV2(name, persistentPages)
    }

    companion object {
        fun fromPersistentProfile(persistentProfile: PersistentProfileV2): Profile {
            // TODO: Implement pages and columns
            val singlePersistentPage = persistentProfile.pages.single()
            val singlePersistentColumn = singlePersistentPage.columns.single()

            val signals = singlePersistentColumn.signals.map { signal ->
                Signal.fromPersistentSignal(signal)
            }

            return Profile(persistentProfile.name, signals)
        }
    }
}
