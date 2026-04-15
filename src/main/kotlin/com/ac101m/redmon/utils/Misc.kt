package com.ac101m.redmon.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.core.Vec3i
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.PlainTextContents
import kotlin.math.absoluteValue
import kotlin.math.sign
import kotlin.math.sqrt

val mapper = ObjectMapper().registerKotlinModule()

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
