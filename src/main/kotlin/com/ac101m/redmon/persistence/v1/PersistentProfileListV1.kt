package com.ac101m.redmon.persistence.v1

import com.ac101m.redmon.persistence.PersistentProfileList
import com.ac101m.redmon.utils.mapper
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.outputStream


data class PersistentProfileListV1(
    @JsonProperty("profiles", required = true)
    val profiles: List<PersistentProfileV1>
) : PersistentProfileList() {
    fun save(outputStream: OutputStream) {
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, this)
    }

    fun save(path: Path) {
        path.outputStream().use { outputStream ->
            save(outputStream)
        }
    }
}
