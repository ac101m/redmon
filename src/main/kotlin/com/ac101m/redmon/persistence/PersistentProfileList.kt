package com.ac101m.redmon.persistence

import com.ac101m.redmon.persistence.v1.PersistentProfileListV1
import com.ac101m.redmon.utils.RedmonConfigurationException
import com.ac101m.redmon.utils.mapper
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "version",
    defaultImpl = PersistentProfileList::class
)
@JsonSubTypes(
    JsonSubTypes.Type(name = "1", value = PersistentProfileListV1::class)
)
open class PersistentProfileList {
    companion object {

        fun load(path: Path): PersistentProfileList {
            if (!path.exists()) {
                throw RedmonConfigurationException("No such file '$path'.")
            }
            if (!path.isRegularFile()) {
                throw RedmonConfigurationException("File '$path' is not a regular file.")
            }
            return load(path.inputStream())
        }

        fun load(inputStream: InputStream): PersistentProfileList {
            val registry = try {
                mapper.readValue(inputStream, PersistentProfileList::class.java)
            } catch (e: Exception) {
                throw RedmonConfigurationException("Failed to load profile information, error parsing configuration.", e)
            }

            return when (registry) {
                is PersistentProfileListV1 -> registry
                else -> throw RedmonConfigurationException("Failed to load profile information, unrecognised registry type.")
            }
        }
    }
}
