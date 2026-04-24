package com.ac101m.redmon.isa

import com.ac101m.redmon.isa.instruction.OpcodeField
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class InstructionLayoutTests {
    val testOpcodeField = OpcodeField("0101", 0, null)

    @Test
    fun `If no opcode field is supplied, an error is generated`() {
        val e = assertThrows<IllegalArgumentException> {
            InstructionLayout("TST", 16, null, emptyList())
        }
        assertThat(e).hasMessage("Opcode field is missing.")
    }

    @Test
    fun `If the opcode bit pattern does not match the size of the opcode field, an error is generated`() {
        val e = assertThrows<IllegalArgumentException> {
            InstructionLayout("TST", 16, null, listOf(testOpcodeField, testOpcodeField))
        }
        assertThat(e).hasMessage("Multiple opcode fields are present.")
    }

    @Test
    fun `If one or more fields do not fit within the instruction, an error is generated`() {
        val e = assertThrows<IllegalArgumentException> {
            InstructionLayout("TST", 3, null, listOf(testOpcodeField))
        }
        assertThat(e).hasMessage("One or more fields do not fit within the instruction.")
    }

    @ParameterizedTest
    @ValueSource(strings = ["0000000000000000", "1111111111111111", "1001100110011000", "1010101010100110"])
    fun `When an instruction word does not match, opcodeMatches() returns false`(testBitPattern: String) {
        val testInstructionSize = 16
        val testBits = testBitPattern.toULong(2)

        assertThat(testBitPattern).hasSize(testInstructionSize)

        val instruction = InstructionLayout(
            name = "TST",
            size = testInstructionSize,
            fields = listOf(OpcodeField("1001", 0, null)),
            description = "A test instruction"
        )

        assertThat(instruction.opcodeMatches(testBits)).isFalse
    }

    @ParameterizedTest
    @ValueSource(strings = ["0000000000001001", "1111111111111001", "1001100110011001", "1010101010101001"])
    fun `When an instruction word does match, opcodeMatches() returns true`(testBitPattern: String) {
        val testInstructionSize = 16
        val testBits = testBitPattern.toULong(2)

        assertThat(testBitPattern).hasSize(testInstructionSize)

        val instruction = InstructionLayout(
            name = "TST",
            size = testInstructionSize,
            fields = listOf(OpcodeField("1001", 0, null)),
            description = "A test instruction"
        )

        assertThat(instruction.opcodeMatches(testBits)).isTrue
    }

    @ParameterizedTest
    @MethodSource("conflictingOpcodePatterns")
    fun `When instructions have conflicting opcodes, conflictsWith() returns true`(
        opcodeField1: OpcodeField,
        opcodeField2: OpcodeField
    ) {
        val testInstructionSize = 16

        val instruction1 = InstructionLayout(
            name = "TST1",
            size = testInstructionSize,
            fields = listOf(opcodeField1),
            description = "A test instruction"
        )

        val instruction2 = InstructionLayout(
            name = "TST2",
            size = testInstructionSize,
            fields = listOf(opcodeField2),
            description = "Another test instruction"
        )

        assertThat(instruction1.conflictsWith(instruction2)).isTrue
    }

    @ParameterizedTest
    @MethodSource("nonConflictingOpcodePatterns")
    fun `When instructions do not have conflicting opcodes, conflictsWith() returns false`(
        opcodeField1: OpcodeField,
        opcodeField2: OpcodeField
    ) {
        val testInstructionSize = 16

        val instruction1 = InstructionLayout(
            name = "TST1",
            size = testInstructionSize,
            fields = listOf(opcodeField1),
            description = "A test instruction"
        )

        val instruction2 = InstructionLayout(
            name = "TST2",
            size = testInstructionSize,
            fields = listOf(opcodeField2),
            description = "Another test instruction"
        )

        assertThat(instruction1.conflictsWith(instruction2)).isFalse
    }

    @ParameterizedTest
    @MethodSource("validInstructionStrings")
    fun `Valid instructions can be successfully constructed from text`(name: String, fields: List<String>) {
        val instruction = assertDoesNotThrow {
            InstructionLayout.createFromArgs(name, fields, null)
        }

        instruction.apply {
            assertThat(size).isEqualTo(16)
            assertThat(name).isEqualTo(name)
        }
    }

    companion object {

        @JvmStatic
        fun conflictingOpcodePatterns() = listOf<Arguments>(
            Arguments.of(OpcodeField("1010", 0, null), OpcodeField("1010", 0, null)),     // Identical
            Arguments.of(OpcodeField("1010", 0, null), OpcodeField("1010", 8, null)),     // Non overlapping
            Arguments.of(OpcodeField("1010", 0, null), OpcodeField("01010", 0, null)),    // Identical within overlapping section, same offset
            Arguments.of(OpcodeField("1010", 0, null), OpcodeField("1010", 2, null))      // Identical with overlapping section, different offsets
        )

        @JvmStatic
        fun nonConflictingOpcodePatterns() = listOf<Arguments>(
            Arguments.of(OpcodeField("1010", 0, null), OpcodeField("1011", 0, null)),     // Non-identical
            Arguments.of(OpcodeField("1010", 0, null), OpcodeField("11011", 0, null)),    // Non-identical within overlapping section
            Arguments.of(OpcodeField("1010", 0, null), OpcodeField("1011", 2, null)),     // Non-identical within overlapping section, different offsets
            Arguments.of(OpcodeField("110010", 0, null), OpcodeField("10100", 5, null))   // Non-identical one shared bit
        )

        @JvmStatic
        fun validInstructionStrings() = listOf<Arguments>(
            Arguments.of("LSLI", listOf("opcode:01001", "ignore:3", "imm_u:4", "reg_w:4")),
            Arguments.of("MOV", listOf("opcode:0000001", "flag_bit:S", "reg_r:4", "reg_w:4")),
            Arguments.of("ADD", listOf("opcode:0000001", "flag_bit:S", "reg_r:4", "reg_rw:4")),
        )
    }
}
