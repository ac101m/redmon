package com.ac101m.redmon

import com.ac101m.redmon.profile.ProfileRegistry
import com.ac101m.redmon.profile.v1.ProfileRegistryV1
import com.ac101m.redmon.utils.Config.Companion.PROFILE_REGISTRY_SAVE_PATH
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import java.nio.file.Path
import kotlin.io.path.exists


class RedmonRoot : ClientModInitializer {
    private lateinit var profileRegistry: ProfileRegistryV1

    fun loadProfileRegistry(path: Path) {
        if (path.exists()) {
            profileRegistry = ProfileRegistry.load(PROFILE_REGISTRY_SAVE_PATH)
        } else {
            profileRegistry = ProfileRegistryV1()
            profileRegistry.save(PROFILE_REGISTRY_SAVE_PATH)
        }
    }

    override fun onInitializeClient() {
        // Load the profile registry (if it exists)
        profileRegistry = try {
            ProfileRegistry.load(PROFILE_REGISTRY_SAVE_PATH)
        } catch (e: Exception) {
            println("Failed to load profile registry, ${e.message}")
            ProfileRegistryV1()
        }

        // Register rendering code with hud render callback
        HudRenderCallback.EVENT.register { matrixStack, _ ->
            MinecraftClient.getInstance().textRenderer.drawWithShadow(
                matrixStack, "test", 0f, 0f, 0xffffff
            )
        }
    }
}
