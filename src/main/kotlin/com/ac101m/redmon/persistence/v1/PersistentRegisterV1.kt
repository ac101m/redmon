package com.ac101m.redmon.persistence.v1

import com.ac101m.redmon.profile.RegisterFormat
import com.ac101m.redmon.profile.RegisterType
import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentRegisterV1(
    @JsonProperty("name", required = true)
    val name: String,
    @JsonProperty("type", required = true)
    val type: RegisterType,
    @JsonProperty("invert", required = false)
    val invert: Boolean = false,
    @JsonProperty("format", required = false)
    val format: RegisterFormat = RegisterFormat.UNSIGNED,
    @JsonProperty("bitLocations", required = true)
    val bitLocations: List<PersistentRegisterBitV1>
)
