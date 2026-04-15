package com.ac101m.redmon

import com.ac101m.redmon.persistence.PersistentProfileList
import com.ac101m.redmon.persistence.v1.PersistentProfileListV1
import com.ac101m.redmon.profile.Profile
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile

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

    private fun loadProfilesV1(profileList: PersistentProfileListV1): List<Profile> {
        return profileList.profiles.map { Profile.fromPersistentV1(it) }
    }

    /**
     * Load profiles from disk.
     * Returns a list of [Profile] objects.
     */
    fun loadProfiles(): List<Profile> {
        return when (val persistentProfileList = PersistentProfileList.load(profileStoragePath)) {
            is PersistentProfileListV1 -> loadProfilesV1(persistentProfileList)
        }
    }

    /**
     * Save profiles to disk.
     *
     * @param profiles The profiles to store.
     */
    fun saveProfiles(profiles: List<Profile>) {
        val persistentProfiles = profiles.map { it.toPersistentV1() }
        PersistentProfileListV1(persistentProfiles).save(profileStoragePath)
    }
}
