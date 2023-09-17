package com.ac101m.redmon.gui

import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3i


interface Drawable2D {
    val width: Int
    val height: Int

    fun draw(matrixStack: MatrixStack, position: Vec3i)
}
