package com.ac101m.redmon.isa

import com.ac101m.redmon.isa.instruction.UnsignedImmediateField
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InstructionFieldTests {

    @Test
    fun `Creating a field with size zero results in an error`() {
        val e = assertThrows<IllegalArgumentException> {
            UnsignedImmediateField(0, 0)
        }
        assertThat(e).hasMessage("Instruction field size must be greater than zero.")
    }

    @Test
    fun `Creating a field with excessive size results in an error`() {
        val testSize = 1
        val testOffset = 9001

        val e = assertThrows<IllegalArgumentException> {
            UnsignedImmediateField(testSize, testOffset)
        }

        assertThat(e).hasMessage(
            "Maximum extent of ${testSize + testOffset} exceeds maximum instruction length."
        )
    }

    @Test
    fun `Creating a field with an extent that exceeds the max instruction size results in an error`() {
        val offset = 61
        val size = 4

        val e = assertThrows<IllegalArgumentException> {
            UnsignedImmediateField(offset, size)
        }

        val expectedExtent = offset + size

        assertThat(e).hasMessage("Maximum extent of $expectedExtent exceeds maximum instruction length.")
    }
}
