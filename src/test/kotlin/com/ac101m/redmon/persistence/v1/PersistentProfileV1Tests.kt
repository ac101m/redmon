package com.ac101m.redmon.persistence.v1

import com.ac101m.redmon.StorageManager
import com.ac101m.redmon.profile.SignalType
import org.junit.jupiter.api.Test

class PersistentProfileV1Tests {

    @Test
    fun `round trip test`() {
        val original = PersistentProfileV1(
            name = "profile_1",
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

        val serialized = StorageManager.mapper.writeValueAsString(original)
        val deserialized = StorageManager.mapper.readValue(serialized, PersistentProfileV1::class.java)

        check(deserialized == original)
    }
}
