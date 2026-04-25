package com.ac101m.redmon.gui

import com.ac101m.redmon.profile.Profile
import com.ac101m.redmon.utils.Colour
import com.ac101m.redmon.utils.Rectangle
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.core.Vec3i

class ProfileOverlay : Drawable2D {
    private val pageAndIsaText = SingleLineTextBox()
    private val profileText = SingleLineTextBox()
    private val signalNameTextElements = ArrayList<MultiLineTextBox>()
    private val signalValueTextElements = ArrayList<MultiLineTextBox>()

    private val titleSection = HorizontalDivider(pageAndIsaText, profileText)
    private var signalSection = VerticalDivider()
    private val layout = HorizontalDivider(titleSection, signalSection)

    override val height get() = layout.height
    override val width get() = layout.width

    private var prevColumnCount: Int? = null

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
        val currentIsa = currentPage.currentIsa
        val columnCount = currentPage.columns.size

        if (columnCount != prevColumnCount) {
            updateLayout(columnCount)
            prevColumnCount = columnCount
        }

        val pageText = "${Colour.GRAY.prefix}Page ${profile.getCurrentPageNumber()}/${profile.getPageCount()}"
        val isaText = "${Colour.GRAY.prefix}ISA: ${currentIsa?.name ?: "none"}"

        pageAndIsaText.text = "$pageText - $isaText"
        profileText.text = "${Colour.DARK_AQUA.prefix}${profile.name} - ${profile.getCurrentPage().name}"

        currentPage.columns.forEachIndexed { columnIndex, column ->
            val signalNamesElement = signalNameTextElements[columnIndex].apply { lines.clear() }
            val signalValuesElement = signalValueTextElements[columnIndex].apply { lines.clear() }

            column.signals.forEach { signal ->
                signalNamesElement.lines.add("${Colour.GREEN.prefix}${signal.name}${Colour.WHITE.prefix}: ")
                signalValuesElement.lines.add(signal.getRepresentation(currentIsa))
            }
        }
    }

    /**
     * Updates the layout (must be recalculated when the column count changes)
     */
    private fun updateLayout(columnCount: Int) {
        signalNameTextElements.clear()
        signalValueTextElements.clear()

        val combinedSignalTextArray = ArrayList<MultiLineTextBox>(columnCount * 2)

        repeat(columnCount) {
            val nameTextBox = MultiLineTextBox()
            val valueTextBox = MultiLineTextBox()

            signalNameTextElements.add(nameTextBox)
            signalValueTextElements.add(valueTextBox)

            combinedSignalTextArray.add(nameTextBox)
            combinedSignalTextArray.add(valueTextBox)
        }

        signalSection.columns = combinedSignalTextArray
    }

    companion object {
        private const val TITLE_BACKGROUND_COLOUR = 0x7f000000
        private const val SIGNAL_BACKGROUND_COLOUR = 0x5f000000
    }
}
