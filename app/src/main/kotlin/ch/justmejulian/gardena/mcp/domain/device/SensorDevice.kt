/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.device

/**
 * Sensor device combining COMMON and SENSOR services. Provides environmental monitoring data like
 * soil humidity, temperature, and light intensity.
 */
data class SensorDevice(
  override val id: String,
  override val name: String? = null,
  override val batteryLevel: Int? = null,
  override val batteryState: String? = null,
  override val rfLinkLevel: Int? = null,
  override val rfLinkState: String? = null,
  override val serial: String? = null,
  override val modelType: String? = null,
  // SENSOR service attributes
  val soilHumidity: Int? = null,
  val soilTemperature: Int? = null,
  val ambientTemperature: Int? = null,
  val lightIntensity: Int? = null,
) : Device
