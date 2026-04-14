package com.ac101m.redmon.gui

import com.ac101m.redmon.utils.Rectangle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.Vec3i

class TextOverlay(text: String) : Drawable2D {
    private val layout = SingleLineTextBox(text)

    override val height get() = layout.height
    override val width get() = layout.width

    override fun draw(context: GuiGraphics, position: Vec3i) {
        Rectangle(position.x, position.y, position.z, width, height, 0x7f000000).draw(context)
        layout.draw(context, position)
    }
}
