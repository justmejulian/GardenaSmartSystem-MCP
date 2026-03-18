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
    suspend fun handler(connection: ClientConnection, request: CallToolRequest): CallToolResult {
      val locationId = request.arguments?.get("locationId")?.jsonPrimitive?.content
      val deviceId = request.arguments?.get("deviceId")?.jsonPrimitive?.content
      val command = request.arguments?.get("command")?.jsonPrimitive?.content
      val seconds = request.arguments?.get("seconds")?.jsonPrimitive?.content?.toIntOrNull()

      if (locationId == null || deviceId == null || command == null) {
        return CallToolResult(
          content =
            listOf(TextContent(text = "Error: locationId, deviceId and command are required")),
          isError = true,
        )
      }

      val device =
        gardenaService.getDevice(locationId, deviceId)
          ?: return CallToolResult(
            content = listOf(TextContent(text = "Error: Could not find device for id $deviceId")),
            isError = true,
          )

      val supportedCommand =
        device.supportedCommands[command]
          ?: return CallToolResult(
            content =
              listOf(
                TextContent(text = "Error: device with id $deviceId does not support $command")
              ),
            isError = true,
          )

      gardenaService.sendCommand(deviceId, supportedCommand.toRequest(seconds))
      return CallToolResult(
        content = listOf(TextContent(text = "Sent Command $command to device $deviceId")),
        isError = false,
      )
    }

    server.addTool(
      tool =
        Tool(
          name = "send_command",
          description = "Send a command to a device (mower, power socket, valve, or valve set)",
          inputSchema =
            ToolSchema(
              properties =
                buildJsonObject {
                  put("locationId", buildJsonObject { put("type", "string") })
                  put("deviceId", buildJsonObject { put("type", "string") })
                  put("command", buildJsonObject { put("type", "string") })
                  put("seconds", buildJsonObject { put("type", "integer") })
                },
              required = listOf("locationId", "deviceId", "command"),
            ),
        ),
      handler = ::handler,
    )
  }
}
