package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.isa.InstructionLayout
import com.ac101m.redmon.utils.Colour

/**
 * Represents a register address field within an instruction.
 *
 * @param size The size of the field.
 * @param offset The offset of the field.
 */
class RegisterAddressField(
    size: Int,
    offset: Int
) : Field(size, offset) {
    override val maxSize get() = InstructionLayout.MAX_INSTRUCTION_SIZE

    override fun bitRepresentation(): String {
        val sb = StringBuilder()
        sb.append(COLOUR.prefix)
        repeat(size) { sb.append('R') }
        return sb.toString()
    }

    companion object {
        val COLOUR = Colour.AQUA

        fun of(text: String, offset: Int): RegisterAddressField {
            val sizeInt = try {
                text.toUInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected an unsigned integer but got '$text'.", e)
            }
            return RegisterAddressField(sizeInt.toInt(), offset)
        }
    }
}
