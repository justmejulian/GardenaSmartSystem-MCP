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
import io.ktor.server.request.*
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
 * For SSE transport the bearer token is extracted from the incoming
 * `Authorization: Bearer <token>` header on each session.
 * For stdio transport the bearer token is supplied via the `--bearer-token` CLI argument.
 */
class MCPServer(
  private val apiKey: String,
  private val apiBaseUrl: String,
) {

  private val logger = KotlinLogging.logger {}

  /** Creates a fresh MCP [Server] wired to a [GardenaService] for the given [authorizationHeader]. */
  private fun buildServer(authorizationHeader: String): Server {
    val gardenaService =
      GardenaService(
        authorizationHeader = authorizationHeader,
        apiKey = apiKey,
        apiBaseUrl = apiBaseUrl,
      )

    return Server(
        serverInfo = Implementation(name = "gardena-smart-system", version = "1.0.0"),
        options =
          ServerOptions(
            capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = false))
          ),
      ) {
        "MCP Server for Gardena Smart System - control your garden devices via MCP"
      }
      .also { server ->
        LocationTools.register(server, gardenaService)
        DeviceTools.register(server, gardenaService)
        CommandTools.register(server, gardenaService)
      }
  }

  /**
   * Run the MCP server with stdio transport.
   *
   * @param authorizationHeader OAuth bearer token supplied via `--bearer-token` CLI argument.
   */
  fun runStdio(authorizationHeader: String) = runBlocking {
    logger.info { "Starting stdio transport" }
    val server = buildServer(authorizationHeader)
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
   * The bearer token is extracted from each session's `Authorization: Bearer` header and
   * forwarded to the Gardena API. A fresh [GardenaService] is created per session.
   */
  fun runSse(port: Int = 3000) {
    embeddedServer(CIO, host = "0.0.0.0", port = port) {
        mcp {
          // BREAKPOINT: Authorization header forwarded as-is from the MCP client's request
          val authorizationHeader =
            call.request.header("Authorization")
              ?: error("Missing Authorization: Bearer header")

          buildServer(authorizationHeader)
        }
      }
      .start(wait = true)
  }
}
