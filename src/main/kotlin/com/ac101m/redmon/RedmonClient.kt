package com.ac101m.redmon

import com.ac101m.redmon.gui.ProfileOverlay
import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.profile.Register
import com.ac101m.redmon.profile.RegisterFormat
import com.ac101m.redmon.profile.RegisterType
import com.ac101m.redmon.utils.*
import com.ac101m.redmon.utils.Config.Companion.COMMAND_GRAMMAR
import com.ac101m.redmon.utils.Config.Companion.PROFILE_SAVE_PATH
import com.ac101m.redmon.utils.Config.Companion.REDMON_VERSION
import com.ac101m.redmon.utils.Config.Companion.HELP_COMMAND_PROMPT
import com.ac101m.redmon.utils.Config.Companion.OVERLAY_POSITION
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
import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.LiteralText
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import net.minecraft.world.World
import org.docopt.Docopt
import org.docopt.DocoptExitException
import kotlin.math.abs


class RedmonClient : ClientModInitializer {
    private val commandParser = Docopt(COMMAND_GRAMMAR)
        .withExit(false)
        .withVersion(REDMON_VERSION)

    private val redmon = RedmonState(PROFILE_SAVE_PATH)


    private fun processProfileListCommand(): String {
        if (redmon.profiles.size == 0) {
            return "No profiles available"
        }

        val list = redmon.profiles.names.joinToString(
            separator = "\n"
        ) { name ->
            "- $name (${redmon.profiles.getProfile(name).registers.size} registers)"
        }

        return "Available profiles:\n$list"
    }


    private fun processProfileCreateCommand(player: ClientPlayerEntity, args: Map<String, Any>): String {
        val profileName = args.getStringCommandArgument("<name>")

        redmon.profiles.addProfile(Profile(profileName))

        redmon.setActiveProfile(player, profileName)
        redmon.saveProfiles()

        return "Created new profile '$profileName', and set as active profile"
    }


    private fun processProfileDeleteCommand(args: Map<String, Any>): String {
        val profileName = args.getStringCommandArgument("<name>")
        redmon.profiles.requireProfileExists(profileName)

        if (redmon.activeProfile != null) {
            if (redmon.activeProfile!!.name == profileName) {
                redmon.clearActiveProfile()
            }
        }

        redmon.profiles.removeProfile(profileName)
        redmon.saveProfiles()

        return "Removed profile '$profileName'"
    }


    private fun processProfileSelectCommand(player: ClientPlayerEntity, args: Map<String, Any>): String {
        val profileName = args.getStringCommandArgument("<name>")
        redmon.setActiveProfile(player, profileName)
        return "Set profile '$profileName' as active profile"
    }


    private fun processProfileDeselectCommand(): String {
        return if (redmon.activeProfile == null) {
            "No active profile"
        } else {
            val profileName = redmon.activeProfile!!.name
            redmon.clearActiveProfile()
            "Deactivated profile '$profileName'"
        }
    }


    private fun processProfileRenameCommand(args: Map<String, Any>): String {
        val profileName = args.getStringCommandArgument("<name>")
        val newProfileName = args.getStringCommandArgument("<new-name>")

        val profile = redmon.profiles.getProfile(profileName)

        profile.name = newProfileName
        redmon.saveProfiles()

        return "Renamed profile '$profileName' to '$newProfileName'"
    }


    private fun processProfileCommand(context: CommandContext<FabricClientCommandSource>, args: Map<String, Any>): String {
        return if (args["list"] == true) {
            processProfileListCommand()
        } else if (args["create"] == true) {
            processProfileCreateCommand(context.source.player, args)
        } else if (args["delete"] == true) {
            processProfileDeleteCommand(args)
        } else if (args["select"] == true) {
            processProfileSelectCommand(context.source.player, args)
        } else if (args["deselect"] == true) {
            processProfileDeselectCommand()
        } else if (args["rename"] == true) {
            processProfileRenameCommand(args)
        } else {
            throw RedmonCommandException(UNHANDLED_COMMAND_ERROR_MESSAGE)
        }
    }


