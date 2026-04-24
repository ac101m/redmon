package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.isa.InstructionLayout

/**
 * Represents a field within an instruction which must have a specific bit pattern.
 * Used for matching, e.g. opcodes.
 *
 * @param bitPattern The bit pattern to use.
 * @param offset The offset of the field within the instruction.
 * @param description Optional user defined description of the field.
 */
abstract class LiteralField(
    val bitPattern: String,
    offset: Int,
    description: String?
) : Field(bitPattern.length, offset, description) {
    override val maxSize get() = InstructionLayout.MAX_INSTRUCTION_SIZE

    init {
        try {
            bitPattern.toULong(2)
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Expected binary bit pattern but got '$bitPattern'.", e)
        }
    }

    override fun bitRepresentation(crossOut: Boolean): String {
        return StringBuilder(size * 2).apply {
            if (crossOut) {
                append(CROSSED_OUT_COLOUR.prefix)
                repeat(bitPattern.length) { append('-') }
            } else {
                append(displayColour.prefix)
                append(bitPattern)
            }
        }.toString()
    }
}
