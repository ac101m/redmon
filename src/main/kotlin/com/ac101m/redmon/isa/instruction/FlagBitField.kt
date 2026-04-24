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
    description: String?
) : Field(1, offset, description) {
    override val maxSize get() = 1
    override val displayColour get() = COLOUR

    override fun bitRepresentation(crossOut: Boolean): String {
        return StringBuilder(8).apply {
            if (crossOut) {
                append(CROSSED_OUT_COLOUR.prefix)
                append('-')
            } else {
                append(displayColour.prefix)
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
            metadata = char.toString(),
            description = description
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
