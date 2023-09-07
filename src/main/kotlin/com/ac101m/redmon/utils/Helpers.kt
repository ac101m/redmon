package com.ac101m.redmon.utils

import com.fasterxml.jackson.databind.JsonNode


private fun hat() {

}


private fun JsonNode.getNonNullNode(key: String, errorPrefix: () -> String): JsonNode {
    val node = checkNotNull(this.get(key)) {
        "${errorPrefix()}, no such node."
    }

    require(!node.isNull) {
        "${errorPrefix()}, is a null node."
    }

    return node
}


fun JsonNode.getArrayNode(key: String): JsonNode {
    val errorPrefix = "Error getting array node '$key'"
    val node = this.getNonNullNode(key) { errorPrefix }

    require(node.isArray) {
        "$errorPrefix, node is not an array."
    }

    return node
}


fun JsonNode.getObjectNode(key: String): JsonNode {
    val errorPrefix = "Error getting object node '$key'"
    val node = this.getNonNullNode(key) { errorPrefix }

    require(node.isObject) {
        "$errorPrefix, node is not an object."
    }

    return node
}


fun JsonNode.getInt(key: String): Int {
    val errorPrefix = "Error getting integer node '$key'"
    val node = this.getNonNullNode(key) { errorPrefix }

    require(node.isNull) {
        "$errorPrefix, node is not an integer."
    }

    return node.asInt()
}
