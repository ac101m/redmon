package com.ac101m.redmon.persistence.v1

import com.fasterxml.jackson.annotation.JsonProperty

data class RegisterV1(
    @JsonProperty("name", required = true)
    val name: String,
    @JsonProperty("bits", required = true)
    val bits: List<WatchPointV1> = listOf()
)
