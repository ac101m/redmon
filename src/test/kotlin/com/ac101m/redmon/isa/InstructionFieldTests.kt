package com.ac101m.redmon.isa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class InstructionFieldTests {

    @Test
    fun `Creating a field with size zero results in an error`() {
        val e = assertThrows<IllegalArgumentException> {
            InstructionField(FieldType.OPCODE, 0, 0)
        }
        assertThat(e).hasMessage("Instruction field size must be greater than zero.")
    }

    @ParameterizedTest
    @EnumSource(FieldType::class)
    fun `Creating a field with excessive size results in an error`(fieldType: FieldType) {
        val e = assertThrows<IllegalArgumentException> {
            InstructionField(fieldType, 0, 9001)
        }

        assertThat(e).hasMessage(
            "Fields of type $fieldType may have a size of at most ${fieldType.maxSize} bits."
        )
    }

    @Test
    fun `Creating a field with an extent that exceeds the max instruction size results in an error`() {
        val offset = 61
        val size = 4

        val e = assertThrows<IllegalArgumentException> {
            InstructionField(FieldType.OPCODE, offset, size)
        }

        val expectedExtent = offset + size

        assertThat(e).hasMessage("Maximum extent of $expectedExtent exceeds maximum instruction length.")
    }
}
