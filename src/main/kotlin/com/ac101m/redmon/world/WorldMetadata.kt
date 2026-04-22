package com.ac101m.redmon.world

import com.ac101m.redmon.persistence.v2.PersistentWorldMetadataV2
import com.ac101m.redmon.profile.ProfileRegistry

class WorldMetadata(
    val worldKey: String,
    var activeProfile: ActiveProfileInfo?
) {
    fun isEmpty(): Boolean {
        return activeProfile == null
    }

    fun toPersistent(): PersistentWorldMetadataV2 {
        return PersistentWorldMetadataV2(worldKey, activeProfile?.toPersistent())
    }

    companion object {
        fun fromPersistent(profileRegistry: ProfileRegistry, persistent: PersistentWorldMetadataV2): WorldMetadata {
            return WorldMetadata(
                persistent.worldKey,
                persistent.activeProfile?.let {
                    ActiveProfileInfo.fromPersistent(profileRegistry, it)
                }
            )
        }
    }
}
