package com.ac101m.redmon.world

import net.minecraft.client.Minecraft
import net.minecraft.world.level.storage.LevelResource

/**
 * Class maintains per-world mod state.
 * This is what the mod uses to remember profile activations across worlds.
 *
 * @param initWorldMetadata Initial list of world info objects.
 */
class WorldRegistry(initWorldMetadata: List<WorldMetadata>) {
    private val worldMetadataIndex = HashMap<String, WorldMetadata>()

    val worldMetadata get() = worldMetadataIndex.values.toList()

    init {
        for (world in initWorldMetadata) {
            worldMetadataIndex[world.worldKey] = world
        }
    }

    /**
     * Get world metadata for the currently active world or create a new one and return it.
     * If the world key cannot be computed, return an empty world metadata object.
     * This will allow calling code to proceed normally (just without persistence).
     */
    fun getMetadataForCurrentWorld(): WorldMetadata {
        val worldKey = getUniqueIdForCurrentWorld() ?: return WorldMetadata("", null)
        return worldMetadataIndex.computeIfAbsent(worldKey) {
            WorldMetadata(worldKey, null)
        }
    }

    /**
     * Notify the world registry of profile deletion.
     *
     * @param profileName The name of the profile that was deleted.
     * @returns true if any changes occurred.
     */
    fun notifyProfileDeleted(profileName: String): Boolean {
        var changes = false

        for (world in worldMetadata) {
            world.activeProfile?.let { activeProfile ->
                if (activeProfile.profile.name == profileName) {
                    world.activeProfile = null
                    changes = true
                }
            }
        }

        return changes
    }

    companion object {
        /**
         * Calculates a (hopefully) unique ID for the current world, regardless of whether the player is on
         * singleplayer or not. Very hacky.
         * If the key can't be calculated, returns null.
         */
        private fun getUniqueIdForCurrentWorld(): String? {
            val instance = Minecraft.getInstance()

            val player = instance.player ?: return null
            val level = instance.level ?: return null

            val sb = StringBuilder().apply {
                if (instance.isSingleplayer) {
                    val singlePlayerServer = instance.singleplayerServer ?: return null
                    append("sp:")
                    append(player.name.string)
                    append(":")
                    append(singlePlayerServer.getWorldPath(LevelResource.ROOT).parent.fileName)
                    append(":")
                } else {
                    val server = instance.currentServer ?: return null
                    append("mp:")
                    append(player.name.string)
                    append(":")
                    append(server.ip)
                    append(":")
                }

                append(level.dimension().location().path)
            }

            return sb.toString()
        }
    }
}
