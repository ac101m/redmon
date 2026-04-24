package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.isa.InstructionLayout
import com.ac101m.redmon.persistence.v2.PersistentInstructionFieldV2
import com.ac101m.redmon.utils.Colour

/**
 * Represents a register address field used as a destination.
 *
 * @param size The size of the field.
 * @param offset The offset of the field.
 * @param description The description of the register field.
 */
class DestRegisterField(
    size: Int,
    offset: Int,
    description: String?
) : ParameterField(size, offset, description), DecodableField {
    override val maxSize get() = InstructionLayout.MAX_INSTRUCTION_SIZE
    override val displayColour get() = Colour.BLUE
    override val displayChar get() = 'R'

    override fun descriptionText(): String {
        return description ?: "Destination register."
    }

    override fun decode(bits: ULong): String {
        val bits = (bits and mask) shr offset
        return "r$bits"
    }

    override fun toPersistent(): PersistentInstructionFieldV2 {
        return PersistentInstructionFieldV2(
            type = FieldType.REGISTER_WRITE,
            size = size,
            offset = offset,
            metadata = null,
            description = description
        )
    }

    companion object {
        fun fromPersistent(persistent: PersistentInstructionFieldV2): DestRegisterField {
            return DestRegisterField(persistent.size, persistent.offset, persistent.description)
        }

        fun of(text: String): DestRegisterField {
            val sizeInt = try {
                text.toUInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected integer bit count but got '$text'.", e)
            }
            return DestRegisterField(sizeInt.toInt(), 0, null)
        }
    }
}
