package com.ac101m.redmon.persistence

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * This class is used for extracting the version from a stored profile list.
 * Ignores all properties but the version number and mod version.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class StorageVersionInfo(
    @param:JsonProperty("version", required = true)
    override val version: Int,
    @param:JsonProperty("mod_version", required = false)
    override val modVersion: String? = null
) : PersistentProfileList
