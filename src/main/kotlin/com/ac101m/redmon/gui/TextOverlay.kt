package com.ac101m.redmon.gui

import com.ac101m.redmon.utils.Rectangle
import com.ac101m.redmon.utils.drawRectangle
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3i

class TextOverlay(text: String) : Drawable2D {
    private val layout = SingleLineTextBox(text)

    override val height get() = layout.height
    override val width get() = layout.width

    override fun draw(matrixStack: MatrixStack, position: Vec3i) {
        drawRectangle(Rectangle(position.x, position.y, position.z, width, height, 0x7f000000))
        layout.draw(matrixStack, position)
    }
}
