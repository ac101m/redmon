package com.ac101m.redmon.persistence.v2

import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentSignalBlockV2(
    @param:JsonProperty("x", required = true)
    val x: Int,
    @param:JsonProperty("y", required = true)
    val y: Int,
    @param:JsonProperty("z", required = true)
    val z: Int
)
