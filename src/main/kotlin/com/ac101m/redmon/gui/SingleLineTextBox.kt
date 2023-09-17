package com.ac101m.redmon.gui

import com.ac101m.redmon.utils.drawText
import com.ac101m.redmon.utils.textWidth
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3i


class SingleLineTextBox(var text: String = "UNINITIALIZED") : Drawable2D {
    override val height get() = 10 + 2
    override val width get() = textWidth(text) + 3

    override fun draw(matrixStack: MatrixStack, position: Vec3i) {
        drawText(matrixStack, text, position.x + 1, position.y + 1, 0xffffff)
    }
}
