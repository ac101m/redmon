package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v1.PersistentProfileV1
import com.ac101m.redmon.persistence.v1.PersistentProfileListV1


data class ProfileList(
    val profiles: HashMap<String, Profile> = HashMap(),
) {
    companion object {
        fun fromPersistent(data: PersistentProfileListV1): ProfileList {
            val profiles = HashMap<String, Profile>()
            data.profiles.forEach { profile -> profiles[profile.name] = Profile.fromPersistent(profile) }
            return ProfileList(profiles)
        }
    }

    fun toPersistent(): PersistentProfileListV1 {
        val persistentProfiles = arrayListOf<PersistentProfileV1>()
        profiles.keys.forEach { profileName -> persistentProfiles.add(profiles[profileName]!!.toPersistent()) }
        return PersistentProfileListV1(persistentProfiles)
    }

    val size get() = profiles.size

    fun addProfile(profile: Profile) {
        profiles[profile.name] = profile
    }

    fun getProfile(name: String): Profile? {
        return profiles[name]
    }

    fun removeProfile(name: String) {
        profiles.remove(name)
    }
}
