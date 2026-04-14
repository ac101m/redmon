package com.ac101m.redmon.gui

import com.ac101m.redmon.utils.drawText
import com.ac101m.redmon.utils.textWidth
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.Vec3i

class SingleLineTextBox(var text: String = "UNINITIALIZED") : Drawable2D {
    override val height get() = 10 + 2
    override val width get() = textWidth(text) + 3

    override fun draw(context: GuiGraphics, position: Vec3i) {
        drawText(context, text, position.x + 1, position.y + 1, 0xffffffff.toInt())
    }
}
