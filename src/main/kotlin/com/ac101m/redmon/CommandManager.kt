package com.ac101m.redmon

import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.profile.Register
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
import net.minecraft.core.Vec3i
import net.minecraft.world.level.block.Blocks
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
        val profiles = redmon.getAllProfiles()

        if (profiles.isEmpty()) {
            ctx.sendFeedback("No profiles available")
        }

        val list = profiles.joinToString(
            separator = "\n"
        ) { profile ->
            "- ${profile.name} (${profile.registers.size} registers)"
        }

        ctx.sendFeedback("Available profiles:\n$list")
    }

    private fun profileCreateCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profileName = getString(ctx, "name")
        redmon.addProfile(Profile(profileName))
        redmon.setActiveProfile(ctx.source.player, profileName)
        ctx.sendFeedback("Created new profile '$profileName', and set as active profile")
    }

    private fun profileDeleteCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profileName = getString(ctx, "name")
        redmon.deleteProfile(profileName)
        ctx.sendFeedback("Removed profile '$profileName'")
    }

    private fun profileSelectCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profileName = getString(ctx,"name")
        redmon.setActiveProfile(ctx.source.player, profileName)
        ctx.sendFeedback("Set profile '$profileName' as active profile")
    }

    private fun profileDeselectCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        if (redmon.activeProfile == null) {
            ctx.sendFeedback("No active profile")
        } else {
            val profileName = redmon.activeProfile!!.name
            redmon.clearActiveProfile()
            ctx.sendFeedback("Deactivated profile '$profileName'")
        }
    }

    private fun profileRenameCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val name = getString(ctx, "name")
        val newName = getString(ctx,"new-name")
        redmon.renameProfile(name, newName)
        ctx.sendFeedback("Renamed profile '$name' to '$newName'")
    }

    private fun getProbeBitsFromCrosshairTarget(
        ctx: CommandContext<FabricClientCommandSource>,
        requestedBits: Int,
        type: RegisterType
    ): List<Vec3i> {
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

        val bitPositions = ArrayList<Vec3i>()

        while (bitsFound < requestedBits && (initialPos.subtract(currentPos).length() < 256.0)) {
            val blockPos = BlockPos(currentPos.x, currentPos.y, currentPos.z)
            val blockState = player.level().getBlockState(blockPos)

            if (blockState.block == Blocks.REPEATER) {
                bitsFound++
                bitPositions.add(blockPos)
            }

            currentPos = currentPos.offset(step)
        }

        check(bitsFound == requestedBits) {
            "Failed to find register bits, requested $requestedBits but found $bitsFound"
        }

        bitPositions.reverse()
        return bitPositions
    }

    private fun registerAddCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profile = checkNotNull(redmon.activeProfile) {
            "You must select a profile before adding a register"
        }

        val registerName = getString(ctx, "name")
        val registerType = RegisterType.REPEATER
        val initialBitCount = getInteger(ctx, "bit-count")

        require(profile.getRegister(registerName) == null) {
            "Register with name '$registerName' already exists"
        }

        val bitLocations = getProbeBitsFromCrosshairTarget(ctx, initialBitCount, registerType)

        val newRegister = Register(
            registerName,
            registerType,
            false,
            RegisterFormat.UNSIGNED,
            bitLocations.map { it.subtract(redmon.profileOffset!!) }
        )

        profile.addRegister(newRegister)
        redmon.saveProfiles()

        ctx.sendFeedback("Added register '$registerName' with $initialBitCount bits")
    }

    private fun registerDeleteCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profile = checkNotNull(redmon.activeProfile) {
            "You must select a profile before deleting a register"
        }

        val registerName = getString(ctx, "name")

        require(profile.registers.containsKey(registerName)) {
            "No register with name '$registerName' in profile '${profile.name}'"
        }

        profile.removeRegister(registerName)
        redmon.saveProfiles()

        ctx.sendFeedback("Removed register '$registerName' from profile '${profile.name}'")
    }

    private fun registerInvertCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profile = checkNotNull(redmon.activeProfile) {
            "You must select a profile before deleting a register"
        }

        val registerName = getString(ctx, "name")

        val register = requireNotNull(profile.getRegister(registerName)) {
            "No register with name '$registerName' in profile '${profile.name}'"
        }

        register.invert()
        redmon.saveProfiles()

        when (register.invert) {
            true -> ctx.sendFeedback("Register '$registerName' now in inverting mode")
            false -> ctx.sendFeedback("Register '$registerName' now in non-inverting mode")
        }
    }

    private fun registerFlipCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profile = checkNotNull(redmon.activeProfile) {
            "You must select a profile before flipping a register"
        }

        val registerName = getString(ctx, "name")

        val register = requireNotNull(profile.getRegister(registerName)) {
            "No register with name '$registerName' in profile '${profile.name}'"
        }

        register.flipBits()
        redmon.saveProfiles()

        ctx.sendFeedback("Flipped register '${register.name}'")
    }

    private fun registerAppendBitsCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profile = checkNotNull(redmon.activeProfile) {
            "You must select a profile before appending bits to a register"
        }

        val registerName = getString(ctx,"name")
        val bitCount = getInteger(ctx,"bit-count")
        val registerType = RegisterType.REPEATER

        val register = requireNotNull(profile.getRegister(registerName)) {
            "No register with name '$registerName' in profile '${profile.name}'"
        }

        val bitPositions = getProbeBitsFromCrosshairTarget(ctx, bitCount, registerType)

        register.appendBits(bitPositions.map { position -> position.subtract(redmon.profileOffset!!) })
        redmon.saveProfiles()

        ctx.sendFeedback("Appended $bitCount bits to register '${register.name}' in profile '${profile.name}'")
    }

    private fun registerRenameCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profile = checkNotNull(redmon.activeProfile) {
            "You must select a profile before renaming a register"
        }

        val name = getString(ctx,"name")
        val newName = getString(ctx,"new-name")

        redmon.renameRegister(name, newName)

        ctx.sendFeedback("Renamed register '$name' in profile ${profile.name} to '$newName'")
    }

    private fun registerFormatCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profile = checkNotNull(redmon.activeProfile) {
            "You must select a profile before setting register format"
        }

        val registerName = getString(ctx, "name")
        val newFormatArg = getString(ctx, "format")

        val newFormat = try {
            RegisterFormat.valueOf(newFormatArg.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException(
                "'$newFormatArg' is not a valid format. " +
                        "Valid formats are ${RegisterFormat.entries.joinToString(", ") { it.name.lowercase() }}", e)
        }

        if (registerName == "all") {
            profile.registers.forEach { it.value.format = newFormat }
            ctx.sendFeedback("Set format of all registers in profile ${profile.name} to '$newFormat'")
        } else {
            val register = requireNotNull(profile.getRegister(registerName)) {
                "No register with name '$registerName' in profile '${profile.name}'"
            }
            register.format = newFormat
            ctx.sendFeedback("Set format of register '${register.name}' in profile '${profile.name}' to '$newFormat'")
        }.also {
            redmon.saveProfiles()
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
