package com.ac101m.redmon.isa.instruction

abstract class ParameterField(
    size: Int,
    offset: Int,
    description: String?
) : Field(size, offset, description) {
    abstract val displayChar: Char

    override fun bitRepresentation(crossOut: Boolean): String {
        return StringBuilder(size * 2).apply {
            if (crossOut) {
                append(CROSSED_OUT_COLOUR.prefix)
                repeat(size) { append('-') }
            } else {
                append(displayColour.prefix)
                repeat(size) { append(displayChar) }
            }
        }.toString()
    }
}
