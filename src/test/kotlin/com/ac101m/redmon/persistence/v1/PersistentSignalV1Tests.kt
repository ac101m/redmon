package com.ac101m.redmon.persistence.v1

import com.ac101m.redmon.profile.SignalFormat
import com.ac101m.redmon.profile.SignalType
import com.ac101m.redmon.utils.mapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PersistentSignalV1Tests {

    @Test
    fun `round trip test`() {
        val original = PersistentSignalV1(
            name = "r1",
            type = SignalType.REPEATER,
            invert = false,
            blockLocations = listOf(
                PersistentSignalBitV1(
                    x = 0,
                    y = 1,
                    z = 2
                )
            )
        )

        val serialized = mapper.writeValueAsString(original)
        val deserialized = mapper.readValue(serialized, PersistentSignalV1::class.java)

        assertThat(deserialized).isEqualTo(original)
    }

    @Test
    fun `invert field defaults to false`() {
        val signalName = "a_signal"
        val signalType = SignalType.REPEATER
        val signalFormat = SignalFormat.UNSIGNED

        val jsonWithoutNegate = """
            {
                "name": "$signalName",
                "type": "$signalType",
                "format": "$signalFormat",
                "bitLocations": [
                    { "x": 0, "y": 1, "z": 2 }
                ]
            }
        """.trimIndent()

        val signal = mapper.readValue(jsonWithoutNegate, PersistentSignalV1::class.java)

        assertThat(signal.invert).isFalse
    }

    @Test
    fun `format field defaults to unsigned`() {
        val signalName = "a_signal"
        val signalType = SignalType.REPEATER
        val invert = false

        val jsonWithoutFormat = """
            {
                "name": "$signalName",
                "type": "$signalType",
                "invert": $invert,
                "bitLocations": [
                    { "x": 0, "y": 1, "z": 2 }
                ]
            }
        """.trimIndent()

        val signal = mapper.readValue(jsonWithoutFormat, PersistentSignalV1::class.java)

        assertThat(signal.format).isEqualTo(SignalFormat.UNSIGNED)
    }
}
