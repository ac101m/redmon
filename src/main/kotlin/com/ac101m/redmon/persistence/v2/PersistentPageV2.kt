package com.ac101m.redmon.persistence.v2

import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentPageV2(
    @param:JsonProperty("name", required = true)
    val name: String,
    @param:JsonProperty("columns", required = true)
    val columns: List<PersistentColumnV2>
)
