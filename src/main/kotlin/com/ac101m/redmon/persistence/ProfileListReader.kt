package com.ac101m.redmon.persistence

import com.ac101m.redmon.persistence.v1.PersistentProfileListV1
import com.ac101m.redmon.persistence.v2.PersistentColumnV2
import com.ac101m.redmon.persistence.v2.PersistentPageV2
import com.ac101m.redmon.persistence.v2.PersistentProfileListV2
import com.ac101m.redmon.persistence.v2.PersistentProfileV2
import com.ac101m.redmon.persistence.v2.PersistentBlockV2
import com.ac101m.redmon.persistence.v2.PersistentSignalV2
import com.ac101m.redmon.utils.RedmonException
import com.ac101m.redmon.utils.UnsupportedProfileVersionException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream

/**
 * Class contains logic for managing persistent object upgrades.
 */
class ProfileListReader(val mapper: ObjectMapper) {

    /**
     * Get the persistence object from a stream of JSON text.
     *
     * @param inputStream The input stream to read from.
     */
    fun readProfileListFromJsonStream(inputStream: InputStream): PersistentProfileListV2 {
        val json = mapper.readTree(inputStream)
        val profileListVersionInfo = ProfileListVersionInfo.fromJsonNode(json)

        if (profileListVersionInfo.version < MIN_SUPPORTED_VERSION) {
            throw UnsupportedProfileVersionException(
                "Redmon was unable to read profile data. The profile was written by an older version of the " +
                        "mod (${profileListVersionInfo.modVersion}) and is no longer supported by this version."
            )
        }

        if (profileListVersionInfo.version > MAX_SUPPORTED_VERSION) {
            throw UnsupportedProfileVersionException(
                "Redmon was unable to read profile data. The profile was written by a more recent version of " +
                        "the mod (${profileListVersionInfo.modVersion}) and is not supported by this version."
            )
        }

        val storageSubtype = getStorageSubtype(profileListVersionInfo.version)
        val profileList = readPersistentProfileList(json, storageSubtype)

        return upgradeToLatest(profileList)
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
             * Support for torches (new signal type)
             * Profile pagination and columns.
             * Frozen. Do not change.
             */
            2 -> PersistentProfileListV2::class.java

            else -> error("Storage $version does not correspond to a known subtype")
        }

        /**
         * Converts a persistent profile list in any format to the current format.
         *
         * @param profileList Input persistent profile list in any format.
         */
        fun upgradeToLatest(profileList: PersistentProfileList): PersistentProfileListV2 {
            return when (profileList.version) {
                1 -> upgradeV1toV2(profileList as PersistentProfileListV1)
                2 -> profileList as PersistentProfileListV2
                else -> error("Unrecognized profile list version ${profileList.version}")
            }
        }

        /**
         * Major structural upgrade.
         * V2 format contains pages and columns, features not present in v1.
         * Each old profile is converted into a new one with a single page and single column.
         */
        fun upgradeV1toV2(oldProfileList: PersistentProfileListV1): PersistentProfileListV2 {
            check(oldProfileList.version == 1) {
                "Expected profile list version to be 1, but got ${oldProfileList.version}"
            }
            return PersistentProfileListV2(
                version = 2,
                modVersion = null,
                profiles = oldProfileList.profiles.map { oldProfile -> PersistentProfileV2(
                    name = oldProfile.name,
                    pages = listOf(PersistentPageV2(
                        name = "Page 1",
                        columns = listOf(PersistentColumnV2(
                            signals = oldProfile.signals.map { oldSignal -> PersistentSignalV2(
                                name = oldSignal.name,
                                type = oldSignal.type,
                                invert = oldSignal.invert,
                                format = oldSignal.format,
                                blocks = oldSignal.blockLocations.map { oldBlock ->
                                    PersistentBlockV2(oldBlock.x, oldBlock.y, oldBlock.z)
                                }
                            )}
                        ))
                    ))
                )}
            )
        }
    }
}
