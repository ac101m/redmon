package com.ac101m.redmon.utils

import com.ac101m.redmon.profile.SignalFormat
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

        val REDMON_VERSION = buildProperties["mod_version"]!!.toString().split("+").first()

        // Save file related constants
        private const val PROFILE_SAVE_FILE = "redmon_profiles.json"
        val PROFILE_STORAGE_PATH: Path = FabricLoader.getInstance().configDir.resolve(PROFILE_SAVE_FILE)

        // UI Constants
        val OVERLAY_POSITION = Vec3i(2, 2, 0)
        const val PAGINATED_LIST_ENTRIES_PER_PAGE = 10

        // Misc constants
        val DEFAULT_SIGNAL_FORMAT = SignalFormat.HEX
    }
}
