package com.ac101m.redmon

import com.ac101m.redmon.gui.ProfileOverlay
import com.ac101m.redmon.gui.TextOverlay
import com.ac101m.redmon.isa.InstructionLayout
import com.ac101m.redmon.isa.InstructionSet
import com.ac101m.redmon.isa.InstructionSetRegistry
import com.ac101m.redmon.persistence.StorageReader
import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.profile.ProfileRegistry
import com.ac101m.redmon.profile.Signal
import com.ac101m.redmon.profile.SignalFormat
import com.ac101m.redmon.profile.SignalType
import com.ac101m.redmon.world.ActiveProfileInfo
import com.ac101m.redmon.utils.Config.Companion.OVERLAY_POSITION
import com.ac101m.redmon.utils.NoActiveProfileException
import com.ac101m.redmon.world.WorldMetadata
import com.ac101m.redmon.world.WorldRegistry
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import java.nio.file.Path

/**
 * Contains all state for redmon and implements mod logic.
 *
 * @param profileStoragePath Path to the profile storage location.
 * @param worldMetadataStoragePath Path to the world metadata storage location.
 * @param instructionSetStoragePath Path to the instruction set storage location.
 */
class RedmonState(profileStoragePath: Path, worldMetadataStoragePath: Path, instructionSetStoragePath: Path) {
    private val mapper = ObjectMapper().registerKotlinModule()
    private val storageReader = StorageReader(mapper)
    private val storageManager = StorageManager(
        profileStoragePath,
        worldMetadataStoragePath,
        instructionSetStoragePath,
        mapper,
        storageReader
    )
    private val profileRegistry: ProfileRegistry
    private val worldRegistry: WorldRegistry
    private val instructionSetRegistry: InstructionSetRegistry

    init {
        val profiles = storageManager.loadProfiles()
        val instructionSets = storageManager.loadInstructionSets()

        instructionSetRegistry = InstructionSetRegistry(instructionSets)
        profileRegistry = ProfileRegistry(profiles)

        val worldMetadata = storageManager.loadWorldMetadata(profileRegistry)

        worldRegistry = WorldRegistry(worldMetadata)
    }

    // Internal variables
    private var show = true
    private var currentWorld: WorldMetadata? = null

    // GUI elements
    private val profileUI = ProfileOverlay()
    private val inactiveUI = TextOverlay("Redmon: No active profile")

    /**
     * Hide the redmon UI/overlay.
     */
    fun show() {
        show = true
    }

    /**
     * Hide the redmon UI/overlay.
     */
    fun hide() {
        show = false
    }

    /**
     * Toggle visibility.
     */
    fun toggleVisibility() {
        show = !show
    }

    /**
     * Get profile names.
     */
    fun getProfileNames(): List<String> {
        return profileRegistry.profiles.map { it.name }
    }

    /**
     * Get instruction set names as a list.
     */
    fun getInstructionSetNames(): List<String> {
        return instructionSetRegistry.instructionSets.map { it.name }
    }

    /**
     * Get list of visible signal names.
     */
    fun getVisibleSignalNames(): List<String> {
        return currentWorld?.activeProfile?.profile?.getCurrentPage()?.let { activePage ->
            activePage.signalMap.keys.map { it }
        } ?: emptyList()
    }

    /**
     * Get the names of pages in the current profile.
     */
    fun getCurrentProfilePageNames(): List<String> {
        return currentWorld?.activeProfile?.profile?.let { activeProfile ->
            activeProfile.pages.map { it.name }
        } ?: emptyList()
    }

    /**
     * Get the number of columns in the currently active page.
     * Returns null if no profile is active.
     */
    fun getVisibleColumnCount(): Int? {
        return currentWorld?.activeProfile?.profile?.getCurrentPage()?.columns?.size
    }

    /**
     * Add a new profile.
     *
     * @param profile The profile to add.
     */
    fun addProfile(profile: Profile) {
        profileRegistry.addProfile(profile)
        saveProfiles()
    }

    /**
     * Rename a profile.
     *
     * @param name The name of the profile to rename.
     * @param newName The new name for the profile.
     */
    fun renameProfile(name: String, newName: String) {
        profileRegistry.renameProfile(name, newName)
        saveProfiles()
    }

    /**
     * Delete a profile.
     *
     * @param profileName The name of the profile to delete.
     */
    fun deleteProfile(profileName: String) {
        val profileInfo = currentWorld?.activeProfile
        if (profileInfo != null && profileInfo.profile.name == profileName) {
            clearActiveProfile()
        }
        profileRegistry.deleteProfile(profileName)
        saveProfiles()
    }

