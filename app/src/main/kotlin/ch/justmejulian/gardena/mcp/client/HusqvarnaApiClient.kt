/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TokenResponse(
  val access_token: String,
  val token_type: String,
  val expires_in: Int,
  val scope: String? = null,
)

@Serializable
data class ErrorResponse(val error: String? = null, val error_description: String? = null)

class HusqvarnaApiClient(private val clientId: String, private val clientSecret: String) {
  private val authBaseUrl = "https://api.authentication.husqvarnagroup.dev/v1"

  private val httpClient =
    HttpClient(CIO) {
      install(ContentNegotiation) {
        json(
          Json {
            ignoreUnknownKeys = true
            prettyPrint = true
          }
        )
      }
    }

  suspend fun authenticate(): TokenResponse {
    val response =
      httpClient.post("$authBaseUrl/oauth2/token") {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(
          listOf(
              "client_id" to clientId,
              "client_secret" to clientSecret,
              "grant_type" to "client_credentials",
            )
            .formUrlEncode()
        )
      }

    // Check if response was successful
    if (!response.status.isSuccess()) {
      val errorBody = response.bodyAsText()
      throw IllegalStateException(
        "Authentication failed with status ${response.status}: $errorBody"
      )
    }

    return response.body<TokenResponse>()
  }

  fun close() {
    httpClient.close()
  }
}
