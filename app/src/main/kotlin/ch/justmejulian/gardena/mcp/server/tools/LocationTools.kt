/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.server.tools

import ch.justmejulian.gardena.mcp.service.GardenaService
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
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
    registerListLocations(server, gardenaService)
  }

  /**
   * Tool: list_locations
   *
   * Retrieves all locations for the authenticated user. Each location represents a physical garden
   * or property managed through the Gardena Smart System.
   */
  private fun registerListLocations(server: Server, gardenaService: GardenaService) {
    server.addTool(
      name = "list_locations",
      description = "Get all locations for the authenticated user",
      inputSchema = Tool.Input(properties = buildJsonObject {}, required = emptyList()),
    ) { _ ->
      val locations = gardenaService.getLocations()
      val locationList =
        locations.data.map { location ->
          """
          Location: ${location.attributes?.name ?: "Unknown"}
          ID: ${location.id}
          """
            .trimIndent()
        }

      CallToolResult(
        content = listOf(TextContent(text = locationList.joinToString("\n---\n"))),
        isError = false,
      )
    }
  }
}
