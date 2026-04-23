package com.ac101m.redmon.isa.instruction

import com.ac101m.redmon.isa.InstructionLayout
import com.ac101m.redmon.persistence.v2.PersistentInstructionFieldV2
import com.ac101m.redmon.utils.computeMask

abstract class Field(
    val size: Int,
    initOffset: Int
) {
    val mask get() = computeMask(size) shl offset
    var offset: Int = initOffset
        set(newValue) {
            val extent = size + newValue
            require(extent <= InstructionLayout.MAX_INSTRUCTION_SIZE) {
                "Maximum extent of $extent exceeds maximum instruction length."
            }
            field = newValue
        }

    abstract val maxSize: Int

    init {
        require(size > 0) {
            "Instruction field size must be greater than zero."
        }
        require(size <= maxSize) {
            "Field may have a size of at most $maxSize bits."
        }
        offset = initOffset
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
     * Optionally cross out the characters and make them grey.
     *
     * @param crossOut Grey out characters and replace with hyphens (default: false).
     */
    abstract fun bitRepresentation(crossOut: Boolean = false): String

    /**
     * Convert to persistence object.
     */
    abstract fun toPersistent(): PersistentInstructionFieldV2

    /**
     * Get description text.
     */
    abstract fun descriptionText(): String

    companion object {
        fun fromPersistent(persistent: PersistentInstructionFieldV2): Field {
            return when (persistent.type) {
                FieldType.IGNORE -> IgnoreField.fromPersistent(persistent)
                FieldType.OPCODE -> OpcodeField.fromPersistent(persistent)
                FieldType.FLAG_BIT -> FlagBitField.fromPersistent(persistent)
                FieldType.REGISTER_READ -> RegisterField.fromPersistent(persistent)
                FieldType.REGISTER_WRITE -> RegisterField.fromPersistent(persistent)
                FieldType.REGISTER_READ_WRITE -> RegisterField.fromPersistent(persistent)
                FieldType.UNSIGNED_IMMEDIATE -> UnsignedImmediateField.fromPersistent(persistent)
                FieldType.SIGNED_IMMEDIATE -> SignedImmediateField.fromPersistent(persistent)
            }
        }

        /**
         * Get instruction field from text.
         * Used for reading instruction fields from user input.
         * Note this does not configure the offset, which must be manually set after instantiation.
         *
         * @param text Input string to parse.
         */
        fun of(text: String): Field {
            val tokens = text.split(":", limit = 2)

            require(tokens.size == 2) {
                "Expected colon separated field specification but got '$text'."
            }

            val key = tokens.first()
            val value = tokens.last()

            return when (key) {
                FieldType.IGNORE.commandKey -> IgnoreField.of(value)
                FieldType.OPCODE.commandKey -> OpcodeField.of(value)
                FieldType.FLAG_BIT.commandKey -> FlagBitField.of(value)
                FieldType.REGISTER_READ.commandKey -> SrcRegisterField.of(value)
                FieldType.REGISTER_WRITE.commandKey -> DestRegisterField.of(value)
                FieldType.REGISTER_READ_WRITE.commandKey -> SrcDestRegisterField.of(value)
                FieldType.UNSIGNED_IMMEDIATE.commandKey -> UnsignedImmediateField.of(value)
                FieldType.SIGNED_IMMEDIATE.commandKey -> SignedImmediateField.of(value)
                else -> {
                    val validKeyNames = FieldType.entries.joinToString(", ") { it.commandKey }
                    error("Unrecognized instruction field type '$key'. Valid field types are: $validKeyNames.")
                }
            }
        }
    }
}
