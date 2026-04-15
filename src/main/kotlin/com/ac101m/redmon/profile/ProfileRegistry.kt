package com.ac101m.redmon.profile

class ProfileRegistry(initProfiles: List<Profile>) {
    private val profileIndex = HashMap<String, Profile>()

    val profiles get() = profileIndex.values.toList()

    init {
        initProfiles.forEach { profile ->
            profileIndex[profile.name] = profile
        }
    }

    private fun requireProfileExists(name: String): Profile {
        return requireNotNull(profileIndex[name]) {
            "No profile with name '$name'"
        }
    }

    private fun requireProfileDoesNotExist(name: String) {
        require(!profileIndex.containsKey(name)) {
            "A profile with name '$name' already exists"
        }
    }

    fun getProfile(name: String): Profile {
        requireProfileExists(name)
        return profileIndex[name]!!
    }

    fun addProfile(profile: Profile) {
        requireProfileDoesNotExist(profile.name)
        profileIndex[profile.name] = profile
    }

    fun renameProfile(name: String, newName: String) {
        requireProfileExists(name)
        requireProfileDoesNotExist(newName)
        val profile = profileIndex.remove(name)
        profile!!.name = newName
        profileIndex[profile.name] = profile
    }

    fun deleteProfile(name: String) {
        requireProfileExists(name)
        profileIndex.remove(name)
    }
}
