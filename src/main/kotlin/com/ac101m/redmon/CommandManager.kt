package com.ac101m.redmon

import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.profile.SignalFormat
import com.ac101m.redmon.profile.SignalType
import com.ac101m.redmon.utils.CardinalDirection
import com.ac101m.redmon.utils.Config.Companion.PROFILES_PER_PAGE
import com.ac101m.redmon.utils.RedmonException
import com.ac101m.redmon.utils.ceilDiv
import com.ac101m.redmon.utils.length
import com.ac101m.redmon.utils.sendError
import com.ac101m.redmon.utils.sendFeedback
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.string
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import kotlin.math.min

/**
 * Manages the redmon command interface.
 * Command registration and execution logic lives here.
 *
 * @param redmon The main redmon state object.
 */
class CommandManager(
    val redmon: RedmonState
) {
    private fun LiteralArgumentBuilder<FabricClientCommandSource>.allCommands(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return this.then(literal("hide").executes { _ -> hideCommand() })
            .then(literal("show").executes { _ -> showCommand() })
            .then(literal("profile")
                .then(literal("list").then(int("page", 1)
                    .executes { c -> profileListCommand(c) }))
                .then(literal("search").then(str("query").then(int("page", 1)
                    .executes { c -> profileSearchCommand(c) } )))
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
            .then(literal("signal")
                .then(literal("add").then(str("name").then(int("block-count")
                    .executes { c -> signalAddCommand(c) })))
                .then(literal("delete").then(str("name")
                    .executes { c -> signalDeleteCommand(c) }))
                .then(literal("invert").then(str("name")
                    .executes { c -> signalInvertCommand(c) }))
                .then(literal("flip").then(str("name")
                    .executes { c -> signalFlipCommand(c) }))
                .then(literal("add-block").then(str("name")
                    .executes { c -> signalAppendBlockCommand(c) }))
                .then(literal("add-blocks").then(str("name").then(int("count", 0)
                    .executes { c -> signalAppendBlocksCommand(c) })))
                .then(literal("rename").then(str("name").then(str("new-name")
                    .executes { c -> signalRenameCommand(c) })))
                .then(literal("format").then(str("name").then(str("format")
                    .executes { c -> signalFormatCommand(c) })))
                .then(literal("move").then(str("name")
                    .then(literal("up").then(int("count", 0)
                        .executes { c -> signalMoveUpCommand(c) }))
                    .then(literal("down").then(int("count", 0)
                        .executes { c -> signalMoveDownCommand(c) })))
                )
            )
    }

    fun registerCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        dispatcher.register(literal("redmon").allCommands())
        dispatcher.register(literal("rm").allCommands())
    }

    private fun showCommand(): Int {
        redmon.show()
        return COMMAND_SUCCESS
    }

    private fun hideCommand(): Int {
        redmon.hide()
        return COMMAND_SUCCESS
    }

    private fun paginateProfileList(ctx: CommandContext<FabricClientCommandSource>, names: List<String>) {
        if (names.isEmpty()) {
            ctx.sendFeedback("No profiles found")
            return
        }

        val pageNumber = getInteger(ctx, "page")
        val pageIndex = pageNumber - 1

        val pageCount = names.size.ceilDiv(PROFILES_PER_PAGE)

        require(pageIndex < pageCount) {
            "Requested page does not exist"
        }

        val start = pageIndex * PROFILES_PER_PAGE
        val end = min(start + PROFILES_PER_PAGE, names.size)
        val pageNames = names.slice(start until end)

        val list = pageNames.joinToString(separator = "\n") { "- $it" }
        ctx.sendFeedback("Found ${names.size} profiles (page $pageNumber/$pageCount):\n$list")
    }

    private fun profileListCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val names = redmon.getProfileNames().sorted()
        paginateProfileList(ctx, names)
    }

    private fun profileSearchCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val names = redmon.getProfileNames()
        val queryString = getString(ctx, "query")
        val filteredNames = names.filter { it.contains(queryString, ignoreCase = true) }
        paginateProfileList(ctx, filteredNames)
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

    private fun getBlockFromCrosshairTarget(player: Player, signalType: SignalType): BlockPos {
        val hitResult = Minecraft.getInstance().hitResult ?:
            throw RedmonException("No target block found")

        val blockHitResult = if (hitResult.type != HitResult.Type.BLOCK) {
            throw RedmonException("Target is not a block")
        } else {
            hitResult as BlockHitResult
        }

        val position = blockHitResult.blockPos
        val expectedBlock = signalType.block
        val block = player.level().getBlockState(position).block

        require(block == signalType.block) {
            "Signal expects blocks of type '$expectedBlock', but target block is '$block'"
        }

        return position
    }

    private fun getBlocksFromCrosshairTargetAndLookDirection(
        ctx: CommandContext<FabricClientCommandSource>,
        requestedBlocks: Int,
        signalType: SignalType
    ): List<BlockPos> {
        val player = ctx.source.player
        val step = CardinalDirection.fromLook(player.lookAngle).vector
        val initialPos = getBlockFromCrosshairTarget(player, signalType)
        var currentPos = initialPos
        var bitsFound = 0

        val bitPositions = ArrayList<BlockPos>()
        val blockType = signalType.block

        while (bitsFound < requestedBlocks && (initialPos.subtract(currentPos).length() < 256.0)) {
            val blockPos = BlockPos(currentPos.x, currentPos.y, currentPos.z)
            val blockState = player.level().getBlockState(blockPos)

            if (blockState.block == blockType) {
                bitsFound++
                bitPositions.add(blockPos)
            }

            currentPos = currentPos.offset(step)
        }

        check(bitsFound == requestedBlocks) {
            "Failed to find signal bits of type $signalType, requested $requestedBlocks but found $bitsFound"
        }

        bitPositions.reverse()
        return bitPositions
    }

    private fun signalAddCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val initialBlockCount = getInteger(ctx, "block-count")

        val signalName = getString(ctx, "name")
        val signalType = SignalType.REPEATER
        val inverted = false
        val format = SignalFormat.HEX
        val blockLocations = when (initialBlockCount) {
            0 -> emptyList()
            else -> getBlocksFromCrosshairTargetAndLookDirection(ctx, initialBlockCount, signalType)
        }

        redmon.addSignal(signalName, signalType, inverted, format, blockLocations)

        ctx.sendFeedback("Added signal '$signalName' with $initialBlockCount bits")
    }

    private fun signalDeleteCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val signalName = getString(ctx, "name")
        redmon.deleteSignal(signalName)
        ctx.sendFeedback("Removed signal '$signalName' from active profile")
    }

    private fun signalInvertCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val signalName = getString(ctx, "name")
        val newState = redmon.invertSignal(signalName)
        when (newState) {
            true -> ctx.sendFeedback("Signal '$signalName' now in inverting mode")
            false -> ctx.sendFeedback("Signal '$signalName' now in non-inverting mode")
        }
    }

    private fun signalFlipCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val signalName = getString(ctx, "name")
        redmon.flipSignalBits(signalName)
        ctx.sendFeedback("Flipped signal '$signalName'")
    }

    private fun signalAppendBlockCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val signalName = getString(ctx, "name")
        val signalType = redmon.getSignalType(signalName)
        val blockLocation = getBlockFromCrosshairTarget(ctx.source.player, signalType)

        redmon.appendBlocksToSignal(signalName, listOf(blockLocation))

        ctx.sendFeedback("Added block to signal '$signalName' in the active profile")
    }

    private fun signalAppendBlocksCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val signalName = getString(ctx,"name")
        val blockCount = getInteger(ctx,"count")
        val signalType = redmon.getSignalType(signalName)
        val blockLocations = getBlocksFromCrosshairTargetAndLookDirection(ctx, blockCount, signalType)

        redmon.appendBlocksToSignal(signalName, blockLocations)

        ctx.sendFeedback("Appended $blockCount blocks to signal '$signalName' in the active profile")
    }

    private fun signalRenameCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val signalName = getString(ctx,"name")
        val newSignalName = getString(ctx,"new-name")

        redmon.renameSignal(signalName, newSignalName)

        ctx.sendFeedback("Renamed signal '$signalName' in the active profile to '$newSignalName'")
    }

    private fun signalFormatCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val signalName = getString(ctx, "name")
        val newFormatString = getString(ctx, "format")

        val newFormat = try {
            SignalFormat.valueOf(newFormatString.uppercase())
        } catch (e: IllegalArgumentException) {
            val validFormatString = SignalFormat.entries.joinToString(", ") { it.name.lowercase() }
            throw IllegalArgumentException("Invalid format. Valid formats are $validFormatString", e)
        }

        if (signalName == "all") {
            redmon.setAllSignalFormats(newFormat)
            ctx.sendFeedback("Set format of all signals in active profile to '$newFormat'")
        } else {
            redmon.setSignalFormat(signalName, newFormat)
            ctx.sendFeedback("Set format of signal '${signalName}' in active profile to '$newFormat'")
        }
    }

    private fun signalMoveUpCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val signalName = getString(ctx, "name")
        val count = 0 - getInteger(ctx, "count")
        when (val n = redmon.moveSignal(signalName, count)) {
            0 -> ctx.sendFeedback("Signal '$signalName' is already at the top.")
            else -> ctx.sendFeedback("Moved signal '$signalName' in active profile up ${0 - n} places")
        }
    }

    private fun signalMoveDownCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val signalName = getString(ctx, "name")
        val count = getInteger(ctx, "count")
        when (val n = redmon.moveSignal(signalName, count)) {
            0 -> ctx.sendFeedback("Signal '$signalName' is already at the bottom.")
            else -> ctx.sendFeedback("Moved signal '$signalName' in active profile down $n places")
        }
    }

    companion object {
        private const val COMMAND_ERROR = -1
        private const val COMMAND_SUCCESS = 1

        private fun str(name: String): RequiredArgumentBuilder<FabricClientCommandSource, String> {
            return argument(name, string())
        }

        private fun int(
            name: String,
            min: Int = Int.MIN_VALUE,
            max: Int = Int.MAX_VALUE
        ): RequiredArgumentBuilder<FabricClientCommandSource, Int> {
            return argument(name, integer(min, max))
        }

        private fun commandWrapper(
            ctx: CommandContext<FabricClientCommandSource>,
            actions: (CommandContext<FabricClientCommandSource>) -> Unit
        ): Int {
            return try {
                actions(ctx)
                COMMAND_SUCCESS
            } catch (e: Throwable) {
                ctx.sendError(e.message ?: e.javaClass.toString())
                COMMAND_ERROR
            }
        }
    }
}
