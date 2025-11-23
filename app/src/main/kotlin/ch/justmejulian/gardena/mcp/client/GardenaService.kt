/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.client

import ch.justmejulian.gardena.mcp.domain.device.Device
import ch.justmejulian.gardena.mcp.domain.mapper.DeviceMapper
import com.gardena.smartgarden.service.iapi.generated.ApiClient
import com.gardena.smartgarden.service.iapi.generated.api.ControlApi
import com.gardena.smartgarden.service.iapi.generated.api.HealthCheckApi
import com.gardena.smartgarden.service.iapi.generated.api.SnapshotApi
import com.gardena.smartgarden.service.iapi.generated.model.CommandRequest
import com.gardena.smartgarden.service.iapi.generated.model.LocationResponse
import com.gardena.smartgarden.service.iapi.generated.model.LocationsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service for interacting with the Gardena Smart System API.
 *
 * Handles authentication and provides access to Gardena API operations.
 */
class GardenaService(
  private val clientId: String,
  private val clientSecret: String,
  private val authBaseUrl: String = "https://api.authentication.husqvarnagroup.dev/v1",
  private val apiBaseUrl: String = "https://api.smart.gardena.dev/v2",
) {
  private val authClient = HusqvarnaApiClient(clientId, clientSecret, authBaseUrl)
  private var apiClient: ApiClient? = null
  private var accessToken: String? = null

  /**
   * Get or create the authenticated API client.
   *
   * Authenticates on first access and caches the client for subsequent calls.
   */
  private suspend fun getClient(): ApiClient {
    if (apiClient == null) {
      authenticate()
    }
    return apiClient!!
  }

  /**
   * Authenticate and initialize the Gardena API client. Called automatically on first API access.
   */
  private suspend fun authenticate() {
    val tokenResponse = authClient.authenticate()
    accessToken = tokenResponse.access_token

    // Create and configure API client with Bearer token and required headers
    apiClient =
      ApiClient().apply {
        updateBaseUri(apiBaseUrl)
        setRequestInterceptor { requestBuilder ->
          requestBuilder.header("Authorization", "Bearer ${accessToken}")
          requestBuilder.header("X-Api-Key", clientId)
          requestBuilder.header("Accept", "application/vnd.api+json")
        }
      }
  }

  /** Check if the API is healthy and accessible. */
  suspend fun healthCheck(): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val healthCheckApi = HealthCheckApi(getClient())
        healthCheckApi.getHealth()
        true
      } catch (e: Exception) {
        false
      }
    }

  /** Get all locations for the authenticated user. */
  suspend fun getLocations(): LocationsResponse =
    withContext(Dispatchers.IO) {
      val snapshotApi = SnapshotApi(getClient())
      snapshotApi.listLocations()
    }

  /**
   * Get location details including all devices and services.
   *
   * @param locationId The ID of the location to fetch
   * @return LocationResponse containing location data and included devices/services
   */
  suspend fun getLocation(locationId: String): LocationResponse =
    withContext(Dispatchers.IO) {
      val snapshotApi = SnapshotApi(getClient())
      snapshotApi.listLocation(locationId)
    }

  /**
   * Get all devices for a specific location.
   *
   * @param locationId The ID of the location to fetch devices for
   * @return List of Device instances mapped from the location's services
   */
  suspend fun getDevices(locationId: String): List<Device> =
    withContext(Dispatchers.IO) {
      val locationDetails = getLocation(locationId)
      DeviceMapper.fromLocationResponse(locationDetails.included)
    }

  /**
   * Send a command to a device service.
   *
   * @param serviceId The ID of the service to send the command to
   * @param commandRequest The command request containing the command type and parameters
   */
  suspend fun sendCommand(serviceId: String, commandRequest: CommandRequest) {
    withContext(Dispatchers.IO) {
      val controlApi = ControlApi(getClient())
      controlApi.sendCommand(serviceId, commandRequest)
    }
  }

  /** Close the authentication client and clean up resources. */
  fun close() {
    authClient.close()
  }
}
