package com.ac101m.redmon.persistence.v2

import com.ac101m.redmon.persistence.PersistentProfileList
import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentProfileListV2(
    @param:JsonProperty("version", required = true)
    override val version: Int,
    @param:JsonProperty("mod_version", required = false)
    override val modVersion: String?,
    @param:JsonProperty("profiles", required = true)
    val profiles: List<PersistentProfileV2>
) : PersistentProfileList
