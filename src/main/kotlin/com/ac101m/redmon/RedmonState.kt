package com.ac101m.redmon

import com.ac101m.redmon.gui.ProfileOverlay
import com.ac101m.redmon.gui.TextOverlay
import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.profile.ProfileRegistry
import com.ac101m.redmon.profile.Register
import com.ac101m.redmon.profile.RegisterFormat
import com.ac101m.redmon.profile.RegisterType
import com.ac101m.redmon.utils.ActiveProfileInfo
import com.ac101m.redmon.utils.Config.Companion.OVERLAY_POSITION
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
    private val profileStorageManager = StorageManager(profileStoragePath)
    private var profileRegistry = ProfileRegistry(profileStorageManager.loadProfiles())

    // Internal variables
    private var show = true
    private var activeProfileInfo: ActiveProfileInfo? = null

    // GUI elements
    private val profileUI = ProfileOverlay()
    private val inactiveUI = TextOverlay("No active profiles")

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
     * Add a register to the active profile.
     *
     * @param name Name for the new register.
     * @param type The type of the register.
     * @param inverted Whether the register should be inverting or not.
     */
    fun addRegister(
        name: String,
        type: RegisterType,
        inverted: Boolean,
        format: RegisterFormat,
        bitLocations: List<BlockPos>
    ) {
        val profileInfo = requireActiveProfile {
            "Cannot add register, no profile is selected"
        }

        val watchPoints = bitLocations.map { it.subtract(profileInfo.offset) }
        val newRegister = Register(name, type, inverted, format, watchPoints)

        profileInfo.profile.addRegister(newRegister)
        saveProfiles()
    }

    /**
     * Rename a register in the active profile.
     *
     * @param name The name of the register to rename.
     * @param newName The new name for the register.
     */
    fun renameRegister(name: String, newName: String) {
        val profileInfo = requireActiveProfile {
            "Cannot rename register, no profile is selected"
        }
        profileInfo.profile.renameRegister(name, newName)
        saveProfiles()
    }

    /**
     * Delete a register from the active profile.
     *
     * @param name The name of the register to delete.
     */
    fun deleteRegister(name: String) {
        val profileInfo = requireActiveProfile {
            "Cannot delete register, no profile is selected"
        }
        profileInfo.profile.deleteRegister(name)
        saveProfiles()
    }

    /**
     * Invert a register in the active profile.
     * Also returns the new inversion state of the register.
     *
     * @param name The name of the register to invert.
     * @return The new inversion state of the register.
     */
    fun invertRegister(name: String): Boolean {
        val profileInfo = requireActiveProfile {
            "Cannot invert register, no profile is selected"
        }
        val register = profileInfo.profile.getRegister(name)
        register.invert()
        saveProfiles()
        return register.invert
    }

    /**
     * Flip bits of a register in the active profile.
     * By flip, we mean MSB becomes LSB and vice versa.
     *
     * @param name The name of the register to flip bits in.
     */
    fun flipRegisterBits(name: String) {
        val profileInfo = requireActiveProfile {
            "Cannot flip register bits, no profile is selected"
        }
        val register = profileInfo.profile.getRegister(name)
        register.flipBits()
        saveProfiles()
    }

    /**
     * Get register type for a register in the active profile.
     *
     * @param name The name of the register to get the type for.
     */
    fun getRegisterType(name: String): RegisterType {
        val profileInfo = requireActiveProfile {
            "Cannot get register type, no profile is selected"
        }
        val register = profileInfo.profile.getRegister(name)
        return register.type
    }

    /**
     * Append bits to an existing register.
     *
     * @param name The name of the register to which bits should be appended.
     * @param bitPositions The bit locations to add to the register.
     */
    fun appendBitsToRegister(name: String, bitPositions: List<BlockPos>) {
        val profileInfo = requireActiveProfile {
            "Cannot append bits to register, no profile is selected"
        }

        val watchPoints = bitPositions.map { it.subtract(profileInfo.offset) }
        val register = profileInfo.profile.getRegister(name)

        register.appendBits(watchPoints)
        saveProfiles()
    }

    /**
     * Set the format of a single register in the active profile.
     *
     * @param name The name of the register to change the format for.
     * @param format The new format for the specified register.
     */
    fun setRegisterFormat(name: String, format: RegisterFormat) {
        val profileInfo = requireActiveProfile {
            "Cannot set register format, no profile is selected"
        }
        val register = profileInfo.profile.getRegister(name)
        register.format = format
        saveProfiles()
    }

    /**
     * Set the format of all registers in the active profile.
     *
     * @param format The name of the register to change the format for.
     */
    fun setAllRegisterFormats(format: RegisterFormat) {
        val profileInfo = requireActiveProfile {
            "Cannot set register formats, no profile is selected"
        }
        profileInfo.profile.registers.forEach { it.format = format }
        saveProfiles()
    }

    /**
     * Move a register up or down within a profile.
     * Returns true if the register moved, false if it's already at the top.
     *
     * @param name The name of the register to move.
     * @param n The number of spaces to move the register. Negative for up, positive for down.
     */
    fun moveRegister(name: String, n: Int): Int {
        val profileInfo = requireActiveProfile {
            "Cannot set register formats, no profile is selected"
        }
        val n = profileInfo.profile.moveRegister(name, n)
        saveProfiles()
        return n
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
        profile.updateState(world, profileInfo.offset)

        profileUI.update(profile, profileInfo.offset)
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
        return requireNotNull(activeProfileInfo, lazyMessage)
    }
}
