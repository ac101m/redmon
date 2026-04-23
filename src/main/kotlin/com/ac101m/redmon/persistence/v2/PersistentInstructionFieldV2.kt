package com.ac101m.redmon.persistence.v2

import com.ac101m.redmon.isa.instruction.FieldType
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class PersistentInstructionFieldV2(
    @param:JsonProperty("type", required = true)
    val type: FieldType,
    @param:JsonProperty("size", required = true)
    val size: Int,
    @param:JsonProperty("offset", required = true)
    val offset: Int,
    @param:JsonProperty("metadata", required = false)
    val metadata: String? = null,
    @param:JsonProperty("description", required = false)
    val description: String? = null
)
