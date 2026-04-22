package com.ac101m.redmon.isa.instruction

/**
 * Represents instruction field type.
 *
 * @property key The name by which the field is identified in commands.
 */
enum class FieldType(val key: String) {
    IGNORE("ignore"),
    OPCODE("opcode"),
    FLAG_BIT("flag_bit"),
    REGISTER_ADDRESS("reg_addr"),
    IMMEDIATE_UNSIGNED("imm_u"),
    IMMEDIATE_SIGNED("imm_s")
}
