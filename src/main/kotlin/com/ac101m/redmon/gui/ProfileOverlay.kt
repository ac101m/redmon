package com.ac101m.redmon.gui

import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.utils.Rectangle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.Vec3i

class ProfileOverlay : Drawable2D {
    private val titleText = SingleLineTextBox()
    private val offsetText = SingleLineTextBox()
    private val signalNameText = MultiLineTextBox()
    private val signalValueText = MultiLineTextBox()

    private val layout = VerticalDivider(
        listOf(
            titleText,
            offsetText,
            HorizontalDivider(
                listOf(
                    signalNameText,
                    signalValueText
                )
            )
        )
    )

    override val height get() = layout.height
    override val width get() = layout.width

    override fun draw(context: GuiGraphics, position: Vec3i) {
        Rectangle(position.x, position.y, position.z, width, height, 0x7f000000).draw(context)
        layout.draw(context, position)
    }

    fun update(profile: Profile, offset: Vec3i) {
        titleText.text = "Profile: ${profile.name}"
        offsetText.text = "Offset: (${offset.x}, ${offset.y}, ${offset.z})"

        signalNameText.lines.clear()
        signalValueText.lines.clear()

        profile.signals.forEach { signal ->
            signalNameText.lines.add("${signal.name}: ")
            signalValueText.lines.add(signal.getRepresentation())
        }
    }
}
