package com.ac101m.redmon.profile

import com.ac101m.redmon.isa.InstructionSet
import com.ac101m.redmon.isa.InstructionSetRegistry
import com.ac101m.redmon.persistence.v2.PersistentPageV2
import net.minecraft.core.Vec3i
import net.minecraft.world.level.Level

class ProfilePage(
    var name: String,
    initColumns: List<ProfilePageColumn>,
    var currentIsa: InstructionSet?
) {
    val columns = initColumns.toMutableList()
    val signalMap = HashMap<String, SignalInfo>()

    class SignalInfo(
        var column: ProfilePageColumn,
        val signal: Signal
    )

    init {
        for (column in columns) {
            for (signal in column.signals) {
                require(!signalMap.containsKey(name)) {
                    "Failed to initialize profile page. Multiple signals with name '$name'."
                }
                signalMap[signal.name] = SignalInfo(column, signal)
            }
        }
    }

    private fun requireSignalExists(name: String): SignalInfo {
        return requireNotNull(signalMap[name]) {
            "Profile '${this.name}' does not contain a signal with name '$name'"
        }
    }

    private fun requireSignalDoesNotExist(name: String) {
        require(!signalMap.containsKey(name)) {
            "Profile '${this.name}' already contains a signal with name '$name'"
        }
    }

    private fun getOrCreateColumn(columnIndex: Int): ProfilePageColumn {
        return if (columnIndex < columns.size) {
            columns[columnIndex]
        } else if (columnIndex == columns.size) {
            columns.add(ProfilePageColumn(emptyList()))
            columns[columnIndex]
        } else {
            throw IllegalStateException("Column index out of range.")
        }
    }

    fun addSignal(signal: Signal, columnIndex: Int) {
        requireSignalDoesNotExist(signal.name)
        val signalColumn = getOrCreateColumn(columnIndex)
        signalMap[signal.name] = SignalInfo(signalColumn, signal)
        signalColumn.addSignal(signal)
    }

    fun getSignal(name: String): Signal {
        return requireSignalExists(name).signal
    }

    fun renameSignal(name: String, newName: String) {
        requireSignalDoesNotExist(newName)
        val signalInfo = requireSignalExists(name)
        signalInfo.signal.name = newName
        signalMap.remove(name)
        signalMap[newName] = signalInfo
    }

    fun removeSignal(name: String) {
        val signalInfo = requireSignalExists(name)
        signalInfo.column.removeSignal(name)
        signalMap.remove(name)
        columns.removeIf { it.signals.isEmpty() }
    }

    fun updateState(world: Level, offset: Vec3i) {
        if (currentIsa?.deleted == true) {
            currentIsa = null
        }
        signalMap.values.forEach { signalInfo ->
            signalInfo.signal.updateState(world, offset)
        }
    }

    fun moveSignalVertically(name: String, n: Int): Int {
        val signalInfo = requireSignalExists(name)
        return signalInfo.column.moveSignalVertically(name, n)
    }

    fun changeSignalColumn(name: String, columnIndex: Int) {
        val signalInfo = requireSignalExists(name)
        val oldColumn = signalInfo.column
        val newColumn = getOrCreateColumn(columnIndex)

        if (oldColumn === newColumn) {
            return
        }

        oldColumn.removeSignal(name)
        newColumn.addSignal(signalInfo.signal)
        signalInfo.column = newColumn

        columns.removeIf { it.signals.isEmpty() }
    }

    fun toPersistentProfilePage(): PersistentPageV2 {
        val persistentColumns = columns.map { it.toPersistentColumn() }
        val isaName = currentIsa?.let {
            if (it.deleted) {
                null
            } else {
                it.name
            }
        }
        return PersistentPageV2(name, persistentColumns, isaName)
    }

    companion object {
        fun fromPersistentProfilePage(
            persistentPage: PersistentPageV2,
            instructionSetRegistry: InstructionSetRegistry
        ): ProfilePage {
            val columns = persistentPage.columns.map {
                ProfilePageColumn.fromPersistentColumn(it)
            }
            val isa = persistentPage.currentIsa?.let {
                instructionSetRegistry.getInstructionSetOrNull(it)
            }
            return ProfilePage(persistentPage.name, columns, isa)
        }
    }
}
