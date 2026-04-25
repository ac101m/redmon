package com.ac101m.redmon.isa

/**
 * Class for managing instruction sets.
 *
 * @param initInstructionSets Initial set of instruction sets to populate the registry with.
 */
class InstructionSetRegistry(initInstructionSets: List<InstructionSet>) {
    private val instructionSetIndex = HashMap<String, InstructionSet>()

    val instructionSets get() = instructionSetIndex.values.toList()

    init {
        for (instructionSet in initInstructionSets) {
            instructionSetIndex[instructionSet.name] = instructionSet
        }
    }

    private fun requireInstructionSetExists(name: String): InstructionSet {
        return requireNotNull(instructionSetIndex[name]) {
            "No instruction set exists with name '$name'."
        }
    }

    private fun requireInstructionSetDoesNotExist(name: String) {
        require(!instructionSetIndex.containsKey(name)) {
            "An instruction set with name '$name' already exists."
        }
    }

    fun addInstructionSet(instructionSet: InstructionSet) {
        requireInstructionSetDoesNotExist(instructionSet.name)
        instructionSetIndex[instructionSet.name] = instructionSet
    }

    fun getInstructionSet(name: String): InstructionSet {
        return requireInstructionSetExists(name)
    }

    fun getInstructionSetOrNull(name: String): InstructionSet? {
        return instructionSetIndex[name]
    }

    fun removeInstructionSet(name: String) {
        requireInstructionSetExists(name).deleted = true
        instructionSetIndex.remove(name)
    }

    fun renameInstructionSet(name: String, newName: String) {
        requireInstructionSetDoesNotExist(newName)
        val instructionSet = requireInstructionSetExists(name)
        instructionSet.name = newName
        instructionSetIndex.remove(name)
        instructionSetIndex[newName] = instructionSet
    }
}
