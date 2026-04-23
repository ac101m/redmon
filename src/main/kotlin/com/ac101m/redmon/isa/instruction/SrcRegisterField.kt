package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.persistence.v2.PersistentInstructionFieldV2
import com.ac101m.redmon.utils.Colour

/**
 * Represents a register address field used as a source.
 *
 * @param size The size of the field.
 * @param offset The offset of the field.
 * @param description The description of the register field.
 */
class SrcRegisterField(
    size: Int,
    offset: Int,
    description: String?
) : RegisterField(size, offset, description) {
    override val colour get() = Colour.AQUA

    override fun descriptionText(): String {
        return description ?: "Source register."
    }

    override fun toPersistent(): PersistentInstructionFieldV2 {
        return PersistentInstructionFieldV2(
            type = FieldType.REGISTER_READ,
            size = size,
            offset = offset,
            metadata = null,
            description = description
        )
    }

    companion object {
        fun fromPersistent(persistent: PersistentInstructionFieldV2): SrcRegisterField {
            return SrcRegisterField(persistent.size, persistent.offset, persistent.description)
        }

        fun of(text: String): SrcRegisterField {
            val sizeInt = try {
                text.toUInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected integer bit count but got '$text'.", e)
            }
            return SrcRegisterField(sizeInt.toInt(), 0, null)
        }
    }
}
