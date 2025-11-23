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
    server.addTool(
      name = "get_devices",
      description = "Get all devices for a specific location",
      inputSchema =
        Tool.Input(
          properties =
            buildJsonObject { put("locationId", buildJsonObject { put("type", "string") }) },
          required = listOf("locationId"),
        ),
    ) { request ->
      val locationId = request.arguments["locationId"]?.jsonPrimitive?.content

      if (locationId == null) {
        return@addTool CallToolResult(
          content = listOf(TextContent(text = "Error: locationId is required")),
          isError = true,
        )
      }

      val devices = gardenaService.getDevices(locationId)
      val deviceList = devices.map { it.toString() }

      CallToolResult(
        content = listOf(TextContent(text = deviceList.joinToString("\n---\n"))),
        isError = false,
      )
    }
  }
}
