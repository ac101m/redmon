package com.ac101m.redmon

import com.ac101m.redmon.persistence.PersistentProfileList
import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.profile.ProfileList
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
    private val commandParser = Docopt(COMMAND_GRAMMAR)
        .withExit(false)
        .withVersion(REDMON_VERSION)

    private lateinit var profileList: ProfileList
    private var activeProfile: Profile? = null


    private fun loadProfileList() {
        profileList = if (PROFILE_SAVE_PATH.exists()) {
            val data = PersistentProfileList.load(PROFILE_SAVE_PATH)
            ProfileList.fromPersistent(data)
        } else {
            ProfileList().also {
                it.toPersistent().save(PROFILE_SAVE_PATH)
            }
        }
    }


    private fun saveProfileData() {
        profileList.toPersistent().save(PROFILE_SAVE_PATH)
    }


    private fun processProfileListCommand(): String {
        if (profileList.profiles.size == 0) {
            return "No profiles available"
        }

        val list = profileList.profiles.keys.joinToString(
            separator = "\n"
        ) { key ->
            "- $key (${profileList.profiles[key]!!.registers.size} registers)"
        }

        return "Available profiles:\n$list"
    }


    private fun processProfileCreateCommand(context: CommandContext<FabricClientCommandSource>, args: Map<String, Any>): String {
        val profileName = args.getStringCommandArgument("<name>")

        require(profileList.getProfile(profileName) == null) {
            "A profile with name '$profileName' already exists."
        }

        profileList.addProfile(Profile(profileName))
        activeProfile = profileList.getProfile(profileName)

        saveProfileData()

        return "Created new profile '$profileName', and set as active profile"
    }


    private fun processProfileDeleteCommand(args: Map<String, Any>): String {
        val profileName = args.getStringCommandArgument("<name>")

        requireNotNull(profileList.getProfile(profileName)) {
            "No profile with name '$profileName'"
        }

        profileList.removeProfile(profileName)
        saveProfileData()

        return "Removed profile '$profileName'"
    }


    private fun processProfileSelectCommand(context: CommandContext<FabricClientCommandSource>, args: Map<String, Any>): String {
        val profileName = args.getStringCommandArgument("<name>")

        requireNotNull(profileList.getProfile(profileName)) {
            "No profile with name '$profileName'"
        }

        activeProfile = profileList.getProfile(profileName)

        return "Set profile '$profileName' as active profile"
    }


    private fun processProfileDeselectCommand(): String {
        if (activeProfile == null) {
            return "No active profile"
        }

        val profileName = activeProfile!!.name
        activeProfile = null

        return "Deselected profile '$profileName'"
    }


    private fun processProfileCommand(context: CommandContext<FabricClientCommandSource>, args: Map<String, Any>): String {
        return if (args["list"] == true) {
            processProfileListCommand()
        } else if (args["create"] == true) {
            processProfileCreateCommand(context, args)
        } else if (args["delete"] == true) {
            processProfileDeleteCommand(args)
        } else if (args["select"] == true) {
            processProfileSelectCommand(context, args)
        } else if (args["deselect"] == true) {
            processProfileDeselectCommand()
        } else {
            throw RedmonCommandException(UNHANDLED_COMMAND_ERROR_MESSAGE)
        }
    }


    private fun processRegisterAddCommand(args: Map<String, Any>): String {
        val profile = checkNotNull(activeProfile) {
            "You must select a profile before adding a register"
        }

        val initialBitCount = args.getIntCommandArgument("--bits")

        TODO("Not yet implemented $initialBitCount")
    }


    private fun processRegisterDeleteCommand(args: Map<String, Any>): String {
        TODO("Not yet implemented")
    }


    private fun processRegisterCommand(context: CommandContext<FabricClientCommandSource>, args: Map<String, Any>): String {
        return if (args["create"] == true) {
            processRegisterAddCommand(args)
        } else if (args["delete"] == true) {
            processRegisterDeleteCommand(args)
        } else {
            throw RedmonCommandException(UNHANDLED_COMMAND_ERROR_MESSAGE)
        }
    }


    private fun processCommand(context: CommandContext<FabricClientCommandSource>, command: String) {
        val commandTokens = command.posixLexicalSplit()

        val args = try {
            commandParser.parse(commandTokens)
        } catch (e: DocoptExitException) {
            when (e.message) {
                null -> context.sendError("Error: Invalid arguments. $HELP_COMMAND_PROMPT")
                else -> context.sendFeedback(e.message!!)
            }
            return
        }

        try {
            if (args["profile"] == true) {
                context.sendFeedback(processProfileCommand(context, args))
            } else if (args["register"] == true) {
                context.sendFeedback(processRegisterCommand(context, args))
            } else {
                throw RedmonCommandException(UNHANDLED_COMMAND_ERROR_MESSAGE)
            }
        } catch(e: Throwable) {
            context.sendError("Error: ${e.message}")
        }
    }


    private fun getOutputText(): List<String> {
        if (activeProfile == null) {
            return listOf("No active profile")
        }

        val lines = ArrayList<String>()

        lines.add(activeProfile!!.name)

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
        loadProfileList()

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
