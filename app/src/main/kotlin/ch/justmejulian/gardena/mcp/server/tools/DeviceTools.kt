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
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * MCP tools for managing Gardena Smart System devices.
 *
 * Provides functionality to query device information and send commands to devices.
 */
object DeviceTools {

  /**
   * Register device-related tools with the MCP server.
   *
   * @param server The MCP server instance
   * @param gardenaService Service for interacting with Gardena API
   */
  fun register(server: Server, gardenaService: GardenaService) {
    registerGetDevices(server, gardenaService)
  }

  /**
   * Tool: get_devices
   *
   * Retrieves all devices for a specific location. Returns device details including type-specific
   * attributes like battery level, connection status, and device state.
   */
  private fun registerGetDevices(server: Server, gardenaService: GardenaService) {
    val handler: suspend (ClientConnection, CallToolRequest) -> CallToolResult =
      { _, request ->
        val locationId = request.arguments?.get("locationId")?.jsonPrimitive?.content

        if (locationId == null) {
          CallToolResult(
            content = listOf(TextContent(text = "Error: locationId is required")),
            isError = true,
          )
        } else {
          val devices = gardenaService.getDevices(locationId)
          val deviceList = devices.map { it.toString() }

          CallToolResult(
            content = listOf(TextContent(text = deviceList.joinToString("\n---\n"))),
            isError = false,
          )
        }
      }

    server.addTool(
      tool =
        Tool(
          name = "get_devices",
          description = "Get all devices for a specific location",
          inputSchema =
            ToolSchema(
              properties =
                buildJsonObject { put("locationId", buildJsonObject { put("type", "string") }) },
              required = listOf("locationId"),
            ),
        ),
      handler = handler,
    )
  }
}
