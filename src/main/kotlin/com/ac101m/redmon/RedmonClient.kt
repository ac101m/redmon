package com.ac101m.redmon

import com.ac101m.redmon.persistence.PersistentProfileList
import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.profile.ProfileList
import com.ac101m.redmon.profile.Register
import com.ac101m.redmon.profile.RegisterType
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
import kotlin.io.path.exists
import kotlin.math.abs


class RedmonClient : ClientModInitializer {
    private val commandParser = Docopt(COMMAND_GRAMMAR)
        .withExit(false)
        .withVersion(REDMON_VERSION)

    private lateinit var profileList: ProfileList

    private var selectedProfile: Profile? = null
    private var profileOffset: Vec3i? = null


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


    private fun setActiveProfile(player: ClientPlayerEntity, profileName: String) {
        selectedProfile = profileList.getProfile(profileName)
        profileOffset = if (selectedProfile != null) {
            player.blockPos
        } else {
            null
        }
    }


    private fun deactivateSelectedProfile() {
        selectedProfile = null
        profileOffset = null
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


    private fun processProfileCreateCommand(player: ClientPlayerEntity, args: Map<String, Any>): String {
        val profileName = args.getStringCommandArgument("<name>")

        require(profileList.getProfile(profileName) == null) {
            "A profile with name '$profileName' already exists."
        }

        profileList.addProfile(Profile(profileName))
        setActiveProfile(player, profileName)

        saveProfileData()

        return "Created new profile '$profileName', and set as active profile"
    }


    private fun processProfileDeleteCommand(args: Map<String, Any>): String {
        val profileName = args.getStringCommandArgument("<name>")

        requireNotNull(profileList.getProfile(profileName)) {
            "No profile with name '$profileName'"
        }

        profileList.removeProfile(profileName)

        if (selectedProfile != null) {
            if (selectedProfile!!.name == profileName) {
                deactivateSelectedProfile()
            }
        }

        saveProfileData()

        return "Removed profile '$profileName'"
    }


    private fun processProfileSelectCommand(player: ClientPlayerEntity, args: Map<String, Any>): String {
        val profileName = args.getStringCommandArgument("<name>")

        requireNotNull(profileList.getProfile(profileName)) {
            "No profile with name '$profileName'"
        }

        setActiveProfile(player, profileName)

        return "Set profile '$profileName' as active profile"
    }


    private fun processProfileDeselectCommand(): String {
        if (selectedProfile == null) {
            return "No active profile"
        }

        val profileName = selectedProfile!!.name
        deactivateSelectedProfile()

        return "Deactivated profile '$profileName'"
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
        } else {
            throw RedmonCommandException(UNHANDLED_COMMAND_ERROR_MESSAGE)
        }
    }


    private fun getRegisterBitsFromLookDirection(
        player: ClientPlayerEntity,
        requestedBits: Int,
        type: RegisterType
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

        return bitPositions
    }


    private fun processRegisterAddCommand(player: ClientPlayerEntity, args: Map<String, Any>): String {
        val profile = checkNotNull(selectedProfile) {
            "You must select a profile before adding a register"
        }

        val registerName = args.getStringCommandArgument("<name>")
        val registerType = RegisterType.REPEATER
        val initialBitCount = args.getIntCommandArgument("--bits")

        require(profile.getRegister(registerName) == null) {
            "Register with name '$registerName' already exists"
        }

        val bitLocations = getRegisterBitsFromLookDirection(player, initialBitCount, registerType)

        val register = Register(
            registerName,
            registerType,
            args.getBooleanCommandArgument("--invert"),
            bitLocations.map { it.subtract(profileOffset!!) }
        )

        profile.addRegister(register)
        saveProfileData()

        return "Added register '$registerName' with $initialBitCount bits"
    }


    private fun processRegisterDeleteCommand(args: Map<String, Any>): String {
        val profile = checkNotNull(selectedProfile) {
            "You must select a profile before deleting a register"
        }

        val registerName = args.getStringCommandArgument("<name>")

        require(profile.registers.containsKey(registerName)) {
            "No register with name '$registerName' in profile '${profile.name}'"
        }

        profile.removeRegister(registerName)

        return "Removed register '$registerName' from profile '${profile.name}'"
    }


    private fun processRegisterInvertCommand(args: Map<String, Any>): String {
        val profile = checkNotNull(selectedProfile) {
            "You must select a profile before deleting a register"
        }

        val registerName = args.getStringCommandArgument("<name>")

        require(profile.registers.containsKey(registerName)) {
            "No register with name '$registerName' in profile '${profile.name}'"
        }

        val register = profile.getRegister(registerName)!!
        register.invert()

        return when (register.invert) {
            true -> "Register '$registerName' now in inverting mode."
            false -> "Register '$registerName' now in non-inverting mode."
        }
    }


    private fun processRegisterCommand(context: CommandContext<FabricClientCommandSource>, args: Map<String, Any>): String {
        return if (args["create"] == true) {
            processRegisterAddCommand(context.source.player, args)
        } else if (args["delete"] == true) {
            processRegisterDeleteCommand(args)
        } else if (args["invert"] == true) {
            processRegisterInvertCommand(args)
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
        val profile = if (selectedProfile == null) {
            return listOf("No active profile")
        } else {
            selectedProfile!!
        }

        val lines = ArrayList<String>()

        lines.add("${profile.name}@(x=${profileOffset!!.x} y=${profileOffset!!.y} z=${profileOffset!!.z})")

        profile.registers.values.forEach { register ->
            register.updateState(world, profileOffset!!)
            lines.add("${register.name} | ${register.getState()}")
        }

        return lines
    }


    private fun drawOutput(matrixStack: MatrixStack) {
        val world = MinecraftClient.getInstance().player?.world ?: return

        val text = getOutputText(world)

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
