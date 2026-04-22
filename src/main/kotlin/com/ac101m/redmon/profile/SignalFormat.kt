package com.ac101m.redmon.profile

import com.ac101m.redmon.utils.Config.Companion.DEFAULT_SIGNAL_FORMAT
import com.ac101m.redmon.utils.Colour
import com.ac101m.redmon.utils.RedmonException
import com.ac101m.redmon.utils.floatFromFp16Bits
import java.io.StringWriter

enum class SignalFormat {
    UNSIGNED,
    SIGNED,
    HEX,
    BINARY,
    FP16_IEEE,
    FP32_IEEE;

    /**
     * Get the textual representation if a bit vector with fixed size.
     *
     * @param bits The raw bit vector as an unsigned long.
     * @param bitCount The number of bits to consider as part of the vector.
     */
    fun getRepresentation(bits: ULong, bitCount: Int): String {
        return when (this) {
            UNSIGNED -> "$bits"
            SIGNED -> formatSigned(bits, bitCount)
            HEX -> formatHex(bits, bitCount)
            BINARY -> formatBinary(bits, bitCount)
            FP16_IEEE -> formatIeeeFp16(bits, bitCount)
            FP32_IEEE -> formatIeeeFp32(bits, bitCount)
        }
    }

    private fun formatSigned(bits: ULong, bitCount: Int): String {
        val signBitMask = (1UL shl (bitCount - 1))

        val signExtended = if ((bits and signBitMask) != 0UL) {
            bits or ((1UL shl bitCount) - 1UL).inv()
        } else {
            bits
        }

        return "${signExtended.toLong()}"
    }

    private fun formatHex(bits: ULong, bitCount: Int): String {
        val digitCount = when (bitCount % 4) {
            0 -> bitCount / 4
            else -> (bitCount / 4) + 1
        }

        val hex = bits.toString(16).uppercase()
        val sw = StringWriter()

        for (i in hex.length until digitCount) {
            sw.append("0")
        }

        sw.append(hex)

        return "${Colour.GRAY.prefix}0x${Colour.WHITE.prefix}$sw"
    }

    private fun formatBinary(bits: ULong, bitCount: Int): String {
        val bin = bits.toString(2).uppercase()
        val sw = StringWriter()

        for (i in bin.length until bitCount) {
            sw.append("0")
        }

        sw.append(bin)

        return "${Colour.GRAY.prefix}0b${Colour.WHITE.prefix}$sw"
    }

    private fun formatIeeeFp16(bits: ULong, bitCount: Int): String {
        if (bitCount != 16) {
            return "${Colour.RED.prefix}WRONG_SIZE"
        }
        return floatFromFp16Bits(bits.toInt()).toString()
    }

    private fun formatIeeeFp32(bits: ULong, bitCount: Int): String {
        if (bitCount != 32) {
            return "${Colour.RED.prefix}WRONG_SIZE"
        }
        return Float.fromBits(bits.toInt()).toString()
    }

    companion object {
        fun suggestNames(filter: String): List<String> {
            return SignalFormat.entries.map { it.name.lowercase() }.filter { it.contains(filter) }
        }

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
    }
}
