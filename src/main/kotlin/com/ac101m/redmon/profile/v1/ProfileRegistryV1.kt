package com.ac101m.redmon.profile.v1

import com.ac101m.redmon.profile.ProfileRegistry
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.outputStream


data class ProfileRegistryV1(
    @JsonProperty("contextMap", required = true)
    val contextMap: HashMap<String, ProfileContextV1> = HashMap()
) : ProfileRegistry() {
    private val objectMapper = ObjectMapper()

    fun addContext(worldId: String, context: ProfileContextV1) {
        contextMap[worldId] = context
    }

    fun save(path: Path) {
        save(path.outputStream())
    }

    fun save(outputStream: OutputStream) {
        objectMapper.writeValue(outputStream, ProfileRegistryV1::class.java)
    }
}
