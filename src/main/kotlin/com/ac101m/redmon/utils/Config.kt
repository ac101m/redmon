package com.ac101m.redmon.utils

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.core.Vec3i
import java.nio.file.Path
import java.util.*

class Config {
    companion object {

        // Build properties
        private val buildProperties = Properties().apply {
            load(Companion::class.java.classLoader.getResource("build.properties")!!.openStream())
        }

        val REDMON_VERSION = buildProperties["mod_version"]!!.toString()

        // Save files related constants
        private const val PROFILE_SAVE_FILE = "redmon_profiles.json"
        val PROFILE_STORAGE_PATH: Path = FabricLoader.getInstance().configDir.resolve(PROFILE_SAVE_FILE)

        // UI Constants
        val OVERLAY_POSITION = Vec3i(1, 1, 0)
    }
}
