package com.ac101m.redmon.persistence.v1

import com.ac101m.redmon.profile.RegisterFormat
import com.ac101m.redmon.profile.RegisterType
import com.ac101m.redmon.utils.mapper
import org.junit.jupiter.api.Test


class PersistentRegisterV1Tests {

    @Test
    fun `round trip test`() {
        val original = PersistentRegisterV1(
            name = "r1",
            type = RegisterType.REPEATER,
            invert = false,
            bitLocations = listOf(
                PersistentRegisterBitV1(
                    x = 0,
                    y = 1,
                    z = 2
                )
            )
        )

        val serialized = mapper.writeValueAsString(original)
        val deserialized = mapper.readValue(serialized, PersistentRegisterV1::class.java)

        check(deserialized == original)
    }

    @Test
    fun `invert field defaults to false`() {
        val registerName = "a_register"
        val registerType = RegisterType.REPEATER
        val registerFormat = RegisterFormat.UNSIGNED

        val jsonWithoutNegate = """
            {
                "name": "$registerName",
                "type": "$registerType",
                "format": "$registerFormat",
                "bitLocations": [
                    { "x": 0, "y": 1, "z": 2 }
                ]
            }
        """.trimIndent()

        val register = mapper.readValue(jsonWithoutNegate, PersistentRegisterV1::class.java)

        check(!register.invert)
    }

    @Test
    fun `format field defaults to unsigned`() {
        val registerName = "a_register"
        val registerType = RegisterType.REPEATER
        val invert = false

        val jsonWithoutFormat = """
            {
                "name": "$registerName",
                "type": "$registerType",
                "invert": $invert,
                "bitLocations": [
                    { "x": 0, "y": 1, "z": 2 }
                ]
            }
        """.trimIndent()

        val register = mapper.readValue(jsonWithoutFormat, PersistentRegisterV1::class.java)
        val jsonOut = mapper.writeValueAsString(register)

        check(register.format == RegisterFormat.UNSIGNED)
    }
}
