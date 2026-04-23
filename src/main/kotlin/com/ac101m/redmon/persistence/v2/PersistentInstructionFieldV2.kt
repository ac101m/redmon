package com.ac101m.redmon.persistence.v2

import com.ac101m.redmon.isa.instruction.FieldType
import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentInstructionFieldV2(
    @param:JsonProperty("type", required = true)
    val type: FieldType,
    @param:JsonProperty("size", required = true)
    val size: Int,
    @param:JsonProperty("offset", required = true)
    val offset: Int,
    @param:JsonProperty("metadata", required = false)
    val metadata: String? = null
)
