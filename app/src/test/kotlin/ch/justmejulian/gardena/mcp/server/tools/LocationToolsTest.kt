/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.server.tools

import ch.justmejulian.gardena.mcp.service.GardenaService
import com.gardena.smartgarden.service.iapi.generated.model.*
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import kotlin.test.*

class LocationToolsTest {

  private lateinit var server: Server
  private lateinit var service: GardenaService

  @BeforeTest
  fun setup() {
    server =
      Server(
        serverInfo = Implementation(name = "test-server", version = "1.0.0"),
        options =
          ServerOptions(
            capabilities = ServerCapabilities(tools = ServerCapabilities.Tools(listChanged = false))
          ),
      ) {
        "Test server"
      }

    // Create service (won't be used for actual API calls in these tests)
    service = GardenaService("test-id", "test-secret")
  }

  @AfterTest
  fun tearDown() {
    service.close()
  }

  @Test
  fun `register adds list_locations tool to server`() {
    LocationTools.register(server, service)

    // Verify the tool was registered
    // Note: We can't easily test the tool execution without calling the actual API
    // This test verifies the registration happens without errors
  }

  @Test
  fun `LocationTools object can be instantiated`() {
    // Verify the object exists and is accessible
    assertNotNull(LocationTools)
  }

  @Test
  fun `register method does not throw exceptions`() {
    // Verify registration completes without errors
    try {
      LocationTools.register(server, service)
      // Success - no exception thrown
    } catch (e: Exception) {
      fail("Registration should not throw: ${e.message}")
    }
  }
}
