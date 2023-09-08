package com.ac101m.redmon.profile.v1

import com.ac101m.redmon.utils.mapper
import org.junit.jupiter.api.Test


class SaveDataV1Tests {

    @Test
    fun `round trip test`() {
        val original = SaveDataV1().apply {
            addProfile(ProfileV1("a_profile"))
        }

        val serialized = mapper.writeValueAsString(original)
        val deserialized = mapper.readValue(serialized, SaveDataV1::class.java)

        check(deserialized == original)
    }
}
