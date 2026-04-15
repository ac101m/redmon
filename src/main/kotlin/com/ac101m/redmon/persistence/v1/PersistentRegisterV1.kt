package com.ac101m.redmon.persistence.v1

import com.ac101m.redmon.profile.RegisterFormat
import com.ac101m.redmon.profile.RegisterType
import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentRegisterV1(
    @param:JsonProperty("name", required = true)
    val name: String,
    @param:JsonProperty("type", required = true)
    val type: RegisterType,
    @param:JsonProperty("invert", required = false)
    val invert: Boolean = false,
    @param:JsonProperty("format", required = false)
    val format: RegisterFormat = RegisterFormat.UNSIGNED,
    @param:JsonProperty("bitLocations", required = true)
    val bitLocations: List<PersistentRegisterBitV1>
)
