package com.ac101m.redmon

import com.ac101m.redmon.gui.ProfileOverlay
import com.ac101m.redmon.gui.TextOverlay
import com.ac101m.redmon.persistence.PersistentProfileList
import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.profile.ProfileList
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.util.math.Vec3i
import java.nio.file.Path
import kotlin.io.path.exists


class RedmonState(private val profileSavePath: Path) {
    lateinit var profiles: ProfileList

    var activeProfile: Profile? = null
    var profileOffset: Vec3i? = null

    val profileUI = ProfileOverlay()
    val inactiveUI = TextOverlay("No active profiles")


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


    fun saveProfiles() {
        profiles.toPersistent().save(profileSavePath)
    }


    fun setActiveProfile(player: ClientPlayerEntity, profileName: String) {
        activeProfile = profiles.getProfile(profileName)
        profileOffset = player.blockPos
    }


    fun clearActiveProfile() {
        activeProfile = null
        profileOffset = null
    }
}
