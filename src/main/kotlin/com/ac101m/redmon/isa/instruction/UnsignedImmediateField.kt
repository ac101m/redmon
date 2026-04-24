package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.isa.InstructionLayout
import com.ac101m.redmon.persistence.v2.PersistentInstructionFieldV2
import com.ac101m.redmon.utils.Colour

class UnsignedImmediateField(
    size: Int,
    offset: Int,
    description: String?
) : ParameterField(size, offset, description) {
    override val maxSize get() = InstructionLayout.MAX_INSTRUCTION_SIZE
    override val displayColour get() = COLOUR
    override val displayChar get() = 'U'

    override fun descriptionText(): String {
        return description ?: "$size bit unsigned immediate."
    }

    override fun toPersistent(): PersistentInstructionFieldV2 {
        return PersistentInstructionFieldV2(
            type = FieldType.UNSIGNED_IMMEDIATE,
            size = size,
            offset = offset,
            metadata = null,
            description = description
        )
    }

    companion object {
        val COLOUR = Colour.DARK_GREEN

        fun fromPersistent(persistent: PersistentInstructionFieldV2): UnsignedImmediateField {
            return UnsignedImmediateField(persistent.size, persistent.offset, persistent.description)
        }

        fun of(sizeText: String): UnsignedImmediateField {
            val sizeInt = try {
                sizeText.toUInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected an unsigned integer but got '$sizeText'.", e)
            }
            return UnsignedImmediateField(sizeInt.toInt(), 0, null)
        }
    }
}
