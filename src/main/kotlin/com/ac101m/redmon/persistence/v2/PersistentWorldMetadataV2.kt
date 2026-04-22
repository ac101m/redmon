package com.ac101m.redmon.persistence.v2

import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentWorldMetadataV2(
    @param:JsonProperty("world_key", required = true)
    val worldKey: String,
    @param:JsonProperty("active_profile", required = false)
    val activeProfile: PersistentActiveProfileInfoV2? = null
)
