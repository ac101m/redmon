package com.ac101m.redmon.persistence.v2

import com.ac101m.redmon.profile.SignalType
import com.fasterxml.jackson.annotation.JsonProperty

class PersistentSignalV2(
    @param:JsonProperty("name", required = true)
    val name: String,
    @param:JsonProperty("type", required = true)
    val type: SignalType,
    @param:JsonProperty("invert", required = true)
    val invert: Boolean,
    @param:JsonProperty("format", required = true)
    val format: String,
    @param:JsonProperty("blockLocations", required = true)
    val blockLocations: List<PersistentSignalBlockV2>
)
