/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.service

import ch.justmejulian.gardena.mcp.domain.device.Device
import ch.justmejulian.gardena.mcp.domain.mapper.DeviceMapper
import com.gardena.smartgarden.service.iapi.generated.ApiClient
import com.gardena.smartgarden.service.iapi.generated.api.ControlApi
import com.gardena.smartgarden.service.iapi.generated.api.HealthCheckApi
import com.gardena.smartgarden.service.iapi.generated.api.SnapshotApi
import com.gardena.smartgarden.service.iapi.generated.model.CommandRequest
import com.gardena.smartgarden.service.iapi.generated.model.LocationResponse
import com.gardena.smartgarden.service.iapi.generated.model.LocationsResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Service for interacting with the Gardena Smart System API.
 *
 * Uses a pre-obtained OAuth bearer token for authentication and the provided API key for the
 * X-Api-Key header.
 */
class GardenaService(
  private val authorizationHeader: String,
  private val apiKey: String,
  private val apiBaseUrl: String = "https://api-test.smart.gardena.dev",
) {
  private val logger = KotlinLogging.logger {}

  private val apiClient: ApiClient =
    ApiClient().apply {
      updateBaseUri(apiBaseUrl)
      setRequestInterceptor { requestBuilder ->
        requestBuilder.header("Authorization", authorizationHeader)
        requestBuilder.header("X-Api-Key", apiKey)
        requestBuilder.header("Accept", "application/vnd.api+json")
      }
  }

  /** Check if the API is healthy and accessible. */
  suspend fun healthCheck(): Boolean =
    withContext(Dispatchers.IO) {
      try {
        val healthCheckApi = HealthCheckApi(apiClient)
        healthCheckApi.getHealth()
        true
      } catch (e: Exception) {
        logger.warn { "Health check failed: ${e.message}" }
        false
      }
    }

  /** Get all locations for the authenticated user. */
  suspend fun getLocations(): LocationsResponse =
    withContext(Dispatchers.IO) {
      logger.debug { "Fetching locations" }
      val snapshotApi = SnapshotApi(apiClient)
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
      logger.debug { "Fetching location $locationId" }
      val snapshotApi = SnapshotApi(apiClient)
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
   * Get specific device for a specific location.
   *
   * @param locationId The ID of the location to fetch devices for
   * @param deviceId
   * @return Device
   */
  suspend fun getDevice(locationId: String, deviceId: String): Device? =
    withContext(Dispatchers.IO) {
      val locationDetails = getLocation(locationId)
      val devices = DeviceMapper.fromLocationResponse(locationDetails.included)

      devices.find { it.id == deviceId }
    }

  /**
   * Send a command to a device service.
   *
   * @param serviceId The ID of the service to send the command to
   * @param commandRequest The command request containing the command type and parameters
   */
  suspend fun sendCommand(serviceId: String, commandRequest: CommandRequest) {
    withContext(Dispatchers.IO) {
      logger.info { "Sending command to service $serviceId: ${commandRequest.data}" }
      val controlApi = ControlApi(apiClient)
      controlApi.sendCommand(serviceId, commandRequest)
      logger.debug { "Command sent successfully to service $serviceId" }
    }
  }

  /** Close the authentication client and clean up resources. */
  fun close() {
   // TODO clean up resources
  }
}
