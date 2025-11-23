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
 * MCP tools for sending commands to Gardena Smart System devices.
 *
 * Provides functionality to execute commands on supported devices (mowers, power sockets, valves).
 */
object CommandTools {

  /**
   * Register command-related tools with the MCP server.
   *
   * @param server The MCP server instance
   * @param gardenaService Service for interacting with Gardena API
   */
  fun register(server: Server, gardenaService: GardenaService) {
    server.addTool(
      name = "send_command",
      description = "Send a command to a device (mower, power socket, valve, or valve set)",
      inputSchema =
        Tool.Input(
          properties =
            buildJsonObject {
              put("locationId", buildJsonObject { put("type", "string") })
              put("deviceId", buildJsonObject { put("type", "string") })
              put("command", buildJsonObject { put("type", "string") })
              put("seconds", buildJsonObject { put("type", "int") })
            },
          required = listOf("locationId", "deviceId", "command"),
        ),
    ) { request ->
      val locationId = request.arguments["locationId"]?.jsonPrimitive?.content
      val deviceId = request.arguments["deviceId"]?.jsonPrimitive?.content
      val command = request.arguments["command"]?.jsonPrimitive?.content
      val seconds = request.arguments["seconds"]?.jsonPrimitive?.content?.toIntOrNull()

      if (locationId == null || deviceId == null || command == null) {
        return@addTool CallToolResult(
          content =
            listOf(TextContent(text = "Error: locationId, deviceId and command is required")),
          isError = true,
        )
      }

      val device = gardenaService.getDevice(locationId, deviceId)

      if (device == null) {
        return@addTool CallToolResult(
          content = listOf(TextContent(text = "Error: Could not find device for id $deviceId")),
          isError = true,
        )
      }

      val supportedCommand = device.supportedCommands.get(command)

      if (supportedCommand == null) {
        return@addTool CallToolResult(
          content =
            listOf(TextContent(text = "Error: device with id $deviceId does not support $command")),
          isError = true,
        )
      }

      gardenaService.sendCommand(deviceId, supportedCommand.toRequest(seconds))

      CallToolResult(
        content = listOf(TextContent(text = "Sent Command $command to deivce $deviceId")),
        isError = false,
      )
    }
  }
}
