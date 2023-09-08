package com.ac101m.redmon.profile.v1

import com.ac101m.redmon.utils.mapper
import org.junit.jupiter.api.Test


class ProfileV1Tests {

    @Test
    fun `round trip test`() {
        val original = ProfileV1(
            name = "profile_1"
        )

        val serialized = mapper.writeValueAsString(original)
        val deserialized = mapper.readValue(serialized, ProfileV1::class.java)

        check(deserialized == original)
    }
}
