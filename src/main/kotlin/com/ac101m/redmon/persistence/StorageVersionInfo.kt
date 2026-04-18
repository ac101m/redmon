package com.ac101m.redmon.persistence

import com.fasterxml.jackson.databind.JsonNode

/**
 * This class is used for extracting the version from a stored profile list.
 * Ignores all properties but the version number and mod version.
 */
data class StorageVersionInfo(
    override val version: Int,
    override val modVersion: String? = null
) : PersistentProfileList {
    companion object {
        /**
         * Extract from a JSON tree.
         * Done this way so that we can determine the storage version without mapping the whole structure to a
         * persistent object. Allows us to check the version of stored/serialized profile lists greater than what we
         * even when we don't know the format (e.g. future versions).
         */
        fun fromJsonNode(json: JsonNode): StorageVersionInfo {
            check(json.hasNonNull("version")) {
                "Could not get storage version info, version field is missing or null."
            }

            val version = json.get("version").let {
                check(it.isTextual) {
                    "Could not get storage version info, version field is not text."
                }
                try {
                    it.asText().toInt()
                } catch (_: NumberFormatException) {
                    throw IllegalStateException(
                        "Could not get storage version info, version field could not be converted to a number."
                    )
                }
                it.asInt()
            }

            val modVersion = if (json.hasNonNull("mod_version")) {
                json.get("mod_version").let {
                    check(it.isTextual) {
                        "Could not get storage version info, mod version field is not text."
                    }
                    it.asText()
                }
            } else {
                null
            }

            return StorageVersionInfo(version, modVersion)
        }
    }
}
