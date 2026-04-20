package com.ac101m.redmon.persistence

import com.fasterxml.jackson.databind.JsonNode

/**
 * This class is used for extracting version information from a stored or imported profile list.
 * Ignores all properties but the version number and mod version.
 */
data class ProfileListVersionInfo(
    override val version: Int,
    override val modVersion: String? = null
) : PersistentProfileList {
    companion object {
        /**
         * Extract profile list version information from a JSON tree.
         * Done this way so that we don't have to map the whole structure to an object.
         *  - Avoids the need for multiple passes when reading a stored profile.
         *  - Allows us to check the version of stored/serialized profile lists written by later versions of the mod.
         */
        fun fromJsonNode(json: JsonNode): ProfileListVersionInfo {
            check(json.hasNonNull("version")) {
                "Could not get storage version info, version field is missing or null."
            }

            val version = json.get("version").let {
                check(it.isTextual || it.isInt) {
                    "Could not get storage version info, version field is not text."
                }
                try {
                    it.asText().toInt()
                } catch (_: NumberFormatException) {
                    throw IllegalStateException(
                        "Could not get storage version info, version field could not be converted to a number."
                    )
                }
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

            return ProfileListVersionInfo(version, modVersion)
        }
    }
}
