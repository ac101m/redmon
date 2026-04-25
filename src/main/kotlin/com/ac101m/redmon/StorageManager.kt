package com.ac101m.redmon

import com.ac101m.redmon.isa.InstructionSet
import com.ac101m.redmon.isa.InstructionSetRegistry
import com.ac101m.redmon.persistence.StorageReader
import com.ac101m.redmon.persistence.v2.PersistentInstructionSetV2
import com.ac101m.redmon.persistence.v2.PersistentStorageV2
import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.profile.ProfileRegistry
import com.ac101m.redmon.utils.Config.Companion.REDMON_VERSION
import com.ac101m.redmon.utils.RedmonException
import com.ac101m.redmon.utils.UnsupportedProfileVersionException
import com.ac101m.redmon.world.WorldMetadata
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.outputStream

/**
 * Class manages storage of profiles and versioning of profile storage files.
 *
 * @param profileStoragePath Path to profile storage file.
 * @param worldMetadataStoragePath Path to world metadata storage file.
 * @param instructionSetStoragePath Path to instruction set storage file.
 * @param jsonMapper Jackson object mapper for stored profile deserialisation.
 * @param storageReader Reader for reading profiles.
 */
class StorageManager(
    private val profileStoragePath: Path,
    private val worldMetadataStoragePath: Path,
    private val instructionSetStoragePath: Path,
    private val jsonMapper: ObjectMapper,
    private val storageReader: StorageReader
) {
    init {
        validateOrCreateStorage(profileStoragePath)
        validateOrCreateStorage(worldMetadataStoragePath)
        validateOrCreateStorage(instructionSetStoragePath)
    }

    /**
     * Load profiles from disk.
     *
     * @param instructionSetRegistry The instruction set registry. Required to look up active ISAs on pages.
     * @return A list of loaded [Profile] objects.
     */
    fun loadProfiles(instructionSetRegistry: InstructionSetRegistry): List<Profile> {
        return loadStorage(profileStoragePath).profiles.map {
            Profile.fromPersistentProfile(it, instructionSetRegistry)
        }
    }

    /**
     * Load stored world metadata from disk.
     *
     * @return A list of loaded world metadata objects.
     */
    fun loadWorldMetadata(profileRegistry: ProfileRegistry): List<WorldMetadata> {
        return loadStorage(worldMetadataStoragePath).worldData.map {
            WorldMetadata.fromPersistent(profileRegistry, it)
        }
    }

    /**
     * Load stored instruction sets from disk.
     *
     * @return A list of loaded instruction set objects.
     */
    fun loadInstructionSets(): List<InstructionSet> {
        return loadStorage(instructionSetStoragePath).instructionSets.map {
            InstructionSet.fromPersistent(it)
        }
    }

    /**
     * Save profiles to disk.
     *
     * @param profiles The profiles to store.
     */
    fun saveProfiles(profiles: List<Profile>) {
        val persistentProfiles = profiles.map { it.toPersistentProfile() }
        val persistentStorage = PersistentStorageV2(
            version = StorageReader.MAX_SUPPORTED_VERSION,
            modVersion = REDMON_VERSION,
            profiles = persistentProfiles
        )
        saveStorage(profileStoragePath, persistentStorage)
    }

    /**
     * Save world metadata to disk.
     *
     * @param worldMetadata The world metadata objects to save.
     */
    fun saveWorldMetadata(worldMetadata: List<WorldMetadata>) {
        val persistentWorldMetadata = worldMetadata.filter { !it.isEmpty() }.map { it.toPersistent() }
        val persistentStorage = PersistentStorageV2(
            version = StorageReader.MAX_SUPPORTED_VERSION,
            modVersion = REDMON_VERSION,
            worldData = persistentWorldMetadata
        )
        saveStorage(worldMetadataStoragePath, persistentStorage)
    }

    /**
     * Save instruction sets to disk.
     *
     * @param instructionSets The instruction sets to save.
     */
    fun saveInstructionSets(instructionSets: List<InstructionSet>) {
        val persistentInstructionSets = instructionSets.map { it.toPersistent() }
        val persistentStorage = PersistentStorageV2(
            version = StorageReader.MAX_SUPPORTED_VERSION,
            modVersion = REDMON_VERSION,
            instructionSets = persistentInstructionSets
        )
        saveStorage(instructionSetStoragePath, persistentStorage)
    }

    private fun loadStorage(path: Path): PersistentStorageV2 {
        return try {
            storageReader.readStorageFromJsonStream(getInputStream(path))
        } catch (e: UnsupportedProfileVersionException) {
            throw UnsupportedProfileVersionException(
                cause = e,
                message = "Failed to load persistent storage due to a profile versioning problem. Please remove or " +
                        "backup the profiles at $path, or use the appropriate version of the mod"
            )
        } catch (e: Exception) {
            throw IllegalStateException("Failed to load storage at $path, unknown error.", e)
        }
    }

    private fun saveStorage(path: Path, persistentStorage: PersistentStorageV2) {
        path.outputStream().use { outputStream ->
            jsonMapper.writer().writeValue(outputStream, persistentStorage)
        }
    }

    private fun validateOrCreateStorage(path: Path) {
        if (!path.exists()) {
            val emptyPersistentStorage = PersistentStorageV2(
                version = StorageReader.MAX_SUPPORTED_VERSION,
                modVersion = REDMON_VERSION
            )
            saveStorage(path, emptyPersistentStorage)
        } else if (!path.isRegularFile()) {
            error("Storage file at '$path' is not a regular file.")
        }
    }

    companion object {
        private fun getInputStream(path: Path): InputStream {
            if (!path.exists()) {
                throw RedmonException("No such file '$path'.")
            }
            if (!path.isRegularFile()) {
                throw RedmonException("File '$path' is not a regular file.")
            }
            return path.inputStream()
        }
    }
}
