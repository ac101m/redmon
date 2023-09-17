package com.ac101m.redmon.utils

enum class MinecraftColor(val prefix: String) {
    BLACK("§0"),
    DARK_BLUE("§1"),
    DARK_GREEN("§2"),
    DARK_AQUA("§3"),
    DARK_RED("§4"),
    DARK_PURPLE("§5"),
    GOLD("§6"),
    GRAY("§7"),
    DARK_GRAY("§8"),
    BLUE("§9"),
    GREEN("§a"),
    AQUA("§b"),
    RED("§c"),
    LIGHT_PURPLE("§d"),
    YELLOW("§e"),
    WHITE("§f");
}


fun String.color(color: MinecraftColor): String {
    return "${color.prefix}$this"
}

fun String.red() = this.color(MinecraftColor.RED)
