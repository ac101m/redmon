package com.ac101m.redmon

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.util.UUID

class StorageManagerTests {
    @TempDir
    lateinit var temporaryFolder: File
    private val mapper = ObjectMapper().registerKotlinModule()

    private fun testFile(resourceName: String): Path {
        val stream = this::class.java.classLoader.getResourceAsStream(resourceName)!!
        val testFilePath = temporaryFolder.resolve(UUID.randomUUID().toString())
        val outputStream = testFilePath.outputStream()
        stream.copyTo(outputStream)
        return testFilePath.toPath()
    }

    @Test
    fun `Loading a file with a non matching (high) version results in an exception`() {
        val file = testFile("profiles/absurd-high-version.json")
        val manager = StorageManager(mapper, file)

        val e = assertThrows<IllegalStateException> {
            manager.loadProfiles()
        }

        assertThat(e).hasMessageContaining("Redmon was unable to load stored profiles")
        assertThat(e).hasMessageContaining("update to a more recent version of the mod")
    }

    @Test
    fun `Loading a file with a non matching (low) version results in an exception`() {
        val file = testFile("profiles/absurd-low-version.json")
        val manager = StorageManager(mapper, file)

        val e = assertThrows<IllegalStateException> {
            manager.loadProfiles()
        }

        assertThat(e).hasMessageContaining("Redmon was unable to load stored profiles")
        assertThat(e).hasMessageContaining("revert to an earlier version of the mod")
    }

    @Test
    fun `Can load a v1 profile storage file`() {
        val file = testFile("profiles/test-profiles-v1.json")
        val manager = StorageManager(mapper, file)

        val profiles = assertDoesNotThrow {
            manager.loadProfiles()
        }

        assertThat(profiles[0].name).isEqualTo("jampu1.fetch")
    }
}
