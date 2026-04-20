package com.ac101m.redmon.gui

import com.ac101m.redmon.utils.drawText
import com.ac101m.redmon.utils.textWidth
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.Vec3i

class MultiLineTextBox(var lines: MutableList<String> = ArrayList()) : Drawable2D {
    override val height get() = (lines.size * 10) + 1
    override val width get() = lines.maxOfOrNull { textWidth(it) + 5 } ?: 0

    override fun draw(context: GuiGraphics, position: Vec3i) {
        var currentPosition = position.offset(Vec3i(1, 1, 0))
        for (line in lines) {
            drawText(context, line, currentPosition.x, currentPosition.y, 0xffffffff.toInt())
            currentPosition = currentPosition.offset(Vec3i(0, 10, 0))
        }
    }
}
