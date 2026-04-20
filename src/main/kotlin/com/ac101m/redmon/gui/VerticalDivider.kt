package com.ac101m.redmon.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.Vec3i

class VerticalDivider(var columns: List<Drawable2D> = ArrayList()) : Drawable2D {
    override val height get() = columns.maxOf { it.height }
    override val width get() = columns.sumOf { it.width }

    override fun draw(context: GuiGraphics, position: Vec3i) {
        var currentPosition = position
        for (subObject in columns) {
            subObject.draw(context, currentPosition)
            currentPosition = currentPosition.offset(Vec3i(subObject.width, 0, 0))
        }
    }
}
