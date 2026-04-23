package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.isa.InstructionLayout
import com.ac101m.redmon.persistence.v2.PersistentInstructionFieldV2
import com.ac101m.redmon.utils.Colour

class UnsignedImmediateField(
    size: Int,
    offset: Int
) : Field(size, offset) {
    override val maxSize get() = InstructionLayout.MAX_INSTRUCTION_SIZE

    override fun bitRepresentation(): String {
        val sb = StringBuilder(size * 2)
        sb.append(COLOUR.prefix)
        repeat(size) { sb.append('U') }
        return sb.toString()
    }

    override fun toPersistent(): PersistentInstructionFieldV2 {
        return PersistentInstructionFieldV2(FieldType.UNSIGNED_IMMEDIATE, size, offset)
    }

    companion object {
        val COLOUR = Colour.DARK_GREEN

        fun fromPersistent(persistent: PersistentInstructionFieldV2): UnsignedImmediateField {
            return UnsignedImmediateField(persistent.size, persistent.offset)
        }

        fun of(sizeText: String, offset: Int): UnsignedImmediateField {
            val sizeInt = try {
                sizeText.toUInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected an unsigned integer but got '$sizeText'.", e)
            }
            return UnsignedImmediateField(sizeInt.toInt(), offset)
        }
    }
}
