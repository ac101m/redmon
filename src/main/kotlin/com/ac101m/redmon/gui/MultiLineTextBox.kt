package com.ac101m.redmon.gui

import com.ac101m.redmon.utils.drawText
import com.ac101m.redmon.utils.textWidth
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3i


class MultiLineTextBox(var lines: MutableList<String> = ArrayList()) : Drawable2D {
    override val height get() = (lines.size * 10) + 2
    override val width get() = lines.maxOfOrNull { textWidth(it) + 3 } ?: 0

    override fun draw(matrixStack: MatrixStack, position: Vec3i) {
        var currentPosition = position.add(Vec3i(1, 1, 0))
        for (line in lines) {
            drawText(matrixStack, line, currentPosition.x, currentPosition.y, 0xffffff)
            currentPosition = currentPosition.add(Vec3i(0, 10, 0))
        }
    }
}
