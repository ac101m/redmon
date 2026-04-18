package com.ac101m.redmon

import com.ac101m.redmon.persistence.PersistentStateReader
import com.ac101m.redmon.persistence.v1.PersistentProfileListV1
import com.ac101m.redmon.utils.UnsupportedProfileVersionException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.InputStream

class PersistentStateReaderTests {
    val testMapper = ObjectMapper().registerKotlinModule()
    val stateReader = PersistentStateReader(testMapper)

    @Test
    fun `Reading a file with a non matching (high) version results in an exception`() {
        val stream = getStream("profiles/absurd-high-version.json")

        val e = assertThrows<UnsupportedProfileVersionException> {
            stateReader.readPersistenceObject(stream)
        }

        assertThat(e).hasMessageContaining("Redmon was unable to read the provided profile")
        assertThat(e).hasMessageContaining("The profile was written by a more recent version of the mod")
    }

    @Test
    fun `Loading a file with a non matching (low) version results in an exception`() {
        val stream = getStream("profiles/absurd-low-version.json")

        val e = assertThrows<UnsupportedProfileVersionException> {
            stateReader.readPersistenceObject(stream)
        }

        assertThat(e).hasMessageContaining("Redmon was unable to read the provided profile")
        assertThat(e).hasMessageContaining("The profile was written by an older version of the mod")
    }

    @Test
    fun `Can load a v1 profile storage file`() {
        val stream = getStream("profiles/test-profiles-v1.json")

        val persistentObject = assertDoesNotThrow {
            stateReader.readPersistenceObject(stream)
        }

        assertThat(persistentObject).isInstanceOf(PersistentProfileListV1::class.java)
    }

    companion object {
        private fun getStream(resourceName: String): InputStream {
            return this::class.java.classLoader.getResourceAsStream(resourceName)!!
        }
    }
}
