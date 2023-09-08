package com.ac101m.redmon

import com.ac101m.redmon.profile.SaveData
import com.ac101m.redmon.profile.v1.SaveDataV1
import com.ac101m.redmon.utils.Config.Companion.PROFILE_REGISTRY_SAVE_PATH
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import java.nio.file.Path
import kotlin.io.path.exists


class RedmonClient : ClientModInitializer {
    private lateinit var profileRegistry: SaveDataV1
    private var currentContext: String? = null

    private fun loadProfileRegistry(path: Path) {
        if (path.exists()) {
            profileRegistry = SaveData.load(path)
        } else {
            profileRegistry = SaveDataV1()
            profileRegistry.save(path)
        }
    }

    private fun drawOutput(matrixStack: MatrixStack) {
        MinecraftClient.getInstance().textRenderer.drawWithShadow(
            matrixStack, currentContext.toString(), 0f, 0f, 0xffffff
        )
    }

    override fun onInitializeClient() {
        // Load the profile registry (if it exists)
        loadProfileRegistry(PROFILE_REGISTRY_SAVE_PATH)

        // When leaving a world, reset the context ID
        ServerPlayConnectionEvents.DISCONNECT.register { networkHandler, minecraftServer ->
            currentContext = null
        }

        // Register rendering code with hud render callback
        HudRenderCallback.EVENT.register { matrixStack, _ ->
            if (currentContext != null) {
                drawOutput(matrixStack)
            }
        }
    }
}
