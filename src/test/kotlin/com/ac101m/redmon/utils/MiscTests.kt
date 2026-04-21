package com.ac101m.redmon.utils

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Percentage
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class MiscTests {

    @Test
    fun `ceilDiv works as expected when no overflow is present`() {
        assertThat(4.ceilDiv(2)).isEqualTo(2)
        assertThat(64.ceilDiv(4)).isEqualTo(16)
    }

    @Test
    fun `ceilDiv works as expected when an overflow is present`() {
        assertThat(5.ceilDiv(2)).isEqualTo(3)
        assertThat(65.ceilDiv(4)).isEqualTo(17)
    }

    @ParameterizedTest
    @MethodSource("halfPrecisionTestValues")
    fun `Janky half precision conversion works (more or less) as it's supposed to`(bits: Int, expected: Double) {
        val float = floatFromFp16Bits(bits)
        assertThat(float.toDouble()).isCloseTo(expected, Percentage.withPercentage(0.05))
    }

    companion object {
        @JvmStatic
        fun halfPrecisionTestValues() = listOf<Arguments>(
            Arguments.of(0x4248, 3.141592653589793),
            Arguments.of(0xc248, -3.141592653589793),
            Arguments.of(0x7200, 12288.0),
            Arguments.of(0xF200, -12288.0)
        )
    }
}
