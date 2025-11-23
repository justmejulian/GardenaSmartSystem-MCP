/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.client

import kotlin.test.Test
import kotlin.test.assertEquals

class HusqvarnaApiClientTest {

  @Test
  fun `TokenResponse can be constructed with required fields`() {
    val token = TokenResponse(access_token = "test-token", token_type = "Bearer", expires_in = 3600)

    assertEquals("test-token", token.access_token)
    assertEquals("Bearer", token.token_type)
    assertEquals(3600, token.expires_in)
  }

  @Test
  fun `TokenResponse can be constructed with optional scope`() {
    val token =
      TokenResponse(
        access_token = "test-token",
        token_type = "Bearer",
        expires_in = 3600,
        scope = "iam:read iam:write",
      )

    assertEquals("iam:read iam:write", token.scope)
  }

  @Test
  fun `ErrorResponse can be constructed with error details`() {
    val error =
      ErrorResponse(error = "invalid_client", error_description = "Invalid client credentials")

    assertEquals("invalid_client", error.error)
    assertEquals("Invalid client credentials", error.error_description)
  }

  @Test
  fun `ErrorResponse can be constructed with null values`() {
    val error = ErrorResponse()

    assertEquals(null, error.error)
    assertEquals(null, error.error_description)
  }

  @Test
  fun `HusqvarnaApiClient can be instantiated with default auth URL`() {
    val client = HusqvarnaApiClient("test-id", "test-secret")
    // Verify client can be created and closed
    client.close()
  }

  @Test
  fun `HusqvarnaApiClient can be instantiated with custom auth URL`() {
    val customUrl = "https://custom-auth.example.com"
    val client = HusqvarnaApiClient("test-id", "test-secret", customUrl)
    // Verify client can be created with custom URL and closed
    client.close()
  }
}
