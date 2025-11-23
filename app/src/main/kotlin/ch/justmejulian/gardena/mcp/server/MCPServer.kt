/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.server

import ch.justmejulian.gardena.mcp.domain.device.*
import ch.justmejulian.gardena.mcp.service.GardenaService
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.*

/**
 * MCP Server that exposes Gardena Smart System devices and commands as tools.
 *
 * This server provides tools for interacting with the Gardena Smart System API through the Model
 * Context Protocol (MCP). It allows AI assistants to list locations, view devices, and send
 * commands to garden devices.
 */
class MCPServer(private val gardenaService: GardenaService) {

  /**
   * The MCP server instance with basic configuration.
   * - name: "gardena-smart-system" - identifies this server
   * - version: "1.0.0" - current version
   * - capabilities: tools support enabled
   */
  private val server =
    Server(
      serverInfo = Implementation(name = "gardena-smart-system", version = "1.0.0"),
      options =
        ServerOptions(
          capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = false))
        ),
    ) {
      "MCP Server for Gardena Smart System - control your garden devices via MCP"
    }

  init {
    registerTools()
  }

  /** Register all available MCP tools */
  private fun registerTools() {
    registerListLocations()
    registerGetDevices()
  }

  /**
   * Tool: list_locations
   *
   * Retrieves all locations for the authenticated user. Each location represents a physical garden
   * or property managed through the Gardena Smart System.
   */
  private fun registerListLocations() {
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

  /**
   * Tool: get_devices
   *
   * Retrieves all devices for a specific location. Returns device details including type-specific
   * attributes like battery level, connection status, and device state.
   */
  private fun registerGetDevices() {
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

  /**
   * Run the MCP server with stdio transport.
   *
   * The server uses standard input/output for communication, making it compatible with MCP clients
   * that spawn server processes (like Claude Desktop).
   */
  fun run() = runBlocking {
    val transport =
      StdioServerTransport(
        inputStream = System.`in`.asSource().buffered(),
        outputStream = System.out.asSink().buffered(),
      )

    server.createSession(transport)

    val done = Job()
    server.onClose { done.complete() }
    done.join()
  }
}
