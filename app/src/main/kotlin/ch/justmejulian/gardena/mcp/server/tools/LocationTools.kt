/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.server.tools

import ch.justmejulian.gardena.mcp.service.GardenaService
import io.modelcontextprotocol.kotlin.sdk.server.ClientConnection
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.buildJsonObject

/**
 * MCP tools for managing Gardena Smart System locations.
 *
 * Provides functionality to list and query location information for the authenticated user.
 */
object LocationTools {

  /**
   * Register location-related tools with the MCP server.
   *
   * @param server The MCP server instance
   * @param gardenaService Service for interacting with Gardena API
   */
  fun register(server: Server, gardenaService: GardenaService) {
    suspend fun handler(_: ClientConnection, _: CallToolRequest): CallToolResult {
      val locations = gardenaService.getLocations()
      val locationList =
        locations.data.joinToString("\n---\n") { location ->
          """
          Location: ${location.attributes?.name ?: "Unknown"}
          ID: ${location.id}
          """
            .trimIndent()
        }

      return CallToolResult(
        content = listOf(TextContent(text = locationList)),
        isError = false,
      )
    }

    server.addTool(
      tool =
        Tool(
          name = "list_locations",
          description = "Get all locations for the authenticated user",
          inputSchema = ToolSchema(properties = buildJsonObject {}, required = emptyList()),
        ),
      handler = ::handler,
    )
  }
}
