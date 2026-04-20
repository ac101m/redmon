package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v2.PersistentColumnV2

class ProfilePageColumn(initSignals: List<Signal>) {
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
            "Column does not contain a signal with name '$name'"
        }
    }

    private fun requireSignalDoesNotExist(name: String) {
        require(getSignalIndex(name) == null) {
            "Column already contains a signal with name '$name'"
        }
    }

    fun addSignal(signal: Signal) {
        requireSignalDoesNotExist(signal.name)
        signals.add(signal)
    }

    fun removeSignal(name: String) {
        val index = requireSignalExists(name)
        signals.removeAt(index)
    }

    fun moveSignalVertically(name: String, n: Int): Int {
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

    fun toPersistentColumn(): PersistentColumnV2 {
        val persistentSignals = signals.map { it.toPersistentSignal() }
        return PersistentColumnV2(persistentSignals)
    }

    companion object {
        fun fromPersistentColumn(persistentColumn: PersistentColumnV2): ProfilePageColumn {
            val signals = persistentColumn.signals.map { Signal.fromPersistentSignal(it) }
            return ProfilePageColumn(signals)
        }
    }
}
