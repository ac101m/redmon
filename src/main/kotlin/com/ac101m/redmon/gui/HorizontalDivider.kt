package com.ac101m.redmon.gui

import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3i


class HorizontalDivider(private val subObjects: List<Drawable2D>) : Drawable2D {
    override val height get() = subObjects.maxOf { it.height }
    override val width get() = subObjects.sumOf { it.width }

    override fun draw(matrixStack: MatrixStack, position: Vec3i) {
        var currentPosition = position
        for (subObject in subObjects) {
            subObject.draw(matrixStack, currentPosition)
            currentPosition = currentPosition.add(Vec3i(subObject.width, 0, 0))
        }
    }
}
