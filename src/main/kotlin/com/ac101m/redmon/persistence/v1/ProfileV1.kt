package com.ac101m.redmon.persistence.v1

import com.fasterxml.jackson.annotation.JsonProperty

data class ProfileV1(
    @JsonProperty("name", required = true)
    var name: String,
    @JsonProperty("registers", required = true)
    var registers: List<RegisterV1> = listOf()
)
