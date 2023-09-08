package com.ac101m.redmon.profile

import com.ac101m.redmon.profile.v1.SaveDataV1
import com.ac101m.redmon.utils.RedmonConfigurationException
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile


/**
 * Profiles each belong to a context.
 * Each world-space the player may join has its own context which contains the users profiles for that world.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "version",
    defaultImpl = SaveData::class
)
@JsonSubTypes(
    JsonSubTypes.Type(name = "1", value = SaveDataV1::class)
)
open class SaveData {
    companion object {
        private val objectMapper = ObjectMapper()

        fun load(path: Path): SaveDataV1 {
            if (!path.exists()) {
                throw RedmonConfigurationException("No such file '$path'.")
            }
            if (!path.isRegularFile()) {
                throw RedmonConfigurationException("File '$path' is not a file.")
            }
            return load(path.inputStream())
        }

        fun load(inputStream: InputStream): SaveDataV1 {
            val registry = try {
                objectMapper.readValue(inputStream, SaveDataV1::class.java)
            } catch (e: Exception) {
                throw RedmonConfigurationException("Failed to load profile information, error parsing configuration.", e)
            }

            return when (registry) {
                is SaveDataV1 -> registry
                else -> throw RedmonConfigurationException("Failed to load profile information, unrecognised registry type.")
            }
        }
    }
}
