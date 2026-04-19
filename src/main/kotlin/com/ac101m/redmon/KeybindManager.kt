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
        if (toggleVisibilityKey.consumeClick()) toggleVisibility()
        if (nextPageKey.consumeClick()) nextPageCommand()
        if (previousPageKey.consumeClick()) previousPageCommand()
    }

    private fun nextPageCommand() {
        try {
            redmon.nextPage()
        } catch (_: NoActiveProfileException) {
            /* Do nothing */
        }
    }

    private fun previousPageCommand() {
        try {
            redmon.previousPage()
        } catch (_: NoActiveProfileException) {
            /* Do nothing */
        }
    }

    private fun toggleVisibility() {
        redmon.toggleVisibility()
    }

    companion object {
        private const val KEYBIND_CATEGORY_NAME = "Redmon"
        private const val NEXT_PAGE_KEYBIND_NAME = "Next page"
        private const val PREVIOUS_PAGE_KEYBIND_NAME = "Previous page"
        private const val TOGGLE_UI_KEYBIND_NAME = "Toggle UI"

        private val toggleVisibilityKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                TOGGLE_UI_KEYBIND_NAME,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_EQUALS,
                KEYBIND_CATEGORY_NAME
            )
        )

        private val nextPageKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                NEXT_PAGE_KEYBIND_NAME,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_LBRACKET,
                KEYBIND_CATEGORY_NAME
            )
        )

        private val previousPageKey = KeyBindingHelper.registerKeyBinding(
            KeyMapping(
                PREVIOUS_PAGE_KEYBIND_NAME,
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_RBRACKET,
                KEYBIND_CATEGORY_NAME
            )
        )
    }
}
