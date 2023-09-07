package com.ac101m.redmon.utils

import net.fabricmc.loader.api.FabricLoader


class Config {
    companion object {

        // Save files related constants
        val PROFILE_REGISTRY_SAVE_FILE = "redmon_profiles.json"
        val PROFILE_REGISTRY_SAVE_PATH = FabricLoader.getInstance().configDir.resolve(PROFILE_REGISTRY_SAVE_FILE)
    }
}
