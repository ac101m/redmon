package com.ac101m.redmon.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.Vec3i

class HorizontalDivider(private vararg val rows: Drawable2D) : Drawable2D {
    override val height get() = rows.sumOf { it.height }
    override val width get() = rows.maxOf { it.width }

    override fun draw(context: GuiGraphics, position: Vec3i) {
        var currentPosition = position
        for (subObject in rows) {
            subObject.draw(context, currentPosition)
            currentPosition = currentPosition.offset(Vec3i(0, subObject.height, 0))
        }
    }
}
