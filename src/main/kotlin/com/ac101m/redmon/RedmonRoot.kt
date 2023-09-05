package com.ac101m.redmon

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient


class RedmonRoot : ClientModInitializer {
    override fun onInitializeClient() {
        HudRenderCallback.EVENT.register { matrixStack, _ ->
            MinecraftClient.getInstance().textRenderer.drawWithShadow(
                matrixStack, "test", 0f, 0f, 0xffffff
            )
        }
    }
}
