package com.ac101m.redmon.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.Vec3i

interface Drawable2D {
    val width: Int
    val height: Int

    fun draw(context: GuiGraphics, position: Vec3i)
}
