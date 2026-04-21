package com.ac101m.redmon.isa

/**
 * Represents instruction field type.
 *
 * @property key The name by which the field is identified in commands.
 * @property maxSize The maximum size of a field of this type in bits.
 */
enum class FieldType(val key: String, val maxSize: Int) {
    IGNORE("ignore", Instruction.MAX_INSTRUCTION_SIZE),
    OPCODE("opcode", Instruction.MAX_INSTRUCTION_SIZE),
    FLAG_BIT("flag_bit", 1),
    REGISTER_ADDRESS("reg_addr", Instruction.MAX_INSTRUCTION_SIZE),
    IMMEDIATE_UNSIGNED("imm_u", Instruction.MAX_INSTRUCTION_SIZE),
    IMMEDIATE_SIGNED("imm_s", Instruction.MAX_INSTRUCTION_SIZE)
}
