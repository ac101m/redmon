package com.ac101m.redmon.utils

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.math.Vec3i
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
        val PROFILE_SAVE_PATH: Path = FabricLoader.getInstance().configDir.resolve(PROFILE_SAVE_FILE)

        // Command interface info
        val COMMAND_GRAMMAR = Companion::class.java.classLoader.getResource("command-grammar.txt")!!.readText()

        // Error messages
        const val ISSUE_CREATE_PROMPT =
            "This is a bug! Please report the issue here: https://github.com/ac101m/redmon/issues"
        const val HELP_COMMAND_PROMPT =
            "See '/redmon -h' for usage information."
        const val UNHANDLED_COMMAND_ERROR_MESSAGE =
            "Unhandled command. $ISSUE_CREATE_PROMPT"

        // UI Constants
        val OVERLAY_POSITION = Vec3i(1, 1, 0)
    }
}
