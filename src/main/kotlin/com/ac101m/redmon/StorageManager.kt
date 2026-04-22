package com.ac101m.redmon

import com.ac101m.redmon.persistence.StorageReader
import com.ac101m.redmon.persistence.v2.PersistentStorageV2
import com.ac101m.redmon.profile.Profile
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
 * @param jsonMapper Jackson object mapper for stored profile deserialisation.
 * @param storageReader Reader for reading profiles.
 */
class StorageManager(
    private val profileStoragePath: Path,
    private val jsonMapper: ObjectMapper,
    private val storageReader: StorageReader
) {
    init {
        if (!profileStoragePath.exists()) {
            saveState(emptyList(), emptyList())
        } else if (!profileStoragePath.isRegularFile()) {
            error("Profile storage file at '$profileStoragePath' is not a regular file.")
        }
    }

    /**
     * Load profiles from disk.
     *
     * @return A list of loaded [Profile] objects.
     */
    fun loadStorage(): PersistentStorageV2 {
        val inputStream = getInputStream(profileStoragePath)

        val persistentStorage = try {
            storageReader.readStorageFromJsonStream(inputStream)
        } catch (e: UnsupportedProfileVersionException) {
            throw UnsupportedProfileVersionException(
                cause = e,
                message = "Failed to load stored profiles due to a profile versioning problem. Please remove or " +
                        "backup the profiles at $profileStoragePath, or use the appropriate version of the mod"
            )
        } catch (e: Exception) {
            throw IllegalStateException("Failed to load stored profiles.", e)
        }

        return persistentStorage
    }

    /**
     * Save profiles and world metadata to disk.
     *
     * @param profiles The profiles to store.
     * @param worldMetadata World metadata items to store.
     */
    fun saveState(profiles: List<Profile>, worldMetadata: List<WorldMetadata>) {
        val persistentProfiles = profiles.map { it.toPersistentProfile() }
        val persistentWorldMetadata = worldMetadata.filter { !it.isEmpty() }.map { it.toPersistent() }
        val persistentStorage = PersistentStorageV2(
            version = StorageReader.MAX_SUPPORTED_VERSION,
            modVersion = REDMON_VERSION,
            profiles = persistentProfiles,
            worldData = persistentWorldMetadata
        )
        profileStoragePath.outputStream().use { outputStream ->
            jsonMapper.writer().writeValue(outputStream, persistentStorage)
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
