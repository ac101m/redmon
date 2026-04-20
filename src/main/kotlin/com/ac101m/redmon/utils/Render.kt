package com.ac101m.redmon.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics

data class Rectangle(
    val x: Int,
    val y: Int,
    val z: Int,
    val width: Int,
    val height: Int,
    val colour: Int
) {
    fun draw(context: GuiGraphics) {
        context.fill(x, y, x + width, y + height, colour)
    }
}

fun drawText(context: GuiGraphics, text: String, x: Int, y: Int, color: Int) {
    val client = Minecraft.getInstance()
    context.drawString(client.font, text, x, y, color)
}

fun textWidth(text: String): Int {
    return Minecraft.getInstance().font.width(text)
}
