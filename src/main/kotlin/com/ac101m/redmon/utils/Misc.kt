package com.ac101m.redmon.utils

import com.ac101m.redmon.utils.Config.Companion.ISSUE_CREATE_PROMPT
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.PlainTextContents

val mapper = ObjectMapper().registerKotlinModule()

fun CommandContext<FabricClientCommandSource>.sendError(error: String) {
    val componentContents = PlainTextContents.create(error)
    this.source.sendError(MutableComponent.create(componentContents))
}

fun CommandContext<FabricClientCommandSource>.sendFeedback(message: String) {
    val componentContents = PlainTextContents.create("§2$message§f")
    this.source.sendFeedback(MutableComponent.create(componentContents))
}

fun Map<String, Any>.getStringCommandArgument(key: String): String {
    val anyValue = requireNotNull(this[key]) {
        "$key parameter is missing. $ISSUE_CREATE_PROMPT"
    }
    require(anyValue is String) {
        "$key parameter is not a string. $ISSUE_CREATE_PROMPT"
    }
    return anyValue
}

fun Map<String, Any>.getIntCommandArgument(key: String): Int {
    val anyValue = requireNotNull(this[key]) {
        "$key parameter is missing. $ISSUE_CREATE_PROMPT"
    }
    require(anyValue is String) {
        "$key parameter is not a string. $ISSUE_CREATE_PROMPT"
    }
    return try {
        anyValue.toInt()
    } catch (e: Exception) {
        throw RedmonCommandException("$key expects an integer value, got $anyValue")
    }
}

fun Map<String, Any>.getBooleanCommandArgument(key: String): Boolean {
    val anyValue = requireNotNull(this[key]) {
        "$key parameter is missing. $ISSUE_CREATE_PROMPT"
    }
    require(anyValue is Boolean) {
        "$key parameter is not a boolean. $ISSUE_CREATE_PROMPT"
    }
    return anyValue == true
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
