package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.isa.InstructionLayout
import com.ac101m.redmon.utils.Colour

/**
 * Represents an opcode field within an instruction.
 *
 * @property bitPattern String containing the bit pattern for the opcode (LSB last).
 * @param offset The offset of the field within the instruction.
 */
class OpcodeField(
    val bitPattern: String,
    offset: Int,
) : Field(bitPattern.length, offset) {
    override val maxSize get() = InstructionLayout.MAX_INSTRUCTION_SIZE

    init {
        try {
            bitPattern.toULong(2)
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Expected binary bit pattern but got '$bitPattern'.", e)
        }
    }

    override fun bitRepresentation(): String {
        return "${COLOUR.prefix}$bitPattern"
    }

    companion object {
        val COLOUR = Colour.RED

        fun of(text: String, offset: Int): OpcodeField {
            try {
                text.toULong(2)
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected a bit pattern but got '$text'.", e)
            }
            return OpcodeField(text, offset)
        }
    }
}
