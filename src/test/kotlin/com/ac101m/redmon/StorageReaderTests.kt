package com.ac101m.redmon

import com.ac101m.redmon.persistence.StorageReader
import com.ac101m.redmon.persistence.v2.PersistentStorageV2
import com.ac101m.redmon.utils.UnsupportedProfileVersionException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.InputStream

class StorageReaderTests {
    val testMapper = ObjectMapper().registerKotlinModule()
    val stateReader = StorageReader(testMapper)

    @Test
    fun `Reading a file with a non matching (high) version results in an exception`() {
        val stream = getStream("profiles/absurd-high-version.json")

        val e = assertThrows<UnsupportedProfileVersionException> {
            stateReader.readStorageFromJsonStream(stream)
        }

        assertThat(e).hasMessageContaining("Redmon was unable to read profile data")
        assertThat(e).hasMessageContaining("The profile was written by a more recent version of the mod")
    }

    @Test
    fun `Loading a file with a non matching (low) version results in an exception`() {
        val stream = getStream("profiles/absurd-low-version.json")

        val e = assertThrows<UnsupportedProfileVersionException> {
            stateReader.readStorageFromJsonStream(stream)
        }

        assertThat(e).hasMessageContaining("Redmon was unable to read profile data")
        assertThat(e).hasMessageContaining("The profile was written by an older version of the mod")
    }

    @Test
    fun `Can read a v1 profile`() {
        val stream = getStream("profiles/test-profiles-v1.json")

        val persistentObject = assertDoesNotThrow {
            stateReader.readStorageFromJsonStream(stream)
        }

        assertThat(persistentObject).isInstanceOf(PersistentStorageV2::class.java)
        assertThat(persistentObject.version).isEqualTo(3)
    }

    @Test
    fun `Can read a v2 profile`() {
        val stream = getStream("profiles/test-profiles-v2.json")

        val persistentObject = assertDoesNotThrow {
            stateReader.readStorageFromJsonStream(stream)
        }

        assertThat(persistentObject).isInstanceOf(PersistentStorageV2::class.java)
        assertThat(persistentObject.version).isEqualTo(3)
    }

    @Test
    fun `Can read a v3 profile`() {
        val stream = getStream("profiles/test-profiles-v3.json")

        val persistentObject = assertDoesNotThrow {
            stateReader.readStorageFromJsonStream(stream)
        }

        assertThat(persistentObject).isInstanceOf(PersistentStorageV2::class.java)
        assertThat(persistentObject.version).isEqualTo(3)
    }

    companion object {
        private fun getStream(resourceName: String): InputStream {
            return this::class.java.classLoader.getResourceAsStream(resourceName)!!
        }
    }
}
