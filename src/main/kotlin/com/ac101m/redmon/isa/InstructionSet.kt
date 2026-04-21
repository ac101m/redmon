package com.ac101m.redmon.isa

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
    val registerAliases: Map<ULong, String> = emptyMap(),
    initInstructions: List<Instruction> = emptyList()
) {
    private val instructions = initInstructions.toMutableList()
    private val instructionMask = computeMask(instructionSize)

    fun addInstruction(newInstruction: Instruction) {
        for (instruction in instructions) {
            TODO("Not yet implemented")
        }
    }

    fun decodeInstruction(bits: ULong): String {
        val bitsMasked = bits and instructionMask
        TODO("Not yet implemented")
    }

    init {
        require(instructionSize > 0) {
            "Instruction size must be greater than zero."
        }
        require(instructionSize <= Instruction.MAX_INSTRUCTION_SIZE) {
            "Instruction size must be less than ${Instruction.MAX_INSTRUCTION_SIZE}."
        }
    }
}
