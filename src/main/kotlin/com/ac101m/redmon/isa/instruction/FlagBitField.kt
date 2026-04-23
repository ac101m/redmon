package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.persistence.v2.PersistentInstructionFieldV2
import com.ac101m.redmon.utils.Colour

/**
 * Represents a flag bit within an instruction.
 *
 * @property char The character that should be used to indicate the flag.
 * @property offset The offset of the flag bit within the instruction.
 */
class FlagBitField(
    val char: Char,
    offset: Int
) : Field(1, offset) {
    override val maxSize get() = 1

    override fun bitRepresentation(): String {
        return "${COLOUR.prefix}$char"
    }

    override fun toPersistent(): PersistentInstructionFieldV2 {
        return PersistentInstructionFieldV2(FieldType.FLAG_BIT, size, offset, char.toString())
    }

    companion object {
        val COLOUR = Colour.GOLD

        fun fromPersistent(persistent: PersistentInstructionFieldV2): FlagBitField {
            val metadata = requireNotNull(persistent.metadata) {
                "Flag metadata is missing."
            }
            return FlagBitField(metadata[0], persistent.offset)
        }

        fun of(text: String, offset: Int): FlagBitField {
            require(text.length == 1) {
                "Expected single flag bit character but got '$text'."
            }
            return FlagBitField(text[0], offset)
        }
    }
}
