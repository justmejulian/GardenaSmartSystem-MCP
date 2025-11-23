/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.device

import ch.justmejulian.gardena.mcp.domain.command.Command

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
  override val supportedCommands: Map<String, Command> = emptyMap(),
) : Device {
  override fun toString(): String =
    buildString {
        appendLine("Device: ${name ?: "Unknown"}")
        appendLine("ID: $id")
        appendLine("Type: SensorDevice")
        serial?.let { appendLine("Serial: $it") }
        modelType?.let { appendLine("Model: $it") }
        batteryLevel?.let { appendLine("Battery: $it%") }
        batteryState?.let { appendLine("Battery State: $it") }
        rfLinkLevel?.let { appendLine("RF Link Level: $it") }
        rfLinkState?.let { appendLine("RF Link State: $it") }
        soilHumidity?.let { appendLine("Soil Humidity: $it%") }
        soilTemperature?.let { appendLine("Soil Temperature: ${it}°C") }
        ambientTemperature?.let { appendLine("Ambient Temperature: ${it}°C") }
        lightIntensity?.let { appendLine("Light Intensity: ${it} lux") }
      }
      .trimIndent()
}
