package com.ac101m.redmon.persistence.v1

import com.ac101m.redmon.profile.SignalFormat
import com.ac101m.redmon.profile.SignalType
import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentSignalV1(
    @param:JsonProperty("name", required = true)
    val name: String,
    @param:JsonProperty("type", required = true)
    val type: SignalType,
    @param:JsonProperty("invert", required = false)
    val invert: Boolean = false,
    @param:JsonProperty("format", required = false)
    val format: SignalFormat = SignalFormat.UNSIGNED,
    @param:JsonProperty("bitLocations", required = true)
    val blockLocations: List<PersistentSignalBitV1>
)
