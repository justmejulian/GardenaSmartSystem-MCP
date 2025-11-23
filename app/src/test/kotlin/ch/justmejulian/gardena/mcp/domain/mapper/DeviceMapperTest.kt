/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.mapper

import ch.justmejulian.gardena.mcp.domain.device.PowerSocketDevice
import ch.justmejulian.gardena.mcp.domain.device.SensorDevice
import ch.justmejulian.gardena.mcp.domain.device.ValveSetDevice
import ch.justmejulian.gardena.mcp.util.*
import com.gardena.smartgarden.service.iapi.generated.model.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeviceMapperTest {

  @Test
  fun `fromLocationResponse returns empty list when input is null`() {
    val result = DeviceMapper.fromLocationResponse(null)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `fromLocationResponse returns empty list when input is empty`() {
    val result = DeviceMapper.fromLocationResponse(emptyList())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `fromLocationResponse skips devices with empty device ID`() {
    val commonService = createCommonService("", "No ID Device", "99999")
    val sensorService = createSensorService("", 50, 20, 15, 1000)

    val includedItems =
      listOf(
        LocationResponseIncludedInner(commonService),
        LocationResponseIncludedInner(sensorService),
      )

    val devices = DeviceMapper.fromLocationResponse(includedItems)

    assertTrue(devices.isEmpty())
  }

  @Test
  fun `fromLocationResponse groups services by device ID`() {
    // Create two separate devices with their services
    val common1 = createCommonService("device-1", "Sensor 1", "111")
    val sensor1 = createSensorService("device-1", 50, 20, 15, 1000)
    val common2 = createCommonService("device-2", "Sensor 2", "222")
    val sensor2 = createSensorService("device-2", 60, 25, 18, 1200)

    val includedItems =
      listOf(
        LocationResponseIncludedInner(common1),
        LocationResponseIncludedInner(sensor1),
        LocationResponseIncludedInner(common2),
        LocationResponseIncludedInner(sensor2),
      )

    val devices = DeviceMapper.fromLocationResponse(includedItems)

    // Should create 2 separate devices
    assertEquals(2, devices.size)
    assertEquals("device-1", devices[0].id)
    assertEquals("device-2", devices[1].id)
  }

  @Test
  fun `fromLocationResponse maps SensorDevice correctly`() {
    val deviceId = "sensor-device-1"
    val common = createCommonService(deviceId, "My Sensor", "SN12345")
    val sensor = createSensorService(deviceId, 65, 22, 18, 1500)

    val includedItems =
      listOf(LocationResponseIncludedInner(common), LocationResponseIncludedInner(sensor))

    val devices = DeviceMapper.fromLocationResponse(includedItems)

    assertEquals(1, devices.size)
    val device = devices[0] as SensorDevice
    assertEquals(deviceId, device.id)
    assertEquals("My Sensor", device.name)
    assertEquals("SN12345", device.serial)
    assertEquals(65, device.soilHumidity)
    assertEquals(22, device.soilTemperature)
    assertEquals(18, device.ambientTemperature)
    assertEquals(1500, device.lightIntensity)
  }

  @Test
  fun `fromLocationResponse maps PowerSocketDevice correctly`() {
    val deviceId = "power-socket-1"
    val common = createCommonService(deviceId, "My Power Socket", "PS98765")
    val powerSocket = createPowerSocketService(deviceId, ServiceState.OK, 3600)

    val includedItems =
      listOf(LocationResponseIncludedInner(common), LocationResponseIncludedInner(powerSocket))

    val devices = DeviceMapper.fromLocationResponse(includedItems)

    assertEquals(1, devices.size)
    val device = devices[0] as PowerSocketDevice
    assertEquals(deviceId, device.id)
    assertEquals("My Power Socket", device.name)
    assertEquals("PS98765", device.serial)
    assertEquals("OK", device.state)
    assertEquals(3600, device.duration)
  }

  @Test
  fun `fromLocationResponse maps ValveSetDevice correctly`() {
    val deviceId = "valve-set-1"
    val common = createCommonService(deviceId, "My Valve Set", "VS55555")
    val valveSet = createValveSetService(deviceId, ServiceState.OK)
    val valve1 = createValveService(deviceId, 1, "Valve 1", ServiceState.OK, ValveActivity.CLOSED)
    val valve2 =
      createValveService(deviceId, 2, "Valve 2", ServiceState.OK, ValveActivity.MANUAL_WATERING)

    val includedItems =
      listOf(
        LocationResponseIncludedInner(common),
        LocationResponseIncludedInner(valveSet),
        LocationResponseIncludedInner(valve1),
        LocationResponseIncludedInner(valve2),
      )

    val devices = DeviceMapper.fromLocationResponse(includedItems)

    assertEquals(1, devices.size)
    val device = devices[0] as ValveSetDevice
    assertEquals(deviceId, device.id)
    assertEquals("My Valve Set", device.name)
    assertEquals("VS55555", device.serial)
    assertEquals("OK", device.valveSetState)
    assertEquals(2, device.valves.size)
    assertEquals("Valve 1", device.valves[0].name)
    assertEquals("OK", device.valves[0].state)
    assertEquals("Valve 2", device.valves[1].name)
    assertEquals("OK", device.valves[1].state)
  }
}