    /**
     * Add a signal to the active profile.
     *
     * @param name Name for the new signal.
     * @param type The type of the signal.
     * @param inverted Whether the signal should be inverting or not.
     * @param format The format to display the new signal in.
     * @param blockLocations Absolute positions of blocks to include in the signal.
     * @param columnIndex The index of the column to which the signal should be added.
     */
    fun addSignal(
        name: String,
        type: SignalType,
        inverted: Boolean,
        format: SignalFormat,
        blockLocations: List<BlockPos>,
        columnIndex: Int
    ) {
        val profileInfo = requireActiveProfile {
            "Cannot add signal, no profile is selected"
        }

        val blockLocationsRelative = blockLocations.map { it.subtract(profileInfo.offset) }
        val newSignal = Signal(name, type, inverted, format, blockLocationsRelative)

        profileInfo.profile.getCurrentPage().addSignal(newSignal, columnIndex)
        saveProfiles()
    }

    /**
     * Rename a signal in the active profile.
     *
     * @param name The name of the signal to rename.
     * @param newName The new name for the signal.
     */
    fun renameSignal(name: String, newName: String) {
        val profileInfo = requireActiveProfile {
            "Cannot rename signal, no profile is selected"
        }
        profileInfo.profile.getCurrentPage().renameSignal(name, newName)
        saveProfiles()
    }

    /**
     * Remove a signal from the active profile.
     *
     * @param name The name of the signal to delete.
     */
    fun removeSignal(name: String) {
        val profileInfo = requireActiveProfile {
            "Cannot delete signal, no profile is selected"
        }
        profileInfo.profile.getCurrentPage().removeSignal(name)
        saveProfiles()
    }

    /**
     * Invert a signal in the active profile.
     * Also returns the new inversion state of the signal.
     *
     * @param name The name of the signal to invert.
     * @return The new inversion state of the signal.
     */
    fun invertSignal(name: String): Boolean {
        val profileInfo = requireActiveProfile {
            "Cannot invert signal, no profile is selected"
        }
        val signal = profileInfo.profile.getCurrentPage().getSignal(name)
        signal.invert()
        saveProfiles()
        return signal.invert
    }

    /**
     * Flip bits of a signal in the active profile.
     * By flip, we mean MSB becomes LSB and vice versa.
     *
     * @param name The name of the signal to flip bits in.
     */
    fun flipSignalBits(name: String) {
        val profileInfo = requireActiveProfile {
            "Cannot flip signal bits, no profile is selected"
        }
        val signal = profileInfo.profile.getCurrentPage().getSignal(name)
        signal.flipBits()
        saveProfiles()
    }

    /**
     * Get signal type for a signal in the active profile.
     *
     * @param name The name of the signal to get the type for.
     */
    fun getSignalType(name: String): SignalType {
        val profileInfo = requireActiveProfile {
            "Cannot get signal type, no profile is selected"
        }
        val signal = profileInfo.profile.getCurrentPage().getSignal(name)
        return signal.type
    }

    /**
     * Append bits to an existing signal.
     *
     * @param name The name of the signal to which bits should be appended.
     * @param blockLocations Absolute positions of blocks to add to the signal.
     */
    fun appendBlocksToSignal(name: String, blockLocations: List<BlockPos>) {
        val profileInfo = requireActiveProfile {
            "Cannot append bits to signal, no profile is selected"
        }

        val relativeBlockLocations = blockLocations.map { it.subtract(profileInfo.offset) }
        val signal = profileInfo.profile.getCurrentPage().getSignal(name)

        for (location in relativeBlockLocations) {
            require(!signal.blocks.contains(location)) {
                "Unable to append blocks. One or more blocks are already part of the signal."
            }
        }

        signal.appendBlocks(relativeBlockLocations)
        saveProfiles()
    }

    /**
     * Set the format of a single signal in the active profile.
     *
     * @param name The name of the signal to change the format for.
     * @param format The new format for the specified signal.
     */
    fun setSignalFormat(name: String, format: SignalFormat) {
        val profileInfo = requireActiveProfile {
            "Cannot set signal format, no profile is selected"
        }
        val signal = profileInfo.profile.getCurrentPage().getSignal(name)
        signal.format = format
        saveProfiles()
    }

    /**
     * Set the format of all signals in the active profile.
     *
     * @param format The name of the signal to change the format for.
     */
    fun setAllSignalFormats(format: SignalFormat) {
        val profileInfo = requireActiveProfile {
            "Cannot set signal formats, no profile is selected"
        }
        profileInfo.profile.getCurrentPage().signalMap.values.forEach { it.signal.format = format }
        saveProfiles()
    }

