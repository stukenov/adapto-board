package com.playoutedge.server.mcp

import kotlinx.serialization.json.*
import java.util.UUID

data class McpToolDefinition(
    val name: String,
    val description: String,
    val inputSchema: JsonObject
)

data class McpToolContext(
    val tenantId: UUID,
    val userId: UUID?
)

typealias McpToolHandler = suspend (McpToolContext, JsonObject) -> JsonElement

class McpToolRegistry {
    private val tools = mutableMapOf<String, Pair<McpToolDefinition, McpToolHandler>>()

    fun register(definition: McpToolDefinition, handler: McpToolHandler) {
        tools[definition.name] = definition to handler
    }

    fun listTools(): List<McpToolDefinition> = tools.values.map { it.first }

    suspend fun callTool(name: String, context: McpToolContext, arguments: JsonObject): JsonElement {
        val (_, handler) = tools[name]
            ?: throw IllegalArgumentException("Unknown tool: $name")
        return handler(context, arguments)
    }

    fun hasTool(name: String): Boolean = name in tools
}

// Helper to build JSON schema for tool input
fun toolSchema(block: JsonObjectBuilder.() -> Unit): JsonObject = buildJsonObject {
    put("type", "object")
    block()
}

fun JsonObjectBuilder.properties(block: JsonObjectBuilder.() -> Unit) {
    putJsonObject("properties", block)
}

fun JsonObjectBuilder.stringProp(name: String, description: String) {
    putJsonObject(name) {
        put("type", "string")
        put("description", description)
    }
}

fun JsonObjectBuilder.intProp(name: String, description: String) {
    putJsonObject(name) {
        put("type", "integer")
        put("description", description)
    }
}

fun JsonObjectBuilder.boolProp(name: String, description: String) {
    putJsonObject(name) {
        put("type", "boolean")
        put("description", description)
    }
}

fun JsonObjectBuilder.required(vararg names: String) {
    putJsonArray("required") { names.forEach { add(it) } }
}
