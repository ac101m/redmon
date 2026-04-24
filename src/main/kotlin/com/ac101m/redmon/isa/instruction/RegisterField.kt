package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.isa.InstructionLayout
import com.ac101m.redmon.persistence.v2.PersistentInstructionFieldV2
import com.ac101m.redmon.utils.Colour

/**
 * Represents a register address field within an instruction.
 *
 * @param size The size of the field.
 * @param offset The offset of the field.
 * @param description The description of the register field.
 */
abstract class RegisterField(
    size: Int,
    offset: Int,
    description: String?
) : Field(size, offset, description) {
    override val maxSize get() = InstructionLayout.MAX_INSTRUCTION_SIZE

    override fun bitRepresentation(crossOut: Boolean): String {
        return StringBuilder(size * 2).apply {
            if (crossOut) {
                append(CROSSED_OUT_COLOUR.prefix)
                repeat(size) { append('-') }
            } else {
                append(colour.prefix)
                repeat(size) { append('R') }
            }
        }.toString()
    }

    companion object {
        fun fromPersistent(persistent: PersistentInstructionFieldV2): RegisterField {
            return when (persistent.type) {
                FieldType.REGISTER_READ -> SrcRegisterField.fromPersistent(persistent)
                FieldType.REGISTER_WRITE -> DestRegisterField.fromPersistent(persistent)
                FieldType.REGISTER_READ_WRITE -> SrcDestRegisterField.fromPersistent(persistent)
                else -> error("${persistent.type} is not a register type.")
            }
        }
    }
}
