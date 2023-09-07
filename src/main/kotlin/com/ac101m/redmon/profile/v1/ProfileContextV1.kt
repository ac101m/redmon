package com.ac101m.redmon.profile.v1

import com.ac101m.redmon.profile.ProfileContext
import com.fasterxml.jackson.annotation.JsonProperty


data class ProfileContextV1(
    @JsonProperty("profiles", required = true)
    val profiles: HashMap<String, ProfileDataV1> = HashMap()
) : ProfileContext() {
    fun addProfile(profile: ProfileDataV1) {
        profiles[profile.name] = profile
    }
}
