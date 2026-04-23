package com.ac101m.redmon.isa.instruction

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents instruction field type.
 *
 * @property commandKey The name by which the field type is identified in commands.
 */
enum class FieldType(val commandKey: String) {
    @JsonProperty("ignore")
    IGNORE(commandKey = "ignore"),
    @JsonProperty("op")
    OPCODE(commandKey = "opcode"),
    @JsonProperty("flag")
    FLAG_BIT(commandKey = "flag_bit"),
    @JsonProperty("reg")
    REGISTER_ADDRESS(commandKey = "reg_addr"),
    @JsonProperty("imm_u")
    UNSIGNED_IMMEDIATE(commandKey = "imm_u"),
    @JsonProperty("imm_s")
    SIGNED_IMMEDIATE(commandKey = "imm_s")
}
