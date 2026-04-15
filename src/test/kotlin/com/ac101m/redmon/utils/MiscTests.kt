package com.ac101m.redmon.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

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
}
