package com.ac101m.redmon

import com.ac101m.redmon.gui.ProfileOverlay
import com.ac101m.redmon.gui.TextOverlay
import com.ac101m.redmon.persistence.PersistentProfileList
import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.profile.ProfileList
import com.ac101m.redmon.utils.Config.Companion.OVERLAY_POSITION
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.core.Vec3i
import java.nio.file.Path
import kotlin.io.path.exists

/**
 * Contains all state for redmon and implements mod logic.
 */
class RedmonState(private val profileSavePath: Path) {
    private lateinit var profiles: ProfileList

    // Internal variables
    private var show = true
    var activeProfile: Profile? = null
    var profileOffset: Vec3i? = null

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
     * Get the names of all currently loaded profiles.
     */
    fun getAllProfiles(): List<Profile> {
        return profiles.profiles
    }

    /**
     * Add a new profile.
     *
     * @param profile The profile to add.
     */
    fun addProfile(profile: Profile) {
        profiles.addProfile(profile)
        saveProfiles()
    }

    /**
     * Rename a profile.
     *
     * @param name The name of the profile to rename.
     * @param newName The new name for the profile.
     */
    fun renameProfile(name: String, newName: String) {
        profiles.renameProfile(name, newName)
        saveProfiles()
    }

    /**
     * Rename a register.
     *
     * @param name The name of the register to rename.
     * @param newName The new name for the register.
     */
    fun renameRegister(name: String, newName: String) {
        val profile = requireNotNull(activeProfile) {
            "No profile selected."
        }
        profile.renameRegister(name, newName)
        saveProfiles()
    }

    /**
     * Delete a profile.
     *
     * @param profileName The name of the profile to delete.
     */
    fun deleteProfile(profileName: String) {
        if (activeProfile?.name == profileName) {
            clearActiveProfile()
        }
        profiles.deleteProfile(profileName)
        saveProfiles()
    }

    /**
     * Draw the redmon overlay.
     *
     * @param context The GUI rendering context to use for drawing.
     */
    fun drawOverlay(context: GuiGraphics) {
        if (!show) return

        val profile = if (activeProfile == null) {
            inactiveUI.draw(context, OVERLAY_POSITION)
            return
        } else {
            activeProfile!!
        }

        val world = Minecraft.getInstance().player?.level() ?: return
        profile.updateState(world, profileOffset!!)

        profileUI.update(profile, profileOffset!!)
        profileUI.draw(context, OVERLAY_POSITION)
    }

    /**
     * Load all profiles from disk.
     */
    fun loadProfiles() {
        profiles = if (profileSavePath.exists()) {
            val data = PersistentProfileList.load(profileSavePath)
            ProfileList.fromPersistent(data)
        } else {
            ProfileList().also {
                it.toPersistent().save(profileSavePath)
            }
        }
    }

    /**
     * Save profiles to disk.
     */
    fun saveProfiles() {
        profiles.toPersistent().save(profileSavePath)
    }

    /**
     * Set the active profile for a given player.
     *
     * @param player The player to set the profile for.
     * @param profileName The name of the profile to activate.
     */
    fun setActiveProfile(player: AbstractClientPlayer, profileName: String) {
        activeProfile = profiles.getProfile(profileName)
        val p = player.position()
        profileOffset = Vec3i(p.x.toInt(), p.y.toInt(), p.z.toInt())
    }

    /**
     * Disable the currently active profile.
     */
    fun clearActiveProfile() {
        activeProfile = null
        profileOffset = null
    }
}
