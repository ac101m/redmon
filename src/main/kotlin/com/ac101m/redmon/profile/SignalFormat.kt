package com.ac101m.redmon.profile

import com.ac101m.redmon.isa.InstructionSet
import com.ac101m.redmon.utils.Config.Companion.DEFAULT_SIGNAL_FORMAT
import com.ac101m.redmon.utils.Colour
import com.ac101m.redmon.utils.RedmonException
import com.ac101m.redmon.utils.floatFromFp16Bits

enum class SignalFormat {
    UNSIGNED,
    SIGNED,
    HEX,
    BINARY,
    FP16_IEEE,
    FP32_IEEE,
    ASM;

    /**
     * Get the textual representation if a bit vector with fixed size.
     *
     * @param bits The raw bit vector as an unsigned long.
     * @param bitCount The number of bits to consider as part of the vector.
     * @param instructionSet The instruction set to use for ASM formatting.
     */
    fun getRepresentation(bits: ULong, bitCount: Int, instructionSet: InstructionSet?): String {
        return when (this) {
            UNSIGNED -> "$bits"
            SIGNED -> formatSigned(bits, bitCount)
            HEX -> formatHex(bits, bitCount)
            BINARY -> formatBinary(bits, bitCount)
            FP16_IEEE -> formatIeeeFp16(bits, bitCount)
            FP32_IEEE -> formatIeeeFp32(bits, bitCount)
            ASM -> formatAsm(bits, bitCount, instructionSet)
        }
    }

    private fun formatSigned(bits: ULong, bitCount: Int): String {
        val signBitMask = 1UL shl (bitCount - 1)

        val signExtended = if ((bits and signBitMask) != 0UL) {
            bits or ((1UL shl bitCount) - 1UL).inv()
        } else {
            bits
        }

        return "${signExtended.toLong()}"
    }

    private fun formatHex(bits: ULong, bitCount: Int): String {
        val sb = StringBuilder(bitCount * 2)

        sb.append(Colour.GRAY.prefix)
        sb.append("0x")
        sb.append(Colour.WHITE.prefix)
        sb.hexDigits(bits, bitCount)

        return sb.toString()
    }

    private fun formatBinary(bits: ULong, bitCount: Int): String {
        val bin = bits.toString(2).uppercase()
        val sb = StringBuilder(bitCount + 8)

        repeat(bitCount - bin.length) {
            sb.append("0")
        }

        sb.append(Colour.GRAY.prefix)
        sb.append("0b")
        sb.append(Colour.WHITE.prefix)
        sb.append(bin)

        return sb.toString()
    }

    private fun formatIeeeFp16(bits: ULong, bitCount: Int): String {
        if (bitCount != 16) {
            return WRONG_SIZE_STRING
        }
        return floatFromFp16Bits(bits.toInt()).toString()
    }

    private fun formatIeeeFp32(bits: ULong, bitCount: Int): String {
        if (bitCount != 32) {
            return WRONG_SIZE_STRING
        }
        return Float.fromBits(bits.toInt()).toString()
    }

    private fun formatAsm(bits: ULong, bitCount: Int, instructionSet: InstructionSet?): String {
        val sb = StringBuilder(32)

        if (instructionSet == null) {
            sb.append(Colour.RED.prefix)
            sb.append("NO_ISA")
        } else if (bitCount != instructionSet.instructionSize) {
            sb.append(WRONG_SIZE_STRING)
        } else when (val disassembly = instructionSet.disassemble(bits)) {
            null -> {
                sb.append(Colour.RED.prefix)
                sb.append("? ")
                sb.hexDigits(bits, bitCount)
                sb.append(" ?")
            }
            else -> sb.append(disassembly)
        }

        return sb.toString()
    }

    companion object {
        private val WRONG_SIZE_STRING = "${Colour.RED.prefix}WRONG_SIZE"

        fun fromCommandString(str: String): SignalFormat {
            return try {
                SignalFormat.valueOf(str.uppercase())
            } catch (e: IllegalArgumentException) {
                val validFormatString = SignalFormat.entries.joinToString(", ") { it.name.lowercase() }
                throw RedmonException("Invalid signal format. Valid formats are: $validFormatString", e)
            }
        }

        fun fromStringOrDefault(str: String): SignalFormat {
            return try {
                SignalFormat.valueOf(str)
            } catch (_: IllegalArgumentException) {
                return DEFAULT_SIGNAL_FORMAT
            }
        }

        private fun StringBuilder.hexDigits(bits: ULong, bitCount: Int) {
            val digitCount = when (bitCount % 4) {
                0 -> bitCount / 4
                else -> (bitCount / 4) + 1
            }

            val hex = bits.toString(16).uppercase()

            repeat(digitCount - hex.length) {
                append("0")
            }

            append(hex)
        }
    }
}
