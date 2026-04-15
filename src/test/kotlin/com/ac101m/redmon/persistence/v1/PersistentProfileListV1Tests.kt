package com.ac101m.redmon.persistence.v1

import com.ac101m.redmon.persistence.PersistentProfileList
import com.ac101m.redmon.profile.SignalType
import com.ac101m.redmon.utils.mapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PersistentProfileListV1Tests {

    @Test
    fun `Round trip test`() {
        val original = PersistentProfileListV1(
            listOf(
                PersistentProfileV1(
                    name = "a_profile",
                    signals = listOf(
                        PersistentSignalV1(
                            name = "r1",
                            type = SignalType.REPEATER,
                            invert = false,
                            bitLocations = listOf(
                                PersistentSignalBitV1(
                                    x = 0,
                                    y = 1,
                                    z = 2
                                )
                            )
                        )
                    )
                )
            )
        )

        val serialized = mapper.writeValueAsString(original)
        val deserialized = mapper.readValue(serialized, PersistentProfileListV1::class.java)

        assertThat(deserialized).isEqualTo(original)
    }

    @Test
    fun `Round trip test with real data`() {
        val stream = this::class.java.classLoader.getResourceAsStream("profiles/test-profiles-v1.json")!!
        val original = PersistentProfileList.load(stream)

        assertThat(original).isInstanceOf(PersistentProfileListV1::class.java)

        val serialized = mapper.writeValueAsString(original)
        val deserialized = mapper.readValue(serialized, PersistentProfileListV1::class.java)

        assertThat(deserialized).isEqualTo(original)
    }
}
