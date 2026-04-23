package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.isa.InstructionLayout
import com.ac101m.redmon.persistence.v2.PersistentInstructionFieldV2
import com.ac101m.redmon.utils.computeMask

abstract class Field(
    val size: Int,
    val offset: Int
) {
    val mask = computeMask(size) shl offset

    abstract val maxSize: Int

    init {
        require(size > 0) {
            "Instruction field size must be greater than zero."
        }
        require(size <= maxSize) {
            "Field may have a size of at most $maxSize bits."
        }
        require(size + offset <= InstructionLayout.MAX_INSTRUCTION_SIZE) {
            "Maximum extent of ${size + offset} exceeds maximum instruction length."
        }
    }

    /**
     * Returns true if it overlaps with the other instruction field.
     *
     * @param other The other instruction field to check.
     */
    fun overlapsWith(other: Field): Boolean {
        return mask and other.mask != 0UL
    }

    /**
     * Returns true if the specified bit index falls within the field.
     *
     * @param bitIndex The index of the bit to check for.
     */
    fun containsBit(bitIndex: Int): Boolean {
        return bitIndex >= offset && bitIndex < (offset + size)
    }

    /**
     * Get a bit-level representation of the field as a string.
     */
    abstract fun bitRepresentation(): String

    abstract fun toPersistent(): PersistentInstructionFieldV2

    companion object {
        fun fromPersistent(persistent: PersistentInstructionFieldV2): Field {
            return when (persistent.type) {
                FieldType.IGNORE -> IgnoreField.fromPersistent(persistent)
                FieldType.OPCODE -> OpcodeField.fromPersistent(persistent)
                FieldType.FLAG_BIT -> FlagBitField.fromPersistent(persistent)
                FieldType.REGISTER_ADDRESS -> RegisterAddressField.fromPersistent(persistent)
                FieldType.UNSIGNED_IMMEDIATE -> UnsignedImmediateField.fromPersistent(persistent)
                FieldType.SIGNED_IMMEDIATE -> SignedImmediateField.fromPersistent(persistent)
            }
        }

        /**
         * Get instruction field from text.
         * Used for reading instruction fields from user input.
         *
         * @param text Input string to parse.
         * @param offset The offset of the field.
         */
        fun of(text: String, offset: Int): Field {
            val tokens = text.split(":")

            require(tokens.size == 2) {
                "Expected colon separated instruction specification but got '$text'."
            }

            val key = tokens.first()
            val value = tokens.last()

            return when (key) {
                FieldType.IGNORE.commandKey -> IgnoreField.of(value, offset)
                FieldType.OPCODE.commandKey -> OpcodeField.of(value, offset)
                FieldType.FLAG_BIT.commandKey -> FlagBitField.of(value, offset)
                FieldType.REGISTER_ADDRESS.commandKey -> RegisterAddressField.of(value, offset)
                FieldType.UNSIGNED_IMMEDIATE.commandKey -> UnsignedImmediateField.of(value, offset)
                FieldType.SIGNED_IMMEDIATE.commandKey -> SignedImmediateField.of(value, offset)
                else -> {
                    val validKeyNames = FieldType.entries.joinToString(", ") { it.commandKey }
                    error("Unrecognized instruction field type '$key'. Valid field types are: $validKeyNames.")
                }
            }
        }
    }
}
