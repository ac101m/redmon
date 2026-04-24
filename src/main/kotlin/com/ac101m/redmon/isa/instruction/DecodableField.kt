package com.ac101m.redmon.isa.instruction

interface DecodableField {
    /**
     * Decode this fields text with respect to a given input bit pattern.
     *
     * @param bits The instruction bits to decode.
     */
    fun decode(bits: ULong): String
}
