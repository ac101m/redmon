package com.ac101m.redmon.gui

import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.utils.Colour
import com.ac101m.redmon.utils.Rectangle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.Vec3i

class ProfileOverlay : Drawable2D {
    private val pageText = SingleLineTextBox()
    private val profileText = SingleLineTextBox()
    private val signalNameText = MultiLineTextBox()
    private val signalValueText = MultiLineTextBox()

    private val titleSection = HorizontalDivider(pageText, profileText)
    private val signalSection = VerticalDivider(signalNameText, signalValueText)
    private val layout = HorizontalDivider(titleSection, signalSection)

    override val height get() = layout.height
    override val width get() = layout.width

    override fun draw(context: GuiGraphics, position: Vec3i) {
        val titleSectionHeight = titleSection.height
        Rectangle(
            x = position.x,
            y = position.y,
            z = position.z,
            width = width,
            height = titleSectionHeight,
            colour = TITLE_BACKGROUND_COLOUR
        ).draw(context)
        Rectangle(
            x = position.x,
            y = position.y + titleSectionHeight,
            z = position.z,
            width = width,
            height = signalSection.height,
            colour = SIGNAL_BACKGROUND_COLOUR
        ).draw(context)
        layout.draw(context, position)
    }

    fun update(profile: Profile) {
        val currentPage = profile.getCurrentPage()

        pageText.text = "${Colour.GRAY.prefix}Page ${profile.getCurrentPageNumber()}/${profile.getPageCount()}"
        profileText.text = "${Colour.DARK_AQUA.prefix}${profile.name} (${profile.getCurrentPage().name})"

        signalNameText.lines.clear()
        signalValueText.lines.clear()

        currentPage.signals.forEach { signal ->
            signalNameText.lines.add("${Colour.GREEN.prefix}${signal.name}${Colour.WHITE.prefix}: ")
            signalValueText.lines.add(signal.getRepresentation())
        }
    }

    companion object {
        private const val TITLE_BACKGROUND_COLOUR = 0x7f000000
        private const val SIGNAL_BACKGROUND_COLOUR = 0x5f000000
    }
}
