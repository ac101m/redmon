package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.isa.InstructionLayout
import com.ac101m.redmon.persistence.v2.PersistentInstructionFieldV2
import com.ac101m.redmon.utils.Colour

class SignedImmediateField(
    size: Int,
    offset: Int,
    description: String?
) : Field(size, offset, description) {
    override val maxSize get() = InstructionLayout.MAX_INSTRUCTION_SIZE
    override val colour get() = COLOUR

    override fun bitRepresentation(crossOut: Boolean): String {
        return StringBuilder(size * 2).apply {
            if (crossOut) {
                append(CROSSED_OUT_COLOUR.prefix)
                repeat(size) { append('-') }
            } else {
                append(colour.prefix)
                repeat(size) { append('S') }
            }
        }.toString()
    }

    override fun descriptionText(): String {
        return description ?: "$size bit signed immediate."
    }

    override fun toPersistent(): PersistentInstructionFieldV2 {
        return PersistentInstructionFieldV2(
            type = FieldType.SIGNED_IMMEDIATE,
            size = size,
            offset = offset,
            metadata = null,
            description = description
        )
    }

    companion object {
        val COLOUR = Colour.GREEN

        fun fromPersistent(persistent: PersistentInstructionFieldV2): SignedImmediateField {
            return SignedImmediateField(persistent.size, persistent.offset, persistent.description)
        }

        fun of(sizeText: String): SignedImmediateField {
            val sizeInt = try {
                sizeText.toUInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected an unsigned integer but got '$sizeText'.", e)
            }
            return SignedImmediateField(sizeInt.toInt(), 0, null)
        }
    }
}
