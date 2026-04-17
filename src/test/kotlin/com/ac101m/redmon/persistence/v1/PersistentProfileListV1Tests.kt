package com.ac101m.redmon.persistence.v1

import com.ac101m.redmon.StorageManager
import com.ac101m.redmon.profile.SignalType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PersistentProfileListV1Tests {

    @Test
    fun `Round trip test`() {
        val original = PersistentProfileListV1(
            1,
            "test-no-version",
            listOf(
                PersistentProfileV1(
                    name = "a_profile",
                    signals = listOf(
                        PersistentSignalV1(
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
                    )
                )
            )
        )

        val serialized = StorageManager.mapper.writeValueAsString(original)
        val deserialized = StorageManager.mapper.readValue(serialized, PersistentProfileListV1::class.java)

        assertThat(deserialized).isEqualTo(original)
    }

    @Test
    fun `Round trip test with real data`() {
        val stream = this::class.java.classLoader.getResourceAsStream("profiles/test-profiles-v1.json")!!
        val original = StorageManager.readPersistentProfileList<PersistentProfileListV1>(stream)

        val serialized = StorageManager.mapper.writeValueAsString(original)
        val deserialized = StorageManager.mapper.readValue(serialized, PersistentProfileListV1::class.java)

        assertThat(deserialized).isEqualTo(original)
    }
}
