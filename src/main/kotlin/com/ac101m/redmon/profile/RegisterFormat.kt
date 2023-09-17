package com.ac101m.redmon.profile

import com.fasterxml.jackson.annotation.JsonProperty


enum class RegisterFormat {
    @JsonProperty("UNSIGNED")
    UNSIGNED,
    @JsonProperty("SIGNED")
    SIGNED,
    @JsonProperty("HEX")
    HEX,
    @JsonProperty("BINARY")
    BINARY
}
