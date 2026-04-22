package com.ac101m.redmon.isa

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class InstructionSetTests {
    val testInstructionSetName = "test-isa-${UUID.randomUUID()}"

    @Test
    fun `Creating an instruction set with zero length instructions causes an error`() {
        val e = assertThrows<IllegalArgumentException> {
            InstructionSet(testInstructionSetName, 0)
        }
        assertThat(e).hasMessage("Instruction size must be greater than zero.")
    }

    @Test
    fun `Creating an instruction set with a size which exceeds to max instruction size causes an error`() {
        val testSize = (InstructionLayout.MAX_INSTRUCTION_SIZE + 1 until Int.MAX_VALUE).random()

        val e = assertThrows<IllegalArgumentException> {
            InstructionSet(testInstructionSetName, testSize)
        }

        assertThat(e).hasMessageContaining("Instruction size must be less than ${InstructionLayout.MAX_INSTRUCTION_SIZE}")
    }
}
