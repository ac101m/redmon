package com.ac101m.redmon.world

import com.ac101m.redmon.persistence.v2.PersistentActiveProfileInfoV2
import com.ac101m.redmon.persistence.v2.PersistentBlockV2
import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.profile.ProfileRegistry
import net.minecraft.core.Vec3i

/**
 * Container class which represents the currently active profile and current offset.
 *
 * @property profile The active profile.
 * @property offset The offset that the profile is active at.
 */
class ActiveProfileInfo(
    val profile: Profile,
    val offset: Vec3i
) {
    fun toPersistent(): PersistentActiveProfileInfoV2 {
        return PersistentActiveProfileInfoV2(
            profile.name,
            PersistentBlockV2(offset.x, offset.y, offset.z)
        )
    }

    companion object {
        fun fromPersistent(profileRegistry: ProfileRegistry, persistent: PersistentActiveProfileInfoV2): ActiveProfileInfo? {
            return profileRegistry.getProfileOrNull(persistent.name)?.let {
                val block = persistent.offset
                ActiveProfileInfo(it, Vec3i(block.x, block.y, block.z))
            }
        }
    }
}
