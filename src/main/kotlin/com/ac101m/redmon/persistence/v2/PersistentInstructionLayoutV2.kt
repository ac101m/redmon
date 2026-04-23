package com.ac101m.redmon.persistence.v2

import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentInstructionLayoutV2(
    @param:JsonProperty("name", required = true)
    val name: String,
    @param:JsonProperty("description", required = false)
    val description: String? = null,
    @param:JsonProperty("fields", required = true)
    val fields: List<PersistentInstructionFieldV2>
)
