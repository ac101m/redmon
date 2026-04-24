package com.ac101m.redmon.isa

import com.ac101m.redmon.isa.instruction.DecodableField
import com.ac101m.redmon.isa.instruction.DestRegisterField
import com.ac101m.redmon.isa.instruction.Field
import com.ac101m.redmon.isa.instruction.FlagBitField
import com.ac101m.redmon.isa.instruction.IgnoreField
import com.ac101m.redmon.isa.instruction.OpcodeField
import com.ac101m.redmon.isa.instruction.SrcDestRegisterField
import com.ac101m.redmon.persistence.v2.PersistentInstructionLayoutV2
import com.ac101m.redmon.utils.Colour

/**
 * Represents an instruction within an instruction set.
 * Instructions consist of a set of fields, exactly one of which must be an opcode field.
 *
 * @property name The name of the instruction.
 * @property size The size of the instruction in bits.
 * @param fields The fields within the instruction.
 * @property description Optional description of the instruction.
 */
class InstructionLayout(
    var name: String,
    val size: Int,
    var description: String?,
    private val fields: List<Field>
) {
    val opcodeBitPattern: String

    private val opcodeBits: ULong
    private val opcodeField: Field

    init {
        for (a in fields) {
            for (b in fields) {
                if (a !== b) {
                    require(!a.overlapsWith(b)) {
                        "One or more fields overlap with eachother."
                    }
                }
            }
        }

        val opcodeFields = fields.filterIsInstance<OpcodeField>().apply {
            require(isNotEmpty()) {
                "Opcode field is missing."
            }
            require(size < 2) {
                "Multiple opcode fields are present."
            }
        }

        for (field in fields) {
            require(field.offset + field.size <= size) {
                "One or more fields do not fit within the instruction."
            }
        }

        opcodeField = opcodeFields.single()
        opcodeBitPattern = opcodeField.bitPattern

        opcodeBits = try {
            (opcodeBitPattern.toULong(2) shl opcodeField.offset) and opcodeField.mask
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Opcode bits are not a valid binary string.", e)
        }
    }

    /**
     * Return true if the supplied bit pattern matches the opcode of this instruction.
     *
     * @param instructionBits The bits of the instruction to check.
     */
    fun opcodeMatches(instructionBits: ULong): Boolean {
        val instructionBitsMasked = instructionBits and opcodeField.mask
        return instructionBitsMasked == opcodeBits
    }

    /**
     * Returns true if the supplied instructions opcode collides with the opcode of this instruction.
     * This method is used to check if instruction opcodes are uniquely identifiable.
     *
     * @param other Instruction to compare with.
     */
    fun conflictsWith(other: InstructionLayout): Boolean {
        val opcodeMask = opcodeField.mask
        val otherOpcodeMask = other.opcodeField.mask
        val sharedOpcodeMask = opcodeMask and otherOpcodeMask
        val maskedOpcodeBits = opcodeBits and sharedOpcodeMask
        val otherMaskedOpcodeBits = other.opcodeBits and sharedOpcodeMask
        return maskedOpcodeBits == otherMaskedOpcodeBits
    }

    /**
     * Pretty print the instruction (single line).
     */
    fun prettyPrint(highlightField: Field? = null): String {
        val sb = StringBuilder(size * 2)
        var i = 0

        sb.append('[')

        while (i < size) {
            val field = getFieldAtBit((size - 1) - i)

            if (field != null) {
                val crossOut = highlightField != null && field !== highlightField
                sb.append(field.bitRepresentation(crossOut))
                i += field.size
            } else {
                sb.append(IgnoreField.COLOUR.prefix)
                sb.append('X')
                i++
            }
        }

        sb.append(Colour.WHITE.prefix)
        sb.append(']')

        return sb.toString()
    }

    /**
     * Dump all information about the instruction to a multi-line string.
     */
    fun infoString(): String {
        val sb = StringBuilder()

        sb.append(name)
        sb.append(":\n")
        sb.append(descriptionText())
        sb.append('\n')

        fields.forEachIndexed { i, field ->
            sb.append(i + 1)
            sb.append(' ')
            sb.append(prettyPrint(field))
            sb.append(" - ")
            sb.append(field.descriptionText())
            if (i < fields.size - 1) {
                sb.append('\n')
            }
        }

        return sb.toString()
    }

    private fun getFieldAtBit(bitIndex: Int): Field? {
        require(bitIndex < size) {
            "Bit index out of range."
        }

        for (field in fields) {
            if (field.containsBit(bitIndex)) {
                return field
            }
        }

        return null
    }

    fun descriptionText(): String {
        return description ?: "No description."
    }

    fun setFieldDescription(index: Int, newDescription: String) {
        require(index < fields.size) {
            "Field index out of range."
        }
        fields[index].description = newDescription
    }

    fun disassemble(bits: ULong): String {
        val flagStringBuilder = StringBuilder(fields.size)
        val strings = ArrayList<String>(fields.size)

        val sortedFields = fields.sortedBy {
            if (it is DestRegisterField || it is SrcDestRegisterField) 0 else 1
        }

        sortedFields.forEach { field ->
            if (field is FlagBitField) {
                if (field.mask and bits != 0UL) {
                    flagStringBuilder.append(field.char)
                }
            } else if (field is DecodableField) {
                strings.add(field.decode(bits))
            }
        }

        val flagString = if (flagStringBuilder.isEmpty()) {
            ""
        } else {
            "[$flagStringBuilder]"
        }

        return "$name$flagString ${strings.joinToString(", ")}"
    }

    fun toPersistent(): PersistentInstructionLayoutV2 {
        val persistentFields = fields.map { it.toPersistent() }
        return PersistentInstructionLayoutV2(name, description, persistentFields)
    }

    companion object {
        const val MAX_INSTRUCTION_SIZE = 64

        fun fromPersistent(size: Int, persistent: PersistentInstructionLayoutV2): InstructionLayout {
            return InstructionLayout(
                name = persistent.name,
                size = size,
                description = persistent.description,
                fields = persistent.fields.map { Field.fromPersistent(it) }
            )
        }

        fun createFromArgs(name: String, fieldText: List<String>, description: String?): InstructionLayout {
            val parsedFields = ArrayList<Field>()
            var instructionSize = 0

            for (str in fieldText) {
                try {
                    val field = Field.of(str)
                    parsedFields.add(field)
                    instructionSize += field.size
                } catch (e: Exception) {
                    throw IllegalArgumentException("Error near '$str': ${e.message ?: e.javaClass}")
                }
            }

            var fieldOffset = instructionSize

            // Compute offsets from the left
            for (field in parsedFields) {
                field.offset = fieldOffset - field.size
                fieldOffset -= field.size
            }

            check(fieldOffset == 0)

            return InstructionLayout(
                name = name,
                size = instructionSize,
                fields = parsedFields,
                description = description
            )
        }
    }
}
