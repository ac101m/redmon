package com.ac101m.redmon.isa

import com.ac101m.redmon.isa.instruction.UnsignedImmediateField
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class InstructionFieldTests {

    @Test
    fun `Creating a field with size zero results in an error`() {
        val e = assertThrows<IllegalArgumentException> {
            UnsignedImmediateField(0, 0, null)
        }
        assertThat(e).hasMessage("Instruction field size must be greater than zero.")
    }

    @Test
    fun `Creating a field with excessive size results in an error`() {
        val testSize = 9001
        val testOffset = 1

        val e = assertThrows<IllegalArgumentException> {
            UnsignedImmediateField(testSize, testOffset, null)
        }

        assertThat(e).hasMessage(
            "Field may have a size of at most ${InstructionLayout.MAX_INSTRUCTION_SIZE} bits."
        )
    }

    @Test
    fun `Creating a field with excessive offset results in an error`() {
        val testSize = 1
        val testOffset = 9001

        val e = assertThrows<IllegalArgumentException> {
            UnsignedImmediateField(testSize, testOffset, null)
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
            UnsignedImmediateField(offset, size, null)
        }

        val expectedExtent = offset + size

        assertThat(e).hasMessage("Maximum extent of $expectedExtent exceeds maximum instruction length.")
    }

    @Test
    fun `Changing the offset to an excessive value causes an error`() {
        val initialOffset = 16
        val initialSize = 16

        val field = assertDoesNotThrow {
            UnsignedImmediateField(initialSize, initialOffset, null)
        }

        val e = assertThrows<IllegalArgumentException> {
            field.offset = 9001
        }

        assertThat(e).hasMessage("Maximum extent of ${initialSize + 9001} exceeds maximum instruction length.")
    }
}
