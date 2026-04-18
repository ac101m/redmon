package com.ac101m.redmon.persistence.v2

import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentProfileV2(
    @param:JsonProperty("name", required = true)
    val name: String,
    @param:JsonProperty("registers", required = true)
    val signals: List<PersistentSignalV2>
)
