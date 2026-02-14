package com.playoutedge.server.mcp

import kotlinx.serialization.json.*

class McpServer(private val toolRegistry: McpToolRegistry) {

    suspend fun handleRequest(request: JsonRpcRequest, context: McpToolContext): JsonRpcResponse? {
        return try {
            when (request.method) {
                "initialize" -> handleInitialize(request)
                "tools/list" -> handleToolsList(request)
                "tools/call" -> handleToolsCall(request, context)
                "notifications/initialized" -> return null // notifications don't get responses per JSON-RPC spec
                else -> JsonRpcResponse(
                    error = JsonRpcError(McpErrorCodes.METHOD_NOT_FOUND, "Unknown method: ${request.method}"),
                    id = request.id
                )
            }
        } catch (e: Exception) {
            JsonRpcResponse(
                error = JsonRpcError(McpErrorCodes.INTERNAL_ERROR, e.message ?: "Internal error"),
                id = request.id
            )
        }
    }

    private fun handleInitialize(request: JsonRpcRequest): JsonRpcResponse {
        val result = buildJsonObject {
            put("protocolVersion", "2025-03-26")
            putJsonObject("capabilities") {
                putJsonObject("tools") {
                    put("listChanged", false)
                }
            }
            putJsonObject("serverInfo") {
                put("name", "playout-edge-mcp")
                put("version", "1.0.0")
            }
        }
        return JsonRpcResponse(result = result, id = request.id)
    }

    private fun handleToolsList(request: JsonRpcRequest): JsonRpcResponse {
        val tools = toolRegistry.listTools()
        val result = buildJsonObject {
            putJsonArray("tools") {
                for (tool in tools) {
                    addJsonObject {
                        put("name", tool.name)
                        put("description", tool.description)
                        put("inputSchema", tool.inputSchema)
                    }
                }
            }
        }
        return JsonRpcResponse(result = result, id = request.id)
    }

    private suspend fun handleToolsCall(request: JsonRpcRequest, context: McpToolContext): JsonRpcResponse {
        val params = request.params ?: return JsonRpcResponse(
            error = JsonRpcError(McpErrorCodes.INVALID_PARAMS, "Missing params"),
            id = request.id
        )

        val toolName = params["name"]?.jsonPrimitive?.contentOrNull
            ?: return JsonRpcResponse(
                error = JsonRpcError(McpErrorCodes.INVALID_PARAMS, "Missing tool name"),
                id = request.id
            )

        if (!toolRegistry.hasTool(toolName)) {
            return JsonRpcResponse(
                error = JsonRpcError(McpErrorCodes.INVALID_PARAMS, "Unknown tool: $toolName"),
                id = request.id
            )
        }

        val arguments = params["arguments"]?.jsonObject ?: buildJsonObject {}

        return try {
            val toolResult = toolRegistry.callTool(toolName, context, arguments)
            val result = buildJsonObject {
                putJsonArray("content") {
                    addJsonObject {
                        put("type", "text")
                        put("text", Json.encodeToString(JsonElement.serializer(), toolResult))
                    }
                }
            }
            JsonRpcResponse(result = result, id = request.id)
        } catch (e: IllegalArgumentException) {
            val result = buildJsonObject {
                putJsonArray("content") {
                    addJsonObject {
                        put("type", "text")
                        put("text", "Error: ${e.message}")
                    }
                }
                put("isError", true)
            }
            JsonRpcResponse(result = result, id = request.id)
        }
    }
}
