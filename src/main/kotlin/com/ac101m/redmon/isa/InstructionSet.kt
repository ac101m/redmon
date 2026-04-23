package com.ac101m.redmon.isa

import com.ac101m.redmon.persistence.v2.PersistentInstructionSetV2
import com.ac101m.redmon.utils.computeMask

/**
 * Class describes an instruction set (required for assembly/disassembly).
 * Currently only supports fixed width instructions.
 *
 * @property name The name of the instruction set.
 * @property instructionSize The width of instructions in bits.
 * @property registerAliases Map of register aliases.
 * @param initInstructions Initial instructions to populate the object with.
 */
class InstructionSet(
    var name: String,
    val instructionSize: Int,
    val registerAliases: Map<Int, Set<String>> = emptyMap(),
    initInstructions: List<InstructionLayout> = emptyList()
) {
    private val instructionIndex = HashMap<String, InstructionLayout>()
    private val instructionMask = computeMask(instructionSize)

    val instructions get() = instructionIndex.values.toList()

    init {
        require(instructionSize > 0) {
            "Instruction size must be greater than zero."
        }

        require(instructionSize <= InstructionLayout.MAX_INSTRUCTION_SIZE) {
            "Instruction size must be less than ${InstructionLayout.MAX_INSTRUCTION_SIZE}."
        }

        for (initInstruction in initInstructions) {
            requireInstructionDoesNotExist(initInstruction.name)
            instructionIndex[initInstruction.name] = initInstruction
        }
    }

    fun addInstruction(newInstruction: InstructionLayout) {
        requireInstructionDoesNotExist(newInstruction.name)
        for (existingInstruction in instructions) {
            require(!newInstruction.conflictsWith(existingInstruction)) {
                "New instruction opcode conflicts with existing instruction ${existingInstruction.name}."
            }
        }
        instructionIndex[newInstruction.name] = newInstruction
    }

    fun disassemble(bits: ULong): String {
        val bitsMasked = bits and instructionMask
        TODO("Not yet implemented")
    }

    private fun requireInstructionDoesNotExist(name: String) {
        require(!instructionIndex.containsKey(name)) {
            "Instruction set already contains an instruction called '$name'."
        }
    }

    fun toPersistent(): PersistentInstructionSetV2 {
        val persistentInstructions = instructions.map { it.toPersistent() }
        return PersistentInstructionSetV2(name, instructionSize, registerAliases, persistentInstructions)
    }

    companion object {
        fun fromPersistent(persistent: PersistentInstructionSetV2): InstructionSet {
            return InstructionSet(
                persistent.name,
                persistent.instructionSize,
                emptyMap(),
                persistent.instructions.map { persistentInstructionLayout ->
                    InstructionLayout.fromPersistent(persistent.instructionSize, persistentInstructionLayout)
                }
            )
        }
    }
}
