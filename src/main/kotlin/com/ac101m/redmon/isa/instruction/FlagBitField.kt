package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.persistence.v2.PersistentInstructionFieldV2
import com.ac101m.redmon.utils.Colour

/**
 * Represents a flag bit within an instruction.
 *
 * @property char The character that should be used to indicate the flag.
 * @property offset The offset of the flag bit within the instruction.
 * @property description Optional user defined description of the flag bit.
 */
class FlagBitField(
    val char: Char,
    offset: Int,
    val description: String?
) : Field(1, offset) {
    override val maxSize get() = 1

    override fun bitRepresentation(crossOut: Boolean): String {
        return StringBuilder(size).apply {
            if (crossOut) {
                append(Colour.GRAY.prefix)
                append('-')
            } else {
                append(COLOUR.prefix)
                append(char)
            }
        }.toString()
    }

    override fun descriptionText(): String {
        return description ?: "Flag bit ($char)."
    }

    override fun toPersistent(): PersistentInstructionFieldV2 {
        return PersistentInstructionFieldV2(
            type = FieldType.FLAG_BIT,
            size = size,
            offset = offset,
            metadata = char.toString()
        )
    }

    companion object {
        val COLOUR = Colour.GOLD

        fun fromPersistent(persistent: PersistentInstructionFieldV2): FlagBitField {
            val metadata = requireNotNull(persistent.metadata) {
                "Flag metadata is missing."
            }
            return FlagBitField(metadata[0], persistent.offset, persistent.description)
        }

        fun of(text: String): FlagBitField {
            require(text.length == 1) {
                "Expected single flag bit character but got '$text'."
            }
            return FlagBitField(text[0], 0, null)
        }
    }
}
