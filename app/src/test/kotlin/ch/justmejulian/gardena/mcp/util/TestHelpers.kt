/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.util

import com.gardena.smartgarden.service.iapi.generated.model.*
import java.time.OffsetDateTime

/**
 * Creates a CommonServiceDataItem for testing purposes.
 *
 * @param deviceId The device ID to use
 * @param name The device name
 * @param serial The device serial number
 * @return A configured CommonServiceDataItem
 */
fun createCommonService(deviceId: String, name: String, serial: String): CommonServiceDataItem {
  val now = OffsetDateTime.now()

  val attrs =
    CommonService()
      .name(UserDefinedNameWrapper().value(name))
      .serial(SerialNumberWrapper().value(serial))
      .batteryLevel(TimestampedPercent().value(100).timestamp(now))
      .batteryState(TimestampedBatteryState().value(BatteryState.OK).timestamp(now))
      .rfLinkLevel(TimestampedPercent().value(80).timestamp(now))
      .rfLinkState(TimestampedRFLinkState().value(RFLinkState.ONLINE).timestamp(now))
      .modelType(DeviceModelWrapper().value("MODEL_TYPE"))

  return CommonServiceDataItem().id("$deviceId:common").attributes(attrs)
}

/**
 * Creates a SensorServiceDataItem for testing purposes.
 *
 * @param deviceId The device ID to use
 * @param soilHumidity Soil humidity percentage
 * @param soilTemp Soil temperature
 * @param ambientTemp Ambient temperature
 * @param light Light intensity
 * @return A configured SensorServiceDataItem
 */
fun createSensorService(
  deviceId: String,
  soilHumidity: Int,
  soilTemp: Int,
  ambientTemp: Int,
  light: Int,
): SensorServiceDataItem {
  val now = OffsetDateTime.now()

  val attrs =
    SensorService()
      .soilHumidity(TimestampedPercent().value(soilHumidity).timestamp(now))
      .soilTemperature(TimestampedTemperature().value(soilTemp).timestamp(now))
      .ambientTemperature(TimestampedTemperature().value(ambientTemp).timestamp(now))
      .lightIntensity(TimestampedLightIntensity().value(light).timestamp(now))

  return SensorServiceDataItem().id("$deviceId:sensor").attributes(attrs)
}

/**
 * Creates a PowerSocketServiceDataItem for testing purposes.
 *
 * @param deviceId The device ID to use
 * @param state Power socket state (OK, WARNING, ERROR, UNAVAILABLE)
 * @param duration Duration in seconds
 * @return A configured PowerSocketServiceDataItem
 */
fun createPowerSocketService(
  deviceId: String,
  state: ServiceState,
  duration: Int,
): PowerSocketServiceDataItem {
  val now = OffsetDateTime.now()

  val attrs =
    PowerSocketService()
      .state(TimestampedServiceState().value(state).timestamp(now))
      .duration(TimestampedSeconds().value(duration).timestamp(now))

  return PowerSocketServiceDataItem().id("$deviceId:power_socket").attributes(attrs)
}

/**
 * Creates a ValveSetServiceDataItem for testing purposes.
 *
 * @param deviceId The device ID to use
 * @param state Valve set state
 * @return A configured ValveSetServiceDataItem
 */
fun createValveSetService(deviceId: String, state: ServiceState): ValveSetServiceDataItem {
  val now = OffsetDateTime.now()

  val attrs = ValveSetService().state(TimestampedServiceState().value(state).timestamp(now))

  return ValveSetServiceDataItem().id("$deviceId:valve_set").attributes(attrs)
}

/**
 * Creates a ValveServiceDataItem for testing purposes.
 *
 * @param deviceId The device ID to use
 * @param valveIndex The valve index (1, 2, etc.)
 * @param name The valve name
 * @param state Valve state
 * @param activity Valve activity
 * @return A configured ValveServiceDataItem
 */
fun createValveService(
  deviceId: String,
  valveIndex: Int,
  name: String,
  state: ServiceState,
  activity: ValveActivity,
): ValveServiceDataItem {
  val now = OffsetDateTime.now()

  val attrs =
    ValveService()
      .name(UserDefinedNameWrapper().value(name))
      .state(TimestampedServiceState().value(state).timestamp(now))
      .activity(TimestampedValveActivity().value(activity).timestamp(now))

  return ValveServiceDataItem().id("$deviceId:valve:$valveIndex").attributes(attrs)
}
