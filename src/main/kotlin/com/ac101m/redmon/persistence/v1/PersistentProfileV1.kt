package com.ac101m.redmon.persistence.v1

import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentProfileV1(
    @param:JsonProperty("name", required = true)
    var name: String,
    @param:JsonProperty("registers", required = true)
    var registers: List<PersistentRegisterV1>
)
