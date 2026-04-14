package com.ac101m.redmon.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.Vec3i

class HorizontalDivider(private val subObjects: List<Drawable2D>) : Drawable2D {
    override val height get() = subObjects.maxOf { it.height }
    override val width get() = subObjects.sumOf { it.width }

    override fun draw(context: GuiGraphics, position: Vec3i) {
        var currentPosition = position
        for (subObject in subObjects) {
            subObject.draw(context, currentPosition)
            currentPosition = currentPosition.offset(Vec3i(subObject.width, 0, 0))
        }
    }
}
