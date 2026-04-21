package com.ac101m.redmon.isa

import com.ac101m.redmon.utils.computeMask

/**
 * Represents an instruction within an instruction set.
 * Instructions consist of a set of fields, exactly one of which must be an opcode field.
 *
 * @property name The name of the instruction.
 * @property size The size of the instruction in bits.
 * @param opcodeBitPattern The opcode bits for this instruction.
 * @param fields The fields within the instruction.
 * @param description Optional description of the instruction.
 */
class Instruction(
    val name: String,
    val size: Int,
    private val opcodeBitPattern: String,
    private val fields: List<InstructionField>,
    private val description: String?
) {
    private val opcodeBits: ULong
    private val opcodeField: InstructionField
    private val instructionMask = computeMask(size)

    init {
        val opcodeFields = fields.filter { it.type == FieldType.OPCODE }.apply {
            require(isNotEmpty()) {
                "Opcode field is missing."
            }
            require(size < 2) {
                "Multiple opcode fields are present."
            }
        }

        for (field in fields) {
            require(field.offset + field.size <= size) {
                "One or more fields do not fit within the instruction."
            }
        }

        opcodeField = opcodeFields.single().apply {
            require(size == opcodeBitPattern.length) {
                "Opcode bit pattern does not match opcode field length."
            }
        }

        opcodeBits = try {
            (opcodeBitPattern.toULong(2) shl opcodeField.offset) and opcodeField.mask
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Opcode bits are not a valid binary string.", e)
        }
    }

    /**
     * Return true if the supplied bit pattern matches the opcode of this instruction.
     *
     * @param instructionBits The bits of the instruction to check.
     */
    fun opcodeMatches(instructionBits: ULong): Boolean {
        val instructionBitsMasked = instructionBits and opcodeField.mask
        return instructionBitsMasked == opcodeBits
    }

    /**
     * Returns true if the supplied instructions opcode collides with the opcode of this instruction.
     * This method is used to check if instruction opcodes are uniquely identifiable.
     *
     * @param other Instruction to compare with.
     */
    fun conflictsWith(other: Instruction): Boolean {
        val opcodeMask = opcodeField.mask
        val otherOpcodeMask = other.opcodeField.mask
        val sharedOpcodeMask = opcodeMask and otherOpcodeMask
        val maskedOpcodeBits = opcodeBits and sharedOpcodeMask
        val otherMaskedOpcodeBits = other.opcodeBits and sharedOpcodeMask
        return maskedOpcodeBits == otherMaskedOpcodeBits
    }

    companion object {
        const val MAX_INSTRUCTION_SIZE = 64

        fun createFromArgs(name: String, fieldText: List<String>, description: String?): Instruction {
            val parsedFields = ArrayList<InstructionField>()
            var currentOffset = 0
            var opcodeBitPattern: String? = null

            for (str in fieldText) {
                try {
                    val field = InstructionField.of(str, currentOffset)
                    if (field.type == FieldType.OPCODE) {
                        opcodeBitPattern = field.data
                    }
                    currentOffset += field.size
                    parsedFields.add(field)
                } catch (e: Exception) {
                    throw IllegalArgumentException("Error near '$str': ${e.message ?: e.javaClass}")
                }
            }

            requireNotNull(opcodeBitPattern) {
                "No opcode was added. Please add an opcode to your instruction."
            }

            return Instruction(
                name = name,
                size = currentOffset,
                opcodeBitPattern = opcodeBitPattern,
                fields = parsedFields,
                description = description
            )
        }
    }
}
