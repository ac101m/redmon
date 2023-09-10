package com.ac101m.redmon

import com.ac101m.redmon.persistence.SaveData
import com.ac101m.redmon.persistence.v1.ProfileV1
import com.ac101m.redmon.persistence.v1.SaveDataV1
import com.ac101m.redmon.utils.*
import com.ac101m.redmon.utils.Config.Companion.COMMAND_GRAMMAR
import com.ac101m.redmon.utils.Config.Companion.PROFILE_SAVE_PATH
import com.ac101m.redmon.utils.Config.Companion.REDMON_VERSION
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
    private lateinit var saveData: SaveDataV1


    private fun loadProfileData() {
        if (PROFILE_SAVE_PATH.exists()) {
            saveData = SaveData.load(PROFILE_SAVE_PATH)
        } else {
            saveData = SaveDataV1()
            saveData.save(PROFILE_SAVE_PATH)
        }
    }


    private fun saveProfileData() {
        saveData.save(PROFILE_SAVE_PATH)
    }


    private fun processListProfilesCommand(context: CommandContext<FabricClientCommandSource>): String {
        val list = saveData.profiles.keys.joinToString(
            separator = "\n",
            prefix = " - "
        )
        return "List of profiles:\n$list"
    }


    private fun processCreateProfileCommand(context: CommandContext<FabricClientCommandSource>, args: Map<String, Any>): String {
        val name = requireNotNull(args["<name>"]) {
            "Internal error, <name> parameter is missing."
        }.toString()

        require(saveData.getProfile(name) == null) {
            "A profile with name '$name' already exists."
        }

        saveData.addProfile(ProfileV1(name))
        saveProfileData()

        return "Created new profile with name '$name', and set as active profile."
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
                null -> context.sendError("Error: Invalid arguments, see '/redmon --help' for usage information.")
                else -> context.sendFeedback(e.message!!)
            }
            return
        }

        try {
            if (args["list-profiles"] == true) {
                context.sendFeedback(processListProfilesCommand(context))
            } else if (args["create-profile"] == true) {
                context.sendFeedback(processCreateProfileCommand(context, args))
            } else {
                context.sendError("Unrecognised command.")
            }
        } catch(e: Exception) {
            context.sendError("Error: ${e.message}")
        }
    }


    private fun drawOutput(matrixStack: MatrixStack) {
        val text = listOf("Line 1", "Another line!", "the number of the lines is 3")

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
                context.source.sendError(LiteralText("Error: Missing arguments, see '/redmon --help' for usage info."))
                0
            }
        )
    }
}
