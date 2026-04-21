package com.ac101m.redmon.isa

/**
 * Class for managing instruction sets.
 *
 * @prop name The name of the string.
 * @param initInstructionSets
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

    fun removeInstructionSet(name: String) {
        requireInstructionSetExists(name)
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
