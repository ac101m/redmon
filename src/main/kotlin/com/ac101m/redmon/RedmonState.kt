package com.ac101m.redmon

import com.ac101m.redmon.gui.ProfileOverlay
import com.ac101m.redmon.gui.TextOverlay
import com.ac101m.redmon.persistence.ProfileListReader
import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.profile.ProfileRegistry
import com.ac101m.redmon.profile.Signal
import com.ac101m.redmon.profile.SignalFormat
import com.ac101m.redmon.profile.SignalType
import com.ac101m.redmon.utils.ActiveProfileInfo
import com.ac101m.redmon.utils.Config.Companion.OVERLAY_POSITION
import com.ac101m.redmon.utils.NoActiveProfileException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3
import java.nio.file.Path

/**
 * Contains all state for redmon and implements mod logic.
 *
 * @param profileStoragePath The path to the profile storage location.
 */
class RedmonState(profileStoragePath: Path) {
    private val mapper = ObjectMapper().registerKotlinModule()
    private val profileListReader = ProfileListReader(mapper)
    private val profileStorageManager = StorageManager(profileStoragePath, mapper, profileListReader)
    private var profileRegistry = ProfileRegistry(profileStorageManager.loadProfiles())

    // Internal variables
    private var show = true
    private var activeProfileInfo: ActiveProfileInfo? = null

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
        val profileInfo = activeProfileInfo
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
     */
    fun addSignal(
        name: String,
        type: SignalType,
        inverted: Boolean,
        format: SignalFormat,
        blockLocations: List<BlockPos>
    ) {
        val profileInfo = requireActiveProfile {
            "Cannot add signal, no profile is selected"
        }

        val blockLocationsRelative = blockLocations.map { it.subtract(profileInfo.offset) }
        val newSignal = Signal(name, type, inverted, format, blockLocationsRelative)

        profileInfo.profile.getCurrentPage().addSignal(newSignal)
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
     * Delete a signal from the active profile.
     *
     * @param name The name of the signal to delete.
     */
    fun deleteSignal(name: String) {
        val profileInfo = requireActiveProfile {
            "Cannot delete signal, no profile is selected"
        }
        profileInfo.profile.getCurrentPage().deleteSignal(name)
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
        profileInfo.profile.getCurrentPage().signals.forEach { it.format = format }
        saveProfiles()
    }

    /**
     * Move a signal up or down within a profile.
     * Returns the number of places the signal moved. Negative for up, positive for down.
     *
     * @param name The name of the signal to move.
     * @param n The number of spaces to move the signal. Negative for up, positive for down.
     */
    fun moveSignal(name: String, n: Int): Int {
        val profileInfo = requireActiveProfile {
            "Cannot set signal formats, no profile is selected"
        }
        val n = profileInfo.profile.getCurrentPage().moveSignal(name, n)
        saveProfiles()
        return n
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
     */
    fun addPageToActiveProfile(name: String) {
        val profileInfo = requireActiveProfile {
            "Cannot add new page, no profile is selected"
        }
        profileInfo.profile.addPage(name)
        saveProfiles()
    }

    /**
     * Rename the current page.
     */
    fun renameCurrentPage(newName: String) {
        val profileInfo = requireActiveProfile {
            "Cannot rename page, no profile is selected"
        }
        profileInfo.profile.getCurrentPage().name = newName
        saveProfiles()
    }

    /**
     * Draw the redmon overlay.
     *
     * @param context The GUI rendering context to use for drawing.
     */
    fun drawOverlay(context: GuiGraphics) {
        if (!show) return

        val profileInfo = activeProfileInfo

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
    fun setActiveProfile(profileName: String, offset: Vec3) {
        activeProfileInfo = ActiveProfileInfo(
            profileRegistry.getProfile(profileName),
            Vec3i(offset.x.toInt(), offset.y.toInt(), offset.z.toInt())
        )
    }

    /**
     * Return true if there is an active profile. False otherwise.
     */
    fun hasActiveProfile(): Boolean {
        return activeProfileInfo != null
    }

    /**
     * Disable the currently active profile.
     */
    fun clearActiveProfile() {
        activeProfileInfo = null
    }

    private fun saveProfiles() {
        profileStorageManager.saveProfiles(profileRegistry.profiles)
    }

    private fun requireActiveProfile(lazyMessage: () -> String): ActiveProfileInfo {
        return activeProfileInfo ?: throw NoActiveProfileException(lazyMessage())
    }
}
