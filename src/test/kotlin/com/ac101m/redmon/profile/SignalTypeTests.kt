package com.ac101m.redmon.profile

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class SignalTypeTests {

    @ParameterizedTest
    @EnumSource(SignalType::class)
    fun `Signal type is formatted correctly`(type: SignalType) {
        assertThat(type.maxBlocks * type.bitsPerBlock).isEqualTo(MAX_BIT_COUNT)
    }

    companion object {
        private const val MAX_BIT_COUNT = 64
    }
}
