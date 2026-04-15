package com.ac101m.redmon.utils

import com.ac101m.redmon.profile.Profile
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
)
