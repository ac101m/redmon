package com.ac101m.redmon.utils

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack


data class Rectangle(
    val x: Int,
    val y: Int,
    val z: Int,
    val width: Int,
    val height: Int,
    val color: Int
) {
    fun addToVertexBuffer(buffer: BufferBuilder) {
        val a = ((color shr 24) and 0xff) * 255.0f
        val r = ((color shr 16) and 0xff) * 255.0f
        val g = ((color shr 8) and 0xff) * 255.0f
        val b = (color and 0xff) * 255.0f

        buffer.vertex(
            x.toDouble(),
            y.toDouble(),
            z.toDouble()
        ).color(r, g, b, a).next()

        buffer.vertex(
            x.toDouble(),
            y.toDouble() + height,
            z.toDouble()
        ).color(r, g, b, a).next()

        buffer.vertex(
            x.toDouble() + width,
            y.toDouble() + height,
            z.toDouble()
        ).color(r, g, b, a).next()

        buffer.vertex(
            x.toDouble() + width,
            y.toDouble(),
            z.toDouble()
        ).color(r, g, b, a).next()
    }
}


fun drawRectangles(rectangles: List<Rectangle>) {
    RenderSystem.setShader(GameRenderer::getPositionColorShader)
    RenderSystem.applyModelViewMatrix()

    val tessellator = Tessellator.getInstance()
    val tessellatorBuffer = tessellator.buffer

    tessellatorBuffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)

    for (quad in rectangles) {
        quad.addToVertexBuffer(tessellatorBuffer)
    }

    RenderSystem.enableBlend()
    RenderSystem.blendFuncSeparate(
        GlStateManager.SrcFactor.SRC_ALPHA,
        GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
        GlStateManager.SrcFactor.ONE,
        GlStateManager.DstFactor.ZERO
    )
    tessellator.draw()
    RenderSystem.disableBlend()
}


fun drawRectangle(rectangle: Rectangle) {
    drawRectangles(listOf(rectangle))
}


fun drawText(matrixStack: MatrixStack, text: String, x: Int, y: Int, color: Int) {
    val client = MinecraftClient.getInstance()
    val textRenderer = client.textRenderer
    textRenderer.drawWithShadow(matrixStack, text, x.toFloat() + 1, y.toFloat() + 1, color)
}


fun textWidth(text: String): Int {
    return MinecraftClient.getInstance().textRenderer.getWidth(text)
}
