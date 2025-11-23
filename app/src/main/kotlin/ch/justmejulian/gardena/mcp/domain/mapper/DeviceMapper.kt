/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.mapper

import ch.justmejulian.gardena.mcp.domain.ServiceDataItem
import ch.justmejulian.gardena.mcp.domain.device.*
import com.gardena.smartgarden.service.iapi.generated.model.CommonServiceDataItem
import com.gardena.smartgarden.service.iapi.generated.model.LocationResponseIncludedInner
import com.gardena.smartgarden.service.iapi.generated.model.PowerSocketServiceDataItem
import com.gardena.smartgarden.service.iapi.generated.model.SensorServiceDataItem
import com.gardena.smartgarden.service.iapi.generated.model.ValveServiceDataItem
import com.gardena.smartgarden.service.iapi.generated.model.ValveSetServiceDataItem

/**
 * Maps Gardena API responses to domain Device instances.
 *
 * Processes the "included" section of LocationResponse which contains services, and groups them by
 * device ID to create unified Device objects.
 */
object DeviceMapper {

  /**
   * Wraps API response items into domain ServiceDataItem types.
   *
   * @param includedItems Raw API response items containing service data
   * @return List of wrapped ServiceDataItem instances
   */
  private fun wrapServices(
    includedItems: List<LocationResponseIncludedInner>
  ): List<ServiceDataItem> =
    includedItems.mapNotNull { item -> ServiceDataItem.wrap(item.actualInstance) }

  /**
   * Groups services by their device ID.
   *
   * Multiple services belong to the same physical device (identified by device ID). This function
   * groups them together for unified device creation.
   *
   * @param services List of wrapped service items
   * @return Map of device ID to list of services for that device
   */
  private fun groupByDeviceId(services: List<ServiceDataItem>): Map<String, List<ServiceDataItem>> =
    services.groupBy { it.deviceId }

  /**
   * Maps a list of included items (services) from the API response to Device instances. Groups
   * services by device ID and creates appropriate device types.
   *
   * @param includedItems The list of services from the API response
   * @return List of mapped Device instances
   */
  fun fromLocationResponse(includedItems: List<LocationResponseIncludedInner>?): List<Device> {
    if (includedItems == null) return emptyList()

    val services = wrapServices(includedItems)
    val servicesByDeviceId = groupByDeviceId(services)

    return servicesByDeviceId.mapNotNull { (deviceId, wrappedServices) ->
      if (deviceId.isEmpty()) return@mapNotNull null

      val commonService =
        wrappedServices.filterIsInstance<ServiceDataItem.Common>().firstOrNull()?.item

      // Pattern match on service types to find the first mappable device
      wrappedServices.firstNotNullOfOrNull { service ->
        when (service) {
          is ServiceDataItem.Sensor -> mapSensorDevice(deviceId, commonService, service.item)
          is ServiceDataItem.PowerSocket ->
            mapPowerSocketDevice(deviceId, commonService, service.item)
          is ServiceDataItem.ValveSet -> {
            val valveServices =
              wrappedServices.filterIsInstance<ServiceDataItem.Valve>().map { it.item }
            mapValveSetDevice(deviceId, commonService, service.item, valveServices)
          }
          else -> null
        }
      }
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

  private fun mapValveSetDevice(
    deviceId: String,
    commonService: CommonServiceDataItem?,
    valveSetService: ValveSetServiceDataItem,
    valveServices: List<ValveServiceDataItem>,
  ): ValveSetDevice {
    val commonAttrs = commonService?.attributes
    val valveSetAttrs = valveSetService.attributes

    val valves =
      valveServices.map { valveService ->
        val valveAttrs = valveService.attributes
        Valve(
          id = valveService.id ?: "",
          name = valveAttrs?.name?.value,
          state = valveAttrs?.state?.value?.value,
          activity = valveAttrs?.activity?.value?.value,
        )
      }

    return ValveSetDevice(
      id = deviceId,
      name = commonAttrs?.name?.value,
      batteryLevel = commonAttrs?.batteryLevel?.value,
      batteryState = commonAttrs?.batteryState?.value?.value,
      rfLinkLevel = commonAttrs?.rfLinkLevel?.value,
      rfLinkState = commonAttrs?.rfLinkState?.value?.value,
      serial = commonAttrs?.serial?.value,
      modelType = commonAttrs?.modelType?.value,
      valveSetState = valveSetAttrs?.state?.value?.value,
      valves = valves,
    )
  }
}
