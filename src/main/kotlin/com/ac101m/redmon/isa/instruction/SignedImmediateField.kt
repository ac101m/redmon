package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.isa.InstructionLayout
import com.ac101m.redmon.persistence.v2.PersistentInstructionFieldV2
import com.ac101m.redmon.utils.Colour

class SignedImmediateField(
    size: Int,
    offset: Int
) : Field(size, offset) {
    override val maxSize get() = InstructionLayout.MAX_INSTRUCTION_SIZE

    override fun bitRepresentation(): String {
        val sb = StringBuilder()
        sb.append(COLOUR.prefix)
        repeat(size) { sb.append('S') }
        return sb.toString()
    }

    override fun toPersistent(): PersistentInstructionFieldV2 {
        return PersistentInstructionFieldV2(FieldType.SIGNED_IMMEDIATE, size, offset)
    }

    companion object {
        val COLOUR = Colour.GREEN

        fun fromPersistent(persistent: PersistentInstructionFieldV2): SignedImmediateField {
            return SignedImmediateField(persistent.size, persistent.offset)
        }

        fun of(sizeText: String, offset: Int): SignedImmediateField {
            val sizeInt = try {
                sizeText.toUInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected an unsigned integer but got '$sizeText'.", e)
            }
            return SignedImmediateField(sizeInt.toInt(), offset)
        }
    }
}
