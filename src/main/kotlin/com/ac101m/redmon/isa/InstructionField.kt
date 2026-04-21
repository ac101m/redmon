package com.ac101m.redmon.isa

import com.ac101m.redmon.utils.computeMask

/**
 * Represents a field within an instruction.
 *
 * @property type The type of the bit field.
 * @property offset The offset within the instruction where the field begins.
 * @property size The size of the field in bits.
 * @property data Optional string to attach to the field.
 */
class InstructionField(
    val type: FieldType,
    val offset: Int,
    val size: Int,
    val data: String? = null
) {
    val mask = computeMask(size) shl offset

    init {
        require(size > 0) {
            "Instruction field size must be greater than zero."
        }
        require(size <= type.maxSize) {
            "Fields of type $type may have a size of at most ${type.maxSize} bits."
        }
        require(size + offset <= Instruction.MAX_INSTRUCTION_SIZE) {
            "Maximum extent of ${size + offset} exceeds maximum instruction length."
        }
    }

    companion object {
        /**
         * Get instruction field from text.
         * Used for reading instruction fields from user input.
         *
         * @param text Input string to parse.
         * @param offset The offset of the field.
         */
        fun of(text: String, offset: Int): InstructionField {
            val tokens = text.split(":")

            require(tokens.size == 2) {
                "Expected colon separated instruction specification but got '$text'."
            }

            val key = tokens.first()
            val value = tokens.last()

            return when (key) {
                FieldType.IGNORE.key -> ignoreFromString(value, offset)
                FieldType.OPCODE.key -> opcodeFromString(value, offset)
                FieldType.FLAG_BIT.key -> flagBitFromString(value, offset)
                FieldType.REGISTER_ADDRESS.key -> registerAddressFromString(value, offset)
                FieldType.IMMEDIATE_UNSIGNED.key -> unsignedImmediateFromString(value, offset)
                FieldType.IMMEDIATE_SIGNED.key -> signedImmediateFromString(value, offset)
                else -> {
                    val validKeyNames = FieldType.entries.joinToString(", ") { it.key }
                    error("Unrecognized instruction field type '$key'. Valid field types are: $validKeyNames.")
                }
            }
        }

        private fun ignoreFromString(sizeText: String, offset: Int): InstructionField {
            val sizeInt = try {
                sizeText.toUInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected an unsigned integer but got '$sizeText'.", e)
            }
            return InstructionField(FieldType.IGNORE, offset, sizeInt.toInt())
        }

        private fun opcodeFromString(patternText: String, offset: Int): InstructionField {
            try {
                patternText.toULong(2)
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected a bit pattern but got '$patternText'.", e)
            }
            return InstructionField(FieldType.OPCODE, offset, patternText.length, patternText)
        }

        private fun flagBitFromString(flagString: String, offset: Int): InstructionField {
            require(flagString.length == 1) {
                "Expected single flag bit character but got '$flagString'."
            }
            return InstructionField(FieldType.FLAG_BIT, offset, 1, flagString)
        }

        private fun registerAddressFromString(sizeText: String, offset: Int): InstructionField {
            val sizeInt = try {
                sizeText.toUInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected an unsigned integer but got '$sizeText'.", e)
            }
            return InstructionField(FieldType.REGISTER_ADDRESS, offset, sizeInt.toInt())
        }

        private fun unsignedImmediateFromString(sizeText: String, offset: Int): InstructionField {
            val sizeInt = try {
                sizeText.toUInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected an unsigned integer but got '$sizeText'.", e)
            }
            return InstructionField(FieldType.IMMEDIATE_UNSIGNED, offset, sizeInt.toInt())
        }

        private fun signedImmediateFromString(sizeText: String, offset: Int): InstructionField {
            val sizeInt = try {
                sizeText.toUInt()
            } catch (e: NumberFormatException) {
                throw IllegalArgumentException("Expected an unsigned integer but got '$sizeText'.", e)
            }
            return InstructionField(FieldType.IMMEDIATE_SIGNED, offset, sizeInt.toInt())
        }
    }
}
