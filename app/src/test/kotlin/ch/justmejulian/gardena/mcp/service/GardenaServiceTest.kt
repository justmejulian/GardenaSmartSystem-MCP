/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.service

import ch.justmejulian.gardena.mcp.domain.device.SensorDevice
import ch.justmejulian.gardena.mcp.util.createCommonService
import ch.justmejulian.gardena.mcp.util.createSensorService
import com.gardena.smartgarden.service.iapi.generated.model.LocationResponse
import com.gardena.smartgarden.service.iapi.generated.model.LocationResponseIncludedInner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GardenaServiceTest {

  @Test
  fun `getDevices returns empty list when location has no included items`() {
    // Note: This is a unit test for the mapping logic only
    // We cannot easily test the actual API calls without mocking the generated API client

    // Simulate what getDevices would do with an empty location response
    val locationResponse = LocationResponse()
    val devices = ch.justmejulian.gardena.mcp.domain.mapper.DeviceMapper.fromLocationResponse(null)

    assertTrue(devices.isEmpty())
  }

  @Test
  fun `getDevices maps location response to devices correctly`() {
    // Test the mapping logic that getDevices uses
    val deviceId = "test-sensor-1"
    val common = createCommonService(deviceId, "Test Sensor", "SN001")
    val sensor = createSensorService(deviceId, 70, 25, 20, 2000)

    val includedItems =
      listOf(LocationResponseIncludedInner(common), LocationResponseIncludedInner(sensor))

    // Simulate the mapping that happens in getDevices
    val devices =
      ch.justmejulian.gardena.mcp.domain.mapper.DeviceMapper.fromLocationResponse(includedItems)

    assertEquals(1, devices.size)
    val device = devices[0] as SensorDevice
    assertEquals(deviceId, device.id)
    assertEquals("Test Sensor", device.name)
    assertEquals(70, device.soilHumidity)
  }

  @Test
  fun `GardenaService can be instantiated with default URLs`() {
    val service = GardenaService("test-client-id", "test-client-secret")

    // Just verify the service can be created
    // We don't call any methods that would trigger authentication
    service.close()
  }

  @Test
  fun `GardenaService can be instantiated with custom URLs`() {
    val customAuthUrl = "https://custom-auth.example.com"
    val customApiUrl = "https://custom-api.example.com"

    val service =
      GardenaService(
        clientId = "test-client-id",
        clientSecret = "test-client-secret",
        authBaseUrl = customAuthUrl,
        apiBaseUrl = customApiUrl,
      )

    // Verify the service can be created with custom URLs
    service.close()
  }
}
