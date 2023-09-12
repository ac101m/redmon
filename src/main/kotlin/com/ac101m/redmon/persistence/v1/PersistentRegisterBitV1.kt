package com.ac101m.redmon.persistence.v1

import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentRegisterBitV1(
    @JsonProperty("x", required = true)
    val x: Int,
    @JsonProperty("y", required = true)
    val y: Int,
    @JsonProperty("z", required = true)
    val z: Int
)
