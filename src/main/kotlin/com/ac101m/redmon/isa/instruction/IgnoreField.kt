package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.isa.InstructionLayout
import com.ac101m.redmon.persistence.v2.PersistentInstructionFieldV2
import com.ac101m.redmon.utils.Colour

/**
 * Represents ignored bits within the instruction.
 *
 * @param size The size of the ignored field.
 * @param offset The offset of the field within the larger instruction.
 */
class IgnoreField(
    size: Int,
    offset: Int,
    description: String?
) : ParameterField(size, offset, description) {
    override val maxSize get() = InstructionLayout.MAX_INSTRUCTION_SIZE
    override val displayColour get() = COLOUR
    override val displayChar get() = 'X'

    override fun descriptionText(): String {
        return description ?: "Ignored."
    }

    override fun toPersistent(): PersistentInstructionFieldV2 {
        return PersistentInstructionFieldV2(
            type = FieldType.IGNORE,
            size = size,
            offset = offset,
            metadata = null,
            description = description
        )
    }

    companion object {
        val COLOUR = Colour.GRAY

        fun fromPersistent(persistent: PersistentInstructionFieldV2): IgnoreField {
            return IgnoreField(persistent.size, persistent.offset, persistent.description)
        }

        fun of(text: String): IgnoreField {
            val sizeInt = try {
                text.toUInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected an unsigned integer but got '$text'.", e)
            }
            return IgnoreField(sizeInt.toInt(), 0, null)
        }
    }
}
