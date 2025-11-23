/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.server

import ch.justmejulian.gardena.mcp.server.tools.DeviceTools
import ch.justmejulian.gardena.mcp.server.tools.LocationTools
import ch.justmejulian.gardena.mcp.service.GardenaService
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered

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
    LocationTools.register(server, gardenaService)
    DeviceTools.register(server, gardenaService)
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
