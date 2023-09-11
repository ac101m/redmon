package com.ac101m.redmon

import com.ac101m.redmon.persistence.ProfileData
import com.ac101m.redmon.persistence.v1.ProfileV1
import com.ac101m.redmon.persistence.v1.ProfileDataV1
import com.ac101m.redmon.utils.*
import com.ac101m.redmon.utils.Config.Companion.COMMAND_GRAMMAR
import com.ac101m.redmon.utils.Config.Companion.PROFILE_SAVE_PATH
import com.ac101m.redmon.utils.Config.Companion.REDMON_VERSION
import com.ac101m.redmon.utils.Config.Companion.HELP_COMMAND_PROMPT
import com.ac101m.redmon.utils.Config.Companion.UNHANDLED_COMMAND_ERROR_MESSAGE
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import org.docopt.Docopt
import org.docopt.DocoptExitException
import kotlin.io.path.exists


class RedmonClient : ClientModInitializer {
    private lateinit var profileData: ProfileDataV1
    private var currentProfile: ProfileV1? = null


    private fun loadProfileData() {
        if (PROFILE_SAVE_PATH.exists()) {
            profileData = ProfileData.load(PROFILE_SAVE_PATH)
        } else {
            profileData = ProfileDataV1()
            profileData.save(PROFILE_SAVE_PATH)
        }
    }


    private fun saveProfileData() {
        profileData.save(PROFILE_SAVE_PATH)
    }


    private fun processProfileListCommand(): String {
        if (profileData.profiles.size == 0) {
            return "No profiles available"
        }

        val list = profileData.profiles.keys.joinToString(
            separator = "\n"
        ) { key ->
            "- $key (${profileData.profiles[key]!!.registers.size} registers)"
        }

        return "Available profiles:\n$list"
    }


    private fun processProfileCreateCommand(args: Map<String, Any>): String {
        val profileName = args.getStringCommandArgument("<name>")

        require(profileData.getProfile(profileName) == null) {
            "A profile with name '$profileName' already exists."
        }

        profileData.addProfile(ProfileV1(profileName))
        currentProfile = profileData.getProfile(profileName)

        saveProfileData()

        return "Created new profile '$profileName', and set as active profile"
    }


    private fun processProfileDeleteCommand(args: Map<String, Any>): String {
        val profileName = args.getStringCommandArgument("<name>")

        requireNotNull(profileData.getProfile(profileName)) {
            "No profile with name '$profileName'"
        }

        profileData.removeProfile(profileName)
        saveProfileData()

        return "Removed profile '$profileName'"
    }


    private fun processProfileSelectCommand(args: Map<String, Any>): String {
        val profileName = args.getStringCommandArgument("<name>")

        requireNotNull(profileData.getProfile(profileName)) {
            "No profile with name '$profileName'"
        }

        currentProfile = profileData.getProfile(profileName)

        return "Set profile '$profileName' as active profile"
    }


    private fun processProfileDeselectCommand(): String {
        if (currentProfile == null) {
            return "No active profile"
        }

        val profileName = currentProfile!!.name
        currentProfile = null

        return "Deselected profile '$profileName'"
    }


    private fun processProfileCommand(args: Map<String, Any>): String {
        return if (args["list"] == true) {
            processProfileListCommand()
        } else if (args["create"] == true) {
            processProfileCreateCommand(args)
        } else if (args["delete"] == true) {
            processProfileDeleteCommand(args)
        } else if (args["select"] == true) {
            processProfileSelectCommand(args)
        } else if (args["deselect"] == true) {
            processProfileDeselectCommand()
        } else {
            throw RedmonCommandException(UNHANDLED_COMMAND_ERROR_MESSAGE)
        }
    }


    private fun processCommand(context: CommandContext<FabricClientCommandSource>, command: String) {
        val commandTokens = command.posixLexicalSplit()

        val parser = Docopt(COMMAND_GRAMMAR)
            .withExit(false)
            .withVersion(REDMON_VERSION)

        val args = try {
            parser.parse(commandTokens)
        } catch (e: DocoptExitException) {
            when (e.message) {
                null -> context.sendError("Error: Invalid arguments. $HELP_COMMAND_PROMPT")
                else -> context.sendFeedback(e.message!!)
            }
            return
        }

        try {
            if (args["profile"] == true) {
                context.sendFeedback(processProfileCommand(args))
            } else {
                throw RedmonCommandException(UNHANDLED_COMMAND_ERROR_MESSAGE)
            }
        } catch(e: Throwable) {
            context.sendError("Error: ${e.message}")
        }
    }


    private fun getOutputText(): List<String> {
        if (currentProfile == null) {
            return listOf("No active profile")
        }

        val lines = ArrayList<String>()

        lines.add(currentProfile!!.name)

        return lines
    }


    private fun drawOutput(matrixStack: MatrixStack) {
        val text = getOutputText()

        val width = text.maxOfOrNull { textWidth(it) }!!

        val x = 2
        val y = 2

        drawRectangle(Rectangle(x, y, 0, width + 3, (text.size * 10) + 2, 0x7f000000))

        text.forEachIndexed { i, line ->
            drawText(matrixStack, line, x + 1, (i * 10) + y + 1, 0xffffff)
        }
    }


    override fun onInitializeClient() {
        loadProfileData()

        HudRenderCallback.EVENT.register { matrixStack, _ ->
            drawOutput(matrixStack)
        }

        ClientCommandManager.DISPATCHER.register(
            literal("redmon").then(
                argument("args", greedyString()).executes { context ->
                    processCommand(context, getString(context, "args"))
                    0
                }
            ).executes { context ->
                context.source.sendError(LiteralText("Error: Missing arguments, $HELP_COMMAND_PROMPT"))
                0
            }
        )
    }
}
