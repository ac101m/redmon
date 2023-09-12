package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v1.PersistentProfileV1
import com.ac101m.redmon.persistence.v1.PersistentPersistentProfileListV1


data class ProfileList(
    val profiles: HashMap<String, Profile> = HashMap(),
) {
    companion object {
        fun fromPersistent(data: PersistentPersistentProfileListV1): ProfileList {
            val profiles = HashMap<String, Profile>()
            data.profiles.forEach { profile -> profiles[profile.name] = Profile.fromPersistent(profile) }
            return ProfileList(profiles)
        }
    }

    fun toPersistent(): PersistentPersistentProfileListV1 {
        val persistentProfiles = arrayListOf<PersistentProfileV1>()
        profiles.keys.forEach { profileName -> persistentProfiles.add(profiles[profileName]!!.toPersistent()) }
        return PersistentPersistentProfileListV1(persistentProfiles)
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
