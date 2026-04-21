package com.ac101m.redmon.utils

import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.core.Vec3i
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.PlainTextContents
import kotlin.math.absoluteValue
import kotlin.math.sign
import kotlin.math.sqrt

private const val SIGN_MASK = 0x8000
private const val EXPONENT_SHIFT = 10
private const val SHIFTED_EXPONENT_MASK = 0x1f
private const val SIGNIFICAND_MASK = 0x3ff
private const val EXPONENT_BIAS = 15
private const val FP32_EXPONENT_SHIFT = 23
private const val FP32_DENORMAL_MAGIC = 126 shl FP32_EXPONENT_SHIFT
private const val FP32_Q_NAN_MASK = 0x400000
private const val FP32_EXPONENT_BIAS = 127
private val FP32_DENORMAL_FLOAT = Float.fromBits(FP32_DENORMAL_MAGIC)

/**
 * Janky manual int to float conversion borrowed from android platform code.
 * Come on guys, shouldn't we have native Half precision already?
 */
fun floatFromFp16Bits(h: Int): Float {
    val bits: Int = h and 0xffff
    val s = bits and SIGN_MASK
    val e = (bits ushr EXPONENT_SHIFT) and SHIFTED_EXPONENT_MASK
    val m = (bits) and SIGNIFICAND_MASK
    var outE = 0
    var outM = 0
    if (e == 0) { // Denormal or 0
        if (m != 0) {
            // Convert denormalized fp16 into normalized fp32
            var o = Float.fromBits(FP32_DENORMAL_MAGIC + m)
            o -= FP32_DENORMAL_FLOAT
            return if (s == 0) o else -o
        }
    } else {
        outM = m shl 13
        if (e == 0x1f) { // Infinite or NaN
            outE = 0xff
            if (outM != 0) { // SNaNs are quieted
                outM = outM or FP32_Q_NAN_MASK
            }
        } else {
            outE = e - EXPONENT_BIAS + FP32_EXPONENT_BIAS
        }
    }
    val out = (s shl 16) or (outE shl FP32_EXPONENT_SHIFT) or outM
    return Float.fromBits(out)
}

fun CommandContext<FabricClientCommandSource>.sendError(message: String) {
    val componentContents = PlainTextContents.create("[redmon] $message")
    this.source.sendError(MutableComponent.create(componentContents))
}

fun CommandContext<FabricClientCommandSource>.sendFeedback(message: String) {
    val componentContents = PlainTextContents.create("§2[redmon]§f $message")
    this.source.sendFeedback(MutableComponent.create(componentContents))
}

fun Vec3i.length(): Double {
    val lengthSquared = (x.toLong() * x.toLong()) + (y.toLong() * y.toLong()) + (z.toLong() * z.toLong())
    return sqrt(lengthSquared.toDouble())
}

fun Int.ceilDiv(other: Int): Int {
    return this.floorDiv(other) + this.rem(other).sign.absoluteValue
}

fun computeMask(bitCount: Int): ULong {
    return if (bitCount < 64) {
        (1UL shl bitCount) - 1UL
    } else {
        ULong.MAX_VALUE
    }
}
