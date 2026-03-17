/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.server

import ch.justmejulian.gardena.mcp.server.tools.CommandTools
import ch.justmejulian.gardena.mcp.server.tools.DeviceTools
import ch.justmejulian.gardena.mcp.server.tools.LocationTools
import ch.justmejulian.gardena.mcp.service.GardenaService
import ch.justmejulian.gardena.mcp.util.OAuthConfig
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.server.mcpStreamableHttp
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class OAuthProtectedResourceMetadata(
  val resource: String,
  @SerialName("authorization_servers") val authorizationServers: List<String>,
)

/**
 * MCP Server that exposes Gardena Smart System devices and commands as tools.
 *
 * This server provides tools for interacting with the Gardena Smart System API through the Model
 * Context Protocol (MCP). It allows AI assistants to list locations, view devices, and send
 * commands to garden devices.
 */
class MCPServer(
  private val gardenaService: GardenaService,
  private val oauthConfig: OAuthConfig? = null,
) {

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

  /**
   * Run the MCP server with HTTP streamable transport.
   *
   * Starts an embedded Ktor HTTP server on the given port. The MCP endpoint is available at /mcp,
   * using the MCP streamable HTTP transport (modern MCP spec).
   */
  fun runHttp(port: Int = 3000) {
    System.err.println("Starting HTTP server on port $port (endpoint: /mcp)...")
    if (oauthConfig != null) {
      System.err.println("OAuth resource: ${oauthConfig.resourceBaseUrl}")
      if (oauthConfig.authServerUrls.isNotEmpty()) {
        System.err.println("OAuth auth servers: ${oauthConfig.authServerUrls.joinToString()}")
      }
    } else {
      System.err.println("OAuth config not set — protected resource metadata will not be served.")
    }
    embeddedServer(CIO, port = port) {
        if (oauthConfig != null) {
          val metadataUrl = "${oauthConfig.resourceBaseUrl}/.well-known/oauth-protected-resource"

          // Return 401 + WWW-Authenticate challenge for any request missing an Authorization
          // header,
          // except the well-known metadata endpoint which must remain publicly accessible (RFC
          // 9728).
          val bearerChallenge =
            createApplicationPlugin("BearerChallenge") {
              onCall { call ->
                val path = call.request.local.uri
                if (path == "/.well-known/oauth-protected-resource") return@onCall
                if (call.request.headers["Authorization"].isNullOrBlank()) {
                  call.response.header(
                    "WWW-Authenticate",
                    "Bearer resource_metadata=\"$metadataUrl\"",
                  )
                  call.respond(HttpStatusCode.Unauthorized)
                }
              }
            }
          install(bearerChallenge)

          routing {
            get("/.well-known/oauth-protected-resource") {
              val metadata =
                OAuthProtectedResourceMetadata(
                  resource = oauthConfig.resourceBaseUrl,
                  authorizationServers = oauthConfig.authServerUrls,
                )
              call.respondText(Json.encodeToString(metadata), ContentType.Application.Json)
            }
          }
        }
        mcpStreamableHttp("/mcp") { server }
      }
      .start(wait = true)
  }
}
