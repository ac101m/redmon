package com.ac101m.redmon.profile.v1

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test


class ProfileRegistryV1Tests {
    private val objectMapper = ObjectMapper()

    @Test
    fun `round trip test`() {
        val original = ProfileRegistryV1().apply {
            addContext(
                "a_world_id",
                ProfileContextV1().apply {
                    addProfile(ProfileDataV1("a_profile"))
                }
            )
        }

        val serialized = objectMapper.writeValueAsString(original)
        val deserialized = objectMapper.readValue(serialized, ProfileRegistryV1::class.java)

        check(deserialized == original)
    }
}
