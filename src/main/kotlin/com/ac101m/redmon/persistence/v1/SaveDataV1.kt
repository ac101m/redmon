package com.ac101m.redmon.persistence.v1

import com.ac101m.redmon.persistence.SaveData
import com.ac101m.redmon.utils.mapper
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.OutputStream
import java.nio.file.Path
import kotlin.io.path.outputStream

data class SaveDataV1(
    @JsonProperty("profiles", required = true)
    val profiles: HashMap<String, ProfileV1> = HashMap()
) : SaveData() {

    fun addProfile(profile: ProfileV1) {
        profiles[profile.name] = profile
    }

    fun getProfile(name: String): ProfileV1? {
        return profiles[name]
    }

    fun save(path: Path) {
        path.outputStream().use { outputStream ->
            save(outputStream)
        }
    }

    fun save(outputStream: OutputStream) {
        mapper.writeValue(outputStream, this)
    }
}
