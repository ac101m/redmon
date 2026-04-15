package com.ac101m.redmon.persistence.v1

import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentRegisterBitV1(
    @param:JsonProperty("x", required = true)
    val x: Int,
    @param:JsonProperty("y", required = true)
    val y: Int,
    @param:JsonProperty("z", required = true)
    val z: Int
)
