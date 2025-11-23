/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain

import com.gardena.smartgarden.service.iapi.generated.model.CommonServiceDataItem
import com.gardena.smartgarden.service.iapi.generated.model.PowerSocketServiceDataItem
import com.gardena.smartgarden.service.iapi.generated.model.SensorServiceDataItem
import com.gardena.smartgarden.service.iapi.generated.model.ValveServiceDataItem
import com.gardena.smartgarden.service.iapi.generated.model.ValveSetServiceDataItem

/**
 * Wrapper for service data items that provides a unified interface for accessing common properties.
 */
sealed class ServiceDataItem(val instance: Any) {
  abstract val deviceId: String

  data class Common(val item: CommonServiceDataItem) : ServiceDataItem(item) {
    override val deviceId: String
      get() = (item.id ?: "").substringBefore(":")
  }

  data class Sensor(val item: SensorServiceDataItem) : ServiceDataItem(item) {
    override val deviceId: String
      get() = (item.id ?: "").substringBefore(":")
  }

  data class PowerSocket(val item: PowerSocketServiceDataItem) : ServiceDataItem(item) {
    override val deviceId: String
      get() = (item.id ?: "").substringBefore(":")
  }

  data class ValveSet(val item: ValveSetServiceDataItem) : ServiceDataItem(item) {
    override val deviceId: String
      get() = (item.id ?: "").substringBefore(":")
  }

  data class Valve(val item: ValveServiceDataItem) : ServiceDataItem(item) {
    override val deviceId: String
      get() = (item.id ?: "").substringBefore(":")
  }

  companion object {
    fun wrap(instance: Any): ServiceDataItem? =
      when (instance) {
        is CommonServiceDataItem -> Common(instance)
        is SensorServiceDataItem -> Sensor(instance)
        is PowerSocketServiceDataItem -> PowerSocket(instance)
        is ValveSetServiceDataItem -> ValveSet(instance)
        is ValveServiceDataItem -> Valve(instance)
        else -> null
      }
  }
}
