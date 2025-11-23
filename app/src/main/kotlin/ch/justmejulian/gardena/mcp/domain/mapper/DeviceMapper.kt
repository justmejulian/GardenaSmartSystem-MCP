/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.mapper

import ch.justmejulian.gardena.mcp.domain.device.*
import com.gardena.smartgarden.service.iapi.generated.model.CommonServiceDataItem
import com.gardena.smartgarden.service.iapi.generated.model.LocationResponseIncludedInner
import com.gardena.smartgarden.service.iapi.generated.model.PowerSocketServiceDataItem
import com.gardena.smartgarden.service.iapi.generated.model.SensorServiceDataItem

/**
 * Maps Gardena API responses to domain Device instances.
 *
 * Processes the "included" section of LocationResponse which contains services, and groups them by
 * device ID to create unified Device objects.
 */
object DeviceMapper {
  /**
   * Maps a list of included items (services) from the API response to Device instances. Groups
   * services by device ID and creates appropriate device types.
   *
   * @param includedItems The list of services from the API response
   * @return List of mapped Device instances
   */
  fun fromLocationResponse(includedItems: List<LocationResponseIncludedInner>?): List<Device> {
    if (includedItems == null) return emptyList()

    val services = extractServices(includedItems)
    val servicesByDeviceId = groupServicesByDeviceId(services)

    return servicesByDeviceId.mapNotNull { (deviceId, serviceInstances) ->
      if (deviceId.isEmpty()) return@mapNotNull null

      // Find specific service types
      val commonService = serviceInstances.filterIsInstance<CommonServiceDataItem>().firstOrNull()
      val sensorService = serviceInstances.filterIsInstance<SensorServiceDataItem>().firstOrNull()
      val powerSocketService =
          serviceInstances.filterIsInstance<PowerSocketServiceDataItem>().firstOrNull()

      // Map to appropriate device type
      when {
        sensorService != null -> mapSensorDevice(deviceId, commonService, sensorService)
        powerSocketService != null ->
            mapPowerSocketDevice(deviceId, commonService, powerSocketService)
        else -> null
      }
    }
  }

  private fun groupServicesByDeviceId(services: List<Pair<Any, String>>): Map<String, List<Any>> =
      services.groupBy({ it.second }, { it.first })

  private fun getDeviceId(instance: Any): String =
      when (instance) {
        is CommonServiceDataItem -> (instance.id ?: "").substringBefore(":")
        is SensorServiceDataItem -> (instance.id ?: "").substringBefore(":")
        is PowerSocketServiceDataItem -> (instance.id ?: "").substringBefore(":")
        else -> ""
      }

  private fun extractServices(
      includedItems: List<LocationResponseIncludedInner>
  ): List<Pair<Any, String>> =
      includedItems.mapNotNull { item ->
        val instance = item.actualInstance
        when (instance) {
          is CommonServiceDataItem -> instance to getDeviceId(instance)
          is SensorServiceDataItem -> instance to getDeviceId(instance)
          is PowerSocketServiceDataItem -> instance to getDeviceId(instance)
          else -> null
        }
      }

  private fun mapSensorDevice(
      deviceId: String,
      commonService: CommonServiceDataItem?,
      sensorService: SensorServiceDataItem,
  ): SensorDevice {
    val commonAttrs = commonService?.attributes
    val sensorAttrs = sensorService.attributes

    return SensorDevice(
        id = deviceId,
        name = commonAttrs?.name?.value,
        batteryLevel = commonAttrs?.batteryLevel?.value,
        batteryState = commonAttrs?.batteryState?.value?.value,
        rfLinkLevel = commonAttrs?.rfLinkLevel?.value,
        rfLinkState = commonAttrs?.rfLinkState?.value?.value,
        serial = commonAttrs?.serial?.value,
        modelType = commonAttrs?.modelType?.value,
        soilHumidity = sensorAttrs?.soilHumidity?.value,
        soilTemperature = sensorAttrs?.soilTemperature?.value,
        ambientTemperature = sensorAttrs?.ambientTemperature?.value,
        lightIntensity = sensorAttrs?.lightIntensity?.value,
    )
  }

  private fun mapPowerSocketDevice(
      deviceId: String,
      commonService: CommonServiceDataItem?,
      powerSocketService: PowerSocketServiceDataItem,
  ): PowerSocketDevice {
    val commonAttrs = commonService?.attributes
    val powerSocketAttrs = powerSocketService.attributes

    return PowerSocketDevice(
        id = deviceId,
        name = commonAttrs?.name?.value,
        batteryLevel = commonAttrs?.batteryLevel?.value,
        batteryState = commonAttrs?.batteryState?.value?.value,
        rfLinkLevel = commonAttrs?.rfLinkLevel?.value,
        rfLinkState = commonAttrs?.rfLinkState?.value?.value,
        serial = commonAttrs?.serial?.value,
        modelType = commonAttrs?.modelType?.value,
        state = powerSocketAttrs?.state?.value?.value,
        duration = powerSocketAttrs?.duration?.value,
    )
  }
}
