package com.ac101m.redmon

import com.ac101m.redmon.persistence.PersistentProfileList
import com.ac101m.redmon.persistence.StorageVersionInfo
import com.ac101m.redmon.persistence.v1.PersistentProfileListV1
import com.ac101m.redmon.persistence.v1.PersistentProfileV1
import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.utils.Config.Companion.REDMON_VERSION
import com.ac101m.redmon.utils.RedmonException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
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
 */
class StorageManager(private val profileStoragePath: Path) {
    init {
        if (!profileStoragePath.exists()) {
            saveProfiles(emptyList())
        } else if (!profileStoragePath.isRegularFile()) {
            error("Profile storage file at '$profileStoragePath' is not a regular file.")
        }
    }

    /**
     * Load profiles from disk.
     *
     * @return A list of loaded [Profile] objects.
     */
    fun loadProfiles(): List<Profile> {
        val storageVersionInfo = readVersionInfo(profileStoragePath)

        check(storageVersionInfo.version >= MIN_SUPPORTED_VERSION) {
            "Redmon was unable to load stored profiles. Profiles were written by an older version of the mod " +
                    "(${storageVersionInfo.modVersion}). Please remove or create backups of the profile storage " +
                    "file at $profileStoragePath or revert to an earlier version of the mod."
        }

        check(storageVersionInfo.version <= MAX_SUPPORTED_VERSION) {
            "Redmon was unable to load stored profiles. Profiles were written by a newer version of the mod " +
                    "(${storageVersionInfo.modVersion}). Please remove or create backups of the profile storage " +
                    "file at $profileStoragePath or update to a more recent version of the mod."
        }

        return when (val storageSubtype = getStorageSubtype(storageVersionInfo.version)) {
            PersistentProfileListV1::class.java -> loadProfilesV1()
            else -> error("Unrecognised profile storage subtype $storageSubtype")
        }
    }

    /**
     * Save profiles to disk.
     *
     * @param profiles The profiles to store.
     */
    fun saveProfiles(profiles: List<Profile>) {
        val persistentProfiles = profiles.map { it.toPersistentV1() }
        val persistenceObject = PersistentProfileListV1(
            version = CURRENT_STORAGE_VERSION,
            modVersion = REDMON_VERSION,
            profiles = persistentProfiles
        )
        save(persistenceObject, profileStoragePath)
    }

    private fun loadProfilesV1(): List<Profile> {
        val profileList = readPersistentProfileList<PersistentProfileListV1>(profileStoragePath)
        return profileList.profiles.map { Profile.fromPersistentV1(it) }
    }

    companion object {
        const val MIN_SUPPORTED_VERSION = 1
        const val MAX_SUPPORTED_VERSION = 1
        const val CURRENT_STORAGE_VERSION = 1

        val mapper = ObjectMapper().registerKotlinModule()

        fun readVersionInfo(path: Path): StorageVersionInfo {
            return readVersionInfo(getInputStream(path))
        }

        fun readVersionInfo(inputStream: InputStream): StorageVersionInfo {
            return mapper.readValue(inputStream, StorageVersionInfo::class.java)
        }

        inline fun <reified T: PersistentProfileList> readPersistentProfileList(path: Path): T {
            return readPersistentProfileList<T>(getInputStream(path))
        }

        inline fun <reified T: PersistentProfileList> readPersistentProfileList(inputStream: InputStream): T {
            val profileList = try {
                mapper.readValue(inputStream, T::class.java)
            } catch (e: Exception) {
                throw RedmonException("Failed to load profile information, error parsing storage file.", e)
            }
            return profileList
        }

        fun getInputStream(path: Path): InputStream {
            if (!path.exists()) {
                throw RedmonException("No such file '$path'.")
            }
            if (!path.isRegularFile()) {
                throw RedmonException("File '$path' is not a regular file.")
            }
            return path.inputStream()
        }

        fun getStorageSubtype(version: Int) = when (version) {
            /**
             * Initial version (1.0.0-pre4 and earlier)
             * Technically some incompatibility between other pre-releases but whatever.
             * Frozen. Do not change.
             */
            1 -> PersistentProfileListV1::class.java

            else -> error("Storage $version does not correspond to a known subtype")
        }

        fun save(profileList: PersistentProfileList, path: Path) {
            path.outputStream().use { outputStream ->
                mapper.writer().writeValue(outputStream, profileList)
            }
        }
    }
}
