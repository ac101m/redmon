package com.ac101m.redmon.persistence

import com.ac101m.redmon.persistence.v1.PersistentStorageV1
import com.ac101m.redmon.persistence.v2.PersistentColumnV2
import com.ac101m.redmon.persistence.v2.PersistentPageV2
import com.ac101m.redmon.persistence.v2.PersistentStorageV2
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
class StorageReader(val mapper: ObjectMapper) {

    /**
     * Get the persistence object from a stream of JSON text.
     *
     * @param inputStream The input stream to read from.
     */
    fun readStorageFromJsonStream(inputStream: InputStream): PersistentStorageV2 {
        val json = mapper.readTree(inputStream)
        val storageVersionInfo = StorageVersionInfo.fromJsonNode(json)

        if (storageVersionInfo.version < MIN_SUPPORTED_VERSION) {
            throw UnsupportedProfileVersionException(
                "Redmon was unable to read profile data. The profile was written by an older version of the " +
                        "mod (${storageVersionInfo.modVersion}) and is no longer supported by this version."
            )
        }

        if (storageVersionInfo.version > MAX_SUPPORTED_VERSION) {
            throw UnsupportedProfileVersionException(
                "Redmon was unable to read profile data. The profile was written by a more recent version of " +
                        "the mod (${storageVersionInfo.modVersion}) and is not supported by this version."
            )
        }

        val storageSubtype = getStorageSubtype(storageVersionInfo.version)
        val storage = readPersistentStorage(json, storageSubtype)

        return upgradeToLatest(storage)
    }

    private fun readPersistentStorage(json: JsonNode, storageType: Class<out PersistentStorage>): PersistentStorage {
        val storage = try {
            mapper.treeToValue(json, storageType)
        } catch (e: Exception) {
            throw RedmonException("Failed to load profile information, error parsing json.", e)
        }
        return storage
    }

    companion object {
        const val MIN_SUPPORTED_VERSION = 1
        const val MAX_SUPPORTED_VERSION = 3

        fun getStorageSubtype(version: Int) = when (version) {
            /**
             * Initial version (1.0.0-pre4 and earlier)
             * Technically some incompatibility between other pre-releases but whatever.
             * Frozen. Do not change.
             */
            1 -> PersistentStorageV1::class.java

            /**
             * Support for torches (new signal type)
             * Profile pagination and columns.
             * Frozen. Do not change.
             */
            2 -> PersistentStorageV2::class.java

            /**
             * Support for lamps (new signal type)
             * Support for IEEE_754 FP16 and FP32 signal formats.
             * Persistence info for re-activation of profiles.
             * Instruction set and disassembler features.
             * Frozen. Do not change.
             */
            3 -> PersistentStorageV2::class.java

            else -> error("Storage $version does not correspond to a known subtype")
        }

        /**
         * Converts a persistent profile list in any format to the current format.
         *
         * @param inputStorage Input persistent profile list in any format.
         */
        fun upgradeToLatest(inputStorage: PersistentStorage): PersistentStorageV2 {
            var storage = inputStorage
            while (storage.version < MAX_SUPPORTED_VERSION) {
                storage = when (storage.version) {
                    1 -> upgradeV1toV2(storage as PersistentStorageV1)
                    2 -> upgradeV2toV3(storage as PersistentStorageV2)
                    else -> error("Unrecognized profile list version ${storage.version}")
                }
            }
            return storage as PersistentStorageV2
        }

        /**
         * Major structural changes.
         * V2 format contains pages and columns, features not present in v1.
         * Each old profile is converted into a new one with a single page and single column.
         */
        fun upgradeV1toV2(inputStorage: PersistentStorageV1): PersistentStorageV2 {
            check(inputStorage.version == 1) {
                "Expected profile list version to be 1, but got ${inputStorage.version}"
            }
            return PersistentStorageV2(
                version = 2,
                modVersion = null,
                profiles = inputStorage.profiles.map { oldProfile -> PersistentProfileV2(
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

        /**
         * Mostly additive upgrade. New fields and object hierarchies are optional.
         * Added support for lamps and floating point.
         * Assembler and instruction set systems.
         */
        fun upgradeV2toV3(inputStorage: PersistentStorageV2): PersistentStorageV2 {
            check(inputStorage.version == 2) {
                "Expected profile list version to be 2, but got ${inputStorage.version}"
            }
            return inputStorage.copy(version = 3)
        }
    }
}
