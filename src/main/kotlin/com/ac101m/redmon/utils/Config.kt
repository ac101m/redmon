package com.ac101m.redmon.utils

import net.fabricmc.loader.api.FabricLoader
import java.nio.file.Path


class Config {
    companion object {

        // Version
        val REDMON_VERSION = "1.0.0-SNAPSHOT"

        // Save files related constants
        private const val PROFILE_SAVE_FILE = "redmon_profiles.json"
        val PROFILE_SAVE_PATH: Path = FabricLoader.getInstance().configDir.resolve(PROFILE_SAVE_FILE)

        // Command interface info
        val COMMAND_GRAMMAR = Companion::class.java.classLoader.getResource("command-grammar.txt")!!.readText()
    }
}
