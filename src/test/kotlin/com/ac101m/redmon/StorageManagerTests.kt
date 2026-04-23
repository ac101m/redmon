package com.ac101m.redmon

import com.ac101m.redmon.persistence.StorageReader
import com.ac101m.redmon.profile.ProfileRegistry
import com.ac101m.redmon.utils.UnsupportedProfileVersionException
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

    private val testMapper = ObjectMapper().registerKotlinModule()
    private val storageReader = StorageReader(testMapper)

    private fun testFile(resourceName: String): Path {
        val stream = this::class.java.classLoader.getResourceAsStream(resourceName)!!
        val testFilePath = temporaryFolder.resolve(UUID.randomUUID().toString())
        val outputStream = testFilePath.outputStream()
        stream.copyTo(outputStream)
        return testFilePath.toPath()
    }

    private fun testManager(path: Path) = StorageManager(
        path,
        path,
        path,
        testMapper,
        storageReader
    )

    @Test
    fun `Loading a file with a non matching (high) version results in an exception`() {
        val file = testFile("profiles/absurd-high-version.json")
        val manager = testManager(file)

        val e = assertThrows<UnsupportedProfileVersionException> {
            manager.loadProfiles()
        }

        assertThat(e).hasMessageContaining("Failed to load persistent storage due to a profile versioning problem")
        assertThat(e).hasMessageContaining("Please remove or backup the profiles")
        assertThat(e).hasMessageContaining("or use the appropriate version of the mod")
    }

    @Test
    fun `Loading a file with a non matching (low) version results in an exception`() {
        val file = testFile("profiles/absurd-low-version.json")
        val manager = testManager(file)

        val e = assertThrows<UnsupportedProfileVersionException> {
            manager.loadProfiles()
        }

        assertThat(e).hasMessageContaining("Failed to load persistent storage due to a profile versioning problem")
        assertThat(e).hasMessageContaining("Please remove or backup the profiles")
        assertThat(e).hasMessageContaining("or use the appropriate version of the mod")
    }

    @Test
    fun `Can load a v1 storage file`() {
        val file = testFile("profiles/test-profiles-v1.json")
        val manager = testManager(file)

        val profiles = assertDoesNotThrow {
            manager.loadProfiles()
        }

        assertThat(profiles[0].name).isEqualTo("jampu1.fetch")

        val worldMetadata = assertDoesNotThrow {
            manager.loadWorldMetadata(ProfileRegistry(profiles))
        }

        assertThat(worldMetadata).hasSize(0)
    }

    @Test
    fun `Can load a v2 storage file`() {
        val file = testFile("profiles/test-profiles-v2.json")
        val manager = testManager(file)

        val profiles = assertDoesNotThrow {
            manager.loadProfiles()
        }

        assertThat(profiles[0].name).isEqualTo("test_profile")
        assertThat(profiles[1].name).isEqualTo("jampu1")

        val worldMetadata = assertDoesNotThrow {
            manager.loadWorldMetadata(ProfileRegistry(profiles))
        }

        assertThat(worldMetadata).hasSize(0)
    }

    @Test
    fun `Can load a v3 storage file`() {
        val file = testFile("profiles/test-profiles-v3.json")
        val manager = testManager(file)

        val profiles = assertDoesNotThrow {
            manager.loadProfiles()
        }

        assertThat(profiles[0].name).isEqualTo("test_profile")
        assertThat(profiles[1].name).isEqualTo("jampu1")

        val worldMetadata = assertDoesNotThrow {
            manager.loadWorldMetadata(ProfileRegistry(profiles))
        }

        assertThat(worldMetadata).hasSize(1)
    }
}
