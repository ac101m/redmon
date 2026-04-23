package com.ac101m.redmon.persistence.v2

import com.fasterxml.jackson.annotation.JsonProperty

data class PersistentInstructionSetV2(
    @param:JsonProperty("name", required = true)
    val name: String,
    @param:JsonProperty("instruction_size", required = true)
    val instructionSize: Int,
    @param:JsonProperty("register_aliases", required = false)
    val registerAliases: Map<Int, Set<String>> = emptyMap(),
    @param:JsonProperty("instructions", required = true)
    val instructions: List<PersistentInstructionLayoutV2>
)
