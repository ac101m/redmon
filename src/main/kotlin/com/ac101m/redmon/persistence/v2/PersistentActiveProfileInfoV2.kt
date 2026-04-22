package com.ac101m.redmon.persistence.v2

import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentActiveProfileInfoV2(
    @param:JsonProperty("name", required = true)
    val name: String,
    @param:JsonProperty("offset", required = true)
    val offset: PersistentBlockV2
)
