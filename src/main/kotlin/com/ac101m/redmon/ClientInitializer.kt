package com.ac101m.redmon

import com.ac101m.redmon.utils.Config.Companion.PROFILE_SAVE_PATH
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation

/**
 * Mod entrypoint.
 * Handles mod initialisation and callback registration.
 */
@Suppress("UNUSED")
class ClientInitializer : ClientModInitializer {
    private lateinit var redmon: RedmonState
    private lateinit var commandManager: CommandManager

    override fun onInitializeClient() {
        redmon = RedmonState(PROFILE_SAVE_PATH)
        commandManager = CommandManager(redmon)

        redmon.loadProfiles()

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            commandManager.registerCommands(dispatcher)
        }

        HudElementRegistry.attachElementAfter(VanillaHudElements.CHAT, resourceLocation) { context, _ ->
            val client = Minecraft.getInstance()
            if (client.player != null && !client.gui.debugOverlay.showDebugScreen()) {
                redmon.drawOverlay(context)
            }
        }
    }

    companion object {
        private val resourceLocation = ResourceLocation.fromNamespaceAndPath("redmon", "gui")
    }
}
