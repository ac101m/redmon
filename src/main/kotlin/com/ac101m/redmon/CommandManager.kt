package com.ac101m.redmon

import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.profile.SignalFormat
import com.ac101m.redmon.profile.SignalType
import com.ac101m.redmon.utils.CardinalDirection
import com.ac101m.redmon.utils.Config.Companion.DEFAULT_SIGNAL_FORMAT
import com.ac101m.redmon.utils.Config.Companion.PAGINATED_LIST_ENTRIES_PER_PAGE
import com.ac101m.redmon.utils.RedmonException
import com.ac101m.redmon.utils.ceilDiv
import com.ac101m.redmon.utils.length
import com.ac101m.redmon.utils.sendError
import com.ac101m.redmon.utils.sendFeedback
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
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
        return this
            .miscCommands()
            .profileCommands()
            .pageCommands()
            .signalCommands()
            // TODO: Complete implementation
            //.isaCommands()
            //.instructionCommands()
    }

    private fun LiteralArgumentBuilder<FabricClientCommandSource>.miscCommands(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return this
            .then(literal("hide").executes { _ -> hideCommand() })
            .then(literal("show").executes { _ -> showCommand() })
    }

    private fun LiteralArgumentBuilder<FabricClientCommandSource>.profileCommands(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return this.then(literal("profile")
            .then(literal("list")
                .then(int("page", 1)
                    .executes { c -> profileListCommand(c) }
                )
                .executes { c -> profileListCommandFirstPage(c) }
            )
            .then(literal("search")
                .then(str("query")
                    .executes { c -> profileSearchCommandSinglePage(c) }
                )
            )
            .then(literal("search")
                .then(str("query")
                    .then(int("page", 1)
                        .executes { c -> profileSearchCommand(c) }
                    )
                )
            )
            .then(literal("create")
                .then(str("name", ::suggestProfileNames)
                    .executes { c -> profileCreateCommand(c) }
                )
            )
            .then(literal("delete")
                .then(str("name", ::suggestProfileNames)
                    .executes { c -> profileDeleteCommand(c) }
                )
            )
            .then(literal("select")
                .then(str("name", ::suggestProfileNames)
                    .executes { c -> profileSelectCommand(c) }
                )
            )
            .then(literal("deselect")
                .executes { c -> profileDeselectCommand(c) }
            )
            .then(literal("rename")
                .then(str("name", ::suggestProfileNames)
                    .then(str("new-name")
                        .executes { c -> profileRenameCommand(c) }
                    )
                )
            )
        )
    }

    private fun LiteralArgumentBuilder<FabricClientCommandSource>.pageCommands(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return this.then(literal("page")
            .then(literal("add")
                .then(str("name")
                    .executes { c -> pageAddCommand(c) }
                )
            )
            .then(literal("remove")
                .then(str("name", ::suggestPageNames)
                    .executes { c -> pageRemoveCommand(c) }
                )
            )
            .then(literal("next")
                .executes { c -> nextPageCommand(c) }
            )
            .then(literal("previous")
                .executes { c -> previousPageCommand(c) }
            )
            .then(literal("rename")
                .then(str("new-name")
                    .executes { c -> pageRenameCommand(c) }
                )
            )
        )
    }

    private fun LiteralArgumentBuilder<FabricClientCommandSource>.signalCommands(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return this.then(literal("signal")
            .then(literal("add")
                .then(str("name")
                    .then(str("signal-type", SignalType::suggestNames)
                        .then(int("block-count")
                            .then(int("column-number", 1, suggestionProvider = ::suggestColumnNumbers)
                                .executes { c -> signalAddWithColumnCommand(c) }
                            )
                            .executes { c -> signalAddCommand(c) }
                        )
                    )
                )
            )
            .then(literal("remove")
                .then(str("name", ::suggestSignalNames)
                    .executes { c -> signalRemoveCommand(c) }
                )
            )
            .then(literal("invert")
                .then(str("name", ::suggestSignalNames)
                    .executes { c -> signalInvertCommand(c) }
                )
            )
            .then(literal("flip")
                .then(str("name", ::suggestSignalNames)
                    .executes { c -> signalFlipCommand(c) }
                )
            )
            .then(literal("add-block")
                .then(str("name", ::suggestSignalNames)
                    .executes { c -> signalAppendBlockCommand(c) }
                )
            )
            .then(literal("add-blocks")
                .then(str("name", ::suggestSignalNames)
                    .then(int("count", 0)
                        .executes { c -> signalAppendBlocksCommand(c) }
                    )
                )
            )
            .then(literal("rename")
                .then(str("name", ::suggestSignalNames)
                    .then(str("new-name")
                        .executes { c -> signalRenameCommand(c) }
                    )
                )
            )
            .then(literal("format")
                .then(str("name", ::suggestSignalNames)
                    .then(str("format", SignalFormat::suggestNames)
                        .executes { c ->
                            signalFormatCommand(c)
                        }
                    )
                )
            )
            .then(literal("move")
                .then(str("name", ::suggestSignalNames)
                    .then(literal("up")
                        .then(int("count", 1)
                            .executes { c -> signalMoveUpCommand(c) }
                        )
                        .executes { c -> signalMoveUpOneCommand(c) }
                    )
                    .then(literal("down")
                        .then(int("count", 1)
                            .executes { c -> signalMoveDownCommand(c) }
                        )
                        .executes { c -> signalMoveDownOneCommand(c) }
                    )
                    .then(literal("column")
                        .then(int("column-number", 1, suggestionProvider = ::suggestColumnNumbers)
                            .executes { c -> signalMoveColumnCommand(c) }
                        )
                    )
                )
            )
        )
    }

    private fun LiteralArgumentBuilder<FabricClientCommandSource>.isaCommands(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return this.then(literal("isa")
            .then(literal("list")
                .then(int("page")
                    .executes { c -> isaListCommand(c) }
                )
                .executes { c -> isaListCommandFirstPage(c) }
            )
            .then(literal("create")
                .then(str("isa-name")
                    .then(int("instruction-size")
                        .executes { c -> isaCreateCommand(c) }
                    )
                )
            )
            .then(literal("delete")
                .then(str("isa-name", ::suggestInstructionSetNames)
                    .executes { c -> isaDeleteCommand(c) }
                )
            )
            .then(literal("rename")
                .then(str("isa-name", ::suggestInstructionSetNames)
                    .then(str("new-isa-name")
                        .executes { c -> isaRenameCommand(c) }
                    )
                )
            )
        )
    }

    private fun LiteralArgumentBuilder<FabricClientCommandSource>.instructionCommands(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return this.then(literal("instruction")
            .then(literal("add")
                .then(str("isa-name", ::suggestInstructionSetNames)
                    .then(str("instruction-name")
                        .then(greedyStr("instruction-sections")
                            .executes { c -> instructionAddCommand(c) }
                        )
                    )
                )
            )
            .then(literal("remove")
                .then(str("isa-name", ::suggestInstructionSetNames)
                    .then(str("instruction-name")
                        .executes { c -> instructionRemoveCommand(c) }
                    )
                )
            )
        )
    }

    fun registerCommands(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
        dispatcher.register(literal("redmon").allCommands())
        dispatcher.register(literal("rm").allCommands())
    }

    private fun suggestProfileNames(currentInput: String): List<String> {
        return ArrayList<String>().apply {
            redmon.getProfileNames().forEach { name ->
                if (name.contains(currentInput)) {
                    add(name)
                }
            }
        }
    }

    private fun suggestSignalNames(currentInput: String): List<String> {
        return ArrayList<String>(MAX_COMMAND_SUGGESTIONS).apply {
            redmon.getVisibleSignalNames().forEach { name ->
                if (name.contains(currentInput)) {
                    add(name)
                }
            }
        }
    }

    private fun suggestInstructionSetNames(currentInput: String): List<String> {
        return ArrayList<String>().apply {
            redmon.getInstructionSetNames().forEach { name ->
                if (name.contains(currentInput)) {
                    add(name)
                }
            }
        }
    }

    private fun suggestColumnNumbers(currentInput: String): List<String> {
        return ArrayList<String>().apply {
            val visibleColumnCount = redmon.getVisibleColumnCount() ?: 0
            repeat(visibleColumnCount + 1) { i ->
                val str = (i + 1).toString()
                if (str.contains(currentInput)) {
                    add(str)
                }
            }
        }
    }

    private fun suggestPageNames(currentInput: String): List<String> {
        return ArrayList<String>().apply {
            redmon.getCurrentProfilePageNames().forEachIndexed { i, name ->
                if (name.contains(currentInput)) {
                    add(name)
                }
            }
        }
    }

    private fun showCommand(): Int {
        redmon.show()
        return COMMAND_SUCCESS
    }

    private fun hideCommand(): Int {
        redmon.hide()
        return COMMAND_SUCCESS
    }

    private fun paginateList(
        ctx: CommandContext<FabricClientCommandSource>,
        names: List<String>,
        pageNumber: Int,
        entryType: String
    ) {
        if (names.isEmpty()) {
            ctx.sendFeedback("No $entryType found")
            return
        }

        val pageIndex = pageNumber - 1
        val pageCount = names.size.ceilDiv(PAGINATED_LIST_ENTRIES_PER_PAGE)

        require(pageIndex < pageCount) {
            "Requested page does not exist"
        }

        val start = pageIndex * PAGINATED_LIST_ENTRIES_PER_PAGE
        val end = min(start + PAGINATED_LIST_ENTRIES_PER_PAGE, names.size)
        val pageNames = names.slice(start until end)

        val list = pageNames.joinToString(separator = "\n") { "- $it" }
        ctx.sendFeedback("Found ${names.size} $entryType (page $pageNumber/$pageCount):\n$list")
    }

    private fun doProfileList(ctx: CommandContext<FabricClientCommandSource>, pageNumber: Int) {
        val names = redmon.getProfileNames().sorted()
        paginateList(ctx, names, pageNumber, "profiles")
    }

    private fun doProfileSearch(ctx: CommandContext<FabricClientCommandSource>, pageNumber: Int) {
        val names = redmon.getProfileNames().sorted()
        val queryString = getString(ctx, "query")
        val filteredNames = names.filter { it.contains(queryString, ignoreCase = true) }
        paginateList(ctx, filteredNames, pageNumber, "profiles")
    }

    private fun profileListCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        doProfileList(ctx, getInteger(ctx, "page"))
    }

    private fun profileListCommandFirstPage(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        doProfileList(ctx, 1)
    }

    private fun profileSearchCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        doProfileSearch(ctx, getInteger(ctx, "page"))
    }

    private fun profileSearchCommandSinglePage(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        doProfileSearch(ctx, 1)
    }

    private fun profileCreateCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profileName = getString(ctx, "name")
        val offset = ctx.source.player.blockPosition()
        redmon.addProfile(Profile(profileName, emptyList()))
        redmon.setActiveProfile(profileName, offset)
        ctx.sendFeedback("Created new profile '$profileName', and set as the active profile")
    }

    private fun profileDeleteCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profileName = getString(ctx, "name")
        redmon.deleteProfile(profileName)
        ctx.sendFeedback("Deleted profile '$profileName'")
    }

    private fun profileSelectCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val profileName = getString(ctx,"name")
        val offset = ctx.source.player.blockPosition()
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

    private fun nextPageCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        redmon.nextPage()
    }

    private fun previousPageCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        redmon.previousPage()
    }

    private fun pageAddCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val name = getString(ctx, "name")
        redmon.addPageToActiveProfile(name)
    }

    private fun pageRemoveCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val name = getString(ctx, "name")
        redmon.removePageFromActiveProfile(name)
    }

    private fun pageRenameCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val newName = getString(ctx, "new-name")
        redmon.renameCurrentPage(newName)
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
        val expectedBlocks = signalType.getValidBlocks()
        val block = player.level().getBlockState(position).block

        require(block in expectedBlocks) {
            val blocksString = expectedBlocks.joinToString(" or ") { "'$it'" }
            "Signal expects blocks of type $blocksString, but target block is '$block'"
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
        val blockTypes = signalType.getValidBlocks()

        while (bitsFound < requestedBlocks && (initialPos.subtract(currentPos).length() < 256.0)) {
            val blockPos = BlockPos(currentPos.x, currentPos.y, currentPos.z)
            val blockState = player.level().getBlockState(blockPos)

            if (blockState.block in blockTypes) {
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

    private fun doSignalAdd(ctx: CommandContext<FabricClientCommandSource>, columnIndex: Int) {
        val initialBlockCount = getInteger(ctx, "block-count")
        val signalName = getString(ctx, "name")
        val type = SignalType.fromCommandString(getString(ctx, "signal-type"))
        val inverted = false
        val format = DEFAULT_SIGNAL_FORMAT
        val blockLocations = when (initialBlockCount) {
            0 -> emptyList()
            else -> getBlocksFromCrosshairTargetAndLookDirection(ctx, initialBlockCount, type)
        }

        redmon.addSignal(signalName, type, inverted, format, blockLocations, columnIndex)

        ctx.sendFeedback("Added signal '$signalName' with $initialBlockCount bits")
    }

    private fun signalAddCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        doSignalAdd(ctx, 0)
    }

    private fun signalAddWithColumnCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val columnIndex = getInteger(ctx, "column-number") - 1
        doSignalAdd(ctx, columnIndex)
    }

    private fun signalRemoveCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val signalName = getString(ctx, "name")
        redmon.removeSignal(signalName)
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
        val newFormat = SignalFormat.fromCommandString(getString(ctx, "format"))
        if (signalName == "all") {
            redmon.setAllSignalFormats(newFormat)
            ctx.sendFeedback("Set format of all signals in active profile to $newFormat")
        } else {
            redmon.setSignalFormat(signalName, newFormat)
            ctx.sendFeedback("Set format of signal '${signalName}' in active profile to $newFormat")
        }
    }

    private fun doSignalMoveUp(ctx: CommandContext<FabricClientCommandSource>, count: Int) {
        val signalName = getString(ctx, "name")
        when (val n = redmon.moveSignalVertically(signalName, 0 - count)) {
            0 -> ctx.sendError("Signal '$signalName' is already at the top.")
            else -> ctx.sendFeedback("Moved signal '$signalName' in active profile up ${0 - n} places")
        }
    }

    private fun signalMoveUpCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val count = getInteger(ctx, "count")
        doSignalMoveUp(ctx, count)
    }

    private fun signalMoveUpOneCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        doSignalMoveUp(ctx, 1)
    }

    private fun doSignalMoveDown(ctx: CommandContext<FabricClientCommandSource>, count: Int) {
        val signalName = getString(ctx, "name")
        when (val n = redmon.moveSignalVertically(signalName, count)) {
            0 -> ctx.sendError("Signal '$signalName' is already at the bottom.")
            else -> ctx.sendFeedback("Moved signal '$signalName' in active profile down $n places")
        }
    }

    private fun signalMoveDownCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val count = getInteger(ctx, "count")
        doSignalMoveDown(ctx, count)
    }

    private fun signalMoveDownOneCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        doSignalMoveDown(ctx, 1)
    }

    private fun signalMoveColumnCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val signalName = getString(ctx, "name")
        val columnIndex = getInteger(ctx, "column-number") - 1
        redmon.changeSignalColumn(signalName, columnIndex)
    }

    private fun doIsaList(ctx: CommandContext<FabricClientCommandSource>, pageNumber: Int) {
        val names = redmon.getInstructionSetNames().sorted()
        paginateList(ctx, names, pageNumber, "instruction sets")
    }

    private fun isaListCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val pageNumber = getInteger(ctx, "page")
        doIsaList(ctx, pageNumber)
    }

    private fun isaListCommandFirstPage(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        doIsaList(ctx, 1)
    }

    private fun isaCreateCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val name = getString(ctx, "isa-name")
        val instructionSize = getInteger(ctx, "instruction-size")
        redmon.createInstructionSet(name, instructionSize)
        ctx.sendFeedback("Created new instruction set '$name' with an instruction size of $instructionSize bits")
    }

    private fun isaDeleteCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val name = getString(ctx, "isa-name")
        redmon.deleteInstructionSet(name)
        ctx.sendFeedback("Deleted instruction set '$name'")
    }

    private fun isaRenameCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        val name = getString(ctx, "isa-name")
        val newName = getString(ctx, "new-isa-name")
        redmon.renameInstructionSet(name, newName)
        ctx.sendFeedback("Renamed instruction set '$name' to '$newName'")
    }

    private fun instructionAddCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        TODO("Not yet implemented")
    }

    private fun instructionRemoveCommand(ctx: CommandContext<FabricClientCommandSource>) = commandWrapper(ctx) {
        TODO("Not yet implemented")
    }

    companion object {
        private const val COMMAND_ERROR = -1
        private const val COMMAND_SUCCESS = 1

        private const val MAX_COMMAND_SUGGESTIONS = 10

        private fun str(
            name: String,
            suggestionProvider: (String) -> List<String> = { emptyList() }
        ): RequiredArgumentBuilder<FabricClientCommandSource, String> {
            return argument(name, string()).suggests { ctx, builder ->
                val partialArg = try {
                    getString(ctx, name)
                } catch (_: Exception) {
                    ""
                }
                for (suggestion in suggestionProvider(partialArg)) {
                    builder.suggest(suggestion)
                }
                builder.buildFuture()
            }
        }

        private fun greedyStr(name: String): RequiredArgumentBuilder<FabricClientCommandSource, String> {
            return argument(name, greedyString())
        }

        private fun int(
            name: String,
            min: Int = Int.MIN_VALUE,
            max: Int = Int.MAX_VALUE,
            suggestionProvider: (String) -> List<String> = { emptyList() }
        ): RequiredArgumentBuilder<FabricClientCommandSource, Int> {
            return argument(name, integer(min, max)).suggests { ctx, builder ->
                val partialArg = try {
                    getString(ctx, name)
                } catch (_: Exception) {
                    ""
                }
                for (suggestion in suggestionProvider(partialArg)) {
                    builder.suggest(suggestion)
                }
                builder.buildFuture()
            }
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
