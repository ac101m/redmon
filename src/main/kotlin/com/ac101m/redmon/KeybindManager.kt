package com.ac101m.redmon

import com.ac101m.redmon.utils.NoActiveProfileException
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft

/**
 * Class for managing keypress side effects.
 * Analogous to the command manager, but for keybinds instead.
 *
 * @param redmon The main redmon state object.
 */
class KeybindManager(private val redmon: RedmonState) {
    fun processKeypresses(client: Minecraft) {
        if (nextPageKey.consumeClick()) nextPageCommand()
        if (previousPageKey.consumeClick()) previousPageCommand()
    }

    fun nextPageCommand() {
        try {
            redmon.nextPage()
        } catch (_: NoActiveProfileException) {
            /* Do nothing */
        }
    }

    fun previousPageCommand() {
        try {
            redmon.previousPage()
        } catch (_: NoActiveProfileException) {
            /* Do nothing */
        }
    }

    companion object {
        private const val KEYBIND_CATEGORY_NAME = "Redmon"
        private const val KEY_NEXT_PAGE = "Next page"
        private const val KEY_PREVIOUS_PAGE = "Previous page"

        private val nextPageKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                KEY_NEXT_PAGE,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_LBRACKET,
                KEYBIND_CATEGORY_NAME
            )
        )

        private val previousPageKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                KEY_PREVIOUS_PAGE,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_RBRACKET,
                KEYBIND_CATEGORY_NAME
            )
        )
    }
}
