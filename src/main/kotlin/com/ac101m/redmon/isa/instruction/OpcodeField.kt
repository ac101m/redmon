package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.isa.InstructionLayout
import com.ac101m.redmon.persistence.v2.PersistentInstructionFieldV2
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

    override fun toPersistent(): PersistentInstructionFieldV2 {
        return PersistentInstructionFieldV2(FieldType.OPCODE, size, offset, bitPattern)
    }

    companion object {
        val COLOUR = Colour.RED

        fun fromPersistent(persistent: PersistentInstructionFieldV2): OpcodeField {
            val bitPattern = requireNotNull(persistent.metadata) {
                "Bit pattern metadata missing."
            }
            require(persistent.size == bitPattern.length) {
                "Bit pattern length mismatch."
            }
            return OpcodeField(bitPattern, persistent.offset)
        }

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
