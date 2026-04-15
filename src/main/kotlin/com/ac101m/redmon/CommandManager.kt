package com.ac101m.redmon

import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.profile.RegisterFormat
import com.ac101m.redmon.profile.RegisterType
import com.ac101m.redmon.utils.CardinalDirection
import com.ac101m.redmon.utils.RedmonException
import com.ac101m.redmon.utils.int
import com.ac101m.redmon.utils.length
import com.ac101m.redmon.utils.sendError
import com.ac101m.redmon.utils.sendFeedback
import com.ac101m.redmon.utils.str
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

/**
 * Manages the redmon command interface.
 * Command registration and execution logic lives here.
 *
 * @param redmon The main redmon state object.
 */
class CommandManager(
    val redmon: RedmonState
) {
    fun registerCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        dispatcher.register(literal("redmon")
            .then(literal("hide").executes { _ -> hideCommand() })
            .then(literal("show").executes { _ -> showCommand() })
            .then(literal("profile")
                .then(literal("list").executes { c -> profileListCommand(c) })
                .then(literal("create").then(str("name")
                    .executes { c -> profileCreateCommand(c) }))
                .then(literal("delete").then(str("name")
                    .executes { c -> profileDeleteCommand(c) }))
                .then(literal("select").then(str("name")
                    .executes { c -> profileSelectCommand(c) }))
                .then(literal("deselect").then(str("name")
                    .executes { c -> profileDeselectCommand(c) }))
                .then(literal("rename").then(str("name").then(str("new-name")
                    .executes { c -> profileRenameCommand(c) })))
            )
            .then(literal("register")
                .then(literal("add").then(str("name").then(int("bit-count")
                    .executes { c -> registerAddCommand(c) })))
                .then(literal("delete").then(str("name")
                    .executes { c -> registerDeleteCommand(c) }))
                .then(literal("invert").then(str("name")
                    .executes { c -> registerInvertCommand(c) }))
                .then(literal("flip").then(str("name")
                    .executes { c -> registerFlipCommand(c) }))
                .then(literal("append-bits").then(str("name").then(int("bit-count")
                    .executes { c -> registerAppendBitsCommand(c) })))
                .then(literal("rename").then(str("name").then(str("new-name")
                    .executes { c -> registerRenameCommand(c) })))
                .then(literal("format").then(str("name").then(str("format")
                    .executes { c -> registerFormatCommand(c) } )))
            )
        )
    }

    private fun showCommand(): Int {
        redmon.show()
        return COMMAND_SUCCESS
    }

    private fun hideCommand(): Int {
        redmon.hide()
        return COMMAND_SUCCESS
    }

    private fun profileListCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val names = redmon.getProfileNames()

        if (names.isEmpty()) {
            ctx.sendFeedback("No profiles available")
        } else {
            val list = names.joinToString(separator = "\n") { "- $it" }
            ctx.sendFeedback("Available profiles:\n$list")
        }
    }

    private fun profileCreateCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profileName = getString(ctx, "name")
        val offset = ctx.source.player.position()
        redmon.addProfile(Profile(profileName, emptyList()))
        redmon.setActiveProfile(profileName, offset)
        ctx.sendFeedback("Created new profile '$profileName', and set as active profile")
    }

    private fun profileDeleteCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profileName = getString(ctx, "name")
        redmon.deleteProfile(profileName)
        ctx.sendFeedback("Removed profile '$profileName'")
    }

    private fun profileSelectCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profileName = getString(ctx,"name")
        val offset = ctx.source.player.position()
        redmon.setActiveProfile(profileName, offset)
        ctx.sendFeedback("Set profile '$profileName' as active profile")
    }

    private fun profileDeselectCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        if (redmon.hasActiveProfile()) {
            redmon.clearActiveProfile()
            ctx.sendFeedback("Deactivated current profile")
        } else {
            ctx.sendFeedback("No active profile")
        }
    }

    private fun profileRenameCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val name = getString(ctx, "name")
        val newName = getString(ctx,"new-name")
        redmon.renameProfile(name, newName)
        ctx.sendFeedback("Renamed profile '$name' to '$newName'")
    }

    private fun getBitsFromCrosshairTarget(
        ctx: CommandContext<FabricClientCommandSource>,
        requestedBits: Int,
        registerType: RegisterType
    ): List<BlockPos> {
        val player = ctx.source.player
        val step = CardinalDirection.fromLook(player.lookAngle).vector

        val hitResult = Minecraft.getInstance().hitResult ?:
        throw RedmonException("Crosshair is not pointing at anything.")

        val blockHitResult = if (hitResult.type != HitResult.Type.BLOCK) {
            throw RedmonException("Crosshair is not pointing at a block.")
        } else {
            hitResult as BlockHitResult
        }

        val initialPos = blockHitResult.blockPos
        var currentPos = initialPos
        var bitsFound = 0

        val bitPositions = ArrayList<BlockPos>()
        val blockType = registerType.getBlock()

        while (bitsFound < requestedBits && (initialPos.subtract(currentPos).length() < 256.0)) {
            val blockPos = BlockPos(currentPos.x, currentPos.y, currentPos.z)
            val blockState = player.level().getBlockState(blockPos)

            if (blockState.block == blockType) {
                bitsFound++
                bitPositions.add(blockPos)
            }

            currentPos = currentPos.offset(step)
        }

        check(bitsFound == requestedBits) {
            "Failed to find register bits of type $registerType, requested $requestedBits but found $bitsFound"
        }

        bitPositions.reverse()
        return bitPositions
    }

    private fun registerAddCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val initialBitCount = getInteger(ctx, "bit-count")

        val registerName = getString(ctx, "name")
        val registerType = RegisterType.REPEATER
        val inverted = false
        val format = RegisterFormat.UNSIGNED
        val bitLocations = getBitsFromCrosshairTarget(ctx, initialBitCount, registerType)

        redmon.addRegister(registerName, registerType, inverted, format, bitLocations)

        ctx.sendFeedback("Added register '$registerName' with $initialBitCount bits")
    }

    private fun registerDeleteCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val registerName = getString(ctx, "name")
        redmon.deleteRegister(registerName)
        ctx.sendFeedback("Removed register '$registerName' from active profile")
    }

    private fun registerInvertCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val registerName = getString(ctx, "name")
        val newState = redmon.invertRegister(registerName)
        when (newState) {
            true -> ctx.sendFeedback("Register '$registerName' now in inverting mode")
            false -> ctx.sendFeedback("Register '$registerName' now in non-inverting mode")
        }
    }

    private fun registerFlipCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val registerName = getString(ctx, "name")
        redmon.flipRegisterBits(registerName)
        ctx.sendFeedback("Flipped register '$registerName'")
    }

    private fun registerAppendBitsCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val registerName = getString(ctx,"name")
        val bitCount = getInteger(ctx,"bit-count")
        val registerType = redmon.getRegisterType(registerName)

        val bitPositions = getBitsFromCrosshairTarget(ctx, bitCount, registerType)
        redmon.appendBitsToRegister(registerName, bitPositions)

        ctx.sendFeedback("Appended $bitCount bits to register '$registerName' in the active profile")
    }

    private fun registerRenameCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val registerName = getString(ctx,"name")
        val newRegisterName = getString(ctx,"new-name")

        redmon.renameRegister(registerName, newRegisterName)

        ctx.sendFeedback("Renamed register '$registerName' in the active profile to '$newRegisterName'")
    }

    private fun registerFormatCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val registerName = getString(ctx, "name")
        val newFormatArg = getString(ctx, "format")

        val newFormat = try {
            RegisterFormat.valueOf(newFormatArg.uppercase())
        } catch (e: IllegalArgumentException) {
            val validFormatString = RegisterFormat.entries.joinToString(", ") { it.name.lowercase() }
            throw IllegalArgumentException("Invalid format. Valid formats are $validFormatString", e)
        }

        if (registerName == "all") {
            redmon.setAllRegisterFormats(newFormat)
            ctx.sendFeedback("Set format of all registers in active profile to '$newFormat'")
        } else {
            redmon.setRegisterFormat(registerName, newFormat)
            ctx.sendFeedback("Set format of register '${registerName}' in active profile to '$newFormat'")
        }
    }

    companion object {
        private const val COMMAND_ERROR = -1
        private const val COMMAND_SUCCESS = 1

        private fun commandWrapper(
            ctx: CommandContext<FabricClientCommandSource>,
            actions: (CommandContext<FabricClientCommandSource>) -> Unit
        ): Int {
            return try {
                actions(ctx)
                COMMAND_SUCCESS
            } catch (e: Throwable) {
                ctx.sendError("Error: ${e.message}")
                COMMAND_ERROR
            }
        }
    }
}