    private fun getRegisterBitsFromLookDirection(
        player: ClientPlayerEntity,
        requestedBits: Int,
        type: RegisterType,
        lsbFirst: Boolean
    ): List<Vec3i> {
        val look = Vec3d.fromPolar(player.pitch, player.yaw)
        val eyePos = player.eyePos

        val stepScaleFactor = 1 / maxOf(abs(look.x), abs(look.y), abs(look.z))
        val step = look.multiply(stepScaleFactor)

        var currentPos = eyePos
        var bitsFound = 0

        val bitPositions = ArrayList<Vec3i>()

        while ((bitsFound < requestedBits) and (currentPos.isInRange(eyePos, 256.0))) {
            val blockPos = BlockPos(currentPos)
            val blockState = player.world.getBlockState(blockPos)

            if (blockState.block == Blocks.REPEATER) {
                bitsFound++
                bitPositions.add(blockPos)
            }

            currentPos = currentPos.add(step)
        }

        check(bitsFound == requestedBits) {
            "Failed to find register bits, requested $requestedBits but found $bitsFound"
        }

        return when (lsbFirst) {
            true -> bitPositions
            false -> List(bitPositions.size) { i -> bitPositions[(bitPositions.size - 1) - i] }
        }
    }


    private fun processRegisterCreateCommand(player: ClientPlayerEntity, args: Map<String, Any>): String {
        val profile = checkNotNull(redmon.activeProfile) {
            "You must select a profile before adding a register"
        }

        val registerName = args.getStringCommandArgument("<name>")
        val registerType = RegisterType.REPEATER
        val initialBitCount = args.getIntCommandArgument("--bits")
        val lsbFirst = args.getBooleanCommandArgument("--lsb")

        require(profile.getRegister(registerName) == null) {
            "Register with name '$registerName' already exists"
        }

        val bitLocations = getRegisterBitsFromLookDirection(player, initialBitCount, registerType, lsbFirst)

        val register = Register(
            registerName,
            registerType,
            false,
            RegisterFormat.UNSIGNED,
            bitLocations.map { it.subtract(redmon.profileOffset!!) }
        )

        profile.addRegister(register)
        redmon.saveProfiles()

        return "Added register '$registerName' with $initialBitCount bits"
    }


    private fun processRegisterDeleteCommand(args: Map<String, Any>): String {
        val profile = checkNotNull(redmon.activeProfile) {
            "You must select a profile before deleting a register"
        }

        val registerName = args.getStringCommandArgument("<name>")

        require(profile.registers.containsKey(registerName)) {
            "No register with name '$registerName' in profile '${profile.name}'"
        }

        profile.removeRegister(registerName)
        redmon.saveProfiles()

        return "Removed register '$registerName' from profile '${profile.name}'"
    }


    private fun processRegisterInvertCommand(args: Map<String, Any>): String {
        val profile = checkNotNull(redmon.activeProfile) {
            "You must select a profile before deleting a register"
        }

        val registerName = args.getStringCommandArgument("<name>")

        require(profile.registers.containsKey(registerName)) {
            "No register with name '$registerName' in profile '${profile.name}'"
        }

        val register = profile.getRegister(registerName)!!
        register.invert()

        redmon.saveProfiles()

        return when (register.invert) {
            true -> "Register '$registerName' now in inverting mode."
            false -> "Register '$registerName' now in non-inverting mode."
        }
    }


    private fun processRegisterFlipCommand(args: Map<String, Any>): String {
        val profile = checkNotNull(redmon.activeProfile) {
            "You must select a profile before flipping a register"
        }

        val registerName = args.getStringCommandArgument("<name>")

        require(profile.registers.containsKey(registerName)) {
            "No register with name '$registerName' in profile '${profile.name}'"
        }

        val register = profile.getRegister(registerName)!!
        register.flipBits()

        redmon.saveProfiles()

        return "Flipped register '${register.name}'"
    }


