package com.ac101m.redmon.utils

import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path


class Config {
    companion object {

        // Save files related constants
        private const val PROFILE_SAVE_FILE = "redmon_profiles.json"
        val PROFILE_SAVE_PATH: Path = FabricLoader.getInstance().configDir.resolve(PROFILE_SAVE_FILE)
    }
}
