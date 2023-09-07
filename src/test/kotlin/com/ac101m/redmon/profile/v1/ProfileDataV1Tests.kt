package com.ac101m.redmon.profile.v1

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test


class ProfileDataV1Tests {
    private val objectMapper = ObjectMapper()

    @Test
    fun `round trip test`() {
        val original = ProfileDataV1(
            name = "profile_1"
        )

        val serialized = objectMapper.writeValueAsString(original)
        val deserialized = objectMapper.readValue(serialized, ProfileDataV1::class.java)

        check(deserialized == original)
    }
}
