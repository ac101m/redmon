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
        if (toggleVisibilityKey.justPressed()) toggleVisibility()
        if (nextPageKey.justPressed()) nextPageCommand()
        if (previousPageKey.justPressed()) previousPageCommand()
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

        private class KeyBind(name: String, type: InputConstants.Type, defaultKey: Int) {
            private val keyMapping: KeyMapping = KeyBindingHelper.registerKeyBinding(
                KeyMapping(name, type, defaultKey, KEYBIND_CATEGORY_NAME)
            )

            private var wasDown: Boolean = false

            fun justPressed(): Boolean {
                val previousDown = wasDown
                wasDown = keyMapping.isDown
                return !previousDown && wasDown
            }
        }

        private val toggleVisibilityKey = KeyBind(
            name = TOGGLE_UI_KEYBIND_NAME,
            type = InputConstants.Type.KEYSYM,
            defaultKey = InputConstants.KEY_EQUALS
        )

        private val nextPageKey = KeyBind(
            name = NEXT_PAGE_KEYBIND_NAME,
            type = InputConstants.Type.KEYSYM,
            defaultKey = InputConstants.KEY_RBRACKET
        )

        private val previousPageKey = KeyBind(
            name = PREVIOUS_PAGE_KEYBIND_NAME,
            type = InputConstants.Type.KEYSYM,
            defaultKey = InputConstants.KEY_LBRACKET
        )
    }
}
