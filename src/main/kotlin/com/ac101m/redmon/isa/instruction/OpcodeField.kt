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
    description: String?
) : Field(bitPattern.length, offset, description) {
    override val maxSize get() = InstructionLayout.MAX_INSTRUCTION_SIZE
    override val colour get() = COLOUR

    init {
        try {
            bitPattern.toULong(2)
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Expected binary bit pattern but got '$bitPattern'.", e)
        }
    }

    override fun bitRepresentation(crossOut: Boolean): String {
        return StringBuilder(size * 2).apply {
            if (crossOut) {
                append(CROSSED_OUT_COLOUR.prefix)
                repeat(bitPattern.length) { append('-') }
            } else {
                append(colour.prefix)
                append(bitPattern)
            }
        }.toString()
    }

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
