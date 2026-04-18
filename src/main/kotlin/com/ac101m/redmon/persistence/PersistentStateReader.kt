package com.ac101m.redmon.persistence

import com.ac101m.redmon.persistence.v1.PersistentProfileListV1
import com.ac101m.redmon.utils.RedmonException
import com.ac101m.redmon.utils.UnsupportedProfileVersionException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream

/**
 * Class contains logic for managing persistent object upgrades.
 */
class PersistentStateReader(val mapper: ObjectMapper) {

    /**
     * Get the persistence object from a stream.
     */
    fun readPersistenceObject(inputStream: InputStream): PersistentProfileList {
        val json = mapper.readTree(inputStream)
        val storageVersionInfo = StorageVersionInfo.fromJsonNode(json)

        if (storageVersionInfo.version < MIN_SUPPORTED_VERSION) {
            throw UnsupportedProfileVersionException(
                "Redmon was unable to read the provided profile. The profile was written by an older version of the " +
                        "mod (${storageVersionInfo.modVersion}) and is no longer supported."
            )
        }

        if (storageVersionInfo.version > MAX_SUPPORTED_VERSION) {
            throw UnsupportedProfileVersionException(
                "Redmon was unable to read the provided profile. The profile was written by a more recent version of " +
                        "the mod (${storageVersionInfo.modVersion}) and is not supported."
            )
        }

        val storageSubtype = getStorageSubtype(storageVersionInfo.version)
        return readPersistentProfileList(json, storageSubtype)
    }

    private fun readPersistentProfileList(json: JsonNode, storageType: Class<out PersistentProfileList>): PersistentProfileList {
        val profileList = try {
            mapper.treeToValue(json, storageType)
        } catch (e: Exception) {
            throw RedmonException("Failed to load profile information, error parsing json.", e)
        }
        return profileList
    }

    companion object {
        const val MIN_SUPPORTED_VERSION = 1
        const val MAX_SUPPORTED_VERSION = 2

        fun getStorageSubtype(version: Int) = when (version) {
            /**
             * Initial version (1.0.0-pre4 and earlier)
             * Technically some incompatibility between other pre-releases but whatever.
             * Frozen. Do not change.
             */
            1 -> PersistentProfileListV1::class.java

            /**
             * - Support for torches (new signal type)
             * Unreleased. May change.
             */
            2 -> PersistentProfileListV1::class.java

            else -> error("Storage $version does not correspond to a known subtype")
        }
    }
}
