package com.ac101m.redmon.persistence.v2

import com.ac101m.redmon.persistence.PersistentStorage
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class PersistentStorageV2(
    @param:JsonProperty("version", required = true)
    override val version: Int,
    @param:JsonProperty("mod_version", required = false)
    override val modVersion: String?,
    @param:JsonProperty("profiles", required = false)
    val profiles: List<PersistentProfileV2> = emptyList(),
    @param:JsonProperty("world_data", required = false)
    val worldData: List<PersistentWorldMetadataV2> = emptyList(),
    @param:JsonProperty("instruction_sets", required = false)
    val instructionSets: List<PersistentInstructionSetV2> = emptyList()
) : PersistentStorage
