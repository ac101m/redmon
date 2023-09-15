package com.ac101m.redmon.profile

import com.ac101m.redmon.persistence.v1.PersistentProfileV1
import com.ac101m.redmon.persistence.v1.PersistentProfileListV1


data class ProfileList(
    private val profileMap: HashMap<String, Profile> = HashMap()
) {
    val size get() = profileMap.size
    val names get() = profileMap.keys

    companion object {
        fun fromPersistent(data: PersistentProfileListV1): ProfileList {
            val profiles = HashMap<String, Profile>()
            data.profiles.forEach { profile -> profiles[profile.name] = Profile.fromPersistent(profile) }
            return ProfileList(profiles)
        }
    }


    fun toPersistent(): PersistentProfileListV1 {
        val persistentProfiles = arrayListOf<PersistentProfileV1>()
        profileMap.keys.forEach { profileName -> persistentProfiles.add(profileMap[profileName]!!.toPersistent()) }
        return PersistentProfileListV1(persistentProfiles)
    }


    fun requireProfileExists(name: String) {
        require(profileMap.containsKey(name)) {
            "No profile with name '$name'"
        }
    }


    fun requireProfileDoesNotExist(name: String) {
        require(!profileMap.containsKey(name)) {
            "A profile with name '$name' already exists"
        }
    }


    fun getProfile(name: String): Profile {
        requireProfileExists(name)
        return profileMap[name]!!
    }


    fun addProfile(profile: Profile) {
        requireProfileDoesNotExist(profile.name)
        profileMap[profile.name] = profile
    }


    fun removeProfile(name: String) {
        requireProfileExists(name)
        profileMap.remove(name)
    }
}
