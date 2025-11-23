/*
 * Copyright (c) 2025 justmejulian
 * SPDX-License-Identifier: MIT
 */
package ch.justmejulian.gardena.mcp.domain.device

import ch.justmejulian.gardena.mcp.domain.command.Command
import ch.justmejulian.gardena.mcp.domain.command.ValveCommand
import ch.justmejulian.gardena.mcp.domain.command.ValveSetCommand

/**
 * Valve device representing a single valve in a valve set. Contains valve-specific attributes like
 * state.
 */
data class Valve(
  val id: String,
  val name: String? = null,
  val state: String? = null,
  val activity: String? = null,
) {
  val supportedCommands: Map<String, Command>
    get() = ValveCommand.commands

  override fun toString(): String = buildString {
    append("  - ${name ?: "Unknown"} (ID: $id)")
    state?.let { append(", State: $it") }
    activity?.let { append(", Activity: $it") }
  }
}

/**
 * Valve set device combining VALVE_SET, COMMON, and multiple VALVE services. Manages irrigation
 * control with multiple individual valves.
 */
data class ValveSetDevice(
  override val id: String,
  override val name: String? = null,
  override val batteryLevel: Int? = null,
  override val batteryState: String? = null,
  override val rfLinkLevel: Int? = null,
  override val rfLinkState: String? = null,
  override val serial: String? = null,
  override val modelType: String? = null,
  // VALVE_SET service attributes
  val valveSetState: String? = null,
  // List of individual valves
  val valves: List<Valve> = emptyList(),
) : Device {
  val supportedCommands: Map<String, Command>
    get() = ValveSetCommand.commands

  override fun toString(): String =
    buildString {
        appendLine("Device: ${name ?: "Unknown"}")
        appendLine("ID: $id")
        appendLine("Type: ValveSetDevice")
        serial?.let { appendLine("Serial: $it") }
        modelType?.let { appendLine("Model: $it") }
        batteryLevel?.let { appendLine("Battery: $it%") }
        batteryState?.let { appendLine("Battery State: $it") }
        rfLinkLevel?.let { appendLine("RF Link Level: $it") }
        rfLinkState?.let { appendLine("RF Link State: $it") }
        valveSetState?.let { appendLine("Valve Set State: $it") }
        if (valves.isNotEmpty()) {
          appendLine("Valves:")
          valves.forEach { appendLine(it.toString()) }
        }
      }
      .trimIndent()
}
