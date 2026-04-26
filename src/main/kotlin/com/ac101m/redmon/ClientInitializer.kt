package com.ac101m.redmon

import com.ac101m.redmon.utils.Config.Companion.PROFILE_STORAGE_PATH
import com.ac101m.redmon.utils.Config.Companion.WORLD_METADATA_STORAGE_PATH
import com.ac101m.redmon.utils.Config.Companion.INSTRUCTION_SET_STORAGE_PATH
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

/**
 * Mod entrypoint.
 * Handles mod initialisation and callback registration.
 */
@Suppress("UNUSED")
class ClientInitializer : ClientModInitializer {
    private lateinit var redmon: RedmonState
    private lateinit var commandManager: CommandManager
    private lateinit var keybindManager: KeybindManager

    private var lastLevel: Level? = null

    override fun onInitializeClient() {
        redmon = RedmonState(
            PROFILE_STORAGE_PATH,
            WORLD_METADATA_STORAGE_PATH,
            INSTRUCTION_SET_STORAGE_PATH
        )
        commandManager = CommandManager(redmon)
        keybindManager = KeybindManager(redmon)

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            commandManager.registerCommands(dispatcher)
        }

        ClientTickEvents.END_CLIENT_TICK.register { client ->
            keybindManager.processKeypresses(client)
        }

        HudLayerRegistrationCallback.EVENT.register { layeredDrawerWrapper ->
            layeredDrawerWrapper.attachLayerAfter(IdentifiedLayer.CHAT, resourceLocation) { context, _ ->
                val client = Minecraft.getInstance()
                if (client.player != null && !client.gui.debugOverlay.showDebugScreen()) {
                    redmon.drawOverlay(context)
                }
            }
        }

        ClientTickEvents.END_WORLD_TICK.register { level ->
            if (lastLevel !== level) {
                redmon.updateCurrentWorld()
                lastLevel = level
            }
        }
    }

    companion object {
        private val resourceLocation = ResourceLocation.fromNamespaceAndPath("redmon", "gui")
    }
}
