package com.ac101m.redmon.persistence.v1

import com.ac101m.redmon.utils.mapper
import org.junit.jupiter.api.Test


class SaveDataV1Tests {

    @Test
    fun `round trip test`() {
        val original = SaveDataV1().apply {
            addProfile(
                ProfileV1(
                    name = "a_profile",
                    registers = listOf(
                        RegisterV1(
                            name = "r1",
                            bits = listOf(
                                WatchPointV1(
                                    x = 0,
                                    y = 1,
                                    z = 2,
                                    type = "repeater"
                                )
                            )
                        )
                    )
                )
            )
        }

        val serialized = mapper.writeValueAsString(original)
        val deserialized = mapper.readValue(serialized, SaveDataV1::class.java)

        check(deserialized == original)
    }
}
