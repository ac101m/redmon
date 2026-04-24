package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.isa.InstructionLayout
import com.ac101m.redmon.persistence.v2.PersistentInstructionFieldV2
import com.ac101m.redmon.utils.Colour

class SignedImmediateField(
    size: Int,
    offset: Int,
    description: String?
) : ParameterField(size, offset, description), DecodableField {
    override val maxSize get() = InstructionLayout.MAX_INSTRUCTION_SIZE
    override val displayColour get() = COLOUR
    override val displayChar get() = 'S'

    override fun descriptionText(): String {
        return description ?: "$size bit signed immediate."
    }

    override fun decode(bits: ULong): String {
        val maskedBits = (bits and mask) shr offset
        val signMask = 1UL shl (size - 1)
        return if (signMask and maskedBits == 0UL) {
            maskedBits.toString()
        } else {
            val signExtended = maskedBits or (mask shr offset).inv()
            signExtended.toLong().toString()
        }
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
