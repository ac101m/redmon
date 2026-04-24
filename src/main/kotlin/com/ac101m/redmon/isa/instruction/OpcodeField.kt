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
    bitPattern: String,
    offset: Int,
    description: String?
) : LiteralField(bitPattern, offset, description) {
    override val maxSize get() = InstructionLayout.MAX_INSTRUCTION_SIZE
    override val displayColour get() = COLOUR

    override fun descriptionText(): String {
        return description ?: "${bitPattern.length} bit opcode."
    }

    override fun toPersistent(): PersistentInstructionFieldV2 {
        return PersistentInstructionFieldV2(
            type = FieldType.OPCODE,
            size = size,
            offset = offset,
            metadata = bitPattern,
            description = description
        )
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
            return OpcodeField(bitPattern, persistent.offset, persistent.description)
        }

        fun of(text: String): OpcodeField {
            try {
                text.toULong(2)
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected a bit pattern but got '$text'.", e)
            }
            return OpcodeField(text, 0, null)
        }
    }
}
