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

    private fun requireInstructionDoesNotExist(instructionName: String) {
        require(!instructionIndex.containsKey(instructionName)) {
            "Instruction set '$name' already contains an instruction called '$instructionName'."
        }
    }

    private fun requireInstructionExists(instructionName: String): InstructionLayout {
        return requireNotNull(instructionIndex[instructionName]) {
            "Instruction set '$name' does not contain an instruction '$instructionName'."
        }
    }

    fun getInstruction(instructionName: String): InstructionLayout {
        return requireInstructionExists(instructionName)
    }

    fun addInstruction(newInstruction: InstructionLayout) {
        requireInstructionDoesNotExist(newInstruction.name)
        require(newInstruction.size == instructionSize) {
            "Instruction set '$name' expects $instructionSize bit instructions, but got ${newInstruction.size}."
        }
        for (existingInstruction in instructions) {
            require(!newInstruction.conflictsWith(existingInstruction)) {
                "New instruction opcode conflicts with existing instruction '${existingInstruction.name}'."
            }
        }
        instructionIndex[newInstruction.name] = newInstruction
    }

    fun removeInstruction(instructionName: String) {
        requireInstructionExists(instructionName)
        instructionIndex.remove(instructionName)
    }

    fun renameInstruction(instructionName: String, newInstructionName: String) {
        val instruction = requireInstructionExists(instructionName)
        requireInstructionDoesNotExist(newInstructionName)
        instruction.name = newInstructionName
        instructionIndex.remove(instructionName)
        instructionIndex[newInstructionName] = instruction
    }

    /**
     * Disassembles an instruction and returns a string.
     * If no instruction matches the specified bit pattern, returns null.
     *
     * @param bits The bits of the instruction.
     */
    fun disassemble(bits: ULong): String? {
        val bitsMasked = bits and instructionMask
        val instructions = instructions.filter { it.opcodeMatches(bitsMasked) }

        if (instructions.isEmpty()) {
            return null
        }

        check(instructions.size == 1) {
            "Multiple instructions match provided opcode. This should not happen!"
        }

        return instructions.single().disassemble(bits)
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