    /**
     * Move a signal up or down within a profile page.
     * Returns the number of places the signal moved. Negative for up, positive for down.
     *
     * @param name The name of the signal to move.
     * @param n The number of spaces to move the signal. Negative for up, positive for down.
     */
    fun moveSignalVertically(name: String, n: Int): Int {
        val profileInfo = requireActiveProfile {
            "Cannot set signal formats, no profile is selected"
        }
        val n = profileInfo.profile.getCurrentPage().moveSignalVertically(name, n)
        saveProfiles()
        return n
    }

    /**
     * Move a signal to a different column within a profile page.
     *
     * @param name The name of the signal to move.
     * @param columnIndex The index of the column to move the signal to.
     */
    fun changeSignalColumn(name: String, columnIndex: Int) {
        val profileInfo = requireActiveProfile {
            "Cannot change signal column, no profile is selected"
        }
        profileInfo.profile.getCurrentPage().changeSignalColumn(name, columnIndex)
        saveProfiles()
    }

    /**
     * Switch to the next page in the current profile.
     */
    fun nextPage() {
        val profileInfo = requireActiveProfile {
            "Cannot switch to next page, no profile is selected"
        }
        profileInfo.profile.nextPage()
    }

    /**
     * Switch to the previous page in the current profile.
     */
    fun previousPage() {
        val profileInfo = requireActiveProfile {
            "Cannot go to previous page, no profile is selected"
        }
        profileInfo.profile.previousPage()
    }

    /**
     * Add a new page to the active profile.
     *
     * @param name The name of the page to create and add to the active profile.
     */
    fun addPageToActiveProfile(name: String) {
        val profileInfo = requireActiveProfile {
            "Cannot add new page, no profile is selected"
        }
        profileInfo.profile.addPage(name)
        saveProfiles()
    }

    /**
     * Remove a page from the active profile.
     *
     * @param name The name of the page to remove from the active profile.
     */
    fun removePageFromActiveProfile(name: String) {
        val profileInfo = requireActiveProfile {
            "Cannot remove page, no profile is selected"
        }
        profileInfo.profile.removePage(name)
        saveProfiles()
    }

    /**
     * Rename the current page.
     *
     * @param newName The new name of the page.
     */
    fun renameCurrentPage(newName: String) {
        val profileInfo = requireActiveProfile {
            "Cannot rename page, no profile is selected"
        }
        profileInfo.profile.getCurrentPage().name = newName
        saveProfiles()
    }

    /**
     * Create a new instruction set.
     *
     * @param name The name of the new instruction set to create.
     * @param instructionSize The size of instructions in the instruction set in bits.
     */
    fun createInstructionSet(name: String, instructionSize: Int) {
        val newInstructionSet = InstructionSet(name, instructionSize)
        instructionSetRegistry.addInstructionSet(newInstructionSet)
        saveInstructionSets()
    }

    /**
     * Delete an instruction set.
     *
     * @param name Name of the instruction set to delete.
     */
    fun deleteInstructionSet(name: String) {
        instructionSetRegistry.removeInstructionSet(name)
        saveInstructionSets()
    }

    /**
     * Rename an instruction set.
     *
     * @param name The name of the instruction set to rename.
     * @param newName The name to rename the instruction set to.
     */
    fun renameInstructionSet(name: String, newName: String) {
        instructionSetRegistry.renameInstructionSet(name, newName)
        saveInstructionSets()
    }

    /**
     * Add an instruction to an instruction set.
     *
     * @param instructionSetName The name of the instruction set.
     * @param instruction The instruction to add to the instruction set.
     */
    fun addInstruction(instructionSetName: String, instruction: InstructionLayout) {
        val instructionSet = instructionSetRegistry.getInstructionSet(instructionSetName)
        instructionSet.addInstruction(instruction)
        saveInstructionSets()
    }

    /**
     * Remove an instruction from an instruction set.
     *
     * @param instructionSetName The name of the instruction set to remove the instruction from.
     * @param instructionName The name of the instruction to remove.
     */
    fun removeInstruction(instructionSetName: String, instructionName: String) {
        val instructionSet = instructionSetRegistry.getInstructionSet(instructionSetName)
        instructionSet.removeInstruction(instructionName)
        saveInstructionSets()
    }

    /**
     * Remove an instruction from an instruction set.
     *
     * @param instructionSetName The name of the instruction set to remove the instruction from.
     * @param instructionName The name of the instruction to remove.
     * @param newInstructionName The new instruction name.
     */
    fun renameInstruction(instructionSetName: String, instructionName: String, newInstructionName: String) {
        val instructionSet = instructionSetRegistry.getInstructionSet(instructionSetName)
        instructionSet.renameInstruction(instructionName, newInstructionName)
        saveInstructionSets()
    }

