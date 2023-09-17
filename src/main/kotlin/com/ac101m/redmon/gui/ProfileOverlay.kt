package com.ac101m.redmon.gui

import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.utils.Rectangle
import com.ac101m.redmon.utils.drawRectangle
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3i


class ProfileOverlay : Drawable2D {
    private val titleText = SingleLineTextBox()
    private val offsetText = SingleLineTextBox()
    private val registerNameText = MultiLineTextBox()
    private val registerValueText = MultiLineTextBox()

    private val layout = VerticalDivider(
        listOf(
            titleText,
            offsetText,
            HorizontalDivider(
                listOf(
                    registerNameText,
                    registerValueText
                )
            )
        )
    )

    override val height get() = layout.height
    override val width get() = layout.width

    override fun draw(matrixStack: MatrixStack, position: Vec3i) {
        drawRectangle(Rectangle(position.x, position.y, position.z, width, height, 0x7f000000))
        layout.draw(matrixStack, position)
    }

    fun update(profile: Profile, offset: Vec3i) {
        titleText.text = "Profile: ${profile.name}"
        offsetText.text = "Offset: (${offset.x}, ${offset.y}, ${offset.z})"

        registerNameText.lines.clear()
        registerValueText.lines.clear()

        profile.registers.forEach { entry ->
            registerNameText.lines.add("${entry.value.name}: ")
            registerValueText.lines.add(entry.value.getRepresentation())
        }
    }
}
