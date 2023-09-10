package com.ac101m.redmon

import com.ac101m.redmon.persistence.SaveData
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
import java.nio.file.Path
import kotlin.io.path.exists


class RedmonClient : ClientModInitializer {
    private lateinit var saveData: SaveDataV1

    private fun loadSaveData(path: Path) {
        if (path.exists()) {
            saveData = SaveData.load(path)
        } else {
            saveData = SaveDataV1()
            saveData.save(path)
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


    private fun processCommand(context: CommandContext<FabricClientCommandSource>, command: String) {
        val commandTokens = command.posixLexicalSplit()

        val parser = Docopt(COMMAND_GRAMMAR)
            .withExit(false)
            .withVersion(REDMON_VERSION)

        val args = try {
            parser.parse(commandTokens)
        } catch (e: DocoptExitException) {
            when (e.message) {
                null -> context.sendError("Invalid arguments, see '/redmon --help' for usage information.")
                else -> context.sendFeedback(e.message!!)
            }
            return
        }

        if (args["hello"] == true) {
            context.sendFeedback("hello ${context.source.player.name.string}!")
        } else {
            context.sendError("Unrecognised command.")
        }
    }


    override fun onInitializeClient() {
        loadSaveData(PROFILE_SAVE_PATH)

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
                context.source.sendError(LiteralText("Missing arguments, see '/redmon --help' for usage info."))
                0
            }
        )
    }
}
