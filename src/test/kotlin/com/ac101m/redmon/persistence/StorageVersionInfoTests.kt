package com.ac101m.redmon.persistence

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.util.UUID

class StorageVersionInfoTests {
    val mapper = ObjectMapper().registerKotlinModule()

    private fun getJsonObject(text: String): JsonNode {
        return mapper.readTree(ByteArrayInputStream(text.toByteArray()))
    }

    @Test
    fun `If the version field is missing, an exception is thrown`() {
        val text = """{ "some_random_field": "hello" }"""

        val json = getJsonObject(text)

        val e = assertThrows<IllegalStateException> {
            StorageVersionInfo.fromJsonNode(json)
        }

        assertThat(e).hasMessageContaining("version field is missing or null")
    }

    @Test
    fun `If the version field is null, an exception is thrown`() {
        val text = """{ "version": null }"""

        val json = getJsonObject(text)

        val e = assertThrows<IllegalStateException> {
            StorageVersionInfo.fromJsonNode(json)
        }

        assertThat(e).hasMessageContaining("version field is missing or null")
    }

    @Test
    fun `If the version field is not a string, an exception is thrown`() {
        val text = """{ "version": [ 42 ] }"""

        val json = getJsonObject(text)

        val e = assertThrows<IllegalStateException> {
            StorageVersionInfo.fromJsonNode(json)
        }

        assertThat(e).hasMessageContaining("version field is not text")
    }

    @Test
    fun `If the version field is a non-integer string, an exception is thrown`() {
        val text = """{ "version": "not an integer lmao" }"""

        val json = getJsonObject(text)

        val e = assertThrows<IllegalStateException> {
            StorageVersionInfo.fromJsonNode(json)
        }

        assertThat(e).hasMessageContaining("version field could not be converted to a number")
    }

    @Test
    fun `If the version field is a valid integer string, it is returned correctly`() {
        val testValue = (0 until Int.MAX_VALUE).random()
        val text = """{ "version": "$testValue" }"""

        val json = getJsonObject(text)
        val versionInfo = StorageVersionInfo.fromJsonNode(json)

        assertThat(versionInfo.version).isEqualTo(testValue)
    }

    @Test
    fun `If the mod version is missing, the returned mod version will be null`() {
        val text = """{ "version": "1" }"""

        val json = getJsonObject(text)
        val versionInfo = StorageVersionInfo.fromJsonNode(json)

        assertThat(versionInfo.modVersion).isNull()
    }

    @Test
    fun `If the mod version is null, the returned mod version will be null`() {
        val text = """{ "version": "1", "mod_version": null }"""

        val json = getJsonObject(text)
        val versionInfo = StorageVersionInfo.fromJsonNode(json)

        assertThat(versionInfo.modVersion).isNull()
    }

    @Test
    fun `If the mod version is not a string, an exception is thrown`() {
        val text = """{
            "version": "1",
            "mod_version": [ "Wait a minute... That's not a string!" ]
        }"""

        val json = getJsonObject(text)

        val e = assertThrows<IllegalStateException> {
            StorageVersionInfo.fromJsonNode(json)
        }

        assertThat(e).hasMessageContaining("mod version field is not text")
    }

    @Test
    fun `If the mod version is a string, it is returned correctly`() {
        val testValue = UUID.randomUUID().toString()
        val text = """{ "version": "1", "mod_version": "$testValue" }"""

        val json = getJsonObject(text)
        val versionInfo = StorageVersionInfo.fromJsonNode(json)

        assertThat(versionInfo.modVersion).isEqualTo(testValue)
    }
}