    /**
     * Get a list of all instruction names within an instruction set.
     *
     * @param instructionSetName The name of the instruction set to get instruction names from.
     */
    fun getInstructionNames(instructionSetName: String): List<String> {
        val instructionSet = instructionSetRegistry.getInstructionSet(instructionSetName)
        return instructionSet.instructions.map { it.name }
    }

    /**
     * Get instruction summaries for all instructions in an instruction set.
     *
     * @param instructionSetName The name of the instruction set to get instruction names from.
     */
    fun getInstructionSummaries(instructionSetName: String): List<String> {
        val instructionSet = instructionSetRegistry.getInstructionSet(instructionSetName)
        return instructionSet.instructions.map { instruction ->
            "${instruction.prettyPrint()} - ${instruction.name}\n     ${instruction.descriptionText()}"
        }
    }

    /**
     * Get details instruction information, including breakdown of fields.
     *
     * @param instructionSetName The name of the instruction set containing the instruction.
     * @param instructionName The name of the instruction to get info for.
     */
    fun getInstructionInfo(instructionSetName: String, instructionName: String): String {
        val instructionSet = instructionSetRegistry.getInstructionSet(instructionSetName)
        val instruction = instructionSet.getInstruction(instructionName)
        return instruction.infoString()
    }

    /**
     * Set the description of an instruction.
     *
     * @param instructionSetName The name of the instruction set containing the instruction.
     * @param instructionName The name of the instruction to change the description of.
     * @param newDescription The description text to set for the new instruction.
     */
    fun setInstructionDescription(instructionSetName: String, instructionName: String, newDescription: String) {
        val instructionSet = instructionSetRegistry.getInstructionSet(instructionSetName)
        val instruction = instructionSet.getInstruction(instructionName)
        instruction.description = newDescription
        saveInstructionSets()
    }

    /**
     * Set instruction field description.
     *
     * @param instructionSetName The name of the instruction set containing the instruction.
     * @param instructionName The name of the instruction to change the description of.
     * @param fieldIndex The index of the field to rename.
     * @param newDescription The description text to set for the new instruction.
     */
    fun setInstructionFieldDescription(
        instructionSetName: String,
        instructionName: String,
        fieldIndex: Int,
        newDescription: String,
    ) {
        val instructionSet = instructionSetRegistry.getInstructionSet(instructionSetName)
        val instruction = instructionSet.getInstruction(instructionName)
        instruction.setFieldDescription(fieldIndex, newDescription)
        saveInstructionSets()
    }

    /**
     * Draw the redmon overlay.
     *
     * @param context The GUI rendering context to use for drawing.
     */
    fun drawOverlay(context: GuiGraphics) {
        if (!show) return

        val profileInfo = currentWorld?.activeProfile

        val profile = if (profileInfo == null) {
            inactiveUI.draw(context, OVERLAY_POSITION)
            return
        } else {
            profileInfo.profile
        }

        val world = Minecraft.getInstance().player?.level() ?: return
        profile.getCurrentPage().updateState(world, profileInfo.offset)

        profileUI.update(profile)
        profileUI.draw(context, OVERLAY_POSITION)
    }

    /**
     * Set the active profile for a given player.
     *
     * @param profileName The name of the profile to activate.
     * @param offset The offset to enable the profile at.
     */
    fun setActiveProfile(profileName: String, offset: Vec3i) {
        currentWorld?.let {
            it.activeProfile = ActiveProfileInfo(
                profileRegistry.getProfile(profileName),
                offset
            )
        }
        saveWorldMetadata()
    }

    /**
     * Return true if there is an active profile. False otherwise.
     */
    fun hasActiveProfile(): Boolean {
        return currentWorld?.activeProfile != null
    }

    /**
     * Disable the currently active profile.
     */
    fun clearActiveProfile() {
        currentWorld?.activeProfile = null
        saveWorldMetadata()
    }

    /**
     * Handle a world switch event.
     * Selects up the last selected profile of the current world (if any).
     */
    fun updateCurrentWorld() {
        currentWorld = worldRegistry.getMetadataForCurrentWorld()
    }

    private fun saveProfiles() {
        storageManager.saveProfiles(profileRegistry.profiles)
    }

    private fun saveWorldMetadata() {
        storageManager.saveWorldMetadata(worldRegistry.worldMetadata)
    }

    private fun saveInstructionSets() {
        storageManager.saveInstructionSets(instructionSetRegistry.instructionSets)
    }

    private fun requireActiveProfile(lazyMessage: () -> String): ActiveProfileInfo {
        return currentWorld?.activeProfile ?: throw NoActiveProfileException(lazyMessage())
    }
}
