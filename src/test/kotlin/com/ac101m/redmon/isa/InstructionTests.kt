package com.ac101m.redmon.isa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource

class InstructionTests {
    val testOpcodeField = InstructionField(FieldType.OPCODE, 0, 4)
    val testOpcodeBits = "0101"

    @Test
    fun `If an invalid opcode bit pattern is supplied, an error is generated`() {
        val e = assertThrows<IllegalArgumentException> {
            Instruction("TST", 16, "nope", listOf(testOpcodeField), null)
        }
        assertThat(e).hasMessage("Opcode bits are not a valid binary string.")
    }

    @Test
    fun `If no opcode field is supplied, an error is generated`() {
        val e = assertThrows<IllegalArgumentException> {
            Instruction("TST", 16, testOpcodeBits, emptyList(), null)
        }
        assertThat(e).hasMessage("Opcode field is missing.")
    }

    @Test
    fun `If the opcode bit pattern does not match the size of the opcode field, an error is generated`() {
        val e = assertThrows<IllegalArgumentException> {
            Instruction("TST", 16, testOpcodeBits, listOf(testOpcodeField, testOpcodeField), null)
        }
        assertThat(e).hasMessage("Multiple opcode fields are present.")
    }

    @Test
    fun `If the opcode bit pattern does not have the same size as the opcode field, an error is generated`() {
        val e = assertThrows<IllegalArgumentException> {
            Instruction("TST", 16, "00000", listOf(testOpcodeField), null)
        }
        assertThat(e).hasMessage("Opcode bit pattern does not match opcode field length.")
    }

    @Test
    fun `If one or more fields do not fit within the instruction, an error is generated`() {
        val e = assertThrows<IllegalArgumentException> {
            Instruction("TST", 3, "0000", listOf(testOpcodeField), null)
        }
        assertThat(e).hasMessage("One or more fields do not fit within the instruction.")
    }

    @ParameterizedTest
    @ValueSource(strings = ["0000000000000000", "1111111111111111", "1001100110011000", "1010101010100110"])
    fun `When an instruction word does not match, opcodeMatches() returns false`(testBitPattern: String) {
        val testInstructionSize = 16
        val testBits = testBitPattern.toULong(2)

        assertThat(testBitPattern).hasSize(testInstructionSize)

        val instruction = Instruction(
            name = "TST",
            size = testInstructionSize,
            opcodeBitPattern = "1001",
            fields = listOf(testOpcodeField),
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

        val instruction = Instruction(
            name = "TST",
            size = testInstructionSize,
            opcodeBitPattern = "1001",
            fields = listOf(testOpcodeField),
            description = "A test instruction"
        )

        assertThat(instruction.opcodeMatches(testBits)).isTrue
    }

    @ParameterizedTest
    @MethodSource("conflictingOpcodePatterns")
    fun `When instructions have conflicting opcodes, conflictsWith() returns true`(
        opcode1: String,
        opcode1Offset: Int,
        opcode2: String,
        opcode2Offset: Int
    ) {
        val testInstructionSize = 16

        val instruction1 = Instruction(
            name = "TST1",
            size = testInstructionSize,
            opcodeBitPattern = opcode1,
            fields = listOf(InstructionField(FieldType.OPCODE, opcode1Offset, opcode1.length)),
            description = "A test instruction"
        )

        val instruction2 = Instruction(
            name = "TST2",
            size = testInstructionSize,
            opcodeBitPattern = opcode2,
            fields = listOf(InstructionField(FieldType.OPCODE, opcode2Offset, opcode2.length)),
            description = "Another test instruction"
        )

        assertThat(instruction1.conflictsWith(instruction2)).isTrue
    }

    @ParameterizedTest
    @MethodSource("nonConflictingOpcodePatterns")
    fun `When instructions do not have conflicting opcodes, conflictsWith() returns false`(
        opcode1: String,
        opcode1Offset: Int,
        opcode2: String,
        opcode2Offset: Int
    ) {
        val testInstructionSize = 16

        val instruction1 = Instruction(
            name = "TST1",
            size = testInstructionSize,
            opcodeBitPattern = opcode1,
            fields = listOf(InstructionField(FieldType.OPCODE, opcode1Offset, opcode1.length)),
            description = "A test instruction"
        )

        val instruction2 = Instruction(
            name = "TST2",
            size = testInstructionSize,
            opcodeBitPattern = opcode2,
            fields = listOf(InstructionField(FieldType.OPCODE, opcode2Offset, opcode2.length)),
            description = "Another test instruction"
        )

        assertThat(instruction1.conflictsWith(instruction2)).isFalse
    }

    @ParameterizedTest
    @MethodSource("validInstructionStrings")
    fun `Valid instructions can be successfully constructed from text`(name: String, fields: List<String>) {
        val instruction = assertDoesNotThrow {
            Instruction.createFromArgs(name, fields, null)
        }

        instruction.apply {
            assertThat(size).isEqualTo(16)
            assertThat(name).isEqualTo(name)
        }
    }

    companion object {

        @JvmStatic
        fun conflictingOpcodePatterns() = listOf<Arguments>(
            Arguments.of("1010", 0, "1010", 0),     // Identical
            Arguments.of("1010", 0, "1010", 8),     // Non overlapping
            Arguments.of("1010", 0, "01010", 0),    // Identical within overlapping section, same offset
            Arguments.of("1010", 0, "1010", 2)      // Identical with overlapping section, different offsets
        )

        @JvmStatic
        fun nonConflictingOpcodePatterns() = listOf<Arguments>(
            Arguments.of("1010", 0, "1011", 0),     // Non-identical
            Arguments.of("1010", 0, "11011", 0),    // Non-identical within overlapping section
            Arguments.of("1010", 0, "1011", 2),     // Non-identical within overlapping section, different offsets
            Arguments.of("110010", 0, "10100", 5)   // Non-identical one shared bit
        )

        @JvmStatic
        fun validInstructionStrings() = listOf<Arguments>(
            Arguments.of("LSLI", listOf("opcode:01001", "ignore:3", "imm_u:4", "reg_addr:4")),
            Arguments.of("MOV", listOf("opcode:0000001", "flag_bit:S", "reg_addr:4", "reg_addr:4"))
        )
    }
}
