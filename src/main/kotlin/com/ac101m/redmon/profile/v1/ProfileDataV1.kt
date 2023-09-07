package com.ac101m.redmon.profile.v1

import com.ac101m.redmon.profile.ProfileData
import com.fasterxml.jackson.annotation.JsonProperty


data class ProfileDataV1(
    @JsonProperty("name", required = true)
    var name: String
) : ProfileData()
