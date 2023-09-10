package com.ac101m.redmon.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource
import net.minecraft.text.LiteralText


val mapper = ObjectMapper()

fun CommandContext<FabricClientCommandSource>.sendError(error: String) {
    this.source.sendError(LiteralText(error))
}

fun CommandContext<FabricClientCommandSource>.sendFeedback(message: String) {
    this.source.sendFeedback(LiteralText(message))
}

fun String.posixLexicalSplit(): List<String> {
    val tokens: MutableList<String> = ArrayList()
    var escaping = false
    var quoteChar = ' '
    var quoting = false
    var lastCloseQuoteIndex = Int.MIN_VALUE
    var current = StringBuilder()
    for (i in this.indices) {
        val c = this[i]
        if (escaping) {
            current.append(c)
            escaping = false
        } else if (c == '\\' && !(quoting && quoteChar == '\'')) {
            escaping = true
        } else if (quoting && c == quoteChar) {
            quoting = false
            lastCloseQuoteIndex = i
        } else if (!quoting && (c == '\'' || c == '"')) {
            quoting = true
            quoteChar = c
        } else if (!quoting && Character.isWhitespace(c)) {
            if (current.isNotEmpty() || lastCloseQuoteIndex == i - 1) {
                tokens.add(current.toString())
                current = StringBuilder()
            }
        } else {
            current.append(c)
        }
    }
    if (current.isNotEmpty() || lastCloseQuoteIndex == this.length - 1) {
        tokens.add(current.toString())
    }
    return tokens
}
