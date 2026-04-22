package com.ac101m.redmon.persistence

/**
 * Interface for all profile storage subtypes.
 */
interface PersistentStorage {
    /**
     * Persistent storage version.
     * If this is incremented, this indicates a breaking change in the profile format has occurred.
     */
    val version: Int

    /**
     * Persistent storage author version.
     * String contains the version string of the mod version that loaded the profile.
     */
    val modVersion: String?
}
