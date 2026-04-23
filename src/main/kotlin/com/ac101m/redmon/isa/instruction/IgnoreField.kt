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
    offset: Int
) : Field(size, offset) {
    override val maxSize get() = InstructionLayout.MAX_INSTRUCTION_SIZE

    override fun bitRepresentation(): String {
        val sb = StringBuilder(size * 2)
        sb.append(COLOUR.prefix)
        repeat(size) { sb.append('X') }
        return sb.toString()
    }

    override fun toPersistent(): PersistentInstructionFieldV2 {
        return PersistentInstructionFieldV2(FieldType.IGNORE, size, offset)
    }

    companion object {
        val COLOUR = Colour.GRAY

        fun fromPersistent(persistent: PersistentInstructionFieldV2): IgnoreField {
            return IgnoreField(persistent.size, persistent.offset)
        }

        fun of(text: String, offset: Int): IgnoreField {
            val sizeInt = try {
                text.toUInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected an unsigned integer but got '$text'.", e)
            }
            return IgnoreField(sizeInt.toInt(), offset)
        }
    }
}
