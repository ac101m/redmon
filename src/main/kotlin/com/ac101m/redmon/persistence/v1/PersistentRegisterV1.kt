package com.ac101m.redmon.persistence.v1

import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentRegisterV1(
    @JsonProperty("name", required = true)
    val name: String,
    @JsonProperty("type", required = true)
    val type: String,
    @JsonProperty("invert", required = false)
    val invert: Boolean = false,
    @JsonProperty("bitLocations", required = true)
    val bitLocations: List<PersistentRegisterBitV1>
)
