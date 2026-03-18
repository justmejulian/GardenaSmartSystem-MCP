/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.server

import ch.justmejulian.gardena.mcp.server.tools.CommandTools
import ch.justmejulian.gardena.mcp.server.tools.DeviceTools
import ch.justmejulian.gardena.mcp.server.tools.LocationTools
import ch.justmejulian.gardena.mcp.service.GardenaService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.server.mcp
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
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

  private val logger = KotlinLogging.logger {}

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
    CommandTools.register(server, gardenaService)
  }

  /**
   * Run the MCP server with stdio transport.
   *
   * The server uses standard input/output for communication, making it compatible with MCP clients
   * that spawn server processes (like Claude Desktop).
   */
  fun runStdio() = runBlocking {
    logger.info { "Starting stdio transport" }
    val transport =
      StdioServerTransport(
        inputStream = System.`in`.asSource().buffered(),
        outputStream = System.out.asSink().buffered(),
      )

    server.createSession(transport)

    val done = Job()
    server.onClose {
      logger.info { "MCP session closed" }
      done.complete()
    }
    done.join()
  }

  /**
   * Run the MCP server with SSE transport over HTTP.
   *
   * Starts a Ktor CIO HTTP server on the given [port] (default 3000) and exposes the MCP server
   * via Server-Sent Events at `GET /sse` and `POST /sse`. This makes it possible to tunnel the
   * server over a public URL with a tool like ngrok (`ngrok http <port>`).
   */
  fun runSse(port: Int = 3000) {
    embeddedServer(CIO, host = "0.0.0.0", port = port) {
      mcp { server }
    }
      .start(wait = true)
  }
}