    private fun processRegisterAppendCommand(player: ClientPlayerEntity, args: Map<String, Any>): String {
        val profile = checkNotNull(redmon.activeProfile) {
            "You must select a profile before appending bits to a register"
        }

        val registerName = args.getStringCommandArgument("<name>")
        val registerType = RegisterType.REPEATER
        val bitCount = args.getIntCommandArgument("--bits")
        val lsbFirst = args.getBooleanCommandArgument("--lsb")

        val register = requireNotNull(profile.getRegister(registerName)) {
            "No register with name '$registerName' in profile '${profile.name}'"
        }

        val bitPositions = getRegisterBitsFromLookDirection(player, bitCount, registerType, lsbFirst)

        register.appendBits(bitPositions.map { position -> position.subtract(redmon.profileOffset!!) })
        redmon.saveProfiles()

        return "Appended $bitCount bits to register '${register.name}' in profile '${profile.name}'"
    }


    private fun processRegisterRenameCommand(args: Map<String, Any>): String {
        val profile = checkNotNull(redmon.activeProfile) {
            "You must select a profile before appending bits to a register"
        }

        val registerName = args.getStringCommandArgument("<name>")
        val newRegisterName = args.getStringCommandArgument("<new-name>")

        val register = requireNotNull(profile.getRegister(registerName)) {
            "No register with name '$registerName' in profile '${profile.name}'"
        }

        register.name = newRegisterName
        redmon.saveProfiles()

        return "Renamed register '$registerName' in profile ${profile.name} to '$newRegisterName'"
    }


    private fun processRegisterFormatCommand(args: Map<String, Any>): String {
        val profile = checkNotNull(redmon.activeProfile) {
            "You must select a profile before setting register format"
        }

        val registerName = args.getStringCommandArgument("<name>")
        val newFormatArg = args.getStringCommandArgument("<format>")

        val newFormat = try {
            RegisterFormat.valueOf(newFormatArg.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException(
                "'$newFormatArg' is not a valid format. " +
                "Valid formats are ${RegisterFormat.entries.joinToString(", ") { it.name.lowercase() }}", e)
        }

        return if (registerName == "all") {
            profile.registers.forEach { it.value.format = newFormat }
            "Set format of all registers in profile ${profile.name} to '$newFormat'"
        } else {
            val register = requireNotNull(profile.getRegister(registerName)) {
                "No register with name '$registerName' in profile '${profile.name}'"
            }
            register.format = newFormat
            "Set format of register '${register.name}' in profile '${profile.name}' to '$newFormat'"
        }.also {
            redmon.saveProfiles()
        }
    }


    private fun processRegisterCommand(context: CommandContext<FabricClientCommandSource>, args: Map<String, Any>): String {
        return if (args["create"] == true) {
            processRegisterCreateCommand(context.source.player, args)
        } else if (args["delete"] == true) {
            processRegisterDeleteCommand(args)
        } else if (args["invert"] == true) {
            processRegisterInvertCommand(args)
        } else if (args["flip"] == true) {
            processRegisterFlipCommand(args)
        } else if (args["append"] == true) {
            processRegisterAppendCommand(context.source.player, args)
        } else if (args["rename"] == true) {
            processRegisterRenameCommand(args)
        } else if (args["format"] == true) {
            processRegisterFormatCommand(args)
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


    private fun getOutputText(world: World): List<String> {
        val profile = if (redmon.activeProfile == null) {
            return listOf("No active profile")
        } else {
            redmon.activeProfile!!
        }

        val lines = ArrayList<String>()
        val offset = redmon.profileOffset!!

        lines.add("${profile.name}@(x=${offset.x} y=${offset.y} z=${offset.z})")

        profile.registers.values.forEach { register ->
            register.updateState(world, offset)
            lines.add("${register.name} | ${register.getState()}")
        }

        return lines
    }


    private fun drawOutput(matrixStack: MatrixStack) {
        val profile = if (redmon.activeProfile == null) {
            redmon.inactiveUI.draw(matrixStack, OVERLAY_POSITION)
            return
        } else {
            redmon.activeProfile!!
        }

        val world = MinecraftClient.getInstance().player?.world ?: return
        profile.updateState(world, redmon.profileOffset!!)

        redmon.profileUI.update(profile, redmon.profileOffset!!)
        redmon.profileUI.draw(matrixStack, OVERLAY_POSITION)
    }


    override fun onInitializeClient() {
        redmon.loadProfiles()

        HudRenderCallback.EVENT.register { matrixStack, _ ->
            val client = MinecraftClient.getInstance()
            if (client.player != null && !client.options.debugEnabled) {
                drawOutput(matrixStack)
            }
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
